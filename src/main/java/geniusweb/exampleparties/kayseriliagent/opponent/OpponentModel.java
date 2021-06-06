package geniusweb.exampleparties.kayseriliagent.opponent;

import geniusweb.issuevalue.Bid;

public interface OpponentModel {
    double getUtility(Bid bid);
    void updateModel(Bid bid);
}
