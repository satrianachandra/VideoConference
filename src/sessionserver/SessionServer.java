/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author chandra
 */
public class SessionServer implements Runnable{

    private boolean stop = false;
    private ServerSocket serverSocket = null;
    private int serverPort = 8080;
    private Thread runningThread= null;
    
    public SessionServer(int port){
        this.serverPort = port;
    }
    
    @Override
    public void run() {
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port "+this.serverPort , e);
        }
        
        while (!stop){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(stop) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            new Thread(new ClientThread(clientSocket)).start();
        
        }
    }
    
    public static void main(String[]args){
        SessionServer ss = new SessionServer(8080);
        new Thread(ss).start();
        
    }
    
}
