package behavioursAreas;

import java.io.IOException;
import model.Marker;
import others.TipoMensaje;
import others.Util;
import others.teTiposAgentes;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import agents.AreaAgent;

/**
 * This behaviour sends a Marker, Route and Polyline to the API.
 * This is for demo purposes only.
 *
 */
public class EliminacionAreaBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = 3793363950076294714L;
	private AreaAgent area;
	
	
	public EliminacionAreaBehaviour(AreaAgent a){
		
		area = a;
	}

	@Override
	public void action() {
		
		limpiarChartOne();
		
		reiniciarAreas();
		
		reubicarVehiculos();
		
		mostrarPanelParaReactivar();
		
		//Elimino el area
		myAgent.doDelete();
		
	}
	
	private void limpiarChartOne(){
		
		System.out.println("Voy a limpiar el gráfico 1");
		
		//Creo el mensaje y lo envío al GUI para actualizar el gráfico
		ACLMessage messageGraf = new ACLMessage(ACLMessage.INFORM);
		messageGraf.setOntology("deleteChartOne");
		messageGraf.setConversationId(TipoMensaje.ENVIAR_OCUPACION);
		messageGraf.setReplyWith(TipoMensaje.ENVIAR_OCUPACION + "-" + myAgent.getLocalName());
		messageGraf.setContent("");
						
		//Sets the agent to send by name
		messageGraf.addReceiver( new AID( "GUI", AID.ISLOCALNAME) );

		//Actually send it
		myAgent.send(messageGraf);
	}					
	
	private void reiniciarAreas(){
		AID[] areas;
		
		areas = Util.LocalizarAgentes(myAgent, teTiposAgentes.Areas);
		if (areas == null) {			
			System.out.println("ATENCION!!!!!! NO HAY AGENTES. Estoy en APIRReactivationAreaBehaviour");
		}
		else
		{
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			msg.setConversationId(TipoMensaje.ELIMINAR_RESERVAS);
			int i = 0;
			for (AID a : areas) 			
			{
				msg.clearAllReceiver();
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				msg.addReceiver(a);
				myAgent.send(msg);
				i++;
			}
			System.out.println("!!!!!! Acabo de limpiar: " + i + "areas");
		}
	}
	
	private void reubicarVehiculos(){
		AID[] vehicles;
		
		vehicles = Util.LocalizarAgentes(myAgent, teTiposAgentes.Vehiculos);
		if (vehicles == null) {			
			System.out.println("ATENCION!!!!!! NO HAY AGENTES. Estoy en APIRReactivationAreaBehaviour");
		}
		else
		{
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			msg.setConversationId(TipoMensaje.ELIMINAR_RESERVAS);
			msg.setPerformative(ACLMessage.CANCEL);
			int i = 0;
			for (AID v : vehicles) 	
			{
				i++;
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				msg.clearAllReceiver();
				msg.addReceiver(v);
				myAgent.send(msg);
			}
			System.out.println("!!!!!! Acabo de limpiar: " + i + "vehiculos");
			System.out.println("!!!!!! vehicles es: " + vehicles.length + "vehiculos");
			
		}
		
	}
		
	private void mostrarPanelParaReactivar(){
	    // Mandamos el mensaje al GUI para mostrar el mensaje de ocupación
		
		//Prepare the message
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);

		//**************************************************
		// Añadimos la info del agente Area que ha invocado
		// el comportamiento
		//**************************************************
		
		//The Ontology is used to choose the function.
		message.setOntology("reactivateMarker");

		Marker marker = new Marker();
		marker.id = myAgent.getLocalName();

		marker.coordinates = new double[]{((AreaAgent)myAgent).getCoordX(), ((AreaAgent)myAgent).getCoordY()};
		marker.name = ((AreaAgent)myAgent).getDescrip();
		marker.icon = "https://irtic.uv.es/~jfgarcia/icon/Truck_Park_orange.png";

		
		//Pasamos la informacion del panel en marker.informacion
		//con cadenas, usando el separador -
		// 0.Nombre del área,
		// 1. Imagen izquierda
		// 2. Imagen derecha
		// 3. Numero de plazas libres
		// 4. Numero de plazas libres (En information)
		// 5. Numero de plazas ocupadas
		// 6. Numero de ilegales
		
		int libres = 0;
		
		marker.information = ((AreaAgent)myAgent).getDescrip();
		marker.information += "-" + "https://robotica.uv.es/~jfgarcia/icon/logo_aspa.png";
		marker.information += "-" + "https://robotica.uv.es/~jfgarcia/icon/logo_aspa.png";
		marker.information += "-" + "OUT OF ORDER";
		marker.information += "-" + libres;
		marker.information += "-" + libres;
		marker.information += "-" + libres;
				
		//Set the object to send
		try {
			message.setContentObject(marker);
		} catch (IOException e) {

			System.out.println("Error trying to serialize the object.");
			e.printStackTrace();
		}

		message.setConversationId(TipoMensaje.ENVIAR_OCUPACION);
		message.setReplyWith(TipoMensaje.ENVIAR_OCUPACION + "-" + myAgent.getLocalName());  
		
		//Sets the agent to send by name
		message.addReceiver( new AID( "GUI", AID.ISLOCALNAME) );

		//Actually send it
		myAgent.send(message);
		System.out.println("====>>>>ENVIO DE INFO AL server: " + message.getReplyWith());

	}
}
