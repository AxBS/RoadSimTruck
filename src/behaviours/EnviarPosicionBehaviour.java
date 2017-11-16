package behavioursAreas;

import others.TipoMensaje;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class EnviarPosicionBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int posicion;
	ACLMessage msg;

	public EnviarPosicionBehaviour(int posicion, ACLMessage m) {
		this.posicion = posicion;
		msg = m;
	}
	
	@Override
	public void action() {		
		ACLMessage reply = msg.createReply();	
		reply.setReplyWith(msg.getReplyWith());
		reply.setConversationId(TipoMensaje.LOCALIZAR_AREAS);
		reply.setContent(String.valueOf(posicion));
		reply.setPerformative(ACLMessage.CONFIRM);
		myAgent.send(reply);
	}

}
