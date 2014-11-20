/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionserver;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.Message;
import message.MessageType;
import message.User;
import util.Config;


/**
 *
 * @author chandra
 */
public class ClientThread implements Runnable{

    private SessionServer server;
    private Socket clientSocket = null;
    private User myUser = null;
    private boolean stop = false;
    private ClientThread destinationUserThread=null;
    
    //private BufferedReader in;
    //private PrintStream out;
    private ObjectInputStream inputStream;	
    private ObjectOutputStream outputStream;
    
    
    public ClientThread(Socket clientSocket, SessionServer server){
        this.clientSocket = clientSocket;
        this.server = server;
        try {
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public User getMyUser(){
        return myUser;
    }
    
    @Override
    public void run() {
        Message message = null;
        while (!stop){
            try {
                message = (Message)inputStream.readObject();
                System.out.println("message receive, type "+message.getType());
            }catch (EOFException ex){
                //the client disconnects, delete him/her from the list
                if (myUser!=null){
                    server.getUsersList().remove(myUser);
                    server.getClientThreadList().remove(this);
                    server.updateListUsersInLocals();
                }
                stop = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (message == null) {
                //the client disconnects, delete him/her from the list
                if (myUser!=null){
                    server.getUsersList().remove(myUser);
                    server.getClientThreadList().remove(this);
                    server.updateListUsersInLocals();
                }
                stop = true;
            }else{
                processMessage(message);
            }
            
        }
        System.out.println("client thread closed");
    }

    private void processMessage(Message message) {
        if (message.getType() == MessageType.REGISTERING){
            String userName = (String)message.getContent();
            System.out.println("registering username: "+userName);
            String userIP = clientSocket.getInetAddress().getHostAddress();
            System.out.println(userIP);
            this.myUser = new User(userName,userIP, Config.rtpaPort, Config.rtcpasrcPort, Config.rtpvPort, Config.rtcpvsrcPort);
            if (!server.getUsersList().contains(this.myUser)){
                server.getUsersList().add(myUser);
                
                send(new Message(MessageType.REGISTERED));
                server.updateListUsersInLocals();
            }
            
            
        }else if (message.getType()==MessageType.FETCHUSERS){
           pushUpdatedUsersList();
        }else if (message.getType()==MessageType.CALL_REQUEST){
            User destUser = (User)message.getContent();
            //write the request to the destUser's socket
            ClientThread aClientThread = getClientThreadBasedOnUser(destUser);
            aClientThread.send(new Message(MessageType.CALL_REQUEST, myUser));
            destinationUserThread = aClientThread;
            destinationUserThread.setDestinationUserThread(this);
        }else if (message.getType() == MessageType.CALL_ACCEPTED){
            //User originator = (User)message.getContent();
            //ClientThread originatorsClientThread = getClientThreadBasedOnUser(originator);
            //originatorsClientThread.send(new Message(MessageType.CALL_ACCEPTED, myUser));
            destinationUserThread.send(new Message(MessageType.CALL_ACCEPTED, myUser));
        }else if (message.getType()==MessageType.BYE){
            /*
            User theOtherParty = (User)message.getContent();
            ClientThread theOtherPartyThread = getClientThreadBasedOnUser(theOtherParty);
            theOtherPartyThread.send(new Message(MessageType.BYE));
                    */
            if (destinationUserThread !=null){
                destinationUserThread.send(new Message(MessageType.BYE));
                System.out.println("send bye to "+destinationUserThread.getMyUser().getIpAddress());
            }
        }
               
    }
    
    public void pushUpdatedUsersList(){
        System.out.println("pushing updated users list---------");
        for (int i=0;i<server.getUsersList().size();i++){
            System.out.println("user-"+i+":"+server.getUsersList().get(i).getUserName());
        }
        send(new Message(MessageType.FETCHUSERS, server.getUsersList()));
    }

    public void send(Message message){
        try {
            outputStream.reset();
            outputStream.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private ClientThread getClientThreadBasedOnUser(User aUser){
        for (int i=0;i<server.getClientThreadList().size();i++){
            ClientThread aClientThread = server.getClientThreadList().get(i);
            if (aClientThread.getMyUser().getIpAddress().equalsIgnoreCase(aUser.getIpAddress()) ){
                return aClientThread;
            }    
        }
        return null;
    }
    
    private ClientThread getDestinationUserThread(){
        return this.destinationUserThread;
    }
    
    private void setDestinationUserThread(ClientThread destinationUserThread){
        this.destinationUserThread = destinationUserThread;
    }
    
}
