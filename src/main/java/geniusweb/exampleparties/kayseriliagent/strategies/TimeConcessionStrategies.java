package geniusweb.exampleparties.kayseriliagent.strategies;

import java.util.function.Function;

public class TimeConcessionStrategies {
    public static BaseTimeConcessionStrategy LinearTimeConcession(double targetUtility, double minTargetUtility){
        Function<Double,Double> deltaFunc = t -> t * (targetUtility - minTargetUtility);
        return new BaseTimeConcessionStrategy(targetUtility,minTargetUtility,deltaFunc);
    }
    public static BaseTimeConcessionStrategy QuadraticTimeConcession(double targetUtility, double minTargetUtility){
        Function<Double,Double> deltaFunc = t -> Math.pow(t,2) * (targetUtility - minTargetUtility);
        return new BaseTimeConcessionStrategy(targetUtility,minTargetUtility,deltaFunc);
    }
    public static BaseTimeConcessionStrategy CubicTimeConcession(double targetUtility, double minTargetUtility){
        Function<Double,Double> deltaFunc = t -> Math.pow(t,3) * (targetUtility - minTargetUtility);
        return new BaseTimeConcessionStrategy(targetUtility,minTargetUtility,deltaFunc);
    }
}
