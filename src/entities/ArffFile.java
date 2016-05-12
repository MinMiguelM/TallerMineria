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
import weka.filters.Filter;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.unsupervised.attribute.NumericToNominal;

/**
 *
 * @author david
 */
public class ArffFile {
    /**
     * instancias que represntan el archivo arfff
     */
    private Instances instances;
    /**
     * copia de las intancias a la cual se le aplican los filtrows
     */
    private Instances instancesFilter;

    /**
     * crea un nuevo archivo dadas unas isntancias Arfff
     * @param instance 
     */
    public ArffFile(Instances instance) {
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
     *
     * @pre: debe estar cargado el arff correctamente
     * @param attributes
     * @return
     */
    public List< Map.Entry< String, Integer>> findPseudoIdentifiers(List<Integer> attributes) {
        HashMap< String, Integer> map = new HashMap<>();
        for (Integer attribute : attributes) {
            map.put(attribute + "", findPseudoIdentifiersByAttrinute(attribute).size());
        }
        List< Map.Entry< String, Integer>> list = new ArrayList<>(map.entrySet());
        return list;
    }
    /**
     * imprime todas las intancias del atributo 
     * @param attribute 
     */
    public void imprimir(int attribute) {
        for (int i = 0; i < instances.numInstances(); i++) {
            System.out.println(instances.instance(i).toString(attribute));
        }
    }
    /**
     * Lee el archivo de taxonomias 
     * @param archivo, ruta del archivo
     * @return  lista de taxonomias
     */
    public ArrayList<Taxonomia> leerTaxonomia(String archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String line;
            ArrayList<Taxonomia> taxonomias = new ArrayList<>();
            int indice = -1;
            while ((line = br.readLine()) != null) {
                String palabra = line.replace("\t", "");
                int tam = line.length() - palabra.length();
                Taxonomia tax = new Taxonomia(palabra, null);
                if (tam == 0) {
                    taxonomias.add(tax);
                    indice++;
                } else {
                    taxonomias.get(indice).agregarAncestro(tax);
                }
            }

            return taxonomias;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * generaliza un atributo al valor de la taxonomia
     * @param attribute, identificador del atributo a seleccionar
     * @param archivo, ruta del archivo
     * @throws Exception 
     */
    public void generalizarpunto2(int attribute, String archivo) throws Exception {
        //instancesFilter = new Instances(instances);
        ArrayList<Taxonomia> taxonomias = leerTaxonomia(archivo);
        FastVector values = new FastVector();
        List<String> newValues = new ArrayList<>();
        if (instancesFilter.attribute(attribute).type() == weka.core.Attribute.NOMINAL) {
            for (int i = 0; i < instancesFilter.numInstances(); i++) {
                String palabrita = instancesFilter.instance(i).toString(attribute);
                String generalizar = null;
                for (int j = 0; j < taxonomias.size(); j++) {
                    String temp = taxonomias.get(j).generalizar(palabrita);
                    if (temp != null) {
                        generalizar = temp;
                    }
                }
                if (generalizar == null) {
                    throw new Exception("PALABRA NO ENCONTRADA EN LA TAXONOMÍA: " + palabrita);
                }
                if (!values.contains(generalizar)) {
                    values.addElement(generalizar);
                }
                newValues.add(generalizar);

                ///AHORA SI TENGO EL RES
            }
            String oldName = new String(instancesFilter.attribute(attribute).name());
            instancesFilter.deleteAttributeAt(attribute);
            instancesFilter.insertAttributeAt(new Attribute(oldName, values), instancesFilter.numAttributes());
            for (int i = 0; i < instancesFilter.numInstances(); i++) {
                instancesFilter.instance(i).setValue(instancesFilter.numAttributes() - 1, newValues.get(i));
            }
        } else {
            throw new Exception("EL ATRIBUTO: " + instancesFilter.attribute(attribute).name() + " NO ES NOMINAL");
        }
        saveToFile(2);
    }

    /**
     * Generaliza el atributo especificado en la variable attribute segun la
     * variable n. Aca se evalua si el atributo es numerico para convertirlo a
     * nominal
     *
     * @param attribute indice del atributo a generalizar
     * @param n cantidad de digitos a ser reemplazados
     * @throws Exception
     */
    public boolean generalizar(int attribute, int n) throws Exception {
        //instancesFilter = new Instances(instances);
        if (instancesFilter.attribute(attribute).type() == weka.core.Attribute.NUMERIC) {
            NumericToNominal numeric = new NumericToNominal();
            numeric.setAttributeIndices((attribute + 1) + "");
            numeric.setInputFormat(instances);
            instancesFilter = Filter.useFilter(instancesFilter, numeric);
        }
        FastVector values = new FastVector();
        List<String> newValues = new ArrayList<>();
        for (int i = 0; i < instancesFilter.numInstances(); i++) {
            String value = instancesFilter.instance(i).toString(attribute);
            int n2 = n;
            char[] copy = value.toCharArray();
            if(copy.length < n){
                return false;
            }
            while (n2 != 0) {
                copy[copy.length - n2] = '*';
                n2--;
            }
            String newValue = new String(copy);
            if (!values.contains(newValue)) {
                values.addElement(new String(copy));
            }
            newValues.add(newValue);
        }
        String oldName = new String(instancesFilter.attribute(attribute).name());
        instancesFilter.deleteAttributeAt(attribute);
        instancesFilter.insertAttributeAt(new Attribute(oldName, values), instancesFilter.numAttributes());
        for (int i = 0; i < instancesFilter.numInstances(); i++) {
            instancesFilter.instance(i).setValue(instancesFilter.numAttributes() - 1, newValues.get(i));
        }
        saveToFile(3);
        return true;
    }

    /**
     * Dada una lista de parametros, se ejecuta el filtro de microagregacion.
     * Todos estos parametros son entrada del usuario.
     * @param df Puede ser Euclidian o Manhattan distance, se especifica en la entrada.
     * @param numCluster
     * @param seed
     * @param maxIterations
     * @param replaceMissingValues
     * @param preserveInstancesOrder
     * @param attributes lista de los atributos que se desean generalizar con cluster
     */
    public void microAgregacion(DistanceFunction df, int numCluster, int seed, int maxIterations,
            boolean replaceMissingValues, boolean preserveInstancesOrder, List<Integer> attributes) throws Exception{
        //instancesFilter = new Instances(instances);
        SimpleKMeans kMeans;
        kMeans = new SimpleKMeans();
        Instances uniqueAttribute;
        int i = 0;
        for (Integer attribute : attributes) {
            uniqueAttribute = new Instances(instances);
            String name = new String(instances.attribute(attribute).name());
            if(instances.attribute(attribute).isDate() || instances.attribute(attribute).isString())
                throw new Exception("No se puede hacer cluster con atributos de tipo DATE o STRING");
            i=0;
            while(uniqueAttribute.numAttributes()!=1){
                if(!name.equals(uniqueAttribute.attribute(i).name()))
                    uniqueAttribute.deleteAttributeAt(i);
                else
                    i++;
            }
            try {
                kMeans.setNumClusters(numCluster);
                kMeans.setMaxIterations(maxIterations);
                kMeans.setSeed(seed);
                kMeans.setDisplayStdDevs(false);
                kMeans.setDistanceFunction(df);
                kMeans.setDontReplaceMissingValues(replaceMissingValues);
                kMeans.setPreserveInstancesOrder(preserveInstancesOrder);
                kMeans.buildClusterer(uniqueAttribute);
                for (int j = 0; j < uniqueAttribute.numInstances(); j++) {
                    if(uniqueAttribute.attribute(0).isNumeric())
                        uniqueAttribute.instance(j).setValue(0, Double.parseDouble(kMeans.getClusterCentroids().instance
                                (kMeans.clusterInstance(uniqueAttribute.instance(j))).toString()));
                    else
                        uniqueAttribute.instance(j).setValue(0, kMeans.getClusterCentroids().instance
                                (kMeans.clusterInstance(uniqueAttribute.instance(j))).toString());
                }
                replaceValues(uniqueAttribute,attribute);
            } catch (Exception ex) {
                Logger.getLogger(ArffFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        saveToFile(4);
    }
    
    /**
     * Agrega los nuevos valores que se encuentran en uniqueAttribute
     * A instancesFilter para luego ser exportado en archivo arff
     * @param uniqueAttribute 
     */
    public void replaceValues(Instances uniqueAttribute, int attribute){
        for (int i = 0; i < instancesFilter.numInstances(); i++) {
            if(instancesFilter.attribute(attribute).isNumeric())
                instancesFilter.instance(i).setValue(attribute, Double.parseDouble(uniqueAttribute.instance(i).toString(0)));
            else
                instancesFilter.instance(i).setValue(attribute, uniqueAttribute.instance(i).toString(0));
        }
    }

    /**
     * filtro que suprime el atributo, todos los valores se vuelven vacio
     * @param attribute, identificador del atributo a suprimir
     */
    public void supresor(int attribute) {
        //instancesFilter = new Instances(instances);
        FastVector values = new FastVector();
        List<String> newValues = new ArrayList<>();
        for (int i = 0; i < instancesFilter.numInstances(); i++) {
            String value = instancesFilter.instance(i).toString(attribute);
            String newValue = new String("?");
            if (!values.contains(newValue)) {
                values.addElement(newValue);
            }
            newValues.add(newValue);
        }
        String oldName = new String(instancesFilter.attribute(attribute).name());
            instancesFilter.deleteAttributeAt(attribute);
            instancesFilter.insertAttributeAt(new Attribute(oldName, values), instancesFilter.numAttributes());
        for (int i = 0; i < instancesFilter.numInstances(); i++) {
            instancesFilter.instance(i).setValue(instancesFilter.numAttributes() - 1, newValues.get(i));
        }
        saveToFile(5);
    }

    /**
     * Salva las instancias que se encuentren en instances en un archivo arff
     *
     * @param filterNumber
     */
    private void saveToFile(int filterNumber) {
        try {
            ArffSaver saver = new ArffSaver();
            saver.setInstances(instancesFilter);
            saver.setFile(new File("filter" + filterNumber + ".arff"));
            saver.writeBatch();
        } catch (IOException ex) {
            Logger.getLogger(ArffFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * cuenta la cantidad de instancias repetidas que que hay dado un atributo
     * @param attribute, identificador del atributo
     * @return 
     */
    private HashMap< String, Integer> findPseudoIdentifiersByAttrinute(int attribute) {
        HashMap< String, Integer> map = new HashMap<>();
        for (int i = 0; i < instancesFilter.numInstances(); i++) {
            String value = instancesFilter.instance(i).toString(attribute);
            if (map.get(value) == null) {
                map.put(value, 1);
            } else {
                map.put(value, map.get(value) + 1);
            }
        }
        return map;
    }
    
    public void generalizacionSupresion(List<Integer> attributes, int k, Map<Integer,String> attTaxonomia) throws Exception{
        instancesFilter = new Instances(instances);
        int numGeneralizar=1;
        while(!revisionDelK(k, attributes))
        {
          
            List< Map.Entry< String, Integer > > map=findPseudoIdentifiers(attributes);
            int max=0;
            String identificadorMax="";
            for (Map.Entry<String, Integer> map1 : map) {
                if(map1.getValue()>max)
                {
                    max=map1.getValue();
                    identificadorMax=map1.getKey();
                }
            }
            int idMax=Integer.parseInt(identificadorMax);
            if (instancesFilter.attribute(idMax).type() == weka.core.Attribute.NUMERIC) 
            {
                if(generalizar(idMax,numGeneralizar))
                    numGeneralizar++;
                else
                    supresor(idMax);
            }
            else{
                
                if (instancesFilter.attribute(idMax).type() == weka.core.Attribute.NOMINAL){
                    
                        if(attTaxonomia.containsKey(idMax)){
                            //MIRAR PRIMERO EL PAPA SINO FUNCIONA ENTONCES CON EL ABUELO PARA EVITAR PERDER INFO
                            generalizarpunto2(idMax, attTaxonomia.get(idMax));
                            // try - catch
                        }
                        else{
                               if(generalizar(idMax,numGeneralizar))
                                   numGeneralizar++;
                               else
                                   supresor(idMax);
                               
                        }       
                }
            }
        }
    }
    
    public void generalizacionMicroAgregacion(int k, List<Integer> attributes){
        instancesFilter = new Instances(instances);
        while(!revisionDelK(k, attributes)){
            //microAgregacion(null, k, k, k, true, true, attributes);
            
        }
    }
    
    public boolean revisionDelK(int k, List<Integer> attributes){
        Map<String,Integer > map = new HashMap<>();
        for(int i = 0; i < instancesFilter.numInstances();i++){
            String pseudos = "";
            for (Integer attribute : attributes) {
                pseudos += instancesFilter.instance(i).toString(attribute);
            }
            System.out.println(pseudos);
            if(!map.containsKey(pseudos))
                map.put(pseudos, 1);
            else
                map.put(pseudos,map.get(pseudos)+1);
        }
        Set<String> set = map.keySet();
        for(String s : set){
            if(map.get(s) < k)
                return false;
        }
        return true;
    }
    
    /**
     * Builder, crea una nueva isntancia de la clase  apartir de un archovo
     * @param filename, ruta del archivo
     * @return Instancia de ArffFile
     * @throws Exception El archivo no existe o es de un formato inesperado
     */
    public static ArffFile construct(String filename) throws Exception {
        try (
            BufferedReader bf = new BufferedReader(new FileReader(filename));) {
            Instances data = new Instances(bf);
            ArffFile arff = new ArffFile(data);
            return arff;
        } catch (Exception e) {
            throw new Exception("Error al leer el archivo ");
        }
    }
}


