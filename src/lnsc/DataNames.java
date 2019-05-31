package lnsc;

/** <P> This class contains constants for reserved keywords used by the package
 *  to store and retrieve data in a {@link DataSet} or in a
 *  {@link DataSetCollection}. </P>
 *
 *  @see DataSet
 *  @see DataSetCollection
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */
public class DataNames {

	/** Make class non constructible. */
	protected DataNames() {}

	//FunctionalUnit DataNames

	/** An <code>Integer</code> indicating the number of patterns.
	 * @see FunctionalUnit#processPattern
	 */
	public static final String PATTERN_COUNT = "PatternCount";

	/** An array of the form <code>double[p][i]</code> where <i>p</i> is usually
	 * defined by {@link #PATTERN_COUNT} and <i>i</i> by
	 * {@link FunctionalUnit#getInputCount}.
	 * @see FunctionalUnit#processPattern
	 */
	public static final String INPUT_PATTERNS = "InputPatterns";

	/** An array of the form <code>double[p][o]</code> where <i>p</i> is
	 * usually defined by {@link #PATTERN_COUNT} and <i>o</i> by
	 * {@link FunctionalUnit#getOutputCount}.
	 * @see FunctionalUnit#processPattern
	 */
	public static final String OUTPUT_PATTERNS = "OutputPatterns";

	/** An array of the form <code>double[p][o][i]</code> where <i>p</i> is usually
	 * defined by {@link #PATTERN_COUNT}, <i>o</i> by
	 * {@link FunctionalUnit#getOutputCount}, and <i>i</i> by
	 * {@link FunctionalUnit#getInputCount}.
	 * @see FunctionalUnit#processPattern
	 */
	public static final String DERIVATIVES = "Derivatives";

	/** An array of the form <code>double[p][o][i][i]</code> where <i>p</i> is usually
	 * defined by {@link #PATTERN_COUNT}, <i>o</i> by
	 * {@link FunctionalUnit#getOutputCount}, and <i>i</i> by
	 * {@link FunctionalUnit#getInputCount}.
	 * @see FunctionalUnit#processPattern
	 */
	public static final String SECOND_DERIVATIVES = "SecondDerivatives";

        /** An array of the form <code>double[p][k]</code> where <i>p</i> is usually
         * defined by {@link #PATTERN_COUNT}, <i>k</i> by
         * {@link FunctionalUnit2#getParameterCount}.
         * @see FunctionalUnit#processPattern
         */
        public static final String PARAMETERS = "Parameters";

        /** An array of the form <code>double[p][o][k]</code> where <i>p</i> is usually
         * defined by {@link #PATTERN_COUNT}, <i>o</i> by
         * {@link FunctionalUnit#getOutputCount}, and <i>k</i> by
         * {@link FunctionalUnit2#getParameterCount}.
         * @see FunctionalUnit#processPattern
         */
        public static final String PARAMETER_DERIVATIVES = "ParameterDerivatives";

        /** An array of the form <code>double[p][o][k][k]</code> where <i>p</i> is usually
         * defined by {@link #PATTERN_COUNT}, <i>o</i> by
         * {@link FunctionalUnit#getOutputCount}, <i>k</i> by
         * {@link FunctionalUnit2#getParameterCount}.
         * @see FunctionalUnit#processPattern
         */
        public static final String PARAMETER_SECOND_DERIVATIVES = "ParameterSecondDerivatives";

	/** An array of the form <code>double[p][o][i][i]</code> where <i>p</i> is
	 * usually defined by {@link #PATTERN_COUNT} and <i>o</i> by
	 * {@link FunctionalUnit#getOutputCount}.
	 * @see FunctionalUnit#processPattern
	 */
	public static final String TARGET_PATTERNS = "TargetPatterns";

	/** An array of the form <code>double[p][o]</code> where <i>p</i> is
	 * usually defined by {@link #PATTERN_COUNT} and <i>o</i> by
	 * {@link FunctionalUnit#getOutputCount}. It is usually the elementwise
	 * subtraction of {@link #OUTPUT_PATTERNS} minus {@link #TARGET_PATTERNS}.
	 * @see FunctionalUnit#processPattern
	 */
	public static final String ERROR_PATTERNS = "ErrorPatterns";


	/** A <code>DataSetCollection</code> where the number of pattern is
	 * usually defined by {@link #PATTERN_COUNT}. It is usually the elementwise
	 * to the {@link #INPUT_PATTERNS} array.
	 * @see FunctionalUnit2#processPattern
	 */
	public static final String EXTRA_DATA = "ExtraData";

		//AbstractFunctionalUnit DataNames

		//BiasUnit DataNames

		//AbstractSimpleUnit DataNames

		    //GaussianUnit DataNames

			//LinearUnit DataNames

	    	//LogisticUnit DataNames

		    //TanhUnit DataNames

			//SinUnit DataNames

			//ThresholdUnit DataNames

		//LayerUnit DataNames

		//MultiLayerNetwork
		public static final String LAYER_OUTPUTS = "LayerOutputs";
		public static final String LAYER_DERIVATIVES = "LayerDerivatives";

		//CascadeNetwork
		public static final String INTERNAL_OUTPUTS = "InternalOutputs";
		public static final String INTERNAL_DERIVATIVES = "InternalDerivatives";
		public static final String INTERNAL_SECOND_DERIVATIVES = "InternalSecondDerivatives";
		//public static final String HIDDEN_OUTPUTS = "HiddenOutputs";
		public static final String HIDDEN_DERIVATIVES = "HiddenDerivatives";
		public static final String HIDDEN_SECOND_DERIVATIVES = "HiddenSecondDerivatives";
		public static final String OUTPUTLAYER_DERIVATIVES = "OutputLayerDerivative";
		public static final String OUTPUTLAYER_SECOND_DERIVATIVES = "OutputLayerSecondDerivative";

