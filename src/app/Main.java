/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import message.User;
import sessionserver.SessionServer;

/**
 *
 * @author chandra
 */
public class Main {
   public static void main(String[]args) {
       
       //start the server
       //just 1 server needed.
       SessionServer ss = new SessionServer(8080);
       new Thread(ss).start();
       
       try {
                Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
       //start the client
       final VideoConference vc= new VideoConference();
       boolean testing = false;
       
       if (testing){
       
            //Format: User aUser = new User(null, rtpaPort, rtcpasrcPort, rtpvPort, rtcpvsrcPort)
            User user2 = new User("user2","127.0.0.1", 4050, 4051,
                    4055,4056);
            
            User user1 = new User("user2","127.2.0.1", 6050, 6051,
                    6055,6056);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    

                    //Testing
                    System.out.println("Receiving");

                    //user2 receives from user1
                    int portAudio = vc.getReceiverPipeline().receiveFromUnicast(user2,user1);
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
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            new Thread(new Runnable() {

                @Override
                public void run() {
                    System.out.println("Welcome");

                     //Testing
                     System.out.println("Send");
                     //vc.getSenderVideo().streamTo("127.0.0.1", 5050);
                     //user1 stream to user2
                     vc.getSenderPipeline().streamTo(user1,user2);
                     //vc.getSender().streamTo("127.0.0.1", 5055);

                     try {
                        Thread.sleep(1000*60*10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();

       }else{
           
           SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                final GUI gui = new GUI(vc);
                gui.setVisible(true);
                vc.setGUI(gui);
                vc.init();
                
                final GUIConferenceRoom guiCR = new GUIConferenceRoom(vc);
                vc.setGUICR(guiCR);
                guiCR.setVisible(false);
                
                
                }
            });
       
           
        
           
       }
       
   }
}
