package geniusweb.exampleparties.kayseriliagent;

import geniusweb.bidspace.AllBidsList;
import geniusweb.issuevalue.Bid;
import geniusweb.profile.utilityspace.UtilitySpace;

import java.util.HashMap;
import java.util.Random;

public class Bidding {
    private UtilitySpace ourUtility;
    private AllBidsList bidSpace;
    private Bid ourPreviousBid;
    private boolean gahboninhoConcReactionDet;
    private boolean gahboninhoSelfishReactionDet;
    private HashMap<Object,Double> selfishnessAgainstConceding;
    private HashMap<Object,Double> selfishnessAgainstHardhead;
    private boolean hardHeadGahboninho;//learned concession strategy for Gahboninho, true=hardheaded, false=slow concession
    private Random rand;
    private double gahboninoConcActualDeadline;

    public Bidding(UtilitySpace ownUtility){
        ourUtility=ownUtility;
        bidSpace= new AllBidsList(this.ourUtility.getDomain());
        //ourPreviousBid= bidSpace.get();
        gahboninhoConcReactionDet=false;
        gahboninhoSelfishReactionDet=false;
        hardHeadGahboninho=true;
        rand= new Random(1);//Constant seed doesn't matter, not like the opponents will try to predict our bids in that way
        gahboninoConcActualDeadline=0;
    }
}
