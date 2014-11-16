/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.util.logging.Level;
import java.util.logging.Logger;
import message.User;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;

/**
 *
 * @author chandra
 */
public class Main {
   public static void main(String[]args) {
       
       //Format: User aUser = new User(null, rtpaPort, rtcpasrcPort, rtpvPort, rtcpvsrcPort)
       User destUser = new User("destIP", 5055, 5056, 5050, 5051);
       User myUser = new User("127.0.0.1", 6055, 6056, 6050, 6051);
       User senderUser = destUser;
       
       new Thread(new Runnable() {

           @Override
           public void run() {
               final VideoConference vc2= new VideoConference();
       
               //Testing
               System.out.println("Receiving");
                
               
               int portAudio = vc2.getReceiver().receiveFromUnicast(myUser,senderUser);
                //System.out.println("receiver portVideo"+portVideo);
               System.out.println("receiver portAudio"+portAudio);
               try {
                   Thread.sleep(1000*60*10);
               } catch (InterruptedException ex) {
                   Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
               }

           }
       }).start();
       
       try {
           Thread.sleep(1000);
       } catch (InterruptedException ex) {
           Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
       }
       
       new Thread(new Runnable() {

           @Override
           public void run() {
               System.out.println("Welcome");
               final VideoConference vc= new VideoConference();
       
                //Testing
                System.out.println("Send");
                //vc.getSenderVideo().streamTo("127.0.0.1", 5050);
                vc.getSender().streamTo(myUser, destUser);
                //vc.getSender().streamTo("127.0.0.1", 5055);
                
                try {
                   Thread.sleep(1000*60*10);
               } catch (InterruptedException ex) {
                   Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
               }
           }
       }).start();
       
       
       
       
   }
}
