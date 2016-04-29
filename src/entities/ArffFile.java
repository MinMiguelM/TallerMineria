/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DistanceFunction;
import weka.core.FastVector;
import weka.core.Instance;
import weka.filters.Filter;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
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
    
    /**
     * Generaliza el atributo  especificado en la variable attribute
     * segun la variable n. Aca se evalua si el atributo es numerico para
     * convertirlo a nominal
     * @param attribute
     * @param n
     * @throws Exception 
     */
    public void generalizar(int attribute , int n) throws Exception{
        if (instances.attribute(attribute).type()==weka.core.Attribute.NUMERIC){
            System.out.println("Es numerico");
            NumericToNominal numeric = new NumericToNominal();
            String[] options= new String[2];
            options[0]="-A";
            options[1]= attribute+"";
            numeric.setOptions(options);
            numeric.setInputFormat(instances);
            instances = Filter.useFilter(instances,numeric);
        }
        FastVector values = new FastVector();
        List<String> newValues = new ArrayList<>();
        for (int i = 0; i < instances.numInstances(); i++) {
            String value = instances.instance( i ).toString(attribute);
            int n2 = n;
            char [] copy = value.toCharArray();
            while (n2 != 0){
                copy[copy.length - n2 ] = '*';
                n2--;
            }
            String newValue = new String(copy);
            if(!values.contains(newValue))
                values.addElement( new String(copy) );
            newValues.add(newValue);
        }
        String oldName = new String(instances.attribute(attribute).name());
        instances.deleteAttributeAt(attribute);
        instances.insertAttributeAt( new Attribute(oldName, values) , instances.numAttributes() );
        for (int i = 0; i < instances.numInstances(); i++) {
            instances.instance(i).setValue(instances.numAttributes()-1, newValues.get(i));
        }
        saveToFile(3);
    }
    
    public void microAgregacion(DistanceFunction df, int numCluster, int seed, int maxIterations, 
            boolean replaceMissingValues, boolean preserveInstancesOrder, List<Integer> attributes){
        SimpleKMeans kMeans;
        kMeans = new SimpleKMeans();
        try {
            kMeans.setNumClusters(numCluster);
            kMeans.setMaxIterations(maxIterations);
            kMeans.setSeed(seed);
            kMeans.setDisplayStdDevs(false);
            kMeans.setDistanceFunction(df);
            kMeans.setDontReplaceMissingValues(replaceMissingValues);
            kMeans.setPreserveInstancesOrder(preserveInstancesOrder);
            kMeans.buildClusterer(instances);
            System.out.println(kMeans.clusterInstance(instances.instance(2)));
            System.out.println(kMeans.getClusterCentroids().instance(0));
            System.out.println(kMeans.getClusterCentroids().instance(1));
            System.out.println(kMeans.getClusterCentroids().instance(2));
        } catch (Exception ex) {
            Logger.getLogger(ArffFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Salva las instancias que se encuentren en instances en un
     * archivo arff
     * @param filterNumber 
     */
    private void saveToFile(int filterNumber){
        try {
            ArffSaver saver = new ArffSaver();
            saver.setInstances(instances);
            saver.setFile(new File("filter"+filterNumber+".arff"));
            saver.writeBatch();
        } catch (IOException ex) {
            Logger.getLogger(ArffFile.class.getName()).log(Level.SEVERE, null, ex);
        }
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
