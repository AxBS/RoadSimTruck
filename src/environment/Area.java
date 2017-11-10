package environment;

import agents.AreaAgent;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Area {

	//Location of the Area
	private int locationX, locationY;
	private float locationPK;
	private Segment locationSegment;
	
	//Capacity
	private int capacity;
	
	//Area Info
	private String id;
	
	//Variable to draw the GUI
	private boolean drawGUI;
	
	//The container where the agents will be created
	@SuppressWarnings("unused")
	private transient jade.wrapper.AgentContainer areaContainer;
	
	//Area agent representing the area
	private AreaAgent areaAgent;
	

	public Area(int locationX, int locationY, float locationPK, Segment locationSegment, int capacity, String id, jade.wrapper.AgentContainer areaContainer ) {
		
		this.locationX = locationX;
		this.locationY = locationY;
		this.locationPK = locationPK;
		this.locationSegment = locationSegment;
		this.capacity = capacity;
		this.id = id;
		this.areaContainer = areaContainer;
		
		
		//Create the agents
				try {

					//Agent Controller to segments with Interface
					AgentController agent = areaContainer.createNewAgent(
							this.id, "agents.AreaAgent", new Object[]{this, this.drawGUI});

					agent.start();
					
				} catch (StaleProxyException e) {

					System.out.println("Error starting " + this.id);
					e.printStackTrace();
				}
		
	}


	public Area() {
		
		this.locationX = 0;
		this.locationY = 0;
		this.locationPK = 0.0f;
		this.locationSegment = new Segment();
		this.capacity = 0;
		this.id = "defaultID";
	}

	public int getLocationX() {
		return locationX;
	}

	public void setLocationX(int locationX) {
		this.locationX = locationX;
	}

	public int getLocationY() {
		return locationY;
	}

	public void setLocationY(int locationY) {
		this.locationY = locationY;
	}

	public float getLocationPK() {
		return locationPK;
	}

	public void setLocationPK(float locationPK) {
		this.locationPK = locationPK;
	}

	public Segment getLocationSegment() {
		return locationSegment;
	}

	public void setLocationSegment(Segment locationSegment) {
		this.locationSegment = locationSegment;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	
	public boolean isDrawGUI() {
		return drawGUI;
	}

	public void setDrawGUI(boolean drawGUI) {
		this.drawGUI = drawGUI;
	}

	public AreaAgent getAreaAgent() {
		return areaAgent;
	}

	public void setAreaAgent(AreaAgent areaAgent) {
		this.areaAgent = areaAgent;
	}
	
	@Override
	public String toString() {
		return "Area [locationX=" + locationX + ", locationY=" + locationY + ", locationPK=" + locationPK
				+ ", locationSegment=" + locationSegment + ", capacity=" + capacity + ", id=" + id + "]";
	}
	
	

	
	
	
}
