package compilador.tablaSimbolos;

import java.util.EnumMap;
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
	
	/**
	 * Enumerado con los atributos de los identificadores 
	 */
	public enum Atributos {
		LEXEMA,
		TIPO, 
		NUMARGS,
		TIPOARGS,
		PASOARGS,
		RETORNO,
	}
	
	/** Atributo que identifica el ambito en el gestor de tablas de simbolos */
	private Integer id;
	
	/** Atributo que guarda el continente de este ambito */
	private TablaSimbolos continente;
	
	/** Atributo que guarda la lista de contenidos de este ambito */
	private Vector<TablaSimbolos> contenidos;
	
	/** Atributo que guarda la informacion de los identificadores de este ambito */
	private Hashtable<String, EnumMap<Atributos, Object>> filasTS;
	
	
	/**
	 * Constructora de la clase
	 * @param id - el identificador de la tabla
	 * @param continente - el continente del ambito o null si es el global
	 */
	public TablaSimbolos(Integer id, TablaSimbolos continente) {
		this.id = id;
		this.continente = continente;
		contenidos = new Vector<TablaSimbolos>();
		filasTS = new Hashtable<String, EnumMap<Atributos, Object>>();
	}

	public TablaSimbolos getContinente() {
		return continente;
	}

	public void addContenido(TablaSimbolos contenido) {
		contenidos.add(contenido);
	}

	public boolean contiene(String lexema) {
		return filasTS.containsKey(lexema);
	}

	public Integer getId() {
		return id;
	}

	/**
	 * Metodo que inserta un identificador en la tabla de simbolos
	 * @param lexema
	 * @return id de la tabla
	 */
	public Integer inserta(String lexema) {
		filasTS.put(lexema, new EnumMap<Atributos,Object>(Atributos.class));
		return id;
	}
	
}
