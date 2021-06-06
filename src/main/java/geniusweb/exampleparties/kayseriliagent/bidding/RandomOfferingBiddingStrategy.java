package geniusweb.exampleparties.kayseriliagent.bidding;

import geniusweb.issuevalue.Bid;
import geniusweb.issuevalue.Domain;
import geniusweb.profile.utilityspace.UtilitySpace;

import java.util.List;
import java.util.Random;

public class RandomOfferingBiddingStrategy extends BaseOfferingStrategy{
    private static Random random = new Random();
    private UtilitySpace utilitySpace;
    public RandomOfferingBiddingStrategy(Domain domain, UtilitySpace utilitySpace) {
        super(domain);
        this.utilitySpace = utilitySpace;
    }

    @Override
    public Bid generateBid(double targetUtility, List<Bid> offerSpace, Bid opponentBestOffer, double time) {
        Bid bid = offerSpace.get(0);
        while (utilitySpace.getUtility(bid).doubleValue() < targetUtility)
            bid = offerSpace.get(random.nextInt(offerSpace.size()));
        return bid;
    }
}
