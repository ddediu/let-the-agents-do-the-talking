package agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class StrategyMutator extends Mutator {
	private final double globalTau;
	private final double localTau;
	private final Random random;
	
	StrategyMutator(final int problemSize, final double learningRate) {
		super(0, 0);
		
		this.globalTau = StrategyMutator.calculateGlobalTau(learningRate, problemSize);
		this.localTau = learningRate / Math.sqrt(2 * Math.sqrt(problemSize));
		this.random = new Random();
	}
	
	public final static double calculateGlobalTau(final double learningRate, final int problemSize) {
		return learningRate / Math.sqrt(2 * problemSize);
	}

	@Override
	public final List<AbstractSolution> apply(final List<AbstractSolution> population, 
			final Random rng) {
		
		final List<AbstractSolution> mutants = new ArrayList<AbstractSolution>(population.size());
		
		for(final AbstractSolution parent: population) {
			final List<Double> mutantStepsizes = mutateStepsizes(parent);
			final AbstractSolution mutant = super.mutateGenotype(parent, rng, mutantStepsizes);
			mutant.setStepSizes(mutantStepsizes);
			mutants.add(mutant);
		}
		
		return mutants;
	}

	private List<Double> mutateStepsizes(final AbstractSolution parent) {
		final double globalGauss = this.globalTau * this.random.nextGaussian();
		final List<Double> stepsizes = parent.getStepSizes();
		
		final List<Double> mutatedStepizes = new ArrayList<Double>(stepsizes.size());

		for(final double stepsize: stepsizes) {
			final double localGauss = this.localTau * this.random.nextGaussian();
			
			double mutatedStepsize = stepsize * Math.exp(globalGauss + localGauss);
			mutatedStepsize = mutatedStepsize < 0.01 ? 0.01 : mutatedStepsize;
			
			mutatedStepizes.add(mutatedStepsize);
		}
		
		return mutatedStepizes;
	}
}
