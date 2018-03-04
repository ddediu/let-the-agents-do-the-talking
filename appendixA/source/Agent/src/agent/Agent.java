package agent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import parameters.AgentParams;
import util.FileHandler;
import util.Util;

public final class Agent {
	
	public void think(final AgentParams agentParams, final HashMap<String,String> paths) {
		final int SAVE_INTERVAL = 5;
		
		for(final String path: paths.values()) {
			if(path != null && path.endsWith("/")) {
				Util.createDir(path);
			}
		}
		
    	final String populationPath = paths.get("root") + paths.get("population");
    	final String genotypePath = paths.get("root") + paths.get("genotypes");
    	final String phenotypePath = paths.get("root") + paths.get("phenotypes");		
		
		Util.deleteIfExists(populationPath);
		Util.deleteIfExists(genotypePath);
		Util.deleteIfExists(phenotypePath);
		
		final Newests newests = this.resumeSnapshot(paths);
		
		final FileHandler populationFileHandler = new FileHandler(populationPath);
		final FileHandler eliteGenoFileHandler = new FileHandler(genotypePath);
		final FileHandler elitePhenoFileHandler = new FileHandler(phenotypePath);
		
		final Brain brain = new Brain(eliteGenoFileHandler, elitePhenoFileHandler, 
				populationFileHandler, paths, SAVE_INTERVAL);			
		
		brain.process(agentParams, newests.prevPopulation, newests.prevElite, newests.iGeneration);
	}
	
	private final class Newests {
		private final Collection<AbstractSolution> prevPopulation;
		private final AbstractSolution prevElite;
		private final int iGeneration;
		
		private Newests(final Collection<AbstractSolution> prevPopulation, 
				final AbstractSolution prevElite, final int iGeneration) {
			
			this.prevPopulation = prevPopulation;
			this.prevElite = prevElite;
			this.iGeneration = iGeneration;
		}
	}
	
	private List<File> cleanSnapshots(final File[] snapshotFiles) {
    	final File finalItem = snapshotFiles[snapshotFiles.length - 1];
    	final String finalGen = AbstractLogger.getGeneration(finalItem);
    	
    	final List<File> earlierFiles = new LinkedList<File>();
    	final List<File> latestFiles = Util.filterExtension(snapshotFiles, finalGen, false, 
    			earlierFiles);
    	
    	if(latestFiles.size() < 3) {
    		System.out.println("cleaning snapshots " + finalGen);
    		
    		for(Iterator<File> it = latestFiles.iterator(); it.hasNext();) {
    			Util.delete(it.next().getPath());
    			it.remove();
    		}
    	}
    	
    	if(!earlierFiles.isEmpty()) {
    		final List<File> cleanedFiles = cleanSnapshots(earlierFiles.toArray(
    				new File[earlierFiles.size()]));
    		cleanedFiles.addAll(latestFiles);
    		return cleanedFiles;
    	}
    	else {
    		return latestFiles;
    	}
	}
	
	private Newests resumeSnapshot(final HashMap<String,String> paths) {
		final String snapshotPath = paths.get("snapshot");
        final File[] snapshotFiles = new File(snapshotPath).listFiles();
        
        if(snapshotFiles.length > 0) {
        	Arrays.sort(snapshotFiles, new Util.GenerationComparator());
        	final List<File> clearedSnapshotFiles = this.cleanSnapshots(snapshotFiles);
        	final File[] clearFilesArray = clearedSnapshotFiles.toArray(
        			new File[clearedSnapshotFiles.size()]);
        	
	        final List<File> popFiles = Util.filterExtension(clearFilesArray, ".ser", true);
	    	final File newestPopFile = popFiles.get(popFiles.size() - 1);
	    	final int newestPopGen = Integer.parseInt(AbstractLogger.getGeneration(newestPopFile));
        	        	
        	final File[] wavFiles = new File("wavs").listFiles();
        	final List<File> filteredWavFiles = new LinkedList<File>();
        	for(final File file: wavFiles) {
        		if(Character.isDigit(file.getName().charAt(0))) {
        			filteredWavFiles.add(file);
        		}
        	}
        	
        	for(final File wavFile: filteredWavFiles) {
        		final String name = wavFile.getName();
        		final int wavGen = Integer.parseInt(name.substring(0, name.indexOf("_")));
        		if(wavGen > newestPopGen) {
        			Util.delete(wavFile.getAbsolutePath());
        		}
        	}
        	
        	final List<AbstractSolution> newestPopulation = Util.readPopulation(
        			newestPopFile.getPath());        	
        	final AbstractSolution newestElite = newestPopulation.get(0);
        	
        	final String populationFilename = paths.get("population");
        	final String genotypeFilename = paths.get("genotypes");
        	final String phenotypeFilename = paths.get("phenotypes");
        	final String root = paths.get("root");
        	
        	final String snapshotPrefix = snapshotPath + newestPopGen + "_";
        	
			try {
				Util.copyFile(snapshotPrefix + populationFilename, root + populationFilename);
				Util.copyFile(snapshotPrefix + genotypeFilename, root + genotypeFilename);
				Util.copyFile(snapshotPrefix + phenotypeFilename, root + phenotypeFilename);
			} catch (IOException e) {
				System.err.println("Failed to copy file");
				e.printStackTrace();
				System.exit(-1);
			}
        	
        	return new Newests(newestPopulation, newestElite, newestPopGen);
        }
        else {
        	return new Newests(null, null, 0);
        }
	}
}