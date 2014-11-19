/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.util.ArrayList;
import java.util.List;
import senderreceiverpipe.ReceiverPipeline;
import senderreceiverpipe.SenderPipeline;
import message.Call;
import message.Message;
import message.MessageType;
import message.User;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;


/**
 *
 * @author chandra
 */
public class VideoConference {

    
    private GUI gui;
    private GUIConferenceRoom guiCR;
    
    private String myUserName;
    private User myUser = null;
    private User destinationUser = null;
    
    
    /** GStreamer pipeline to receive from rooms and contact */
    private ReceiverPipeline receiverPipeline;
    /** GStreamer pipeline to send to rooms and contact */
    private SenderPipeline senderPipeline;
    
    private ServerChannel serverChannel;
    
    private List<User>usersListLocal;
    
    public VideoConference(){
       
        //GStreamer inits
        Gst.init("AudioVideoConferencing", new String[] { "--gst-debug-level=2",
                        "--gst-debug-no-color" });
        
        
        usersListLocal = new ArrayList<>();
        
        
        
    }
    
    public void joinRoom(int roomId) {
        /*
         * remember my SSRC to remove it from the incoming stream from multicast
         * (prevents echo of my own voice)
         */
        //long mySSRC = senderAudio.streamTo(roomId);
        //receiverAudio.receiveFromRoom(roomId, mySSRC);

            /*
        send(new Join(roomId));
        try {
                control.getRoomsListFinished().acquire();
        } catch (InterruptedException e) {
                System.err
                                .println("This thread has been interrupted while waiting the end "
                                                + "of a message from the server, message might be incomplete...");
                e.printStackTrace();
        }
        Room newRoom = updateAfterJoin(control.getUpdatedAudience());
        boolean createRoom = true;
        for (Room oldRoom : allRooms) {
                if (oldRoom.getId() == newRoom.getId()) {
                        allRooms.set(allRooms.indexOf(oldRoom), newRoom);
                        createRoom = false;
                }
        }
        if (createRoom) {
                allRooms.add(newRoom);
        }*/
        
        
    }
    
    public void leaveRoom(int roomId) {
        /*
        getControl().send(new Leave(roomId).toString());
        for (Room room : allRooms) {
                if (room.getId() == roomId) {
                        allRooms.get(allRooms.indexOf(room)).getAudience()
                                        .remove(username);
                }
        }*/
        
        //senderAudio.stopStreamingToRoom(roomId);
        //receiverAudio.stopRoomReceiving(roomId);
        senderPipeline.stopStreamingToRoom(roomId);
        receiverPipeline.stopRoomReceiving(roomId);
    }
    
    public void askToCall(String contact) {
        if (contact.endsWith("(Disconnected)")) {
                contact = contact.substring(0, contact.length() - 15);
        }
        // open a local port for our (maybe) future conversation
        //int portVideo = this.receiverVideo.receiveFromUnicast(myRtpBin);
        //int portVideo = this.receiverAudio.receiveFromUnicast();
        //send(new Call(username, contact, "0", port));
        // remember name of my friend I'm talking with
        
        //int portAudio = this.receiverAudio.receiveFromUnicast();
        //int portAudio = this.receiverAudio.receiveFromUnicast(myRtpBin);
        
       // this.friend = contact;
    }

    public void call(String ipReceiver, int port) {
        //senderVideo.streamTo(ipReceiver, port);
        //senderAudio.streamTo(ipReceiver, port);
        //gui.getCallBtn().setVisible(false);
        //gui.getHangUpBtn().setVisible(true);
    }

    /**
    * hang up. Stop the pipelines and send a message to the server for telling
    * the other client the call is finished
    */
    public void askToStopCall() {
           stopCall();

           //send(new StopCall(friend));
    }
    
    /**
    * Stop streaming from/to friend and update UI buttons.
    */
    public void stopCall() {
           // stop streaming from friend
           //receiverVideo.stopUnicastReceiving();
           // stop streaming to friend
           //senderVideo.stopStreamingToUnicast();

           receiverPipeline.stopUnicastReceiving();
           // stop streaming to friend
           senderPipeline.stopStreamingToUnicast();
           
           //gui.getCallBtn().setVisible(true);
           //gui.getHangUpBtn().setVisible(false);
    }
    
