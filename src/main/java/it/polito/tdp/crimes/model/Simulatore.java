package it.polito.tdp.crimes.model;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.crimes.db.EventsDao;
import it.polito.tdp.crimes.model.Evento.EventType;

public class Simulatore {

	// TIPI DI EVENTO 
	
	//1. Evento criminoso
	// 1.1 La centrale seleziona l'agente libero più vicino
	// 1.2 Se non ci sono disponibilità -> crimine mal gestito
	// 1.3 Se c'è un agente libero -> setto l'agente a occupato
		
	//2. L'agente selezionato ARRIVA sul posto
	// 2.1 Definisco quanto durerà l'intervento
	// 2.2 Controllo se il crimine è mal gestito (ritardo dell'agente)
		
	//3. Crimine TERMINATO
	// 3.1 "Libero" l'agente, che torna a essere disponibile
	
	// Parametri della simulazione
//	private Integer N;
//	private Integer anno;
//	private Integer mese;
//	private Integer giorno;
	
	// modello del mondo
	private Graph<Distretto, DefaultWeightedEdge> grafo;
	private Map<Integer, Integer> agenti; // mappa distrettoID -> # agenti liberi
	private Map<Integer, Distretto> idMap;
	
	// Coda degli eventi
	private PriorityQueue<Evento> queue;
	
	// Output della simulazione
	private Integer malGestiti;
	
	public void init(Integer N, Integer anno, Integer mese, Integer giorno,
			Graph<Distretto, DefaultWeightedEdge> grafo) {
//		this.N = N;
//		this.anno = anno;
//		this.mese = mese;
//		this.giorno = giorno;
		this.grafo = grafo;
		
		this.malGestiti = 0;
		
		this.agenti = new HashMap<>();
		for (Distretto d : this.grafo.vertexSet()) {
			this.agenti.put(d.getDistrettoId(), 0); // metto il numero di agenti liberi in ogni distretto pari a 0
		}
		
		// Adesso dobbiamo capire dove si trova la centrale di polizia e mettere gli N agenti in tale distretto
		// (cioè il distretto con il minor numero di crimini nell'anno selezionato)
		EventsDao dao = new EventsDao();
		idMap = new HashMap<>();
		dao.getAllDistretti(anno, idMap);
		Distretto minimo = dao.getDistrettoMenoCrimini(anno, idMap);
		this.agenti.put(minimo.getDistrettoId(), N);
		
		// creo e inizializzo la coda
		this.queue = new PriorityQueue<Evento>();
		
		// tutti gli eventi di un dato giorno devono essere gestiti (o almeno bisogna provarci)
		for(Event e : dao.listAllEventsByDate(anno, mese, giorno)) {
			queue.add(new Evento(EventType.CRIMINE, e.getReported_date(), e));
		}
		
	}
	
