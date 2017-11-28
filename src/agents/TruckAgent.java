package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import searchAlgorithms.Algorithm;
import searchAlgorithms.AlgorithmFactory;
import searchAlgorithms.Method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import behaviours.TruckBehaviour;
import environment.Area;
import environment.Map;
import environment.Path;
import environment.Segment;
import environment.Step;

/**
 * This code represents a Truck, it will have an origin an a 
 * destination and will get there using either the shortest, 
 * fastest or smartest path. Also, it will stop in some areas to rest.
 *
 *
 * Los comportamientos que Jose implementa (ListenerVehiculosBehaviour):
 * 		NEGOCIACION_SOLICITAR_PREFERENCIAS - NegociacionVehiculoBehaviour
 * 		NEGOCIACION_ASIGNACION_RESERVAS - AsignacionReservasVehiculosBehaviour
 * 		ELIMINAR_RESERVAS - EliminacionVehiculoBehaviour
 * 	(VehicleBehaviour) añade subbehaviours:
 * 		ObtenerAreasBehaviour
 * 		OrdenarListaAreasBehaviour
 * 		SolicitarReservaBehaviour
 *
 */
public class TruckAgent extends Agent {


	private static final long serialVersionUID = 1L;

	private float x, y;
	private float currentPk;
	
	//Time spent resting IN SECONDS
	private long timeToRest = 120; 

	//Selected Area to stop
	private float AreaX=317, AreaY=304;
	
	
	//Favourite area list
	private ArrayList<Area> favouriteAreas;
	private Area designatedArea;
	
	//MaxDistance the truck can cover without stopping
	double maxDistanceToGo = 0;
	double distanceCovered = 0;
	
	private int direction;
	private int ratio;
	private int currentSpeed, maxSpeed;
	private double currentTrafficDensity;
	private long tini; // For measuring temporal intervals of traffic
	private String id; 
	private DFAgentDescription interfaceAgent;
	private boolean drawGUI;
	private Map map;
	private Path path;
	private Segment currentSegment;
	private String initialIntersection, finalIntersection;	//Origin of the truck and final destination
	private String actualDestination; 		//Where the truck is heading NOW
	private boolean specialColor = false;
	private Algorithm alg;
	private int algorithmType;

	/**
	 * Jose variables
	 * */
	private int tiempoConduccion = 180;
	private int tiempoMaximoConduccion = 240;

	protected void setup() {
		
		//Register the agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("truckAgent");
		sd.setName(getLocalName());

		dfd.addServices(sd);
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) {
			
			//Sometimes an agent cannot find the DF in time
			//I still don't know when this happens so I will
			//simply kill it for now.
			this.takeDown();
		}
		
		//Is necessary draw the gui
		this.drawGUI = (boolean) this.getArguments()[6];
		this.maxDistanceToGo = (double) this.getArguments()[4];

		//Get the map from an argument
		this.map = (Map) this.getArguments()[0];
		//Get the starting and final points of my trip
		this.initialIntersection = (String) this.getArguments()[1];
		this.finalIntersection = (String) this.getArguments()[2];
		
		//Get the speeds
		this.maxSpeed = (int) this.getArguments()[3];
		this.currentSpeed = 0; //Se gestiona en el comportamiento 
		                       // (int) this.getArguments()[4];

		//Get the method we want
		AlgorithmFactory factory = new AlgorithmFactory();
		this.alg = null;
		
		String routeType = (String) this.getArguments()[5];
		
		if (routeType.equals("fastest")) {
			
			this.algorithmType = Method.FASTEST.value;
			this.alg = factory.getAlgorithm(Method.FASTEST);
			
		} else if (routeType.equals("shortest")) {
			 
			this.algorithmType = Method.SHORTEST.value;
			this.alg = factory.getAlgorithm(Method.SHORTEST);
			
		}

		//Get the initial time tick from eventManager
		tini = (long) this.getArguments()[7];
		
		//Get the ratio of sensoring for this agentCar
		ratio = (int) this.getArguments()[8];
		
		//Get the desired Path from the origin to the destination
		this.path = alg.getPath(this.map, getInitialIntersection(),
				getFinalIntersection(),
				this.maxSpeed);

		if(this.calculateDistanceFromSegments(this.path.getSegmentPath()) > this.maxDistanceToGo){
			//Generas las areas favoritas
			this.generateFavouriteAreas(this.map.getListAreas());
			//TODO Behavior prereserva --> area
		}

		
		//Starting point
		setX(map.getIntersectionByID(getInitialIntersection()).
				                                          getX());
		setY(map.getIntersectionByID(getInitialIntersection()).
				                                          getY());
		
