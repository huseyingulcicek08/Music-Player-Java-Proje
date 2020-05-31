
/**
 * @author: Ashwin Kamalakannan
 * last Edited: October 28, 2019
 *
 * Interface containing method definitions that will be invoked by client to
 * communicate with server through RMI.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface FileInterface extends Remote {
    public void uploadSong(byte[] content, String fileName) throws RemoteException;

    public byte[] downloadSong(String fileName) throws RemoteException;

    public void deleteSong(String fileName) throws RemoteException;

    public ArrayList<String> checkAvailableSongs() throws RemoteException;

    public byte[] downloadImage(int size) throws RemoteException;
}