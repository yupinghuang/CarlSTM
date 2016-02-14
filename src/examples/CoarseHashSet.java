package examples;

import java.util.*;

/**
 * This is a simple implementation of a Hash Set with separate chaining and no
 * rehashing. We've implemented coarse locking here.
 * 
 * @param <T>
 *            type of the objects in the set.
 */
public class CoarseHashSet<T> implements Set<T> {

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
	private Bucket[] table;

	/**
	 * Capacity of the array. Since we do not support resizing, this is a
	 * constant.
	 */
	private static final int CAPACITY = 1024;

	/**
	 * Create a new HashSet.
	 */
	public CoarseHashSet() {
		this.table = new Bucket[CAPACITY];
	}

	/**
	 * A helper method to see if an item is stored at a given bucket.
	 * 
	 * @param bucket
	 *            bucket to be searched
	 * @param item
	 *            item to be searched for
	 * @return true if the item is in the bucket
	 */
	private synchronized boolean contains(Bucket bucket, T item) {
		while (bucket != null) {
			if (item.equals(bucket.item)) {
				return true;
			}
			bucket = bucket.next;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see examples.Set#add(java.lang.Object)
	 */
	@Override
	public synchronized boolean add(T item) {
		// Java returns a negative number for the hash; this is just converting
		// the negative number to a location in the array.
		int hash = (item.hashCode() % CAPACITY + CAPACITY) % CAPACITY;
		Bucket bucket = table[hash];
		if (contains(bucket, item)) {
			return false;
		}
		table[hash] = new Bucket(item, bucket);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see examples.Set#contains(java.lang.Object)
	 */
	@Override
	public synchronized boolean contains(T item) {
		int hash = (item.hashCode() % CAPACITY + CAPACITY) % CAPACITY;
		Bucket bucket = table[hash];
		return contains(bucket, item);
	}

	/**
	 * test the class with multithreading
	 */
	public static void main(String[] args) throws InterruptedException {
		final int NUM_THREADS = 40;
		CoarseHashSet<Integer> c = new CoarseHashSet<Integer>();
		testCoarseHashSet[] threads = new testCoarseHashSet[NUM_THREADS];
		for (int j = 0; j < NUM_THREADS; j++) {
			threads[j] = new testCoarseHashSet(j * 6000, (j + 1) * 6000, c);
		}
		long startTime = System.currentTimeMillis();
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
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime);
	}

	/**
	 * inner class to multithread hash set operations using Thread
	 */
	static class testCoarseHashSet extends Thread {
		private int low, high;
		private CoarseHashSet<Integer> c;

		public testCoarseHashSet(int low, int high, CoarseHashSet<Integer> c) {
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
		}
	}
}
