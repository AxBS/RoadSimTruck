package behaviours;

import javax.swing.SwingUtilities;

import org.json.JSONObject;

import agents.InterfaceAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour is used by the InterfaceAgent and adds a new car to 
 *  the GUI and executes a behaviour to update the speed of the car.
 *
 */
public class InterfaceAddAreaBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 1L;

	//Template to listen for the new communications from cars
	private MessageTemplate mtNewArea = 
			MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchOntology("newAreaOntology"));
	
	private InterfaceAgent agent;
	
	public InterfaceAddAreaBehaviour(InterfaceAgent a) {
		
		this.agent = a; 
	}

	@Override
	public void action() {

		ACLMessage msg = myAgent.receive(mtNewArea);
		
		if (msg != null) {

			JSONObject cont = new JSONObject(msg.getContent());

			final String id = cont.getString("id");
			final int x = (int) cont.getInt("x");
			final int y = (int) cont.getInt("y");
			final int capacity = cont.getInt("capacity");
			
			//Add the car to the scene
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					agent.getMap().addArea(myAgent.getLocalName(), id,
							              capacity, x, y);
				}
			});
			
		} else block();
	}
}
