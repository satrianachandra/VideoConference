/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

import message.User;
import org.gstreamer.Bin;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.GhostPad;
import org.gstreamer.Pad;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.State;
import org.gstreamer.elements.FakeSink;
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
    
    private Element udpSource;
    private Element rtpBin;
    /** to mix room's participants streams */
    private Element adderAudio;
    private Pad src;
    
    /**
    * Create a new {@link RoomReceiver} and connect everything.
    * 
    * @param name
    *            Gstreamer element name
    * @param ip
    *            multicast IP (group) to join
    * @param port
    *            UDP port
    * @param ssrcToIgnore
    *            my SSRC as a sender, will be connected to a fakesink and not
    *            mixed with other participants
    */
    public RoomReceiver(String name, User myUser,User room,final long ssrcToIgnore) {
           super(name);

           //For Audio
           rtpasrc = ElementFactory.make("udpsrc", "rtpasrc");
           rtpasrc.set("multicast-group", room.getIpAddress());
           rtpasrc.set("auto-multicast", true);
           rtpasrc.set("port", room.getrtpaPort());
           rtpasrc.getStaticPad("src").setCaps(Caps.fromString(AUDIO_CAPS));
           
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
                            if (pad.getName().contains(String.valueOf(ssrcToIgnore))) {
                                Element fakesink = new FakeSink((String) null);
                                RoomReceiver.this.add(fakesink);
                                fakesink.syncStateWithParent();

                                Util.doOrDie(
                                                "bin-fakesinkAudio",
                                                pad.link(fakesink.getStaticPad("sink")).equals(
                                                                PadLinkReturn.OK));
                            } else {
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
                            }
                        }
                }
           });
           
           ///////////Video
           rtpvsrc = ElementFactory.make("udpsrc", "rtpvsrc");
           rtpvsrc.set("port", myUser.getrtpvPort()); // ask for a port
           System.out.println("port rtpvsrc "+myUser.getrtpvPort());
           rtpvsrc.getStaticPad("src").setCaps(Caps.fromString(VIDEO_CAPS));
        
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
                            if (pad.getName().contains(String.valueOf(ssrcToIgnore))) {
                                Element fakesink = new FakeSink("fakeSinkVideo");
                                RoomReceiver.this.add(fakesink);
                                fakesink.syncStateWithParent();

                                Util.doOrDie(
                                                "bin-fakesinkVideo",
                                                pad.link(fakesink.getStaticPad("sink")).equals(
                                                                PadLinkReturn.OK));
                            } else {
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

                                Pad adderAudioPad = adderAudio.getRequestPad("sink%d");
                                Util.doOrDie("decoder-adder",
                                                decoderVideo.getStaticPad("src").link(adderAudioPad)
                                                            .equals(PadLinkReturn.OK));
                            }
                        }
                }
           });
 
           
           //////////////////////////

           // ############## ADD THEM TO PIPELINE ####################
           addMany(udpSource, rtpBin, adderAudio);

           // Now they are in the pipeline, we can add the ghost pad
           src = new GhostPad("src", adderAudio.getStaticPad("src"));
           addPad(src);

           // ###################### LINK THEM ##########################
           Pad pad = rtpBin.getRequestPad("recv_rtp_sink_1");
           Util.doOrDie("udpSource-rtpbin", udpSource.getStaticPad("src")
                           .link(pad).equals(PadLinkReturn.OK));

           // get this ready for playing
           pause();
   }
    
   public void getOut() {
        // clean request pad from adder
        Pad downstreamPeer = src.getPeer();

        this.setState(State.NULL);

        ((Bin) this.getParent()).remove(this);

        downstreamPeer.getParentElement().releaseRequestPad(downstreamPeer);
    }

}
