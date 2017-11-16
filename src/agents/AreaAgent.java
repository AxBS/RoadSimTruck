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
	private ArrayList<String> lstIlegales;			// Guarda el nombre del vehículo que aparca sin reserva
	private ArrayList<String> parking;					// Guarda el nombre del vehiculo que ocupa una plaza
	private ArrayList<ACLMessage> bufferNegociaciones; //Buffer con las negociaciones no acabadas
	private DFAgentDescription interfaceAgent;
	
	private boolean drawGUI;

	// Como pueden haber varias resefvas a la vez hemos de
	// tener cuidado con la distribución de las reservas
	private boolean estadoNegociacion = false;
	private int numTotalNegociaciones;

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
		this.lstIlegales = new ArrayList<String>();
		this.numTotalNegociaciones = 0;
		this.bufferNegociaciones = new ArrayList<ACLMessage>();
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
		this.lstIlegales.add(truckName);
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	public ArrayList<String> getLstReservas() {
		return lstReservas;
	}

	public void setLstReservas(ArrayList<String> lstReservas) {
		this.lstReservas = lstReservas;
	}

	public ArrayList<String> getLstPreReservas() {
		return lstPreReservas;
	}

	public void setLstPreReservas(ArrayList<String> lstPreReservas) {
		this.lstPreReservas = lstPreReservas;
	}

	public ArrayList<String> getLstIlegales() {
		return lstIlegales;
	}

	public void setLstIlegales(ArrayList<String> lstIlegales) {
		this.lstIlegales = lstIlegales;
	}

	public ArrayList<String> getParking() {
		return parking;
	}

	public void setParking(ArrayList<String> parking) {
		this.parking = parking;
	}

	public boolean isDrawGUI() {
		return drawGUI;
	}

	public void setDrawGUI(boolean drawGUI) {
		this.drawGUI = drawGUI;
	}

	public boolean isNegociando() {
		return estadoNegociacion;
	}

	public void setEstadoNegociacion(boolean estadoNegociacion) {
		this.estadoNegociacion = estadoNegociacion;
	}

	public ArrayList<ACLMessage> getBufferNegociaciones() {
		return bufferNegociaciones;
	}

	public void setBufferNegociaciones(ArrayList<ACLMessage> bufferNegociaciones) {
		this.bufferNegociaciones = bufferNegociaciones;
	}

	public int getNumTotalNegociaciones() {
		return numTotalNegociaciones;
	}

	public void setNumTotalNegociaciones(int numTotalNegociaciones) {
		this.numTotalNegociaciones = numTotalNegociaciones;
	}

	public void addNegociacion(ACLMessage msg){
		this.bufferNegociaciones.add(msg);
	}

	public void incNumeroTotalNegociaciones(){
		this.numTotalNegociaciones++;
	}

	public void deleteLstPrereservas(){
		this.lstPreReservas.clear();
	}
	public void deleteBufferNegociaciones(){
		this.bufferNegociaciones.clear();
	}
	public void deleteLstReservas(){
		this.lstReservas.clear();
	}
	public void deleteLstSinReservas(){
		this.lstIlegales.clear();
	}
	public void deleteParking(){
		this.parking.clear();
	}
}