	public int run() {
		Evento e;
		while((e = queue.poll()) != null) {
			switch (e.getTipo()) {
				case CRIMINE:
					System.out.println("NUOVO CRIMINE! " + e.getCrimine().getIncident_id());
					//cerco l'agente libero più vicino
					Integer distretto = null;
					distretto = cercaDistrettoPiuVicino(e.getCrimine().getDistrict_id()); // vado alla ricerca del distretto più vicino in cui vi sono agenti liberi
					if(distretto != null) {  // in questo caso ho trovato il distretto più vicino a quello in cui si è verificato il crimine con degli agenti liberi in tale distretto
						// ci sarà almeno un agente disponibile nel distretto appena trovato (non mi interessa quale agente)
						this.agenti.put(distretto, this.agenti.get(distretto) - 1); // il numero di agenti in tale distretto diminuisce di uno
						// cerco di capire quanto ci metterà l'agente libero ad arrivare sul posto dal distretto in cui si trova
						Double distanza;
						if(distretto.equals(e.getCrimine().getDistrict_id()))
							distanza = 0.0; // l'agente si trovava già nel distretto in cui si è verificato il crimine
						else
							distanza = this.grafo.getEdgeWeight(this.grafo.getEdge(idMap.get(distretto), idMap.get(e.getCrimine().getDistrict_id())));
						
						Long seconds = (long) ((distanza * 1000)/(60/3.6)); // tempo di percorrenza per arrivare sul luogo
						
						// se si verifica un evento di tipo 'CRIMINE' allora dobbiamo generare un evento di tipo 'ARRIVA_AGENTE'
						this.queue.add(new Evento(EventType.ARRIVA_AGENTE, e.getData().plusSeconds(seconds), e.getCrimine()));  
						// la data dell'evento 'ARRIVA_AGENTE' è uguale alla data dell'evento 'CRIMINE' più il tempo di percorrenza
					} else {
						// NON c'è nessun agente libero al momento -> crimine mal gestito
						System.out.println("CRIMINE " + e.getCrimine().getIncident_id() + " MAL GESTITO!");
						this.malGestiti ++;
					}
					break;
				case ARRIVA_AGENTE:
					System.out.println("ARRIVA AGENTE PER CRIMINE! " + e.getCrimine().getIncident_id());
					Long duration = getDurata(e.getCrimine().getOffense_category_id()); // quanto ci vuole a gestire il suddetto crimine

					// se l'evento è di tipo 'ARRIVA_AGENTE' significa che il crimine verrà gestito e quindi creiamo un nuovo evento di tipo 'GESTITO'
					this.queue.add(new Evento(EventType.GESTITO, e.getData().plusSeconds(duration), e.getCrimine()));
					// la data di questo nuovo evento è pari alla data di arrivo dell'agente più quanto tempo ci vuole per gestire tale crimine
					
					// controllare se il crimine è mal gestito
					// se la data di arrivo dell'agente è successiva alla data del crimine maggiorata di 15 minuti allora avrò un crimine mal gestito
					if (e.getData().isAfter(e.getCrimine().getReported_date().plusMinutes(15))) {
						System.out.println("CRIMINE " + e.getCrimine().getIncident_id() + " MAL GESTITO!");
						this.malGestiti ++;
					}
					break;
				case GESTITO:
					System.out.println("CRIMINE " + e.getCrimine().getIncident_id() + " GESTITO");
					this.agenti.put(e.getCrimine().getDistrict_id(), this.agenti.get(e.getCrimine().getDistrict_id())+1); 
					// l'agente che ha gestito il crimine rimane sul posto e risulta disponibile ad occuparsi di un nuovo evento
					break;
			}
		}
		
		return this.malGestiti;
	}

	private Long getDurata(String offense_category_id) {
		if(offense_category_id.equals("all_other_crimes")) {
			Random r = new Random();
			if(r.nextDouble() > 0.5)
				return Long.valueOf(2*60*60); // 2 ore (espresse in secondi)
			else
				return Long.valueOf(1*60*60); // 1 ora (espressa in secondi)
		} else {
			return Long.valueOf(2*60*60);
		}
	}

	private Integer cercaDistrettoPiuVicino(Integer district_id) {
		Double distanza = Double.MAX_VALUE;
		Integer distretto = null;
		
		for(Integer d : this.agenti.keySet()) {
			if(this.agenti.get(d) > 0) {  // se il numero di agenti disponibili in quel distretto è maggiore di 0 
				if(district_id.equals(d)) { // in questo caso vorrà dire che ci sono agenti liberi nel distretto in cui si è verificato il crimine
					distanza = 0.0; // chiaramente la distanza è nulla in quanto l'agente si trova già nel posto
					distretto = d; // il distretto più vicino è proprio lo stesso distretto in cui si è verificato il crimine
				} else if (this.grafo.getEdgeWeight(this.grafo.getEdge(idMap.get(district_id), idMap.get(d))) < distanza) {
					distanza = this.grafo.getEdgeWeight(this.grafo.getEdge(idMap.get(district_id), idMap.get(d)));
					distretto = d;
				}
			}
		} 
		return distretto;
	}
	
}
