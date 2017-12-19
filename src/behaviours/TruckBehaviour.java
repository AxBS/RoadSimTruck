package behaviours;

import java.text.DecimalFormat;

import environment.Area;
import org.json.JSONObject;
import org.json.ToJSON;

import agents.CarAgent;
import agents.TruckAgent;
import environment.Segment;
import environment.Step;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import trafficData.TrafficData;

/**
 * This behaviour is used by the truckAgent and calculates the next 
 * graphical position of the truck. It also registers and deregisters
 * the car from the segments.
 * 
 * The truck is registered when it enters a new segment and deregistered
 * when it leaves a segment.
 *
 */
public class TruckBehaviour extends CyclicBehaviour {

	private TruckAgent agent;
	private AID topic;
	private boolean done = false;
	private char serviceLevelSegment;
	private boolean drawGUI;
	private long previousTick;
	private long timeToRest;
	private long tiempoDeParadaMedia;

	private MessageTemplate mtPrereserveFails =	MessageTemplate.MatchOntology("getTruckPreferencesOntology");
	private MessageTemplate mtCancelPrereserve = MessageTemplate.MatchOntology("cancelPrereserveOntology");
	private MessageTemplate mtIllegalPrereserve = MessageTemplate.MatchOntology("mustDoIllegalParkingOntology");

