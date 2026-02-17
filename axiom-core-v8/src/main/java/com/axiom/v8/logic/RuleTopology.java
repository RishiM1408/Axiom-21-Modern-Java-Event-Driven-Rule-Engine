package com.axiom.v8.logic;

import com.axiom.v8.domain.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import java.util.List;

public class RuleTopology {

    // ... Serde setup omitted for brevity in V8 example ...

    // Simulate Evaluation Logic
    public void evaluate(Transaction tx, List<Rule> rules) {
        if (rules == null || rules.isEmpty())
            return;

        for (Rule rule : rules) {
            // THE LEGACY WAY: Instanceof checks and casting
            boolean passed = true;
            String reason = "";

            if (rule instanceof ThresholdRule) {
                ThresholdRule tr = (ThresholdRule) rule;
                if (tx.getAmount().compareTo(tr.getMaxAmount()) > 0) {
                    passed = false;
                    reason = "Amount > " + tr.getMaxAmount();
                }
            } else if (rule instanceof LocationRule) {
                LocationRule lr = (LocationRule) rule;
                if (!lr.getAllowedRegions().contains(tx.getMerchantCategory())) {
                    passed = false;
                    reason = "Invalid Region";
                }
            }

            if (!passed) {
                System.out.println("Rule " + rule.getRuleId() + " failed: " + reason);
            }
        }
    }
}
