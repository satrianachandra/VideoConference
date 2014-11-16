/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package video;

import org.gstreamer.Bin;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.GhostPad;
import org.gstreamer.Pad;

/**
 *
 * @author chandra
 */
public class VideoRtpEncodeBin extends Bin{
    
    // queue to create a new thread for this branch
    private Element queue;
    // could be useful
    //private Element resample;
    
    
    private Element videoscale;
    private Element ffmpegcs;
    
    private Element encoder;
    private Element rtpPay;
    // helps to set the good stream parameters in this bin
    private Element capsFilter;

    //private final String VCAPS = "video/x-raw,width=352,height=288,framerate=15/1";
    private final String VCAPS = "video/x-raw-yuv, width=320, height=240,framerate=15/1";
    
    
    private Pad sink;
    private Pad src;

    /**
     * Create and add all necessary stuff.
     */
    public VideoRtpEncodeBin() {
            super();

            queue = ElementFactory.make("queue", null);
            
            videoscale= ElementFactory.make("videoscale", null);
            ffmpegcs = ElementFactory.make("ffmpegcolorspace", null);
            //resample = ElementFactory.make("audioconvert", null);

            capsFilter = ElementFactory.make("capsfilter", null);
            capsFilter.set("caps", Caps.fromString(VCAPS));

            // h264 coder
            encoder = ElementFactory.make("vp8enc", null);
            //encoder.set("speed", 7);
            //encoder.set("threads", 4);
            
            
            rtpPay = ElementFactory.make("rtpvp8pay", null);

            this.addMany(queue, videoscale, ffmpegcs, capsFilter, encoder, rtpPay);
            Bin.linkMany(queue, videoscale, ffmpegcs, capsFilter, encoder, rtpPay);

            sink = new GhostPad("sink", queue.getStaticPad("sink"));
            sink.setActive(true);
            src = new GhostPad("src", rtpPay.getStaticPad("src"));
            src.setActive(true);

            this.addPad(sink);
            this.addPad(src);
    }


}
