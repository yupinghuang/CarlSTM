package carlstm;

/**
 * This exception is thrown if a transaction aborts mid-run.
 */
@SuppressWarnings("serial")
public class TransactionAbortedException extends Exception {

	public TransactionAbortedException(String s) {
		super(s);
	}
}
