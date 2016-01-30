package carlstm;

import java.util.HashMap;

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
	private static final boolean DEBUG = true;
	boolean currentTransactionActive() {
		return currentTxActive;
	}
	
	void start() throws TransactionAlreadyActiveException {
		if (currentTxActive) {
			throw new TransactionAlreadyActiveException();
		}
		currentValues = new HashMap<>();
		initialValues = new HashMap<>();
		currentTxActive = true;
		if (DEBUG) {
			System.out.println(Thread.currentThread().getName()+" TxInfo started");
		}
	}

	/**
	 * Try to commit a completed transaction. This method should update any
	 * written TxObjects, acquiring locks on those objects as needed.
	 * 
	 * @return true if the commit succeeds, false if the transaction aborted
	 */
	boolean commit() {
		// TODO implement me
		return false;
	}

	/**
	 * This method cleans up any transactional state if a transaction aborts.
	 */
	void abort() {
		// TODO implement me
	}

	@SuppressWarnings("rawtypes")
	boolean hasTxObject(TxObject txobject) {
		if (initialValues.containsKey(txobject)) {
			return true;
		}else{
			return false;
		}
	}
	@SuppressWarnings("rawtypes")
	void addTxObject(TxObject txobject) {
		initialValues.put(txobject, txobject.value);
		currentValues.put(txobject, txobject.value);
		if (DEBUG) {
			System.out.println(Thread.currentThread().getName()+" Txobject added to TxInfo");
		}
	}

	@SuppressWarnings("rawtypes")
	void editTxObject(TxObject txobject, Object value) throws TransactionAbortedException {
		if (!currentValues.containsKey(txobject)) {
			throw new TransactionAbortedException("TxObject not registered in TxInfo for " + txobject.toString());
		} else {
			currentValues.put(txobject, value);
		}
		if (DEBUG) {
			System.out.println(Thread.currentThread().getName()+" Txobject value updated to "+value);
		}
	}

	@SuppressWarnings("rawtypes")
	Object readTxObjectCurrentValue(TxObject txobject) {
		if (DEBUG) {
			System.out.println(Thread.currentThread().getName()+" Get updated value for TxObject");
		}
		return currentValues.get(txobject);
	}
}
