package carlstm;

/**
 * This transaction is thrown if {@link CarlSTM#execute(Transaction)} is called
 * within an already-executing transaction.
 */
@SuppressWarnings("serial")
public class TransactionAlreadyActiveException extends RuntimeException {

}
