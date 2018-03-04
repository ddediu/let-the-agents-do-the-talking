package agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.uncommons.watchmaker.framework.EvolutionaryOperator;

class Mutator implements EvolutionaryOperator<AbstractSolution> {
	private final List<Double> bogusStepsizes;
	
	Mutator(final int problemSize, final double sd) {
		final List<Double> bogusStepsizes = new ArrayList<Double>(problemSize);
		
		for(int i = 0; i < problemSize; i++) {
			bogusStepsizes.add(sd);
		}
		
		this.bogusStepsizes = Collections.unmodifiableList(bogusStepsizes);		
	}

	@Override
	public List<AbstractSolution> apply(final List<AbstractSolution> population, 
			final Random rng) {
		
		final List<AbstractSolution> mutants = new ArrayList<AbstractSolution>(population.size());
		
		for(final AbstractSolution parent: population) {
			final AbstractSolution mutant = mutateGenotype(parent, rng, this.bogusStepsizes);		
			mutants.add(mutant);
		}
		
		return mutants;
	}
	
	protected AbstractSolution mutateGenotype(final AbstractSolution parent, final Random rng, 
			final List<Double> stepSizes) {
		
		final List<Double> genotype = parent.getGenotype();
		final List<Double> mutantGenotype = new ArrayList<Double>(genotype.size());
		
		for(int i = 0; i < genotype.size(); i++) {
			final double gene = genotype.get(i);
			final double stepsize = stepSizes.get(i);			
			final double mutatedGene = gene + stepsize * rng.nextGaussian();
			mutantGenotype.add(mutatedGene);
		}
		
		final AbstractSolution mutant = parent.getBlankClone();
		mutant.setGenotype(mutantGenotype);
		
		return mutant;
	}
}
