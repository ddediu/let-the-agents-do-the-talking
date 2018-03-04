package agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.CachingFitnessEvaluator;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.EvolutionUtils;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.SteadyStateEvolutionEngine;

class JanssenEngine extends SteadyStateEvolutionEngine<AbstractSolution>{
	private final AbstractLogger<AbstractSolution> logger;
	private final boolean plusSelection;
	private final boolean isNatural;
	private final SelectionStrategy<Object> offspringSelection;
	
	@SuppressWarnings("unchecked")
	JanssenEngine(final CachingFitnessEvaluator<? extends AbstractSolution> evaluator,
			final AbstractLogger<? extends AbstractSolution> logger,
			final CandidateFactory<AbstractSolution> solutionFactory,
			final EvolutionaryOperator<AbstractSolution> pipeline,
			final SelectionStrategy<Object> parentSelection,
			final SelectionStrategy<Object> offspringSelection,
			final int selectionSize, final boolean plusSelection) {
		
		super(solutionFactory, pipeline, (FitnessEvaluator<? super AbstractSolution>) evaluator, 
				parentSelection, selectionSize, false, new MersenneTwisterRNG());
		
		this.setSingleThreaded(true);
		this.plusSelection = plusSelection;
		
		this.isNatural = evaluator.isNatural();
		this.offspringSelection = offspringSelection;
		this.logger = (AbstractLogger<AbstractSolution>) logger;
	}	

	@Override
	protected List<EvaluatedCandidate<AbstractSolution>> nextEvolutionStep(
			final List<EvaluatedCandidate<AbstractSolution>> population, final int eliteCount, 
			final Random rng) {
		
		this.logger.log(population);
		
		// this does mutation, evaluation and selection, generates offspring and calls doReplacement
		final List<EvaluatedCandidate<AbstractSolution>> survivors = 
				super.nextEvolutionStep(population, eliteCount, rng);
		
		return survivors;
	}
	
	@Override
	protected void doReplacement(final List<EvaluatedCandidate<AbstractSolution>> population,
			final List<EvaluatedCandidate<AbstractSolution>> offspring, final int nElites, 
			final Random rng) {
		
		// merge populations
		final List<EvaluatedCandidate<AbstractSolution>> combinedPopulation = 
				new ArrayList<EvaluatedCandidate<AbstractSolution>>(offspring); 
		
		if(this.plusSelection) {
			combinedPopulation.addAll(population);
		}
		
		// get elites
		final List<EvaluatedCandidate<AbstractSolution>> elites = 
				new ArrayList<EvaluatedCandidate<AbstractSolution>>(nElites);
		
		if(nElites >= 1) {
			EvolutionUtils.sortEvaluatedPopulation(combinedPopulation, this.isNatural);
			
			for(int i = 0; i < nElites; i++) {
				elites.add(combinedPopulation.get(i));
			}
		}
		
		// get survivors
		final List<AbstractSolution> survivors = this.offspringSelection.select(combinedPopulation,
				this.isNatural, population.size() - nElites, rng);
		
		// clear population, then add elites and survivors
		population.clear();
		population.addAll(elites);
		
		for(final AbstractSolution survivor: survivors) {
			final EvaluatedCandidate<AbstractSolution> evaluatedCandidate = 
					new EvaluatedCandidate<AbstractSolution>(survivor, survivor.getMeanFitness());
			
			population.add(evaluatedCandidate);
		}
	}
	
	@SuppressWarnings("unused")
	private void printEvalualtedCandidates(
			final List<EvaluatedCandidate<AbstractSolution>> candidates) {
		
		final List<AbstractSolution> solutions = new ArrayList<AbstractSolution>(candidates.size());
		
		for(final EvaluatedCandidate<AbstractSolution> candidate: candidates) {
			solutions.add(candidate.getCandidate());
		}
		
		System.out.println(solutions);
	}
}
