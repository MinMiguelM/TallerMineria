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
import weka.core.Attribute;
import weka.core.FastVector;
import weka.filters.Filter;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.NumericToNominal;

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
           map.put( attribute + "" , findPseudoIdentifiersByAttrinute( attribute ).size() );
        }
        List< Map.Entry< String , Integer > > list = new ArrayList<>( map.entrySet() );
        return list;
    }   
    
    public void imprimir(int attribute){
        for ( int i = 0; i < instances.numInstances(); i++ )
                System.out.println(instances.instance( i ).toString( attribute ));
    }
    
    public void generalizar(int attribute , int n) throws Exception{
        System.out.println(instances.attribute(attribute).type() == weka.core.Attribute.NOMINAL);
        if (instances.attribute(attribute).type()==weka.core.Attribute.NUMERIC){
            System.out.println("Es numerico");
            NumericToNominal numeric = new NumericToNominal();
            String[] options= new String[2];
            options[0]="-A";
            options[1]= attribute+"";
            numeric.setOptions(options);
            numeric.setInputFormat(instances);
            instances = Filter.useFilter(instances,numeric);
            /*NominalToString string = new NominalToString();
            options[0] = "-A";
            options[1]= attribute+"";
            string.setOptions(options);
            string.setInputFormat(instances);
            instances = Filter.useFilter(instances,string);*/
        }
        
        System.out.println(instances.attribute(attribute).isString()); 
        FastVector values = new FastVector();
        
        for (int i = 0; i < instances.numInstances(); i++) {
            String value = instances.instance( i ).toString(attribute);
            int n2 = n;
            char [] copy = value.toCharArray();
            //System.out.println(copy);
            while (n2 != 0){
                copy[copy.length - n2 ] = 'l';
                n2--;
            }
            System.out.println("hola");
            //System.out.println(instances.instance(i).toString(attribute));
            String newValue = new String(copy);
            //System.out.println(newValue);
            //System.out.println(instances.instance( i ).toString(attribute));
            //instances.instance(i).attribute(attribute).addStringValue(newValue);
            //instances.instance(i).setValue(attribute, newValue);
        
            //System.out.println();
            values.addElement( newValue );
        }
        instances.insertAttributeAt( new Attribute("jasjda", values) , instances.numAttributes() );
        instances.toString();
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
