/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tallermineria;

import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author david
 */
public class EntryComparator  implements Comparator< Map.Entry<String , Integer > >{

    @Override
    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
        return  o2.getValue() - o1.getValue();
    }
    
}
