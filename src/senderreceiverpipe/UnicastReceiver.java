/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senderreceiverpipe;


import audio.AudioRtpDecodeBin;
import message.User;
import org.gstreamer.Bin;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.GhostPad;
import org.gstreamer.Pad;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.State;
import util.Config;
import util.Util;

import video.VideoRtpDecodeBin;


/**
 *
 * @author chandra
 */
class UnicastReceiver extends Bin{
    /** Name of _the_ unicast bin */
    private static final String RECEIVER_UNICAST = "receiver_unicast";
    private static final String AUDIO_CAPS="application/x-rtp,media=(string)audio,clock-rate=(int)8000,encoding-name=(string)PCMA";
    
    
    private Element rtpasrc,rtcpasrc,rtcpasink;
    private Element rtpBin;
    private Pad src;
    
    private int port=0;
    
    
    //video
    private static final String VIDEO_CAPS="application/x-rtp, media=(string)video, clock-rate=(int)90000,encoding-name=(string)VP8-DRAFT-IETF-01,width=320, height=240";
    private Element rtpvsrc,rtcpvsrc,rtcpvsink;
    private Pad srcV;
    ////
    
    //public AudioUnicastReceiver(final Element connectSrcTo,Element myRtpBin ){
    public UnicastReceiver(String senderIP, final Element connectSrcTo,final Element connectSrcToV){
        rtpasrc = ElementFactory.make("udpsrc", "rtpasrc");
        //udpSource.set("port", 0); // ask for a port
        
        //for testing, make it static
        rtpasrc.set("port", Config.rtcpasrcPort); // ask for a port
        rtpasrc.getStaticPad("src").setCaps(Caps.fromString(AUDIO_CAPS));
        System.out.println("port rtpasrc "+  Config.rtcpasrcPort);
        
        rtcpasrc = ElementFactory.make("udpsrc", "rtcpasrc");
        rtcpasrc.set("port", Config.rtcpasrcPort);
        System.out.println("port rtcpasrc "+  Config.rtcpasrcPort);
        
        rtcpasink = ElementFactory.make("udpsink", "rtcpasink");
        rtcpasink.set("host", senderIP);
        System.out.println("host rtcpasink "+ senderIP);
        rtcpasink.set("port", Config.rtcpasrcPort);
        System.out.println("port rtcpasink"+ Config.rtcpasrcPort);
        rtcpasink.set("async", false);
        rtcpasink.set("sync", false);
        
        
        rtpBin = ElementFactory.make("gstrtpbin", null);
        rtpBin.set("latency", 1000);
        rtpBin.set("use-pipeline-clock",true);
        
        rtpBin.connect(new Element.PAD_ADDED() {
            @Override
            public void padAdded(Element element, Pad pad) {
            if (pad.getName().startsWith("recv_rtp_src_1")) {
            // create elements
            AudioRtpDecodeBin decoder = new AudioRtpDecodeBin(false);
            // add them
            UnicastReceiver.this.add(decoder);
            // sync them
            decoder.syncStateWithParent();
            // link them
            Util.doOrDie("pad_to_Decoder_sink",pad.link(decoder.getStaticPad("sink")).equals(
            PadLinkReturn.OK));
            /*
            * now that we have what we should connect to it, add the
            * ghost pad
            */
            src = new GhostPad("srcA", decoder.getStaticPad("src"));
            src.setActive(true);
            addPad(src);
            /*
            * connect this UnicastReceiver to the Element we've been
            * asked to do
            */
        
            Util.doOrDie("unicastreceiver-connectsrcto", Element.linkMany(UnicastReceiver.this, connectSrcTo));
            }
            }
       });
       
        //////Video
        rtpvsrc = ElementFactory.make("udpsrc", "rtpvsrc");
        //udpSource.set("port", 0); // ask for a port
        
        //for testing, make it static
        //int testing_receiver_port = 5050;
        rtpvsrc.set("port", Config.rtpvPort); // ask for a port
        System.out.println("port rtpvsrc "+Config.rtpvPort);
        rtpvsrc.getStaticPad("src").setCaps(Caps.fromString(VIDEO_CAPS));
        
        
        rtcpvsrc = ElementFactory.make("udpsrc", "rtcpvsrc");
        rtcpvsrc.set("port", Config.rtcpvsrcPort);
        System.out.println("rtcpvsrc port: "+Config.rtcpvsrcPort);
        
        rtcpvsink = ElementFactory.make("udpsink", "rtcpvsink");
        rtcpvsink.set("host", senderIP);
        System.out.println("host rtcpvsink "+senderIP);
        rtcpvsink.set("port", Config.rtcpvsrcPort);
        System.out.println("port rtcpvsink "+Config.rtcpvsrcPort);
        rtcpvsink.set("async", false);
        rtcpvsink.set("sync", false);
        
        rtpBin.connect(new Element.PAD_ADDED() {
            @Override
            public void padAdded(Element element, Pad pad) {
            if (pad.getName().startsWith("recv_rtp_src_0")) {
            // create elements
            VideoRtpDecodeBin decoder = new VideoRtpDecodeBin(false);
            // add them
            UnicastReceiver.this.add(decoder);
            // sync them
            decoder.syncStateWithParent();
            // link them
            Util.doOrDie("pad_to_Decoder_sink",pad.link(decoder.getStaticPad("sink")).equals(
            PadLinkReturn.OK));
            /*
            * now that we have what we should connect to it, add the
            * ghost pad
            */
            srcV = new GhostPad("srcV", decoder.getStaticPad("src"));
            srcV.setActive(true);
            addPad(srcV);
            /*
            * connect this UnicastReceiver to the Element we've been
            * asked to do
            */
        
            Element.linkMany(UnicastReceiver.this, connectSrcToV);
            }
            }
        });
       
        
        ///////////////
        
        
        
        
        //add them to the pipeline
        addMany(rtcpasink,rtcpasrc,rtpasrc, rtpBin);
        addMany(rtcpvsink,rtcpvsrc,rtpvsrc);
        //link them
        Pad pad = rtpBin.getRequestPad("recv_rtp_sink_1");
        Util.doOrDie("udp_src_to_rtpBin_recv_rtp_sink_1", rtpasrc.getStaticPad("src").link(pad).equals(PadLinkReturn.OK));
        
        Util.doOrDie("rtpvsrc_to_rtpBin_recv_rtcp_sink_1", 
                rtcpasrc.getStaticPad("src").link(rtpBin.getRequestPad("recv_rtcp_sink_1")).equals(PadLinkReturn.OK));
        
        Util.doOrDie("rtpBin-rtcpvsink_sink", 
                rtpBin.getRequestPad("send_rtcp_src_1").link(rtcpasink.getStaticPad("sink")).equals(PadLinkReturn.OK));
        
        
        //video
        //link them
        Pad padV = rtpBin.getRequestPad("recv_rtp_sink_0");
        Util.doOrDie("rtpvsrc_to_rtpBin_recv_rtp_sink_0", 
                rtpvsrc.getStaticPad("src").link(padV).equals(PadLinkReturn.OK));
        
        Util.doOrDie("rtpvsrc_to_rtpBin_recv_rtcp_sink_0", 
                rtcpvsrc.getStaticPad("src").link(rtpBin.getRequestPad("recv_rtcp_sink_0")).equals(PadLinkReturn.OK));
        
        Util.doOrDie("rtpBin-rtcpvsink_sink", 
                rtpBin.getRequestPad("send_rtcp_src_0").link(rtcpvsink.getStaticPad("sink")).equals(PadLinkReturn.OK));
        
        ////////////
        
        
        /*
        * get this ready for playing, after this the UDP port will have been
        * assigned too
        */
        pause();
        port = (Integer) rtpasrc.get("port");

        
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
