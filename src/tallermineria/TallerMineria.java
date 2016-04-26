/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tallermineria;

import java.io.BufferedReader;
import java.io.FileReader;
import weka.core.Instances;

/**
 *
 * @author ASUS
 */
public class TallerMineria {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try(
            BufferedReader bf = new BufferedReader(new FileReader("VTargetMailWEKA.arff"));  
        ){
            Instances data = new Instances(bf);
            for (int i = 0; i < data.numInstances(); i++) {
                System.out.println((data.instance(i).classAttribute()));
                
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
}
