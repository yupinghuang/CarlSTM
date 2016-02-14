package carlstm;

import java.util.concurrent.TimeUnit;

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
 * 	public Integer run() throws NoActiveTransactionException, TransactionAbortedException {
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
	 * @param <T>
	 *            return type of the transaction
	 * @param tx
	 *            transaction to be executed
	 * @return result of the transaction
	 */
	static final ThreadLocal<TxInfo> TxInfoThreadLocal = new ThreadLocal<TxInfo>();
	// waiting time in case of backoff and the exponential factor
	private static final boolean DEBUG = false;
	private static final long sleeptimefactor = 2;
	private static ThreadLocal<Long> threadSleepTime = new ThreadLocal<Long>();

	public static void getCounts() {
		int commitCount = TxInfoThreadLocal.get().commitCount;
		int abortCount = TxInfoThreadLocal.get().abortCount;
		System.out.printf(Thread.currentThread().getName() + " Commit count: %d, Abort count: %d\n", commitCount,
				abortCount);
	}

	public static <T> T execute(Transaction<T> tx) {
		// TODO implement me
		// Initialize the threadTxInfo and start it
		threadSleepTime.set(new Long(20));
		if (TxInfoThreadLocal.get() == null) {
			TxInfoThreadLocal.set(new TxInfo());
		}
		TxInfoThreadLocal.get().start();
		try {
			T result = tx.run();
			TxInfoThreadLocal.get().commit();
			return result;
		} catch (NoActiveTransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (TransactionAbortedException e) {
			// Exponential backoff
			TxInfoThreadLocal.get().abort();
			if (TxInfoThreadLocal.get().shouldWait) {
				threadSleepTime.set(threadSleepTime.get() * sleeptimefactor);
				if (DEBUG) {
					System.out.println(
							Thread.currentThread().getName() + " aborted, retry in" + threadSleepTime.get() + " us");
				}
				try {
					TimeUnit.NANOSECONDS.sleep(threadSleepTime.get());
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					return null;
				}
			}
			T result = execute(tx);
			return result;
		}
	}
}
