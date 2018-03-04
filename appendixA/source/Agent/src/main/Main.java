package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import util.Util;
import agent.Agent;
import agent.vtl_agent.vt.Manager;
import nativelib.VTL;
import parameters.Activation;
import parameters.AgentParams;
import parameters.AgentParams.ParamBuilder;
import parameters.Fitness;
import parameters.Problem;
import parameters.Selection;
import parameters.AlgoType;

public final class Main {	
	private enum EnumKey {
		ACTIVATION, TYPE, FITNESS, SELECTION 
	}
	
	public final static void main(String[] args) {
		final String root =  args.length == 1 ? args[0] : "";
		
		final List<List<String>> configList = Util.readCsv(root + "config.csv");
		final Problem problem = Problem.valueOf(configList.remove(0).remove(1).toUpperCase());
		Iterator<List<String>> configIterator = configList.iterator();

		// parse string parameters				
		final HashMap<String,Enum<?>> enums = new HashMap<String,Enum<?>>();
		
		while(configIterator.hasNext()) {
			final List<String> configLine = configIterator.next();
			final String value = configLine.get(1).toUpperCase();
			
			if(!(value.equals("TRUE") || value.equals("FALSE"))) {
				final String stringkey = configLine.get(0);
				final EnumKey key;
				
				if(stringkey.endsWith("Selection")) {
					key = EnumKey.valueOf("SELECTION");
				}
				else {
					key = EnumKey.valueOf(stringkey.toUpperCase());
				}
				
				switch(key) {
					case ACTIVATION:
						enums.put(stringkey, Activation.valueOf(value));
						break;
					case TYPE:
						enums.put(stringkey, AlgoType.valueOf(value));
						break;
					case FITNESS:
						enums.put(stringkey, Fitness.valueOf(value));
						break;
					case SELECTION:
						enums.put(stringkey, Selection.valueOf(value));
						break;
					default:
						System.err.println("Invalid enum key!");
			        	System.exit(-1);
				}
				
				System.out.println(key + ": " + value);
				
				configIterator.remove();
			}
			else {
				break;
			}
		}
		
		// parse boolean parameters
		configIterator = configList.iterator();
		final HashMap<String,Boolean> booleans = new HashMap<String,Boolean>();
		
		while(configIterator.hasNext()) {
			final List<String> configLine = configIterator.next();
			final String value = configLine.get(1).toUpperCase();
			
			if(value.equals("TRUE") || value.equals("FALSE")) {
				final String key = configLine.get(0);
				booleans.put(key, Boolean.valueOf(configLine.get(1)));
				System.out.println(key + ": " + value);			
				
				configIterator.remove();
			}
			else {
				break;
			}
		}				
		
		// parse hidden layer config
		final List<String> hiddenFactorsLine = configList.remove(0);
		final String hiddenFactorskey = hiddenFactorsLine.remove(0);
		final List<Double> hiddenFactors = new ArrayList<Double>(hiddenFactorsLine.size());
		
			for(final String hiddenFactor: hiddenFactorsLine) {
				hiddenFactors.add(Double.parseDouble(hiddenFactor));
			}
		
		System.out.println(hiddenFactorskey + ": " + hiddenFactors);		
		
		// parse acoustic targets
		final List<String> targetNames = configList.remove(0);
		final String targetskey = targetNames.remove(0);
		System.out.println(targetskey + ": " + targetNames);
		
		// parse float parameters
		final HashMap<String,Number> numbers = new HashMap<String,Number>();
		
		for(final List<String> parameter: configList) {
			final String key = parameter.remove(0);
			final double value = Double.parseDouble(parameter.remove(0));
			System.out.println(key + ": " + value);
			numbers.put(key, value);
		}
		
		System.out.println();
		
		// set common params
		final ParamBuilder paramBuilder = new AgentParams.ParamBuilder().
				nIterations(numbers.get("nIterations")).
				popSize(numbers.get("popSize")).
				mutationRate(numbers.get("mutationRate")).
				crossoverRate(numbers.get("crossoverRate")).
				nElites(numbers.get("nElites")).
				lambdaFactor(numbers.get("lambdaFactor")).
				tauFactor(numbers.get("tauFactor")).
				fitness(enums.get("fitness")).
				type(enums.get("type")).
				parentSelection(enums.get("parentSelection")).
				activation(enums.get("activation")).
				offspringSelection(enums.get("offspringSelection")).
				sigmaScaling(booleans.get("sigmaScaling")).
				rankingSelection(booleans.get("rankingSelection")).
				plusSelection(booleans.get("plusSelection"));
		
		switch(problem) {
			case VTL:
				paramBuilder.mseExponent(numbers.get("mseExponent"));
				
				Main.setAndRunVtlAgent(paramBuilder, numbers, hiddenFactors, targetNames, 
						numbers.get("nFormants").intValue(), root, booleans.get("wav"));
				break;
			case FUNCTION:
				break;
			default:
				break;
		}
	}

