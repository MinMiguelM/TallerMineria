/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tallermineria;

import entities.ArffFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import weka.core.DistanceFunction;

/**
 *
 * @author ASUS
 */
public class TallerMineria {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            ArffFile arff = ArffFile.construct( "bank.arff" );
            List< Map.Entry< String, Integer > > map = arff.findPseudoIdentifiers(  Arrays.asList( 1 , 2 , 3 ) );
        //arff.supresor(1);
        //arff.generalizar(0, 2);
         // arff.microAgregacion(new weka.core.EuclideanDistance(), 3, 10, 500, false, false, Arrays.asList(1));
            arff.generalizarpunto2(1,"Taxonomia.txt");
            System.out.println("mapa " + map);
        } catch (Exception ex) {
            Logger.getLogger(TallerMineria.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
