package compilador.tablaSimbolos;

import java.util.*;

import compilador.lexico.tokens.Token;
import compilador.lexico.tokens.Token.TipoToken;
import compilador.tablaSimbolos.Ambito.Atributos;

/**
 * 
 * @author Grupo 1
 *
 */
public class TablaSimbolos {
	
	/**
	 * Tabla de palabras reservadas
	 */
	private Hashtable<String, Integer> palRes;
	
	/**
	 * Una lista de ambitos
	 * Cada ambito tiene un atributo para continente y una tabla hash
	 * (Hashtable<String, EnumMap<Atributos, Object>>) con un campo clave, que seria el identificador del
	 * procedimiento, variable o constante
	 * y un campo valor que sera una tabla con todos sus atributos (Tipo, numArgs, tipoArgs, pasoArgs, retorno, contenido..)
	 * No se si os parece bien esta forma de implementarlo, si veis alguna otra forma mejor comentarlo
	 */
	private ArrayList<Ambito> listaAmbitos;
	
	/** Puntero al ambito actual */
	private int bloque_actual;
	
	
	/**
	 * Constructora de la clase
	 */
	public TablaSimbolos(){
		bloque_actual = 0;
		listaAmbitos = new ArrayList<Ambito>();
		inicializaPalRes();
		
		
	}
	
	/**
	 * Busca una palabra en la tabla de palabras reservadas
	 */
	public boolean esReservada(String palabra){
		return palRes.containsKey(palabra);
	}


	public Hashtable<String, Integer> getPalres() {
		return palRes;
	}
	
	/**
	 * Busca una palabra dada en el ambito actual. 
	 * Devuelve el numero de ambito donde se encuentra el identificador
	 * o -1 si no la ha encontrado.
	 */
	public int estaInsertada(String id){
		Ambito ambito;
		int numAmbito = bloque_actual;
		ambito = listaAmbitos.get(bloque_actual);
		
		// Busca por el ámbito actual y los padres hasta dar con la solución
		while (ambito != null){
			if (ambito.getFilaAmbito().contains(id))
				return listaAmbitos.indexOf(ambito);
				
			else { // Vuelve al ámbito padre
				if (numAmbito == 0) // No hay más ámbitos padre
					return -1;
				else { // Actualiza al ámbito padre
					numAmbito = (Integer)ambito.getContinente();
					ambito = listaAmbitos.get(numAmbito);
				}
			}
		}
		return -1;
	}
	
	
	/**
	 * Añade un nuevo ambito a la lista y modifica los parametros que haga falta
	 * campo lexema es el id del lexema (procedimiento, bucle, if, switch,..) al que se le añade un nuevo ambito
	 */
	public void abreAmbito(String lexema){
		
		//Crea un ambito, actualiza continente y lo añade a la lista de ambitos
		Ambito a = new Ambito();
		a.setContinente(bloque_actual);
		listaAmbitos.add(a);
		int bloque_nuevo = listaAmbitos.size() - 1;
		
		//Actualiza contenido del ambito anterior
		ArrayList<Object> l = new ArrayList<Object>();
		l.add(bloque_nuevo);
		listaAmbitos.get(bloque_actual).getFilaAmbito().get(lexema).put(Ambito.Atributos.CONTENIDO, bloque_nuevo);

			
		//Actualiza el puntero de ámbito actual
		bloque_actual = bloque_nuevo;
	}
	
	/**
	 *Cierra el ambito actual y regresa a su padre
	 */
	public void CierraAmbito(){
		Ambito ambito = listaAmbitos.get(bloque_actual);
		bloque_actual = ambito.getContinente();
	}
	
	/**
	 * Busca a ver si esta el identificador en el ambito actual, si no esta lo inserta y devuelve cierto
	 * si esta o es una palabra reservada no lo inserta y devuelve falso
	 */
	public boolean insertaIdentificador(String lexema){
		
		if (esReservada(lexema)){
			return false;
		}

		if(listaAmbitos.get(bloque_actual).getFilaAmbito().containsKey(lexema))
			return false;
		
		EnumMap<Atributos, Object> filaAmbito = new EnumMap<Atributos, Object>(Ambito.Atributos.class);
		filaAmbito.put(Atributos.LEXEMA, lexema);
		listaAmbitos.get(bloque_actual).getFilaAmbito().put(lexema, filaAmbito);
		return true;
	}
	
	public Token BuscaPalRes(String lexema)
	{
		if(esReservada(lexema))
			return new Token(TipoToken.PAL_RESERVADA,palRes.get(lexema));
		return null;
	}
	
	public Token BuscaId(String lexema)
	{
		if(listaAmbitos.get(bloque_actual).getFilaAmbito().containsKey(lexema))	
			return new Token(TipoToken.IDENTIFICADOR,listaAmbitos.get(bloque_actual).getFilaAmbito().get(lexema).get(Atributos.TIPO));
		return null;
	}
	

	private void inicializaPalRes()
	{
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
		palRes.put("dynamic_cast", 30);
		palRes.put("dynamic_cast", 31);
		palRes.put("friend",32);
		palRes.put("goto",33);
		palRes.put("if",34);
		palRes.put("inline",35);
		palRes.put("int",36);
		palRes.put("long",37);
		palRes.put("mutable",38);
		palRes.put("namespace",39);
		palRes.put("new",40);
		palRes.put("noexcept",41);
		palRes.put("nullptr",42);
		palRes.put("operator",43);
		palRes.put("private",44);
		palRes.put("protected",45);
		palRes.put("public",46);
		palRes.put("register",47);
		palRes.put("reinterpret_cast",48);
		palRes.put("return",49);
		palRes.put("short",50);
		palRes.put("signed",51);
		palRes.put("sizeof",52);
		palRes.put("static",53);
		palRes.put("static_assert",54);
		palRes.put("static_cast",55);
		palRes.put("struct",56);
		palRes.put("switch",57);
		palRes.put("template",58);
		palRes.put("this",59);
		palRes.put("thread_local",60);
		palRes.put("throw",61);
		palRes.put("true",62);
		palRes.put("try",62);
		palRes.put("typedef",64);
		palRes.put("typeid",65);
		palRes.put("typename",66);
		palRes.put("union",67);
		palRes.put("unsigned",68);
		palRes.put("using",69);
		palRes.put("virtual",70);
		palRes.put("void",71);
		palRes.put("volatile",72);
		palRes.put("wchar_t",73);

	}	
	


}
