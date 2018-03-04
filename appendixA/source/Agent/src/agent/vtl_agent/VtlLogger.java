package agent.vtl_agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.uncommons.watchmaker.framework.EvaluatedCandidate;

import agent.AbstractLogger;
import parameters.AgentParams;
import util.FileHandler;
import util.Util;

public final class VtlLogger extends AbstractLogger<VtlSolution> {
	private final FileHandler elitePhenoFileHandler;
	private final List<List<Double>> targetsAcoustics;
	private final List<String> targetNames;

	public VtlLogger(final FileHandler eliteGenoFileHandler, final FileHandler elitePhenoFileHandler,
			final FileHandler populationFileHandler, final AgentParams agentParams, 
			final List<Phonation> targets, final List<Phonation> altTargets, final int saveInterval,	
			final HashMap<String, String> paths, final int iGeneration, final VtlSolution prevElite) {
		
		super(eliteGenoFileHandler, populationFileHandler, agentParams, 
				targets, saveInterval, paths, iGeneration, prevElite);
		
		this.elitePhenoFileHandler = elitePhenoFileHandler;
		this.targetsAcoustics = Phonation.getAcTargets(targets);
		this.targetNames = agentParams.targetNames();
		
		final boolean firstRun = prevElite == null ? true : false;
		
		if(firstRun) {
			try {
				this.writeEliteHeaders(agentParams, elitePhenoFileHandler);
				this.writeEliteSubHeaders(targets, elitePhenoFileHandler, "target");
				this.writeEliteSubHeaders(altTargets, elitePhenoFileHandler, "alt");
				super.writePopulationHeaders(agentParams);
			} 
			catch (IOException e) {
				System.err.println("Failed to write to logger!");
				e.printStackTrace();
				System.exit(-1);
			}
		}		
	}
	
	@ Override
	protected final void writeEliteHeaders(final AgentParams agentParams, 
			final FileHandler elitePhenoFileHandler) throws IOException {
		
		super.writeEliteHeaders(agentParams, null);
		
		elitePhenoFileHandler.write("generation,");
		
		// elite formants
		for(final String targetName: agentParams.targetNames()) {
			for(int iFormant = 0; iFormant < agentParams.nFormants(); iFormant++) {
				elitePhenoFileHandler.write(targetName + "_F" + (iFormant + 1) + ",");
			}
		}
		
		// elite vtl params
		super.writeParamsHeader(agentParams, elitePhenoFileHandler, "");
		
		this.writeNishimuraHeader(agentParams, elitePhenoFileHandler);
		
		elitePhenoFileHandler.newLine();
	}
	
