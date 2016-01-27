package carlstm;

/**
 * This exception is thrown if the user attempts to access a {@link TxObject}
 * outside of a transaction.
 */
@SuppressWarnings("serial")
public class NoActiveTransactionException extends Exception {

}
