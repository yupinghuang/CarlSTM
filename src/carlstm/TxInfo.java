package carlstm;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class holds transactional state for a single thread. You should use
 * {@link java.lang.ThreadLocal} to allocate a TxInfo to each Java thread. This
 * class is only used within the STM implementation, so it and its members are
 * set to package (default) visibility.
 */
class TxInfo {
	/**
	 * Start a transaction by initializing any necessary state. This method
	 * should throw {@link TransactionAlreadyActiveException} if a transaction
	 * is already being executed.
	 */
	private boolean currentTxActive;
	private HashMap<TxObject<?>, Object> initialValues;
	private HashMap<TxObject<?>, Object> currentValues;
	private LinkedList<TxObject<?>> lockedTxObjects;
	private static final boolean DEBUG = false;
	// Variable indicating if another thread is holding the writeLock, if
	// shouldWait is true, then exponential backoff should be used
	public boolean shouldWait;

	boolean currentTransactionActive() {
		return currentTxActive;
	}

	/**
	 * Initialize the thread transaction
	 * 
	 * @throws TransactionAlreadyActiveException
	 */
	void start() throws TransactionAlreadyActiveException {
		if (currentTxActive) {
			throw new TransactionAlreadyActiveException();
		}
		currentValues = new HashMap<>();
		initialValues = new HashMap<>();
		lockedTxObjects = new LinkedList<>();
		currentTxActive = true;
		shouldWait = false;
		if (DEBUG) {
			System.out.println(Thread.currentThread().getName() + " TxInfo started");
		}
	}

	/**
	 * Try to commit a completed transaction. This method should update any
	 * written TxObjects, acquiring locks on those objects as needed.
	 * 
	 * @return true if the commit succeeds, false if the transaction fails
	 *         unmanageably
	 * @throws TransactionAbortedException
	 *             if the commit aborts
	 */
	@SuppressWarnings({})
	boolean commit() throws TransactionAbortedException {
		try {
			for (TxObject<?> txobject : initialValues.keySet()) {
				// Try readlock, if unable to readlock, then use exponential backoff
				if (!txobject.tryLockRead()) {
					shouldWait = true;
					throw new TransactionAbortedException();
				} else {
					// locked
					lockedTxObjects.add(txobject);
				}
			}
			// Check if the object value matches the current value registered,
			// if so, release the readlock, if not, aquires the writelock and
			// overwrite it
			for (TxObject<?> txobject : initialValues.keySet()) {
				if (txobject.getTrueTxObjectValue() != initialValues.get(txobject)) {
					// inconsistent state
					throw new TransactionAbortedException();
				} else {
					if (txobject.getTrueTxObjectValue() == currentValues.get(txobject)) {
						txobject.releaseRead();
					} else {
						try {
							txobject.releaseRead();
							txobject.lockWrite();
							if (txobject.getTrueTxObjectValue() != initialValues.get(txobject)) {
								// inconsistent state
								throw new TransactionAbortedException();
							} else {
								txobject.setValue(currentValues.get(txobject));
							}
						} finally {
							txobject.releaseWrite();
						}
					}
				}
			}
			if (DEBUG) {
				System.out.println(Thread.currentThread().getName() + " transaction committed");
			}
		} finally {
			unlockAll();
		}
		return true;
	}

	/**
	 * Unlock all the readLocks acquired in this thread
	 */
	private void unlockAll() {
		for (TxObject<?> txobject : lockedTxObjects) {
			txobject.releaseRead();
		}
		lockedTxObjects.clear();
	}

	/**
	 * This method cleans up any transactional state if a transaction aborts.
	 * Also unlock all TxObjects that have been readlock'ed (and maybe already
	 * unlocked
	 */
	void abort() {
		unlockAll();
		currentTxActive = false;
		currentValues.clear();
		initialValues.clear();
		System.out.println(Thread.currentThread().getName() + " transaction aborted");
	}

	@SuppressWarnings("rawtypes")
	/**
	 * Check if a TxObject object is registered with the thread TxInfo
	 * 
	 * @param txobject
	 * @return registered
	 */
	boolean hasTxObject(TxObject txobject) {
		if (initialValues.containsKey(txobject)) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	/**
	 * Register a TxObject with the TxInfo
	 * 
	 * @param txobject
	 */
	void addTxObject(TxObject txobject) {
		initialValues.put(txobject, txobject.value);
		currentValues.put(txobject, txobject.value);
		if (DEBUG) {
			System.out.println(Thread.currentThread().getName() + " Txobject added to TxInfo");
		}
	}

	@SuppressWarnings("rawtypes")
	/**
	 * Lazy buffer the changes made to a TxObject
	 * 
	 * @param txobject
	 * @param value
	 * @throws TransactionAbortedException
	 */
	void editTxObject(TxObject txobject, Object value) {
		currentValues.put(txobject, value);
		if (DEBUG) {
			System.out.println(Thread.currentThread().getName() + " Txobject value updated to " + value);
		}
	}

	@SuppressWarnings("rawtypes")
	Object readTxObjectCurrentValue(TxObject txobject) {
		if (DEBUG) {
			System.out.println(Thread.currentThread().getName() + " Get updated value for TxObject");
		}
		return currentValues.get(txobject);
	}
}
