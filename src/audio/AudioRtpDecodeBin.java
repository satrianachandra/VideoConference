/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

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
class AudioRtpDecodeBin extends Bin{
    private final Element rtpDepay;
    private final Element decoder;
    private final Element convert;
    private final Element resample;
    private final Pad sink;
    private final Pad src;
    
    public AudioRtpDecodeBin(boolean autoDisconnect) {
        super();
        
        // this is a speex encoded payload
        rtpDepay = ElementFactory.make("rtppcmadepay", null);
        // use speex codec
        decoder = ElementFactory.make("alawdec", null);
        convert = ElementFactory.make("audioconvert", null);
        resample = ElementFactory.make("audioresample", null);

        this.addMany(rtpDepay, decoder, convert,resample);
        Bin.linkMany(rtpDepay, decoder, convert,resample);

        // create Bin's pads
        sink = new GhostPad("sink", rtpDepay.getStaticPad("sink"));
        src = new GhostPad("src", resample.getStaticPad("src"));

        this.addPad(sink);
        this.addPad(src);

        if (autoDisconnect) {
                // detect unlinking of sink pad (= upstream peer is gone)
              //  this.sink.connect(new OnPadUnlinked(this));
        }
        
    }
    
    public void getOut() {
        // clean request pad from adder
        Pad downstreamPeer = src.getPeer();
        downstreamPeer.getParentElement().releaseRequestPad(downstreamPeer);
        ((Bin) this.getParent()).remove(this);
    }
    
    private class OnPadUnlinked implements GhostPad.UNLINKED {
    AudioRtpDecodeBin parentBin;
    
        @Override
        public void unlinked(Pad complainer, Pad gonePad) {
            if (gonePad.getDirection().equals(PadDirection.SRC)) {
                parentBin.getOut();
            }
        }
    }
    
}
