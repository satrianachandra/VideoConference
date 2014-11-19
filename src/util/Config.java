/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author chandra
 */
public class Config {
    /**
    * Holds the first 3 octets of the multicast IP including last dot. Just
    * concatenate the room number to get the IP.
    */
    public static final String BASE_IP = "224.1.42.";
    
    
    public static final String ROOM_IP = "224.1.42.1";
    public static final int rtpaPortRoom = 6050;
    public static final int rtcpasrcPortRoom = 6051;
    public static final int rtpvPortRoom = 6055;
    public static final int rtcpvsrcPortRoom = 6056;
    
    
    
    /** Port to send RTP multicast stream. */
    public static final int RTP_MULTICAST_PORT = 5000;
    
    
    /** Server address (IP or hostname) */
    public static final String SERVER_ADDRESS = "127.0.0.1";
    /** Server TCP port */
    public static final int SERVER_PORT = 8080;
    
    
    public static final int rtpaPort = 5050;
    public static final int rtcpasrcPort = 5051;
    public static final int rtpvPort = 5055;
    public static final int rtcpvsrcPort = 5056;
    
    
}
