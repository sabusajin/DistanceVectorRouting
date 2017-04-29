class DistVect {	
	private String source;
	private String via;
	private String destination;
	private double cost;
	
	public DistVect(String source, String via, String destination, double cost) {

		this.source = source;
		this.via = via;
		this.destination = destination;
		this.cost = cost;
	}

	public String getSource() {
		return source;
	}
	public String getVia() {
		return via;
	}
	public String getDestination() {
		return destination;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double newCost) {
		cost = newCost;
	}
}
