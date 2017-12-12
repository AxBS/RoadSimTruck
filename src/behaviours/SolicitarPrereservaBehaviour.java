package behaviours;

import java.util.Iterator;
import environment.Area;
import agents.TruckAgent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SolicitarPrereservaBehaviour extends Behaviour{

	private static final long serialVersionUID = 1L;
	private int step = 0;
	private TruckAgent vehicle;
	private MessageTemplate mt;
	private String areaCancelada; //Area de la que se la ha cancelado la reserva. La primera vez recibe cadena vac�a
	private String areaAReservar; //Pr�xima �rea a la que le solicitar� una reserva.
		
	public SolicitarPrereservaBehaviour(TruckAgent v, String aCancelada) {
		vehicle = v;
		areaCancelada = aCancelada;
	}
	
	@Override
	public void action() {
		

		switch (step)
		{
			case 0:
				PreReservarArea();
				break;
			case 1:
				PreReservaConfirmada();
				
				break;
			default:
				break;
		}
	}

	private void PreReservaConfirmada() {
		ACLMessage msg = vehicle.receive(mt);
		
		if (msg != null)
		{				
			if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) 
			{		
				vehicle.setDesignatedAreaFromString(msg.getSender().getLocalName());
				vehicle.recalculate(vehicle.getCurrentSegment().getOrigin().getId(), vehicle.getDesignatedArea().getIntersection().getId());
				System.out.println("El veh�culo " + vehicle.getLocalName() + " tiene la PREreserva confirmada en " + msg.getSender().getLocalName());
				
			}
			else
			{
				//vehicle.delAreaAsignada();
				System.out.println("El veh�culo " + vehicle.getLocalName() + " se le deniega la PREreserva en " + msg.getSender().getLocalName());
			}
			step = 10;
		}
		else
			block();
	}

	private void PreReservarArea() {
		
		if (vehicle.getFavouriteAreas().isEmpty())
		{
			System.out.println("Vehiculo " + vehicle.getAID().getLocalName() + " no tiene lista de �reas preferidas");
			//vehicle.delAreaIlegal();
			step = 10;
		}
		else
		{
			if (AsignarAreaAReservar()) // Si tengo �rea para reservar reservo
			{
				System.out.println("El vehiculo " + vehicle.getLocalName() + " solicita prereserva en: " + areaAReservar);
						
				ACLMessage msg = new ACLMessage(ACLMessage.CFP);
				msg.addReceiver(new AID(areaAReservar, false));
				msg.setOntology("prereserveOntology");
				msg.setReplyWith("prereserveOntology" + "-" + vehicle.getLocalName() + "-" + areaAReservar + "-" + System.currentTimeMillis());
				//vehicle.inc_Mensajes_sol();
				vehicle.send(msg);
		
				mt = MessageTemplate.and(MessageTemplate.MatchOntology("prereserveOntology"), MessageTemplate.MatchReplyWith(msg.getReplyWith()));
				step = 1;
			}
			else // La pongo ilegalmente en su primera preferencia.
			{
				Area areaPref;
				
				Iterator<Area> itLst = vehicle.getFavouriteAreas().iterator();
				areaPref = itLst.next();
				//Designamos el area al que ir
				vehicle.setDesignatedArea(areaPref);
				//Cambiamos el path por el nuevo destino
				vehicle.recalculate(vehicle.getCurrentSegment().getOrigin().getId(), areaPref.getIntersection().getId());
				
				System.out.println("ESTOY EN SolicitarReserva. Envio a: " + areaPref + " preregistro ilegal de: " + vehicle.getLocalName());
				avisarAreaDeIlegal(areaPref.getId());
				
				step = 10;
			}
		}
				
	}
	
	/**
	 * Devuelve en area_A_Reservar la nueva area a la que se le solicitar� una reserva
	 * Comprueba que el �rea a reservar siga viva
	 *
	 * @return true si quedan �reas en su lista de preferidas
	 * @return false si no quedan m�s �reas en su lista de preferidas
	 */
	private boolean AsignarAreaAReservar(){
		boolean quedan = false;
		
		Iterator<Area> it = vehicle.getFavouriteAreas().iterator();
		
		if (vehicle.getFavouriteAreas().isEmpty())
		{
			System.out.println("Vehiculo " + vehicle.getAID().getLocalName() + " no tiene lista de �reas preferidas");
			//vehicle.delAreaIlegal();
			quedan = false;
		}
		else if (it.hasNext())
		{
			// Obtener area siguiente a la que responde la cancelacion
			String clave;
			
			clave = it.next().getId();
			
			//Si area cancelada es  "-" cogemos la primera de la lista de preferencias
			//Si no cogemos la siguiente al �rea cancelada
			if (!areaCancelada.equals("-"))
			{ 
				while (!clave.equals(areaCancelada) && it.hasNext())
					clave = it.next().getId();
				
				if (it.hasNext()) //voy a solicitar en la siguiente a la cancelada
				{
					clave = it.next().getId();
					quedan = true;
				}
			}
			else //voy a solicitar en la primera
			{
				quedan = true;
			}
			areaAReservar = clave;
			
		}
		
		return quedan;
	}
	
	private void avisarAreaDeIlegal(String areaP){
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		message.setContent("");
		message.setOntology("illegalParkingOntology");
		//message.setReplyWith(TipoMensaje.REGISTRAR_VEHICULO_ILEGAL + "-" + vehicle.getLocalName());
	
		//Sets the agent to send by name
		message.addReceiver( new AID(areaP, AID.ISLOCALNAME) );

		//Actually send it
		vehicle.send(message);

		System.out.println("====>>>>ENVIO AL AREA: " + areaP + "REGISTRO ILEGAL: " + vehicle.getLocalName());
		
		//Actualizo log 2 con la informaci�n de la ubicaci�n ilegal del veh�culo
		
		String texto = "Vehicle: " + vehicle.getLocalName() + " => " + areaP + " illegal";
		
	}
	
	
	@Override
	public boolean done() {
		return step == 10;
	}
	
}
