package behaviours;

import agents.TruckAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;


public class NegociacionVehiculoBehaviour extends Behaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TruckAgent vehicle;
	private ACLMessage msg;
	
	public NegociacionVehiculoBehaviour(TruckAgent v, ACLMessage mensaje) {
		super();
		vehicle = v;	
		msg = mensaje;
		//idNegociacion = id;
	}

	@Override
	public void action() {
		ACLMessage reply = msg.createReply();		
		reply.setOntology("sendTruckPreferencesOntology");
		reply.setReplyWith(msg.getReplyWith());
		reply.setContent(vehicle.getLstAreas().keySet().toString());		
		reply.setInReplyTo(String.valueOf(vehicle.getKmToStop()));
		reply.setEncoding(String.valueOf(vehicle.getCurrentPk())); //Usado para pasar la posicion.
		//vehicle.inc_Mensajes_pref();
		vehicle.send(reply);
		
	}

	
	@Override
	public boolean done() {		
		return true;
	}
	
	


}
