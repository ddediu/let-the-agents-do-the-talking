package agent.vtl_agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import agent.AbstractNnSolution;
import agent.vtl_agent.vt.Manager;
import parameters.Activation; 

public final class VtlSolution extends AbstractNnSolution {
	private static final long serialVersionUID = -6607173257447340078L;
	//private static double[][] formatRanges = {{2,7},{4,15},{14,16},{15.5,17.5},{16.5,19}};
	private final List<List<Double>> acTargets;
	private List<Phonation> phonations;
	
	final class PhonationResults implements Phenotype {
		private final List<Phonation> phonations;
		
		public PhonationResults(List<Phonation> phonations) {
			this.phonations = phonations;
		}

		@Override
		public List<Phonation> getValues() {
			return this.phonations;
		}
	}
	
	public VtlSolution(final boolean random, final Activation activation, 
			final List<Integer> layerSizes, final List<List<Double>> acTargets, 
			final double globalTau) {
		
		super(activation, layerSizes, random, acTargets.size(), globalTau);
		
		this.phonations = new ArrayList<Phonation>(acTargets.size());
		this.acTargets = acTargets;
		
		if(random) {
			this.validateNn();
		}
	}
	
	@Override
	protected final VtlSolution getBlankClone() {
		return new VtlSolution(false, super.activation, super.layerSizes, this.acTargets, 
				super.globalTau);
	}
	
	@ Override
	protected final double[] getNeuralOutput(final List<Double> input) {
		final List<Double> nnInput = new ArrayList<Double>(input.size());
		
		for(int i = 0; i < input.size(); i++) {
			//final double formantMin = VtlSolution.formatRanges[i][0];
			//final double formantMax = VtlSolution.formatRanges[i][1];
			
			double frequency = input.get(i);
			//double n_frequency = (frequency - formantMin) / (formantMax - formantMin);
			double n_frequency = (frequency - 2) / 14;
			n_frequency = n_frequency * 10 - 5;
			
			nnInput.add(n_frequency);
		}
		
		final double[] scaledOutput = super.getNeuralOutput(input);
		return scaledOutput;
	}	
	
	@ Override
	public final PhonationResults getPhenotype() {
		return new PhonationResults(phonations);
	}

	@ Override
	@SuppressWarnings("unchecked")
	public final PhonationResults getPhenotype(final List<?> acTargets) {		
		if(this.phonations.size() != acTargets.size()) {
			this.submitTasks((List<List<Double>>) acTargets, null);
			
			final Manager manager = Manager.getInstance();
			final List<Phonation> phonations = new LinkedList<Phonation>();
			
			for(int i = 0; i < acTargets.size(); i++) {
				try {
					final Phonation phonation = manager.takeResult();
					phonations.add(phonation);
				}
				catch(NullPointerException e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
			
			Collections.sort(phonations);
			this.phonations = phonations;
		}
		
		return (PhonationResults) this.getPhenotype();
	}
	
	final void writeWav(final List<Double> acTarget, final String dataPath) {
		final List<List<Double>> acTargets = new ArrayList<List<Double>>(1);
		acTargets.add(acTarget);
		this.submitTasks(acTargets, dataPath);
	}
	
	private void submitTasks(final List<List<Double>> acTargets, final String wavPath) {
		final Manager manager = Manager.getInstance();
		
		for(int iTarget = 0; iTarget < acTargets.size(); iTarget++) {			
			final List<Double> acTarget = acTargets.get(iTarget);
			final double[] params = this.getNeuralOutput(acTarget);
			
			final PhonationTask phonationTask = new PhonationTask(params, acTarget, iTarget);
			phonationTask.setWavPath(wavPath);
			manager.submitTask(phonationTask);
			
			if(wavPath != null) {
				manager.takeResult();
			}
		}
	}
	
	private void validateNn() {
		this.getPhenotype(this.acTargets);
		
		final int nInputs = this.acTargets.get(0).size();
		
		for(final Phonation phonation: this.phonations) {
			final int nFormants = phonation.getFormants().size();
			
			if(nFormants < nInputs) {
				this.phonations.clear();
				super.randomize();
				this.validateNn();
				break;
			}
		}
	}	
	
	private final class PhonationTask implements Callable<Phonation>{
		private final double[] params;
		private final List<Double> target;
		private final int targetId;
		private String wavPath;
		
		private PhonationTask(final double[] params, final List<Double> target, final int targetId) {
			this.params = params;
			this.target = target;
			this.targetId = targetId;
			this.wavPath = null;
		}
		
		private void setWavPath(final String wavPath) {
			this.wavPath = wavPath;
		}
		
		@Override
		public Phonation call() {
			try {
				final Manager manager = Manager.getInstance();
				
				final Phonation phonation = manager.vocalize(this.params, 
						this.target.size(), false, wavPath);
				phonation.setTarget(this.target);
				phonation.setId(this.targetId);
				
				//this.wavPath = null;
				
				return phonation;
			}
			catch(NullPointerException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}