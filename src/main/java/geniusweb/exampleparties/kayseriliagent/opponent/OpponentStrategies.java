package geniusweb.exampleparties.kayseriliagent.opponent;

import geniusweb.bidspace.IssueInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class OpponentStrategies {
    private HashMap<String, ArrayList<HashMap<String,Double>>> opponentSpace;

    public OpponentStrategies(HashMap<String, ArrayList<HashMap<String, Double>>> opponentSpace) {
        this.opponentSpace = opponentSpace;
    }
}
