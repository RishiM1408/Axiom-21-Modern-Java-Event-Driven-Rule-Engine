package com.axiom.v21.domain;

import java.util.List;

/**
 * A container record to hold a list of rules.
 * Used for the GlobalKTable value to allow broadcasting a full set of rules.
 */
public record RuleContainer(List<Rule> rules) {
}
