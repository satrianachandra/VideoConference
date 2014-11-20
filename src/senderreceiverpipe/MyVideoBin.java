/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senderreceiverpipe;

import app.VideoConference;
import javax.swing.JPanel;
import org.gstreamer.Bin;
import org.gstreamer.Element;
import org.gstreamer.GhostPad;
import org.gstreamer.Pad;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.swing.VideoComponent;
import util.Util;

/**
 *
 * @author chandra
 */
public class MyVideoBin extends Bin{
    
    VideoComponent videoComponent;
    Element myVideoSink;
    Pad ghost;
    JPanel panel;
    javax.swing.JFrame theGUI;
    
    public MyVideoBin(String name, javax.swing.JPanel panel,
            javax.swing.JFrame theGUI){
        super(name);   
        
        this.panel = panel;
        this.theGUI = theGUI;
        videoComponent = new VideoComponent();
        myVideoSink = videoComponent.getElement();
        add(myVideoSink);
        myVideoSink.syncStateWithParent();
        
        
        ghost = new GhostPad("sink", myVideoSink.getStaticPad("sink"));
        ghost.setActive(true);
        addPad(ghost);
        /*
        Pad teeVPad = vc.getSenderPipeline().getTeeV().getRequestPad("src%d");
        ghost = new GhostPad("sinkA", teeVPad);
        
        addPad(ghost);
        
        Util.doOrDie(
                        "teeV-myVideoPipeline",
                        ghost
                                        .link(myVideoSink.getStaticPad("sink"))
                                        .equals(PadLinkReturn.OK));
        
        */
        
        showMyVideo(videoComponent);
        //
        
        
        pause();
        
    }
    
    private void showMyVideo(javax.swing.JComponent c){
        panel.removeAll();
        panel.add(c);
        theGUI.revalidate();
        theGUI.repaint();
    }
    
}
