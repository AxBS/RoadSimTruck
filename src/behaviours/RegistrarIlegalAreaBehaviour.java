package behaviours;

import jade.core.behaviours.OneShotBehaviour;
import agents.AreaAgent;

/**
 * This behaviour sends a Marker, Route and Polyline to the API.
 * This is for demo purposes only.
 *
 */
public class RegistrarIlegalAreaBehaviour extends OneShotBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3392177207099976678L;
	private String vehicle;
	
	
	public RegistrarIlegalAreaBehaviour(String v){
		
		vehicle = v;
	}

	@Override
	public void action() {
		
		System.out.println("Voy a registrar un ilegal " + ((AreaAgent) myAgent).getLocalName() + " vehiculo: " + vehicle);
		((AreaAgent) myAgent).doIllegalParking(vehicle);			
	}	
}
