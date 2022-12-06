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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Each RuleSet has a name a list of rules.
 */
public class RuleSet {
	private String name;
	private List<Rule> rules;
	final static ObjectMapper MAPPER = new ObjectMapper();

	public RuleSet(final String name) {
		this.name = name;
		rules = new ArrayList<>();
	}

	/**
	 * Add a Rule to the RuleSet.
	 * @param rule The Rule to add to the RuleSet.
	 */
	public void add(final Rule rule) {
		rules.add(rule);
	}

	public boolean nonEmpty() {
		return rules.size() != 0;
	}

	public ObjectNode asJSON() {
		final ObjectNode ruleSet = MAPPER.createObjectNode();

		ruleSet.put("name", name);
		final ArrayNode rulesNode = ruleSet.putArray("rules");

		for (final Rule rule : rules)
			rulesNode.add(rule.asJSON());

		return ruleSet;
	}

	private String fmt(final String input, final String type) {
		if ("Long".equals(type) || "Double".equals(type))
			return input;

		return "\"" + input + "\"";
	}

	public String asDQDL() {
		final StringBuilder b = new StringBuilder();
		String type = null;

		for (final Rule rule : rules)
			switch (rule.getName()) {
			case "BaseType":
				type = rule.getArguments()[0];
				break;
			case "Unique":
				b.append("IsUnique \"" + name + "\", ");
				break;
			case "NullPercent":
				b.append("IsComplete \"" + name + "\", ");
				break;
			case "Min":
				b.append("ColumnValues \"" + name + "\" >= " + fmt(rule.getArguments()[0], type) + ", ");
				break;
			case "Max":
				b.append("ColumnValues \"" + name + "\" <= " + fmt(rule.getArguments()[0], type) + ", ");
				break;
			case "OneOf":
				b.append("ColumnValues \"" + name + "\" in [");
				for (int i = 0; i < rule.getArguments().length; i++) {
					if (i != 0)
						b.append(", ");
					b.append(fmt(rule.getArguments()[i], type));
				}
				b.append("]");
				break;
			default:
				b.append(" ");
				break;
			}

		return b.toString();
	}

	public String getName() {
		return name;
	}

	public List<Rule> getRules() {
		return rules;
	}
}
