import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class TxHandler {
    private UTXOPool utxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {

        int index = 0;
        List<UTXO> usedList = new ArrayList<>();
        double inputSum = 0;
        double outputSum = 0;

        for (Transaction.Input txInput: tx.getInputs())
        {

             // (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
            UTXO utxo = new UTXO(txInput.prevTxHash, txInput.outputIndex);

            if(!this.utxoPool.contains(utxo))
            {
                return false;
            }
            
            //  (2) the signatures on each input of {@code tx} are valid, 

            Transaction.Output txOutput = this.utxoPool.getTxOutput(utxo);

            PublicKey pk = txOutput.address;
            byte[] signature = txInput.signature;
            byte[] message = tx.getRawDataToSign(index);
            
            if(!Crypto.verifySignature(pk, message, signature))
            {
                return false;
            }

            // (3) no UTXO is claimed multiple times by {@code tx},
            if(usedList.contains(utxo))
            {
                return false;
            }

            
            inputSum += txOutput.value;
            index++;
            usedList.add(utxo);
        }

        for (Transaction.Output txOutput: tx.getOutputs()) {

            // (4) all of {@code tx}s output values are non-negative, and
            if(txOutput.value < 0)
            {
                return false;
            }
            outputSum += txOutput.value;
        }
        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
        //  values; and false otherwise.
        if(inputSum < outputSum)
        {
            return false;
        }

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> acceptedTransactions = new ArrayList<>();

        for(Transaction tx: possibleTxs)
        {
            if(isValidTx(tx))
            {
                acceptedTransactions.add(tx);
                for (Transaction.Input txInput: tx.getInputs())
                {
                    UTXO utxo = new UTXO(txInput.prevTxHash, txInput.outputIndex);
                    this.utxoPool.removeUTXO(utxo);
                }

                byte[] txHash = tx.getHash();
                int index = 0;
                for(Transaction.Output txOutput: tx.getOutputs())
                {
                    UTXO utxo = new UTXO(txHash, index);
                    index++;
                    this.utxoPool.addUTXO(utxo, txOutput);
                }
            }

        }
        return acceptedTransactions.toArray(new Transaction[acceptedTransactions.size()]);
    }

}
