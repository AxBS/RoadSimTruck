package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * This code represents a Resting Area.
 */
public class AreaAgent extends Agent{
	
	//Location of the Area
	private float locationX, locationY;
	private float locationPK;
	private String roadCode;
	
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
		

		
	}
	
	
	
	
	
	
	

}