	public TruckBehaviour(TruckAgent a, long timeout, boolean drawGUI, long timeToRest) {

		this.agent = a;
		this.drawGUI = drawGUI;
		this.topic = null;
		previousTick = agent.getTini() - 1;
		this.timeToRest = timeToRest;
		this.tiempoDeParadaMedia = timeToRest;
		
		try {
			TopicManagementHelper topicHelper =(TopicManagementHelper) 
				this.agent.getHelper(TopicManagementHelper.
						                                SERVICE_NAME);
			topic = topicHelper.createTopic("tick");
			topicHelper.register(topic);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void action() {


		ACLMessage msgFailPrereserve = myAgent.receive(mtPrereserveFails);
		ACLMessage msgCancelPrereserve = myAgent.receive(mtCancelPrereserve);
		ACLMessage msgIllegalPrereserve = myAgent.receive(mtIllegalPrereserve);

		if(msgFailPrereserve != null){
			System.out.println("TRUCK : Nos solicitan las preferencias " + agent.getAID().getLocalName());
			if(!agent.isIllegalParking())
				agent.addBehaviour(new NegociacionVehiculoBehaviour(agent,msgFailPrereserve ));
		}

		if(msgCancelPrereserve != null){
			System.out.println("TRUCK : "+ agent.getAID().getLocalName()+ " Eliminamos la prereserva en " + msgCancelPrereserve.getSender().getLocalName());
			if(!agent.isIllegalParking()&& !agent.isReserved())
				agent.addBehaviour(new SolicitarPrereservaBehaviour(agent, agent.getFavouriteAreas().get(0).getId()));
		}

		if(msgIllegalPrereserve != null){
			System.out.println("TRUCK: "+ agent.getAID().getLocalName()+ " va a aparcar ilegalmente.");
			agent.setIllegalParking(true);
			this.agent.setDesignatedArea(this.agent.getFavouriteAreas().get(0));
		}


		// Block until tick is received
		ACLMessage msg = myAgent.receive(MessageTemplate.MatchTopic(topic));
		if (msg != null) {

			//If truck is stopped, take a tick out of waiting time until waiting time is 0
			if (agent.isStopped()) {
				//Truck dentro del area
				agent.setReserved(false);

				timeToRest--;
				//
				
				if (timeToRest <= 0) {
					//El truck sale del area
					//TODO Desatar negociación para siguiente area destino si no podemos llegar al destino final
					agent.setStopped(false);
					//stopped = false;

					timeToRest = this.tiempoDeParadaMedia;

					agent.setDistanceCovered(0);
					if(agent.isIllegalParking()) {
						this.sendMsgWithoutConversationId("leavingIllegalParkingOntology", this.agent.getDesignatedArea().getAreaAgent().getAID(), new JSONObject());
						this.agent.setDesignatedArea(null);
						agent.setIllegalParking(false);
					}else {
						this.sendMsgWithoutConversationId("leavingParkingOntology", this.agent.getDesignatedArea().getAreaAgent().getAID(), new JSONObject());
						System.out.println("Tiempo de espera acabado, saliendo..." + myAgent.getAID().getLocalName());
						this.agent.setDesignatedArea(null);
					}
					this.agent.calculateWay(this.agent.getFinalIntersection());
				}
			} else {

				long currentTick = Long.parseLong(msg.getContent());
				// Increase elapsed time
				// agent.increaseElapsedtime();
				// If I still have to move somewhere
				if (this.agent.getPath().getGraphicalPath().size() > 0) {

					// Get the path
					Step next = this.agent.getPath().getGraphicalPath().get(0);
					// First calculate the currentSpeed,Greenshield model
					int currentSpeed = (int) Math.min(agent.getMaxSpeed(),
							agent.getCurrentSegment().getMaxSpeed() * (1 - agent.getCurrentTrafficDensity() / 28.2));

					agent.setCurrentSpeed(currentSpeed);

					float currentPk = this.agent.getCurrentPk();

					// Virtual position
					float currentX = this.agent.getX();
					float currentY = this.agent.getY();

					// AreaX & AreaY
					Area areaDesignada = this.agent.getDesignatedArea();
					float AreaX;
					float AreaY;
					if(areaDesignada != null) {
						AreaX = areaDesignada.getIntersection().getX();
						AreaY = areaDesignada.getIntersection().getY();
					}else{
						AreaX = this.agent.getFavouriteAreas().get(0).getIntersection().getX();
						AreaY = this.agent.getFavouriteAreas().get(0).getIntersection().getY();
						/*this.agent.setDesignatedArea(this.agent.getFavouriteAreas().get(0));
						this.agent.setIllegalParking(true);*/
					}

					if(this.agent.getDesignatedArea() != null &&
							this.agent.getDistanceToIntersection(this.agent.getDesignatedArea().getIntersection()) <= 30
							&& !agent.isReserved()) {
						System.out.println("TRUCK: "+ myAgent.getAID().getLocalName()+"Mandamos un mensaje de reserva desde el TruckBehaviour");
						agent.setReserved(true);
						this.sendMsgWithoutConversationId("reserveOntology", this.agent.getDesignatedArea().getAreaAgent().getAID(), new JSONObject());
						//Actualizamos el lugar en el que paramos
						this.agent.setAreaX(this.agent.getDesignatedArea().getIntersection().getX());
						this.agent.setAreaY(this.agent.getDesignatedArea().getIntersection().getY());
					}

					// Update pkCurrent with this speed and the difference
					// between previousTick and currentTick
					// We transform km/h to k/s if divide it by 3600
					float pkIncrement = (float) (currentSpeed / 3600) * (currentTick - this.previousTick);
					//System.out.println("PKCurrent: " + currentPk);
					// The proportion of the map is 1px ~= 29m and one
					// tick =1s. Calculate the pixels per tick I have to
					// move
					float increment = this.agent.getCurrentSpeed() * 0.2778f * 0.035f;

					// The distance between my current position and my next
					// desired position
					float distNext = (float) Math
							.sqrt((currentX - next.getDestinationX()) * (currentX - next.getDestinationX())
									+ (currentY - next.getDestinationY()) * (currentY - next.getDestinationY()));

					// Check if we need to go to the next step
					while (increment > distNext) {

						// If there is still a node to go
						if (this.agent.getPath().getGraphicalPath().size() > 1) {
							// Remove the already run path
							increment -= distNext;
							this.agent.getPath().getGraphicalPath().remove(0);
							next = this.agent.getPath().getGraphicalPath().get(0);
							currentX = next.getOriginX();
							currentY = next.getOriginY();
							distNext = (float) Math.sqrt((currentX - next.getDestinationX())
									* (currentX - next.getDestinationX())
									+ (currentY - next.getDestinationY()) * (currentY - next.getDestinationY()));
						} else {
							if(this.agent.getCurrentSegment().getDestination().getId().equals(this.agent.getFinalIntersection()))
								this.kill();
							else {
								if(!this.agent.isIllegalParking()) {
									this.sendMsgWithoutConversationId("parkingOntology", this.agent.getDesignatedArea().getAreaAgent().getAID(), new JSONObject());
									System.out.println("::::::LEGAL TRUCK RESTING::::::");
								} else{
									this.sendMsgWithoutConversationId("illegalParkingOntology", this.agent.getDesignatedArea().getAreaAgent().getAID(), new JSONObject());
									System.out.println("::::::ILLEGAL TRUCK RESTING::::::");
								}
								agent.setStopped(true);


								System.out.println(agent.getAID().getLocalName() +  " ha llegado a un area");
								System.out.println("Area designada de " + agent.getAID().getLocalName() + " : " + agent.getDesignatedArea().getId());
							}
							break;
						}
					}

					if (!this.done) {

						// Proportion inside the segment
						float proportion = increment / distNext;

						// Update the current pk when update the x and y
						if ("up".compareTo(this.agent.getCurrentSegment().getDirection()) == 0) {
							this.agent.setCurrentPk(currentPk + proportion);
						} else {
							this.agent.setCurrentPk(currentPk - proportion);
						}
						this.agent.setX(((1 - proportion) * currentX + proportion * next.getDestinationX()));
						this.agent.setY(((1 - proportion) * currentY + proportion * next.getDestinationY()));

						// If I am in a new segment
						if (!this.agent.getCurrentSegment().equals(next.getSegment())) {

							long tfin = Long.parseLong(msg.getContent());
							// and Deregister from previous segment
							this.serviceLevelSegment = this.agent.getCurrentSegment().getCurrentServiceLevel();

							// Deregister from previous segment
							this.informSegment(this.agent.getCurrentSegment(), "deregister");

							String previousSegmentId = agent.getCurrentSegment().getId();
							// Set the new previous segment
							this.agent.setCurrentSegment(next.getSegment());

							// Register in the new segment
							this.informSegment(next.getSegment(), "register");

							// Calculate de information to remove the
							// segment that you register
							agent.setTini(tfin);
							agent.setCurrentPk(next.getSegment().getPkIni());
							// I don't know if remove the edge or if remove
							// the content of the edge
						}

						if (this.drawGUI) {
							if (this.agent.getCurrentSpeed() < Math.min(this.agent.getMaxSpeed(),
									this.agent.getCurrentSegment().getMaxSpeed())) {

								this.agent.setSpecialColor(true);
							} else {

								this.agent.setSpecialColor(false);
							}
						}

						this.informSegment(next.getSegment(), "update");

						previousTick = Long.parseLong(msg.getContent());
					}
				}
			}
		} else
			block();

	}

	//This method will send a message to a given segment
	private void informSegment(Segment segment, String type) {

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("carToSegmentOntology");
		msg.setConversationId(type);
		msg.addReceiver(segment.getSegmentAgent().getAID());
		JSONObject carDataRegister = new JSONObject();
		carDataRegister.put("id", this.agent.getId());
		carDataRegister.put("x", this.agent.getX());
		carDataRegister.put("y", this.agent.getY());
		carDataRegister.put("specialColor", 
				            this.agent.getSpecialColor());
		carDataRegister.put("radio", this.agent.getRatio());
		
		msg.setContent(carDataRegister.toString());
		myAgent.send(msg);
	}

	public void sendMsgWithoutConversationId(String ontology, AID receiver, JSONObject obj){
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology(ontology);
		msg.addReceiver(receiver);
		msg.setContent(obj.toString());
		myAgent.send(msg);
	}

	public void kill() {

		//Done flag
		this.done = true;
		//Deregister from previous segment
		this.informSegment(this.agent.getCurrentSegment(),
				           "deregister");

		//Delete the car from the canvas
		if (this.agent.getInterfaceAgent() != null && this.drawGUI) {

			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setOntology("deleteCarOntology");
			msg.addReceiver(this.agent.getInterfaceAgent().getName());
			msg.setContent(ToJSON.toJSon("id",this.agent.getId()));

			myAgent.send(msg);
		}
		
		//Deregister the agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.agent.getAID());
		
		try {
			DFService.deregister(this.agent,  dfd);
		} catch (Exception e) { 
		}

		this.agent.doDelete();
	}
	
	public static boolean aprox(final double d1, final double d2) {
		return Math.abs(d1 - d2) < 0.5;
	}
	
	private boolean rested() {
		if(timeToRest>0)
			timeToRest--;
		
		return true;
	}

}