		//ComposedFunction
		public static final String INTERMEDIATE_OUTPUTS = "IntermediateOutputs";

		//FastSingleLayerNetwork
		public static final String NET_INPUT = "NetInput";

	//Optimizer DataNames
	public static final String VALUE = "Value";
	public static final String GRADIENT = "Gradient";
	public static final String VARIABLES = "Variables";
	public static final String VARIABLE_CHANGES = "VariableChanges";

		//BackProp DataNames

		//QuickProp DataNames

		//RProp DataNames
		public static final String UPDATE_VALUES = "UpdateValues";

	//GenericTest DataNames

		//IntervalTest DataNames

		//DisjunctiveTest DataNames

	//Statistic DataNames

		//AbstractStatistic DataNames

		//CovarianceMatrix
		public final static String STAT_COV_MATRIX = "CovarianceMatrix";

		//CorrelationMatrix
		public final static String STAT_CORR_MATRIX = "CorrelationMatrix";

		//ErrorBits
		public final static String STAT_ERROR_BITS = "ErrorBits";

		//FahlmanErrorPatterns
		public final static String STAT_FAHLMAN_ERROR_PAT = "FahlmanErrorPatterns";

		//FahlmanNormalizedCovariance, RivestNormalizedCovariance,
		//RivestFrobeniusCovariance
		public final static String STAT_NORM_COV = "NormalizedCovariance";

		//MeanVector
		public final static String STAT_MEAN_VECTOR = "MeanVector";

		//SumSquared
		public final static String STAT_SUM_SQUARED = "SumSquared";

		//MeanSquared
		public final static String STAT_MEAN_SQUARED = "MeanSquared";

		//TRS
		public final static String STAT_TRS = "TRS";

		//FrobeniusNorm
		public final static String STAT_FROBENIUS_NORM = "FrobeniusNorm";

		//SSE, MSE, LogSSE, HalfSSE, HalfMSE
		public final static String STAT_SSE = "SSE";
		public final static String STAT_MSE = "MSE";
                public final static String STAT_HALFSSE = "HalfSSE";
                public final static String STAT_HALFMSE = "HalfMSE";
                public final static String STAT_LOGSSE = "LogSSE";

		//Contributions
		public final static String STAT_CONTRIBUTIONS = "Contributions";

	//AbstractBatchLearning
	public final static String EPOCH = "Epoch";
	public final static String ANALYSIS = "Analysis";
	public final static String STOPPING_REASON = "StoppingReason";
	public final static String OPTIMIZATION = "Optimization";

		//CascadeCorrelationLearning
        public final static String SUM_SQUARED_ERROR = "SumSquaredError";
		public final static String CANDIDATE_COVARIANCES = "CandidateCovariances";
		public final static String CANDIDATE_DERIVATIVES = "CandidateDerivatives";
		public final static String MEAN_ERROR = "MeanError";
		public final static String PHASE = "Phase";
		public final static String OUTPUT_WEIGHTS = "OutputWeights";
		public final static String ENDPHASE_ANALYSIS = "EndPhaseAnalysis";
		public final static String CANDIDATE_SCORES = "CandidateScores";
		public final static String CANDIDATE_OPTIMIZATIONS = "CandidateOptimizations";
		public final static String FAHLMAN_SUM_SQUARED_ERROR = "FahlmanSumSquaredError";
		public final static String CANDIDATE_WEIGHTS = "CandidateWeights";
		public final static String CANDIDATE_VALUES = "CandidateValues";
		public final static String BEST_CANDIDATE = "BestCandidate";
		public final static String BEST_SCORE = "BestScore";
		public final static String INSTALLED_CANDIDATE_NAME = "InstalledCandidateName";
		public final static String WEIGHT_UPDATE_COUNT = "WeightUpdateCount";

	//On-line trainer
	public final static String GRADIENTS = "Gradients";


	//Tools
	/** A {@link DataSet} containing at least {@link #INPUT_PATTERNS},
	 * {@link #TARGET_PATTERNS} and {@link #PATTERN_COUNT}.
	 */
	public final static String TRAIN_SET = "TrainSet";
	/** A {@link DataSet} containing at least {@link #INPUT_PATTERNS},
	 * {@link #TARGET_PATTERNS} and {@link #PATTERN_COUNT}.
	 */
	public final static String TEST_SET = "TestSet";


	/*********************************************************************/
	//Static helper funtions

	/** Indicates whether a given keyword is a member of a given keyword list.
	 *  @param          keyword             The keyword to search for
	 *  @param          keywordList         The list of keywords to search in
	 *  @return         true if the keyword is in the list
	 */
	public static boolean isMember(String keyword, String[] keywordsList)
	{
		int i;
		for (i=0; i<keywordsList.length; i++)
		{
			if (keywordsList[i].equalsIgnoreCase(keyword)) return true;
		}
		return false;
	}

	/** Concatenates two lists of keywords in one. Repetitions are not removed.
	 *  @param          list1               The first list
	 *  @param          list2               The second list
	 *  @return         The concatenation of the lists.
	 */
	public static String[] concat(String[] list1, String[] list2)
	{
		int i;
		String[] ret = new String[list1.length + list2.length];
		for (i=0; i<list1.length; i++)
		{
			ret[i] = list1[i];
		}
		for (i=0; i<list2.length; i++)
		{
			ret[list1.length+i] = list2[i];
		}
		return ret;
	}

	/** Defines an empty record list. */
	public static final String[] EMPTY_RECORDLIST = new String[0];

}