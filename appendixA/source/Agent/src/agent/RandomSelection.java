package agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.SelectionStrategy;

class RandomSelection implements SelectionStrategy<Object> {
	
	@Override
	public <S extends Object> List<S> select(final List<EvaluatedCandidate<S>> population, 
			final boolean notUsed, final int selectionSize, final Random rng) {
		
		final List<S> selectedCandidates = new ArrayList<S>(selectionSize);
		
		while(selectedCandidates.size() < selectionSize) {
			final EvaluatedCandidate<S> randomCandidate = population.get(rng.nextInt(population.size()));
			
			if(randomCandidate.getFitness() != Double.POSITIVE_INFINITY) {
				selectedCandidates.add(randomCandidate.getCandidate());
			}
		}
		
		return selectedCandidates;
	}
}
