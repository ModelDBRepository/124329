package stimulusdelayrewardanalyzer;

/** Set of SQL tools for the environment and simulations data.
 *
 * @author Francois Rivest
 * @version 1.0
 */


public class SQLTools {

	/** Construct an SQL INSERT statement in string
	 * @param tableName    The table name
	 * @param fieldNames   The field names
	 * @param values       The field values in the same order and properly
	 *                     quoted when necessary (eg: string 'my example')
	 * @return A valid SQL insert statement
	 */
	public static String buildSQLInsert(String tableName,
										String[] fieldNames,
										String[] values) {
		String insert = "INSERT INTO " + tableName + " (";
		for (int i=0; i<fieldNames.length-1; i++) {
			insert += fieldNames[i] + ", ";
		}
		insert += fieldNames[fieldNames.length-1] + ") VALUES (";
		for (int i=0; i<values.length-1; i++) {
			insert += values[i] + ", ";
		}
		insert += values[values.length-1] + ")";
		return insert;
    }

}