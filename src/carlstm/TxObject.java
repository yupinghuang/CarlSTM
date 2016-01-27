package carlstm;

/**
 * A TxObject is a special kind of object that can be read and written as part
 * of a transaction.
 * 
 * @param <T> type of the value stored in this TxObject
 */
public final class TxObject<T> {
	T value;

	public TxObject(T value) {
		this.value = value;
	}

	public T read() throws NoActiveTransactionException,
			TransactionAbortedException {
		// TODO implement me
		return value;
	}

	public void write(T value) throws NoActiveTransactionException,
			TransactionAbortedException {
		// TODO implement me
		this.value = value;
	}
}
