package agents;

import behaviours.SolicitarPrereservaBehaviour;
import environment.*;
import environment.Map;
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

import java.util.*;

import org.json.JSONObject;

import behaviours.TruckBehaviour;

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
	private float AreaX=0, AreaY=0;

	//Parking Ilegal o no
	private boolean illegalParking = false;
	
	
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

		//Get the map from an argument
		this.map = (Map) this.getArguments()[0];
		//Get the starting and final points of my trip

		//Starting point
		float pkIni = (float) this.getArguments()[1];
		Segment segmentIni = this.map.getSegmentByID((String)this.getArguments()[2]);
		this.initialIntersection = segmentIni.getDestination().getId();
		this.finalIntersection = (String) this.getArguments()[3];
		
		//Get the speeds
		this.maxSpeed = (int) this.getArguments()[4];
		this.currentSpeed = 0; //Se gestiona en el comportamiento 
		                       // (int) this.getArguments()[4];

		this.maxDistanceToGo = (double) this.getArguments()[5];
		//Get the method we want
		AlgorithmFactory factory = new AlgorithmFactory();
		this.alg = null;


		String routeType = (String) this.getArguments()[6];
		
		if (routeType.equals("fastest")) {
			
			this.algorithmType = Method.FASTEST.value;
			this.alg = factory.getAlgorithm(Method.FASTEST);
			
		} else if (routeType.equals("shortest")) {
			 
			this.algorithmType = Method.SHORTEST.value;
			this.alg = factory.getAlgorithm(Method.SHORTEST);
			
		}

		//Is necessary draw the gui
		this.drawGUI = (boolean) this.getArguments()[7];

		//Get the initial time tick from eventManager
		tini = (long) this.getArguments()[8];
		
		//Get the ratio of sensoring for this agentCar
		ratio = (int) this.getArguments()[9];

		this.path = this.alg.getPath(this.map, this.initialIntersection,
				this.finalIntersection, this.maxSpeed);

		//Get the desired Path from the origin to the destination
		this.calculateWay(finalIntersection);
		
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

		//TODO corregir la x e y
		this.calculateCoordinatesFromPk(pkIni, segmentIni);

		if(this.drawGUI){
			//We notify the interface (send msg to InterfaceAgent) about the new truck
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(interfaceAgent.getName());
			JSONObject truckData = new JSONObject();
			truckData.put("seg", segmentIni);
			truckData.put("x", this.x);
			truckData.put("y", this.y);
			truckData.put("id", this.id);
			truckData.put("algorithmType", this.algorithmType);
			msg.setContent(truckData.toString());
			msg.setOntology("newTruckOntology");
			send(msg);
		}
		//:::::::::::::::::::::::::::::::::::::		//:::::::::::::::::::::::::::::::::::::		//:::::::::::::::::::::::::::::::::::::

		// Set the initial values for the truckAgent on the road
		Step next = getPath().getGraphicalPath().get(0);
	    setCurrentSegment(next.getSegment());

		//Register
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		//:::::::::::::::::::::::::::::::::::::
		//TODO change to truckToSegmentOntology
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
	    
		addBehaviour(new TruckBehaviour(this, 50, this.drawGUI,this.timeToRest));
		//addBehaviour(new CarReceivingDataBehaviour(this));

	}

	/**
	 * Calcular el camino tanto la inicio como cada vez que acabemos
	 * uno de los semitrayectos
	 *
	 * @param interFinal Id de la intersección a la que queremos llegar
	 * */
	public void calculateWay(String interFinal){
		System.out.println("CalculateWay");
		System.out.println("Distancia a la intersección --> " + this.getDistanceToIntersection(this.map.getIntersectionByID(interFinal)));
		System.out.println("Maxima distacia --> " + this.maxDistanceToGo);
		if(this.getDistanceToIntersection(this.map.getIntersectionByID(interFinal)) > this.maxDistanceToGo){
			//Generas las areas favoritas
			this.generateFavouriteAreas(this.map.getListAreas());
			addBehaviour(new SolicitarPrereservaBehaviour(this, "-"));
			// En la prereserva cambiaremos el path del coche para que vaya a ese sitio
		} else{
			//TODO Pensamos desde el origen y es más eficiente pensar desde el destino para no
			//TODO tener que dar la vuelta
			this.path = this.alg.getPath(this.map, this.getCurrentSegment().getOrigin().getId(),
					this.finalIntersection, this.maxSpeed);
		}
	}


	/*
	 * Calcula la distancia hasta un area concreta desde nuestra posicción actual
	 * @params  area Area hasta la que queremos calcular la distancia
	 */
	
	public double getDistanceToIntersection(Intersection i) {
		double distance = 0;

		Path path;
		if(this.currentSegment == null)
			path = alg.getPath(map, this.getCurrentSegment().getOrigin().getId(), i.getId(), this.maxSpeed);
		else
			path = alg.getPath(map, this.getCurrentSegment().getDestination().getId(), i.getId(), this.maxSpeed);

		for(Segment seg : path.getSegmentPath()) {
			distance+= seg.getLength();
			
			//Tenemos en cuenta la distancia desde la posición actual del camión hasta el inicio del segmento (intersección)
			if(seg.getId().equals(getCurrentSegment().getId())) {
				double ini = getCurrentSegment().getPkIni();
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
		ArrayList<Area> preferences = new ArrayList<>();
		HashMap<Double,Area> bag = new HashMap<>();
		
		
		for(Area a:areas) {
			//Filtramos aquellas Areas inalcanzables
			if((this.maxDistanceToGo-this.distanceCovered)>=this.getDistanceToIntersection(a.getIntersection()))
				bag.put(this.getDistanceToIntersection(a.getIntersection()), a);
		}
		
		Set<String> keySet = (Set) bag.keySet();
		ArrayList<String> list = new ArrayList<String>(keySet);     
		Collections.sort(list);
		Collections.reverse(list);
		
		for(int i = 0; i<3 && i<list.size(); i++) {
			preferences.add(bag.get(list.get(i)));
		}

		this.favouriteAreas = preferences;
		System.out.println("Areas favoritas " + this.getFavouriteAreas().toString());
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
	 * Calcular la x e y en función del pk
	 * */
	public void calculateCoordinatesFromPk(float pkToStart, Segment s){
		//System.out.println("----------------------------------------------");
		//System.out.println("CALCULATE COORDINATES FROM PK");
		LinkedList<Step> steps =(LinkedList<Step>) s.getSteps();
		//System.out.println("Steps --> " + steps.toString());
		float pkIni = s.getPkIni();
		//System.out.println("Segment pk ini --> " + pkIni);
		float distFin = pkToStart-pkIni;
		//System.out.println("Distancia final --> " + distFin);
		double segLength = s.getLength();
		//System.out.println("Distancia segment --> " + segLength);
		//Calculamos la proporción del pk en cada step
		ArrayList<Double> distanceStep = new ArrayList<>();
		for(Step st: steps)
			distanceStep.add(Math.sqrt(Math.pow((st.getDestinationX()-st.getOriginX()),2.f)) + Math.pow((st.getDestinationY()-st.getOriginY()),2.f));
		//System.out.println("Distancia en px de steps --> " + distanceStep.toString());
		//Sumarlas todas
		double distTotalSteps = 0;
		for(double d: distanceStep)
			distTotalSteps+= d;
		//System.out.println("Distancia total de los steps --> " + distTotalSteps);

		//Sacar los km de cada step
		ArrayList<Double> kmStep = new ArrayList<>();
		for(double d: distanceStep)
			kmStep.add((d*segLength)/distTotalSteps);

		//System.out.println("Km de los steps --> " + kmStep.toString());
		Step stepToStart = null;
		double distanceAux = 0;
		int i = 0;
		for(double d: kmStep) {
			distanceAux += d;
			if (distanceAux > pkIni){
				stepToStart = steps.get(i);
				break;
			}
			i++;
		}

		//System.out.println("Distancia del step en el que te has pasado -->" + distanceAux);
		//System.out.println("Step en el que inicias --> " + stepToStart);

		//Tenemos el step
		if(stepToStart == null){
			System.out.println("calculateCoordinatesFromPk - No ha encontrado el step. ALgo ha fallado");
		}else {
			int xStep = stepToStart.getOriginX();
			int yStep = stepToStart.getOriginY();
			double distancFromIniStep = distFin;
			int indice = i;
			while(indice > 0){
				//System.out.println("Indice dentro de los km --> " + indice);
				//System.out.println("Indice total --> " + i);
				distancFromIniStep -= kmStep.get(indice);
				indice--;
			}
			//System.out.println("Distancia from init step --> " + distancFromIniStep);
			int yToModify = (int)(Math.abs(stepToStart.getDestinationY()-stepToStart.getOriginY())/kmStep.get(i)*distancFromIniStep);
			int xToModify = (int)(Math.abs(stepToStart.getDestinationX()-stepToStart.getOriginX())/kmStep.get(i)*distancFromIniStep);
			//System.out.println(" y - " + (int)(Math.abs(stepToStart.getDestinationY()-stepToStart.getOriginY())/kmStep.get(i)*distancFromIniStep));
			//System.out.println(" x - " + (int)(Math.abs(stepToStart.getDestinationX()-stepToStart.getOriginX())/kmStep.get(i)*distancFromIniStep));

			//Pensado en la dirección de la recta
			if(stepToStart.getDestinationX() > stepToStart.getOriginX() ){
				xToModify += stepToStart.getOriginX();
			} else{
				xToModify = stepToStart.getOriginX() - xToModify;
			}

			if(stepToStart.getDestinationY() > stepToStart.getOriginY() ){
				yToModify += stepToStart.getOriginY();
			} else{
				yToModify = stepToStart.getOriginY() - yToModify;
			}

			//System.out.println("xFinal --> " + xToModify);
			//System.out.println("yFinal --> " + yToModify);

			setX(xToModify);
			setY(yToModify);
		}
		//System.out.println("---------------------------------------------");
	}


	/**
	 * Recalculate the route, this will be called from the behaviour 
	 *     if we are smart.
	 * 
	 * @param origin ID of the intersection where the car is
	 * @param destination ID of the intersection where the car has to go
	 */
	public void recalculate(String origin, String destination) {
		
		// A JGraph envision structure must be obteined from jgraphsT 
		//     received by other cars in the twin segment of the 
		//     current segment where the car is going.
		// TODO:
		this.path = this.alg.getPath(this.map, origin, 
				               destination, this.maxSpeed);
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
		if(currentSegment == null)
			return this.map.getIntersectionByID(this.initialIntersection).getOutSegments().get(0);
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


	public ArrayList<Area> getFavouriteAreas() {
		return favouriteAreas;
	}

	public void setFavouriteAreas(ArrayList<Area> favouriteAreas) {
		this.favouriteAreas = favouriteAreas;
	}

	public Area getDesignatedArea() {
		return designatedArea;
	}

	public boolean isIllegalParking() {
		return illegalParking;
	}

	public void setIllegalParking(boolean illegalParking) {
		this.illegalParking = illegalParking;
	}

	public void setDesignatedArea(Area designatedArea) {
		this.designatedArea = designatedArea;
	}

	public String getActualDestination() {
		return actualDestination;
	}

	public void setActualDestination(String actualDestination) {
		this.actualDestination = actualDestination;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public double getDistanceCovered() {
		return distanceCovered;
	}

	public void setDistanceCovered(double distanceCovered) {
		this.distanceCovered = distanceCovered;
	}

	public void setDesignatedAreaFromString(String areaId){
		for(Area a : this.map.getListAreas())
			if(a.getId().equals(areaId))
				this.setDesignatedArea(a);
	}

	public java.util.Map getLstAreas(){
		HashMap<String, Area> map = new HashMap<String, Area>();
		for(Area a: this.getFavouriteAreas()){
			map.put(a.getId(), a);
		}

		return map;
	}

	public double getKmToStop(){
		return this.maxDistanceToGo - this.distanceCovered;
	}

}

