package nativelib;

/**
 * A java facade for the entire VTL library, providing the minimal but essential methods to control
 * a vocal tract and fetch its properties. You can instantiate multiple vocaltracts, each of which
 * is accessed by the corresponding index argument the methods in this class provide.  
 * @author ricjan
 *
 */
public class VTL 
{		
	/*
	 * Cannot instantiate this class
	 */
	private VTL(){
	}
	
	/**
	 * Instantiate a number of VTs.
	 * @param size The number of VTs to instantiate.
	 */
	public native static void instantiate(int size);
	
	/**
	 * Finalise a vocal tract. Call this after you have set the parameters and want to produce
	 * e.g., acoustics.
	 * @param vtIndex Index of VT to finalize.
	 */
	public native static void finalize(int vtIndex, boolean recalculateJaw);
	
	/**
	 * Get VTL parameter names.
	 * @return An array of parameter names.
	 */
	public native static String[] getParamNames();
	
	/**
	 * Get VTL parameter abbreviations.
	 * @return An array of parameter abbreviations.
	 */
	public native static String getParamAbbreviation(String paramName);
	
	/**
	 * Get parameter value in the native VTL range.
	 * @param vtIndex VT index.
	 * @param paramName Parameter name.
	 * @return Parameter value.
	 */
	public native static double getParam(int vtIndex, String paramName);	

	/**
	 * Set a single VT parameter.
	 * @param vtIndex Index of VT.
	 * @param pName Parameter name.
	 * @param param Parameter value.
	 * @param normalized Are you providing normalised parameter values (0<p<1) (true) or are they 
	 * within the native VTL ranges (false)?
	 */	
	public native static void setParam(int vtIndex, String pName, double param, boolean normalized);
	
	/**
	 * Let tongue root be automatically calculated by VTL.
	 * @param vtIndex Index of VT.
	 * @param autoTongueRoot Should VTL determine tongue root parameters automatically?
	 */
	public native static void setAutoTongueRoot(int vtIndex, boolean autoTongueRoot);
	
	/**
	 * Set hyoid calculation Birkholz-style (classic) or scale hyoid range with vertical VT length.
	 * @param vtIndex
	 * @param classicHyoid Use Birkholz-style hyoid or restrict and scale?
	 */
	public native static void setHyoidClassic(int vtIndex, boolean classicHyoid);
	
	/**
	 * Phonate VT (play sound).
	 * @param vtIndex Index of VT to phonate.
	 */
	public native static void playSound(int vtIndex);
	
	/**
	 * Save wav-file.
	 * @param vtIndex Index of VT to phonate.
	 */
	public native static void saveWav(int vtIndex, String filePath);
	
	/**
	 * Get VT formants.
	 * @param vtIndex Index of VT to get formants from.
	 * @param nFormants Maximum number of formants to fetch.
	 * @return Formant frequencies.
	 */
	public native static double[] getFormants(int vtIndex, int nFormants);

	/**
	 * Get VT tube segment lengths.
	 * @param vtIndex Index of VT to get tube lengths from.
	 * @return Array of tube lengths.
	 */
	public native static double[] getTubeLengths(int vtIndex);

	/**
	 * Get VT tube segment areas.
	 * @param vtIndex Index of VT to get tube areas from.
	 * @return Array of tube areas.
	 */
	public native static double[] getTubeAreas(int vtIndex);
	
	/**
	 * Get Nishimura (2006)'s values.
	 * @param vtIndex
	 * @return Array containing: svtvMinX, svtvMinY, svtvMaxX, svtvMaxY, svthMinX, svthMinY, 
	 * svthMaxX, svthMaxY, hyoidX, hyoidY.
	 */
	public native static double[] getNishimura(int vtIndex);
}