package carlstm;

/**
 * This class coordinates transaction execution. You can execute a transaction
 * using {@link #execute}. For example:
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
 */
public class CarlSTM {

	/**
	 * Execute a transaction and return its result. This method needs to
	 * repeatedly start, execute, and commit the transaction until it
	 * successfully commits.
	 * 
	 * @param <T> return type of the transaction
	 * @param tx transaction to be executed
	 * @return result of the transaction
	 */
	static final ThreadLocal<TxInfo> TxInfoThreadLocal = new ThreadLocal<TxInfo>();
	public static <T> T execute(Transaction<T> tx) {
		// TODO implement me
		// Initialize the threadTxInfo and start it
		TxInfoThreadLocal.set(new TxInfo());
		TxInfoThreadLocal.get().start();
		try {
			T result= tx.run();
			TxInfoThreadLocal.get().commit();
			return result;
		} catch (NoActiveTransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (TransactionAbortedException e) {
			// TODO Auto-generated catch block
			TxInfoThreadLocal.get().abort();
			e.printStackTrace();
			return null;
		}
	}
}
