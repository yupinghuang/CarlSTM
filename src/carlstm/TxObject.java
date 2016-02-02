package carlstm;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A TxObject is a special kind of object that can be read and written as part
 * of a transaction.
 * 
 * @param <T>
 *            type of the value stored in this TxObject
 */
public final class TxObject<T> {
	T value;
	private Lock lock;
	
	public TxObject(T value) {
		this.value = value;
		this.lock= new ReentrantLock();
	}

	@SuppressWarnings("unchecked")
	public T read() throws NoActiveTransactionException, TransactionAbortedException {
		// If threadTxInfo if null, it means that this TxOject is not registered with the TxInfo yet (i.e. first time read/write)
		registerTxObject();
		TxInfo threadTxInfo = CarlSTM.TxInfoThreadLocal.get();
		checkCurrentTransactionActive();
		//TODO not sure about this casting
		T currentValue = (T) threadTxInfo.readTxObjectCurrentValue(this);
		return currentValue;
	}

	public void write(T value) throws NoActiveTransactionException, TransactionAbortedException {
		// TODO implement me
		registerTxObject();
		TxInfo threadTxInfo = CarlSTM.TxInfoThreadLocal.get();
		checkCurrentTransactionActive();
		threadTxInfo.editTxObject(this, value);
	}
	
	private void checkCurrentTransactionActive() throws NoActiveTransactionException {
		TxInfo threadTxInfo = CarlSTM.TxInfoThreadLocal.get();
		if (!threadTxInfo.currentTransactionActive()) {
			throw new NoActiveTransactionException();
		}else{
				return;
			}
	}
	
	private void registerTxObject() {
		TxInfo threadTxInfo = CarlSTM.TxInfoThreadLocal.get();
		if (!threadTxInfo.hasTxObject(this)) {
			threadTxInfo.addTxObject(this);
		}else{
			return;
		}
	}
	
	/** Try to lock this TxObject, return true if locked
	 * @return boolean locked
	 */
	boolean trylock() {
		return lock.tryLock();
	}
	
	void unlock() {
		lock.unlock();
	}
	@SuppressWarnings("unchecked")
	void setValue(Object value) {
		this.value = (T) value;
	}
	/** return the true value field of the TxObject
	 * @return value
	 */
	public T getTrueTxObjectValue() {
		return value;
	}
}
