package carlstm;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * A TxObject is a special kind of object that can be read and written as part
 * of a transaction.
 * 
 * @param <T>
 *            type of the value stored in this TxObject
 */
public final class TxObject<T> {
	T value;
	private ReentrantReadWriteLock rwLock;
	private ReadLock readLock;
	private WriteLock writeLock;

	public TxObject(T value) {
		this.value = value;
		this.rwLock = new ReentrantReadWriteLock();
		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();
	}

	/**
	 * Get the current value of the TxObject inside the transaction
	 * 
	 * @return
	 * @throws NoActiveTransactionException
	 * @throws TransactionAbortedException
	 */
	@SuppressWarnings("unchecked")
	public T read() throws NoActiveTransactionException, TransactionAbortedException {
		// If threadTxInfo if null, it means that this TxOject is not registered
		// with the TxInfo yet (i.e. first time read/write)
		registerTxObject();
		TxInfo threadTxInfo = CarlSTM.TxInfoThreadLocal.get();
		checkCurrentTransactionActive();
		T currentValue = (T) threadTxInfo.readTxObjectCurrentValue(this);
		return currentValue;
	}

	/**
	 * Change the value of a TxObject inside the transaction, lazy buffer is
	 * used
	 * 
	 * @param value
	 * @throws NoActiveTransactionException
	 * @throws TransactionAbortedException
	 */
	public void write(T value) throws NoActiveTransactionException, TransactionAbortedException {
		registerTxObject();
		TxInfo threadTxInfo = CarlSTM.TxInfoThreadLocal.get();
		checkCurrentTransactionActive();
		threadTxInfo.editTxObject(this, value);
	}

	/**
	 * Helper function to make sure that there is an active transaction running
	 * 
	 * @throws NoActiveTransactionException
	 */
	private void checkCurrentTransactionActive() throws NoActiveTransactionException {
		TxInfo threadTxInfo = CarlSTM.TxInfoThreadLocal.get();
		if (!threadTxInfo.currentTransactionActive()) {
			throw new NoActiveTransactionException();
		} else {
			return;
		}
	}

	/**
	 * Register the TxObject with the current thread TxInfo
	 * 
	 */
	private void registerTxObject() {
		TxInfo threadTxInfo = CarlSTM.TxInfoThreadLocal.get();
		if (!threadTxInfo.hasTxObject(this)) {
			threadTxInfo.addTxObject(this);
		} else {
			return;
		}
	}

	public void lockRead() {
		readLock.lock();
	}

	public boolean tryLockRead() {
		return readLock.tryLock();
	}

	public void releaseRead() {
		try {
			readLock.unlock();
		} catch (IllegalMonitorStateException e) {

		}
	}

	public void lockWrite() {
		try {
			writeLock.lock();
		} catch (Exception e) {
		}
	}

	public void releaseWrite() {
		try {
			writeLock.unlock();
		} catch (Exception e) {
		}
	}

	@SuppressWarnings("unchecked")
	void setValue(Object value) {
		this.value = (T) value;
	}

	/**
	 * return the true value field of the TxObject
	 * 
	 * @return value
	 */
	public T getTrueTxObjectValue() {
		return value;
	}
}
