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
public class User {
    
    private int rtpaPort,rtcpasrcPort;
    private int rtpvPort,rtcpvsrcPort;
    
    
    private String ipAddress;
    
    
    public User(String ipAddress,int rtpaPort, int rtcpasrcPort,int rtpvPort,int rtcpvsrcPort){
        this.ipAddress = ipAddress;
        this.rtpaPort = rtpaPort;
        //this.rtcpasinkPort = rtcpasinkPort;
        this.rtcpasrcPort = rtcpasrcPort;
        this.rtpvPort = rtpvPort;
        //this.rtcpvsinkPort = rtcpvsinkPort;
        this.rtcpvsrcPort = rtcpvsrcPort;
        
    }
    
    public int getrtpaPort(){
        return rtpaPort;
    }
    
    
    public int getrtcpasrcPort(){
        return rtcpasrcPort;
    }
    
    public int getrtpvPort(){
        return rtpvPort;
    }
     
    public int getrtcpvsrcPort(){
        return rtcpvsrcPort;
    }
    
    public String getIpAddress(){
        return ipAddress;
    }
    
}
