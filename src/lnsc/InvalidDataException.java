package lnsc;

/** Exception raised when data under DataName is not of the appropriate type in a DataSet.
 *
 * @see DataSet
 * @see DataNames
 * @author Francois Rivest
 * @version 1.0
 */
public class InvalidDataException extends DataSetException {

  public String m_DataName;

  public InvalidDataException() {
  }

  public InvalidDataException(String message) {
    super(message);
  }

  public InvalidDataException(String message, String dataName) {
    super(message);
    m_DataName = dataName;
  }

  public InvalidDataException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidDataException(Throwable cause) {
    super(cause);
  }
}