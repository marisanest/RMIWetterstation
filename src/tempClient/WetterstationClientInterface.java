package tempClient;

import java.rmi.Remote;
import java.util.Date;

import wetterServer.TempPoint;

public interface WetterstationClientInterface extends Remote {

	public void updateTemperatures(Date date, TempPoint temp) throws java.rmi.RemoteException;

} 

