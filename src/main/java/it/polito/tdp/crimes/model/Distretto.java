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
	
}
