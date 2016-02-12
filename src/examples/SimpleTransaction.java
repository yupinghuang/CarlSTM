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
	private static TxObject<String> y = new TxObject<String>("a");

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
		public Integer run() throws NoActiveTransactionException, TransactionAbortedException {
			// This print may happen more than once if the transaction aborts
			// and restarts.

			// This loop repeatedly reads and writes a TxObject. The read and
			// write operations should all behave as if the entire transaction
			// happened exactly once, and as if there were no
			// intervening reads or writes from other threads.
			for (int i = 0; i < 5; i++) {
				Integer val = x.read();
				// String valy = y.read();
				x.write(val + 1);
				// y.write(valy+valy);
				System.out.println(Thread.currentThread().getName() + " wrote x = " + (val + 1));
				// System.out.println(Thread.currentThread().getName()
				// + " wrote y = " + valy + valy);
				Thread.yield();
			}
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
			Integer result = CarlSTM.execute(new MyTransaction());
			// Should print 5 or 10, depending on which thread went first.
			if (result != null) {
				System.out.println(Thread.currentThread().getName() + "x: " + result);
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		// Create two threads
		Thread thread1 = new MyThread();
		Thread thread2 = new MyThread();
		Thread thread3 = new MyThread();
		Thread thread4 = new MyThread();

		// Start the threads (executes MyThread.run)
		thread1.start();
		thread2.start();
		thread3.start();
		thread4.start();
		// Wait for the threads to finish.
		thread1.join();
		thread2.join();
		thread3.join();
		thread4.join();
	}
}