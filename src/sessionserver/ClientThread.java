/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 *
 * @author chandra
 */
public class ClientThread implements Runnable{

    private Socket clientSocket = null;
    private boolean stop = false;
    
    private BufferedReader in;
    private PrintStream out;
    
    public ClientThread(Socket clientSocket){
        this.clientSocket = clientSocket;
        
        try {
            in = new BufferedReader(new InputStreamReader(
            clientSocket.getInputStream()));
            out = new PrintStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        String message = null;
        while (!stop){
            try {
                message = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message == null) {
                //the client disconnects
                stop = true;
            } 
            
            
        }
        
    }
    
}
