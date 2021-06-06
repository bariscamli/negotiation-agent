package geniusweb.exampleparties.kayseriliagent.bidding;

import geniusweb.exampleparties.kayseriliagent.opponent.OpponentModel;
import geniusweb.issuevalue.Bid;
import geniusweb.issuevalue.Domain;
import geniusweb.profile.utilityspace.UtilitySpace;

import java.util.List;
import java.util.Random;

public class HybridOfferingBiddingStrategy extends BaseOfferingStrategy{
    private OpponentModel opponentModel;
    private UtilitySpace userspace;
    private RandomOfferingBiddingStrategy randomOfferingBiddingStrategy;
    public HybridOfferingBiddingStrategy(Domain domain, OpponentModel opponentModel, UtilitySpace userspace) {
        super(domain);
        this.opponentModel = opponentModel;
        this.userspace = userspace;
        this.randomOfferingBiddingStrategy = new RandomOfferingBiddingStrategy(domain,userspace);
    }

    @Override
    public Bid generateBid(double targetUtility, List<Bid> offerSpace, Bid opponentBestOffer, double time) {
        Bid highestOpponentBid = null;
        Bid chosenBid = null;
        for (Bid bid : offerSpace) {
            if (highestOpponentBid == null || opponentModel.getUtility(bid) > opponentModel.getUtility(highestOpponentBid)) {
                highestOpponentBid = bid;
            }
        }

        if (time <= 0.5) {
            Bid randomBid = randomOfferingBiddingStrategy.generateBid(targetUtility, offerSpace, opponentBestOffer, time);
            Bid[] bidChoices = new Bid[]{highestOpponentBid, randomBid};
            chosenBid = bidChoices[new Random().nextInt(bidChoices.length)];
        } else {
            chosenBid = highestOpponentBid;
        }

        if (chosenBid == null)
            return opponentBestOffer;
        else if (userspace.getUtility(chosenBid).doubleValue() > userspace.getUtility(opponentBestOffer).doubleValue())
            return chosenBid;
        else
            return opponentBestOffer;
    }
    }



