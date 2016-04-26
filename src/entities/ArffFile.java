/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tallermineria.EntryComparator;
import weka.core.Instances;

/**
 *
 * @author david
 */
public class ArffFile {
    
    private Instances instances;

    
    public ArffFile( Instances instance ){
        this.instances = instance;
    }
    
    public Instances getInstances() {
        return instances;
    }

    public void setInstances(Instances instance) {
        this.instances = instance;
    }
    
    /**
     * retorna un mapa con los valores repetidos, falta que se ordene por valor
     * @pre: debe estar cargado el arff correctamente
     * @param attributes
     * @return 
     */
    public List< Map.Entry< String , Integer> > findPseudoIdentifiers( List<Integer> attributes ){
        HashMap< String , Integer > map = new HashMap<>();
        for( Integer attribute : attributes ) {
           map.put( attribute + "" , findPseudoIdentifiersByAttrinute( attribute ).size() )
           .putAll( ); // OJO Preguntar si se puede asumir que entre atributos no hay valores repetidos
        }
        List< Map.Entry< String , Integer > > list = new ArrayList<>( map.entrySet() );
        return list;
    }   
    
   
    private HashMap< String , Integer > findPseudoIdentifiersByAttrinute( int attribute ){
         HashMap< String , Integer > map = new HashMap<>();
         for ( int i = 0; i < instances.numInstances(); i++ ) {
                String value = instances.instance( i ).toString( attribute ); 
                if( map.get( value ) == null ){
                    map.put( value , 1 );
                }else{
                    map.replace( value , map.get( value ) + 1 );
                }
            }
        return map;
     }
    
    public static ArffFile construct( String filename ) throws Exception{
        try(
            BufferedReader bf = new BufferedReader( new FileReader( filename ) );  
        ){
            Instances data = new Instances( bf );
            ArffFile arff = new ArffFile( data );
            return arff;
        }catch(Exception e){
            throw new Exception( "Error al leer el archivo ");
        }    
    }   
}
