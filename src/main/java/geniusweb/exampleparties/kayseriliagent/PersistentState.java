package geniusweb.exampleparties.kayseriliagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * This class can hold the persistent state of your agent. You can off course
 * also write something else to the file path that is provided to your agent,
 * but this provides an easy usable method. This object is serialized using
 * Jackson. NOTE that Jackson can serialize many default java classes, but not
 * custom classes out-of-the-box.
 */
/*
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class PersistentState {

    private Double averageUtility = 0.0;
    private Integer negotiations = 0;
    private Map<String, Double> avgMaxUtilityOpponent = new HashMap<String, Double>();
    private Map<String, Integer> opponentEncounters = new HashMap<String, Integer>();


    /**
     * Update the persistent state with a negotiation data of a previous negotiation
     * session
     * 
     * @param negotiationData NegotiationData class holding the negotiation data
     *                        that is obtain during a negotiation session.
     */
/*
    public void update(NegotiationData negotiationData) {
        // Keep track of the average utility that we obtained

        double mean_five = 0.0;
        double mean_seven = 0.0;
        double mean_eight = 0.0;
        double mean_else = 0.0;

        double std_five = 0.0;
        double std_seven = 0.0;
        double std_eight = 0.0;
        double std_else = 0.0;

        ArrayList<Double> array = new ArrayList<Double>();
        array.add(0.5);
        array.add(0.7);
        array.add(0.8);
        array.add(1.0);

        int size_five = 0;
        int size_seven = 0;
        int size_eight = 0;
        int size_else = 0;

        for (int i = 0; i < negotiationData.gettotalNegotation() ; i++) {
            for (int j = 0; j < negotiationData.getbidsHistory().get(i).size(); j++) {
                if (j <= negotiationData.getbidsHistory().get(i).size() * 0.8) {
                    mean_five += negotiationData.getbidsHistory().get(i).get(j);
                    size_eight += 1;
                }
                if (j <= negotiationData.getbidsHistory().get(i).size() * 0.7) {
                    mean_seven += negotiationData.getbidsHistory().get(i).get(j);
                    size_seven += 1;
                }
                if (j <= negotiationData.getbidsHistory().get(i).size() * 0.5) {
                    mean_eight += negotiationData.getbidsHistory().get(i).get(j);
                    size_five += 1;
                } else {
                    mean_else += negotiationData.getbidsHistory().get(i).get(j);
                    size_else += 1;
                }
            }
        }

        mean_five /=  size_five;
        mean_seven /=  size_seven;
        mean_eight /=  size_eight;
        mean_else /=  size_else;

        for (int i = 0; i < negotiationData.gettotalNegotation() ; i++) {
            for(int j = 0; j < negotiationData.getbidsHistory().get(i).size(); j++) {
                if (j <= negotiationData.getbidsHistory().get(i).size() * 0.8)
                    std_eight += Math.abs(negotiationData.getbidsHistory().get(i).get(j) - mean_eight);
                if (j <= negotiationData.getbidsHistory().get(i).size() * 0.7)
                    std_seven += Math.abs(negotiationData.getbidsHistory().get(i).get(j) - mean_seven);
                if (j <= negotiationData.getbidsHistory().get(i).size() * 0.5)
                    std_five += Math.abs(negotiationData.getbidsHistory().get(i).get(j) - mean_five);
                else
                    std_else += Math.abs(negotiationData.getbidsHistory().get(i).get(j) - mean_else);
            }
        }

        std_five = Math.sqrt(std_five / size_five);
        std_seven = Math.sqrt(std_five / size_seven);
        std_eight = Math.sqrt(std_five / size_eight);
        std_else = Math.sqrt(std_five / size_else);



        // Keep track of the number of negotiations that we performed
        negotiationData.gettotalNegotation();

        // Get the name of the opponent that we negotiated against
        String opponent = negotiationData.getOpponentName();

        // Check for safety
        if (opponent != null) {
		// std dusuyorsa -> competitive davraniyor 
		// std yükseliyorsa -> social davraniyor
        }
    }

    public Double getAvgMaxUtility(String opponent) {
        if (avgMaxUtilityOpponent.containsKey(opponent)) {
            return avgMaxUtilityOpponent.get(opponent);
        }
        return null;
    }

    public Integer getOpponentEncounters(String opponent) {
        if (opponentEncounters.containsKey(opponent)) {
            return opponentEncounters.get(opponent);
        }
        return null;
    }

    public Boolean knownOpponent(String opponent) {
        return opponentEncounters.containsKey(opponent);
    }

    public boolean isCompetitive() {
        return true;
    }

    public boolean isSocial() {
        return false;
    }
}
*/

/**
 * This class can hold the persistent state of your agent. You can off course
 * also write something else to the file path that is provided to your agent,
 * but this provides an easy usable method. This object is serialized using
 * Jackson. NOTE that Jackson can serialize many default java classes, but not
 * custom classes out-of-the-box.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class PersistentState {

    private Double averageUtility = 0.0;
    private Integer negotiations = 0;
    private Map<String, Double> avgMaxUtilityOpponent = new HashMap<String, Double>();
    private Map<String, Integer> opponentEncounters = new HashMap<String, Integer>();

    /**
     * Update the persistent state with a negotiation data of a previous negotiation
     * session
     *
     * @param negotiationData NegotiationData class holding the negotiation data
     *                        that is obtain during a negotiation session.
     */
    public void update(NegotiationData negotiationData) {
        // Keep track of the average utility that we obtained
        this.averageUtility = (this.averageUtility * negotiations + negotiationData.getAgreementUtil())
                / (negotiations + 1);

        // Keep track of the number of negotiations that we performed
        negotiations++;

        // Get the name of the opponent that we negotiated against
        String opponent = negotiationData.getOpponentName();

        // Check for safety
        if (opponent != null) {
            // Update the number of encounters with an opponent
            Integer encounters = opponentEncounters.containsKey(opponent) ? opponentEncounters.get(opponent) : 0;
            opponentEncounters.put(opponent, encounters + 1);
            // Track the average value of the maximum that an opponent has offered us across
            // multiple negotiation sessions
            Double avgUtil = avgMaxUtilityOpponent.containsKey(opponent) ? avgMaxUtilityOpponent.get(opponent) : 0.0;
            avgMaxUtilityOpponent.put(opponent,
                    (avgUtil * encounters + negotiationData.getMaxReceivedUtil()) / (encounters + 1));
        }
    }

    public Double getAvgMaxUtility(String opponent) {
        if (avgMaxUtilityOpponent.containsKey(opponent)) {
            return avgMaxUtilityOpponent.get(opponent);
        }
        return null;
    }

    public Integer getOpponentEncounters(String opponent) {
        if (opponentEncounters.containsKey(opponent)) {
            return opponentEncounters.get(opponent);
        }
        return null;
    }

    public Boolean knownOpponent(String opponent) {
        return opponentEncounters.containsKey(opponent);
    }
    public boolean isCompetitive() {
        return true;
    }

    public boolean isSocial() {
        return false;
    }
}
