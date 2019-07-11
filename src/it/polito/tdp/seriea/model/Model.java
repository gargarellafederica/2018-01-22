package it.polito.tdp.seriea.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.seriea.db.SerieADAO;

public class Model {
	
	private SerieADAO dao;
	
	private List<Team> squadre;
	private List<Season> stagioni;	
	private Map<Integer, Season> mappaStagioni;
	private Map<String, Team> mappaSquadre;
	private Graph <Season, DefaultWeightedEdge> grafo;
	private int punteggiomax;
	
	
	private Map<Season, Integer> punteggi;

	public Model() {
		dao= new SerieADAO();
		
		mappaStagioni= new HashMap<>();
		mappaSquadre = new HashMap<>();
		
		this.squadre= dao.listTeams();
		for(Team t: this.squadre)
		{
		this.mappaSquadre.put(t.getTeam(), t);
		}
		this.stagioni= dao.listAllSeasons();
		for(Season s: this.stagioni)
		{
		this.mappaStagioni.put(s.getSeason(),s);
		}
	}
	
	public Map<Season, Integer> riempimappapunteggi(Team squadrasel) {
				
		this.punteggi= new HashMap<>();
		
		List<Match> match= new ArrayList<>();
		
		match = dao.listMatchesdataSquadra(squadrasel, mappaStagioni, mappaSquadre);

		for(Match m: match)
		{
			int punt=0;
			if(m.getHomeTeam().equals(squadrasel))
				{
					if(m.getFtr().equals("H"))
						punt =3;
					else if (m.getFtr().equals("D"))
						punt=1;
				}
			else if(m.getAwayTeam().equals(squadrasel))
			{
				if(m.getFtr().equals("H"))
					punt =3;
				else if (m.getFtr().equals("D"))
					punt=1;
			}

			Integer attuale = punteggi.get(m.getSeason());
			if (attuale == null)
				attuale = 0;

			punteggi.put(m.getSeason(), attuale + punt);
		}
		return this.punteggi;
		
	}

	public List<Team> getSquadre() {
		return this.squadre;
	}
	
	public Season calcolaAnnataDoro() {
		
		this.grafo= new SimpleDirectedWeightedGraph<Season, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		Graphs.addAllVertices(this.grafo, punteggi.keySet());
		for(Season s1: punteggi.keySet())
			for(Season s2: punteggi.keySet())
				if(!s1.equals(s2))
				{
					int differenza= punteggi.get(s1) - punteggi.get(s2);
					
					if(differenza>=0)
						Graphs.addEdge(this.grafo, s2, s1,differenza);
					else 
						Graphs.addEdge(this.grafo,s1, s2, -differenza);	
				}
		Season annatamigliore=null;
		punteggiomax=0;
		
		for(Season s: grafo.vertexSet()) {
		int punteggio=calcolapunteggio(s);
		if (punteggio>punteggiomax) {
			punteggiomax=punteggio;
			annatamigliore=s;
			}		
		}
		return annatamigliore;
		}

	private int calcolapunteggio(Season stagione) {
		int somma=0;
		
		for(DefaultWeightedEdge e: grafo.incomingEdgesOf(stagione)) 
			somma= (int) (somma+ grafo.getEdgeWeight(e));
		for(DefaultWeightedEdge e: grafo.outgoingEdgesOf(stagione)) 
			somma= (int) (somma -grafo.getEdgeWeight(e));
		
		return somma;
		}

	public int getPunteggiomax() {
		return punteggiomax;
	}
	}
