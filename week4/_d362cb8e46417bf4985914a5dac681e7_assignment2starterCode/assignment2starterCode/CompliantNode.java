import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private int numRounds;

    private boolean[] followees;

    private Set<Transaction> pendingTransactions;

    private static final int NUM_OF_ROUNDS = 3;
    
    private int round;

	private Map<Integer,Integer> mapEdges = new HashMap<Integer,Integer>();


    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.setP_graph(p_graph);
        this.setP_malicious(p_malicious);
        this.setP_txDistribution(p_txDistribution);
        this.setNumRounds(numRounds);
        this.followees = null;
        this.setPendingTransactions(new HashSet<>()); 
    }

    public Map<Integer,Integer> getMapEdges() {
        return mapEdges;
    }

    public void setMapEdges(Map<Integer,Integer> mapEdges) {
        this.mapEdges = mapEdges;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public static int getNumOfRounds() {
        return NUM_OF_ROUNDS;
    }

    public Set<Transaction> getPendingTransactions() {
        return pendingTransactions;
    }

    public void setPendingTransactions(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public int getNumRounds() {
        return numRounds;
    }

    public void setNumRounds(int numRounds) {
        this.numRounds = numRounds;
    }

    public double getP_txDistribution() {
        return p_txDistribution;
    }

    public void setP_txDistribution(double p_txDistribution) {
        this.p_txDistribution = p_txDistribution;
    }

    public double getP_malicious() {
        return p_malicious;
    }

    public void setP_malicious(double p_malicious) {
        this.p_malicious = p_malicious;
    }

    public double getP_graph() {
        return p_graph;
    }

    public void setP_graph(double p_graph) {
        this.p_graph = p_graph;
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        for(Transaction transaction : pendingTransactions){
    		this.pendingTransactions.add(transaction);
    		getMapEdges().put(transaction.id, 0);
    	}
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS

        if(getRound() == getNumOfRounds()){
            Set<Transaction> sendTransactions = new HashSet<>();
        
        	sendTransactions.addAll(pendingTransactions);
            pendingTransactions.clear();

            return sendTransactions;
            
        }
    	return pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        for(Candidate candidate : candidates){
    		Transaction transaction = candidate.tx;
    		int sender = candidate.sender;
    		
    		if(!followees[sender]) continue;
    		
    		pendingTransactions.add(transaction);
    		
    		Integer count = getMapEdges().get(transaction.id) != null ? getMapEdges().get(transaction.id) : 0;
    		
    		count++;
    		getMapEdges().put(transaction.id, count);
    		
    	}
    }
}
