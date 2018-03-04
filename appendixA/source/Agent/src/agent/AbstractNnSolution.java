package agent;

import java.util.ArrayList;
import java.util.List;

import org.encog.engine.network.activation.ActivationElliott;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;

import parameters.Activation;
import util.Util;

public abstract class AbstractNnSolution extends AbstractSolution {
	protected final List<Integer> layerSizes;
	protected final Activation activation;
	private static final long serialVersionUID = -3363129673235983466L;
	private final BasicNetwork nn;
	private double[] nnOutput;
	
	protected AbstractNnSolution(final Activation activation, final List<Integer> layerSizes, 
			final boolean random, final int nObjectives, final double globalTau) {
		
		super((int) AbstractNnSolution.getProblemSize(layerSizes), random, nObjectives, globalTau);
		
		this.nn = this.createNn(layerSizes, activation);
		this.layerSizes = layerSizes;
		this.activation = activation;
		
		if(random) {
			this.randomize();
		}
	}
	
	public static int getProblemSize(final List<Integer> layerSizes) {
		int problemSize = 0;
		
		for(int i = 0; i < layerSizes.size() - 1; i++) {
			problemSize += (layerSizes.get(i) + 1) * layerSizes.get(i + 1);
		}
		
		return problemSize;
	}
	
	protected double[] getNeuralOutput(final List<Double> input) {		
		final double[] nnInput = new double[input.size()];
		
		for(int i = 0; i < input.size(); i++) {
			nnInput[i] = input.get(i);
		}
		
		double[] nnOutput = new double[this.nn.getOutputCount()];
		this.nn.compute(nnInput, nnOutput);
		
		this.nnOutput = nnOutput;
		return this.getNeuralOutput();
	}
	
	protected final double[] getNeuralOutput() {
		return this.nnOutput;
	}
	
	@Override
	protected final List<List<List<Double>>> getStructuredGenotype() {		
		final int nLayers = this.nn.getLayerCount();
		final List<List<List<Double>>> weights = new ArrayList<List<List<Double>>>(nLayers-1);
		
		for(int iLayer = 0; iLayer < nLayers-1; iLayer++) {
			final int layerSize = this.nn.getLayerTotalNeuronCount(iLayer);
			final List<List<Double>> layerWeights = new ArrayList<List<Double>>(layerSize);
			
			for(int iNeuronA = 0; iNeuronA < layerSize; iNeuronA++) {
				final int nextLayerSize = this.nn.getLayerNeuronCount(iLayer+1);
				final List<Double> neuronWeights = new ArrayList<Double>(nextLayerSize);
				
				for(int iNeuronB = 0; iNeuronB < nextLayerSize; iNeuronB++) {
					final double weight = this.nn.getWeight(iLayer, iNeuronA, iNeuronB);
					neuronWeights.add(weight);
				}
				layerWeights.add(neuronWeights);
			}
			weights.add(layerWeights);
		}
		
		return weights;
	}
	
	protected final void randomize() {
		this.nn.getFlat().randomize();
	}
	
	@ Override
	protected final void setGenotype(final List<Double> genotype){
		this.nn.getFlat().setWeights(Util.listToArray(genotype));
	}
	
	@Override
	protected List<Double> getGenotype() {
		return Util.arrayToList(this.nn.getFlat().getWeights());
	}
	
	private BasicNetwork createNn(final List<Integer> layerSizes, final Activation activation) {
		final BasicNetwork nn = new BasicNetwork();
		
		for(int iLayer = 0; iLayer < layerSizes.size(); iLayer++) {
			final boolean bias = iLayer < layerSizes.size() - 1 ? true : false;
			
			final ActivationFunction activationFunction;
			
			switch(activation) {
				case SIGMOID:
					activationFunction = new ActivationSigmoid();
					break;
				case ELLIOTT:
					activationFunction = new ActivationElliott();
					break;
				default:
					System.err.println("Invalid activation function!");
					activationFunction = null;
		        	System.exit(-1);
			}
			
			nn.addLayer(new BasicLayer(activationFunction, bias, layerSizes.get(iLayer)));
		}
		
		nn.getStructure().finalizeStructure();
		return nn;
	}
}
