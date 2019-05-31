package stimulusdelayreward;

import java.io.FilenameFilter;
import java.io.File;

/** Accept files that have one of the provided extension.
 *
 * @author Francois Rivest
 * @version 1.0
 */


public class ExtensionFilter implements FilenameFilter {

	/** List of allowed extensions. */
	private String[] m_ExtensionsList;

	/** Construct a filter that accept any file ending with one of the provided
	 * extension. The . should not be in the string.
	 * @param     extensionsList     List of extensions allowed.
	 */
	public ExtensionFilter(String[] extensionsList) {
		m_ExtensionsList = new String[extensionsList.length];
		//preprocess by adding '.' and lower casing.
		for (int i=0; i<extensionsList.length; i++)
		{
			m_ExtensionsList[i] = "." + extensionsList[i].toLowerCase();
		}
	}

	public boolean accept(File dir, String name) {
		//preprocess
		String lname = name.toLowerCase();
		//check
		for (int i=0; i<m_ExtensionsList.length; i++) {
			if (lname.endsWith(m_ExtensionsList[i])) {
				return true;
			}
		}
		return false;
    }

}
