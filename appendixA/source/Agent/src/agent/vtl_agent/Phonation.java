package agent.vtl_agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Phonation implements Comparable<Phonation>, Serializable{
	private static final long serialVersionUID = -7595631954780754577L;
	private final List<Double> freeParams;
	private final List<Double> fixedParams;
	private final List<Double> nishimura;
	private List<Double> formants;	
	private int targetId;
	private List<Double> acTarget;
	
	public static List<List<Double>> getAcTargets(final List<Phonation> phonations) {
		final List<List<Double>> acTargets = new ArrayList<List<Double>>(phonations.size());
		
		for(final Phonation phonation: phonations) {
			acTargets.add(phonation.getTarget());
		}
		
		return acTargets;
	}
	
	public Phonation(final List<Double> formants, final List<Double> freeParams, 
			final List<Double> fixedParams, final List<Double> nishimura) {
		
		this.formants = formants;
		this.freeParams = freeParams;
		this.fixedParams = fixedParams;
		this.nishimura = nishimura;
	}
	
	@Override
	public int compareTo(final Phonation comparison) {
		return this.targetId - comparison.getId();
	}
	
	public List<Double> getFormants() {
		return this.formants;
	}
	
	public List<Double> getFreeParams() {
		return this.freeParams;
	}
	
	public void setTarget(final List<Double> target) {
		this.acTarget = target;
	}
	
	public List<Double> getTarget() {
		return this.acTarget;
	}
	
	List<Double> getFixedParam() {
		return this.fixedParams;
	}
	
	public List<Double> getNishimura() {
		return this.nishimura;
	}
	
	void setId(int targetId) {
		this.targetId = targetId;
	}
	
	private int getId() {
		return this.targetId;
	}
}