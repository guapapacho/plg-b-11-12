package compilador.tablaSimbolos;

import java.util.Hashtable;

/**
 * 
 * @author Grupo 1
 *
 */
public class GestorTablasSimbolos {
	
	/**
	 * Tabla de palabras reservadas
	 */
	private Hashtable<String, Integer> palRes;
	
	/**
	 * Hashtable de las Tablas de Simbolos
	 */
	private Hashtable<Integer, TablaSimbolos> bloques;
	
	private static Integer cont;
	
//	/**
//	 * Una lista de ambitos
//	 * Cada ambito tiene un atributo para continente y una tabla hash
//	 * (Hashtable<String, EnumMap<Atributos, Object>>) con un campo clave, que seria el identificador del
//	 * procedimiento, variable o constante
//	 * y un campo valor que sera una tabla con todos sus atributos (Tipo, numArgs, tipoArgs, pasoArgs, retorno, contenido..)
//	 * No se si os parece bien esta forma de implementarlo, si veis alguna otra forma mejor comentarlo
//	 */
//	private ArrayList<TablaSimbolos> listaAmbitos;
	
	/** Puntero al ambito actual */
	private TablaSimbolos bloque_actual;
	
	
	/**
	 * Constructora de la clase
	 */
	public GestorTablasSimbolos(){
		cont = 0;
		bloque_actual = new TablaSimbolos(cont, null);
		bloques.put(cont++, bloque_actual);
		inicializaPalRes();
	}
	
	/**
	 * Busca una palabra dada en el ambito actual y en sus predecesores. 
	 * Devuelve el numero de ambito donde se encuentra el identificador
	 * o -1 si no la ha encontrado.
	 */
	
	/**
	 * Añade un nuevo ambito a la lista y modifica los parametros que haga falta
	 * campo lexema es el id del lexema (procedimiento, bucle, if, switch,..) al que se le añade un nuevo ambito
	 */
	public void abreBloque(){
		//Crea un ambito, actualiza continente y lo añade a la lista de ambitos
		TablaSimbolos bloque = new TablaSimbolos(cont, bloque_actual);
		bloques.put(cont++, bloque);
		
		//Actualiza contenido del ambito anterior
		bloque_actual.addContenido(bloque);
			
		//Actualiza el puntero de ámbito actual
		bloque_actual = bloque;
	}
	
	/**
	 * Cierra el ambito actual y regresa a su padre
	 */
	public void cierraBloque(){
		bloque_actual = bloque_actual.getContinente();
	}
	
	/**
	 * Busca a ver si esta el identificador en el ambito actual, si no esta lo inserta y devuelve 
	 * el identificador de la tabla de simbolos y
	 * si esta o es una palabra reservada no lo inserta y devuelve -1
	 */
	public Integer insertaIdentificador(String lexema){
		
		if (esReservada(lexema) || bloque_actual.contiene(lexema)){
			return -1;
		}
		
		return bloque_actual.inserta(lexema);
	}
	
	public Integer buscaIdGeneral(String lexema){
		TablaSimbolos ambito = bloque_actual;
		
		// Busca por el ámbito actual y los padres hasta dar con la solución
		while (ambito != null){
			if (ambito.contiene(lexema))
				return ambito.getId();
			else { // Vuelve al ámbito padre
				ambito = ambito.getContinente(); // Actualiza al ámbito padre
				if (ambito == null) // No hay más ámbitos padre
					return -1;
			}
		}
		return -1;
	}
	
	/**
	 * Busca una palabra dada en el ambito actual. 
	 * Devuelve el numero de ambito actual
	 * o -1 si no la ha encontrado.
	 */
	public Integer buscaIdBloqueActual(String lexema) {
		if(bloque_actual.contiene(lexema))	
			return bloque_actual.getId();
		return -1;
	}
	
	/**
	 * Busca un lexema en la tabla de palabras reservadas
	 * @param lexema
	 * @return numero identificativo o null si no es una palabra reservada
	 */
	public Integer buscaPalRes(String lexema) {
		if(esReservada(lexema))
			return palRes.get(lexema);
		return null;
	}
	
	/**
	 * Comprueba si una palabra es palabra reservadas
	 */
	private boolean esReservada(String palabra){
		return palRes.containsKey(palabra);
	}
	
	private void inicializaPalRes()	{
		palRes = new Hashtable<String,Integer>();
		palRes.put("alignas", 0);
		palRes.put("alignof", 1);
		palRes.put("asm", 2);
		palRes.put("auto", 3);
		palRes.put("bool", 4);
		palRes.put("break", 5);
		palRes.put("case", 6);
		palRes.put("catch", 7);
		palRes.put("class", 8);
		palRes.put("const", 9);
		palRes.put("const_cast", 10);
		palRes.put("constexpr", 11);
		palRes.put("continue", 12);
		palRes.put("char", 13);
		palRes.put("char16_t", 14);
		palRes.put("char32_t", 15);
		palRes.put("decltype", 16);
		palRes.put("default", 17);
		palRes.put("delete", 18);
		palRes.put("do", 19);
		palRes.put("double", 20);
		palRes.put("dynamic_cast", 21);
		palRes.put("else", 22);
		palRes.put("enum", 23);
		palRes.put("explicit", 24);
		palRes.put("export", 25);
		palRes.put("extern", 26);
		palRes.put("false", 27);
		palRes.put("float", 28);
		palRes.put("for", 29);
		palRes.put("friend",30);
		palRes.put("goto",31);
		palRes.put("if",32);
		palRes.put("inline",33);
		palRes.put("int",34);
		palRes.put("long",35);
		palRes.put("mutable",36);
		palRes.put("namespace",37);
		palRes.put("new",38);
		palRes.put("noexcept",39);
		palRes.put("nullptr",40);
		palRes.put("operator",41);
		palRes.put("private",42);
		palRes.put("protected",43);
		palRes.put("public",44);
		palRes.put("register",45);
		palRes.put("reinterpret_cast",46);
		palRes.put("return",47);
		palRes.put("short",48);
		palRes.put("signed",49);
		palRes.put("sizeof",50);
		palRes.put("static",51);
		palRes.put("static_assert",52);
		palRes.put("static_cast",53);
		palRes.put("struct",54);
		palRes.put("switch",55);
		palRes.put("template",56);
		palRes.put("this",57);
		palRes.put("thread_local",58);
		palRes.put("throw",59);
		palRes.put("true",60);
		palRes.put("try",61);
		palRes.put("typedef",62);
		palRes.put("typeid",63);
		palRes.put("typename",64);
		palRes.put("union",65);
		palRes.put("unsigned",66);
		palRes.put("using",67);
		palRes.put("virtual",68);
		palRes.put("void",69);
		palRes.put("volatile",70);
		palRes.put("wchar_t",71);
		palRes.put("while", 72);
		palRes.put("String", 73);
		palRes.put("cin", 74);
		palRes.put("cout", 75);

	}	
	
}
