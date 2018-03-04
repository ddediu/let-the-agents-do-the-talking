package parameters;

import java.util.List;

public final class AgentParams {
	private final Fitness fitnessFunction;
	private final AlgoType type;
	private final Activation activation;
	private final Problem problem;
	private final Selection parentSelection;
	private final Selection offspringSelection;	
	private final List<String> targetNames;
	private final List<String> freeParamAbbrevs;
	private final List<Integer> layerSpecs;
	private final String rootDir;
	private final double mutationRate;
	private final double crossoverRate;
	private final double tauFactor;
	private final double mseExponent;
	private final int iAnatomy;
	private final int nIterations;
	private final int popSize;
	private final int nFormants;
	private final int nElites;
	private final int lambdaFactor;
	private final boolean sigmaScaling;
	private final boolean rankingSelection;
	private final boolean plusSelection;
	
	private AgentParams(
			final Fitness fitnessFunction,
			final AlgoType type,
			final Activation activation,
			final Problem problem,
			final Selection parentSelection,
			final Selection offspringSelection,
			final List<String> targetNames, 
			final List<Integer> layerSpecs,
			final List<String> freeParamAbbrevs,
			final String rootDir,
			final double mutationRate, 
			final double crossoverRate,
			final double tauFactor,
			final double mseExponent,
			final int iAnatomy,
			final int nIterations,
			final int popSize,
			final int nFormants,
			final int nElites,
			final int lambdaFactor,
			final boolean sigmaScaling,
			final boolean rankingSelection,
			final boolean plusSelection) {
		
		this.fitnessFunction = fitnessFunction;
		this.type = type;
		this.activation = activation;
		this.problem = problem;
		this.parentSelection = parentSelection;
		this.offspringSelection = offspringSelection;
		this.targetNames = targetNames;
		this.layerSpecs = layerSpecs;
		this.freeParamAbbrevs = freeParamAbbrevs;
		this.rootDir = rootDir;		
		this.mutationRate = mutationRate;
		this.crossoverRate = crossoverRate;
		this.tauFactor = tauFactor;
		this.mseExponent = mseExponent;
		this.iAnatomy = iAnatomy;
		this.nIterations = nIterations;
		this.popSize = popSize;
		this.nFormants = nFormants;
		this.nElites = nElites;
		this.lambdaFactor = lambdaFactor;
		this.sigmaScaling = sigmaScaling;
		this.rankingSelection = rankingSelection;
		this.plusSelection = plusSelection;
	}
	
	public double mutationRate() {
		return this.mutationRate;
	}
	
	public double crossoverRate() {
		return this.crossoverRate;
	}
	
	public int nIterations() {
		return this.nIterations;
	}
	
	public int popSize() {
		return this.popSize;
	}
	
	public List<String> targetNames() {
		return this.targetNames;
	}
	
	public List<Integer> layerSpecs() {
		return this.layerSpecs;
	}
	
	public String rootDir() {
		return this.rootDir;
	}
	
	public List<String> freeParamAbbrevs() {
		return this.freeParamAbbrevs;
	}
	
	public Fitness fitnessFunction() {
		return this.fitnessFunction;
	}
	
	public AlgoType algoType() {
		return this.type;
	}
	
	public Activation activation() {
		return this.activation;
	}
	
	public Problem problem() {
		return this.problem;
	}
	
	public Selection parentSelection() {
		return this.parentSelection;
	}
	
	public Selection offspringSelection() {
		return this.offspringSelection;
	}
	
	public int nFormants() {
		return this.nFormants;
	}
	
	public int nElites() {
		return this.nElites;
	}
	
	public int lambdaFactor() {
		return this.lambdaFactor;
	}
	
	public int iAnatomy() {
		return this.iAnatomy;
	}
	
	public double tauFactor() {
		return this.tauFactor;
	}
	
	public double mseExponent() {
		return this.mseExponent;
	}
	
	public boolean sigmaScaling() {
		return this.sigmaScaling;
	}
	
	public boolean rankingSelection() {
		return this.rankingSelection;
	}
	
	public boolean plusSelection() {
		return this.plusSelection;
	}
	
	public static class ParamBuilder {
		private Fitness fitnessFunction;
		private AlgoType type;
		private Activation activation;
		private Problem problem;
		private Selection parentSelection;
		private Selection offspringSelection;
		private List<String> targetNames;
		private List<Integer> layerSpecs;
		private List<String> freeParamAbbrevs;
		private String rootDir;
		private double mutationRate;
		private double crossoverRate;
		private double tauFactor;
		private double mseExponent;		
		private int nIterations;
		private int popSize;
		private int nFormants;
		private int nElites;
		private int iAnatomy;
		private int lambdaFactor;
		private boolean sigmaScaling;
		private boolean rankingSelection;
		private boolean plusSelection;
		
