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
import org.gstreamer.PadLinkReturn;
import org.gstreamer.State;
import org.gstreamer.elements.FakeSink;
import util.Util;

/**
 *
 * @author chandra
 */
public class AudioRoomReceiver extends Bin{
    
    private Element udpSource;
    private Element rtpBin;
    /** to mix room's participants streams */
    private Element adder;
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
    public AudioRoomReceiver(String name, String ip, int port,
                   final long ssrcToIgnore) {
           super(name);

           // refer to GStreamer udpsrc plugin documentation
           udpSource = ElementFactory.make("udpsrc", null);
           udpSource.set("multicast-group", ip);
           udpSource.set("auto-multicast", true);
           udpSource.set("port", port);

           /*
            * set the caps from UDP, it flows downstream in the bin. Must match
            * what is sent by everyone in the room of course
            */
           Util.doOrDie("caps",
                            udpSource.getStaticPad("src").setCaps(
                                            Caps.fromString("application/x-rtp,"
                                                            + "media=(string)audio,"
                                                            + "clock-rate=(int)16000,"
                                                            + "encoding-name=(string)SPEEX, "
                                                            + "encoding-params=(string)1, "
                                                            + "payload=(int)110")));

            rtpBin = ElementFactory.make("gstrtpbin", null);
            adder = ElementFactory.make("liveadder", null);
           /*
            * when someone joins the room, a new SSRC appears on the stream and the
            * plugin gstrtpbin automatically demux this and creates the specific
            * pad
            */
           rtpBin.connect(new Element.PAD_ADDED() {
                @Override
                public synchronized void padAdded(Element element, Pad pad) {
                        // don't react to other pads than new sound input
                        if (pad.getName().startsWith("recv_rtp_src")) {
                            /*
                             * if the SSRC if this incoming new participant is mine,
                             * then connect to fakesink to prevent echo of my own voice.
                             * We must connect it to something otherwise sound will be
                             * pushed in the void and it isn't supported by gstreamer.
                             */
                            if (pad.getName().contains(String.valueOf(ssrcToIgnore))) {
                                Element fakesink = new FakeSink((String) null);
                                AudioRoomReceiver.this.add(fakesink);
                                fakesink.syncStateWithParent();

                                Util.doOrDie(
                                                "bin-fakesink",
                                                pad.link(fakesink.getStaticPad("sink")).equals(
                                                                PadLinkReturn.OK));
                            } else {
                                // create all the useful stuff for this new participant
                                AudioRtpDecodeBin decoder = new AudioRtpDecodeBin(true);

                                // add them
                                AudioRoomReceiver.this.add(decoder);

                                // sync them
                                decoder.syncStateWithParent();

                                // link them downstreamer and upstream
                                Util.doOrDie(
                                                "bin-decoder",
                                                pad.link(decoder.getStaticPad("sink")).equals(
                                                                PadLinkReturn.OK));

                                Pad adderPad = adder.getRequestPad("sink%d");
                                Util.doOrDie("decoder-adder",
                                                decoder.getStaticPad("src").link(adderPad)
                                                            .equals(PadLinkReturn.OK));
                            }
                        }
                }
           });

           // ############## ADD THEM TO PIPELINE ####################
           addMany(udpSource, rtpBin, adder);

           // Now they are in the pipeline, we can add the ghost pad
           src = new GhostPad("src", adder.getStaticPad("src"));
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
