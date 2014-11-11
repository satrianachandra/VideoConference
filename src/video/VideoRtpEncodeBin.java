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
    private Element resample;
    private Element encoder;
    private Element rtpPay;
    // helps to set the good stream parameters in this bin
    private Element capsFilter;

    private Pad sink;
    private Pad src;

    /**
     * Create and add all necessary stuff.
     */
    public VideoRtpEncodeBin() {
            super();

            queue = ElementFactory.make("queue", null);
            resample = ElementFactory.make("audioconvert", null);

            capsFilter = ElementFactory.make("capsfilter", null);
            capsFilter.set("caps", Caps.fromString("video/x-vp8"));

            // vp8 codec
            encoder = ElementFactory.make("vp8enc", null);
            
            rtpPay = ElementFactory.make("rtpvp8pay", null);

            this.addMany(queue, resample, capsFilter, encoder, rtpPay);
            Bin.linkMany(queue, resample, capsFilter, encoder, rtpPay);

            sink = new GhostPad("sink", queue.getStaticPad("sink"));
            sink.setActive(true);
            src = new GhostPad("src", rtpPay.getStaticPad("src"));
            src.setActive(true);

            this.addPad(sink);
            this.addPad(src);
    }


}
