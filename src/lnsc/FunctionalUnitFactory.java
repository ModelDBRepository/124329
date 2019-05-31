package lnsc;

/** General interfaced shared by FunctionalUnit factories.
 *
 * @author Francois Rivest
 * @version 1.0
 */

public interface FunctionalUnitFactory {

	/** Creates a new functional unit. */
    public FunctionalUnit createUnit();

}