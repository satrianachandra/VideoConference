/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senderreceiverpipe;

import audio.AudioRoomReceiver;
import message.User;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pipeline;
import util.Config;

/**
 *
 * @author chandra
 */
public class ReceiverPipeline extends Pipeline{
    
    UnicastReceiver unicastReceiver = null;
    private static final String RECEIVER_ROOM_PREFIX = "receiver_room";
    
    private final Element adder = ElementFactory.make("liveadder", null);
    private final Element sink = ElementFactory.make("autoaudiosink", null);
    
    private final Element sinkV = ElementFactory.make("autovideosink", null);
    
    public ReceiverPipeline(){
        super("audio_receiver_pipeline");
        //add(sink);
        //link(sink);
        //sink.set("sync", true);
        addMany(adder, sink);
        linkMany(adder, sink);

        //video
        add(sinkV);
        link(sinkV);
        
        
        play();
        
    }
    
    //public int receiveFromUnicast(Element myRtpBin) {
    public int receiveFromUnicast(User myUser,User senderUser) {
        // create the receiver bin
        //unicastReceiver = new AudioUnicastReceiver(adder,myRtpBin);
        unicastReceiver = new UnicastReceiver(myUser,senderUser, adder,sinkV);
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
        AudioRoomReceiver room = new AudioRoomReceiver(RECEIVER_ROOM_PREFIX + roomId,
        Config.BASE_IP + roomId, Config.RTP_MULTICAST_PORT,
        ssrcToIgnore);
        // add it to this
        add(room);
        room.syncStateWithParent();
        // connect its output to the adder
        //room.link(adder);
    }
    
    public void stopRoomReceiving(int roomId) {
        ((AudioRoomReceiver) getElementByName(RECEIVER_ROOM_PREFIX + roomId))
        .getOut();
    }

}
