/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

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
public class AudioRtpEncodeBin extends Bin{
    
    private Element queue;
    // could be useful
    private Element convert;
    private Element resample;
    private Element encoder;
    private Element rtpPay;
    // helps to set the good stream parameters in this bin
    private Element capsFilter;
    private Pad sink;
    private Pad src;
    
    private final String ACAPS = "audio/x-raw-int,rate=16000";

    /**
     * Create and add all necessary stuff.
     */
    public AudioRtpEncodeBin() {
            super();

            queue = ElementFactory.make("queue", null);
            
            convert = ElementFactory.make("audioconvert", null);
            resample = ElementFactory.make("audioresample", null);
            
            capsFilter = ElementFactory.make("capsfilter", null);
            capsFilter.set("caps", Caps.fromString(ACAPS));

            // speex codec
            encoder = ElementFactory.make("alawenc", null);

            rtpPay = ElementFactory.make("rtppcmapay", null);

            this.addMany(queue, resample,convert, encoder, rtpPay);
            Bin.linkMany(queue, resample,convert, encoder, rtpPay);

            sink = new GhostPad("sink", queue.getStaticPad("sink"));
            sink.setActive(true);
            src = new GhostPad("src", rtpPay.getStaticPad("src"));
            src.setActive(true);

            this.addPad(sink);
            this.addPad(src);
    }


}
