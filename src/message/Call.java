/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package message;

/**
 *
 * @author chandra
 */
public class Call {
    private String sender;
    private String receiver;
    private String ipSender;
    private int portSender;

    
    public Call(String sender,String receiver,String ipSender, int portSender){
        this.sender = sender;
        this.receiver = receiver;
        this.ipSender = ipSender;
        this.portSender = portSender;
    }
    
    public String toString() {
            return "CALL," + sender + "," + receiver + "," + ipSender + ","
                            + portSender;
    }

    /**
     * build a Call object from a String. The format of the string must be:
     * CALL,<senderName>,<receiverName>,<senderIp>,<senderPort>
     */
    public static Call fromString(String str) {
            String[] tokens = str.split(",");

            return new Call(tokens[1], tokens[2], tokens[3], new Integer(tokens[4]));
    }
    
    public String getSender(){
        return this.sender;
    }
}
