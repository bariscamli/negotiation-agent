package geniusweb.exampleparties.kayseriliagent;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;

import geniusweb.actions.FileLocation;

import geniusweb.actions.Accept;
import geniusweb.actions.Action;
import geniusweb.actions.LearningDone;
import geniusweb.actions.Offer;
import geniusweb.actions.PartyId;
import geniusweb.bidspace.AllBidsList;
import geniusweb.inform.ActionDone;
import geniusweb.inform.Agreements;
import geniusweb.inform.Finished;
import geniusweb.inform.Inform;
import geniusweb.inform.Settings;
import geniusweb.inform.YourTurn;
import geniusweb.issuevalue.Bid;
import geniusweb.issuevalue.Value;
import geniusweb.party.Capabilities;
import geniusweb.party.DefaultParty;
import geniusweb.profile.Profile;
import geniusweb.profile.utilityspace.UtilitySpace;
import geniusweb.profileconnection.ProfileConnectionFactory;
import geniusweb.profileconnection.ProfileInterface;
import geniusweb.progress.Progress;
import geniusweb.progress.ProgressRounds;
import geniusweb.references.Parameters;
import tudelft.utilities.logging.Reporter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class KayseriliAgent extends DefaultParty {

    private Bid lastReceivedBid = null;
    private PartyId me;
    private  Random random = new Random();
    protected ProfileInterface profileint = null;
    private Progress progress;
    private String protocol;
    private Profile profile;
    private Parameters parameters;
    private UtilitySpace utilitySpace;
    private PersistentState persistentState;
    private NegotiationData negotiationData;
    private List<File> dataPaths;
    private File persistentPath;
    private String opponentName;
    private HashMap<String,HashMap<String,Double>> bidsHistory;
    private Double turnCount = 0.0;
    private OpponentModelling opponentModelling;

    public KayseriliAgent() {
    }

    public KayseriliAgent(Reporter reporter) { // TODO: change name
        super(reporter); // for debugging
    }

    /**
     * This method mostly contains utility functionallity for the agent to function
     * properly. The code that is of most interest for the ANL competition is
     * further below and in the other java files in this directory. It does,
     * however, not hurt to read through this code to have a better understanding of
     * what is going on.
     * 
     * @param info information object for agent
     */
    @Override
    public void notifyChange(Inform info) {
        try {
            if (info instanceof Settings) {
                // info is a Settings object that is passed at the start of a negotiation
                Settings settings = (Settings) info;
//                this.profile = (Profile) settings.getProfile();

                // initialize history list
                this.bidsHistory = new HashMap<String,HashMap<String,Double>>();
//                System.out.println("DOMAIN:   " + this.profile.getDomain());
//                System.out.println("DOMAIN2:   " + this.profile.getReservationBid());

                //for (String domains : profile.getDomain()) {
                //    bidsHistory[domains] = new ArrayList<>;
                //}

                this.opponentModelling = new OpponentModelling();
                // ID of my agent
                this.me = settings.getID();

                // The progress object keeps track of the deadline
                this.progress = settings.getProgress();

                // Protocol that is initiate for the agent
                this.protocol = settings.getProtocol().getURI().getPath();

                // Parameters for the agent (can be passed through the GeniusWeb GUI, or a
                // JSON-file)
                this.parameters = settings.getParameters();

                // The PersistentState is loaded here (see 'PersistenData,java')
                if (this.parameters.containsKey("persistentstate"))
                    this.persistentPath = new FileLocation(
                            UUID.fromString((String) this.parameters.get("persistentstate"))).getFile();
                if (this.persistentPath != null && this.persistentPath.exists()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    this.persistentState = objectMapper.readValue(this.persistentPath, PersistentState.class);
                } else {
                    this.persistentState = new PersistentState();
                }

                // The negotiation data paths are converted here from List<String> to List<File>
                // for improved usage. For safety reasons, this is more comprehensive than
                // normally.
                if (this.parameters.containsKey("negotiationdata")) {
                    List<String> dataPaths_raw = (List<String>) this.parameters.get("negotiationdata");
                    System.out.println("Raw data path: " + dataPaths_raw);
                    this.dataPaths = new ArrayList<>();
                    for (String path : dataPaths_raw)
                        this.dataPaths.add(new FileLocation(UUID.fromString(path)).getFile());
                }
                if ("Learn".equals(protocol)) {
                    // We are in the learning step: We execute the learning and notify when we are
                    // done. REMEMBER that there is a deadline of 60 seconds for this step.
                    learn();
                    getConnection().send(new LearningDone(me));
                } else {
                    // We are in the negotiation step.

                    // Create a new NegotiationData object to store information on this negotiation.
                    // See 'NegotiationData.java'.
                    this.negotiationData = new NegotiationData();

                    // Obtain our utility space, i.e. the problem we are negotiating and our
                    // preferences over it.
                    try {
                        this.profileint = ProfileConnectionFactory.create(settings.getProfile().getURI(),
                                getReporter());
                        this.utilitySpace = ((UtilitySpace) profileint.getProfile());
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            } else if (info instanceof ActionDone) {
                // The info object is an action that is performed by an agent.
                Action action = ((ActionDone) info).getAction();

                // Check if this is not our own action
                if (!this.me.equals(action.getActor())) {
                    // Check if we already know who we are playing against.
                    if (this.opponentName == null) {
                        // The part behind the last _ is always changing, so we must cut it off.
                        String fullOpponentName = action.getActor().getName();
                        int index = fullOpponentName.lastIndexOf("_");
                        this.opponentName = fullOpponentName.substring(0, index);

                        // Add name of the opponent to the negotiation data
                        this.negotiationData.setOpponentName(this.opponentName);
                    }
                    // Process the action of the opponent.
                    processAction(action);
                }
            } else if (info instanceof YourTurn) {
                // Advance the round number if a round-based deadline is set.
                if (progress instanceof ProgressRounds) {
                    progress = ((ProgressRounds) progress).advance();
                }

                // The info notifies us that it is our turn
                myTurn();
            } else if (info instanceof Finished) {
                // The info is a notification that th negotiation has ended. This Finished
                // object also contains the final agreement (if any).
                Agreements agreements = ((Finished) info).getAgreement();
                processAgreements(agreements);

                // Write the negotiation data that we collected to the path provided.
                if (this.dataPaths != null && this.negotiationData != null) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.writerWithDefaultPrettyPrinter().writeValue(this.dataPaths.get(0),
                                this.negotiationData);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to write negotiation data to disk", e);
                    }
                }

                // Log the final outcome and terminate
                getReporter().log(Level.INFO, "Final outcome:" + info);
                terminate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to handle info", e);
        }
    }

    /** Let GeniusWeb know what protocols that agent is capable of handling */
    @Override
    public Capabilities getCapabilities() {
        return new Capabilities(new HashSet<>(Arrays.asList("SAOP", "Learn")), Collections.singleton(Profile.class));
    }

    /** Terminate agent */
    @Override
    public void terminate() {
        super.terminate();
        if (this.profileint != null) {
            this.profileint.close();
            this.profileint = null;
        }
    }

    /*
     * *****************************NOTE:************************************
     * Everything below this comment is most relevant for the ANL competition.
     * **********************************************************************
     */

    /** Provide a description of the agent */
    @Override
    public String getDescription() {
        return "Kayserili Agent 2021";
    }

    /**
     * Processes an Action performed by the opponent.
     * 
     * @param action
     */
    private void processAction(Action action) {
        if (action instanceof Offer) {
            // If the action was an offer: Obtain the bid and add it's value to our
            // negotiation data.
            this.lastReceivedBid = ((Offer) action).getBid();
            this.negotiationData.addBidUtil(this.utilitySpace.getUtility(this.lastReceivedBid).doubleValue());
        }
    }

    /**
     * This method is called when the negotiation has finished. It can process the
     * final agreement.
     * 
     * @param agreements
     */
    private void processAgreements(Agreements agreements) {
        // Check if we reached an agreement (walking away or passing the deadline
        // results in no agreement)
        if (!agreements.getMap().isEmpty()) {
            // Get the bid that is agreed upon and add it's value to our negotiation data
            Bid agreement = agreements.getMap().values().iterator().next();
            this.negotiationData.addAgreementUtil(this.utilitySpace.getUtility(agreement).doubleValue());
           // this.negotiationData.addOpponentBidUtil(this.utilitySpace.getUtility(agreement).doubleValue());
           // this.negotiationData.settotalNegotation(1);

        }
       /* for (String k: bidsHistory.keySet()) {
            if(k != null)
                System.out.println(bidsHistory.get(k).toString());

        }*/

    }


    private void myTurn() throws IOException {
        System.out.println("Time Step: " + progress.get(System.currentTimeMillis()));
        Action action;
        turnCount++;
        if (lastReceivedBid != null) {
            // Store last Received Bids
                for (String domains : lastReceivedBid.getIssues()) {
                    if (bidsHistory.containsKey(domains)) {
                        break;
                    } else {
                        bidsHistory.put(domains, new HashMap<>());
                    }
                }
                for (String keys : lastReceivedBid.getIssueValues().keySet()) {
                    String value = lastReceivedBid.getIssueValues().get(keys).toString(); // cola, fanta
                    if (bidsHistory.get(keys).containsKey(value) && bidsHistory != null)
                        bidsHistory.get(keys).put(value, bidsHistory.get(keys).get(value) + (1.0  - progress.get(System.currentTimeMillis())));
                    else
                        bidsHistory.get(keys).put(value, 1.0);
                }
                opponentModelling.validateOthers(turnCount,bidsHistory);
                opponentModelling.addOpponentModel(lastReceivedBid.getIssueValues().values());
            }


        if (isGood(lastReceivedBid)) {
            // If the last received bid is good: create Accept action
            action = new Accept(me, lastReceivedBid);

        } else {
            // Obtain ist of all bids
            AllBidsList bidspace = new AllBidsList(this.utilitySpace.getDomain());
            Bid bid = null;
            Double timeStep = progress.get(System.currentTimeMillis());
            ArrayList<Double> opponentSpace = new ArrayList<>();
            if (timeStep > 0.1) {
                opponentSpace = opponentModelling.calculateFrequency();
            }
                bid = bidspace.get(0);
                if (timeStep < 0.5){
                    if (this.utilitySpace.getUtility(bid).doubleValue() > 0.9){
                        action = new Offer(me, bid);
                        getConnection().send(action);
                    }
                    else{
                        bid = randomBidGenerator(bidspace,0.81);
                        action = new Offer(me, bid);
                        getConnection().send(action);
                    }
                } else if (timeStep <= 0.7){
                    double val = 0.0;
                    if (opponentSpace.get(opponentSpace.size()-1) > 0.75)
                        val = opponentSpace.get(opponentSpace.size() -1);
                    else
                        val = 0.75;
                    if (this.utilitySpace.getUtility(bid).doubleValue() > val){
                        action = new Offer(me, bid);
                        getConnection().send(action);
                    } else {
                        bid = randomBidGenerator(bidspace,val);
                        action = new Offer(me, bid);
                        getConnection().send(action);
                    }
                } else if (timeStep <= 0.8){
                    double val = 0.0;
                    if (opponentSpace.get(opponentSpace.size()-1) > 0.70)
                        val = opponentSpace.get(opponentSpace.size() -1);
                    else
                        val = 0.70;
                    if (this.utilitySpace.getUtility(bid).doubleValue() > val){
                        action = new Offer(me, bid);
                        getConnection().send(action);
                    } else {
                        bid = randomBidGenerator(bidspace,val);
                        action = new Offer(me, bid);
                        getConnection().send(action);
                    }
                } else {
                    bid = randomBidGenerator(bidspace,0.7);
                    action = new Offer(me, bid);
                    getConnection().send(action);
                }
            }
            //System.out.println("BidSpace Length: " + bidspace.size().intValue());
            // Iterate randomly through list of bids until we find a good bid
           /* for (int attempt = 0; attempt < 500 && !isGood(bid); attempt++) {
                long i = random.nextInt(bidspace.size().intValue());
                bid = bidspace.get(BigInteger.valueOf(i));
            }*/

            // Create offer action
          //  action = new Offer(me, bid);

        // Send action
      //  getConnection().send(action);
    }

    private Bid randomBidGenerator(AllBidsList bidsList, double threshold){
        int random = new Random().nextInt(bidsList.size().intValue());
        Bid bid = bidsList.get(random);
        while (this.utilitySpace.getUtility(bid).doubleValue() < threshold){
            random = new Random().nextInt(bidsList.size().intValue());
            bid = bidsList.get(random);
        }
        return bid;
    }


    /**
     * The method checks if a bid is good.
     * 
     * @param bid the bid to check
     * @return true iff bid is good for us.
     */
    private boolean isGood(Bid bid) {
        Double timeStep = progress.get(System.currentTimeMillis());
        Double avgMaxUtility = this.persistentState.getAvgMaxUtility(this.opponentName);
        double rangeMin = 0.81;
        double rangeMax = 0.90;
        Boolean nearDeadline = progress.get(System.currentTimeMillis()) > 0.95;

        ArrayList<Double> opponentSpace = new ArrayList<>();
        if (timeStep > 0.2)
            opponentSpace = opponentModelling.calculateFrequency();

        int index = opponentSpace.size() - 1;
        if (bid == null) {
            return false;
        }
        Boolean acceptable = this.utilitySpace.getUtility(bid).doubleValue() > 0.7;
        Boolean good = this.utilitySpace.getUtility(bid).doubleValue() > 0.9;
        // Check if we already know the opponent
        if (this.persistentState.knownOpponent(this.opponentName)) {
            // Obtain the average of the max utility that the opponent has offered us in
            // previous negotiations.
           // System.out.println("We know the opponent!");
            double threshold = 0.10;
            if (this.persistentState.isCompetitive()) {
                System.out.println("We are here! is competitive");
                if (timeStep <= 0.5) {
                    double value = 0.0;
                    if (avgMaxUtility + threshold > 1.0) {
                        value = avgMaxUtility;
                    } else {
                        value = avgMaxUtility + threshold;
                    }
                   // System.out.println("Our Acceptance Value: " + value);
                    return this.utilitySpace.getUtility(bid).doubleValue() > value;
                } else if (timeStep <= 0.7) {
                    double value = 0.0;
                    if (avgMaxUtility + (threshold / 3) > 1.0) {
                        value = avgMaxUtility;
                    } else {
                        value = avgMaxUtility + (threshold / 3);
                    }
                   // System.out.println("Our Acceptance Value: " + value);
                    return this.utilitySpace.getUtility(bid).doubleValue() > value;
                } else if (timeStep <= 0.8) {
                    double value = 0.0;
                    if (avgMaxUtility + (2 * threshold / 3) > 1.0) {
                        value = avgMaxUtility;
                    } else {
                        value = avgMaxUtility + (2 * threshold / 3);
                    }
                   // System.out.println("Our Acceptance Value: " + value);
                    return this.utilitySpace.getUtility(bid).doubleValue() > value;
                } else {
                    double value = 0.0;
                    if (avgMaxUtility + (threshold / 5) > 1.0) {
                        value = avgMaxUtility;
                    } else {
                        value = avgMaxUtility + (threshold / 5);
                    }
                   // System.out.println("Our Acceptance Value: " + value);
                    return this.utilitySpace.getUtility(bid).doubleValue() > value;
                }
            } else if (this.persistentState.isSocial()) {

            } else {
                // Request 5% more than the average max utility offered by the opponent.
                return this.utilitySpace.getUtility(bid).doubleValue() > (avgMaxUtility * 1.05);
            }

        } else {
            // System.out.println("We don't know the opponent!");
            // Check a simple business rule

            //System.out.println(opponentSpace);

            if (timeStep < 0.5) {
                Double randomStarter = rangeMin + (rangeMax - rangeMin) * random.nextDouble();
              //  System.out.println("Offers Utility: " + this.utilitySpace.getUtility(bid).doubleValue());
             //   System.out.println("Random Starter: " + randomStarter);
                return this.utilitySpace.getUtility(bid).doubleValue() > randomStarter || good;
            } else if (timeStep <= 0.7) {
                //System.out.println("Opponents Bid: " + bid.getIssueValues().values());
              /* System.out.println("Our OpponentSpace: " + opponentSpace);
                for (Collection<Value> key: opponentSpace.keySet()) {
                    if (key.toString().equals(bid.getIssueValues().values().toString())) {
                        System.out.println("We are in!");
                        double estimatedUtility = opponentSpace.get(bid.getIssueValues().values()).doubleValue();
                        System.out.println("Estimated Utility: " + estimatedUtility);
                    }*/
                //index--;
             //   System.out.println("Offers Utility: " + this.utilitySpace.getUtility(bid).doubleValue());
             //   System.out.println("Utility Value: " + opponentSpace.get(index));
                if (opponentSpace.get(index) > 0.75)
                    return this.utilitySpace.getUtility(bid).doubleValue() > opponentSpace.get(index) || good;
            } else if (timeStep <= 0.8) {
                //index--;
             //   System.out.println("Offers Utility: " + this.utilitySpace.getUtility(bid).doubleValue());
             //   System.out.println("Utility Value: " + opponentSpace.get(index));
                if (opponentSpace.get(index) > 0.70)
                    return this.utilitySpace.getUtility(bid).doubleValue() > (opponentSpace.get(index) * timeStep) || good;
            }
        }
        return (nearDeadline && acceptable) || good;
    }




    /**
     * This method is invoked if the learning phase is started. There is now time to
     * process previously stored data and use it to update our persistent state.
     * This persistent state is passed to the agent again in future negotiation
     * session. REMEMBER that there is a deadline of 60 seconds for this step.
     */
    private void learn() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Iterate through the negotiation data file paths
        for (File dataPath : this.dataPaths)
            try {
                // Load the negotiation data object of a previous negotiation
                System.out.println("DataPath: " + this.dataPaths.toString());
                NegotiationData negotiationData = objectMapper.readValue(dataPath, NegotiationData.class);
                System.out.println("Negotiation Data Name: " + negotiationData.getOpponentName());
                System.out.println("Negotiation Data Agreement Util: " + negotiationData.getAgreementUtil());
                System.out.println("Negotiation Data GetMax: " + negotiationData.getMaxReceivedUtil());

                // Process the negotiation data in our persistent state
                this.persistentState.update(negotiationData);
                System.out.println("Opponent Encounters: " + this.persistentState.getOpponentEncounters(this.opponentName));
            } catch (IOException e) {
                throw new RuntimeException("Negotiation data provided to learning step does not exist", e);
            }

        // Write the persistent state object to file
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(this.persistentPath, this.persistentState);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write persistent state to disk", e);
        }
    }

}
