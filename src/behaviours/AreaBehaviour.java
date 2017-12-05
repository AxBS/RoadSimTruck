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
			MessageTemplate.or(
			MessageTemplate.or(
			MessageTemplate.MatchOntology("prereserveOntology"),
			MessageTemplate.MatchOntology("reserveOntology")),
			MessageTemplate.MatchOntology("parkingOntology")),
			MessageTemplate.MatchOntology("leavingParkingOntology")),
			MessageTemplate.MatchOntology("leavingIllegalParkingOntology")),
			MessageTemplate.MatchOntology("illegalParkingOntology"));
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
		ACLMessage msgtmp;

		if(msg != null){
			if (msg.getOntology().equals("prereserveOntology")){
				if (areaAgent.isNegociando())
				{
					System.out.println("Ya hay negociacion en area: " + areaAgent.getLocalName());
					areaAgent.getBufferNegociaciones().add(msg);
					System.out.println("Apilo la reserva para " + areaAgent.getLocalName() + " del vehiculo " + msg.getSender().getLocalName() + " La cola es: " + areaAgent.getBufferNegociaciones().size());
				}
				else {
					areaAgent.setEstadoNegociacion(true);
					this.areaAgent.addBehaviour(new RecepcionPrereservasBehaviour(this.areaAgent, msg));
				}

			} else if (msg.getOntology().equals("reserveOntology")){
				this.areaAgent.addBehaviour(new RecepcionReservasBehaviour(this.areaAgent, msg));

			} else if (msg.getOntology().equals("parkingOntology")){
				this.areaAgent.getParking().add(msg.getSender().getLocalName());
				this.areaAgent.getLstReservas().remove(msg.getSender().getLocalName());
				System.out.println(" ---------------- Parking Ontology ------------------");
				System.out.println(this.areaAgent.getParking().toString());

			} else if (msg.getOntology().equals("leavingParkingOntology")){

				this.areaAgent.getParking().remove(msg.getSender().getLocalName());

			} else if (msg.getOntology().equals("illegalParkingOntology")){

				this.areaAgent.doIllegalParking(msg.getSender().getLocalName());
			} else if (msg.getOntology().equals("leavingIllegalParkingOntology"))
				this.areaAgent.getLstIlegales().remove(msg.getSender().getLocalName());
		}
		else if (areaAgent.getBufferNegociaciones().size() > 0)
		{
			if (!areaAgent.isNegociando()){
				areaAgent.setEstadoNegociacion(true);
				msgtmp = areaAgent.getBufferNegociaciones().get(0);
				areaAgent.getBufferNegociaciones().remove(0);
				System.out.println("Se DESapila AREA: " + areaAgent.getLocalName() + " VEHICULO: " + msgtmp.getSender().getLocalName() + " QUEDAN: " + areaAgent.getBufferNegociaciones().size());
				areaAgent.addBehaviour(new RecepcionPrereservasBehaviour(areaAgent, msgtmp));
			}
		}
		else
			block();
		
	}
	
}