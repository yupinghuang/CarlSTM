package carlstm;

/**
 * A transaction is a subprogram for which operations on TxObjects are atomic
 * (all-or-nothing) and isolated (no intervening operations by other threads).
 * Create a transaction by implementing this interface and writing an
 * {@link #run} method that contains the transactional code, and execute the
 * transaction by passing the implementing class to {@link CarlSTM#execute}. For
 * example:
 * 
 * <pre>
 * class MyTransaction implements Transaction&lt;Integer&gt; {
 * 	TxObject&lt;Integer&gt; x;
 * 
 * 	MyTransaction(TxObject&lt;Integer&gt; x) {
 * 		this.x = x;
 * 	}
 * 
 * 	public Integer run() throws NoActiveTransactionException,
 * 			TransactionAbortedException {
 * 		int value = x.read();
 * 		x.write(value + 1);
 * 		return value;
 * 	}
 * 
 * 	public static void main(String[] args) {
 * 		TxObject&lt;Integer&gt; x = new TxObject&lt;Integer&gt;(0);
 * 		int result = CarlSTM.execute(new MyTransaction(x));
 * 		System.out.println(result);
 * 	}
 * }
 * </pre>
 * 
 * @param <T> return type of the transaction (Void if no return type)
 */
public interface Transaction<T> {
	/**
	 * The main computation performed by this transaction.
	 * 
	 * @return the result of executing the transaction
	 * @throws NoActiveTransactionException may be thrown if called outside of
	 *             {@link CarlSTM#execute}
	 * @throws TransactionAbortedException if the transaction aborts mid-run.
	 */
	public T run() throws NoActiveTransactionException,
			TransactionAbortedException;
}
