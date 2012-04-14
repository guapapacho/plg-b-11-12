package compilador.gestionTablasSimbolos;

import java.util.Hashtable;
import java.util.Vector;

import compilador.analizadorSemantico.ExpresionTipo.TipoNoBasico;


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
	
	public void removeLastContenido() {
		contenidos.remove(contenidos.lastElement());
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
	
	public String toString() {
		String s = "";
		
		s += "--- Entradas ---\n";
		for(EntradaTS entrada: entradasTS.values()){
			if (entrada.getTipo().equals(TipoNoBasico.funcion) || entrada.getTipo().equals(TipoNoBasico.cabecera)) {
				s += "  " + entrada.getLexema()+" \'"+entrada.getTipo().toString()+"\'\n";
			} else {
				s += entrada.isConstante() ? "  Constante ": "  Variable ";
				s += entrada.getLexema()+" declarada con tipo \'"+entrada.getTipo().toString()+"\'\n";
			}
		}
		
		s += "--- Tablas ---\n";
		for(TablaSimbolos contenido: contenidos) {
			s += contenido + "\n";
		}
		
		return s + "\n";
	}
	
}
