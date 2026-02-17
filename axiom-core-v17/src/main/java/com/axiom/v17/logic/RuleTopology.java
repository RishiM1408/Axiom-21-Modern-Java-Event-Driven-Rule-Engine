package com.axiom.v17.logic;

import com.axiom.v17.domain.*;

import java.util.List;

public class RuleTopology {

    public void evaluate(Transaction tx, List<Rule> rules) {
        if (rules == null)
            return;

        for (Rule rule : rules) {
            // Java 17: We have Switch Expressions (JEP 361), but pattern matching for
            // switch is Preview/Not standard yet.
            // We usually still use instanceof pattern matching (JEP 394).

            boolean passed = true;

            if (rule instanceof ThresholdRule tr) {
                // JEP 394: Pattern Matching for instanceof (Java 16)
                // No need to cast manually: 'tr' is already available.
                if (tx.amount().compareTo(tr.maxAmount()) > 0) {
                    passed = false;
                }
            } else if (rule instanceof LocationRule lr) {
                if (!lr.allowedRegions().contains(tx.merchantCategory())) {
                    passed = false;
                }
            }

            // Switch Expression could be used if we were returning values, but here we are
            // side-effecting/checking.
            // Java 17 switch is still mostly about values, not types (unless preview
            // enabled).

            if (!passed) {
                System.out.println("Rule failed");
            }
        }
    }
}
