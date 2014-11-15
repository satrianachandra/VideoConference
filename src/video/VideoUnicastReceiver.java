/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package video;

import javax.tools.Tool;
import org.gstreamer.Bin;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.GhostPad;
import org.gstreamer.Pad;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.State;
import util.Util;

/**
 *
 * @author chandra
 */
class VideoUnicastReceiver extends Bin{
    /** Name of _the_ unicast bin */
    private static final String RECEIVER_UNICAST = "receiver_unicast";
    private static final String VIDEO_CAPS="application/x-rtp, media=(string)video, clock-rate=(int)90000,encoding-name=(string)VP8-DRAFT-IETF-01,width=320, height=240";
    
    private Element rtpvsrc,rtcpvsrc,rtcpvsink;
    private Element rtpBin;
    private Pad src;
    
    private int port=0;
    
    //public VideoUnicastReceiver(final Element connectSrcTo,Element myRtpBin){
    public VideoUnicastReceiver(final Element connectSrcTo){
        rtpvsrc = ElementFactory.make("udpsrc", "rtpvsrc");
        //udpSource.set("port", 0); // ask for a port
        
        //for testing, make it static
        //int testing_receiver_port = 5050;
        rtpvsrc.set("port", 5050); // ask for a port
        rtpvsrc.getStaticPad("src").setCaps(Caps.fromString(VIDEO_CAPS));
        
        
        rtcpvsrc = ElementFactory.make("udpsrc", "rtcpvsrc");
        rtcpvsrc.set("port", 5051);
        
        rtcpvsink = ElementFactory.make("udpsink", "rtcpvsink");
        rtcpvsink.set("host", "127.0.0.1");
        rtcpvsink.set("port", 5052);
        rtcpvsink.set("async", false);
        rtcpvsink.set("sync", false);
        
        rtpBin = ElementFactory.make("gstrtpbin", null);
        rtpBin.set("latency", 400);
        //rtpBin = myRtpBin;
        
        rtpBin.connect(new Element.PAD_ADDED() {
            @Override
            public void padAdded(Element element, Pad pad) {
            if (pad.getName().startsWith("recv_rtp_src_0")) {
            // create elements
            VideoRtpDecodeBin decoder = new VideoRtpDecodeBin(false);
            // add them
            VideoUnicastReceiver.this.add(decoder);
            // sync them
            decoder.syncStateWithParent();
            // link them
            Util.doOrDie("pad_to_Decoder_sink",pad.link(decoder.getStaticPad("sink")).equals(
            PadLinkReturn.OK));
            /*
            * now that we have what we should connect to it, add the
            * ghost pad
            */
            src = new GhostPad("src", decoder.getStaticPad("src"));
            src.setActive(true);
            addPad(src);
            /*
            * connect this UnicastReceiver to the Element we've been
            * asked to do
            */
        
            Element.linkMany(VideoUnicastReceiver.this, connectSrcTo);
            }
            }
       });
       
        
        
        
        
        //add them to the pipeline
        addMany(rtcpvsink,rtcpvsrc,rtpvsrc, rtpBin);
        //link them
        Pad pad = rtpBin.getRequestPad("recv_rtp_sink_0");
        Util.doOrDie("rtpvsrc_to_rtpBin_recv_rtp_sink_0", 
                rtpvsrc.getStaticPad("src").link(pad).equals(PadLinkReturn.OK));
        
        Util.doOrDie("rtpvsrc_to_rtpBin_recv_rtcp_sink_0", 
                rtcpvsrc.getStaticPad("src").link(rtpBin.getRequestPad("recv_rtcp_sink_0")).equals(PadLinkReturn.OK));
        
        Util.doOrDie("rtpBin-rtcpvsink_sink", 
                rtpBin.getRequestPad("send_rtcp_src_0").link(rtcpvsink.getStaticPad("sink")).equals(PadLinkReturn.OK));
        
        /*
        * get this ready for playing, after this the UDP port will have been
        * assigned too
        */
        pause();
        port = (Integer) rtpvsrc.get("port");

        
    }
    
    public void getOut() {
        /*
        * if we were connected to something downstream (may haven't been the
        * cause if call was refused for example)
        */
        Pad downstreamPeer = null;
        if (src != null) {
            // before disconnecting, remember the request pad we were linked to
            downstreamPeer = src.getPeer();
        }
        this.setState(State.NULL);
        ((Bin) this.getParent()).remove(this);
        if (downstreamPeer != null) {
            // clean request pad from adder
            downstreamPeer.getParentElement().releaseRequestPad(downstreamPeer);
        }
    }

    public int getPort(){
        return this.port;
    }
    
    public void setPort(int portNumber){
        this.port = portNumber;
    }
    
}
