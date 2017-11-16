package behavioursAreas;

import jade.core.behaviours.OneShotBehaviour;
import agents.AreaAgent;

/**
 * This behaviour sends a Marker, Route and Polyline to the API.
 * This is for demo purposes only.
 *
 */
public class EliminacionReservasAreaBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = 3793363950076294714L;
	
	public EliminacionReservasAreaBehaviour(){
		
	}

	@Override
	public void action() {
		
		((AreaAgent) myAgent).deleteBufferNegociaciones();
		((AreaAgent) myAgent).deleteLstPrereservas();
		((AreaAgent) myAgent).deleteLstReservas();
		((AreaAgent) myAgent).deleteLstSinReservas();
		((AreaAgent) myAgent).deleteParking();
		((AreaAgent) myAgent).setEstadoNegociacion(false);
	}
		
}
