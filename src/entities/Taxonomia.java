/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

/**
 *
 * @author María
 */
public class Taxonomia {
    String nombre;
    Taxonomia padre;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Taxonomia getPadre() {
        return padre;
    }

    public void setPadre(Taxonomia padre) {
        this.padre = padre;
    }

    public Taxonomia(String nombre, Taxonomia padre) {
        this.nombre = nombre;
        this.padre = padre;
    }
    public void agregarAncestro(Taxonomia p)
    {
        if(padre==null)
        {
            padre=p;
        }
        else
        {
            padre.agregarAncestro(p);
        }
    }

    public String generalizar(String palabrita) {
        if(nombre.equals(palabrita))
        {
            if(padre==null)
            {
                System.out.println("no tiene padre");
                return nombre;
            }
            else
            {
                return padre.getNombre();
            }
        }
        else
        {
            if(padre!=null)
            {
                return padre.generalizar(palabrita);
            }
            
        }
        return null;
    }
    
}
