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
import util.Config;

/**
 *
 * @author chandra
 */
public class Main {
   public static void main(String[]args) {
       System.out.println("hahaha2");
       //start the server
       //just 1 server needed.
       SessionServer ss = new SessionServer(Config.SERVER_PORT);
       new Thread(ss).start();
      
       
       
       //start the client
       final VideoConference vc= new VideoConference();
       boolean testing = false;
       
           
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
             final GUI gui = new GUI(vc);
             gui.setVisible(true);
             vc.setGUI(gui);
             System.out.println("2");

             final GUIConferenceRoom guiCR = new GUIConferenceRoom(vc);
             System.out.println("3");

             vc.setGUICR(guiCR);
             System.out.println("4");

            guiCR.setVisible(false);
             vc.init();
             System.out.println("5");

             }
         });

           
        
           
       
       
   }
}
