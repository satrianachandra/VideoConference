/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

/**
 *
 * @author chandra
 */
public class Main2 {
   public static void main(String[]args) {
       System.out.println("Welcome Receiver");
       final VideoConference vc= new VideoConference();
       
       //Testing
       System.out.println("Receiving");
        int port = vc.getReceiver().receiveFromUnicast();
        System.out.println("receiver port"+port);
       
   }
}
