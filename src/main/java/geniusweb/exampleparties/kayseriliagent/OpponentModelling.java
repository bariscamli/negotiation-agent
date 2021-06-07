package geniusweb.exampleparties.kayseriliagent;


import geniusweb.issuevalue.Value;

import java.util.*;

public class OpponentModelling {
    private Collection<Value> issueValueList;
    private Double totalOccurrence;
    private ArrayList<Collection<Value>> issuesList = new ArrayList<Collection<Value>>();
    private HashMap<String, HashMap<String, Double>> bidsHistory;
    private HashMap<Collection<Value>, Double> opponentSpace;


    public OpponentModelling(Collection<Value> issueValueList, Double occurrence, HashMap<String, HashMap<String, Double>> bidsHistory) {
        this.issueValueList = issueValueList;
        this.totalOccurrence = occurrence;
        this.issuesList.add(issueValueList);
        this.bidsHistory = bidsHistory;
        opponentSpace = new HashMap<Collection<Value>,Double>();
    }
    public void printBids(){
        for (Collection<Value> issue : issuesList){

            //System.out.println(issue + " : " + this.totalOccurrence);
        }
        calculateFrequency();
    }
    public HashMap<Collection<Value>, Double> calculateFrequency(){
        HashMap<String,Double> values = new HashMap<>();
        ArrayList<HashMap<String,Double>> valuesList = new ArrayList<HashMap<String,Double>>();
        double sum = 0.0;
        int size = 0;

        for (String k : bidsHistory.keySet()) {
            if (k != null)
                values = bidsHistory.get(k);
           // System.out.println("Values: " + values);
            valuesList.add(values);
        }
        for (Collection<Value> issue: issuesList){
            size += issue.size();
            for (int i = 0; i<issue.size(); i++){
                String issueVal = issue.toArray()[i].toString();
                for (HashMap<String,Double> tmpValues: valuesList){
                    if (tmpValues.containsKey(issueVal)){
                      //  System.out.println(issueVal + " Double Value: " + tmpValues.get(issueVal));
                        sum += tmpValues.get(issueVal);
                    }
                }
                double sumNum = sum / size / this.totalOccurrence;
                opponentSpace.put(issue,sumNum);
            }
        }
            System.out.println("Opponent Space: " + opponentSpace + "\n**********************");
            System.out.println("Total Sum: " + sum / size / this.totalOccurrence+"\n-----------------");

            return opponentSpace;
    }

}

