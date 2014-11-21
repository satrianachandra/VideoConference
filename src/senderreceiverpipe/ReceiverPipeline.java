/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senderreceiverpipe;

import app.VideoConference;
import message.User;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pipeline;
import org.gstreamer.swing.VideoComponent;
import util.Config;

/**
 *
 * @author chandra
 */
public class ReceiverPipeline extends Pipeline{
    
    UnicastReceiver unicastReceiver = null;
    private static final String RECEIVER_ROOM_PREFIX = "receiver_room";
    
    private final Element adderAudio = ElementFactory.make("liveadder", null);
    private final Element sinkAudio = ElementFactory.make("autoaudiosink", null);
    
    private Element sinkVideo=null;// = ElementFactory.make("autovideosink", null);
    private VideoConference vc;
    
    private Element sinkVideoCR1;
    private Element sinkVideoCR2;
    private Element sinkVideoCR3;
    private Element sinkVideoCR4;
    
    public ReceiverPipeline(VideoConference vc){
        super("audio_receiver_pipeline");
        this.vc = vc;
        
        //add(sink);
        //link(sink);
        //sink.set("sync", true);
        
        //video
        /*
        VideoComponent videoComponent = new VideoComponent();
        sinkVideo = videoComponent.getElement();
        sinkVideo.setName("vcunicast");
        
        
        add(sinkVideo);
        link(sinkVideo);
        sinkVideo.syncStateWithParent();
        vc.getGUI().showOtherVideo(videoComponent);
        */
        
        addMany(adderAudio, sinkAudio);
        linkMany(adderAudio, sinkAudio);
        
        
        play();
        
    }
    
    //public int receiveFromUnicast(Element myRtpBin) {
    public int receiveFromUnicast(String senderIP) {
        // create the receiver bin
        //unicastReceiver = new AudioUnicastReceiver(adder,myRtpBin);
        unicastReceiver = new UnicastReceiver( senderIP, adderAudio,vc);
        // add it to this
        add(unicastReceiver);
        unicastReceiver.syncStateWithParent();
        return unicastReceiver.getPort();
    }
    
    public void stopUnicastReceiving() {
        if (unicastReceiver != null) {
            unicastReceiver.getOut();
        }
        unicastReceiver = null;
    }


    public void receiveFromRoom(long ssrcToIgnore, User myUser) {
        // create the receiver bin
        int roomId = 1;
       // User aRoom = new User("A Room", Config.ROOM_IP, Config.rtpaPortRoom, Config.rtcpasrcPortRoom,
       //         Config.rtpvPortRoom, Config.rtcpvsrcPortRoom);
        
        RoomReceiver room = new RoomReceiver(RECEIVER_ROOM_PREFIX + roomId,
        Config.ROOM_IP, ssrcToIgnore,vc);
        // add it to this
        add(room);
        room.syncStateWithParent();
        // connect its output to the adder
        room.link(adderAudio);
    }
    
    
    
    public void stopRoomReceiving(int roomId) {
        ((RoomReceiver) getElementByName(RECEIVER_ROOM_PREFIX + roomId))
        .getOut();
    }
    
    public void stopRoomReceiving() {
        ((RoomReceiver) getElementByName(RECEIVER_ROOM_PREFIX + 1))
        .getOut();
    }
    
    
}
