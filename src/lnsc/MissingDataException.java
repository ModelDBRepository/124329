package lnsc;

/** Exception raised when a necessary DataName is missing from a DataSet.
 *
 * @see DataSet
 * @see DataNames
 * @author Francois Rivest
 * @version 1.0
 */
public class MissingDataException extends DataSetException {

  public String m_DataName;

  public MissingDataException() {
  }

  public MissingDataException(String message) {
    super(message);
  }

  public MissingDataException(String message, String dataName) {
    super(message);
    m_DataName = dataName;
  }

  public MissingDataException(String message, Throwable cause) {
    super(message, cause);
  }

  public MissingDataException(Throwable cause) {
    super(cause);
  }
}