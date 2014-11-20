/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senderreceiverpipe;

import audio.AudioRtpEncodeBin;
import java.util.List;
import message.User;
import org.gstreamer.Bin;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.GhostPad;
import org.gstreamer.Pad;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.State;
import org.gstreamer.elements.Tee;
import org.gstreamer.elements.good.RTPBin;
import util.Config;
import util.Util;
import video.VideoRtpEncodeBin;

/**
 *
 * @author chandra
 */
public class SenderBin extends Bin{
    private Pad sink;
    private AudioRtpEncodeBin encoder;
    private Element rtpasink,rtcpasink,rtcpasrc;
    private RTPBin rtpBin;
    
    private Pad sinkV;
    private VideoRtpEncodeBin encoderV;
    //private Element udpSink;
    private Element rtpvsink,rtcpvsink,rtcpvsrc;
    
    
    
    
    public SenderBin(String name, String destUserIP, boolean multicast){
        super(name);   
        
        encoder = new AudioRtpEncodeBin();
        encoder.syncStateWithParent();
        rtpBin = new RTPBin((String) null);
        rtpBin.set("latency", 1000);
        rtpBin.set("use-pipeline-clock",true);
        
        // asking this put the gstrtpbin plugin in sender mode
        Pad rtpSink1 = rtpBin.getRequestPad("send_rtp_sink_1");

        rtpasink = ElementFactory.make("udpsink", "rtpasink");
        rtpasink.set("host", destUserIP);
        System.out.println("hostrtpasink destuser "+destUserIP);
        rtpasink.set("port", Config.rtpaPort);
        System.out.println("port rtpasink "+Config.rtpaPort);
        
        rtpasink.set("async", false);
        rtpasink.set("sync",false);

        rtcpasink = ElementFactory.make("udpsink", "rtcpasink");
        rtcpasink.set("host", destUserIP);
        rtcpasink.set("port", Config.rtcpasrcPort);
        System.out.println("port rtcpasink"+Config.rtcpasrcPort);
        rtcpasink.set("async", false);
        rtcpasink.set("sync", false);
        
        rtcpasrc = ElementFactory.make("udpsrc", "rtcpasrc");
        rtcpasrc.set("port", Config.rtcpasrcPort);
        System.out.println("port rtcpasrc"+Config.rtcpasrcPort);
        
        if (multicast) {
                // make OS automatically join multicast group
                rtpasink.set("auto-multicast", true);
                rtcpasink.set("auto-multicast", true);
                rtcpasrc.set("auto-multicast", true);
        }
        
        /////////////Video
        encoderV = new VideoRtpEncodeBin();
        encoderV.syncStateWithParent();
        // asking this put the gstrtpbin plugin in sender mode
        Pad rtpSink0 = rtpBin.getRequestPad("send_rtp_sink_0");

        rtpvsink = ElementFactory.make("udpsink", "rtpvsink");
        rtpvsink.set("host", destUserIP);
        System.out.println("host destuser "+ destUserIP);
        rtpvsink.set("port", Config.rtpvPort);
        System.out.println("port rtpvsink "+Config.rtpvPort);
        
        rtpvsink.set("async", false);
        rtpvsink.set("sync", false);

        rtcpvsink = ElementFactory.make("udpsink", "rtcpvsink");
        rtcpvsink.set("host", destUserIP);
        System.out.println("hostdestuser rtcpvsink"+ destUserIP);
        rtcpvsink.set("port", Config.rtcpvsrcPort);
        System.out.println("port rtcpvsink"+ Config.rtcpvsrcPort);
        rtcpvsink.set("async", false);
        rtcpvsink.set("sync", false);
        
        rtcpvsrc = ElementFactory.make("udpsrc", "rtcpvsrc");
        rtcpvsrc.set("port", Config.rtcpvsrcPort);
        System.out.println("port rtcpvsrc "+Config.rtcpvsrcPort);
        if (multicast) {
                // make OS automatically join multicast group
                rtpvsink.set("auto-multicast", true);
                rtcpvsink.set("auto-multicast", true);
                rtcpvsrc.set("auto-multicast", true);
        }
        
        
        //////////////
        
        // ############## ADD THEM TO PIPELINE ####################
        addMany(encoder, rtpBin, rtpasink,rtcpasink,rtcpasrc, encoderV,rtpvsink,rtcpvsink,rtcpvsrc);
        
        // ###################### LINK THEM ##########################
        sink = new GhostPad("sinkA", encoder.getStaticPad("sink"));
        sink.setActive(true);
        addPad(sink);

        Util.doOrDie(
                        "encoder-rtpBin",
                        encoder.getStaticPad("src").link(rtpSink1)
                                        .equals(PadLinkReturn.OK));
        Util.doOrDie(
                        "rtpbin-rtpbin_send_rtp_src_1",
                        rtpBin.getStaticPad("send_rtp_src_1")
                                        .link(rtpasink.getStaticPad("sink"))
                                        .equals(PadLinkReturn.OK));
        
        
        Util.doOrDie(
                        "rtpbin_send_rtcp_src_1-rtcpasink",
                        rtpBin.getRequestPad("send_rtcp_src_1")
                                        .link(rtcpasink.getStaticPad("sink"))
                                        .equals(PadLinkReturn.OK));
        
        Util.doOrDie(
                        "rtcpasrc-rtpBin_rec_rtcp_sink_1",
                        rtcpasrc.getStaticPad("src")
                                        .link(rtpBin.getRequestPad("recv_rtcp_sink_1"))
                                        .equals(PadLinkReturn.OK));
        
        
        ///Video
        sinkV = new GhostPad("sinkV", encoderV.getStaticPad("sink"));
        sinkV.setActive(true);
        addPad(sinkV);

        Util.doOrDie(
                        "encoderV-rtpBin",
                        encoderV.getStaticPad("src").link(rtpSink0)
                                        .equals(PadLinkReturn.OK));
        Util.doOrDie(
                        "rtpbin-udpSink",
                        rtpBin.getStaticPad("send_rtp_src_0")
                                        .link(rtpvsink.getStaticPad("sink"))
                                        .equals(PadLinkReturn.OK));
    
        Util.doOrDie(
                        "rtpbin_send_rtcp_src_0-rtcpvsink",
                        rtpBin.getRequestPad("send_rtcp_src_0")
                                        .link(rtcpvsink.getStaticPad("sink"))
                                        .equals(PadLinkReturn.OK));
        
        Util.doOrDie(
                        "rtcpvsrc-rtpBin_rec_rtcp_sink_0",
                        rtcpvsrc.getStaticPad("src")
                                        .link(rtpBin.getRequestPad("recv_rtcp_sink_0"))
                                        .equals(PadLinkReturn.OK));
        ///////
        
    
    }
    
