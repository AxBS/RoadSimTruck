package behaviours;

import org.json.JSONObject;

import agents.SegmentAgent;
import environment.Segment;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour is used by the SegmentAgent and listens to messages
 * either by cars to register, deregister or update themselves from it
 * or from the EventManagerAgent to tell them updates on its status.
 *
 */
public class SegmentListenBehaviour extends Behaviour {

	private static final long serialVersionUID =-2533061568306629976L;

	//Template to listen for the new communications from cars
	private MessageTemplate mtCarControl = 
			MessageTemplate.and(
			  MessageTemplate.MatchPerformative(ACLMessage.INFORM),
			  MessageTemplate.MatchOntology("carToSegmentOntology"));

	private MessageTemplate mtEventManagerControl = 
		 MessageTemplate.and(
		   MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		   MessageTemplate.MatchOntology(
				                   "eventManagerToSegmentOntology"));

	private MessageTemplate mt =
             MessageTemplate.or(mtCarControl, mtEventManagerControl);

	private SegmentAgent agent;

	//Constructor
	public SegmentListenBehaviour(SegmentAgent agent) {

		this.agent = agent;
	}

	@Override
	public void action() {

		ACLMessage msg = myAgent.receive(mt);

		if (msg != null) { //There is a message
			
			if (msg.getOntology().equals("carToSegmentOntology")) {

				JSONObject car = new JSONObject(msg.getContent());
				//System.out.println("coche recibido: " + car);

				//Register
				if (msg.getConversationId().equals("update")) { 
					//Update position

					this.agent.updateCar(car.getString("id"), 
							(float) car.getDouble("x"), 
							(float) car.getDouble("y"), 
							car.getBoolean("specialColor"));
				} else {
					if (msg.getConversationId().equals("register")) {
						// Register
						this.agent.addCar(car.getString("id"), 
								(float) car.getDouble("x"), 
								(float) car.getDouble("y"), 
								       car.getBoolean("specialColor"),
									   car.getInt("radio"));						
					} else             
						this.agent.removeCar(car.getString("id"));
					
					Segment segment = this.agent.getSegment();
					int numCars = this.agent.getCars().size();
					
					//Set the density
					double density = numCars/segment.getLength();
					segment.setDensity(density);
					
					//Set the service level
					if (density < 6.2) {
						
						segment.setCurrentServiceLevel('A');
					} else if (density < 10.0) {
						
						segment.setCurrentServiceLevel('B');
					} else if (density < 15.0) {
						
						segment.setCurrentServiceLevel('C');
					} else if (density < 20.0) {
						
						segment.setCurrentServiceLevel('D');
					} else if (density < 22.8) {
						
						segment.setCurrentServiceLevel('E');
					} else 
						segment.setCurrentServiceLevel('F');
					
					msg = new ACLMessage(ACLMessage.INFORM);
					msg.setOntology("trafficDensityOntology");
					//msg.setContent(""+density);
					
					JSONObject densityData = new JSONObject();
					densityData.put("density", density);
					msg.setContent(densityData.toString());
					
					for (String id:agent.getCars().keySet()) {
						msg.addReceiver(new AID(id, true));;			
					}
					agent.send(msg);	
					
				}

				
			} else if (msg.getOntology().
					        equals("eventManagerToSegmentOntology")) {
				
				Segment segment = this.agent.getSegment();
				
				char serviceLevel = msg.getContent().charAt(0);
				
				segment.setCurrentServiceLevel(serviceLevel);
			}
			
		} else block();
	}


	@Override
	public boolean done() {

		return false;
	}
}
