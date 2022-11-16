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

import com.cobber.fta.core.FTAType;

public interface ProfileMetrics {
	public String getName();
	public long getNullCount();
	public long getBlankCount();
	public String getMinValue();
	public String getMaxValue();
	public int getMinLength();
	public int getMaxLength();
	public boolean getLeadingWhiteSpace();
	public boolean getTrailingWhiteSpace();
	public double getUniqueness();
	public boolean isSemanticType();
	public long getTotalNullCount();
	public long getTotalBlankCount();
	public int getCardinality();
	public String getRegExp();
	public String getTypeModifier();
	public FTAType getType();
	public String getSemanticType();
	public Map<String, Long> getCardinalityDetails();

	/*
	 * A skeleton implementation of Rule Generation.
	 */
	default RuleSet generateRuleSet() {
		RuleSet ruleSet = new RuleSet(getName());

		FTAType ftaType = getType();

		ruleSet.add(new Rule("BaseType", ftaType.toString()));
		if (getNullCount() == 0 && (getTotalNullCount() <= 0))
			ruleSet.add(new Rule("NullPercent", "0.0"));
		if (getBlankCount() == 0 && (getTotalBlankCount() <= 0))
			ruleSet.add(new Rule("BlankPercent", "0.0"));
		if (getLeadingWhiteSpace())
			ruleSet.add(new Rule("TrimLeft", "true"));
		if (getTrailingWhiteSpace())
			ruleSet.add(new Rule("TrimRight", "true"));
		if (getUniqueness() == 1.0)
			ruleSet.add(new Rule("Unique"));
		if (isSemanticType())
			ruleSet.add(new Rule("SemanticType", getSemanticType()));
		else {
			switch (ftaType) {
			case BOOLEAN:
				break;
			case STRING:
				if (getCardinality() != 0 && getCardinality() < 10)
					ruleSet.add(new Rule("OneOf", getCardinalityDetails().keySet().toArray(new String [getCardinalityDetails().size()])));
				else
					ruleSet.add(new Rule("Pattern", getRegExp()));
				break;
			case DOUBLE:
			case LONG:
				ruleSet.add(new Rule("Pattern", getRegExp()));
				ruleSet.add(new Rule("Min", getMinValue()));
				ruleSet.add(new Rule("Max", getMaxValue()));
				break;
			case LOCALDATE:
			case LOCALDATETIME:
			case LOCALTIME:
			case OFFSETDATETIME:
			case ZONEDDATETIME:
				ruleSet.add(new Rule("Format", getTypeModifier()));
				ruleSet.add(new Rule("Min", getMinValue()));
				ruleSet.add(new Rule("Max", getMaxValue()));
				break;
			default:
				break;
			}
		}

		return ruleSet;
	}
}
