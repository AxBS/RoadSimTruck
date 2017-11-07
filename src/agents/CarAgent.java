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
import trafficData.TrafficData;
import trafficData.TrafficDataInStore;
import trafficData.TrafficDataOutStore;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.json.JSONObject;

import behaviours.CarBehaviour;
import behaviours.CarReceivingDataBehaviour;
import environment.Intersection;
import environment.Map;
import environment.Path;
import environment.Segment;
import environment.Step;
import jgrapht.Edge;

/**
 * This code represents a mobile car, it will have an origin an a 
 * destination and will get there using either the shortest, 
 * fastest or smartest path.
 *
 */
public class CarAgent extends Agent {


	private static final long serialVersionUID = 1L;

	private float x, y;
	private float currentPk;
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
	private String initialIntersection, finalIntersection;
	private boolean specialColor = false;
	private boolean smart = false;
	private Algorithm alg;
	private int algorithmType;
	private DefaultDirectedWeightedGraph<Intersection, Edge> jgraht;
   
	// This object stores current traffic sensored data
	// every time a car goes into a new segment, this object is
	// reseting.
	private TrafficData sensorTrafficData;

	// future: is for storing data received from other cars and
	//    used for computing my route to destination
	private TrafficDataInStore futureTraffic;

	// past: is for informing data to send to other cars about
	//    what is the traffic state in my performed route
    private TrafficDataOutStore pastTraffic;

	protected void setup() {

		//Register the agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("carAgent");
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
		this.drawGUI = (boolean) this.getArguments()[5];

		//Get the map from an argument
		this.map = (Map) this.getArguments()[0];
		//Get the jgraph from the map
		this.jgraht = this.map.getJgraht();
		System.out.println("CarAgent.java-- Get JgraphT: " + 
		                   this.jgraht.toString());
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
		
		String routeType = (String) this.getArguments()[4];
		
		if (routeType.equals("fastest")) {
			
			this.algorithmType = Method.FASTEST.value;
			this.alg = factory.getAlgorithm(Method.FASTEST);
			
		} else if (routeType.equals("shortest")) {
			 
			this.algorithmType = Method.SHORTEST.value;
			this.alg = factory.getAlgorithm(Method.SHORTEST);
			
		} else if (routeType.equals("smartest")) {
			
			this.algorithmType = Method.SMARTEST.value;
			this.alg = factory.getAlgorithm(Method.SMARTEST);
			this.smart = true;
		}
		
		//Get the initial time tick from eventManager
		tini = (long) this.getArguments()[6];
		
		//Get the ratio of sensoring for this agentCar
		ratio = (int) this.getArguments()[7];
		
		//Get the desired Path from the origin to the destination
		this.path = alg.getPath(this.map, getInitialIntersection(), 
				                getFinalIntersection(), 
				                this.maxSpeed);
		
		//Starting point
		setX(map.getIntersectionByID(getInitialIntersection()).
				                                          getX());
		setY(map.getIntersectionByID(getInitialIntersection()).
				                                          getY());
		
		//Store data received from other cars in a Map
		futureTraffic = new TrafficDataInStore();
		
		//Store data to send to other cars in my route
		pastTraffic = new TrafficDataOutStore();


		// Store current trafficData sensored by myself
		sensorTrafficData = new TrafficData();
		// Tini for measuring traffic data intervals in twin segments 
		//tini = elapsedtime;
		
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
			//We notify the interface about the new car
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(interfaceAgent.getName());
			JSONObject carData = new JSONObject();
			carData.put("x", this.x);
			carData.put("y", this.y);
			carData.put("id", this.id);
			carData.put("algorithmType", this.algorithmType);
			msg.setContent(carData.toString());
			msg.setOntology("newCarOntology");
			send(msg);
		}


		// Set the initial values for the carAgent on the road
		Step next = getPath().getGraphicalPath().get(0);
	    setCurrentSegment(next.getSegment());

		//Register
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("carToSegmentOntology");
		msg.setConversationId("register");
		msg.addReceiver(next.getSegment().getSegmentAgent().getAID());
		JSONObject carDataRegister = new JSONObject();
		carDataRegister.put("id", getId());
		carDataRegister.put("x", getX());
		carDataRegister.put("y", getY());
		carDataRegister.put("specialColor", getSpecialColor());
		carDataRegister.put("radio", getRatio());
		
		msg.setContent(carDataRegister.toString());
		
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
		addBehaviour(new CarBehaviour(this, 50, this.drawGUI));
		addBehaviour(new CarReceivingDataBehaviour(this));

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
	
	public DefaultDirectedWeightedGraph<Intersection, Edge> 
	                                                   getJgraht() {
		return jgraht;
	}

	public void setJgraht(
			  DefaultDirectedWeightedGraph<Intersection,Edge> jgraht){
		this.jgraht = jgraht;
	}

	public void setSpecialColor(boolean specialColor) {
		this.specialColor = specialColor;
	}
	
	public boolean isSmart() {
		
		return this.smart;
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

//	public long getElapsedtime() {
//		return elapsedtime;
//	}
//
//	public void increaseElapsedtime() {
//		this.elapsedtime++;
//	}

	public long getTini() {
		return tini;
	}

	public void setTini(long tini) {
		this.tini = tini;
	}

	public TrafficData getSensorTrafficData() {
		return sensorTrafficData;
	}

	public void setSensorTrafficData(TrafficData sensorTrafficData) {
		this.sensorTrafficData = sensorTrafficData;
	}

	public TrafficDataOutStore getPastTraffic() {
		return pastTraffic;
	}

	public void setPastTraffic(TrafficDataOutStore pastTraffic) {
		this.pastTraffic = pastTraffic;
	}

	public TrafficDataInStore getFutureTraffic() {
		return futureTraffic;
	}

	public void setFutureTraffic(TrafficDataInStore futureTraffic) {
		this.futureTraffic = futureTraffic;
	}

}
