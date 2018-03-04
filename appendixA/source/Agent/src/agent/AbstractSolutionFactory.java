package agent;

import java.util.Random;

import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

public abstract class AbstractSolutionFactory extends AbstractCandidateFactory<AbstractSolution>{
	protected final double globalTau;
	
	public AbstractSolutionFactory(final double tauFactor, final int problemSize) {		
		if(tauFactor > 0) {			
			this.globalTau = StrategyMutator.calculateGlobalTau(tauFactor, problemSize);
		}
		else {
			this.globalTau = Double.NaN;
		}
	}
	
	protected final boolean randomToBoolean(final Random rng) {		
		if(rng == null) {
			return false;
		}
		else {
			return true;
		}
	}
}
