/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import message.User;
import util.Config;

/**
 *
 * @author chandra
 */
public class SessionServer implements Runnable{

    private boolean stop = false;
    private ServerSocket serverSocket = null;
    private int serverPort = 8080;
    private Thread runningThread= null;
    
    private List<User>usersList;
    private List<ClientThread>clientThreadList;
    private List<User>roomParticipants;
    
    public SessionServer(int port){
        this.serverPort = port;
        usersList = new ArrayList<>();
        clientThreadList = new ArrayList<>();
        roomParticipants = new ArrayList<>();
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
            ClientThread aClientThread = new ClientThread(clientSocket,SessionServer.this);
            if (!clientThreadList.contains(aClientThread)){
                clientThreadList.add(aClientThread);
                new Thread(aClientThread).start();
            }
            
        
        }
    }
    
    public List<User> getUsersList(){
        return usersList;
    }
    
    public static void main(String[]args){
        SessionServer ss = new SessionServer(Config.SERVER_PORT);
        new Thread(ss).start();
        
    }

    public void updateListUsersInLocals() {
        for(int i=0;i<clientThreadList.size();i++){
            clientThreadList.get(i).pushUpdatedUsersList();
        }
    }
    
    public List<ClientThread> getClientThreadList(){
        return clientThreadList;
    }
    
    public List<User>getRoomParticipants(){
        return roomParticipants;
    }

    void updateRoomParticipantsInLocals() {
        for(int i=0;i<clientThreadList.size();i++){
            clientThreadList.get(i).pushUpdatedRoomParticipants();
        }
    }
    
}
