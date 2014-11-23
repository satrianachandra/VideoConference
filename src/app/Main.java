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

             final GUIConferenceRoom guiCR = new GUIConferenceRoom(vc);

             vc.setGUICR(guiCR);

            guiCR.setVisible(false);
             vc.init();

             }
         });

           
        
           
       
       
   }
}
