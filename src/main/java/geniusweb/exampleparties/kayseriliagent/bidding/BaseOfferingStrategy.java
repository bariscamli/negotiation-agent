package geniusweb.exampleparties.kayseriliagent.bidding;

import geniusweb.issuevalue.Bid;
import geniusweb.issuevalue.Domain;

import java.util.List;

public abstract class BaseOfferingStrategy {
    protected Domain domain;

    public BaseOfferingStrategy(Domain domain) {
        this.domain = domain;
    }
    public abstract Bid generateBid(double targetUtility, List<Bid> offerSpace, Bid opponentBestOffer, double time);
}
