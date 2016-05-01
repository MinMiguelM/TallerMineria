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
import weka.filters.unsupervised.attribute.AddCluster;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.NumericToNominal;

/**
 *
 * @author david
 */
public class ArffFile {

    private Instances instances;
    private Instances instancesFilter;

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

    public void imprimir(int attribute) {
        for (int i = 0; i < instances.numInstances(); i++) {
            System.out.println(instances.instance(i).toString(attribute));
        }
    }

    public ArrayList<Taxonomia> leerTaxonomia() {
        try (BufferedReader br = new BufferedReader(new FileReader("Taxonomia.txt"))) {
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

    public void generalizarpunto2(int attribute, int n) {

        ArrayList<Taxonomia> taxonomias = leerTaxonomia();
        
        if (instances.attribute(attribute).type() == weka.core.Attribute.NOMINAL) {
            for (int i = 0; i < instances.numInstances(); i++) {
                String palabrita=instances.instance(i).toString(attribute);
                String generalizar=null;
                for (int j = 0; j < taxonomias.size(); j++) {
                    String temp=taxonomias.get(j).generalizar(palabrita);
                    if(temp!=null)
                    {
                        generalizar=temp;
                    }
                }
                ///AHORA SI TENGO EL RES
                
                
            }
        }
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
    public void generalizar(int attribute, int n) throws Exception {
        instancesFilter = new Instances(instances);
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
                throw new Exception("n es mayor a la longitud del atributo");
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
        instancesFilter = new Instances(instances);
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

    public void supresor(int attribute) {
        instancesFilter = new Instances(instances);
        FastVector values = new FastVector();
        List<String> newValues = new ArrayList<>();
        for (int i = 0; i < instancesFilter.numInstances(); i++) {
            String value = instancesFilter.instance(i).toString(attribute);
            String newValue = new String("Vacio");
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

    private HashMap< String, Integer> findPseudoIdentifiersByAttrinute(int attribute) {
        HashMap< String, Integer> map = new HashMap<>();
        for (int i = 0; i < instances.numInstances(); i++) {
            String value = instances.instance(i).toString(attribute);
            if (map.get(value) == null) {
                map.put(value, 1);
            } else {
                map.put(value, map.get(value) + 1);
            }
        }
        return map;
    }

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
