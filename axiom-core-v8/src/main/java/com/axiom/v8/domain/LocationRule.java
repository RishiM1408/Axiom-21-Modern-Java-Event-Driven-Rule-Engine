package com.axiom.v8.domain;

import java.util.List;

public class LocationRule extends Rule {
    private List<String> allowedRegions;

    public LocationRule() {
    }

    public LocationRule(String ruleId, int priority, List<String> allowedRegions) {
        super(ruleId, priority);
        this.allowedRegions = allowedRegions;
    }

    public List<String> getAllowedRegions() {
        return allowedRegions;
    }

    public void setAllowedRegions(List<String> allowedRegions) {
        this.allowedRegions = allowedRegions;
    }
}
