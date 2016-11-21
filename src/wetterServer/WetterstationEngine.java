package wetterServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.Scanner;
import java.util.Vector;

import tempClient.WetterstationClientInterface;

public class WetterstationEngine implements Wetterstation {

	private static Map<Date, ArrayList<TempPoint>> dates = new HashMap<Date, ArrayList<TempPoint>>();
	private static Vector<WetterstationClientInterface> clientList = new Vector<WetterstationClientInterface>();
	
	public WetterstationEngine() throws RemoteException {
        super();
        clientList = new Vector<WetterstationClientInterface>();
    }
	
	@Override
	public ArrayList<TempPoint> getTemperatures(Date date) throws RemoteException {
		
		ArrayList<TempPoint> temps = dates.get(date);
		
		return temps;
	}
	
	public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            String name = "Wetterstation";
            Wetterstation engine = new WetterstationEngine();
            Wetterstation stub = (Wetterstation) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind(name, stub);
            System.out.println("WetterstationEngine bound...");
            
            try {
    			dates = parseCSV("/Users/admin/Git/Wetterstation/Temperaturen.csv");
    			System.out.println("Weather data fetched...");
    		} catch (FileNotFoundException e) {
    			System.err.println("CSVFile exception:");
    			e.printStackTrace();
    		}
            
            while(true)
            	interact();
            
        } catch (Exception e) {
            System.err.println("WetterstationEngine exception:");
            e.printStackTrace();
        }
    }
	
	private static Map<Date, ArrayList<TempPoint>> parseCSV(String filePath) throws FileNotFoundException {
		
		Map<Date, ArrayList<TempPoint>> dates = new HashMap<Date, ArrayList<TempPoint>>();

		Scanner scanner = new Scanner(new File(filePath));
        
		scanner.useDelimiter(";");
		
		DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
		
		int index = 0;
		Date date = null;
		ArrayList<TempPoint> temps = new ArrayList<TempPoint>();
		
		while(scanner.hasNext()){
			
			String data = eliminateAllEscapeSymboles(scanner.next());
		    
			try {
				Date temp = df.parse(data);
				
				if(date != null){
					dates.put(date, temps);
					index = 0;
					temps = new ArrayList<TempPoint>();
				}
				date = temp;
				
			} catch (ParseException e) {
				if(date != null){
					try{
						temps.add(new TempPoint(index, Double.parseDouble(data)));
						index++;
					} catch (NumberFormatException ex) { 
						/* TODO
						 * exception handling
						 */
					}
				}
			}
		}
		dates.put(date, temps);
		
        scanner.close();
         
        return dates;
	}
	
	private static String eliminateAllEscapeSymboles(String str) {
		return str.replaceAll("\\s+","").replaceAll("\\n+","").replaceAll("\\r+","").replaceAll("\\t+","").replaceAll("\\b+","").replaceAll("\\f+","");
	}

	public synchronized void register(WetterstationClientInterface client) throws RemoteException {
		if (!(clientList.contains(client))) {
			clientList.addElement(client);
			System.out.println("Neuer Client wurde erfolgreich registriert...");
		}
		else
			System.out.println("Client war bereits registriert...");
	}  

	public synchronized void unregister(WetterstationClientInterface client) throws RemoteException {
		if (clientList.removeElement(client))
			System.out.println("Client wurde abgemeldet...");
		else
			System.out.println("Client ist nicht registriert...");
	} 

	private static synchronized void callback(Date date, TempPoint temp) throws RemoteException {
		
		System.out.println();
		System.out.println( "*********************Callback beginnt*********************");
		
		for (int i = 0; i < clientList.size(); i++){
			System.out.println("Callback Nummer "+ i+1 +"...");    
			WetterstationClientInterface client = (WetterstationClientInterface)clientList.elementAt(i);
			client.updateTemperatures(date, temp);
		}
		
		System.out.println("*********************Callback beendet*********************");
		System.out.println();
	}
	
	private static void interact() throws IOException{
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println();
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("                  Was wollen sie tun?");
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("a : Aenderungen an den Temperaturen vornehmen");
		System.out.println("q : Dienst quittieren");
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println();
		
		String response = stdIn.readLine();
		
		if(response.equals("a"))
			changeTemperatures();
		else if(response.equals("q"))
			System.exit(0);
	}
	
	private static void changeTemperatures() throws IOException{
		
		System.out.println("Zum hinzufügen oder aktualisieren einer Temperatur gebe bitte ein Datum, die Stunde und eine Temperatur an (Format: tt.mm.jjjj,ss,dd.dd):");
        
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String str = eliminateAllEscapeSymboles(stdIn.readLine());
		
		String[] strArr = str.split(","); 
		
	    DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
	    
		try {
			Date date = df.parse(strArr[0]);
			
			int hour = Integer.parseInt(strArr[1]);
			
			double temp = Double.parseDouble(strArr[2]);
			
			insertTemperatures(date, hour, temp);
			System.out.println("Eintrag wurde erfolgreich hinzugefügt oder aktualisiert!");
			callback(date, new TempPoint(hour, temp));
				
		} catch (Exception e) {
			System.out.println("Angaben sind im falschem Format!");
		}
	}
	
	private static void insertTemperatures(Date date, int hour, double temp) {
		ArrayList<TempPoint> tempArr = dates.get(date);
		if(tempArr != null){
			boolean changed = false;
			for(int i = 0; i < tempArr.size(); i++){
				if(tempArr.get(i).getId() == hour) {
					tempArr.get(i).setTemp(temp);
					changed = true;
				}
			}
			if(!changed)
				tempArr.add(new TempPoint(hour, temp));
		}
		else {
			dates.put(date, new ArrayList<TempPoint>());
			dates.get(date).add(new TempPoint(hour, temp));
		}
	}
}