    public void answerCall(String answer, Call call) {
        int portVideo = -1;
        int portAudio = -1;

        if (answer.equals("yes")) {
                //portVideo = receiverVideo.receiveFromUnicast(myRtpBin);
                //portVideo = receiverVideo.receiveFromUnicast();
                
                //portAudio = receiverAudio.receiveFromUnicast();
               // portAudio = receiverAudio.receiveFromUnicast();
               // this.friend = call.getSender();
                
                //gui.getCallBtn().setVisible(false);
                //gui.getHangUpBtn().setVisible(true);
        }

        //send(new AnswerCall(port, call.getSender(), call.getReceiver(), answer,
                  //          "0"));
    }

    
    public SenderPipeline getSenderPipeline(){
        return this.senderPipeline;
    }
    
    public ReceiverPipeline getReceiverPipeline(){
        return this.receiverPipeline;
    }
    
    public void setGUI(GUI gui){
        this.gui = gui;
    }

    public void setGUICR(GUIConferenceRoom guiCR){
        this.guiCR = guiCR;
    }
    
    public void signin(String userName) {
        myUser = new User(userName, "");
        serverChannel.send(new Message(MessageType.REGISTERING, userName));
    }

    void registered() {
        //hide the welcome gui
        System.out.println("User registerd");
        gui.getWelcomePanel().setVisible(false);
        //show the communication gui
        gui.getMainPanel().setVisible(true);
        
        //fetch userslist
        //serverChannel.send(new Message(MessageType.FETCHUSERS));
        
    }

    void updateUsersList(List<User> list) {
        usersListLocal = list;
         if (usersListLocal!=null){
            String[]usersArray = new String[usersListLocal.size()];
            for (int i=0;i<usersArray.length;i++){
                usersArray[i]=usersListLocal.get(i).getUserName();
                System.out.println(usersArray[i]);
            }
            gui.getUsersListList().setModel(new javax.swing.DefaultComboBoxModel(usersArray));
        }
        
    }
    
    public void init(){
        //communication with server
        serverChannel = new ServerChannel(this);
        new Thread(serverChannel).start();
        
        receiverPipeline = new ReceiverPipeline(this);
        senderPipeline = new SenderPipeline(this);
        
    }

    void privateCall(int index) {
        if (destinationUser == null){
            User theUser = usersListLocal.get(index);
            destinationUser = theUser;
            
            //ask server to tell the receiver to prepare
            serverChannel.send(new Message(MessageType.CALL_REQUEST, theUser) );

            //get ready for listening
            getReceiverPipeline().receiveFromUnicast(myUser, theUser);
        }
    }

    void acceptCall(User senderUser) {
        //get ready for listening from the originator
        getReceiverPipeline().receiveFromUnicast(myUser, senderUser);
        
        //tell the sender that his/her call is accepted
        serverChannel.send(new Message(MessageType.CALL_ACCEPTED, senderUser));
        
        //start sending to the originator
        senderPipeline.streamTo(myUser, senderUser);
        
    }

    void callAccepted(User destUser) {
        //my call is accepted, start sending to that destUser
        senderPipeline.streamTo(myUser, destUser);  
    }
    
    public GUI getGUI(){
        return gui;
    }

    void endPrivateCall() {
        if (destinationUser!=null){
            serverChannel.send(new Message(MessageType.BYE, destinationUser));
            
            
            //stop sending
            senderPipeline.stopStreamingToUnicast();
            
            //stop receiviing
            receiverPipeline.stopUnicastReceiving();
        }
        destinationUser=null;
    }

    void joinRoom() {
        serverChannel.send(new Message(MessageType.JOIN_ROOM));
        
        //start receiving from room
        long mySSRC = senderPipeline.streamToRoom(myUser);
        receiverPipeline.receiveFromRoom(mySSRC, myUser);
        
    }

    void showGUIConferenceRoom() {
        guiCR.setVisible(true);
        gui.setVisible(false);
    }

    void showMainGUI() {
        guiCR.setVisible(false);
        gui.setVisible(true);
    }
    
}