		public ParamBuilder() {
			this.fitnessFunction = Fitness.MEAN;
			this.type = AlgoType.JANSSEN;
			this.activation = Activation.SIGMOID;
			this.problem = Problem.VTL;
			this.parentSelection = Selection.SUS;
			this.offspringSelection = Selection.SUS;
			this.mutationRate = 0.05;
			this.crossoverRate = 0;
			this.tauFactor = 0;
			this.mseExponent = 0.5;
			this.iAnatomy = 0;
			this.nElites = 1;
			this.lambdaFactor = 1;
			this.sigmaScaling = false;
			this.rankingSelection = true;
			this.plusSelection = false;	
		}
		
		public ParamBuilder mutationRate(final Number mutationRate) {
			this.mutationRate = mutationRate.doubleValue();
			return this;
		}
		
		public ParamBuilder crossoverRate(final Number crossoverRate) {
			this.crossoverRate = crossoverRate.doubleValue();
			return this;
		}
		
		public ParamBuilder nIterations(final Number nIterations) {
			this.nIterations = nIterations.intValue();
			return this;
		}
		
		public ParamBuilder popSize(final Number popSize) {
			this.popSize = popSize.intValue();
			return this;
		}
		
		public ParamBuilder targetNames(final List<String> targetNames) {
			this.targetNames = targetNames;
			return this;
		}
		
		public ParamBuilder layerSpecs(List<Integer> layerSpecs) {
			this.layerSpecs = layerSpecs;
			return this;
		}
		
		public ParamBuilder rootDir(final String rootDir) {
			this.rootDir = rootDir;
			return this;
		}
		
		public ParamBuilder freeParamAbbrevs(final List<String> freeParamAbbrevs) {			
			this.freeParamAbbrevs = freeParamAbbrevs;
			return this;
		}
		
		public ParamBuilder fitness(final Enum<?> fitnessFunction) {
			this.fitnessFunction = (Fitness) fitnessFunction;
			return this;
		}
		
		public ParamBuilder type(final Enum<?> type) {
			this.type = (AlgoType) type;
			return this;
		}
		
		public ParamBuilder activation(final Enum<?> activation) {
			this.activation = (Activation) activation;
			return this;
		}
		
		public ParamBuilder problem(final Enum<?> problem) {
			this.problem = (Problem) problem;
			return this;
		}
		
		public ParamBuilder parentSelection(final Enum<?> parentSelection) {
			this.parentSelection = (Selection) parentSelection;
			return this;
		}
		
		public ParamBuilder offspringSelection(final Enum<?> offSpringSelection) {
			this.offspringSelection = (Selection) offSpringSelection;
			return this;
		}
		
		public ParamBuilder nFormants(final int nFormants) {
			this.nFormants = nFormants;
			return this;
		}
		
		public ParamBuilder nElites(final Number nElites) {
			this.nElites = nElites.intValue();
			return this;
		}
		
		public ParamBuilder lambdaFactor(final Number lambdaFactor) {
			this.lambdaFactor = lambdaFactor.intValue();
			return this;
		}
		
		public ParamBuilder tauFactor(final Number tauFactor) {
			this.tauFactor = tauFactor.doubleValue();
			return this;
		}
		
		public ParamBuilder mseExponent(final Number mseExponent) {
			this.mseExponent = mseExponent.doubleValue();
			return this;
		}
		
		public ParamBuilder iAnatomy(final Number iAnatomy) {
			this.iAnatomy = iAnatomy.intValue();
			return this;
		}
		
		public ParamBuilder sigmaScaling(final boolean sigmaScaling) {
			this.sigmaScaling = sigmaScaling;
			return this;
		}
		
		public ParamBuilder rankingSelection(final boolean rankingSelection) {
			this.rankingSelection = rankingSelection;
			return this;
		}
		
		public ParamBuilder plusSelection(final boolean plusSelection) {
			this.plusSelection = plusSelection;
			return this;
		}
		
		public AgentParams build() {
			return new AgentParams(this.fitnessFunction, this.type, this.activation, this.problem, 
					this.parentSelection, this.offspringSelection, this.targetNames, this.layerSpecs,
					this.freeParamAbbrevs, this.rootDir, this.mutationRate, this.crossoverRate,
					this.tauFactor, this.mseExponent, this.iAnatomy, this.nIterations, this.popSize, 
					this.nFormants, this.nElites, this.lambdaFactor, this.sigmaScaling, 
					this.rankingSelection, this.plusSelection);
		}
	}
}
