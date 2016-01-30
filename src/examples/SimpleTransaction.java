package examples;

import carlstm.CarlSTM;
import carlstm.NoActiveTransactionException;
import carlstm.Transaction;
import carlstm.TransactionAbortedException;
import carlstm.TxObject;

/**
 * A simple example of a program that uses CarlSTM for synchronization.
 */
public class SimpleTransaction {
	// Create a transactional object that holds an integer.
	private static TxObject<Integer> x = new TxObject<Integer>(0);

	/**
	 * A transaction that repeatedly increments the integer value stored in a
	 * TxObject.
	 */
	static class MyTransaction implements Transaction<Integer> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see carlstm.Transaction#run()
		 */
		@Override
		public Integer run() throws NoActiveTransactionException,
				TransactionAbortedException {
			// This print may happen more than once if the transaction aborts
			// and restarts.

			// This loop repeatedly reads and writes a TxObject. The read and
			// write operations should all behave as if the entire transaction
			// happened exactly once, and as if there were no
			// intervening reads or writes from other threads.
			for (int i = 0; i < 5; i++) {
				Integer val = x.read();
				x.write(val + 1);
				System.out.println(Thread.currentThread().getName()
						+ " wrote x = " + (val + 1));
				Thread.yield();
			}
			// To prove that lazy buffering is working, print the value field of TxObject for each thread after the operation
			System.out.println(Thread.currentThread().getName()
					+ " final TxObject value without committing: " + (x.getTrueTxObjectValue()));
			return x.read();
		}
	}

	/**
	 * A Java Thread that executes a transaction and prints its result.
	 */
	static class MyThread extends Thread {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			int result = CarlSTM.execute(new MyTransaction());

			// Should print 5 or 10, depending on which thread went first.
			System.out.println(Thread.currentThread().getName() + ": " + result);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		// Create two threads
		Thread thread1 = new MyThread();
		Thread thread2 = new MyThread();

		// Start the threads (executes MyThread.run)
		thread1.start();
		thread2.start();

		// Wait for the threads to finish.
		thread1.join();
		thread2.join();
	}
}