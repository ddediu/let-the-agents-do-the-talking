package agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.AbstractEvolutionEngine;
import org.uncommons.watchmaker.framework.CachingFitnessEvaluator;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.operators.ListCrossover;
import org.uncommons.watchmaker.framework.selection.RankSelection;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.selection.SigmaScaling;
import org.uncommons.watchmaker.framework.selection.StochasticUniversalSampling;
import org.uncommons.watchmaker.framework.selection.TruncationSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import agent.vtl_agent.Phonation;
import agent.vtl_agent.VtlEvaluator;
import agent.vtl_agent.VtlLogger;
import agent.vtl_agent.VtlSolution;
import agent.vtl_agent.VtlSolutionFactory;
import agent.vtl_agent.vt.Manager;
import parameters.AgentParams;
import parameters.Selection;
import util.FileHandler;
import util.Util;

final class Brain {
	private final FileHandler eliteGenoFileHandler;
	private final FileHandler elitePhenoFileHandler;
	private final FileHandler populationFileHandler;
	private final HashMap<String, String> paths;
	private final int saveInterval;
	
	Brain(final FileHandler eliteGenoFileHandler, final FileHandler elitePhenoFileHandler,
			final FileHandler populationFileHandler, final HashMap<String, String> paths, 
			final int saveInterval) {
		
		this.eliteGenoFileHandler = eliteGenoFileHandler;
		this.elitePhenoFileHandler = elitePhenoFileHandler;
		this.populationFileHandler = populationFileHandler;
		this.paths = paths;
		this.saveInterval = saveInterval;
	}
	
	void process(final AgentParams params, final Collection<AbstractSolution> prevPopulation, 
			final AbstractSolution prevElite, final int iGeneration) {
		
    	final AbstractEvolutionEngine<AbstractSolution> engine = this.initProblem(params, 
    			iGeneration, prevElite);
    	
    	final int nGenerations = params.nIterations() - iGeneration + 1;
    	
    	if(prevPopulation == null) {
    		engine.evolve(params.popSize(), params.nElites(), new GenerationCount(nGenerations));
    	}
    	else {
    		engine.evolve(params.popSize(), params.nElites(), prevPopulation, 
    				new GenerationCount(nGenerations));
    	}
	}	
	
	private AbstractEvolutionEngine<AbstractSolution> initProblem(final AgentParams agentParams,
			final int iGeneration, final AbstractSolution previousElite) {	
		
		switch(agentParams.problem()) {		
			case VTL:			
				final List<Phonation> targets =  this.getAcTargets(paths.get("targets"), paths.get("wav"), 
						agentParams.targetNames(), agentParams.nFormants(), "target");
				
				final List<Phonation> altTargets =  this.getAcTargets(paths.get("targets"), 
						paths.get("wav"), agentParams.targetNames(), agentParams.nFormants(), 
						"alt");
				
				final AbstractLogger<VtlSolution> vtlLogger = new VtlLogger(
						this.eliteGenoFileHandler, this.elitePhenoFileHandler, 
						this.populationFileHandler, agentParams, targets, altTargets,
						this.saveInterval, this.paths, iGeneration, (VtlSolution) previousElite);
				
				final List<List<Double>> targetsAcoustics = Phonation.getAcTargets(targets);				
				
				final CachingFitnessEvaluator<VtlSolution> vtlEvaluator =
						new CachingFitnessEvaluator<VtlSolution>(new VtlEvaluator(
								targetsAcoustics, agentParams.fitnessFunction(),
								agentParams.mseExponent()));
				
				final AbstractSolutionFactory vtlSolutionFactory = 
						new VtlSolutionFactory(agentParams.activation(), agentParams.layerSpecs(), 
								targetsAcoustics, agentParams.tauFactor(), agentParams.popSize());
				
				return this.createEngine(agentParams, vtlLogger, vtlEvaluator, vtlSolutionFactory);
				
			default:
				System.err.println("Invalid problem!");
	        	System.exit(-1);
	        	return null;
		}
	}
	
	private List<Phonation> getAcTargets(final String targetsFileName, final String wavDir, 
			final List<String> targetVowelNames, final int nFormants, final String iAnatomy) {
		
		final List<List<String>> targetsData = Util.readCsv(targetsFileName);
		
		final List<String> targetsFixedParamNames = targetsData.remove(0);
		final List<Double> targetsFixedParams = Util.stringsToDoubles(targetsData.remove(0));
		final List<String> targetsFreeParamNames = new ArrayList<String>(targetsData.get(0));
		targetsFreeParamNames.remove(0);
		
		final List<Phonation> phonations = new ArrayList<Phonation>(targetVowelNames.size());
		
		for(final List<String> flexParams: targetsData) {
			final String label = flexParams.remove(0);
			final String wavPath = wavDir!=null ? wavDir + iAnatomy + "_" + label : null;
			
			if(targetVowelNames.contains(label)) {
				System.out.println(label);
				
				final List<Double> targetFreeParams = Util.stringsToDoubles(flexParams);
				
				final Phonation phonation;
				if(iAnatomy.equals("target")) {
					
					phonation = Manager.getInstance().getTarget(targetsFixedParams, 
							targetsFixedParamNames, targetFreeParams, targetsFreeParamNames, 
							nFormants, wavPath);					
				}
				else {
					phonation = Manager.getInstance().getTarget(targetsFixedParamNames, 
							targetFreeParams, targetsFreeParamNames, nFormants, 
							wavPath);				
				}
				
				phonation.setTarget(phonation.getFormants());
				phonations.add(phonation);
			}
		}
		
		System.out.println();
		
		return phonations;
	}	
	
