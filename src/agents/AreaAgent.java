package agents;

import java.util.ArrayList;

import behaviours.AreaBehaviour;
import org.json.JSONObject;

import environment.Area;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

/**
 * This code represents a Resting Area.
 */
public class AreaAgent extends Agent{
	
	
	private Area area;
	
	private final int reservationRadious = 30;
	
	
	private ArrayList<String> lstReservas;				// Guarda el nombre del vehiculo que reserva
	private ArrayList<String> lstPreReservas;			// Guarda el nombre de los vehiculos pre-reservados
	private ArrayList<String> lstSinReservas;			// Guarda el nombre del vehículo que aparca sin reserva
	private ArrayList<String> parking;					// Guarda el nombre del vehiculo que ocupa una plaza
	private DFAgentDescription interfaceAgent;
	
	private boolean drawGUI;

	protected void setup(){
		
		//Register the agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("areaAgent");
		sd.setName(getLocalName());

		dfd.addServices(sd);
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) {
			
			//Sometimes an agent cannot find the DF in time
			//I still don't know when this happens so I will
			//simply kill it for now.
			this.takeDown();
		}
		
		this.area =  (Area) this.getArguments()[0];
		this.area.setAreaAgent(this);
		this.drawGUI = (boolean) this.getArguments()[1];

		
		//Initializing values 
		
		this.lstPreReservas = new ArrayList<String>();
		this.lstReservas = new ArrayList<String>();
		this.parking = new ArrayList<String>();
		this.lstSinReservas = new ArrayList<String>();

		if(this.drawGUI){
			//Find the interface agent
			dfd = new DFAgentDescription();
			sd = new ServiceDescription();
			sd.setType("interfaceAgent");
			dfd.addServices(sd);

			DFAgentDescription[] result = null;

			try {
				result = DFService.searchUntilFound(
						this, getDefaultDF(), dfd, null, 5000);
			} catch (FIPAException e) { e.printStackTrace(); }

			while (result == null || result[0] == null) {
				
				try {
					result = DFService.searchUntilFound(
							this, getDefaultDF(), dfd, null, 5000);
				} catch (FIPAException e) { e.printStackTrace(); }
			}
			
			this.interfaceAgent = result[0];
		}
		
		addBehaviour(new AreaBehaviour(this));
		
	}

	public boolean doPrereserve(String truckName){
		// Si hay sitio en el area que preserve sino nada
		return true;
	}

	public boolean doReserve(String truckName){
		// Hacer la reserva si se puede
		return true;
	}

	public void doUnreserve(String truckName){
		//Realizar la dereserva
	}

	public void doIllegalParking(String truckName){
		// Realizar la reserva ilegal
	}

}
