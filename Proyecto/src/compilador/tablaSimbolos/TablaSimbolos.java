package compilador.tablaSimbolos;

import java.util.*;

/**
 * 
 * @author Grupo 1
 *
 */
public class TablaSimbolos {
	
	/**
	 * Tabla de palabras reservadas
	 */
	Hashtable<String, Integer> palres;
	
	/**
	 * 
	 */
	Hashtable<Integer,TablaSimbolos> global;	//No se como ponerlo... :S
	
	/**
	 * Constructora de la clase
	 */
	public TablaSimbolos(){
		palres = new Hashtable<String,Integer>();
	}
	
	
	public Hashtable<String, Integer> getPalres() {
		return palres;
	}

	public void setPalres(Hashtable<String, Integer> palres) {
		this.palres = palres;
	}
	
	
	
	

}
