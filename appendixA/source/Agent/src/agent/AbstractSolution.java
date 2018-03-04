package agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.Util;

public abstract class AbstractSolution implements Serializable, Comparable<AbstractSolution> {
	private static final long serialVersionUID = 74662187646349609L;
	private final List<Double> rmses;
	private final List<Double> fitnesses;
	protected final double globalTau;
	private List<Double> genotype;
	private List<Double> stepSizes;	
	
	public interface Phenotype {		
		List<?> getValues();
	}
	
	public class DoubleResults implements Phenotype {
		private List<Double> values;
		
		public DoubleResults(List<Double> values) {
			this.values = values;
		}

		@Override
		public List<Double> getValues() {
			return this.values;
		}
	}
	
	protected AbstractSolution(final int nInputs, final boolean random, final int nObjectives, 
			final double globalTau) {
		
		this.globalTau = globalTau;
		this.rmses = new ArrayList<Double>(nObjectives);
		this.fitnesses = new ArrayList<Double>(nObjectives);
		this.genotype = new ArrayList<Double>(nInputs);
		
		if(random && ! Double.isNaN(globalTau)) {
			this.stepSizes = new ArrayList<Double>(nInputs);
			
			final Random randomGenerator = new Random();
			
			for(int i = 0; i < nInputs; i++) {
				final double stepSize = this.globalTau * Math.exp(this.globalTau * randomGenerator.nextGaussian());
				this.stepSizes.add(stepSize);
			}
		}
	}
	
	protected abstract AbstractSolution getBlankClone();
	
	@Override
	public int compareTo(final AbstractSolution compareTo) {
		return (int) Math.signum(this.getMeanFitness() - compareTo.getMeanFitness());
	}	
	
	@Override
	public String toString() {
		return Double.toString(this.getMeanFitness());
	}	
	
	public final void addFitness(final double fitness) {
		this.fitnesses.add(fitness);
	}
	
	public final List<Double> getFitnesses() {
		return this.fitnesses;
	}
	
	public final double getMeanFitness() {
		return Util.mean(this.getFitnesses());
	}
	
	public final void addRMSE(final double rmse) {
		this.rmses.add(rmse);
	}
	
	public final List<Double> getRMSEs() {
		return this.rmses;
	}
	
	public final double getMRMSE() {
		return Util.mean(this.getRMSEs());
	}
	
	public Phenotype getPhenotype(final List<?> targets) {
		return this.getPhenotype();
	}
	
	public Phenotype getPhenotype() {
		return new DoubleResults(this.genotype);
	}
	
	protected final List<Double> getStepSizes() {
		return this.stepSizes;
	}
	
	protected final void setStepSizes(final List<Double> stepSizes) {
		this.stepSizes = stepSizes;
	}
	
	protected List<Double> getGenotype() {
		return this.genotype;
	}
	
	protected void setGenotype(final List<Double> genotype) {
		this.genotype = genotype;
	}
	
	protected List<List<List<Double>>> getStructuredGenotype() {
		final List<List<List<Double>>> outerList = new ArrayList<List<List<Double>>>(1);
		final List<List<Double>> innterList = new ArrayList<List<Double>>(1);
		
		innterList.add(this.getGenotype());
		outerList.add(innterList);
		
		return outerList;
				
		
	}
}
