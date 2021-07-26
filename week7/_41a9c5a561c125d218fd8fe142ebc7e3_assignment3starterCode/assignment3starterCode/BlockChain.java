// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.*;

public class BlockChain {
    protected static final int CUT_OFF_AGE = 10;
    public ArrayList<ArrayList<Block>> blockPool;
    protected TxHandler transactionHandler;
    protected TransactionPool txPool;
    protected UTXOPool utxoPool;
    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        txPool = new TransactionPool();
        utxoPool = new UTXOPool();

        blockPool = new ArrayList<ArrayList<Block>>();
        ArrayList<Block> newBlockList = new ArrayList<Block>();
        newBlockList.add(genesisBlock);
        blockPool.add(newBlockList);


    }

    public TxHandler getTxHandler(){
        return transactionHandler;
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return blockPool.get(blockPool.size()-1).get(0);
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
        if(block == null || block.getPrevBlockHash() == null){
            return false;
        }


        transactionHandler = new TxHandler(getMaxHeightUTXOPool());
        Transaction[] transactions = block.getTransactions().toArray(new Transaction[0]);

        for(int i = 0; i < block.getTransactions().size();i++){
            for(int j = 0; j < block.getTransactions().size();j++){
                if(i != j){

                    for (Transaction.Input in : block.getTransaction(i).getInputs()){
                        for(Transaction.Input jin: block.getTransaction(j).getInputs()){
                            if(Arrays.equals(jin.prevTxHash, in.prevTxHash) && Arrays.equals(jin.signature, in.signature)){
                                return false;
                            }
                        }
                    }

                }
            }
        }

        Transaction[] validTransactions = transactionHandler.handleTxs(transactions);

//        block.getTransactions().clear();
//        for (Transaction t: validTransactions){
//            block.getTransactions().add(t);
//        }



        int height = getHeightParent(block)+1;

        if (height == 0){
            return false;
        }
        if(height <= getHeight(getMaxHeightBlock()) - CUT_OFF_AGE){
            return false;
        }

        utxoPool = transactionHandler.getUTXOPool();
        Transaction coinbase = block.getCoinbase();
        this.addTransaction(coinbase);
        for (int i = 0; i < coinbase.numOutputs(); i++) {
            Transaction.Output out = coinbase.getOutput(i);
            UTXO utxo = new UTXO(coinbase.getHash(), i);
            utxoPool.addUTXO(utxo, out);
        }

        return this.addBlockPool(block) ;

    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        getTransactionPool().addTransaction(tx);
    }

    public boolean addBlockPool(Block block){
        int height = 1;
        for (ArrayList<Block> list : blockPool){
            for (Block b : list){

                if (b.getHash() == block.getPrevBlockHash()){
                    if (height == blockPool.size()) {
                        blockPool.add(new ArrayList<Block>());
                    }
                    blockPool.get(height).add(block);


                    return true;
                }

            }
            height++;
        }
        return false;
    }

    public int getHeightParent(Block block){
        int height = 1;
        for (ArrayList<Block> list : blockPool){
            for (Block b : list){

                if (b.getHash() == block.getPrevBlockHash()){
                    return height;
                }
            }
            height++;
        }
        return -1;
    }

    public int getHeight(Block block){
        int height = 1;
        for (ArrayList<Block> list : blockPool){
            for (Block b : list){

                if (b.getHash() == block.getHash()){
                    return height;
                }
            }
            height++;
        }
        return -1;
    }

}