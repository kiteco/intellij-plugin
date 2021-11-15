package com.kite.intellij.backend.model;

public class LicenseInfo {
    private final String product;
    private final String plan;
    private final int daysRemaining;
    private final boolean trialAvailable;

    public LicenseInfo(String product, String plan, int daysRemaining, boolean trialAvailable) {
        this.product = product;
        this.plan = plan;
        this.daysRemaining = daysRemaining;
        this.trialAvailable = trialAvailable;
    }

    public String getProduct() {
        return product;
    }

    public int getDaysRemaining() {
        return daysRemaining;
    }

    public boolean isTrialAvailable() {
        return trialAvailable;
    }

    public boolean isKitePro() {
        return "pro".equals(this.product);
    }

    public boolean isTrialing() {
        return "pro_trial".equals(this.plan);
    }
}
