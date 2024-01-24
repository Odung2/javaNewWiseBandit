package com.example.javanewwisebandit.goldentime_v1.Model;

import java.util.ArrayList;

public class ContextCardDataObject {

    private String context;
    private IncentiveInfoTuple optimalIncentiveTuple;
    private int totalTimeSlot;
    private ArrayList<IncentiveInfoTuple> incentiveInfoTupleList;

    public ContextCardDataObject(String context, IncentiveInfoTuple optimalIncentiveTuple, int totalTimeSlot, ArrayList<IncentiveInfoTuple> incentiveInfoTupleList){
        this.context = context;
        this.optimalIncentiveTuple = optimalIncentiveTuple;
        this.totalTimeSlot = totalTimeSlot;
        this.incentiveInfoTupleList = incentiveInfoTupleList;
    }

    public String getContext() {
        return context;
    }

    public IncentiveInfoTuple getOptimalIncentiveTuple() {
        return optimalIncentiveTuple;
    }

    public int getTotalTimeSlot() {
        return totalTimeSlot;
    }

    public ArrayList<IncentiveInfoTuple> getIncentiveInfoTupleList() {
        return incentiveInfoTupleList;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setOptimalIncentiveTuple(IncentiveInfoTuple optimalIncentiveTuple) {
        this.optimalIncentiveTuple = optimalIncentiveTuple;
    }

    public void setTotalTimeSlot(int totalTimeSlot) {
        this.totalTimeSlot = totalTimeSlot;
    }

    public void setIncentiveInfoTupleList(ArrayList<IncentiveInfoTuple> incentiveInfoTupleList) {
        this.incentiveInfoTupleList = incentiveInfoTupleList;
    }
}
