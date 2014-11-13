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
public class Util {
    
    public static void doOrDie(String reason,boolean result){
        if (!result){
            System.out.println("Error: "+reason);
        }
    }
    
}
