package geniusweb.exampleparties.kayseriliagent.strategies;

import java.util.function.Function;

public class BaseTimeConcessionStrategy implements IStrategies{
    private double targetUtility;
    private double minTargetUtility;
    private Function<Double,Double> deltaFunc;

    public BaseTimeConcessionStrategy(double targetUtility, double minTargetUtility, Function<Double, Double> deltaFunc) {
        if (targetUtility < minTargetUtility)
            throw new IllegalArgumentException("Target utility can't lower than minimum target utility");
        this.targetUtility = targetUtility;
        this.minTargetUtility = minTargetUtility;
        this.deltaFunc = deltaFunc;
    }



    @Override
    public double getTargetUtility(double time) {
        if (time > 1.0) {
            throw new IllegalArgumentException("Time can't be greater than 1");
        }
        double delta = this.deltaFunc.apply(time);
        return targetUtility - delta;
    }

    @Override
    public void updateRate(double rate) {

    }
}
