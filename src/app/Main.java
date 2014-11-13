/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;

/**
 *
 * @author chandra
 */
public class Main {
   public static void main(String[]args) {
       
       new Thread(new Runnable() {

           @Override
           public void run() {
               final VideoConference vc2= new VideoConference();
       
                //Testing
                System.out.println("Receiving");
                
                //Element myRtpBin = ElementFactory.make("gstrtpbin", null);
                int portVideo = vc2.getReceiverVideo().receiveFromUnicast();
                int portAudio = vc2.getReceiverAudio().receiveFromUnicast();
                System.out.println("receiver portVideo"+portVideo);
                System.out.println("receiver portAudio"+portAudio);
               try {
                   Thread.sleep(1000*60*10);
               } catch (InterruptedException ex) {
                   Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
               }

           }
       }).start();
       
       new Thread(new Runnable() {

           @Override
           public void run() {
               System.out.println("Welcome");
               final VideoConference vc= new VideoConference();
       
                //Testing
                System.out.println("Send");
                vc.getSenderVideo().streamTo("127.0.0.1", 5050);
                vc.getSenderAudio().streamTo("127.0.0.1", 5055);
                
                try {
                   Thread.sleep(1000*60*10);
               } catch (InterruptedException ex) {
                   Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
               }
           }
       }).start();
       
       
       
       
   }
}
