package it.polito.tdp.crimes.model;

import com.javadocmd.simplelatlng.LatLng;

public class Distretto {
	
	private int distrettoId;
	private LatLng centroGeografico;
	
	public Distretto(int distrettoId, LatLng centroGeografico) {
		super();
		this.distrettoId = distrettoId;
		this.centroGeografico = centroGeografico;
	}

	public int getDistrettoId() {
		return distrettoId;
	}

	public void setDistrettoId(int distrettoId) {
		this.distrettoId = distrettoId;
	}

	public LatLng getCentroGeografico() {
		return centroGeografico;
	}

	public void setCentroGeografico(LatLng centroGeografico) {
		this.centroGeografico = centroGeografico;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + distrettoId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Distretto other = (Distretto) obj;
		if (distrettoId != other.distrettoId)
			return false;
		return true;
	}
	
}
