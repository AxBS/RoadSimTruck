package environment;

import agents.AreaAgent;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Area {

	//Location of the Area
//	private int locationX, locationY;
//	private float locationPK;
//	private Segment locationSegment;
	
	private Intersection intersection;
	
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
	

	public Area(Intersection intersection, int capacity, String id, jade.wrapper.AgentContainer areaContainer ) {
		
		this.intersection =intersection;
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
		
		this.intersection = new Intersection();
		this.capacity = 0;
		this.id = "defaultID";
	}


	
	
	
	public Intersection getIntersection() {
		return intersection;
	}


	public void setIntersection(Intersection intersection) {
		this.intersection = intersection;
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
		return "Area [locationX=" + intersection.getX()+ ", locationY=" + intersection.getY() + ", capacity=" + capacity + ", id=" + id + "]";
	}
	
	

	
	
	
}