	private final static void setAndRunVtlAgent(final ParamBuilder param,
			final HashMap<String, Number> metaParams, final List<Double> hiddenFactors,
			final List<String> targetNames, final int nFormants, final String root,
			final boolean wav) {
		
		final HashMap<String,String> paths = new HashMap<String,String>(3);
		paths.put("root", root);
		paths.put("snapshot", root + "snapshot/");
		paths.put("wav", wav ? root + "wavs/": null);
		paths.put("targets", root + "targets.csv");
		paths.put("anatomy", root + "anatomy.csv");
		paths.put("genotypes", "logElitesGenotypes.csv");
		paths.put("phenotypes", "logElitesPhenotypes.csv");
		paths.put("population", "logPopulation.csv");
		
		final int nThreads = metaParams.remove("nThreads").intValue();
		final int iAnatomy = metaParams.get("iAnatomy").intValue();
		final List<List<String>> anatomyData = Util.readCsv(paths.get("anatomy"));
		Manager.getInstance(nThreads, anatomyData, iAnatomy);
		
		final List<String> paramNames = anatomyData.get(0);
		final int nParams = paramNames.size();
		final List<String> paramAbbreviations = new ArrayList<String>(nParams);
		
		for(final String paramName: paramNames) {
			paramAbbreviations.add(VTL.getParamAbbreviation(paramName));
		}
		
		final List<Integer> layerSpecs = Main.createConvergentNnSpecs(nFormants, hiddenFactors, nParams);
		
		final AgentParams agentParams = param.problem(Problem.VTL).layerSpecs(layerSpecs).
				freeParamAbbrevs(paramAbbreviations).nFormants(nFormants).targetNames(targetNames).
				nFormants(nFormants).iAnatomy(iAnatomy).build();
		
		Main.runAgent(agentParams, paths);
	}
	
	private static List<Integer> createConvergentNnSpecs(final int nInput, 
			final List<Double> hiddenFactors, final int nOuptut) {
		
		final List<Integer> layerSpecs = new ArrayList<Integer>(hiddenFactors.size() + 2);
		
		layerSpecs.add(nInput);
		System.out.println("input layer " + nInput);
		
		for(double hiddenFactor: hiddenFactors) {
			final int nHidden = (int) Math.round((nInput + nOuptut) * hiddenFactor);
			layerSpecs.add(nHidden);
			System.out.println("hidden layer " + nHidden);
		}
		
		layerSpecs.add(nOuptut);
		System.out.println("output layer " + nOuptut);
		
		return layerSpecs;
	}
	
	private static void runAgent(final AgentParams agentParams, final HashMap<String,String> paths) {
		// run agent
		final Agent agent = new Agent();
		agent.think(agentParams, paths);
		System.out.println("Finished!");
		System.exit(0);
	}
}