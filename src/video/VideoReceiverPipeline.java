/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package video;

import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pipeline;
import util.Config;

/**
 *
 * @author chandra
 */
public class VideoReceiverPipeline extends Pipeline{
    
    VideoUnicastReceiver unicastReceiver = null;
    private static final String RECEIVER_ROOM_PREFIX = "receiver_room";
    
    //private final Element adder = ElementFactory.make("liveadder", null);
    //private final Element sink = ElementFactory.make("xvimagesink", null);
    private final Element sink = ElementFactory.make("autovideosink", null);
    
    public VideoReceiverPipeline(){
        super("video_receiver_pipeline");
        add(sink);
        link(sink);
        
        //addMany(adder, sink);
        //linkMany(adder, sink);

        
        play();
        
    }
    
    public int receiveFromUnicast() {
        // create the receiver bin
        unicastReceiver = new VideoUnicastReceiver(sink);
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


    public void receiveFromRoom(int roomId, long ssrcToIgnore) {
        // create the receiver bin
        VideoRoomReceiver room = new VideoRoomReceiver(RECEIVER_ROOM_PREFIX + roomId,
        Config.BASE_IP + roomId, Config.RTP_MULTICAST_PORT,
        ssrcToIgnore);
        // add it to this
        add(room);
        room.syncStateWithParent();
        // connect its output to the adder
        //room.link(adder);
    }
    
    public void stopRoomReceiving(int roomId) {
        ((VideoRoomReceiver) getElementByName(RECEIVER_ROOM_PREFIX + roomId))
        .getOut();
    }

}