	private AbstractEvolutionEngine<AbstractSolution> createEngine(final AgentParams agentParams,
			final AbstractLogger<? extends AbstractSolution> logger, 
			final CachingFitnessEvaluator<? extends AbstractSolution> evaluator,
			final AbstractCandidateFactory<AbstractSolution> solutionFactory) {
		
	    final EvolutionaryOperator<AbstractSolution> pipeline = this.createPipeline(
	    		agentParams, solutionFactory);		
		
		switch(agentParams.algoType()) {
			case WMF:
				return new LoggingStrategyEngine(evaluator, logger, solutionFactory, pipeline);
			case JANSSEN:
				final SelectionStrategy<Object> parentSelection = this.getStrategy(
						agentParams.parentSelection(),agentParams.rankingSelection(), 
						agentParams.sigmaScaling());		
				
				final SelectionStrategy<Object> offSpringSelection = this.getStrategy(
						agentParams.offspringSelection(),agentParams.rankingSelection(), 
						agentParams.sigmaScaling());
				
				return new JanssenEngine(evaluator, logger, solutionFactory, pipeline, 
						parentSelection, offSpringSelection, 
						agentParams.popSize() * agentParams.lambdaFactor(), agentParams.plusSelection());
			default:
	        	System.err.println("Invalid strategy!");
	        	System.exit(-1);
	        	return null;
		}		
	}
	
	private SelectionStrategy<Object> getStrategy(final Selection selection, 
			final boolean rankingSelection, final boolean sigmaScaling) {
		
		SelectionStrategy<Object> selectionStrategy;
		
		switch(selection) {
			case RANDOM:
				selectionStrategy = new RandomSelection();
				break;
			case RWS:
				selectionStrategy = new RouletteWheelSelection();
				break;
			case SUS:
				selectionStrategy = new StochasticUniversalSampling();
				break;
			case TRUNCATION:
				selectionStrategy = new TruncationSelection(0.5);
				break;
			default:
	        	System.err.println("Invalid selection!");
	        	selectionStrategy = null;
	        	System.exit(-1);		
		}
		
		if(rankingSelection) {
			selectionStrategy = new RankSelection(selectionStrategy);
		}
		else if(sigmaScaling) {
			selectionStrategy = new SigmaScaling(selectionStrategy);	
		}
		
		return selectionStrategy;
	}
	
	private EvolutionaryOperator<AbstractSolution> createPipeline(final AgentParams agentParams,
			final AbstractCandidateFactory<? extends AbstractSolution> solutionFactory) {
        
        final class Crossover implements EvolutionaryOperator<AbstractSolution> {
        	private final ListCrossover<Double> innerCrossover;
        	
        	private Crossover(final int crossoverPoints, final Probability probability) {
        		this.innerCrossover = new ListCrossover<Double>(crossoverPoints, probability);
        	}
        	
			@Override
			public List<AbstractSolution> apply(final List<AbstractSolution> parents, 
					final Random rng) {
				
				final List<AbstractSolution> mutants = new LinkedList<AbstractSolution>();
				final List<List<Double>> innerGenotypes = new ArrayList<List<Double>>(parents.size());
				
				for(final AbstractSolution parent: parents) {
					mutants.add(solutionFactory.generateRandomCandidate(null));
					innerGenotypes.add(parent.getGenotype());
				}
				
				final List<List<Double>> mutatedInnerGenotypes = 
						this.innerCrossover.apply(innerGenotypes, rng);
				
				for(int i = 0; i < mutants.size(); i++) {
					mutants.get(i).setGenotype(mutatedInnerGenotypes.get(i));
				}
				
				if(agentParams.tauFactor() > 0) {
					final List<List<Double>> innerStepSizes = new ArrayList<List<Double>>(parents.size());
				
					for(final AbstractSolution parent: parents) {
						innerStepSizes.add(parent.getStepSizes());
					}
					
					final List<List<Double>> mutatedInnerStepSizes = 
							this.innerCrossover.apply(innerStepSizes, rng);
					
					for(int i = 0; i < mutants.size(); i++) {
						mutants.get(i).setStepSizes(mutatedInnerStepSizes.get(i));
					}
				}
				
				return mutants;
			}
        }
        
        final List<EvolutionaryOperator<AbstractSolution>> operators = 
        		new LinkedList<EvolutionaryOperator<AbstractSolution>>();
        
        final int problemSize = solutionFactory.generateRandomCandidate(null).
        		getGenotype().size();
        
        if(agentParams.tauFactor() > 0) {
        	operators.add(new StrategyMutator(problemSize, agentParams.tauFactor()));
        }
        else if(agentParams.mutationRate() > 0) {
        	operators.add(new Mutator(problemSize, agentParams.mutationRate()));
        }
        else {
        	System.err.println("Either mutatationRate or tauFactor must be large than 0!");
        	System.exit(-1);
        	return null;
        }
        
        if(agentParams.crossoverRate() > 0) {
        	operators.add(new Crossover(1, new Probability(agentParams.crossoverRate())));
        }
        
        return new EvolutionPipeline<AbstractSolution>(operators);
	}
}