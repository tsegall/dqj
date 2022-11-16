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

import java.util.Map;

import com.cobber.fta.TextAnalysisResult;
import com.cobber.fta.core.FTAType;

public class ProxyProfile implements ProfileMetrics {
	private TextAnalysisResult proxy;

	public ProxyProfile(TextAnalysisResult proxy) {
		this.proxy = proxy;
	}

	@Override
	public String getName() {
		return proxy.getName();
	}

	@Override
	public long getNullCount() {
		return proxy.getNullCount();
	}

	@Override
	public long getBlankCount() {
		return proxy.getBlankCount();
	}

	@Override
	public String getMinValue() {
		return proxy.getMinValue();
	}

	@Override
	public String getMaxValue() {
		return proxy.getMaxValue();
	}

	@Override
	public int getMinLength() {
		return proxy.getMinLength();
	}

	@Override
	public int getMaxLength() {
		return proxy.getMaxLength();
	}

	@Override
	public boolean getLeadingWhiteSpace() {
		return proxy.getLeadingWhiteSpace();
	}

	@Override
	public boolean getTrailingWhiteSpace() {
		return proxy.getTrailingWhiteSpace();
	}

	@Override
	public double getUniqueness() {
		return proxy.getUniqueness();
	}

	@Override
	public boolean isSemanticType() {
		return proxy.isSemanticType();
	}

	@Override
	public long getTotalNullCount() {
		return proxy.getTotalBlankCount();
	}

	@Override
	public long getTotalBlankCount() {
		return proxy.getTotalBlankCount();
	}

	@Override
	public int getCardinality() {
		return proxy.getCardinality();
	}

	@Override
	public String getRegExp() {
		return proxy.getRegExp();
	}

	@Override
	public String getTypeModifier() {
		return proxy.getTypeModifier();
	}

	@Override
	public FTAType getType() {
		return proxy.getType();
	}

	@Override
	public String getSemanticType() {
		return proxy.getSemanticType();
	}

	@Override
	public Map<String, Long> getCardinalityDetails() {
		return proxy.getCardinalityDetails();
	}
}
