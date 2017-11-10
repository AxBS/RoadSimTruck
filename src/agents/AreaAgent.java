package agents;

import java.util.ArrayList;

import org.json.JSONObject;

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
	
	//Location of the Area
	private float locationX, locationY;
	private float locationPK;
	private String roadCode;
	
	private ArrayList<String> lstReservas;				// Guarda el nombre del vehiculo que reserva
	private ArrayList<String> lstSinReservas;			// Guarda el nombre del vehículo que aparca sin reserva
	private ArrayList<String> parking;					// Guarda el nombre del vehiculo que ocupa una plaza
	private DFAgentDescription interfaceAgent;
	
	//Capacity
	private int freeSpaces;
	private int totalSpaces;
	
	//Area Info
	private String id;
	private boolean drawGUI;

	
	//Initial Tick
	private long tini;
	private int ratio;
	
	
	
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
		
		//An unique identifier for the Area
		this.id = getName().toString();
		
		//Area location
		this.locationX = Integer.parseInt((String) this.getArguments()[1]);
		this.locationY = Integer.parseInt((String) this.getArguments()[2]);
		
		//Total capacity of the AREA
		this.totalSpaces = Integer.parseInt((String) this.getArguments()[3]);
		this.freeSpaces = totalSpaces;
		
		//drawGUI
		this.drawGUI = (boolean) this.getArguments()[4];
		
		//Get the initial time tick from eventManager
		this.tini = (long) this.getArguments()[5];
		this.ratio = (int) this.getArguments()[6];
		

		
		
		
		
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
		
		//An unique identifier for the car
		this.id = getName().toString();
		
		
		
		if(this.drawGUI){
			//We notify the interface (send msg to InterfaceAgent) about the new area
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(interfaceAgent.getName());
			JSONObject areaData = new JSONObject();
			areaData.put("x", this.locationX);
			areaData.put("y", this.locationY);
			areaData.put("id", this.id);
			areaData.put("capacity", this.totalSpaces);
			msg.setContent(areaData.toString());
			
			//TODO change to newTruckOntology
			msg.setOntology("newAreaOntology");
			send(msg);
		}
		
	}
	
	
	
	
	
	
	

}
