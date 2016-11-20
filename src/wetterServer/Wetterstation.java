package wetterServer;

import java.io.FileNotFoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;

import tempClient.WetterstationClientInterface;

public interface Wetterstation extends Remote {
	
	public ArrayList<TempPoint> getTemperatures(Date date) throws RemoteException, FileNotFoundException;
	
	public void register(WetterstationClientInterface client) throws RemoteException;

	public void unregister(WetterstationClientInterface client) throws RemoteException;
}

