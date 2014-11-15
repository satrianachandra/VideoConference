/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package video;

import org.gstreamer.Bin;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.GhostPad;
import org.gstreamer.Pad;
import org.gstreamer.PadDirection;

/**
 *
 * @author chandra
 */
public class VideoRtpDecodeBin extends Bin{
    private Element rtpDepay;
    private Element decoder;
    private Element convert;
    private Pad sink;
    private Pad src;
    
    private Element videoscale;
    private Element ffmpegcs;
    
    public VideoRtpDecodeBin(boolean autoDisconnect) {
        super();
        
        
        // this is a vp8 encoded payload
        rtpDepay = ElementFactory.make("rtpvp8depay", null);
        // use vp8 codec
        decoder = ElementFactory.make("vp8dec", null);
        /////convert = ElementFactory.make("videoconvert", null);
        
        videoscale= ElementFactory.make("videoscale", null);
        ffmpegcs = ElementFactory.make("ffmpegcolorspace", null);
        
        this.addMany(rtpDepay, decoder, ffmpegcs,videoscale);
        Bin.linkMany(rtpDepay, decoder, ffmpegcs,videoscale);
        // create Bin's pads
        sink = new GhostPad("sink", rtpDepay.getStaticPad("sink"));
        src = new GhostPad("src", videoscale.getStaticPad("src"));
        this.addPad(sink);
        this.addPad(src);
        if (autoDisconnect) {
            // detect unlinking of sink pad (= upstream peer is gone)
            //this.sink.connect(new OnPadUnlinked(this));
        }
        
        
    }
    
    public void getOut() {
        // clean request pad from adder
        Pad downstreamPeer = src.getPeer();
        downstreamPeer.getParentElement().releaseRequestPad(downstreamPeer);
        ((Bin) this.getParent()).remove(this);
    }
    
    private class OnPadUnlinked implements GhostPad.UNLINKED {
    VideoRtpDecodeBin parentBin;
    
        @Override
        public void unlinked(Pad complainer, Pad gonePad) {
            if (gonePad.getDirection().equals(PadDirection.SRC)) {
                parentBin.getOut();
            }
        }
    }
    
}
