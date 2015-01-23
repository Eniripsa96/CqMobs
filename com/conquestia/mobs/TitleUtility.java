/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conquestia.mobs;

/**
 *
 * @author Ferrago
 */
public class TitleUtility {
    
    private static TitleUtility instance = null;
    
    private TitleUtility() {
        
    }
    
    public static TitleUtility getInstance() {
        
        if (instance == null) {
            instance = new TitleUtility();
        }
        
        return instance;
    }
    
}
