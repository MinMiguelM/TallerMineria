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

    public void generalizarpunto2(int attribute, String archivo) throws Exception {
        instancesFilter = new Instances(instances);
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
     * @param attribute
     * @param n
     * @throws Exception
     */
    public void generalizar(int attribute, int n) throws Exception {
        instancesFilter = new Instances(instances);
        if (instancesFilter.attribute(attribute).type() == weka.core.Attribute.NUMERIC) {
            NumericToNominal numeric = new NumericToNominal();
            String[] options = new String[2];
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

    public void microAgregacion(DistanceFunction df, int numCluster, int seed, int maxIterations,
            boolean replaceMissingValues, boolean preserveInstancesOrder, List<Integer> attributes) {
        try {

            SimpleKMeans kmeans = new SimpleKMeans();
            kmeans.setSeed(seed);

            // This is the important parameter to set
            kmeans.setPreserveInstancesOrder(preserveInstancesOrder);
            kmeans.setNumClusters(numCluster);
            kmeans.buildClusterer(instances);

            // This array returns the cluster number (starting with 0) for each instance
            // The array has as many elements as the number of instances
            int[] assignments = kmeans.getAssignments();
            int i = 0;
            for (int clusterNum : assignments) {
                System.out.printf("Instance %d -> Cluster %d \n", i, clusterNum);
                i++;
            }
            Instances instances2 = kmeans.getClusterCentroids();
            for (i = 0; i < instances2.numInstances(); i++) {
                // for each cluster center
                Instance inst = instances2.instance(i);
                // as you mentioned, you only had 1 attribute
                // but you can iterate through the different attributes
                String value = inst.toString(0);
                System.out.println("Value for centroid " + i + ": " + value);
            }
            /*instancesFilter = new Instances(instances);
             SimpleKMeans kMeans;
             kMeans = new SimpleKMeans();
             for (Integer attribute : attributes) {
             String name = new String(instances.attribute(attribute).name());
             for (int i = 0; i < instancesFilter.numAttributes(); i++) {
             if(name.equals(instancesFilter.attribute(attribute).name()))
             instancesFilter.deleteAttributeAt(i);
             }
             try {
             kMeans.setNumClusters(numCluster);
             kMeans.setMaxIterations(maxIterations);
             kMeans.setSeed(seed);
             kMeans.setDisplayStdDevs(false);
             kMeans.setDistanceFunction(df);
             kMeans.setDontReplaceMissingValues(replaceMissingValues);
             kMeans.setPreserveInstancesOrder(preserveInstancesOrder);
             kMeans.buildClusterer(instancesFilter);
             instancesFilter.deleteAttributeAt(seed);
             System.out.println(kMeans.clusterInstance(instancesFilter.instance(2)));
             System.out.println(kMeans.getClusterCentroids().instance(0));
             System.out.println(kMeans.getClusterCentroids().instance(1));
             System.out.println(kMeans.getClusterCentroids().instance(2));
             } catch (Exception ex) {
             Logger.getLogger(ArffFile.class.getName()).log(Level.SEVERE, null, ex);
             }
             }*/
        } catch (Exception ex) {
            Logger.getLogger(ArffFile.class.getName()).log(Level.SEVERE, null, ex);
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


