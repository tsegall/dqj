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

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.cobber.fta.core.FTAType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JSONProfile implements ProfileMetrics {
	private String fieldName;
	private long totalCount;
	private long sampleCount;
	private long matchCount;
	private long nullCount;
	private long blankCount;
	private long distinctCount;
	private String regExp;
	private double confidence;
	private String type;
	private boolean isSemanticType;
	private String semanticType;
	private String typeModifier;
	private String min;
	private String max;
	private int minLength;
	private int maxLength;
	private double mean;
	private double standardDeviation;
	private String decimalSeparator;
	private String[] topK;
	private String[] bottomK;
	private int cardinality;
	private DetailEntry[] cardinalityDetail;
	private int outlierCardinality;
	private DetailEntry[] outlierDetail;
	private int invalidCardinality;
	private int shapesCardinality;
	private DetailEntry[] shapesDetail;
	private long leadingZeroCount;
	private String[] percentiles;
	private long[] histogram;
	private boolean leadingWhiteSpace;
	private boolean trailingWhiteSpace;
	private boolean multiline;
	private double keyConfidence;
	private double uniqueness;
	private String detectionLocale;
	private String dateResolutionMode;
	private String ftaVersion;
	private String structureSignature;
	private String dataSignature;
	private long totalNullCount;
	private long totalBlankCount;

	@Override
	public String getName() {
		return fieldName;
	}

	@Override
	public long getNullCount() {
		return nullCount;
	}

	@Override
	public long getBlankCount() {
		return blankCount;
	}

	@Override
	public String getMinValue() {
		return min;
	}

	@Override
	public String getMaxValue() {
		return max;
	}

	@Override
	public int getMinLength() {
		return minLength;
	}

	@Override
	public int getMaxLength() {
		return maxLength;
	}

	@Override
	public boolean getLeadingWhiteSpace() {
		return leadingWhiteSpace;
	}

	@Override
	public boolean getTrailingWhiteSpace() {
		return trailingWhiteSpace;
	}

	@Override
	public double getUniqueness() {
		return uniqueness;
	}

	@Override
	public boolean isSemanticType() {
		return isSemanticType;
	}

	@Override
	public long getTotalNullCount() {
		return totalNullCount;
	}

	@Override
	public long getTotalBlankCount() {
		return totalBlankCount;
	}

	@Override
	public int getCardinality() {
		return cardinality;
	}

	@Override
	public String getRegExp() {
		return regExp;
	}

	@Override
	public String getTypeModifier() {
		return typeModifier;
	}

	@Override
	public FTAType getType() {
		return FTAType.valueOf(type.toUpperCase(Locale.ROOT));
	}

	@Override
	public String getSemanticType() {
		return semanticType;
	}

	@Override
	public Map<String, Long> getCardinalityDetails() {
		return Arrays.stream(cardinalityDetail)
		        .collect(Collectors.toMap(d -> d.key, d -> d.count));
	}
}
