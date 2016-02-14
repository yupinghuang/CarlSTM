package examples;

import carlstm.*;

/**
 * This is a Transactional implementation of a Hash Set with separate chaining
 * and no rehashing.
 * 
 * @param <T>
 *            type of the objects in the set.
 */
public class TxHashSet<T> implements Set<T> {

	/**
	 * Helper class - basically is a linked list of items that happen to map to
	 * the same hash code.
	 */
	private static class Bucket {
		/**
		 * The item stored at this entry. This is morally of type T, but Java
		 * generics do not play well with arrays, so we have to use Object
		 * instead.
		 */
		Object item;

		/**
		 * Next item in the list.
		 */
		Bucket next;

		/**
		 * Create a new bucket.
		 * 
		 * @param item
		 *            item to be stored
		 * @param next
		 *            next item in the list
		 */
		public Bucket(Object item, Bucket next) {
			this.item = item;
			this.next = next;
		}
	}

	/**
	 * Our array of items. Each location in the array stores a linked list items
	 * that hash to that locations.
	 */
	private TxObject<Bucket>[] table;

	/**
	 * Capacity of the array. Since we do not support resizing, this is a
	 * constant.
	 */
	private static final int CAPACITY = 15;

	/**
	 * Create a new HashSet.
	 */
	@SuppressWarnings("unchecked")
	public TxHashSet() {
		this.table = new TxObject[CAPACITY];
		// Initialize the TxObject's
		for (int i = 0; i < table.length; i++) {
			table[i] = new TxObject<Bucket>(null);
		}

	}

	/**
	 * A helper method wrapped by transaction to see if a bucket has a given
	 * item
	 * 
	 * @param bucketTxObject
	 * @param item
	 * @return
	 */
	private boolean contains(TxObject<Bucket> bucketTxObject, T item) {
		Boolean result = CarlSTM.execute(new Transaction<Boolean>() {
			@Override
			public Boolean run() throws NoActiveTransactionException, TransactionAbortedException {
				Bucket bucket = (Bucket) bucketTxObject.read();
				while (bucket != null) {
					if (item.equals(bucket.item)) {
						return true;
					}
					bucket = bucket.next;
				}
				return false;
			}
		});
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see examples.Set#add(java.lang.Object)
	 */
	@Override
	public boolean add(T item) {
		// Java returns a negative number for the hash; this is just converting
		// the negative number to a location in the array.
		int hash = (item.hashCode() % CAPACITY + CAPACITY) % CAPACITY;
		TxObject<Bucket> bucketTxObject = table[hash];
		// the query is a transaction
		if (contains(bucketTxObject, item)) {
			return false;
		} else {
			// the insertion is a transaction
			Boolean result = CarlSTM.execute(new Transaction<Boolean>() {
				@Override
				public Boolean run() throws NoActiveTransactionException, TransactionAbortedException {
					Bucket bucket = (Bucket) bucketTxObject.read();
					bucket = new Bucket(item, bucket);
					bucketTxObject.write(bucket);
					return true;
				}
			});
			return result;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see examples.Set#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(T item) {
		int hash = (item.hashCode() % CAPACITY + CAPACITY) % CAPACITY;
		TxObject<Bucket> bucketTxObject = table[hash];
		return contains(bucketTxObject, item);
	}

	/**
	 * test the class with multithreading
	 */
	public static void main(String[] args) throws InterruptedException {
		final int NUM_THREADS = 15;
		TxHashSet<Integer> c = new TxHashSet<Integer>();
		TestTxHashSet[] threads = new TestTxHashSet[NUM_THREADS];
		for (int j = 0; j < NUM_THREADS; j++) {
			threads[j] = new TestTxHashSet(j * 6000, (j + 1) * 6000, c);
		}
		// long startTime = System.currentTimeMillis();
		for (int j = 0; j < NUM_THREADS; j++) {
			threads[j].start();
		}
		for (int j = 0; j < NUM_THREADS; j++) {
			threads[j].join();
		}
		for (int i = 0; i < 6000 * NUM_THREADS; i++) {
			if (!c.contains(i)) {
				System.out.printf("Still missing %d\n", i);
			}
		}
		// long endTime = System.currentTimeMillis();
		// System.out.println(endTime - startTime);
	}

	/**
	 * inner class to multithread hash set operations using Thread
	 */
	static class TestTxHashSet extends Thread {
		private int low, high;
		private TxHashSet<Integer> c;

		public TestTxHashSet(int low, int high, TxHashSet<Integer> c) {
			this.low = low;
			this.high = high;
			this.c = c;
		}

		@Override
		public void run() {
			for (int i = low; i < high; i++) {
				c.add(i);

			}
			for (int i = low; i < high; i++) {
				if (!c.contains(i)) {
					System.out.printf("%d is missing\n", i);
				}
			}
			CarlSTM.getCounts();
		}
	}
}
