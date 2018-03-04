package agent.vtl_agent;

import java.util.List;

import org.uncommons.watchmaker.framework.FitnessEvaluator;

import parameters.Fitness;

public final class VtlEvaluator implements FitnessEvaluator<VtlSolution> {
	private final List<List<Double>> acTargets;
	private final Fitness function;
	private final double mseExponent;
	
	public VtlEvaluator(final List<List<Double>> acTargets, final Fitness function,
			final double mseExponent) {
		
		this.acTargets = acTargets;
		this.function = function;
		this.mseExponent = mseExponent;
	}
	
	@Override
	public double getFitness(final VtlSolution solution, 
			final List<? extends VtlSolution> notUsed) {
		
		if(solution.getFitnesses().size() == 0) {
			final List<Phonation> phonations = solution.getPhenotype(this.acTargets).getValues();	
			
			for(final Phonation phonation: phonations) {
				final List<Double> formants = phonation.getFormants();
				final List<Double> acTarget = phonation.getTarget();
				
				if(formants.size() == acTarget.size()) {
					double SSE = 0;
					
					for(int i = 0; i < acTarget.size(); i++) {
						final double error = (formants.get(i) - acTarget.get(i));
						final double squaredError = Math.pow(error, 2);
						SSE += squaredError;
					}
	
					final double MSE = SSE / formants.size();
					final double RMSE = Math.pow(MSE,0.5);
					final double fitnessRMSE = Math.pow(MSE,this.mseExponent);
					
					final double fitness;
					
					switch(this.function) {
						case MEAN:
							fitness = fitnessRMSE;
							break;
						case EXP:
							fitness = Math.exp(fitnessRMSE);
							break;
						case SD:
							fitness = Math.pow(fitnessRMSE,2);
							break;
						default:
							System.err.println("Invalid fitness function!");
							fitness = -1;
							System.exit(-1);
					}
	
					solution.addRMSE(RMSE);
					solution.addFitness(fitness);
				}
				else {
					solution.addRMSE(Double.POSITIVE_INFINITY);
					solution.addFitness(Double.POSITIVE_INFINITY);
					break;
				}
			}
		}
			
		return solution.getMeanFitness();
	}

	@Override
	public boolean isNatural() {
		return false;
	}
}