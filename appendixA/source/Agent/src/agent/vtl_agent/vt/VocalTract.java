package agent.vtl_agent.vt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import agent.vtl_agent.Phonation;
import nativelib.VTL;
import util.Util;

final class VocalTract {
	private final List<String> freeParamNames;
	private final List<String> fixedParamNames;
	private final double[] fixedParams;
	private final boolean autoTongueRoot;
	private final int id;
	private boolean jawInit;
	
	VocalTract(final int id, final List<String> freeParamNames, final List<String> fixedParamNames, 
			final List<Double> fixedParams) {
		
		this.id = id;
		this.freeParamNames = Collections.unmodifiableList(freeParamNames);
		this.fixedParamNames = Collections.unmodifiableList(fixedParamNames);	
		this.fixedParams = Util.listToArray(fixedParams);
		this.autoTongueRoot = this.isAutoTongueRoot(freeParamNames);
		this.jawInit = false;
	}
	
	void manualVocalize(final List<Double> fixedParams, final List<String> fixedParamNames, 
			final List<Double> freeParams, final List<String> freeParamNames) {
		
		VTL.setHyoidClassic(this.id, true);
		final boolean recalculateJaw = this.setFixedParams(Util.listToArray(fixedParams), fixedParamNames);
		final boolean autoTongueRoot = this.isAutoTongueRoot(fixedParamNames);
		this.setFreeParams(Util.listToArray(freeParams), freeParamNames,false, autoTongueRoot);
		VTL.finalize(this.id, recalculateJaw);
	}
	
	void vocalize(final double[] freeParams) {
		VTL.setHyoidClassic(this.id, false);
		final boolean recalculateJaw = this.setFixedParams(this.fixedParams, this.fixedParamNames);
		this.setFreeParams(freeParams, this.freeParamNames, true, null);
		VTL.finalize(this.id, recalculateJaw);
	}
	
	Phonation getPhonation(final int nFormants, final boolean phonate,
			final String wavPath) {
		
		if(phonate) {
			VTL.playSound(this.id);
			
			try {
				Thread.sleep(1500);
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(wavPath != null) {			
			VTL.saveWav(this.id, wavPath + ".wav");
		}
		
		final List<Double> formants = this.getFormants(nFormants);
		final List<Double> freeParams = this.getVtlFreeParams();
		final List<Double> fixedParams = this.getVtlFixedParams();
		final List<Double> nishimura = Util.arrayToList(VTL.getNishimura(this.id));
		
		final Phonation phonation = new Phonation(formants, freeParams, fixedParams, nishimura);		
		
		return phonation;
	}		
	
	private void setFreeParams(final double[] params, final List<String> paramNames, 
			final boolean normalized, final Boolean autoTongueRoot) {
		
		final int nFreeParams;
		
		if(autoTongueRoot == null) {
			VTL.setAutoTongueRoot(this.id, this.autoTongueRoot);
			nFreeParams = paramNames.size();			
		}
		else {
			VTL.setAutoTongueRoot(this.id, autoTongueRoot);
			
			if(this.autoTongueRoot && !autoTongueRoot) {
				nFreeParams = paramNames.size() + 2;
			}
			else if(!this.autoTongueRoot && autoTongueRoot) {
				nFreeParams = paramNames.size() - 2;
			}			
			else {
				nFreeParams = paramNames.size();
			}
		}
		
		if(params.length == nFreeParams) {
			this.setParams(paramNames, params, normalized);
		}
		else {
			throw new IllegalArgumentException("Illegal parameter set size: !");
		}
	}
	
	private boolean setFixedParams(final double[] params, final List<String> paramNames) {			
		if(params.length == paramNames.size()) {
			
			boolean recalculateJaw = false;
			
			if(!this.jawInit) {
				this.jawInit = true;
				recalculateJaw = true;
			}
			else {				
				for(int i = 0; i < paramNames.size(); i++) {
					final String name = paramNames.get(i);
					
					if(name.startsWith("Ma") || name.startsWith("Palat") || name.startsWith("Alveo")) {											
						if(params[i] != Util.round(VTL.getParam(this.id, name), 6)) {
							recalculateJaw = true;
							break;
						}
					}
				}
			}

			this.setParams(paramNames, params, false);
			return recalculateJaw;
		}
		else {
			throw new IllegalArgumentException("Illegal parameter set size!");
		}
	}	
	
	private void setParams(final List<String> paramNames, final double [] paramValues, 
			final boolean normalized) {
		
		for(int i = 0; i < paramValues.length; i++) {
			final String paramName = paramNames.get(i);
			final double paramValue = paramValues[i];
			VTL.setParam(this.id, paramName, paramValue, normalized);
		}
	}	
	
	private List<Double> getVtlFreeParams() {
		final List<Double> vtlParams = new LinkedList<Double>();
		
		for(final String paramName: this.freeParamNames) {
			final double param = VTL.getParam(this.id, paramName);
			vtlParams.add(param);
		}
		
		return vtlParams;
	}
	
	private List<Double> getVtlFixedParams() {
		final List<Double> vtlParams = new LinkedList<Double>();
		
		for(final String paramName: this.fixedParamNames) {
			final double param = VTL.getParam(this.id, paramName);
			vtlParams.add(param);
		}
		
		return vtlParams;
	}		
	
	private List<Double> getFormants(final int nFormants) {
		final double[] formants = VTL.getFormants(this.id, nFormants);
		
		final List<Double> barksFormants = new ArrayList<Double>(formants.length);
		for(final double formant: formants) {
			barksFormants.add((26.81 / (1 + (1960 / formant))) - 0.53);
		}
		
		return barksFormants;
	}
	
	private boolean isAutoTongueRoot(final List<String> paramNames) {
		
		boolean autoTongueRoot = true;
		
		for(final String tongueRootName: new String[] {"Tongue root X", "Tongue root Y"}) {
			if(paramNames.contains(tongueRootName)) {
				autoTongueRoot = false;
				break;
			}
		}
		
		return autoTongueRoot;
	}	
}