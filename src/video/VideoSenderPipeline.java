/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package video;

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
public class VideoSenderPipeline extends Pipeline{
    /** Name _the_ unicast sender bin */
    private static final String SENDER_UNICAST = "sender_unicast";
    /** Prefix to name the rooms bins */
    private static final String SENDER_ROOM_PREFIX = "sender_room";
    
    private final BaseSrc src = (BaseSrc) ElementFactory.make("alsasrc", null);
    private final Element tee = ElementFactory.make("tee", null);
    // THE SenderBin to talk with somebody
    VideoSenderBin unicastSender = null;
    
    public VideoSenderPipeline(){
        super("video_sender_pipeline");
        // live source => drop stream when in paused state
        src.setLive(true);

        addMany(src, tee);
        Util.doOrDie("src-tee", linkMany(src, tee));

    }
    
    public long streamTo(int roomId) {
        // create the sender bin, name it after the room id
        VideoSenderBin room = new VideoSenderBin(SENDER_ROOM_PREFIX + roomId,
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
    }
    
    public void stopStreamingToRoom(int roomId) {
        ((VideoSenderBin) getElementByName(SENDER_ROOM_PREFIX + roomId)).getOut();
    }

    
    public void streamTo(String ip, int port) {
        // create the sender bin
        unicastSender = new VideoSenderBin(SENDER_UNICAST, ip, port, false);
        // add it to this
        add(unicastSender);
        unicastSender.syncStateWithParent();

        // connect its input to the tee
        Util.doOrDie(
                        "tee-unicastSender",
                        tee.getRequestPad("src%d")
                                        .link(unicastSender.getStaticPad("sink"))
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
