/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
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
public class ServerChannel implements Runnable {

    private VideoConference vc;
    //private BufferedReader in;
    //private PrintStream out;
    private ObjectInputStream inputStream;	
    private ObjectOutputStream outputStream;
    
    private boolean stop = false;
    
    public ServerChannel(VideoConference vc){
        this.vc = vc;
        
        try {
            //open connection to the server
            Socket socket = new Socket(InetAddress.getByName(Config.SERVER_ADDRESS),
                    Config.SERVER_PORT);
            //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(socket.getInputStream());
            
            //out = new PrintStream(socket.getOutputStream());
        } catch (UnknownHostException ex) {
            Logger.getLogger(ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println(ex);
            Logger.getLogger(ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    
    @Override
    public void run() {
        Message message = null;
        while (!stop){
            try {
                message = (Message)inputStream.readObject();
            }catch(EOFException ex){
                stop = true;
                Logger.getLogger(ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (message == null){ //user has diconnected somehow
                stop = true;
            }else{
                processMessage((Message)message);
            }
        }   
        System.out.println("server channel closed");
        vc.endPrivateCall();
        
    }

    private void processMessage(Message message) {
        if (message.getType()==MessageType.REGISTERED){
            vc.registered();
        }else if (message.getType()==MessageType.FETCHUSERS){
            List<User> users;
            users = (List<User>)message.getContent();
            for (User user: users){
                System.out.println(user.getUserName());
            }
            System.out.println("---------");
            vc.updateUsersList(users);
        }else if (message.getType()==MessageType.CALL_REQUEST){
            User senderUser = (User)message.getContent();
            vc.acceptCall(senderUser);
        }else if (message.getType()==MessageType.CALL_ACCEPTED){
            User destUser = (User)message.getContent();
            vc.callAccepted(destUser);
        }else if (message.getType()==MessageType.BYE){
            vc.endPrivateCall();
        }
    }
    
    public void send(Message message) {
        try {
            outputStream.reset();
            outputStream.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
