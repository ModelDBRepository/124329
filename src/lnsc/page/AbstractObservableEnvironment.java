package lnsc.page;
import java.util.Observable;

/** Optional basis for class implementing {@link Environment} interface.
 *  Derived from java.util.Observer.
 *
 *  <P> Environment derived from this class should have some switch allowing
 *  automatic observer notification. Such notification, could send the current
 *  {@link State.toDataSet} description to obervers.
 *
 * @author Francois Rivest
 * @version 1.1
 */


public abstract class AbstractObservableEnvironment extends Observable implements Environment {

}