package lnsc;

/** Exception raised when there are problems with a DataSet.
 *
 * @see DataSet
 * @author Francois Rivest
 * @version 1.0
 */
public class DataSetException extends RuntimeException {

  public DataSetException() {
  }

  public DataSetException(String message) {
    super(message);
  }

  public DataSetException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataSetException(Throwable cause) {
    super(cause);
  }
}