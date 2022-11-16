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

import com.cobber.fta.AnalysisConfig;
import com.cobber.fta.LogicalType;
import com.cobber.fta.LogicalTypeFactory;
import com.cobber.fta.PluginDefinition;
import com.cobber.fta.core.FTAPluginException;
import com.univocity.parsers.common.TextParsingException;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

/*
 * A ***rudimentary*** example of how to validate using the previously computed RuleSets .
 */
public class Quality {
	public static void execute(final String filename, final ArrayList<RuleSet> allRules, final DriverOptions options) throws FTAPluginException {
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
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)), StandardCharsets.UTF_8))) {

			final CsvParser parser = new CsvParser(settings);
			parser.beginParsing(in);

			header = parser.getRecordMetadata().headers();
			if (header == null) {
				System.err.printf("ERROR: Cannot parse header for file '%s'%n", filename);
				System.exit(1);
			}
			numFields = header.length;
			int col = -1;
			for (int i = 0; i < numFields; i++) {
				if (options.field != null && options.field.equals(header[i]))
					col = i;
			}

			String[] row;

			while ((row = parser.parseNext()) != null) {
				thisRecord++;
				if (row.length != numFields) {
					System.err.printf("ERROR: Record %d has %d fields, expected %d, skipping%n",
							thisRecord, row.length, numFields);
					continue;
				}
				for (int i = 0; i < numFields; i++) {
					if (!check(allRules.get(col == -1 ? i : 0), row[i]))
						System.err.printf("Error in field '%s'(%d) on line %d, content: '%s'%n", header[i], i, thisRecord, row[i]);
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
	}

	private static boolean check(final RuleSet ruleSet, final String input) throws FTAPluginException {
		for (final Rule rule : ruleSet.getRules()) {
			if (rule.getName().equals("NullPercent") && input == null)
				return false;
			else if (rule.getName().equals("OneOf") && input != null) {
				String[] validMembers = rule.getArguments();
				for (String member : validMembers) {
					if (input.equalsIgnoreCase(member))
						return true;
				}
				return false;
			}
			else if (rule.getName().equals("SemanticType") && input != null && !input.trim().isEmpty()) {
				String[] types = rule.getArguments();
				PluginDefinition defn = PluginDefinition.findByQualifier(types[0]);
				if (defn != null) {
					LogicalType logical = LogicalTypeFactory.newInstance(defn, new AnalysisConfig());
						return logical.isValid(input);
				}
			}
		}

		return true;
	}
}
