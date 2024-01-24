package com.example.javanewwisebandit.goldentime_v1.Model;

public class IncentiveInfoTuple {
    private double incentive;
    private double expectedRate;

    public IncentiveInfoTuple (double incentive, double expectedRate){
        this.incentive = incentive;
        this.expectedRate = expectedRate;
    }

    public double getIncentive() {
        return incentive;
    }

    public double getExpectedRate() {
        return expectedRate;
    }

    public void setIncentive(double incentive) {
        this.incentive = incentive;
    }

    public void setExpectedRate(double expectedRate) {
        this.expectedRate = expectedRate;
    }
}
