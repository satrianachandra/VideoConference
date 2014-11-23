/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senderreceiverpipe;

import app.VideoConference;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import message.User;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.BaseSrc;
import org.gstreamer.swing.VideoComponent;
import util.Config;
import util.Util;

/**
 *
 * @author chandra
 */
public class SenderPipeline extends Pipeline{
    /** Name _the_ unicast sender bin */
    private static final String SENDER_UNICAST = "sender_unicast";
    /** Prefix to name the rooms bins */
    private static final String SENDER_ROOM_PREFIX = "sender_room";
    
    //from mic
    private BaseSrc src = (BaseSrc) ElementFactory.make("alsasrc", null);
    
    //from v4l2 (webcam)
    private BaseSrc srcV = (BaseSrc) ElementFactory.make("v4l2src", null);
    //private BaseSrc srcV = (BaseSrc) ElementFactory.make("videotestsrc", null);
    
    private final Element tee = ElementFactory.make("tee", "teeA");
    // THE SenderBin to talk with somebody
    
    private final Element teeV = ElementFactory.make("tee", "teeV");
    // THE SenderBin to talk with somebody
    
    private VideoConference vc;
    
    SenderBin unicastSender = null;
    VideoComponent videoComponent;
    Element myVideoSink;
    
    //VideoComponent videoComponentCR;
    //Element myVideoSinkCR;
    
    public SenderPipeline(VideoConference vc){
        
        super("audio_sender_pipeline");
        
        this.vc = vc;
        
        // live source => drop stream when in paused state
        src.setLive(true);
        addMany(src, tee);
        Util.doOrDie("src-tee", linkMany(src, tee));
        
        //Video
        srcV.set("device", "/dev/video0");
       // srcV.set("pattern", 1);
        srcV.setLive(true);
        addMany(srcV,teeV);
        Util.doOrDie("src-tee", linkMany(srcV, teeV));
        
        
        videoComponent = new VideoComponent();
        myVideoSink = videoComponent.getElement();
        myVideoSink.setName("vidUnicastSender");
        add(myVideoSink);
        myVideoSink.syncStateWithParent();
        
        /*
        videoComponentCR = new VideoComponent();
        myVideoSinkCR = videoComponentCR.getElement();
        myVideoSinkCR.setName("vidCR");
        add(myVideoSinkCR);
        myVideoSinkCR.syncStateWithParent();
        */
        
        //
        
        //
        
    }
    
    public Element getTeeV(){
        return teeV;
    }
    
    public long streamTo(int roomId) {
        /*
        // create the sender bin, name it after the room id
        SenderBin room = new SenderBin(SENDER_ROOM_PREFIX + roomId,
                        Config.BASE_IP + roomId, Config.RTP_MULTICAST_PORT, true);
        // add it to this
        add(room);
        room.syncStateWithParent();

        // connect its input to the tee
        Util.doOrDie("tee-roomSender",
                        tee.getRequestPad("src%d").link(room.getStaticPad("sink"))
                                        .equals(PadLinkReturn.OK));

        play();

        return room.getSSRC();
                */
        return -1;
    }
    
    public long streamToRoom(User myUser){
        int roomId = 1;
        //User aRoom = new User("A Room", Config.ROOM_IP, Config.rtpaPortRoom, Config.rtcpasrcPortRoom,
        //        Config.rtpvPortRoom, Config.rtcpvsrcPortRoom);
        SenderBin room = new SenderBin(SENDER_ROOM_PREFIX + roomId,
                        Config.ROOM_IP, true);
        // add it to this
        add(room);
        room.syncStateWithParent();

        // connect its input to the tee
        /*
        Util.doOrDie("tee-roomSender",
                        tee.getRequestPad("src%d").link(room.getStaticPad("sink"))
                                        .equals(PadLinkReturn.OK));
        */
        
        // connect its input to the tee
        Util.doOrDie(
                        "tee-unicastSender",
                        tee.getRequestPad("src%d")
                                        .link(room.getStaticPad("sinkA"))
                                        .equals(PadLinkReturn.OK));
        
        Util.doOrDie(
                        "teeV-unicastSender",
                        teeV.getRequestPad("src%d")
                                        .link(room.getStaticPad("sinkV"))
                                        .equals(PadLinkReturn.OK));
        
        //show my video
        Util.doOrDie(
                        "teeV-myVideoCR",
                        teeV.getRequestPad("src%d")
                                        .link(myVideoSink.getStaticPad("sink"))
                                        .equals(PadLinkReturn.OK));
        
        switch (vc.getMyPositionInCR()){
            case 0: vc.getGUICR().showVideo1(videoComponent);
                    break;
            case 1: vc.getGUICR().showVideo2(videoComponent);
                    break;
            case 2: vc.getGUICR().showVideo3(videoComponent);
                    break;
            case 3:vc.getGUICR().showVideo4(videoComponent);
                    break;
        }
        //////////
        

        play();

        return room.getSSRC();
    }
    
    public void stopStreamingToRoom(int roomId) {
        ((SenderBin) getElementByName(SENDER_ROOM_PREFIX + roomId)).getOut();
    }

    public void stopStreamingToRoom() {
        ((SenderBin) getElementByName(SENDER_ROOM_PREFIX + 1)).getOut();
    }
    
    public void streamTo(String destUserIP) {
        // create the sender bin
        unicastSender = new SenderBin(SENDER_UNICAST, destUserIP, false);
        
        // add it to this
        add(unicastSender);
        unicastSender.syncStateWithParent();

        // connect its input to the tee
        Util.doOrDie(
                        "tee-unicastSender",
                        tee.getRequestPad("src%d")
                                        .link(unicastSender.getStaticPad("sinkA"))
                                        .equals(PadLinkReturn.OK));
        
        Util.doOrDie(
                        "teeV-unicastSender",
                        teeV.getRequestPad("src%d")
                                        .link(unicastSender.getStaticPad("sinkV"))
                                        .equals(PadLinkReturn.OK));
        
        //show my video
        
        //
        
        Util.doOrDie(
                        "teeV-myVideo",
                        teeV.getRequestPad("src%d")
                                        .link(myVideoSink.getStaticPad("sink"))
                                        .equals(PadLinkReturn.OK));
        
        
        vc.getGUI().showMyVideo(videoComponent);
        //
        
        //
        
       /*
        MyVideoBin mvp =  new MyVideoBin("myvidbin",vc.getGUI().getMyVideoPanel(),vc.getGUI());
        add(mvp);
        mvp.syncStateWithParent();
         Util.doOrDie(
                        "teeV-myVideoPipe",
                        teeV.getRequestPad("src%d")
                                        .link(mvp.getStaticPad("sink"))
                                        .equals(PadLinkReturn.OK));
         
        */ 
        /*
        MyVideoBin mvp2 =  new MyVideoBin("myvidbin2",vc.getGUICR().getVideo1Panel(),vc.getGUICR());
        add(mvp2);
        mvp2.syncStateWithParent();
         Util.doOrDie(
                        "teeV-myVideoPipe",
                        teeV.getRequestPad("src%d")
                                        .link(mvp2.getStaticPad("sink"))
                                        .equals(PadLinkReturn.OK));
        
        */
        play();
    }
    
    public void stopStreamingToUnicast() {
        if (unicastSender != null) {
                unicastSender.getOut();
        }
        unicastSender = null;
    }
    
    
}
