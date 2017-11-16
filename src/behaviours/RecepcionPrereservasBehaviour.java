package behaviours;

import agents.AreaAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class RecepcionPrereservasBehaviour extends OneShotBehaviour{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private AreaAgent area;
	private ACLMessage msg;

	public RecepcionPrereservasBehaviour(AreaAgent a, ACLMessage m) {
		area = a;
		msg = m;		
	}

	@Override
	public void action() {
		ACLMessage reply = msg.createReply();	
		reply.setReplyWith(msg.getReplyWith());
		reply.setContent("");
		reply.setOntology("prereserveOntologyReply");

		// Se puede reservar si las reservas + los trucks aparcados son < que los tracks que caben
		if (area.getLstReservas().size() + area.getParking().size() < area.getArea().getCapacity()) {
			
			// Anyado el agente a la reserva
			area.getLstReservas().add(msg.getSender().getLocalName());
			reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);			
			System.out.println(area.getArea().getId() + " CONFIRMA reserva de " + msg.getSender().getLocalName() + "Ahora estan: " + area.getLstReservas() );
			area.setEstadoNegociacion(false);
		}		
		//Empieza proceso negociacion
		else {
			reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
			area.setEstadoNegociacion(true);
			area.incNumeroTotalNegociaciones();
			System.out.println("===>>> " + area.getLocalName() + " Lanzo la negociacion numero " + area.getNumTotalNegociaciones() + " lanzada por " + msg.getSender().getLocalName());
			area.addBehaviour(new NegociacionAreaBehaviour(area, reply.getReplyWith(), msg.getSender().getLocalName()));
			System.out.println(area.getAID().getLocalName() + " deniega reserva de " + msg.getSender().getLocalName());			
		}
		//INFO: Incrementa el numero de respuestas  a la reserva pero no lo tenemos en cuenta
		//area.inc_Respuesta_Reserva();
		area.send(reply);

	}


}
