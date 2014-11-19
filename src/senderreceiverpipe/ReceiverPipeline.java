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
        VideoComponent videoComponent = new VideoComponent();
        sinkVideo = videoComponent.getElement();
        sinkVideo.setName("vcunicast");
        
        addMany(adderAudio, sinkAudio);
        linkMany(adderAudio, sinkAudio);
        
        add(sinkVideo);
        link(sinkVideo);
        sinkVideo.syncStateWithParent();
        vc.getGUI().showOtherVideo(videoComponent);
        
        
        //show videos for the conference room, 
        //not sure if this can be actually done dynamically when the pipeline is already running?
        //for now just do it like this:
        
        VideoComponent videoComponentCR1 = new VideoComponent();
        sinkVideoCR1 = videoComponentCR1.getElement();
        sinkVideoCR1.setName("cr1");
        add(sinkVideoCR1);
        link(sinkVideoCR1);
        sinkVideoCR1.syncStateWithParent();
        vc.getGUICR().showVideo1(videoComponentCR1);
        
        
        VideoComponent videoComponentCR2 = new VideoComponent();
        sinkVideoCR2 = videoComponentCR2.getElement();
        sinkVideoCR2.setName("cr2");
        add(sinkVideoCR2);
        link(sinkVideoCR2);
        sinkVideoCR2.syncStateWithParent();
        vc.getGUICR().showVideo2(videoComponentCR2);
        
        
        VideoComponent videoComponentCR3 = new VideoComponent();
        sinkVideoCR3 = videoComponentCR3.getElement();
        sinkVideoCR3.setName("cr3");
        add(sinkVideoCR3);
        link(sinkVideoCR3);
        sinkVideoCR3.syncStateWithParent();
        vc.getGUICR().showVideo3(videoComponentCR3);
        
        
        VideoComponent videoComponentCR4 = new VideoComponent();
        sinkVideoCR4 = videoComponentCR4.getElement();
        sinkVideoCR4.setName("cr4");
        add(sinkVideoCR4);
        link(sinkVideoCR4);
        sinkVideoCR4.syncStateWithParent();
        vc.getGUICR().showVideo4(videoComponentCR4);
        
        
        
        play();
        
    }
    
    //public int receiveFromUnicast(Element myRtpBin) {
    public int receiveFromUnicast(String senderIP) {
        // create the receiver bin
        //unicastReceiver = new AudioUnicastReceiver(adder,myRtpBin);
        unicastReceiver = new UnicastReceiver( senderIP, adderAudio,sinkVideo);
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
        User aRoom = new User("A Room", Config.ROOM_IP, Config.rtpaPortRoom, Config.rtcpasrcPortRoom,
                Config.rtpvPortRoom, Config.rtcpvsrcPortRoom);
        
        RoomReceiver room = new RoomReceiver(RECEIVER_ROOM_PREFIX + roomId,
        Config.ROOM_IP, ssrcToIgnore,sinkVideoCR1,sinkVideoCR2,sinkVideoCR3,sinkVideoCR4);
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
    

    
    
}
