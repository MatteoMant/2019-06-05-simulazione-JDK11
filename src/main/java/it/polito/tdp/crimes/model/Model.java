package it.polito.tdp.crimes.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	private EventsDao dao;
	private Graph<Distretto, DefaultWeightedEdge> grafo;
	
	public Model() {
		dao = new EventsDao();
	}
	
	public void creaGrafo(int anno) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		// Aggiunta dei vertici
		Graphs.addAllVertices(this.grafo, dao.getAllDistretti(anno));
	
		// Aggiunta degli archi
		for (Distretto d1 : this.grafo.vertexSet()) {
			for (Distretto d2 : this.grafo.vertexSet()) {
				if (d1.getDistrettoId() > d2.getDistrettoId()) {
					Graphs.addEdge(this.grafo, d1, d2, LatLngTool.distance(d1.getCentroGeografico(), d2.getCentroGeografico(), LengthUnit.KILOMETER));
				}
			}
		}
	}
	
	public List<Distretto> getAllVertici(){
		return new LinkedList<>(this.grafo.vertexSet());
	}
	
	public List<Adiacente> getAllAdiacenti(Distretto d){
		List<Adiacente> result = new LinkedList<>();
		List<Distretto> vicini = Graphs.neighborListOf(this.grafo, d);

		for (Distretto vicino : vicini) {
			result.add(new Adiacente(vicino, this.grafo.getEdgeWeight(this.grafo.getEdge(vicino, d))));
		}

		Collections.sort(result);
		
		return result;
	}
	
	
	public int getNumVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int getNumArchi() {
		return this.grafo.edgeSet().size();
	}
	
}
