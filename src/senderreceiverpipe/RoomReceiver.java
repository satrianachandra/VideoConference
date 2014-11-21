/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senderreceiverpipe;

import app.VideoConference;
import audio.AudioRtpDecodeBin;
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
public class RoomReceiver extends Bin{
    
    private static final String AUDIO_CAPS="application/x-rtp,media=(string)audio,clock-rate=(int)8000,encoding-name=(string)PCMA";
    private Element rtpasrc,rtcpasrc,rtcpasink;
    
    private static final String VIDEO_CAPS="application/x-rtp, media=(string)video, clock-rate=(int)90000,encoding-name=(string)VP8-DRAFT-IETF-01,width=320, height=240";
    private Element rtpvsrc,rtcpvsrc,rtcpvsink;
    
    //private Element udpSource;
    private Element rtpBin;
    /** to mix room's participants streams */
    private Element adderAudio;
    private Pad src;
    
    private int count = 0;
    
    public RoomReceiver(String name,String mutlicastIP,final long ssrcToIgnore, VideoConference vc) {
           super(name);

           //For Audio
           rtpasrc = ElementFactory.make("udpsrc", "rtpasrc");
           rtpasrc.set("multicast-group", mutlicastIP);
           rtpasrc.set("auto-multicast", true);
           rtpasrc.set("port", Config.rtpaPort);
           rtpasrc.getStaticPad("src").setCaps(Caps.fromString(AUDIO_CAPS));
           
           //rtcp
           rtcpasrc = ElementFactory.make("udpsrc", "rtcpasrc");
        rtcpasrc.set("port", Config.rtcpasrcPort);
        System.out.println("port rtcpasrc "+  Config.rtcpasrcPort);
        
        rtcpasink = ElementFactory.make("udpsink", "rtcpasink");
        rtcpasink.set("host", mutlicastIP);
        System.out.println("host rtcpasink "+ mutlicastIP);
        rtcpasink.set("port", Config.rtcpasrcPort);
        System.out.println("port rtcpasink"+ Config.rtcpasrcPort);
        rtcpasink.set("async", false);
        rtcpasink.set("sync", false);
        
        
           //
           
           
            rtpBin = ElementFactory.make("gstrtpbin", null);
            rtpBin.set("latency", 1000);
            rtpBin.set("use-pipeline-clock",true);
        
            
            
            adderAudio = ElementFactory.make("liveadder", null);
           /*
            * when someone joins the room, a new SSRC appears on the stream and the
            * plugin gstrtpbin automatically demux this and creates the specific
            * pad
            */
           rtpBin.connect(new Element.PAD_ADDED() {
                @Override
                public synchronized void padAdded(Element element, Pad pad) {
                        // don't react to other pads than new sound input
                        if (pad.getName().startsWith("recv_rtp_src_1")) {
                            /*
                             * if the SSRC if this incoming new participant is mine,
                             * then connect to fakesink to prevent echo of my own voice.
                             * We must connect it to something otherwise sound will be
                             * pushed in the void and it isn't supported by gstreamer.
                             */
                            /*
                            if (pad.getName().contains(String.valueOf(ssrcToIgnore))) {
                                Element fakesink = new FakeSink((String) null);
                                RoomReceiver.this.add(fakesink);
                                fakesink.syncStateWithParent();

                                Util.doOrDie(
                                                "bin-fakesinkAudio",
                                                pad.link(fakesink.getStaticPad("sink")).equals(
                                                                PadLinkReturn.OK));
                            } else {
                            */
                                // create all the useful stuff for this new participant
                                AudioRtpDecodeBin decoderAudio = new AudioRtpDecodeBin(true);

                                // add them
                                RoomReceiver.this.add(decoderAudio);

                                // sync them
                                decoderAudio.syncStateWithParent();

                                // link them downstreamer and upstream
                                Util.doOrDie(
                                                "bin-decoderAudio",
                                                pad.link(decoderAudio.getStaticPad("sink")).equals(
                                                                PadLinkReturn.OK));

                                Pad adderAudioPad = adderAudio.getRequestPad("sink%d");
                                Util.doOrDie("decoder-adderAudio",
                                                decoderAudio.getStaticPad("src").link(adderAudioPad)
                                                            .equals(PadLinkReturn.OK));
                            //}
                        }
                }
           });
           
           ///////////Video
           rtpvsrc = ElementFactory.make("udpsrc", "rtpvsrc");
           rtpvsrc.set("port", Config.rtpvPort); // ask for a port
           System.out.println("port rtpvsrc "+Config.rtpvPort);
           rtpvsrc.getStaticPad("src").setCaps(Caps.fromString(VIDEO_CAPS));
           
           rtcpvsrc = ElementFactory.make("udpsrc", "rtcpvsrc");
        rtcpvsrc.set("port", Config.rtcpvsrcPort);
        System.out.println("rtcpvsrc port: "+Config.rtcpvsrcPort);
        
        rtcpvsink = ElementFactory.make("udpsink", "rtcpvsink");
        rtcpvsink.set("host", mutlicastIP);
        System.out.println("host rtcpvsink "+mutlicastIP);
        rtcpvsink.set("port", Config.rtcpvsrcPort);
        System.out.println("port rtcpvsink "+Config.rtcpvsrcPort);
        rtcpvsink.set("async", false);
        rtcpvsink.set("sync", false);
        
           
           rtpBin.connect(new Element.PAD_ADDED() {
                @Override
                public synchronized void padAdded(Element element, Pad pad) {
                        // don't react to other pads than new sound input
                        if (pad.getName().startsWith("recv_rtp_src_0")) {
                            /*
                             * if the SSRC if this incoming new participant is mine,
                             * then connect to fakesink to prevent echo of my own voice.
                             * We must connect it to something otherwise sound will be
                             * pushed in the void and it isn't supported by gstreamer.
                             */
                            
                            /*
                            if (pad.getName().contains(String.valueOf(ssrcToIgnore))) {
                                Element fakesink = new FakeSink("fakeSinkVideo");
                                RoomReceiver.this.add(fakesink);
                                fakesink.syncStateWithParent();

                                Util.doOrDie(
                                                "bin-fakesinkVideo",
                                                pad.link(fakesink.getStaticPad("sink")).equals(
                                                                PadLinkReturn.OK));
                            } else {
                            */
                                    // create all the useful stuff for this new participant
                                VideoRtpDecodeBin decoderVideo = new VideoRtpDecodeBin(true);

                                // add them
                                RoomReceiver.this.add(decoderVideo);

                                // sync them
                                decoderVideo.syncStateWithParent();

                                // link them downstreamer and upstream
                                Util.doOrDie(
                                                "bin-decoder",
                                                pad.link(decoderVideo.getStaticPad("sink")).equals(
                                                                PadLinkReturn.OK));

                                //Pad srcVideo = new GhostPad("srcVideo", decoderVideo.getStaticPad("src"));
                                //srcVideo.setActive(true);
                               // addPad(srcVideo);
                                
                                switch(count){
                                    case 0: //Util.doOrDie("decoder-srcVideo1",
                                            //    Element.linkMany(RoomReceiver.this,video1));
                                        ////////
                                        MyVideoBin mvp =  new MyVideoBin("myvidbin",vc.getGUICR().
                                                getVideo1Panel(),vc.getGUICR());
                                        add(mvp);
                                        mvp.syncStateWithParent();
                                         Util.doOrDie(
                                                        "teeV-myVideoPipe",
                                                        decoderVideo.getStaticPad("src")
                                                                .link(mvp.getStaticPad("sink"))
                                                                .equals(PadLinkReturn.OK));
                                        /////////
                                            break;
                                    case 1://Util.doOrDie("decoder-srcVideo2",
                                           //     Element.linkMany(RoomReceiver.this,video2));
                                         MyVideoBin mvp2 =  new MyVideoBin("myvidbin2",vc.getGUICR().
                                                getVideo2Panel(),vc.getGUICR());
                                        add(mvp2);
                                        mvp2.syncStateWithParent();
                                         Util.doOrDie(
                                                        "teeV-myVideoPipe",
                                                        decoderVideo.getStaticPad("src")
                                                                .link(mvp2.getStaticPad("sink"))
                                                                .equals(PadLinkReturn.OK));
                                            break;
                                    case 2://Util.doOrDie("decoder-srcVideo3",
                                           //     Element.linkMany(RoomReceiver.this,video3));
                                            MyVideoBin mvp3 =  new MyVideoBin("myvidbin3",vc.getGUICR().
                                                getVideo3Panel(),vc.getGUICR());
                                        add(mvp3);
                                        mvp3.syncStateWithParent();
                                         Util.doOrDie(
                                                        "teeV-myVideoPipe",
                                                        decoderVideo.getStaticPad("src")
                                                                .link(mvp3.getStaticPad("sink"))
                                                                .equals(PadLinkReturn.OK));
                                        
                                            break;
                                    case 3://Util.doOrDie("decoder-srcVideo4",
                                           //     Element.linkMany(RoomReceiver.this,video4));
                                        MyVideoBin mvp4 =  new MyVideoBin("myvidbin4",vc.getGUICR().
                                                getVideo4Panel(),vc.getGUICR());
                                        add(mvp4);
                                        mvp4.syncStateWithParent();
                                         Util.doOrDie(
                                                        "teeV-myVideoPipe",
                                                        decoderVideo.getStaticPad("src")
                                                                .link(mvp4.getStaticPad("sink"))
                                                                .equals(PadLinkReturn.OK));
                                        
                                            break;
                              //  }
                              //  count++;
                            }
                                 count++;
                        }
                }
           });
 
           
           //////////////////////////

           //add them to the pipeline
           System.out.println("rr1");
           addMany(rtcpasink,rtcpasrc,rtpasrc, rtpBin,adderAudio);
           //addMany(rtpasrc, rtpBin,adderAudio);
           System.out.println("rr2");
           addMany(rtcpvsink,rtcpvsrc,rtpvsrc);
           //addMany(rtpvsrc);
           System.out.println("rr3");
           // Now they are in the pipeline, we can add the ghost pad
           src = new GhostPad("src", adderAudio.getStaticPad("src"));
           addPad(src);

           // ###################### LINK THEM ##########################
           //audio
           Pad padA = rtpBin.getRequestPad("recv_rtp_sink_1");
           Util.doOrDie("rtpasrc-rtpbin_recv_rtp_sink_1", rtpasrc.getStaticPad("src")
                           .link(padA).equals(PadLinkReturn.OK));

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
        
           
           // get this ready for playing
           pause();
   }
    
   public void getOut() {
        // cflean request pad from adder
        Pad downstreamPeer = src.getPeer();

        this.setState(State.NULL);

        ((Bin) this.getParent()).remove(this);

        downstreamPeer.getParentElement().releaseRequestPad(downstreamPeer);
    }

}