	@ Override
	protected final void logPopulation(
			final List<EvaluatedCandidate<VtlSolution>> evaluatedPopulation) throws IOException{
		
		super.logPopulation(evaluatedPopulation);
		
		final List<List<List<Double>>> params = new LinkedList<List<List<Double>>>();
		
		for(final EvaluatedCandidate<VtlSolution> candidate: evaluatedPopulation) {
			final VtlSolution solution = candidate.getCandidate();
			
			if(solution.getMRMSE() != Double.POSITIVE_INFINITY) {
				
    			final List<List<Double>> solutionParams = new LinkedList<List<Double>>();
				final List<Phonation> phonations = solution.getPhenotype().getValues();
    			for(final Phonation phonation: phonations) {
    				solutionParams.add(phonation.getFreeParams());
    			}
    			params.add(solutionParams);
			}
		}		
		
		final int nValues = params.get(0).size() * params.get(0).get(0).size(); 
		final List<Double> paramMeans = new ArrayList<Double>(nValues);
		final List<Double> paramSds = new ArrayList<Double>(nValues);
		
		for(final List<List<Double>> sound: Util.zip(params)) {
			for(List<Double> vtlParam: Util.zip(sound)) {
				paramMeans.add(Util.mean(vtlParam));
				paramSds.add(Util.sd(vtlParam));
			}
		}
		
		for(final double mean: paramMeans) {
			super.populationFileHandler.writeValue(mean, 4);
		}
		
		for(final double sd: paramSds) {
			super.populationFileHandler.writeValue(sd, 4);
		}
		
		super.populationFileHandler.newLine();		
	}
	
	
	@ Override
	protected final boolean logElites(final VtlSolution elite) throws IOException {
		final boolean isEliteNew = super.logElites(elite);
		
		if(isEliteNew) {
			this.elitePhenoFileHandler.writeValue(this.iGeneration, 0);
			
			// log formants and save wav
			final List<Phonation> phonations = elite.getPhenotype().getValues();
		    
		    for(int i = 0; i < phonations.size(); i++) {	
		    	final Phonation phonation = phonations.get(i);
		    	final List<Double> formants = phonation.getFormants();
		    	this.elitePhenoFileHandler.writeValues(formants, 4);
		    	
		    	final String wavRoot = this.paths.get("wav");
		    	
		    	if(wavRoot != null) {
			    	final String wavPath = wavRoot + this.iGeneration + "_" + this.targetNames.get(i);
			    	final List<Double> acTarget = this.targetsAcoustics.get(i);
			    	elite.writeWav(acTarget, wavPath);
		    	}
		    }
		    
		    // log VTL parameters
		    for(final Phonation phonation: phonations) {
		    	this.elitePhenoFileHandler.writeValues(phonation.getFreeParams());
		    }
		    
		    this.logNishimura(phonations);
		    
		    this.elitePhenoFileHandler.newLine();
		}
	    
	    return isEliteNew;
	}
	
	@ Override
	protected final void writeEliteSubHeaders(final List<Phonation> targets,
			final FileHandler elitePhenoFileHandler, final String label) throws IOException {
		
		elitePhenoFileHandler.write(label + ",");
		
		final List<List<Double>> targetsAcoustics = new ArrayList<List<Double>>(targets.size());
		final List<List<Double>> targetsParams = new ArrayList<List<Double>>(targets.size());
		final List<List<Double>> targetsNishimura = new ArrayList<List<Double>>(targets.size());
		
		for(final Phonation target: targets) {
			targetsAcoustics.add(target.getFormants());
			targetsParams.add(target.getFreeParams());
			targetsNishimura.add(target.getNishimura());
		}
		
		for(final List<Double> targetAcoustics: targetsAcoustics) {
			elitePhenoFileHandler.writeValues(targetAcoustics, 4);
		}
		
		for(final List<Double> targetParams: targetsParams) {
			elitePhenoFileHandler.writeValues(targetParams);
		}
		
		for(final List<Double> targetNishimura: targetsNishimura) {
			final List<Double> varNishimura = targetNishimura.subList(0, 4);
			elitePhenoFileHandler.writeValues(varNishimura, 4);
		}
		
		final List<Double> targetNishimura = targetsNishimura.get(0);
		elitePhenoFileHandler.writeValues(targetNishimura.subList(4, targetNishimura.size()), 4);
		
		elitePhenoFileHandler.newLine();
	}
	
	private void writeNishimuraHeader(final AgentParams params, final FileHandler fileHandler) 
			throws IOException {
		
		final String[] markers = {"hyoidX","hyoidY", "svtvMinX","svtvMinY","svtvMaxX","svtvMaxY",
				"svthMinX","svthMinY","svthMaxX","svthMaxY"};
		
		final List<String> namedMarkers = new ArrayList<String>(markers.length * 4);
		
		for(final String vowelName: params.targetNames()) {
			for(int i = 0; i < 4; i++) {
				namedMarkers.add(vowelName + "_" + markers[i]);
			}
		}
		
		for(final String marker: namedMarkers) {
			fileHandler.write(marker + ",");
		}
		
		for(int i = 4; i < markers.length; i++) {
			fileHandler.write(markers[i] + ",");
		}
	}
	
	private void logNishimura(final List<Phonation> phonations) throws IOException {
		for(final Phonation phonation: phonations) {
			final List<Double> nishimura = phonation.getNishimura().subList(0, 4);
			
			this.elitePhenoFileHandler.writeValues(nishimura, 4);
		}
	}	
}