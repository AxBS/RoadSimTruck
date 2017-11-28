package behaviours;

import agents.AreaAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class RecepcionReservasBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AreaAgent area;
	private ACLMessage msg;
	
	public RecepcionReservasBehaviour(AreaAgent a, ACLMessage m) {
		area = a;
		msg = m;		
	}

	@Override
	public void action() {
		ACLMessage reply = msg.createReply();	
		reply.setReplyWith(msg.getReplyWith());
		reply.setContent("");
		reply.setOntology("reserveOntology");

		
		// Se puede reservar
		if (area.getLstReservas().size() + area.getParking().size() < area.getArea().getCapacity() && this.isPrereserved(msg.getSender().getLocalName())) {
			
			// Anyado el agente a la reserva
			area.getLstReservas().add(msg.getSender().getLocalName());
			area.getLstPreReservas().remove(msg.getSender().getLocalName());
			reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);			
			System.out.println(area.getAID().getLocalName() + " CONFIRMA reserva de " + msg.getSender().getLocalName() + "Ahora estan: " + area.getLstReservas() );
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
		//area.inc_Respuesta_Reserva();
		area.send(reply);

	}

	private boolean isPrereserved(String truckName){
		for(String s: this.area.getLstPreReservas())
			if(s.equals(truckName))
				return true;
		return false;
	}


}
