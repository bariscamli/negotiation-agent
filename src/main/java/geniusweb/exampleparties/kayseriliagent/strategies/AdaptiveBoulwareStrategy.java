package geniusweb.exampleparties.kayseriliagent.strategies;

public class AdaptiveBoulwareStrategy implements IStrategies {
    private double minUtility;
    private double maxUtility;
    private double firstConcession;
    private double rate;

    public AdaptiveBoulwareStrategy(double minUtility, double maxUtility, double firstConcession, double rate) {
        this.minUtility = minUtility;
        this.maxUtility = maxUtility;
        this.firstConcession = firstConcession;
        this.rate = rate;
    }


    @Override
    public double getTargetUtility(double time) {
        double timeFunc = firstConcession + ((1 - firstConcession) * Math.pow(time, 1 / rate));
        return minUtility + (1- timeFunc) * (maxUtility - minUtility);
    }

    @Override
    public void updateRate(double rate) {
        this.rate = rate;
    }
}
