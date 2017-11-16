package behaviours;

import agents.AreaAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AreaBehaviour extends CyclicBehaviour {

	/**
	 * Lo que ha de hacer el comportamiento del areaAgent es :
	 * - Recibir los mensajes de los que quieren prereservar y contestar si pueden o no
	 * - Recibir el mensaje de los que quieren reservar y realizar la reserva
	 * - Recibir los mensajes de los que quieren irse de la reserva
	 */
	private static final long serialVersionUID = 1L;
	MessageTemplate mt =  MessageTemplate.or(
			MessageTemplate.or(
			MessageTemplate.or(
			MessageTemplate.MatchOntology("prereserveOntology"),
			MessageTemplate.MatchOntology("reserveOntology")),
			MessageTemplate.MatchOntology("desreserveOntology")),
			MessageTemplate.MatchOntology("illegalReserveOntology"));
	private AreaAgent areaAgent;

	private AID topic;
	private boolean done = false;
	private boolean drawGUI;
	private long previousTick;
	
	public AreaBehaviour(AreaAgent a) {
		this.areaAgent = a;
	}

	@Override
	public void action() {
		//Receive the areaAgent instruction
		ACLMessage msg = myAgent.receive(mt);
		ACLMessage msgAreaResponse =
				new ACLMessage(ACLMessage.INFORM);

		if(msg != null){
			msgAreaResponse.addReceiver(msg.getSender());
			if (msg.getOntology().equals("prereserveOntology")){

				this.areaAgent.addBehaviour(new RecepcionPrereservasBehaviour(this.areaAgent,msg));

			} else if (msg.getOntology().equals("reserveOntology")){
				if(this.areaAgent.doReserve(msg.getContent())){
					msgAreaResponse.setContent("true");
				} else {
					msgAreaResponse.setContent("false");
				}
				this.areaAgent.send(msgAreaResponse);
			} else if (msg.getOntology().equals("unreserveOntology")){
				this.areaAgent.doUnreserve(msg.getContent());
			} else if (msg.getOntology().equals("illegalParkingOntology")){
				this.areaAgent.doIllegalParking(msg.getContent());
			}
		}
		
	}
	
}