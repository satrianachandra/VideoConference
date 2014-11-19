/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package message;

import java.io.Serializable;

/**
 *
 * @author chandra
 */
public class Message implements Serializable{
    private int type;
    private Object content;
    
    
    public Message(int type,Object content){
        this.type = type;
        this.content = content;
    }
    
    public Message(int type){
        this.type = type;
    }
    
    public int getType(){
        return type;
    }

    public Object getContent(){
        return content;
    }
}

