/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package message;

import java.io.Serializable;
import util.Config;

/**
 *
 * @author chandra
 */
public class User implements Serializable {
    
    private int rtpaPort,rtcpasrcPort;
    private int rtpvPort,rtcpvsrcPort;
    
    
    private String ipAddress;
    private String userName;
    
    public User(String userName, String ipAddress,int rtpaPort, int rtcpasrcPort,int rtpvPort,int rtcpvsrcPort){
        this.userName = userName;
        this.ipAddress = ipAddress;
        
        this.rtpaPort = rtpaPort;
        this.rtcpasrcPort = rtcpasrcPort;
        this.rtpvPort = rtpvPort;
        this.rtcpvsrcPort = rtcpvsrcPort;
        
    }
    
    public User(String userName,String ipAddress){
        this.userName = userName;
        this.ipAddress = ipAddress;
        
        this.rtpaPort = Config.rtpaPort;
        this.rtpvPort = Config.rtpvPort;
        this.rtcpasrcPort = Config.rtcpasrcPort;
        this.rtcpvsrcPort = Config.rtcpvsrcPort;
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
    
    public String getUserName(){
        return userName;
    }
}
