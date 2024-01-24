package com.example.javanewwisebandit.goldentime_v1;

import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Incentive {
    public double[] incentives;

    private final Random random;
    private Map<String, double[][]> arms;

    private static Incentive instance;
    private static String TAG = "Incentive Log";

    private Incentive(double[] incentives, int seed) {
        this.incentives = incentives;
        this.random = new Random(seed);
        // multi_objective not activated
        // optimistic not activated

        this.init_arms();
    }

    public static Incentive getInstance() {
        if(instance == null){
            double[] incentives = {200, 400, 600, 800};
            synchronized(Incentive.class)
            {
                instance = new Incentive(incentives, 0);
            }
        }
        return instance;
    }

    public Double expect(double incentive, String context){
        int i = ArrayUtils.indexOf(getInstance().incentives, incentive);
        if (i < 0) return null;

        double[][] distribution = getInstance().arms.get(context);

        double alpha = 0;
        double beta = 0;
        if (distribution != null){
            alpha = distribution[i][0];
            beta = distribution[i][1];
        }

        Log.d(TAG, "alpha: " + alpha + ", beta: " + beta);
        return new BetaDistribution(alpha + 1, beta + 1).sample();
    }

    public Double choose(String context){
        int arrLength = getInstance().incentives.length;
        double[] successRatioArray = Arrays.stream(getInstance().incentives)
                .map(x -> getInstance().expect(x, context))
                .toArray();
        double[] costArray = getInstance().incentives.clone();
        for (int i=0; i<arrLength; i++){
            // loss frame 상황에서는 cost 수정 필요
            // costArray라는 변수명도 의미있게 바꾸면 좋을 듯(예: lossArray)
            // 기존 코드: costArray[i] *= successRatioArray[i];
            // 개선 코드: costArray[i] = (1 - successRatioArray[i]) * costArray[i];
            costArray[i] = (1 - successRatioArray[i]) * costArray[i];
        }

        // loss frame 상황에서는 cost 수정 필요
        // 사용자가 절제 실패 시 잃는 금액도 최대화할 필요가 있음
        // 기존 코드: int[] orders = {1, -1};
        // 개선 코드: int[] orders = {1, 1};
        int[] orders = {1, 1};
        double[][] objectives = new double[arrLength][2];
        for (int j=0; j<arrLength; j++){
            objectives[j][0] = successRatioArray[j];
            objectives[j][1] = costArray[j];
        }
        List<Integer> optimalIndexList = getInstance().findParetoFrontiers(objectives, orders);
        Log.d(TAG, Arrays.toString(optimalIndexList.toArray()));
        if (optimalIndexList.isEmpty()){
            return getInstance().incentives[random.nextInt(arrLength)];
        }
        else {
            int optimalIndex = optimalIndexList.get(random.nextInt(optimalIndexList.size()));
            return getInstance().incentives[optimalIndex];
        }
    }

    public void update(double incentive, double response, String context){
        int incentiveIdx = ArrayUtils.indexOf(getInstance().incentives, incentive);
        if (incentiveIdx < 0) return;
        double[][] contextDist = getInstance().arms.remove(context);
        if (contextDist == null){
            contextDist = new double[getInstance().incentives.length][2];
        }
        contextDist[incentiveIdx][0] += response;
        contextDist[incentiveIdx][1] += (1 - response);
        getInstance().arms.put(context, contextDist);
    }

    private void init_arms(){
        this.arms = new HashMap<String, double[][]>();
    }

    private List<Integer> findParetoFrontiers(double[][] objectives, int[] orders){
        List<Integer> frontierIndexList = new ArrayList<>();
        int n = objectives.length;

        for (int i=0; i<n; i++){
            boolean isParetoFrontier = true;

            for (int j=0; j<n; j++){
                if (i != j) {
                    boolean isDominated = getInstance().isDominated(i, j, objectives, orders);
                    boolean isIncomparable = getInstance().isIncomparable(i, j, objectives, orders);
                    isParetoFrontier = isParetoFrontier && (isDominated || isIncomparable);
                    if (isDominated){
                        Log.d(TAG, "incentive "+i+" expect: " +objectives[i][0]+ " is dominated "+j+" expect: "+objectives[j][0]);
                    }
                    if (isIncomparable){
                        Log.d(TAG, "incentive "+i+" expect: " +objectives[i][0]+ " is incomparable "+j+" expect: "+objectives[j][0]);
                    }
                }
            }
            if (isParetoFrontier) {
                frontierIndexList.add(i);
            }
        }
        return frontierIndexList;
    }

    private boolean isDominated (int x, int y, double[][] objectives, int[] orders){
        int numberOfObjectives = objectives[0].length;

        for (int i=0; i<numberOfObjectives; i++){
            int order = orders[i];
            boolean isDominate;
            if (order > 0) {
                isDominate = objectives[x][i] > objectives[y][i];
            }
            else {
                isDominate = objectives[x][i] < objectives[y][i];
            }


            for (int j=0; j<numberOfObjectives; j++){
                order = orders[j];
                if (i != j){
                    if (order > 0){
                        isDominate = isDominate && (objectives[x][j] >= objectives[y][j]);
                    }
                    else {
                        isDominate = isDominate && (objectives[x][j] <= objectives[y][j]);
                    }
                }
            }
            if (isDominate){
                return true;
            }
        }

        return false;
    }

//    private boolean isDominated (int x, int y, INDArray objectives, int[] orders){
//        int numberOfObjectives = (int) objectives.shape()[1];
//
//        for (int i=0; i<numberOfObjectives; i++){
//            int order = orders[i];
//            boolean isDominate;
//            if (order > 0) {
//                isDominate = objectives.getDouble(x, i) > objectives.getDouble(y, i);
//            }
//            else {
//                isDominate = objectives.getDouble(x, i) < objectives.getDouble(y, i);
//            }
//
//            for (int j=0; j<numberOfObjectives; j++){
//                order = orders[j];
//                if (i != j){
//                    if (order > 0){
//                        isDominate = isDominate && (objectives.getDouble(x, j) >= objectives.getDouble(y, j));
//                    }
//                    else {
//                        isDominate = isDominate && (objectives.getDouble(x, j) <= objectives.getDouble(y, j));
//                    }
//                }
//            }
//            if (isDominate){
//                return true;
//            }
//        }
//
//        return false;
//    }

    private boolean isIncomparable (int x, int y, double[][] objectives, int[] orders){
        int numberOfObjectives = objectives[0].length;

        for (int i=0; i<numberOfObjectives; i++){
            int order = orders[i];
            boolean isDominate;
            if (order > 0) {
                isDominate = objectives[x][i] > objectives[y][i];
            }
            else {
                isDominate = objectives[x][i] < objectives[y][i];
            }

            for (int j=0; j<numberOfObjectives; j++){
                order = orders[j];
                if (i != j){
                    if (order > 0){
                        if ((objectives[x][j] < objectives[y][j]) && isDominate){
                            return true;
                        }
                    }
                    else {
                        if ((objectives[x][j] > objectives[y][j]) && isDominate){
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

//    private boolean isIncomparable (int x, int y, INDArray objectives, int[] orders){
//        int numberOfObjectives = (int) objectives.shape()[1];
//
//        for (int i=0; i<numberOfObjectives; i++){
//            int order = orders[i];
//            boolean isDominate;
//            if (order > 0) {
//                isDominate = objectives.getDouble(x, i) > objectives.getDouble(y, i);
//            }
//            else {
//                isDominate = objectives.getDouble(x, i) < objectives.getDouble(y, i);
//            }
//
//            for (int j=0; j<numberOfObjectives; j++){
//                order = orders[j];
//                if (i != j){
//                    if (order > 0){
//                        if ((objectives.getDouble(x, j) < objectives.getDouble(y, j)) && isDominate){
//                            return true;
//                        }
//                    }
//                    else {
//                        if ((objectives.getDouble(x, j) > objectives.getDouble(y, j)) && isDominate){
//                            return true;
//                        }
//                    }
//                }
//            }
//        }
//
//        return false;
//    }

}