		if(this.drawGUI){
			//Find the interface agent
			dfd = new DFAgentDescription();
			sd = new ServiceDescription();
			sd.setType("interfaceAgent");
			dfd.addServices(sd);

			DFAgentDescription[] result = null;

			try {
				result = DFService.searchUntilFound(
						this, getDefaultDF(), dfd, null, 5000);
			} catch (FIPAException e) { e.printStackTrace(); }

			while (result == null || result[0] == null) {
				
				try {
					result = DFService.searchUntilFound(
							this, getDefaultDF(), dfd, null, 5000);
				} catch (FIPAException e) { e.printStackTrace(); }
			}
			
			this.interfaceAgent = result[0];
		}
		
		//An unique identifier for the car
		this.id = getName().toString();

		
		if(this.drawGUI){
			//We notify the interface (send msg to InterfaceAgent) about the new truck
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(interfaceAgent.getName());
			JSONObject truckData = new JSONObject();
			truckData.put("x", this.x);
			truckData.put("y", this.y);
			truckData.put("id", this.id);
			truckData.put("algorithmType", this.algorithmType);
			msg.setContent(truckData.toString());
			//TODO change to newTruckOntology
			msg.setOntology("newCarOntology");
			send(msg);
		}
		//:::::::::::::::::::::::::::::::::::::		//:::::::::::::::::::::::::::::::::::::		//:::::::::::::::::::::::::::::::::::::
		//:::::::::::::::::::::::::::::::::::::		//:::::::::::::::::::::::::::::::::::::		//:::::::::::::::::::::::::::::::::::::
		//:::::::::::::::::::::::::::::::::::::		//:::::::::::::::::::::::::::::::::::::		//:::::::::::::::::::::::::::::::::::::
		//:::::::::::::::::::::::::::::::::::::		//:::::::::::::::::::::::::::::::::::::		//:::::::::::::::::::::::::::::::::::::
		//:::::::::::::::::::::::::::::::::::::		//:::::::::::::::::::::::::::::::::::::		//:::::::::::::::::::::::::::::::::::::

		// Set the initial values for the truckAgent on the road
		Step next = getPath().getGraphicalPath().get(0);
	    setCurrentSegment(next.getSegment());

		//Register
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		//:::::::::::::::::::::::::::::::::::::
		//TODO change to truckToSegmentOntology
		//:::::::::::::::::::::::::::::::::::::
		//TODO: Aquí hay que hacer una prereserva pero para ello tenemos que ver cuales son sus predilectos
		msg.setOntology("carToSegmentOntology");
		msg.setConversationId("register");
		msg.addReceiver(next.getSegment().getSegmentAgent().getAID());
		JSONObject truckDataRegister = new JSONObject();
		truckDataRegister.put("id", getId());
		truckDataRegister.put("x", getX());
		truckDataRegister.put("y", getY());
		truckDataRegister.put("specialColor", getSpecialColor());
		truckDataRegister.put("radio", getRatio());
		
		msg.setContent(truckDataRegister.toString());
		
		send(msg);
		// Receive the current traffic density from the current 
		//    segment
		msg = blockingReceive(MessageTemplate.
				             MatchOntology("trafficDensityOntology"));
		JSONObject densityData = new JSONObject(msg.getContent());
		setCurrentTrafficDensity(densityData.getDouble("density"));

		//Change my speed according to the maximum allowed speed
	    setCurrentSpeed(Math.min(getMaxSpeed(), 
	    			getCurrentSegment().getCurrentAllowedSpeed()));
		
	    //The special color is useless without the interfaceAgent
	    if(this.drawGUI){
	    	//If we are going under my absolute maximum speed or the
	    	//   segment's maxSpeed => I am in a congestion, so
	    	//   draw me differently
		    if (getCurrentSpeed() < Math.min(this.getMaxSpeed(), 
		    		       this.getCurrentSegment().getMaxSpeed())) {
		    	setSpecialColor(true);
		    } else {
		    	setSpecialColor(false);
		    }
	    }
		//Runs the agent
	    //TODO
	    //Antes de añadir comportamiento de movimiento, necesitamos saber adonde vamos
	    //Añadir comportamiento de negociación de area
	    
