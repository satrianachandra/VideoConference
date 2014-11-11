/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package video;

import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pipeline;

/**
 *
 * @author chandra
 */
public class VideoReceiverPipeline extends Pipeline{
    
    VideoUnicastReceiver unicastReceiver = null;
    
    private final Element sink = ElementFactory.make("xvimagesink", null);
    
    public VideoReceiverPipeline(){
        super("video_receiver_pipeline");
        add(sink);
        link(sink);
        
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
}
