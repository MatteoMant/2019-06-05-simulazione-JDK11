package it.polito.tdp.crimes.model;

public class Adiacente implements Comparable<Adiacente>{
	
	private Distretto adiacente;
	private Double distanza;
	
	public Adiacente(Distretto adiacente, Double distanza) {
		super();
		this.adiacente = adiacente;
		this.distanza = distanza;
	}

	public Distretto getAdiacente() {
		return adiacente;
	}

	public Double getDistanza() {
		return distanza;
	}

	@Override
	public String toString() {
		return adiacente.getDistrettoId() + " - " + distanza;
	}

	@Override
	public int compareTo(Adiacente other) {
		return this.getDistanza().compareTo(other.getDistanza());
	}
	
}
