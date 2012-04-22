package compilador.gestionTablasSimbolos;

import java.util.Hashtable;
import java.util.Vector;

import compilador.analizadorSemantico.Cabecera;
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
	
	private String nombre;
	
	
	/**
	 * Constructora de la clase
	 * @param id - el identificador de la tabla
	 * @param continente - el continente del ambito o null si es el global
	 * @param nombre 
	 */
	public TablaSimbolos(TablaSimbolos continente, String nombre) {
		this.continente = continente;
		contenidos = new Vector<TablaSimbolos>();
		entradasTS = new Hashtable<String, EntradaTS>();
		this.nombre = nombre;
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
	
	public String toString(String tab) {
		String s = "";
		
		s += tab + "--- Entradas Ámbito "+ nombre + "---\n";
		for(EntradaTS entrada: entradasTS.values()){
			if(entrada.getTipo()!= null)
				if (entrada.getTipo().equals(TipoNoBasico.funcion) || entrada.getTipo().equals(TipoNoBasico.cabecera)) {
					s += tab + entrada.getLexema()+" \'"+entrada.getTipo().toString()+"\'\n";
				} else {
					s += entrada.isConstante() ? tab+"Constante ": tab+"Variable ";
					s += entrada.getLexema()+" declarada con tipo \'"+entrada.getTipo().toString()+"\'\n";
				}
			else 
				s += "Jolines, " + entrada.getLexema() + " tiene tipo null\n";
		}
		
		if(contenidos.size() > 0)
			s += "\n"; //s += tab+"--- Tablas ---\n";
		for(TablaSimbolos contenido: contenidos) {
			s += contenido.toString(tab+"    ") + "\n";
		}
		
		return s;
	}

	public Vector<String> cabecerasRestantes() {
		Vector<String> cabeceras = new Vector<String>();
		for(EntradaTS entrada: entradasTS.values()){
			if(entrada.getTipo()!= null && entrada.getTipo().equals(TipoNoBasico.cabecera)){
				if(((Cabecera) entrada.getTipo()).getImagen() != null) {
					cabeceras.add("La funcion '" + entrada.getLexema() + "' no está implementada.");
				} else {
					cabeceras.add("El procedimiento '" + entrada.getLexema() + "' no está implementado.");
				}
			}
		}
		return cabeceras;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
}
