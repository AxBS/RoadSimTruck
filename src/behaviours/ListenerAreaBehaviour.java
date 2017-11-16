package behaviours;

import others.TipoMensaje;
import agents.AreaAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ListenerAreaBehaviour extends CyclicBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MessageTemplate mt;
	private AreaAgent area;
	
	public ListenerAreaBehaviour(AreaAgent a) {
		MessageTemplate mt1;
		MessageTemplate mt2;
		MessageTemplate mt3;
		area = a;
		mt1 = MessageTemplate.or(MessageTemplate.MatchConversationId(TipoMensaje.ELIMINACION_AREA), MessageTemplate.MatchConversationId(TipoMensaje.LOCALIZAR_AREAS));
		mt2 = MessageTemplate.or(mt1,  MessageTemplate.MatchConversationId(TipoMensaje.ELIMINAR_RESERVAS));
		mt3 = MessageTemplate.or(mt2, MessageTemplate.MatchConversationId(TipoMensaje.RESERVAR_AREA));
		mt = MessageTemplate.or(mt3, MessageTemplate.MatchConversationId(TipoMensaje.REGISTRAR_VEHICULO_ILEGAL));
	}

	@Override
	public void action() {
		ACLMessage msg = myAgent.receive(mt);
		ACLMessage msgtmp;
		if (msg != null)  
		{		
			if (msg.getConversationId().equals(TipoMensaje.RESERVAR_AREA) )
			{
				area.inc_Sol_Reservas();
				if (area.getEstadoNegociacion())
				{
					System.out.println("Ya hay negociaci�n en area: " + area.getLocalName());
					area.getBufferNegociaciones().add(msg);
					System.out.println("Apilo la reserva para " + area.getLocalName() + " del vehiculo " + msg.getSender().getLocalName() + " La cola es: " + area.getBufferNegociaciones().size());
				}
				else
				{
					area.setEstadoNegociacion(true);
					area.addBehaviour(new RecepcionReservasBehaviour(area, msg));
				}
			}
			else if (msg.getConversationId().equals(TipoMensaje.LOCALIZAR_AREAS) ) {
				area.addBehaviour(new EnviarPosicionBehaviour(area.getPosicion(), msg));
			}
			else if (msg.getConversationId().equals(TipoMensaje.ELIMINACION_AREA)) {
				System.out.println("Soy el �rea: " + area.getLocalName() + " Recibo " + msg.getContent() + "!!!!!!!!!!!!!!!!");
				area.addBehaviour(new EliminacionAreaBehaviour(area));
			}
			else if (msg.getConversationId().equals(TipoMensaje.ELIMINAR_RESERVAS)) {
				area.addBehaviour(new EliminacionReservasAreaBehaviour());
			}
			else if (msg.getConversationId().equals(TipoMensaje.REGISTRAR_VEHICULO_ILEGAL)) {
				area.addBehaviour(new RegistrarIlegalAreaBehaviour(msg.getSender().getLocalName()));
			}
		}
		else if (area.getBufferNegociaciones().size() > 0)
		{
			if (!area.getEstadoNegociacion())
			{
				area.setEstadoNegociacion(true);
				msgtmp = area.getBufferNegociaciones().get(0);
				area.getBufferNegociaciones().remove(0);
				System.out.println("Se DESapila AREA: " + area.getLocalName() + " VEHICULO: " + msgtmp.getSender().getLocalName() + " QUEDAN: " + area.getBufferNegociaciones().size());
				area.addBehaviour(new RecepcionReservasBehaviour(area, msgtmp));
			}
		}
		else		
			block();
	}

}
