package behaviours;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import agents.AreaAgent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class NegociacionAreaBehaviour extends Behaviour{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<String> lstVehiculos;
	private ArrayList<String> lstAreasAlcanzables;
	private String idNegociacion;
	private MessageTemplate mt;
	private AreaAgent area;
	private int step = 0;
	
	// Preferencias guardadas como map <vehiculo, lstPreferencias>
	private HashMap<String, ArrayList<String>> preferencias;	
	private HashMap<String,Integer> tiemposConduccion;
	private HashMap<String,Integer> posicionesVehiculos;
	private List<String> solucionGanadora = new ArrayList<String>();
	
	private long t_ini, t_fin; // Para calcular los tiempos de negociacion
	
	public NegociacionAreaBehaviour(AreaAgent a, String id, String vehicle) {
		super();
		area = a;
		idNegociacion = id;
		
		lstVehiculos = new ArrayList<String>(area.getLstReservas());
		lstVehiculos.add(vehicle);
		preferencias = new HashMap<String, ArrayList<String>>();
		tiemposConduccion = new HashMap<String, Integer>();
		posicionesVehiculos = new HashMap<String, Integer>();
		lstAreasAlcanzables = new ArrayList<String>();
		t_ini = System.currentTimeMillis();
	}

	@Override
	public void action() {
		switch (step) {
		case 0:
			solicitarPreferenciasVehiculos();	
			step = 1;
			break;
		case 1:
			obtenerPreferenciasVehiculos();
			// Hemos recibido ya todas las preferencias de los vehiculos
			if (preferencias.size() == lstVehiculos.size()) 
				step = 2;
			break;
		case 2:
			CalcularSoluciones();
			System.out.println("Calcular Ganador de negociacion id " + idNegociacion);
			step = 3;
			break;
		case 3:
			if (solucionGanadora != null && !solucionGanadora.isEmpty())
			{
				EnviarNuevasReservas();
			}
			else // YA NO ENTRARA NUNCA
			{					
				System.out.println("NEGOCIACION: " + idNegociacion + " SIN SOLUCION");
				System.out.println("Veh�culo " + lstVehiculos.get(lstVehiculos.size()-1) + " aparca sin reserva en Area " + area.getLocalName());
			}						
			step = 10;
			break;
		default: break;
		}
		
	}
	
	//Modificado para modificar reservas seg�n la negociaci�n
	//Al resto de veh�culos se les cancela la reserva para que negocien
	//Con la siguiente area
	private void EnviarNuevasReservas() {
		HashMap<String, ArrayList<String>> soluciones = new HashMap<String, ArrayList<String>>();
		for (int i = 0; i < solucionGanadora.size(); i++) {
			String a = solucionGanadora.get(i);
			String v = lstVehiculos.get(i);
			
			if (solucionGanadora.size() != lstVehiculos.size())
			{
				System.err.println("====>> ATENCION!!!! la lista de la solucion no coincide con la de los veh�culos");
				System.err.println("Lista de la solucion: " + solucionGanadora.size());
				System.err.println("Lista de vehiculos  : " + lstVehiculos.size());
			}
				
			if (a.compareTo(area.getLocalName()) == 0)
			{			
				if (soluciones.containsKey(a)) {
					ArrayList<String> lst = soluciones.get(a);
					lst.add(v);				
					soluciones.put(a, lst);				
				}
				else
				{
					ArrayList<String> lst = new ArrayList<String>();
					lst.add(v);					
					soluciones.put(a, lst);
				}
			}
		}
			
		
		area.delLstReservas();
		// A�ado al area todos los vehiculos en la lista de soluciones
		for (String v : soluciones.get(area.getLocalName())) {
			if (!area.getLstReservas().contains(v.toString().trim()))
				area.getLstReservas().add(v.toString().trim());						
		}
		System.out.println(area.getAID().getLocalName() + " REASIGNACION POR NEGOCIACION. Ahora estan: " + area.getLstReservas() );
		
		
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
		msg.setConversationId(TipoMensaje.NEGOCIACION_ASIGNACION_RESERVAS);
		msg.setReplyWith(idNegociacion);
		msg.setContent("");
			
		// Enviamos un mensaje al vehiculo al que se le cancela la reserva
		// Tambien al que se le acepta la reserva, si es el �ltimo (el que lo solicit�)
		
		for (int i = 0; i < lstVehiculos.size(); i++) {
			msg.clearAllReceiver();
			if (solucionGanadora.size()>0) //TODO: numca deberia de ser 0
			{
				String a = solucionGanadora.get(i);
				String v = lstVehiculos.get(i);
				if (a.compareTo(area.getLocalName()) == 0)
				{
					if (i == lstVehiculos.size()-1)
					{
						System.out.println("LLAMANDO AL VEHICULO: " + v + " para reservar en " + a);
						msg.addReceiver(new AID(v, false));
						msg.setContent(a);
						msg.setPerformative(ACLMessage.CONFIRM);
						area.inc_Respuesta_Reserva();
						area.send(msg);
					}
				}
				else
				{ 
					System.out.println("LLAMANDO AL VEHICULO: " + v + " para cancelar reserva en " + area.getLocalName());
					msg.setPerformative(ACLMessage.DISCONFIRM);
					msg.addReceiver(new AID(v, false));
					msg.setContent(area.getLocalName());
					area.inc_Respuesta_Reserva();
					area.send(msg);	
				}
			}
			else
				System.out.println("------->>>  SOLUCION GANADORA ES 0");
		}
			
	}
	
    private void solicitarPreferenciasVehiculos() {
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
		msg.setConversationId(TipoMensaje.NEGOCIACION_SOLICITAR_PREFERENCIAS);
		msg.setReplyWith(idNegociacion);
		msg.setContent("");
		
		for (String v : lstVehiculos) {
			msg.addReceiver(new AID(v, false));
		}
		((AreaAgent)myAgent).inc_Sol_Preferencias();
		((AreaAgent)myAgent).send(msg);
		
		System.out.println("Enviadas peticiones a " + lstVehiculos.toString() + " ID: " + idNegociacion + " DE: " + ((AreaAgent)myAgent).getLocalName());
		mt = MessageTemplate.and(MessageTemplate.MatchConversationId(TipoMensaje.NEGOCIACION_ENVIAR_PREFERENCIAS),
								MessageTemplate.MatchReplyWith(idNegociacion));
		
	}
	
	private void obtenerPreferenciasVehiculos() {
		ArrayList<String> lstPreferidas;
		
		ACLMessage msg = myAgent.receive(mt); 
		if (msg != null) {
			String[] tmp = msg.getContent().replace("[","").replace("]", "").replace(" ", "").split(",");
			lstPreferidas = new ArrayList<String> (Arrays.asList(tmp));
			//Elimino las areas posteriores a la de la negociacion
			while (!lstPreferidas.isEmpty() && !lstPreferidas.get(0).equals(area.getLocalName()) )
			{
				lstPreferidas.remove(0);
			}
			if (lstPreferidas.isEmpty())
 				System.out.println("=====>>>>>  Vehiculo: " + msg.getSender() + " devuelve la lista de preferidas est� vac�a");
/* 			
			//Ahora me cargo el resto de preferencias y dejo solo el �rea en cuesti�n
			while (lstPreferidas.size() > 1)
			{
				lstPreferidas.remove(1);
			}
*/			
			preferencias.put(msg.getSender().getLocalName().trim(), lstPreferidas);		
			lstAreasAlcanzables = (ArrayList<String>) Util.UnionListas(lstAreasAlcanzables, (Arrays.asList(tmp)));
			posicionesVehiculos.put(msg.getSender().getLocalName().trim(), Integer.parseInt(msg.getEncoding()));
			// El tiempo de conduccion se manda en InReplyTo
			tiemposConduccion.put(msg.getSender().getLocalName().trim(), Integer.parseInt(msg.getInReplyTo()));
		}
		else
			block();
	}
	
	private void CalcularSoluciones() {
		int maxNumPref;
		int numPref;

		List<List<String>> lstSolucionesFactibles = new ArrayList<List<String>>();
		
		
		System.out.println("Se negocia por: " + area.getLocalName());
		System.out.println("Num areas alcanzables " + lstAreasAlcanzables);
		System.out.println("Numero de veh�culos " + lstVehiculos.size());
		System.out.println("Num areas preferidas " + preferencias);
		if (preferencias.size() != lstVehiculos.size())
			System.err.println("====>>>  ATENCION: Lista de preferencias incompleta.");

		System.out.println("=>> Calculo Pesos Votos realizado");
				
		//Compruebo si solo hay un area preferida para todos.
		maxNumPref = 0;
		for (Entry<String, ArrayList<String>> entry: preferencias.entrySet())
		{
			numPref = entry.getValue().size();
			if (numPref > maxNumPref)
				maxNumPref = numPref;
		}
		if (maxNumPref == 1) //Caso especial solo se negocia por un area
		{
		      System.out.println("Solo habia una preferencia " + area.getLocalName());
		      for (int i = 0; i < preferencias.size(); i++)
		      {
		    	  List<String> solucion = new ArrayList<String>();
		    	  for (int j = 0; j < preferencias.size(); j++)
		    	  {
		    		  if (i == j)
		    			  solucion.add("NO");
		    		  else
		    			  solucion.add(area.getLocalName());
		    	  }
		    	  lstSolucionesFactibles.add(solucion);
		      }
		      
		}
		else
		{
			int i = -1;
			for (Entry<String, ArrayList<String>> entry: preferencias.entrySet())
			{
				i++;
				numPref = entry.getValue().size();
				if (numPref > 1)
				{
					List<String> solucion = new ArrayList<String>();
					for (int j = 0; j < preferencias.size(); j++)
			    	  {
			    		  if (i == j)
			    			  solucion.add(entry.getValue().get(1));
			    		  else
			    			  solucion.add(area.getLocalName());
			    	  }
			    	  lstSolucionesFactibles.add(solucion);
				}
					
			}
		}

		System.out.println("N� de solu factibles = " + lstSolucionesFactibles.size());
		System.out.println("Calculando Pesos Votos de las soluciones ... " + lstSolucionesFactibles.size());
		if (!lstSolucionesFactibles.isEmpty()) {
			
			switch (area.getmodoCalculoPref()){
				case 1:
					calcularSolGanadora1(lstSolucionesFactibles);
					break;
				case 2:
					calcularSolGanadora2(lstSolucionesFactibles);
					break;
				case 3:
//					calcularSolGanadora3(lstSolucionesFactibles);
					break;
			}
		}
		System.out.println("Calculado Pesos Votos de las soluciones ... ");
				
	}
	
	private void calcularSolGanadora1(List<List<String>> lstSolucionesFactibles) {
	   	int total;
	   	int maxPuntuacion = 0;
		String v;
		String a;
		FileWriter fichero = null;
		PrintWriter pw = null;
		StringBuilder strAreas = new StringBuilder();
		StringBuilder strPesos = new StringBuilder();
		
		solucionGanadora = null;
		System.out.println("CALCULANDO CON METODO 1 ... ");
		
		strAreas.setLength(0);
		strPesos.setLength(0);
		strAreas.append("Negociaci�n por Area: " + area.getLocalName() + ". Id Negociacion: " + idNegociacion + "\n");
		strPesos.append("Negociaci�n por Area: " + area.getLocalName() + ". Id Negociacion: " + idNegociacion + "\n");
		
		//para mostrar en los los vehiculos implicados
		for (int i = 0; i < lstVehiculos.size(); i++){
			strAreas.append(lstVehiculos.get(i).toString().trim() + "   ");
			strPesos.append(lstVehiculos.get(i).toString().trim() + "   ");
		}
		strAreas.append("\n");
		strPesos.append("\n");		
		
		for (List<String> lst : lstSolucionesFactibles) {
			total = 0;
			for (int i = 0; i < lst.size(); i++) {
				a = lst.get(i).toString().trim();
				v = lstVehiculos.get(i).toString().trim();
				strAreas.append(a + "   ");
				//Si es el caso especial area "NO" pondra peso un "0"
				int valor = (preferencias.get(v).indexOf(a) >= 0) ? lstAreasAlcanzables.size() - preferencias.get(v).indexOf(a) : 0;
				int tc = (tiemposConduccion.get(v) * 100)/area.getTiempoMaxConduccion();
				int calculo = valor*tc;
				strPesos.append(calculo + "   ");
				total += calculo;				
			}
			strAreas.append("\n");
			strPesos.append(" = " + total + "\n");
			if (total >= maxPuntuacion) {
				solucionGanadora = lst;
				maxPuntuacion = total;
			}
		}
		
		//IMPRIMIR EN FICHERO LOG
		if (area.getFicheroLog().equals("") == false){
			try
	        {
	            fichero = new FileWriter(area.getFicheroLog().concat(".log"), true);
	            pw = new PrintWriter(fichero);

	            pw.println(strAreas.toString().replace("\n","\r\n"));
	            pw.println(strPesos.toString().replace("\n","\r\n"));
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	           try {
	           // Nuevamente aprovechamos el finally para 
	           // asegurarnos que se cierra el fichero.
	           if (null != fichero)
	              fichero.close();
	           } catch (Exception e2) {
	              e2.printStackTrace();
	           }
	        }
			
		}
		
	}

	private void calcularSolGanadora2(List<List<String>> lstSolucionesFactibles) {
	   	int total;
	   	int maxPuntuacion = 0;
		String v;
		String a;
		FileWriter fichero = null;
		PrintWriter pw = null;
		StringBuilder strAreas = new StringBuilder();
		StringBuilder strPesos = new StringBuilder();
		
		solucionGanadora = null;
		System.out.println("CALCULANDO CON METODO 2 ... ");
		
		strAreas.setLength(0);
		strPesos.setLength(0);
		strAreas.append("Negociaci�n por Area: " + area.getLocalName() + ". Id Negociacion: " + idNegociacion + "\n");
		strPesos.append("Negociaci�n por Area: " + area.getLocalName() + ". Id Negociacion: " + idNegociacion + "\n");
		
		//para mostrar en los los vehiculos implicados
		for (int i = 0; i < lstVehiculos.size(); i++){
			strAreas.append(lstVehiculos.get(i).toString().trim() + "   ");
			strPesos.append(lstVehiculos.get(i).toString().trim() + "   ");
		}
		strAreas.append("\n");
		strPesos.append("\n");		
		
		for (List<String> lst : lstSolucionesFactibles) {
			total = 0;
			for (int i = 0; i < lst.size(); i++) {
				a = lst.get(i).toString().trim();
				v = lstVehiculos.get(i).toString().trim();
				strAreas.append(a + "   ");
				//Si es el caso especial area "NO" pondra peso un "0"
				int valor = (preferencias.get(v).indexOf(a) >= 0) ? 100 / preferencias.get(v).size() * (preferencias.get(v).size() - preferencias.get(v).indexOf(a)) : 0;
				int tc = (tiemposConduccion.get(v) * 100)/area.getTiempoMaxConduccion();
				int calculo = valor*tc;
				strPesos.append(calculo + "   ");
				total += calculo;				
			}
			strAreas.append("\n");
			strPesos.append(" = " + total + "\n");
			if (total >= maxPuntuacion) {
				solucionGanadora = lst;
				maxPuntuacion = total;
			}
		}
		
		//IMPRIMIR EN FICHERO LOG
		if (area.getFicheroLog().equals("") == false){
			try
	        {
	            fichero = new FileWriter(area.getFicheroLog().concat(".log"), true);
	            pw = new PrintWriter(fichero);

	            pw.println(strAreas.toString().replace("\n","\r\n"));
	            pw.println(strPesos.toString().replace("\n","\r\n"));
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	           try {
	           // Nuevamente aprovechamos el finally para 
	           // asegurarnos que se cierra el fichero.
	           if (null != fichero)
	              fichero.close();
	           } catch (Exception e2) {
	              e2.printStackTrace();
	           }
	        }
			
		}
		
	}

	private void calcularSolGanadora3(List<List<String>> lstSolucionesFactibles) {
	   	int total;
	   	int maxPuntuacion = 0;
		String v;
		String a;
		FileWriter fichero = null;
		PrintWriter pw = null;
		StringBuilder strAreas = new StringBuilder();
		StringBuilder strPesos = new StringBuilder();
		
		solucionGanadora = null; 
		System.out.println("CALCULANDO CON METODO 3 ... ");
		
		strAreas.setLength(0);
		strPesos.setLength(0);
		strAreas.append("Negociaci�n por Area: " + area.getLocalName() + ". Id Negociacion: " + idNegociacion + "\n");
		strPesos.append("Negociaci�n por Area: " + area.getLocalName() + ". Id Negociacion: " + idNegociacion + "\n");
		
		//para mostrar los vehiculos implicados
		for (int i = 0; i < lstVehiculos.size(); i++){
			strAreas.append(lstVehiculos.get(i).toString().trim() + "   ");
			strPesos.append(lstVehiculos.get(i).toString().trim() + "   ");
		}
		strAreas.append("\n");
		strPesos.append("\n");		
		
		for (List<String> lst : lstSolucionesFactibles) {
			total = 0;
			for (int i = 0; i < lst.size(); i++) {
				a = lst.get(i).toString().trim();
				v = lstVehiculos.get(i).toString().trim();
				strAreas.append(a + "   ");
				//Si es el caso especial area "NO" pondra una utlidad "0"
				int calculo = (preferencias.get(v).indexOf(a) >= 0) ? area.getTiempoMaxConduccion()-((tiemposConduccion.get(v) + (area.getPosicion() - posicionesVehiculos.get(v))*60/100)) : 0;
//				System.out.println("Vehiculo: " + v + " Area: " + area.getLocalName() + " T. max Cond: " + area.getTiempoMaxConduccion() + " Pos Area: " + area.getPosicion() + "Tcond veh: " + tiemposConduccion.get(v) + " Pos Veh: " + posicionesVehiculos.get(v) + "Dif. Veh: " + diferencialesTiempo.get(v));
				strPesos.append(calculo + "   ");
				total += calculo;				
			}
			strAreas.append("\n");
			strPesos.append(" = " + total + "\n");
			if (total >= maxPuntuacion) {
				solucionGanadora = lst;
				maxPuntuacion = total;
			}
		}
		
		//IMPRIMIR EN FICHERO LOG
		if (area.getFicheroLog().equals("") == false){
			try
	        {
	            fichero = new FileWriter(area.getFicheroLog().concat(".log"), true);
	            pw = new PrintWriter(fichero);

	            pw.println(strAreas.toString().replace("\n","\r\n"));
	            pw.println(strPesos.toString().replace("\n","\r\n"));
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	           try {
	           // Nuevamente aprovechamos el finally para 
	           // asegurarnos que se cierra el fichero.
	           if (null != fichero)
	              fichero.close();
	           } catch (Exception e2) {
	              e2.printStackTrace();
	           }
	        }
			
		}
		
	}
	
	@Override
	public boolean done() {
		return step == 10;
	}

	@Override
	public int onEnd() {				
				
		System.out.println("===>>> " + area.getLocalName() + " Termina la negociacion numero " + area.getNumeroTotalNegociaciones() + " lanzada por " + lstVehiculos.get(lstVehiculos.size()-1) + ". QUEDAN: " + area.getBufferNegociaciones().size());
		area.setEstadoNegociacion(false);
		
		t_fin = System.currentTimeMillis();
		area.anadirTiempoNegociacion(t_fin - t_ini);
			
		return super.onEnd();
	}
	
}
