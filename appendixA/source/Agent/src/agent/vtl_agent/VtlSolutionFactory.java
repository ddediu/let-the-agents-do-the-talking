package agent.vtl_agent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import agent.AbstractNnSolution;
import agent.AbstractSolutionFactory;
import agent.vtl_agent.VtlSolution;
import parameters.Activation;

public final class VtlSolutionFactory extends AbstractSolutionFactory {
	private final List<List<Double>> acTargets;
	private final Activation activation;
	final List<Integer> layerSizes;
	
	public VtlSolutionFactory(final Activation activation, final List<Integer> layerSizes,
			final List<List<Double>> acTargets, final double tauFactor, final int popSize) {
		
		super(tauFactor, AbstractNnSolution.getProblemSize(layerSizes));
		
		this.acTargets = acTargets;
		this.activation = activation;
		this.layerSizes = layerSizes;
		
		final char[] chars = new char[popSize];
		Arrays.fill(chars, '.');
		System.out.println(chars);
			
	}
	
	@Override
	public VtlSolution generateRandomCandidate(final Random rng) {
		final VtlSolution solution = new VtlSolution(super.randomToBoolean(rng), this.activation, 
				this.layerSizes, this.acTargets, super.globalTau);
		
		if(rng != null) {
			System.out.print(".");
		}
		
		return solution;
	}
}