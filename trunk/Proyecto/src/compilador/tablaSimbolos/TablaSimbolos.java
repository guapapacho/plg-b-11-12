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
	 * Constructora de la clase
	 */
	public TablaSimbolos(){
		palres = new Hashtable<String,Integer>();
	}
	
	/**
	 * Crea una nueva tabla de simbolos
	 * @return
	 */
	public static TablaSimbolos creaTS() {
        TablaSimbolos t = new TablaSimbolos();
        return t;
    }

	public Hashtable<String, Integer> getPalres() {
		return palres;
	}

	public void setPalres(Hashtable<String, Integer> palres) {
		this.palres = palres;
	}
	
	
	
	

}
