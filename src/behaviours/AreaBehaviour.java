package behaviours;

import agents.AreaAgent;
import agents.TruckAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AreaBehaviour extends CyclicBehaviour {

	/**
	 * Lo que ha de hacer el comportamiento del area es :
	 * - Recibir los mensajes de los que quieren prereservar y contestar si pueden o no
	 * - Recibir el mensaje de los que quieren reservar y realizar la reserva
	 * - Recibir los mensajes de los que quieren irse de la reserva
	 */
	private static final long serialVersionUID = 1L;
	MessageTemplate mt =  MessageTemplate.or(
			MessageTemplate.or(
			MessageTemplate.MatchOntology("prereserveOntology"),
			MessageTemplate.MatchOntology("reserveOntology")),
			MessageTemplate.MatchOntology("desreserveOntology"));
	private AreaAgent area;

	private AID topic;
	private boolean done = false;
	private boolean drawGUI;
	private long previousTick;
	
	public AreaBehaviour(AreaAgent a) {
		this.area = a;
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		
	}
	
}