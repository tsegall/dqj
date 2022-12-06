/*
 * Copyright 2022 Tim Segall
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cobber.dqj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.cobber.fta.AnalyzerContext;
import com.cobber.fta.TextAnalyzer;
import com.cobber.fta.core.FTAPluginException;
import com.cobber.fta.core.FTAUnsupportedLocaleException;
import com.cobber.fta.core.InternalErrorException;
import com.cobber.fta.dates.DateTimeParser.DateResolutionMode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.univocity.parsers.common.TextParsingException;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

public class Driver {
	private static ObjectMapper mapper = new ObjectMapper();

	public static void main(final String[] args) throws JsonProcessingException, FTAPluginException, FTAUnsupportedLocaleException {
		DriverOptions options = new DriverOptions();
		String specificationFile = null;
		boolean quality = false;
		int idx = 0;

		while (idx < args.length && args[idx].charAt(0) == '-') {
			if ("--help".equals(args[idx])) {
				System.err.println("Usage: dqj [OPTIONS] [<data file>]");
				System.err.println("Valid OPTIONS are:");
				System.err.println(" --field <field name> - choose only a single field to process");
				System.err.println(" --format Native|Glue - select format output (default: Native)");
				System.err.println(" --quality - Execute Quality checks");
				System.err.println(" --specification <specification file> - Supply a JSON specification file");
				System.err.println(" --verbose - output additional debugging information");
				System.exit(1);
			}
			if ("--field".equals(args[idx]))
				options.field = args[++idx];
			if ("--format".equals(args[idx]))
				options.format = args[++idx];
			if ("--quality".equals(args[idx]))
				quality = true;
			if ("--specification".equals(args[idx]))
				specificationFile = args[++idx];
			else if ("--verbose".equals(args[idx]))
				options.verbose = true;
			idx++;
		}

		ArrayList<RuleSet> allRuleSets = null;
		// If we were supplied a Specification file then use it to generate the rules, otherwise generate them from the first <n> lines of the data file
		if (specificationFile == null) {
			if (idx == args.length) {
				System.err.println("Require either a Specification file or a data file.");
				System.exit(1);
			}
			allRuleSets = generateRuleSetsFromDataFile(args[idx], options);
		}
		else
			allRuleSets = generateRuleSetsFromSpecification(specificationFile, options);


		if (options.verbose || !quality) {
			if (options.format == null || options.format.equalsIgnoreCase("native")) {
				for (final RuleSet rules : allRuleSets)
					if (rules.nonEmpty())
						System.err.printf("%s%n", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rules.asJSON()));
			}
			else if (options.format.equalsIgnoreCase("glue")) {
				System.err.println("Rules = [");
				// Dump all the Rule Sets based on the format requested
				for (final RuleSet rules : allRuleSets)
					if (rules.nonEmpty())
						System.err.printf("%s%n", rules.asDQDL());

			}
		}

		if (quality)
			Quality.execute(args[idx++], allRuleSets, options);
	}

	public static ArrayList<RuleSet> generateRuleSets(ProfileMetrics[] profiles, final DriverOptions options) {
		ArrayList<RuleSet> allRules = new ArrayList<>();
		for (final ProfileMetrics profile : profiles) {
			if (options.field != null && options.field.equals(profile.getName()))
				continue;
			allRules.add(profile.generateRuleSet());
		}

		return allRules;
	}

	/*
	 * Build a RuleSet from the FTA JSON output.
	 */
	private static ArrayList<RuleSet> generateRuleSetsFromSpecification(final String filename, final DriverOptions options) {
		try (BufferedReader JSON = new BufferedReader(
				new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {
			return generateRuleSets(mapper.readValue(JSON, JSONProfile[].class), options);
		} catch (Exception e) {
			throw new InternalErrorException("Issues with supplied specification file", e);
		}
	}

	/*
	 * Build a RuleSet by analyzing the first <n> rows of a file.
	 */
	private static ArrayList<RuleSet> generateRuleSetsFromDataFile(final String filename, final DriverOptions options) throws FTAPluginException, FTAUnsupportedLocaleException {
		final CsvParserSettings settings = new CsvParserSettings();

		settings.setHeaderExtractionEnabled(true);
		settings.detectFormatAutomatically();
		settings.setLineSeparatorDetectionEnabled(true);
		settings.setIgnoreLeadingWhitespaces(false);
		settings.setIgnoreTrailingWhitespaces(false);
		settings.setEmptyValue("");
		settings.setDelimiterDetectionEnabled(true, ',', '\t', '|', ';');

		String[] header = null;
		int numFields = 0;
		long thisRecord = 0;
		TextAnalyzer[] analyzers = null;

		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)), StandardCharsets.UTF_8))) {
			final CsvParser parser = new CsvParser(settings);
			parser.beginParsing(in);

			header = parser.getRecordMetadata().headers();
			if (header == null) {
				System.err.printf("ERROR: Cannot parse header for file '%s'%n", filename);
				System.exit(1);
			}
			numFields = header.length;
			analyzers = new TextAnalyzer[numFields];

			for (int i = 0; i < numFields; i++)
				analyzers[i] = new TextAnalyzer(new AnalyzerContext(header[i] == null ? "" : header[i].trim(),
						DateResolutionMode.Auto, filename, header));

			String[] row;

			while ((row = parser.parseNext()) != null) {
				thisRecord++;
				// Use the first 100 rows to build the Semantic analysis
				if (thisRecord == 100)
					break;
				if (row.length != numFields) {
					System.err.printf("ERROR: Record %d has %d fields, expected %d, skipping%n",
							thisRecord, row.length, numFields);
					continue;
				}
				for (int i = 0; i < numFields; i++) {
					analyzers[i].train(row[i]);
				}
			}
		}
		catch (FileNotFoundException e) {
			System.err.printf("ERROR: Filename '%s' not found.%n", filename);
			System.exit(1);
		}
		catch (TextParsingException|java.lang.ArrayIndexOutOfBoundsException e) {
			System.err.printf("ERROR: Filename '%s' Univocity exception. %s%n", filename, e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			System.err.printf("ERROR: Filename '%s' IOException. %s%n", filename, e.getMessage());
			System.exit(1);
		}

		ProxyProfile[] results = new ProxyProfile[numFields];

		for (int i = 0; i < numFields; i++)
			results[i] = new ProxyProfile(analyzers[i].getResult());

		return generateRuleSets(results, options);
	}
}
