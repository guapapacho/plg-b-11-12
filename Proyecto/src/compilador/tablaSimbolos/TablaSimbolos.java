package compilador.tablaSimbolos;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Clase que contiene (al menos) un registro
 * por cada identificador del ámbito, el continente y 
 * los contenidos. 
 * 
 * @author Grupo 1
 */
public class TablaSimbolos {
	
	/** Atributo que guarda el continente de este ambito */
	private TablaSimbolos continente;
	
	/** Atributo que guarda la lista de contenidos de este ambito */
	private Vector<TablaSimbolos> contenidos;
	
	/** Atributo que guarda la informacion de los identificadores de este ambito */
	private Hashtable<String, EntradaTS> entradasTS;
	
	
	/**
	 * Constructora de la clase
	 * @param id - el identificador de la tabla
	 * @param continente - el continente del ambito o null si es el global
	 */
	public TablaSimbolos(TablaSimbolos continente) {
		this.continente = continente;
		contenidos = new Vector<TablaSimbolos>();
		entradasTS = new Hashtable<String, EntradaTS>();
	}

	public TablaSimbolos getContinente() {
		return continente;
	}

	public void addContenido(TablaSimbolos contenido) {
		contenidos.add(contenido);
	}

	public boolean contiene(String lexema) {
		return entradasTS.containsKey(lexema);
	}
	
	public EntradaTS getEntrada(String lexema) {
		return entradasTS.get(lexema);
	}

	/**
	 * Metodo que inserta un identificador en la tabla de simbolos
	 * @param lexema
	 * @return entrada puntero a la entrada de la TS
	 */
	public EntradaTS inserta(String lexema) {
		EntradaTS entrada = new EntradaTS(lexema);
		entradasTS.put(lexema, entrada);
		return entrada;
	}
	
}