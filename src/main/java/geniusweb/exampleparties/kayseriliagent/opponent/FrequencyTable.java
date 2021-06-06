package geniusweb.exampleparties.kayseriliagent.opponent;

import geniusweb.issuevalue.Bid;
import geniusweb.issuevalue.Domain;
import geniusweb.issuevalue.Value;


import java.util.*;
import java.util.stream.IntStream;

public class FrequencyTable {
    private ArrayList< HashMap<String,HashMap<String,Double>>> opponentMapList;

    public void addBidToFreqMap(HashMap<String,HashMap<String,Double>> opponentMap)
    {
        opponentMapList.add(opponentMap);
    }

}
