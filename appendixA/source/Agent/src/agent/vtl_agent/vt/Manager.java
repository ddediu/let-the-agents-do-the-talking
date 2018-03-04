package agent.vtl_agent.vt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import agent.vtl_agent.Phonation;

import java.util.concurrent.ExecutionException;

import util.Util;
import nativelib.VTL;

public final class Manager {
	private static Manager instance;
	private final ExecutorCompletionService<Phonation> completionService;
	private final BlockingQueue<VocalTract> vts;
	private final List<Double> fixedParams;
	private final List<String> fixedParamNames;
	
	private Manager(final int nThreads, final List<List<String>> anatomyData, final int iAnatomy) {
		System.loadLibrary("NativeInterface");
		VTL.instantiate(nThreads);
		
		final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
		this.completionService = new ExecutorCompletionService<Phonation>(executorService);
		
		final List<List<String>> copyAnatomyData = Util.deepCopyList(anatomyData); 
		final List<String> freeParamNames = copyAnatomyData.remove(0);
		
		this.fixedParamNames = copyAnatomyData.remove(0);
		this.fixedParamNames.remove(0);
		final List<String> fixedParamStrings = copyAnatomyData.remove(iAnatomy);
		fixedParamStrings.remove(0);
		this.fixedParams = Collections.unmodifiableList(Util.stringsToDoubles(fixedParamStrings));
		
		System.out.println(this.fixedParamNames);
		System.out.println(this.fixedParams.toString());
		System.out.println(freeParamNames + "\n");
		
		this.vts = new LinkedBlockingQueue<VocalTract>(nThreads);
		for(int i = 0; i < nThreads; i++) {
			vts.add(new VocalTract(i, freeParamNames, this.fixedParamNames, this.fixedParams));
		}
	}
	
	public static Manager getInstance(final int nThreads, final List<List<String>> anatomyData, 
			final int iAnatomy) {
		
		if(Manager.instance == null) {
			Manager.instance = new Manager(nThreads, anatomyData, iAnatomy);
		}
		
		return getInstance();
	}
	
	public static Manager getInstance() {
		return Manager.instance;
	}	
	
	public void submitTask(final Callable<Phonation> task) {
		this.completionService.submit(task);
	}
	
	public Phonation takeResult() {
		try {
			final Future<Phonation> future = this.completionService.take();
			final Phonation result = future.get();
			return result;
		}
		catch(InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Phonation vocalize(final double[] params, final int nFormants, 
			final boolean phonate, final String wavPath) {
		
		final VocalTract vt = this.takeVt();
		this.setVocalizationParams(vt, params);
		
		final Phonation phonation = vt.getPhonation(nFormants, phonate, wavPath);
		
		this.addVt(vt);
		return phonation;
	}
	
	public Phonation getTarget(final List<String> fixedParamNames, 
			final List<Double> freeParams, final List<String> freeParamNames, final int nFormants, 
			final String wavPath) {
		
		final List<Double> filteredFixedParam = new ArrayList<Double>(fixedParamNames.size()); 
		
		for(int i = 0; i < this.fixedParamNames.size(); i++) {
			final String defaultParam = this.fixedParamNames.get(i);
			
			for(int j = 0; j < fixedParamNames.size(); j++) {
				final String targetParam = fixedParamNames.get(j);
				
				if(defaultParam.equals(targetParam)) {
					filteredFixedParam.add(this.fixedParams.get(i));
					break;
				}
			}
		}
		
		return this.getTarget(filteredFixedParam, fixedParamNames, freeParams, freeParamNames, 
				nFormants, wavPath);
	}	
	
	public Phonation getTarget(final List<Double> fixedParams, 
			final List<String> fixedParamNames, final List<Double> freeParams, 
			final List<String> freeParamNames, final int nFormants, final String wavPath) {
		
		final VocalTract vt = this.takeVt();
		vt.manualVocalize(fixedParams, fixedParamNames, freeParams, freeParamNames);
		final Phonation phonation = vt.getPhonation(nFormants, false, wavPath);
		
		final List<Double> formants = phonation.getFormants();

		if(formants.size() < nFormants) {
			formants.clear();
			
			for(int i = 0; i < nFormants; i++) {
				formants.add(0.);
			}
		}
		
		System.out.println(fixedParamNames.toString());
		System.out.println(fixedParams.toString());
		System.out.println(freeParamNames.toString());
		System.out.println(freeParams.toString());
		
		this.addVt(vt);
		
		return phonation;
	}
	
	private void setVocalizationParams(final VocalTract vt, final double[] params) {
		vt.vocalize(params);
	}
	
	private VocalTract takeVt() {
		try {
			final VocalTract vt = this.vts.take();
			return vt;
		}
		catch(InterruptedException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	private void addVt(final VocalTract vt) {
		this.vts.add(vt);
	}
}