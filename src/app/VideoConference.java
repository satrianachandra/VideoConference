/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import senderreceiverpipe.ReceiverPipeline;
import senderreceiverpipe.SenderPipeline;
import message.Call;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;


/**
 *
 * @author chandra
 */
public class VideoConference {

    private Element myRtpBin;
//    private Element rtpBin2;
    
    /** own username */
    private String username;
    /** name of the guy we're currently calling */
    private String friend;
    
    private GUI gui;
    
    /** TCP connection to server */
    //private ControlChannel control;
    
    /** GStreamer pipeline to receive from rooms and contact */
    private ReceiverPipeline receiverAudio;
    /** GStreamer pipeline to send to rooms and contact */
    private SenderPipeline senderAudio;
    
    
    public VideoConference(){
        
        //communication with server
        
        
        //GStreamer inits
        Gst.init("AudioVideoConferencing", new String[] { "--gst-debug-level=2",
                        "--gst-debug-no-color" });
        
        myRtpBin = ElementFactory.make("gstrtpbin", null);
       // rtpBin2 = ElementFactory.make("gstrtpbin", null);
        
       // receiverVideo = new VideoReceiverPipeline();
       // senderVideo = new VideoSenderPipeline();
        
        receiverAudio = new ReceiverPipeline();
        senderAudio = new SenderPipeline();
        
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
        senderAudio.stopStreamingToRoom(roomId);
        receiverAudio.stopRoomReceiving(roomId);
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
        
        int portAudio = this.receiverAudio.receiveFromUnicast();
        //int portAudio = this.receiverAudio.receiveFromUnicast(myRtpBin);
        
        this.friend = contact;
    }

    public void call(String ipReceiver, int port) {
        //senderVideo.streamTo(ipReceiver, port);
        senderAudio.streamTo(ipReceiver, port);
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

           receiverAudio.stopUnicastReceiving();
           // stop streaming to friend
           senderAudio.stopStreamingToUnicast();
           
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
                portAudio = receiverAudio.receiveFromUnicast();
                this.friend = call.getSender();
                
                //gui.getCallBtn().setVisible(false);
                //gui.getHangUpBtn().setVisible(true);
        }

        //send(new AnswerCall(port, call.getSender(), call.getReceiver(), answer,
                  //          "0"));
    }

    
    public SenderPipeline getSenderAudio(){
        return this.senderAudio;
    }
    
    public ReceiverPipeline getReceiverAudio(){
        return this.receiverAudio;
    }
    
}
