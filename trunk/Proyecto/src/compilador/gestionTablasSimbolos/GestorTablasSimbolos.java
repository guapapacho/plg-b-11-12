package compilador.gestionTablasSimbolos;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Gestor de las Tablas de Simbolos
 * @author Grupo 1
 */
public class GestorTablasSimbolos {
	
	/** Tabla de palabras reservadas */
	private Hashtable<String, Integer> palRes;
	/** Tabla de tipos simples */
	private Hashtable<Integer, String> tipos;
	/** Puntero al ambito actual */
	private TablaSimbolos bloque_actual;
	/** Instancia única de la clase */
	private static GestorTablasSimbolos instance= null;
	
	/**
	 * Método que devuelve el gestor de TS
	 * @return instancia del gestor de TS
	 */
	public static GestorTablasSimbolos getGestorTS() {
		if(instance == null) {
			instance = new GestorTablasSimbolos();
		}
		return instance;			
	}
	
	/**
	 * Constructora privada de la clase (singleton)
	 */
	private GestorTablasSimbolos(){
		bloque_actual = new TablaSimbolos(null);
		inicializaPalRes();
		inicializaTiposSimples();
	}
	
	public static void resetTablasSimbolos() {
		instance = new GestorTablasSimbolos();
	}
	
	/**
	 * Añade un nuevo ambito a la lista y modifica los parametros que haga falta
	 * campo lexema es el id del lexema (procedimiento, bucle, if, switch,..) 
	 * al que se le añade un nuevo ambito
	 */
	public void abreBloque(){
		//Crea un ambito, actualiza continente y lo añade a la lista de ambitos
		TablaSimbolos bloque = new TablaSimbolos(bloque_actual);
		
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
	 * Elimina el ámbito actual y regresa a su padre
	 * (usar en el caso de las cabeceras)
	 */
	public void eliminaBloque() {
		cierraBloque();
		bloque_actual.removeLastContenido();
	}
	
	/**
	 * Devuelve la tabla de simbolos actual
	 */
	public TablaSimbolos dameBloqueActual(){
		return bloque_actual;
	}
	
	/**
	 * Busca a ver si esta el identificador en el ambito actual, si no esta lo inserta y devuelve 
	 * el puntero a la entrada de la tabla de simbolos correspondiente. 
	 * Si ya esta o es una palabra reservada no lo inserta y devuelve null
	 */
	public EntradaTS insertaIdentificador(String lexema){
		if (esReservada(lexema) || bloque_actual.contiene(lexema)){
			return null;
		}
		return bloque_actual.inserta(lexema);
	}
	
	/**
	 * Busca una palabra dada en el ambito actual y en sus predecesores. 
	 * Devuelve un puntero a la entrada de la tabla de simbolos
	 * donde se encuentra el identificador o null si no lo ha encontrado.
	 */
	public EntradaTS buscaIdGeneral(String lexema){
		TablaSimbolos ambito = bloque_actual;
		EntradaTS entrada = null;
		// Busca por el ámbito actual y los padres hasta dar con la solución
		while (ambito != null && entrada == null){
			entrada = ambito.getEntrada(lexema);
			if (entrada == null) { // Si no está
				ambito = ambito.getContinente(); // Actualiza al ámbito padre
			}
		}
		return entrada;
	}
	
	/**
	 * Busca una palabra dada en el ambito actual. 
	 * Devuelve un puntero a la entrada de la TS del identificador
	 * o null si no la ha encontrado.
	 */
	public EntradaTS buscaIdBloqueActual(String lexema) {
		return bloque_actual.getEntrada(lexema);
	}
	
	/**
	 * Busca un lexema en la tabla de palabras reservadas
	 * @param lexema
	 * @return numero identificativo o null si no es una palabra reservada
	 */
	public Integer buscaPalRes(String lexema) {
		return palRes.get(lexema);
	}
	
	/**
	 * Comprueba si una palabra es palabra reservadas
	 */
	private boolean esReservada(String palabra){
		return palRes.containsKey(palabra);
	}

	/**
	 * Comprueba si una palabra es de tipo simple
	 */
	public boolean esTipoSimple(Integer integer){
		return tipos.containsKey(integer);
	}
	
	public String toString() {
		String s = "";
		s += "-----------------------------------\n";
		s += "       TABLAS DE SIMBOLOS: \n";
		s += "-----------------------------------\n";
		if(bloque_actual != null)
			s = s + bloque_actual.toString("");
		return s;
	}

	/**
	 * Inicializa la tabla de tipos
	 */
	private void inicializaTiposSimples()	{
		tipos = new Hashtable<Integer,String>();
		tipos.put(4, "bool");
		tipos.put(13, "char");
		tipos.put(14, "char16_t");
		tipos.put(15, "char32_t");
		tipos.put(20, "double");
		tipos.put(28, "float");
		tipos.put(34, "int");
		tipos.put(35, "long");
		tipos.put(48, "short");
		tipos.put(71, "wchar_t");
//		tipos.put(73, "String"); string no es un tipo simple
	}	
	
	public String getTipoSimple(Integer atributo) {
		return tipos.get(atributo);
	}	
	
	/**
	 * @param id
	 * @return string de la palabra reservada asociada
	 */

	public String dameNombrePalRes(Integer id)
	{
		if(palRes.containsValue(id)){

		 for (Enumeration<String> f = palRes.keys(); f.hasMoreElements();)
		 {	 
		       String d = f.nextElement();
				if(palRes.get(d).equals(id))
					return d;
		}	
		}
		return "";
	}			
	
	/**
	 * Inicializa la tabla de palabras reservadas
	 */
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
		palRes.put("String", 73);//creo que es con minuscula
		palRes.put("cin", 74);
		palRes.put("cout", 75);
		palRes.put("endl", 76); // TODO: agregar en otros sitios pertinentes !??
		palRes.put("include", 77);

	}
}