    private Element getElementByNameStartingWith(List<Element> elts,
    String start) {
        Element ret = null;

        for (Element elt : elts) {
                if (elt.getName().startsWith(start)) {
                        return elt;
                }
        }

        return ret;
    }
    
    public Long getSSRC() {
        /*
         * we dig to find it in sink pad's caps by parsing these as a string,
         * didn't find any better way
         */
        String caps = getElementByNameStartingWith(rtpBin.getElements(),
                        "rtpsession").getSinkPads().get(0).getCaps().toString();
        int ssrcBegin = caps.indexOf("ssrc=(uint)") + 11;
        int ssrcEnd = caps.indexOf(";", ssrcBegin);
        return new Long(caps.substring(ssrcBegin, ssrcEnd));
    }
    
    
    public void getOut() {
        // clean request pad from adder
        Pad upstreamPeer = sink.getPeer();
        Tee teeUpstream = ((Tee) sink.getPeer().getParent());
        Bin parentBin = ((Bin) this.getParent());

        upstreamPeer.setBlocked(true);

        this.setState(State.NULL);

        ((Bin) this.getParent()).remove(this);

        /*
         * if upstream tee has no src anymore, the pipeline will push in the
         * void and crash, thus we avoid it by stopping the whole bin
         */
        if (teeUpstream.getSrcPads().size() == 1) {
                parentBin.setState(State.NULL);
        }
        teeUpstream.releaseRequestPad(upstreamPeer);
    }
    
    
}
