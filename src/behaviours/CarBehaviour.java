package behaviours;

import org.json.JSONObject;
import org.json.ToJSON;

import agents.CarAgent;
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
 * This behaviour is used by the CarAgent and calculates the next 
 * graphical position of the car. It also registers and deregisters
 * the car from the segments.
 * 
 * The car is registered when it enters a new segment and deregistered
 * when it leaves a segment.
 *
 */
public class CarBehaviour extends CyclicBehaviour {

	private CarAgent agent;
	private AID topic;
	private boolean done = false;
	private char serviceLevelSegment;
	private boolean drawGUI;
	private long previousTick;

	public CarBehaviour(CarAgent a, long timeout, boolean drawGUI) {

		this.agent = a;
		this.drawGUI = drawGUI;
		this.topic = null;
		previousTick = agent.getTini() - 1;
		
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

		//Block until tick is received
		ACLMessage msg = 
				myAgent.receive(MessageTemplate.MatchTopic(topic));

		if (msg != null) {
			
			long currentTick = Long.parseLong(msg.getContent());
		    // Increase elapsed time
			//agent.increaseElapsedtime();
			//If I still have to move somewhere
			if(this.agent.getPath().getGraphicalPath().size() > 0) {

				//Get the path
				Step next = this.agent.getPath().getGraphicalPath().
						                         get(0);
				// First calculate the currentSpeed,Greenshield model
				int currentSpeed = (int) Math.min(
				         agent.getMaxSpeed(),
				         agent.getCurrentSegment().getMaxSpeed() *
	                     (1-agent.getCurrentTrafficDensity()/28.2));
				
				agent.setCurrentSpeed(currentSpeed);
				
				float currentPk = this.agent.getCurrentPk();
				// Update pkCurrent with this speed and the difference
				//   between previousTick and currentTick
				//   We transform km/h to k/s if divide it by 3600
				float pkIncrement =(float) (currentSpeed / 3600) * (currentTick - this.previousTick) ;
				//System.out.println("PKCurrent: " + currentPk);
				//The proportion of the map is 1px ~= 29m and one 
				//  tick =1s. Calculate the pixels per tick I have to
				//   move
				float increment = this.agent.getCurrentSpeed() * 
						            0.2778f	* 0.035f;

				//Virtual position
				float currentX = this.agent.getX();
				float currentY = this.agent.getY();
				
				

				//The distance between my current position and my next 
				//   desired position
				float distNext = (float) Math.sqrt(
						(currentX - next.getDestinationX()) *
						(currentX - next.getDestinationX()) + 
						(currentY - next.getDestinationY()) *
						(currentY - next.getDestinationY()));

				//Check if we need to go to the next step
				while (increment > distNext) {

					//If there is still a node to go
					if (this.agent.getPath().getGraphicalPath().size()
							> 1) {

						//Remove the already run path
						increment -= distNext;

						this.agent.getPath().getGraphicalPath().
						                     remove(0);
						next = this.agent.getPath().
								getGraphicalPath().get(0);

						currentX = next.getOriginX();
						currentY = next.getOriginY();


						distNext = (float) Math.sqrt(
								(currentX - next.getDestinationX()) *
								(currentX - next.getDestinationX()) + 
								(currentY - next.getDestinationY()) *
								(currentY - next.getDestinationY()));					
					} else {

						this.kill();
						break;
					}
				}

				if (!this.done) {
					
					//Proportion inside the segment
					float proportion = increment / distNext;
					
					//Update the current pk when update the x and y
					if("up".compareTo(this.agent.getCurrentSegment().getDirection()) == 0){
						this.agent.setCurrentPk(currentPk + proportion);
					} else {
						this.agent.setCurrentPk(currentPk - proportion);
					}
					this.agent.setX(((1 - proportion) * currentX + 
							proportion * next.getDestinationX()));
					this.agent.setY(((1 - proportion) * currentY + 
							proportion * next.getDestinationY()));

					//If I am in a new segment
					if (!this.agent.getCurrentSegment().
							equals(next.getSegment())) {

						long tfin = Long.parseLong(msg.getContent());
						//Calculate the information to the jgraph 
						//   and Deregister from previous segment
						this.serviceLevelSegment = this.agent.
								            getCurrentSegment().
								            getCurrentServiceLevel();
						/*this.agent.getJgraht().addEdge(
						   this.agent.getCurrentSegment().getOrigin(),
						   next.getSegment().getOrigin(),
						   new Edge(this.agent.getCurrentSegment(),
							   	    this.serviceLevelSegment,
									agent.getTini(),
									tfin));
						*/
						//Deregister from previous segment
						this.informSegment(
							this.agent.getCurrentSegment(), 
							"deregister");

						String previousSegmentId = agent.
								         getCurrentSegment().getId();
						//Set the new previous segment
						this.agent.
						        setCurrentSegment(next.getSegment());

						//Register in the new segment
						this.informSegment(next.getSegment(),
								           "register");
						
						//Calculate de information to remove the 
						//   segment that you register
						agent.setTini(tfin);
						agent.setCurrentPk(next.getSegment().getPkIni());
						//I don't know if remove the edge or if remove
						//   the content of the edge
						/*this.agent.getJgraht().removeEdge(
								next.getSegment().getOrigin(), 
								next.getSegment().getDestination());*/
						
						// TODO:If we are using the smart algorithm, 
						//  recalculate all the traffic states on the 
						//  map with the information provided from 
						//  othercarAgents, and then rerouting 
						//  accordingly.
						// TODO: futureTrafficStore analysis

						/*if (this.agent.isSmart()) {
						    this.agent.recalculate(
								this.agent.getCurrentSegment().
								           getOrigin().getId());
						}*/
						
						// Once rerouted, Delete data from future 
						//     Traffic related to this new segment
						agent.getFutureTraffic().
						           delete(next.getSegment().getId());
						// Introducir el Tfin en TrafficData
						agent.getSensorTrafficData().
						             setTfin(tfin);
						// Introduce current TrafficData into 
						//     the pastTraffic
						// First give the number of cars detected
						agent.getSensorTrafficData().
						        setNumCars(agent.
						        	    getSensorTrafficData().
						        	    getCarsPositions().size());
						agent.getPastTraffic().put(previousSegmentId, 
								        agent.getSensorTrafficData());
						//Start a new current trafficData by myself
						agent.setSensorTrafficData(new TrafficData());
						agent.getSensorTrafficData().setTini(tfin);
					}
					
					//If we are going under the maximum speed I'm 
					//   allowed to go, or I can go, I am in a 
					//   congestion, draw me differently
					//I don't know if is necessary here but i 
					//   change this in the destination
					if(this.drawGUI){
						if (this.agent.getCurrentSpeed() < 
								Math.min(
								  this.agent.getMaxSpeed(),
								  this.agent.getCurrentSegment().
								             getMaxSpeed())) {
							
							this.agent.setSpecialColor(true);
						} else {
							
							this.agent.setSpecialColor(false);
						}
					}

					this.informSegment(next.getSegment(), "update");
					agent.addBehaviour(
							    new CarSendingDataBehaviour(agent));
					previousTick = Long.parseLong(msg.getContent());
				}
			}
		} else block();
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
}