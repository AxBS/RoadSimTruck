 package behaviours;

import javax.swing.SwingUtilities;

import environment.Step;
import org.json.JSONObject;

import agents.InterfaceAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

 /**
 * This behaviour is used by the InterfaceAgent and adds a new car to 
 *  the GUI and executes a behaviour to update the speed of the car.
 *
 */
public class InterfaceAddTruckBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 1L;

	//Template to listen for the new communications from cars
	private MessageTemplate mtNewTruck = 
			MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchOntology("newTruckOntology"));
	
	private InterfaceAgent agent;
	
	public InterfaceAddTruckBehaviour(InterfaceAgent a) {
		
		this.agent = a;
	}

	@Override
	public void action() {

		ACLMessage msg = myAgent.receive(mtNewTruck);
		
		if (msg != null) {

			JSONObject cont = new JSONObject(msg.getContent());

			final String id = cont.getString("id");
			final String seg = cont.getString("seg");
			final float x = (float) cont.getInt("x");
			final float y = (float) cont.getInt("y");
			final int algorithmType = cont.getInt("algorithmType");
			
			//Add the car to the scene
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					//TODO Add recalculate x and y
					ArrayList<Step> steps = (ArrayList<Step>) agent.getGraphicalMap().getSegmentByID(seg).getSteps();
					for(Step step: steps) {
						// Deberiamos considerar que se encuentre dentro de la recta
						// pero para las pruebas que vamos a hacer no lo
						//voy a considerar porque no tenemos tiempo
						int maxX, maxY, minX, minY;
						if (step.getDestinationX() > step.getOriginX()) {
							maxX = step.getDestinationX();
							minX = step.getOriginX();
						} else {
							minX = step.getDestinationX();
							maxX = step.getOriginX();
						}

						if(step.getDestinationY() > step.getOriginY()){
							maxY = step.getDestinationY();
							minY = step.getOriginY();
						} else {
							minY = step.getDestinationY();
							maxY = step.getOriginY();
						}
						// Tenemos el step en el que está
						if(x <= maxX && x>= minX && y <= maxY && y >= minY ){

						}
					}
					agent.getMap().addCar(myAgent.getLocalName(), id,
							              algorithmType, x, y, false);
				}
			});
			
		} else block();
	}
}