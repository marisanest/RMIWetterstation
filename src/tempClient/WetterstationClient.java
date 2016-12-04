package tempClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import wetterServer.Wetterstation;
import wetterServer.TempPoint;

public class WetterstationClient extends UnicastRemoteObject implements WetterstationClientInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Map<Date, ArrayList<TempPoint>> dates = new HashMap<Date, ArrayList<TempPoint>>();
	
	protected WetterstationClient() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String args[]) {
        
		if (System.getSecurityManager() == null) {
            		System.setSecurityManager(new SecurityManager());
        	}
		
       		try {
            		String name = "Wetterstation";
            		Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            		Wetterstation server = (Wetterstation) registry.lookup(name);
            		WetterstationClientInterface client = new WetterstationClient();
            
            		while(true)
            			interact(server, client);
           
        	} catch (Exception e) {
            		System.err.println("WetterstationClient exception:");
            		e.printStackTrace();
        	}
    	}  
	
	private static void interact(Wetterstation server, WetterstationClientInterface client) throws IOException {
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println();
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("                 Was wollen sie tun?");
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("a : Anfrage an Server");
		System.out.println("r : Beim Server registrieren");
		System.out.println("u : Beim Server abmelden");
		System.out.println("q : Dienst quittieren");
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println();
		
		String response = stdIn.readLine();
		
		if(response.equals("a")) {
			requestServer(server);
		}
		else if(response.equals("r")) {
            server.register(client);
            System.out.println("Client wurde erfolgreich registriert...");
		}
		else if(response.equals("u")) {
            server.unregister(client);
            System.out.println("Client wurde erfolgreich abgemeldet...");
		}
		else if(response.equals("q")){
			server.unregister(client);
			System.out.println("Client wurde erfolgreich abgemeldet...");
			System.exit(0);
		}
	}
	
	private static void requestServer(Wetterstation server) throws IOException{
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Bitte gebe ein Datum ein (Format: tt.mm.jjjj):");
        
		String stringDate = stdIn.readLine();
		
	    DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
	    
		try {
			Date date = df.parse(stringDate);
			
			ArrayList<TempPoint> temp;
		    
		    if(dates.containsKey(date))
		    	temp = dates.get(date);
		    else
		    	temp = server.getTemperatures(date);  
	        
		    
		    if(temp == null)
	    		System.out.println("Für dieses Datum existieren keine Temperaturwerte!");
		    
	    	else if(temp.size() != 24)
	    		System.out.println("Für dieses Datum sind die Temperaturangaben unvollständig!");
		    
	    	else {
	    		dates.put(date, temp);
	    		showTemperatures(date, dates.get(date));
	    	}
		    
		} catch (ParseException e) {
			System.out.println("Datum wurde in falschem Format angegeben!");
		}
	}
	
	private static double calcAverage(ArrayList<TempPoint> temperatures) {
		
		double res = 0;
		
        for(int j = 0; j < temperatures.size(); j++)
        	res +=temperatures.get(j).getTemp();
        	
        res = res/temperatures.size();
        
        return res;
	}
	
	private static double calcMin(ArrayList<TempPoint> temperatures) {
		
		double res = temperatures.get(0).getTemp();
		
        for(int j = 1; j < temperatures.size(); j++)
        	if(temperatures.get(j).getTemp() < res)
        		res = temperatures.get(j).getTemp();
        
        return res;
	}
	
	private static double calcMax(ArrayList<TempPoint> temperatures) {
		
		double res = temperatures.get(0).getTemp();
	
		for(int j = 1; j < temperatures.size(); j++)
			if(temperatures.get(j).getTemp() > res)
				res = temperatures.get(j).getTemp();
    
		return res;
	}

	@Override
	public void updateTemperatures(Date date, TempPoint temp) throws RemoteException {
		if (dates.containsKey(date)){
			
			ArrayList<TempPoint> tempArr = dates.get(date);
				
			for(int i = 0; i < tempArr.size(); i++){
				if(tempArr.get(i).getId() == temp.getId())
					tempArr.get(i).setTemp(temp.getTemp());
			}
			
			System.out.println();
			System.out.println("***********************Zwischen Meldung***********************");
			System.out.println("Es haben sich Temperaturen geändert! Folgendes sind die aktualisierten Daten:");
			showTemperatures(date, dates.get(date));
			System.out.println("***********************Zwischen Meldung***********************");
			System.out.println();
		}
	}
	
	public static void showTemperatures(Date date, ArrayList<TempPoint> temps) {
		System.out.println("***************"+date+"**************");
		System.out.println("Temperaturen: "+temps);
        System.out.println("Durchschnittliche Temperatur: " + calcAverage(temps));
        System.out.println("Minimal Temperatur: " + calcMin(temps));
	    System.out.println("Maximal Temperatur: " + calcMax(temps));
	    System.out.println("**********************************************************");
	}
}
