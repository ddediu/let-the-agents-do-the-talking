package agent;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.CachingFitnessEvaluator;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.EvolutionStrategyEngine;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.TerminationCondition;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

final class LoggingStrategyEngine extends EvolutionStrategyEngine<AbstractSolution> {
	private final AbstractLogger<AbstractSolution> logger;
	
	@SuppressWarnings("unchecked")
	LoggingStrategyEngine(final CachingFitnessEvaluator<? extends AbstractSolution> evaluator,
			final AbstractLogger<? extends AbstractSolution> logger, 
			final AbstractCandidateFactory<AbstractSolution> solutionFactory, 
			final EvolutionaryOperator<AbstractSolution> pipeline) {
		
		super(solutionFactory, pipeline, 
				(FitnessEvaluator<AbstractSolution>) evaluator, true, 1, new MersenneTwisterRNG());
		
		this.setSingleThreaded(true);
		this.logger = (AbstractLogger<AbstractSolution>) logger;	
	}	
	
	@Override
	public AbstractSolution evolve(final int populationSize, final int eliteCount, 
			final Collection<AbstractSolution> seedCandidates, 
			final TerminationCondition... conditions) {
		
		for(int i = 0; i < populationSize; i++) {
    		System.out.print(".");	
		}
		
		System.out.println();
		
		return super.evolve(populationSize, eliteCount, seedCandidates, conditions);
	}
	
	@Override
	protected List<EvaluatedCandidate<AbstractSolution>> nextEvolutionStep(
			final List<EvaluatedCandidate<AbstractSolution>> evaluatedPopulation, 
			final int eliteCount, final Random rng) {
		
		this.logger.log(evaluatedPopulation);
		
		return super.nextEvolutionStep(evaluatedPopulation, eliteCount, rng);
	}
}