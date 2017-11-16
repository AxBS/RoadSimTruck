package behaviours;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.ACLMessage;
import agents.AreaAgent;
import jade.lang.acl.StringACLCodec;

/**
 * Este comportamiento elimina un areaAgent y lanza a los camiones para que lleguen a
 * otra areaAgent, es decir se reposicionan
 *
 */
public class EliminacionAreaBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = 3793363950076294714L;
	private AreaAgent areaAgent;
	
	
	public EliminacionAreaBehaviour(AreaAgent a){
		
		areaAgent = a;
	}

	@Override
	public void action() {
		
		//limpiarChartOne();
		
		//reiniciarAreas();

		try {
			reubicarVehiculos();
		} catch (ACLCodec.CodecException e) {
			System.out.println("Fallo en la reubicación de vehiculos");
		}

		mostrarPanelParaReactivar();
		
		//Elimino el areaAgent
		myAgent.doDelete();
		
	}

	/**
	 * TODO: No nos hace falta este método porque no tenemos ningún gráfico que limpiar
	 * */
	/*private void limpiarChartOne(){
		
		System.out.println("Voy a limpiar el gráfico 1");
		
		//Creo el mensaje y lo envio al GUI para actualizar el grafico
		ACLMessage messageGraf = new ACLMessage(ACLMessage.INFORM);
		messageGraf.setOntology("deleteChartOne");
		//messageGraf.setConversationId(TipoMensaje.ENVIAR_OCUPACION);
		//messageGraf.setReplyWith(TipoMensaje.ENVIAR_OCUPACION + "-" + myAgent.getLocalName());
		messageGraf.setContent("");
						
		//Sets the agent to send by name
		messageGraf.addReceiver( new AID( "GUI", AID.ISLOCALNAME) );

		//Actually send it
		myAgent.send(messageGraf);
	}*/

	/**
	 * TODO: No veo en que nos puede ser útil este método
	 * */
	/*private void reiniciarAreas(){
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
	}*/
	
	private void reubicarVehiculos() throws ACLCodec.CodecException {
		ArrayList<AID> vehicles = new ArrayList<AID>();

		//TODO:Coger todos los coches que estén dentro del parking, prereservados o reservados
		// hacer que recalculen su destino y si no tienen kilometraje que se queden ahí
		for(String s: this.areaAgent.getLstPreReservas()){
			StringACLCodec codec = new StringACLCodec(new StringReader(s), null);
			AID aid_rec = codec.decodeAID();
			vehicles.add(aid_rec);
		}

		if (vehicles == null) {			
			System.out.println("ATENCION!!!!!! NO HAY AGENTES. Estoy en APIRReactivationAreaBehaviour");
		}
		else
		{
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			//TODO: NO tengo claro si son las reservas o las prereservas
			msg.setOntology("removePrereservesOntology");
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
			System.out.println("!!!!!! vehicles es: " + vehicles.size() + "vehiculos");
			
		}
		
	}

	/**
	 * Muestra el panel para poder reactivar el area deshabilitada y que los coches puedan volver a considerarla
	 * */
	private void mostrarPanelParaReactivar(){
	    /*// Mandamos el mensaje al GUI para mostrar el mensaje de ocupaci�n
		
		//Prepare the message
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);

		//**************************************************
		// A�adimos la info del agente Area que ha invocado
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
		// 0.Nombre del �rea,
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
		System.out.println("====>>>>ENVIO DE INFO AL server: " + message.getReplyWith());*/

	}
}
