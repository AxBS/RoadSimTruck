package behavioursAreas;

import java.io.IOException;
import others.TipoMensaje;
import model.Marker;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import agents.AreaAgent;

/**
 * This behaviour sends a Marker, Route and Polyline to the API.
 * This is for demo purposes only.
 *
 */
public class EnviarOcupacionBehaviour extends TickerBehaviour{

	private static final long serialVersionUID = -2826285439534419110L;

	public EnviarOcupacionBehaviour(AreaAgent a, long t){
		super(a, t);

	}

	protected void onTick() {
		 		
		// Envio el estado de ocupacion
		EnviarOcupacion();
		EnviarInfoLog();
	}
	
	
	private void EnviarOcupacion() {

		//Prepare the message
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);

		//**************************************************
		// Añadimos la info del agente Area que ha invocado
		// el comportamiento
		//**************************************************
		
		//The Ontology is used to choose the function.
		message.setOntology("drawMarker");

		Marker marker = new Marker();
		marker.id = myAgent.getLocalName();

		marker.coordinates = new double[]{((AreaAgent)myAgent).getCoordX(), ((AreaAgent)myAgent).getCoordY()};
		marker.name = ((AreaAgent)myAgent).getDescrip();
		marker.icon = "https://irtic.uv.es/~jfgarcia/icon/Truck_Park.png";

		
		//Pasamos la informacion del panel en marker.informacion
		//con cadenas, usando el separador -
		// 0.Nombre del área,
		// 1. Imagen izquierda
		// 2. Imagen derecha
		// 3. Numero de plazas libres
		// 4. Numero de plazas libres (En information)
		// 5. Numero de plazas ocupadas
		// 6. Numero de ilegales
		
		int libres = (((AreaAgent)myAgent).getMaxParking() - ((AreaAgent)myAgent).getLstReservas().size());
		
		marker.information = ((AreaAgent)myAgent).getDescrip();
		marker.information += "-" + "https://robotica.uv.es/~jfgarcia/icon/logo_info.png";
		
		if (libres == 0)
		{
			marker.information += "-" + "https://robotica.uv.es/~jfgarcia/icon/logo_aspa.png";
			marker.information += "-" + "NO";
			marker.icon = "https://irtic.uv.es/~jfgarcia/icon/Truck_Park_full.png";
		}
		else
		{
			marker.information += "-" + "https://robotica.uv.es/~jfgarcia/icon/logo_flecha.png";
			marker.information += "-" + libres;
		}
		marker.information += "-" + ((AreaAgent)myAgent).getLstReservas().size();
		marker.information += "-" + libres;
		marker.information += "-" + ((AreaAgent)myAgent).getLstSinReservas().size();
				
		//Set the object to send
		try {
			message.setContentObject(marker);
		} catch (IOException e) {

			System.out.println("Error trying to serialize the object.");
			e.printStackTrace();
		}

		message.setConversationId(TipoMensaje.ENVIAR_OCUPACION);
		message.setReplyWith(TipoMensaje.ENVIAR_OCUPACION + "-" + myAgent.getLocalName());  // identificador de subasta
		
		//Sets the agent to send by name
		message.addReceiver( new AID( "GUI", AID.ISLOCALNAME) );

		//Actually send it
		myAgent.send(message);
		System.out.println("====>>>>ENVIO DE INFO AL server: " + message.getReplyWith());
		
	}

	private void EnviarInfoLog() {

		System.out.println("Sending info al log");

		//Prepare the message
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);

		//**************************************************
		// Añadimos la info del agente Area que ha invocado
		// el comportamiento
		//**************************************************
		
		//The Ontology is used to choose the function.
		message.setOntology("addStringToLog1");
		message.setContent("Area: " + myAgent.getLocalName() + " Occupied: " + ((AreaAgent)myAgent).getLstReservas().size() + " Free: " + (((AreaAgent)myAgent).getMaxParking() - ((AreaAgent)myAgent).getLstReservas().size()) + " Illegal: " + ((AreaAgent)myAgent).getLstSinReservas().size());
		
		message.setConversationId(TipoMensaje.ENVIAR_OCUPACION);
		message.setReplyWith(TipoMensaje.ENVIAR_OCUPACION + "-" + myAgent.getLocalName()); 
		
		//Sets the agent to send by name
		message.addReceiver( new AID( "GUI", AID.ISLOCALNAME) );

		//Actually send it
		myAgent.send(message);

		System.out.println("====>>>>ENVIO DE INFO DE LOG AL server: " + message.getReplyWith() + "Contenido: " + message.getContent());
		
	}

}
