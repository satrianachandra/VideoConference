/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senderreceiverpipe;

import message.User;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.BaseSrc;
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
    
    
    private final Element tee = ElementFactory.make("tee", null);
    // THE SenderBin to talk with somebody
    
    private final Element teeV = ElementFactory.make("tee", null);
    // THE SenderBin to talk with somebody
    
    SenderBin unicastSender = null;
    
    public SenderPipeline(){
        super("audio_sender_pipeline");
        // live source => drop stream when in paused state
        src.setLive(true);
        
        addMany(src, tee);
        
        Util.doOrDie("src-tee", linkMany(src, tee));
        
        
        //Video
        srcV.set("device", "/dev/video0");
        srcV.setLive(true);
        addMany(srcV,teeV);
        Util.doOrDie("src-tee", linkMany(srcV, teeV));
        
        //
        

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
    
    public void stopStreamingToRoom(int roomId) {
        ((SenderBin) getElementByName(SENDER_ROOM_PREFIX + roomId)).getOut();
    }

    
    public void streamTo(User myUser,User destUser) {
        // create the sender bin
        unicastSender = new SenderBin(SENDER_UNICAST, myUser,destUser, false);
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
        
        play();
    }
    
    public void stopStreamingToUnicast() {
        if (unicastSender != null) {
                unicastSender.getOut();
        }
        unicastSender = null;
    }
    
}
