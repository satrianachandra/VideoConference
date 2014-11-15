/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import audio.AudioReceiverPipeline;
import audio.AudioSenderPipeline;
import message.Call;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import video.VideoReceiverPipeline;
import video.VideoSenderPipeline;

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
    private VideoReceiverPipeline receiverVideo;
    /** GStreamer pipeline to send to rooms and contact */
    private VideoSenderPipeline senderVideo;
    
    /** GStreamer pipeline to receive from rooms and contact */
    private AudioReceiverPipeline receiverAudio;
    /** GStreamer pipeline to send to rooms and contact */
    private AudioSenderPipeline senderAudio;
    
    
    public VideoConference(){
        
        //communication with server
        
        
        //GStreamer inits
        Gst.init("AudioVideoConferencing", new String[] { "--gst-debug-level=2",
                        "--gst-debug-no-color" });
        
        myRtpBin = ElementFactory.make("gstrtpbin", null);
       // rtpBin2 = ElementFactory.make("gstrtpbin", null);
        
       // receiverVideo = new VideoReceiverPipeline();
       // senderVideo = new VideoSenderPipeline();
        
        receiverAudio = new AudioReceiverPipeline();
        senderAudio = new AudioSenderPipeline();
        
    }
    
    public void joinRoom(int roomId) {
        /*
         * remember my SSRC to remove it from the incoming stream from multicast
         * (prevents echo of my own voice)
         */
        long mySSRC = senderVideo.streamTo(roomId);
        receiverVideo.receiveFromRoom(roomId, mySSRC);

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
        
        senderVideo.stopStreamingToRoom(roomId);
        receiverVideo.stopRoomReceiving(roomId);
        senderAudio.stopStreamingToRoom(roomId);
        receiverAudio.stopRoomReceiving(roomId);
    }
    
    public void askToCall(String contact) {
        if (contact.endsWith("(Disconnected)")) {
                contact = contact.substring(0, contact.length() - 15);
        }
        // open a local port for our (maybe) future conversation
        //int portVideo = this.receiverVideo.receiveFromUnicast(myRtpBin);
        int portVideo = this.receiverVideo.receiveFromUnicast();
        //send(new Call(username, contact, "0", port));
        // remember name of my friend I'm talking with
        
        int portAudio = this.receiverAudio.receiveFromUnicast();
        //int portAudio = this.receiverAudio.receiveFromUnicast(myRtpBin);
        
        this.friend = contact;
    }

    public void call(String ipReceiver, int port) {
        senderVideo.streamTo(ipReceiver, port);
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
           receiverVideo.stopUnicastReceiving();
           // stop streaming to friend
           senderVideo.stopStreamingToUnicast();

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
                portVideo = receiverVideo.receiveFromUnicast();
                this.friend = call.getSender();
                
                //portAudio = receiverAudio.receiveFromUnicast();
                portAudio = receiverAudio.receiveFromUnicast();
                
                //gui.getCallBtn().setVisible(false);
                //gui.getHangUpBtn().setVisible(true);
        }

        //send(new AnswerCall(port, call.getSender(), call.getReceiver(), answer,
                  //          "0"));
    }

    
    public VideoSenderPipeline getSenderVideo(){
        return this.senderVideo;
    }
    
    public VideoReceiverPipeline getReceiverVideo(){
        return this.receiverVideo;
    }
    
    public AudioSenderPipeline getSenderAudio(){
        return this.senderAudio;
    }
    
    public AudioReceiverPipeline getReceiverAudio(){
        return this.receiverAudio;
    }
    
}
