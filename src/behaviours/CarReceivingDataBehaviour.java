package behaviours;

import org.json.JSONObject;

import agents.CarAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarReceivingDataBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID =-8397872991964050209L;
	private MessageTemplate mtInform = 
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(
							ACLMessage.INFORM),
					MessageTemplate.MatchOntology(
							"roadStateOntology"));
	private CarAgent carAgent;
	
	public CarReceivingDataBehaviour(CarAgent carAgent) {
		super();
		this.carAgent = carAgent;
	}

	@Override
	public void action() {
		ACLMessage msg = myAgent.receive(mtInform);
		
		if (msg != null) {			
			// Integrate JSON data received from other cars in my 
			//       invision futureTraffic EDD for rerouting
			JSONObject datos = new JSONObject(msg.getContent());
			// Received current id, current speed,
			//   current position and pastTrafficState (that is to 
			//   be added to futureTrafficState of the agent that
			//   receives this msg)
			carAgent.getSensorTrafficData().getCarsSpeeds().
			                         add(datos.getDouble("speed"));
			carAgent.getSensorTrafficData().getCarsPositions().
			                         add(datos.getDouble("position"));
			carAgent.getSensorTrafficData().getIds().add(datos.getString("id"));
			
			for(String key:datos.getJSONObject("futureTraffic").
					             keySet()){
				carAgent.getFutureTraffic().
				         put(key, datos.getJSONObject(key));
			}
			
		} else block();

	}

}