		addBehaviour(new TruckBehaviour(this, 50, this.drawGUI,this.timeToRest));
		//addBehaviour(new CarReceivingDataBehaviour(this));

	}
	/*
	 * Calcula la distancia hasta un area concreta desde nuestra posicción actual
	 * @params  area Area hasta la que queremos calcular la distancia
	 */
	
	private double getDistanceToArea(Area area) {
		double distance = 0;
		
		Path path;

		Segment segmentToBegin;

		if(this.getCurrentSegment()!=null)
			segmentToBegin =  currentSegment;
		else
			segmentToBegin = alg.getPath(map, this.map.getIntersectionByID(this.getInitialIntersection()).getId(), area.getIntersection().getId(), this.maxSpeed)
					.getSegmentPath().get(0);

		path = alg.getPath(map, segmentToBegin.getOrigin().getId(), area.getIntersection().getId(), this.maxSpeed);

		for(Segment seg : path.getSegmentPath()) {
			distance+= seg.getLength();
			
			//Tenemos en cuenta la distancia desde la posición actual del camión hasta el inicio del segmento (intersección)
			if(seg.getId().equals(segmentToBegin.getId())) {
				double ini = segmentToBegin.getPkIni();
				double pos = this.currentPk;
				
				distance -= Math.abs(ini-pos);
			}
		}
		return distance;
	}
	
	
	/**
	 * Generates and sets a favourite list of areas for the truck
	 * 
	 * @param areas List of existing areas
	 */
	public void generateFavouriteAreas(ArrayList<Area> areas) {
		System.out.println("Areas a priorizar");
		System.out.println(areas.toString());
		ArrayList<Area> preferences = new ArrayList<>();
		HashMap<Double,Area> bag = new HashMap<>();
		
		
		for(Area a:areas) {
			//Filtramos aquellas Areas inalcanzables
			if((this.maxDistanceToGo-this.distanceCovered)>=this.getDistanceToArea(a))
				bag.put(this.getDistanceToArea(a), a);
		}
		
		Set<String> keySet = (Set) bag.keySet();
		ArrayList<String> list = new ArrayList<String>(keySet);     
		Collections.sort(list);
		Collections.reverse(list);
		
		for(int i = 0; i<3 && i<list.size(); i++) {
			preferences.add(bag.get(list.get(i)));
		}

		this.favouriteAreas = preferences;
	}
	
	/**
	 * Returns the distance to cover all the segments in a list
	 * 
	 * @param list List of segments to drive through
	 */
	public double calculateDistanceFromSegments(List<Segment> list) {
		double dist=0;

		for(Segment s: list) {
			dist+=s.getLength();
		}
		return dist;
	}
	
	/**
	 * Encuentra la destinación del track actual
	 * 
	 *
	 */
	public void findActualDestination() {
		//TODO
		double dist=0;
		Path roadToDestination = alg.getPath(this.map, getInitialIntersection(),getFinalIntersection(),this.maxSpeed);
		
		
		dist  = calculateDistanceFromSegments(roadToDestination.getSegmentPath());
		
		if(dist>(this.maxDistanceToGo-distanceCovered)) {
			System.err.println("JAJAJAJ");
		}
		
		
	}

	/**
	 * Recalculate the route, this will be called from the behaviour 
	 *     if we are smart.
	 * 
	 * @param origin ID of the intersection where the car is
	 */
	public void recalculate(String origin) {
		
		// A JGraph envision structure must be obteined from jgraphsT 
		//     received by other cars in the twin segment of the 
		//     current segment where the car is going.
		// TODO:
		this.path = this.alg.getPath(this.map, origin, 
				               getFinalIntersection(), this.maxSpeed);
	}

	//Setters and getters
	public int getDirection() {
		return direction;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getCurrentPk() {
		return currentPk;
	}

	public void setCurrentPk(float currentPk) {
		this.currentPk = currentPk;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(int currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public DFAgentDescription getInterfaceAgent() {
		return interfaceAgent;
	}

	public Map getMap() {
		return map;
	}

	public Path getPath() {
		return path;
	}

	public String getId() {
		return id;
	}

	public Segment getCurrentSegment() {
		return currentSegment;
	}

	public void setCurrentSegment(Segment previousSegment) {
		this.currentSegment = previousSegment;
	}

	public String getInitialIntersection() {
		return initialIntersection;
	}

	public String getFinalIntersection() {
		return finalIntersection;
	}

	public boolean getSpecialColor() {
		return specialColor;
	}

	public void setSpecialColor(boolean specialColor) {
		this.specialColor = specialColor;
	}

	public int getAlgorithmType() {
		return algorithmType;
	}	
	
	public int getRatio() {
		return ratio;
	}

	public void setRatio(int ratio) {
		this.ratio = ratio;
	}
	
	public double getCurrentTrafficDensity() {
		return currentTrafficDensity;
	}

	public void setCurrentTrafficDensity(double currentTD) {
		this.currentTrafficDensity = currentTD;
	}

	public long getTini() {
		return tini;
	}

	public void setTini(long tini) {
		this.tini = tini;
	}
	
	public float getAreaX() {
		return AreaX;
	}

	public void setAreaX(float areaX) {
		AreaX = areaX;
	}

	public float getAreaY() {
		return AreaY;
	}

	public void setAreaY(float areaY) {
		AreaY = areaY;
	}
	

	public long getTimeToRest() {
		return timeToRest;
	}

	public void setTimeToRest(long timeToRest) {
		this.timeToRest = timeToRest;
	}


}

