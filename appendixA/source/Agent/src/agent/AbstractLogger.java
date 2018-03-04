package agent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.uncommons.watchmaker.framework.EvaluatedCandidate;

import agent.vtl_agent.Phonation;
import parameters.AgentParams;
import util.FileHandler;
import util.Util;

public abstract class AbstractLogger<T extends AbstractSolution> {	
	protected final HashMap<String, String> paths;
	protected int iGeneration;
	protected final FileHandler populationFileHandler;	
	private final FileHandler eliteGenoFileHandler;
	private final int saveInterval;
	private boolean justResumed;
	private T prevElite;
	
	protected AbstractLogger(final FileHandler eliteGenoFileHandler, 
			final FileHandler populationFileHandler, final AgentParams agentParams, 
			final List<Phonation> targets, final int saveInterval, final HashMap<String, String> paths, 
			final int iGeneration,	final T prevElite) {
		
		this.eliteGenoFileHandler = eliteGenoFileHandler;
		this.populationFileHandler = populationFileHandler;
		this.paths = paths;
		this.saveInterval = saveInterval;
		this.justResumed = prevElite == null ? false: true;
		this.iGeneration = iGeneration;
		this.prevElite = prevElite;
	}
	
	protected abstract void writeEliteSubHeaders(final List<Phonation> targets, 
			final FileHandler elitePhenoFileHandler, final String label) throws IOException;	
	
	protected final void log(List<EvaluatedCandidate<T>> evaluatedPopulation) {        		
		if(!this.justResumed) {
    		System.out.print("\nGeneration " + this.iGeneration + ": ");
    		this.logGeneration(evaluatedPopulation);
		}
		else {
			System.out.println("Resuming with generation " + (this.iGeneration + 1));
			this.justResumed = false;
		}
		
		this.iGeneration++;
	}	
	
	protected final void writeParamsHeader(final AgentParams params, final FileHandler fileHandler, 
			final String prefix) throws IOException {
		
		for(final String targetName: params.targetNames()) {
			for(final String paramAbbrev: params.freeParamAbbrevs()) {
				fileHandler.write(prefix + targetName + "_" + paramAbbrev + ",");
			}
		}
	}
	
	protected final void manageSnapshotFiles(final List<String> writtenPaths,
			final List<EvaluatedCandidate<T>> evaluatedPopulation) throws IOException {
	
		if(this.iGeneration % this.saveInterval == 0) {
			final String sourcePath = this.paths.get("root");
			final String snapshotPath = this.paths.get("snapshot");
			
			this.saveSnapshot(writtenPaths, evaluatedPopulation);		
			
			this.copyFile(writtenPaths, sourcePath, snapshotPath, this.paths.get("population"));
			this.copyFile(writtenPaths, sourcePath, snapshotPath, this.paths.get("genotypes"));
			this.copyFile(writtenPaths, sourcePath, snapshotPath, this.paths.get("phenotypes"));
			
    		final File[] snapshotFiles = new File(snapshotPath).listFiles();
    		
    		if(snapshotFiles.length > 8) {
	    		this.deleteOldest(snapshotFiles);
    		}
		}
	}
	
	protected void logGeneration(List<EvaluatedCandidate<T>> evaluatedPopulation) {        	
		final T elite = evaluatedPopulation.get(0).getCandidate();		
		final List<String> writtenPaths = new ArrayList<String>(3);
		
		try {
			this.logPopulation(evaluatedPopulation);
			this.logElites(elite);
			this.logElites(elite);	
			this.manageSnapshotFiles(writtenPaths, evaluatedPopulation);
		}
		catch(IOException e) {
			System.err.println("Snapshot failed!");
			
			for(final String path: writtenPaths) {
				Util.delete(path);
			}
			
			System.exit(-1);
		}
	}
	
	protected void writeEliteHeaders(final AgentParams agentParams, final FileHandler notUsed) 
			throws IOException {
		
		this.eliteGenoFileHandler.write("generation,");
		this.eliteGenoFileHandler.write("mRMSE,");
		
		// elite errors
		for(final String targetName: agentParams.targetNames()) {
			this.eliteGenoFileHandler.write("RMSE_" + targetName + ",");
		}
		
		int nWeights = 0;
		
		// elite nn params
		for(int iLayer = 0; iLayer < agentParams.layerSpecs().size() - 1; iLayer++) {
			final int layerSize = agentParams.layerSpecs().get(iLayer);
			
			for(int iNeuronA = 0; iNeuronA < layerSize + 1; iNeuronA++) {
				final String inputNeuronLabel = iNeuronA < layerSize ? "I" : "B";
				final int nextLayerSize = agentParams.layerSpecs().get(iLayer + 1);
				
				for(int iNeuronB = 0; iNeuronB < nextLayerSize; iNeuronB++) {
					this.eliteGenoFileHandler.write("L" + iLayer + ":" + inputNeuronLabel + 
							iNeuronA + ">O" + iNeuronB + ",");
					nWeights++;
				}
			}
		}
		
		if(agentParams.tauFactor() > 0)
			for(int i = 0; i < nWeights; i++) {
				this.eliteGenoFileHandler.write("step" + i + ",");
			}
		
		this.eliteGenoFileHandler.newLine();
	}	
	
