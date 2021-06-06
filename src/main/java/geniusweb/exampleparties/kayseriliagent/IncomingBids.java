package geniusweb.exampleparties.kayseriliagent;


import geniusweb.issuevalue.Value;

import java.util.*;

public class IncomingBids {
    private Collection<Value> issueValueList;
    private Double totalOccurrence;
    private ArrayList<String> issuesList = new ArrayList<>();
    private HashMap<String, HashMap<String, Double>> bidsHistory;


    public IncomingBids(Collection<Value> issueValueList, Double occurrence, HashMap<String, HashMap<String, Double>> bidsHistory) {
        this.issueValueList = issueValueList;
        this.totalOccurrence = occurrence;
        this.issuesList.add(issueValueList.toString());
        this.bidsHistory = bidsHistory;

    }
    public void printBids(){
        for (String issue : issuesList){

            System.out.println(issue + " : " + this.totalOccurrence);
        }
        calculateFrequency();
    }
     public void calculateFrequency(){
        HashMap<String,Double> values = new HashMap<>();
        ArrayList<HashMap<String,Double>> valuesList = new ArrayList<HashMap<String,Double>>();
         double sum = 0.0;

             for (String k : bidsHistory.keySet()) {
                 if (k != null)
                      values = bidsHistory.get(k);
                 System.out.println("Values: " + values);
                 valuesList.add(values);
             }
             for (String issue: issuesList){
                 for (HashMap<String,Double> tmpValues: valuesList){
                     if (tmpValues.containsKey(issue)){
                         System.out.println(issue+" Double Value: " + tmpValues.get(issue));
                         sum += tmpValues.get(issue);
                     }
                 }

             }
             System.out.println("Total Sum: " + sum+"\n-----------------");
         }

}
