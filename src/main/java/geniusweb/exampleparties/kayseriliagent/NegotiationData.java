package geniusweb.exampleparties.kayseriliagent; // TODO: change name

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The class hold the negotiation data that is obtain during a negotiation
 * session. It will be saved to disk after the negotiation has finished. During
 * the learning phase, this negotiation data can be used to update the
 * persistent state of the agent. NOTE that Jackson can serialize many default
 * java classes, but not custom classes out-of-the-box.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class NegotiationData {

    private ArrayList<ArrayList<Double>> bidsHistory = new ArrayList<ArrayList<Double>>();
    private int totalNegotation = 0;

    private String opponentName;

    public void addOpponentBidUtil(Double bidUtil) {
        this.bidsHistory.get(this.totalNegotation).add(bidUtil);
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public void settotalNegotation(int num) {
        this.totalNegotation += num;
    }

    public String getOpponentName() {
        return this.opponentName;
    }

    public int gettotalNegotation() {
        return this.totalNegotation;
    }

    public ArrayList<ArrayList<Double>> getbidsHistory() {
        return this.bidsHistory;
    }
}