	protected boolean logElites(final T elite) throws IOException {	
		if(this.prevElite == null || elite.getMeanFitness() < this.prevElite.getMeanFitness()) {			
			
			this.eliteGenoFileHandler.write(Integer.toString(this.iGeneration) + ",");
			this.eliteGenoFileHandler.writeValue(elite.getMRMSE(), 4, true);
			this.eliteGenoFileHandler.writeValues(elite.getRMSEs(), 4);
			
			for(final List<List<Double>> layer: elite.getStructuredGenotype()) {
				for(final List<Double> neuron: layer) {
					this.eliteGenoFileHandler.writeValues(neuron, 4);		        				
				}
			}
			
			final List<Double> stepSizes = elite.getStepSizes();
			
			if(stepSizes != null) {
				this.eliteGenoFileHandler.writeValues(stepSizes, 4);
			}
			
			this.eliteGenoFileHandler.newLine();
			
			this.prevElite = elite;
			return true;
		}
		else {
			return false;
		}
	}
	
	protected void writePopulationHeaders(final AgentParams agentParams) throws IOException {
		this.populationFileHandler.write("time,");
		this.populationFileHandler.write("mmRMSE,");
		
		for(final String targetName: agentParams.targetNames()) {
			this.populationFileHandler.write("mRMSE_" + targetName + ",");
		}
		
		for(final String targetName: agentParams.targetNames()) {
			this.populationFileHandler.write("sdRMSE_" + targetName + ",");
		}
		
		this.writeParamsHeader(agentParams, this.populationFileHandler, "m.");
		this.writeParamsHeader(agentParams, this.populationFileHandler, "sd.");
		
		this.populationFileHandler.newLine();
	}	
	
	protected void logPopulation(
			final List<EvaluatedCandidate<T>> evaluatedPopulation) throws IOException {

		final long time = Math.round((double) System.currentTimeMillis()) / 1000;
		System.out.print("<" + time + "> ");
		
		final List<List<Double>> generationRMSEs = new LinkedList<List<Double>>();
		
		for(final EvaluatedCandidate<T> candidate: evaluatedPopulation) {
			final AbstractSolution solution = candidate.getCandidate();
			
			if(solution.getMRMSE() != Double.POSITIVE_INFINITY) {
    			final List<Double> RMSEs = solution.getRMSEs();
    			generationRMSEs.add(RMSEs);
			}
		}
		
		final List<List<Double>> zippedErrors = Util.zip(generationRMSEs);
		final List<Double> errorMeans = new LinkedList<Double>();
		final List<Double> errorSDs = new LinkedList<Double>();
		
		for(final List<Double> data: zippedErrors) {
			errorMeans.add(Util.mean(data));
			errorSDs.add(Util.sd(data));
		}
		
		final double meanErrorMeans = Util.mean(errorMeans);
		
		this.populationFileHandler.write(Long.toString(time) + ",");
		this.populationFileHandler.writeValue(meanErrorMeans, 4, true);
		this.populationFileHandler.writeValues(errorMeans, 4);
		this.populationFileHandler.writeValues(errorSDs, 4);			
	}
	
	protected final static String getGeneration(final File file) {
		final String filename = file.getName();
		final int genIndex = filename.indexOf("_");
		final String generation = filename.substring(0, genIndex);
		
		return generation;
	}
	
	private void deleteOldest(final File[] snapshotFiles) {
		final List<File> serFiles = Util.filterExtension(snapshotFiles, ".ser", true);
		Collections.sort(serFiles, new Util.GenerationComparator());
		final String oldestGen = getGeneration(serFiles.get(0));
		
		final List<File> oldestFiles = Util.filterExtension(snapshotFiles, oldestGen, false);
		
		for(final File file: oldestFiles) {
			Util.delete(file.getPath());
		}
	}
	
	private void saveSnapshot(final List<String> writtenPaths,
			final List<EvaluatedCandidate<T>> evaluatedPopulation) throws IOException {
		
		final List<T> solutions = new ArrayList<T>(evaluatedPopulation.size());

		for(EvaluatedCandidate<T> candidate: evaluatedPopulation) {
			solutions.add(candidate.getCandidate());
		}
		
		final String snapshotPath = this.paths.get("snapshot");
		final String serDest = snapshotPath + this.iGeneration + "_population";
		writtenPaths.add(serDest + ".ser");
		Util.writePopulation(solutions, serDest);
	}
	
	private void copyFile(final List<String> writtenPaths, final String sourcePath,
			final String snapshotPath, final String fileName) throws IOException {
		
		final String popSource = sourcePath + fileName;
		final String popDest = snapshotPath + this.iGeneration + "_" + fileName;
		writtenPaths.add(popDest);
		Util.copyFile(popSource, popDest);			
	}	
}
