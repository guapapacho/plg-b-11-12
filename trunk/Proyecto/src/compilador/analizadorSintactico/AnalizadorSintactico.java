package compilador.analizadorSintactico;


import java.util.Hashtable;
import java.util.Vector;

import compilador.analizadorLexico.*;
import compilador.analizadorLexico.Token.*;
import compilador.analizadorSemantico.*;
import compilador.analizadorSemantico.ExpresionTipo.TipoBasico;
import compilador.analizadorSemantico.ExpresionTipo.TipoNoBasico;
import compilador.gestionErrores.GestorErrores;
import compilador.gestionTablasSimbolos.*;
import compilador.gestionTablasSimbolos.Tipo.EnumTipo;

public class AnalizadorSintactico {

	/** Token actual obtenido mediante el Analizador lexico */
	private Token token;
	
	private Token tokenAnterior;
	
	/** Lista de tokens obtenidos del Analizador lexico */
	private Vector<Token> tokens;
	/** Analizador lexico */
	private AnalizadorLexico lexico;
	/** Gestor tablas de simbolos */
	private GestorTablasSimbolos gestorTS;
	/** Gestor errores */
	private GestorErrores gestorErr;
	/**
	 * Vector para guardar la secuencia ordenadas de los numeros de reglas aplicadas
	 * para construir el arbol de derivacion de la cadena de tokens de entrada..
	 */
	private Vector<Integer> parse;
	
	/**
	 * Vector para guardar la secuencia ordenadas de las declaraciones que vayamos haciendo
	 */
	private Vector<String> declaraciones;
	/**
	 * Vector para usado en los casos en los que haga falta ampliar la ventana de tokens
	 */
	private Vector<Token> ventana;
	
	private Tipo tipo;
	private EntradaTS entradaTS;
	
	private int numDefaults;
	
	/** Marca si estamos en el cuerpo de un bucle */
	private boolean estamosEnBucle;
	/** Marca si estamos en el cuerpo de un Switch */
	private boolean estamosEnSwitch;
	/** Marca si estamos en una funcion */
	private boolean estamosEnFuncion;
	/** Marca las etiquetas a las que se salta con goto en la función */
	private Vector<String> etiquetasConGoto;
	/** Marca las etiquetas definidas en la función */
	private Hashtable<String, String> etiquetasSinGoto;
	/** Nombre de la clase si es una clase o null si no lo es */
	private String nombreClase;
	
	/** Numero de linea del token anterior */
	private int linea;
	/** Numero de columna del siguiente al token anterior */
	private int columna;
		
	public AnalizadorSintactico(AnalizadorLexico lexico){
		this.lexico = lexico;
		parse = new Vector<Integer>();
		declaraciones = new Vector<String>();
		tokens = new Vector<Token>();
		ventana = new Vector<Token>();
		gestorTS = GestorTablasSimbolos.getGestorTS();
		gestorErr = GestorErrores.getGestorErrores();
		tipo = null;
		entradaTS = null;
		nombreClase = null;
		estamosEnBucle = false;
		estamosEnSwitch = false;
		estamosEnFuncion = false;
		try {
			nextToken();
			programa();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void nextToken() throws Exception {
		tokenAnterior = token;		
		linea = lexico.getLinea();
		columna = lexico.getColumna();
		if(ventana.size() > 0) {
			// recupera el primer elemento y lo borra de la cola.
			token = ventana.remove(0);
		} else {
			token = lexico.scan();
			tokens.add(token);
		}
	}
	
	private ExpresionTipo idConst(ExpresionTipo tipo) throws Exception {
		entradaTS = (EntradaTS)token.getAtributo();
		entradaTS.setTipo(tipo);
		entradaTS.setConstante(true);
		nextToken();
		if(token.esIgual(TipoToken.OP_ASIGNACION,OpAsignacion.ASIGNACION)) {
			nextToken();
			ExpresionTipo LITERAL_tipo=literal();
			if(literal() != null) {
				if(ExpresionTipo.sonEquivAsig(entradaTS.getTipo(), LITERAL_tipo, OpAsignacion.ASIGNACION)!=null){
					return tipo;
				} else {
					//gestorErr.insertaErrorSintactico(linea, columna, "Constante mal inicializada");
					gestorErr.insertaErrorSemantico(linea, columna, "El tipo de la constante es distinto del tipo que que se le quiere asignar.");
					return ExpresionTipo.getError();
				}
			}
			
		} else {
			gestorErr.insertaErrorSintactico(linea, columna, "Constante no inicializada");
			return null;
		}
		return null;
	}


	private void id() throws Exception {
		entradaTS = (EntradaTS)token.getAtributo();
//		entradaTS.setTipo(tipo); TODO usando ExpresionTipo
		entradaTS.setConstante(false);
		nextToken();
	}
	
	/**
	 * Metodo que devuelve el tipo del LITERAL y lee el siguiente token
	 * Devuelve null si no es un literal.
	 * @throws Exception 
	 */
	private ExpresionTipo literal() throws Exception {
		//TODO hacer algo con los valores, si hace falta...
		if(token.esIgual(TipoToken.LIT_CADENA)) {
//			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
//			System.out.println("LITERAL CADENA: " + valor);
			nextToken();
			return new Cadena(((String) token.getAtributo()).length());
		}
		else if(token.esIgual(TipoToken.LIT_CARACTER)){
//			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
//			System.out.println("LITERAL CARACTER: " + valor);
			nextToken();
			return new ExpresionTipo(TipoBasico.caracter);
		} 
		else if (token.esIgual(TipoToken.NUM_ENTERO)){
//			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
//			System.out.println("NUMERO ENTERO: " + valor);
			nextToken();
			return new ExpresionTipo(TipoBasico.entero);
		}
		else if (token.esIgual(TipoToken.NUM_REAL)){
//			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
//			System.out.println("NUMERO REAL: " + valor);
			nextToken();
			return new ExpresionTipo(TipoBasico.real);
		}
		else if (token.esIgual(TipoToken.NUM_REAL_EXPO)){
//			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
//			System.out.println("NUMERO REAL EXPO: " + valor);
			nextToken();
			return new ExpresionTipo(TipoBasico.real);
		}
		else if (token.esIgual(TipoToken.PAL_RESERVADA) &&
				( (Integer)token.getAtributo() == 27 /*false*/ || 
				  (Integer)token.getAtributo() == 60 /*true*/)){
//			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
//			System.out.println("BOOLEANO: " + valor);
			nextToken();
			return ExpresionTipo.expresionTipoDeString("bool");
		}
		
		return null;		
	}
	
	private boolean esLiteral(){
		return (token.esIgual(TipoToken.LIT_CADENA) || token.esIgual(TipoToken.LIT_CARACTER) || token.esIgual(TipoToken.NUM_ENTERO)
				|| token.esIgual(TipoToken.NUM_REAL) || token.esIgual(TipoToken.NUM_REAL_EXPO) || token.esIgual(TipoToken.PAL_RESERVADA, 27 /*false*/)
				|| token.esIgual(TipoToken.PAL_RESERVADA, 60 /*true*/));
	}
	
	public Vector<Integer> getParse() {
		return parse;
	}	
	
	public String getStringParse() {
		String string = "";
		int i = 0;
		while(i < parse.size()) {
			for(int j=0; j<17 && i<parse.size(); j++, i++) 
				string += parse.get(i) + ", ";
			string += "\n";
		}
		return string+"\n";
	}
	
	public String muestraDeclaraciones() {
		String string = "";
		int i = 0;
		while(i < declaraciones.size()) {
			string += declaraciones.get(i);
			string += "\n";
			i++;
		}
		return string+"\n";
	}
	
	public Vector<Token> getTokens() {
		return tokens;

	}
	
	public String getStringTokens() {
		String string = "";
		for(Token token : tokens)
			string += "\nTipo: " + token.getTipo() + "\t Atr: " + token.getAtributo();
		return string+"\n";
	}	
	
	private void inicializaMarcadoresGoto() {
		etiquetasConGoto = new Vector<String>();
		etiquetasSinGoto = new Hashtable<String,String>();
	}
	
	private ExpresionTipo compruebaEtiquetasGoto() {
		boolean error = false;
		for(String etiqueta : etiquetasConGoto) {
			if(!etiquetasSinGoto.containsKey(etiqueta)) {
				error = true;
				gestorErr.insertaErrorSemantico(linea, columna, "La etiqueta '"+etiqueta+"' no está declarada en la función.");
			}
		}
		return error ? ExpresionTipo.getError() : ExpresionTipo.getVacio();
	}
	
	/**
	 * 1. PROGRAMA → LIBRERIA RESTO_PROGRAMA eof
	 * 				 {}
	 * @throws Exception 
	 */
	private void programa() throws Exception {
		parse.add(1);
		System.out.println("1");//TODO
		libreria();
		resto_programa();
		if (!token.esIgual(TipoToken.EOF)) {
			gestorErr.insertaErrorSintactico(linea, columna,"Palabra o termino \""+token.atrString()+"\" inesperado.");
		}
	}
	
	
	/**
	 *  2. LIBRERIA → #include RESTO_LIBRERIA
	 *  				{LIBRERIA.tipo_s:=RESTO_LIBRERIA.tipo_s}
	 *  3. LIBRERIA -> lambda
	 *  				{ LIBRERIA.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo libreria() throws Exception {
		if (!token.esIgual(TipoToken.EOF)) {
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ALMOHADILLA)) {
				parse.add(2);
				nextToken();
				if(token.esIgual(TipoToken.PAL_RESERVADA, 77)) {
					nextToken();
					return resto_libreria();
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta palabra \"include\"");
					return null;
				}
			} else {
				parse.add(3);
				return ExpresionTipo.getVacio();
			}
		} else {
			//gestorErr.insertaErrorSintactico(linea, columna,"Fin de fichero inesperado"); 
			// PORQUÉ HEMOS COMENTADO LO DE ARRIBA??
			return null;
		}
	}

	/**
	 * 4. RESTO_LIBRERIA → LIT_CADENA LIBRERIA
	 * 						{RESTO_LIBRERIA.tipo_s:=LIBRERIA.tipo_s}
	 * 5. RESTO_LIBRERIA → <ID.ID> LIBRERIA
	 * 						{RESTO_LIBRERIA.tipo_s:=LIBRERIA.tipo_s}
	 * 						{if(LIBRERIA.tipo_s!=error_tipo) then RESTO_LIBRERIA:=vacio
	 * 						 else RESTO_LIBRERIA:=error_tipo}
	 * @throws Exception 
	 */
	private ExpresionTipo resto_libreria() throws Exception {
		if(token.esIgual(TipoToken.LIT_CADENA)) {
			parse.add(4);
			nextToken();
			ExpresionTipo LIBRERIA_tipo_s=libreria();
			return LIBRERIA_tipo_s;
		} else if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MENOR)) {
			parse.add(5);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {	
				nextToken();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO)) {
					nextToken();
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						nextToken();
						if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MAYOR)) {
							nextToken();
							//ExpresionTipo LIBRERIA_tipo_s=libreria();
							//if(LIBRERIA_tipo_s.getTipoBasico() != TipoBasico.error_tipo)
							//	return ExpresionTipo.getVacio();
							//else
								//return ExpresionTipo.getError();
							System.out.println("5");//TODO
							ExpresionTipo LIBRERIA_tipo_s=libreria();
							return LIBRERIA_tipo_s;
						} else {
							gestorErr.insertaErrorSintactico(linea, columna,"Falta separador \">\"");
						}
					} else {
						gestorErr.insertaErrorSintactico(linea, columna,"Falta extension de libreria");
					}
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \".\"");
				}
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta la libreria");
			}	
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta separador \"<\" o \"___\"");
		}
		return null;
		
	}
	
	
	/**
	 * 105. RESTO_PROGRAMA → class ID { CUERPO_CLASE } ;
	 * {  if // ( consulta(id.lexema) == null // No puede haber otra clase con el mismo id)  and 
	 *    (CUERPO_CLASE.tipo != error_tipo) 
       		inserta(ID.entrada,TIPO_SEM, CLASE)
            RESTO_PROGRAMA.tipo = vacio  
		  else RESTO_PROGRAMA.tipo = error_tipo
	   }

	 * 106. RESTO_PROGRAMA → COSAS
	 * 						{ RESTO_PROGRAMA.tipo := COSAS.tipo }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_programa() throws Exception {
		if (token.esIgual(TipoToken.PAL_RESERVADA, 8)) {
			parse.add(105);
			nextToken();
			if (token.esIgual(TipoToken.IDENTIFICADOR)) {
				nextToken();
				nombreClase = ((EntradaTS) token.getAtributo()).getLexema();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_LLAVE)) {
					nextToken();
					ExpresionTipo CUERPO_CLASE_tipo = cuerpo_clase();
					if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)) {
						nextToken();
						if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
							nextToken();
							if(CUERPO_CLASE_tipo.getTipoBasico() != TipoBasico.error_tipo)
								//hacer algo con el ID
								return ExpresionTipo.getVacio();
							else
								return ExpresionTipo.getError();
							// TODO: return "bien hecho"
						} else {
							gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \";\"");
							return null;
						}
					} else {
						//gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"}\"");
						gestorErr.insertaErrorSintactico(linea, columna,"Palabra o termino \""+token.atrString()+"\" inesperado.");//"Palabra o termino \""+lexico.getLexema()+"\" inesperado.");
						return null;
						//ruptura=parse.size();
// AÑADIR??------------>>>						
//						if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
//							nextToken();
//						}
//						else{
//							gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \";\"");
//						}
// <<<<------------------						
					}
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"{\"");
					return null;
					//ruptura=parse.size();
				}
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el nombre de la clase");
				return null;
				//ruptura=parse.size();
			}
		} else {
			parse.add(106);
			System.out.println("106");
			return cosas();
			
		}
	}
	
	
	/**
	 * 107.CUERPO_CLASE → friend RESTO_FRIEND CUERPO CLASE
	 *  		{ 
	 *    			if (RESTO_FRIEND.tipo != error_tipo) and  (CUERPO_CLASE1.tipo != error_tipo) then 
	 * 					CUERPO_CLASE.tipo = vacio
	 * 				else 
	 * 					CUERPO_CLASE.tipo = error_tipo 
	 *			}
	 * 108.CUERPO_CLASE → public : LISTA_CLASE CUERPO_CLASE
	 *   		{ 
	 *    			if (LISTA_CLASE.tipo != error_tipo) and  (CUERPO_CLASE1.tipo != error_tipo) then 
	 * 					CUERPO_CLASE.tipo = vacio
	 * 				else 
	 * 					CUERPO_CLASE.tipo = error_tipo 
	 *			}
	 * 109.CUERPO_CLASE → private : LISTA_CLASE CUERPO_CLASE
	 *   		{ 
	 *    			if (LISTA_CLASE.tipo != error_tipo) and  (CUERPO_CLASE1.tipo != error_tipo) then 
	 * 					CUERPO_CLASE.tipo = vacio
	 * 				else 
	 * 					CUERPO_CLASE.tipo = error_tipo 
	 *			}
	 * 110.CUERPO_CLASE → protected : LISTA_CLASE CUERPO_CLASE
	 *   		{ 
	 *    			if (LISTA_CLASE.tipo != error_tipo) and  (CUERPO_CLASE1.tipo != error_tipo) then 
	 * 					CUERPO_CLASE.tipo = vacio
	 * 				else 
	 * 					CUERPO_CLASE.tipo = error_tipo 
	 *			}
	 * 111.CUERPO_CLASE → lambda
	 *   		{ CUERPO_CLASE.tipo = vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo cuerpo_clase() throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getVacio();
		if (!token.esIgual(TipoToken.EOF)) {
			if (token.esIgual(TipoToken.PAL_RESERVADA, 30)) {
				parse.add(107);
				nextToken();
				ExpresionTipo RESTO_FRIEND_tipo = resto_friend();
				ExpresionTipo CUERPO_CLASE1_tipo = cuerpo_clase();
				if(RESTO_FRIEND_tipo.getTipoBasico() != TipoBasico.error_tipo &&
					CUERPO_CLASE1_tipo.getTipoBasico() != TipoBasico.error_tipo)
					return ExpresionTipo.getVacio();
				else
					return ExpresionTipo.getError();
				
			} else if(token.esIgual(TipoToken.PAL_RESERVADA, 44) || token.esIgual(TipoToken.PAL_RESERVADA, 42) || token.esIgual(TipoToken.PAL_RESERVADA, 43)) {
				if (token.esIgual(TipoToken.PAL_RESERVADA, 44 /* public */)) {
					parse.add(108);
				} else if (token.esIgual(TipoToken.PAL_RESERVADA, 42 /* private */)) {
					parse.add(109);
				} else if (token.esIgual(TipoToken.PAL_RESERVADA, 43 /* protected */)) {
					parse.add(110);
				}
				nextToken();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.DOS_PUNTOS)) {
					nextToken();
					ExpresionTipo aux1 = lista_clase();
					ExpresionTipo aux2 = cuerpo_clase();
					if(aux1.equals(TipoBasico.error_tipo) || aux2.equals(TipoBasico.error_tipo))
						tipo = ExpresionTipo.getError();
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \":\"");
				}
			} else {
				parse.add(111);
			}
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Fin de fichero inesperado");
		}
		return tipo;
	}
	
	
	/**
	 * 112.RESTO_FRIEND → void RESTO_FRIEND2
	 * 				{ RESTO_FRIEND.tipo = RESTO_FRIEND2.tipo }
	 * 113.RESTO_FRIEND → TIPO RESTO_FRIEND2
	 * 				{ RESTO_FRIEND.tipo = RESTO_FRIEND2.tipo }
	 * @return RESTO_FRIEND.tipo
	 * @throws Exception 
	 */
	private ExpresionTipo resto_friend() throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getVacio();
		if (token.esIgual(TipoToken.PAL_RESERVADA, 69)) {
			parse.add(112);
			nextToken();
			tipo = resto_friend2(null);
		}else {
			ExpresionTipo tipoFriend = tipo();
			if (tipoFriend != null){
				parse.add(113);
				nextToken();
				tipo = resto_friend2(tipoFriend);
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta tipo de retorno (o void)");
			}
		}
		return tipo;
	}
	
	
	/**
	 * 114.RESTO_FRIEND2 → ID ( LISTA_PARAM ) ; CUERPO_CLASE
	 * 				{
	 * 				    if (LISTA_PARAM.tipo != error_tipo)
	 * 			        	Completa(id.entrada, TIPO_SEM, funcion(LISTA_PARAM.tipo, TIPO.tipo))
	 * 				    if (LISTA_PARAM.tipo = error_tipo || CUERPO_CLASE.tipo = error_tipo)
	 * 				        RESTO_FRIEND2 := error_tipo
	 * 				    else
	 * 				        RESTO_FRIEND2 := vacio
	 * 				}
	 * @param tipoFriend 
	 * @return RESTO_FRIEND2.tipo
	 * @throws Exception 
	 */
	private ExpresionTipo resto_friend2(ExpresionTipo tipoFriend) throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getError();
		
		if (token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(114);
			nextToken();
			EntradaTS entrada = (EntradaTS) token.getAtributo();
			
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
				nextToken();
				ExpresionTipo params = lista_param();
				if(!params.equals(TipoBasico.error_tipo)) {
					entrada.setTipo(new Funcion((Producto) params, tipoFriend));
				}
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
					nextToken();
					if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
						nextToken();
						ExpresionTipo aux = cuerpo_clase();
						if(params.equals(TipoBasico.error_tipo) || aux.equals(TipoBasico.error_tipo)) {
							tipo = ExpresionTipo.getError();
						}
					} else {
						gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \";\"");
					}
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \")\"");
				}
			} else  {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
			}
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta el identificador");
		}
		
		return tipo;
	}
	
	
	/**
	 * 115.LISTA_CLASE → void RESTO_LINEA LISTA_CLASE
	 * 				{ 
     *					if (RESTO_LINEA.tipo != error_tipo) and  (LISTA_CLASE1.tipo != error_tipo) then 
     *						LISTA_CLASE.tipo = vacio
     *					else 
     *						LISTA_CLASE.tipo = error_tipo 
	 *				}
	 * 116.LISTA_CLASE → TIPO RESTO_LINEA LISTA_CLASE
	 * 				{ 
     *					if (RESTO_LINEA.tipo != error_tipo) and  (LISTA_CLASE1.tipo != error_tipo) then 
     *						LISTA_CLASE.tipo = vacio
     *					else 
     *						LISTA_CLASE.tipo = error_tipo 
	 *				}
	 * 141.LISTA_CLASE → lambda
	 * 				{ LISTA_CLASE.tipo = vacio }
	 * @return 
	 * @throws Exception 
	 */
	private ExpresionTipo lista_clase() throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getVacio();
		
		if (token.esIgual(TipoToken.PAL_RESERVADA, 69)) {
			parse.add(115);
			nextToken();
			ExpresionTipo RESTO_LINEA_tipo = resto_linea(null);
			ExpresionTipo LISTA_CLASE_tipo = lista_clase();
			if(RESTO_LINEA_tipo.equals(TipoBasico.error_tipo) || !LISTA_CLASE_tipo.equals(TipoBasico.error_tipo)) {
				tipo = ExpresionTipo.getError();
			}
		}else {
			ExpresionTipo aux = tipo();
			if (aux != null){
				parse.add(116);
				ExpresionTipo RESTO_LINEA_tipo = resto_linea(tipo);
				ExpresionTipo LISTA_CLASE_tipo = lista_clase();
				if(RESTO_LINEA_tipo.equals(TipoBasico.error_tipo) || !LISTA_CLASE_tipo.equals(TipoBasico.error_tipo)) {
					aux = ExpresionTipo.getError();
				}
			} else {
				parse.add(141);
			}
		}
		return tipo;
	}
	
	
	/**
	 * 258. RESTO_LINEA → * ID ;
	 * 				{
	 * 				    Completa(id.entrada, TIPO_SEM, TIPO.tipo)
	 * 				    RESTO_LINEA.tipo := vacio
	 * 				}
	 * 117. RESTO_LINEA2 → ID RESTO_LINEA2
	 * 				{ RESTO_LINEA.tipo = RESTO_LINEA2.tipo }
	 * @param tipoID 
	 * @return RESTO_LINEA.tipo
	 * @throws Exception 
	 */
	private ExpresionTipo resto_linea(ExpresionTipo tipoID) throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getVacio();
		
		if(token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.MULTIPLICACION)) {
			nextToken();
			if (token.esIgual(TipoToken.IDENTIFICADOR)) {
				parse.add(258);
				nextToken();	
				
				if(tipoID != null) { // tipoID != void
					EntradaTS entrada = (EntradaTS) token.getAtributo();
					entrada.setTipo(new Puntero(tipoID));
				} else {
					tipo = ExpresionTipo.getError();
					gestorErr.insertaErrorSemantico(linea, columna, "No puede haber un puntero a void.");
				}
				
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
					parse.add(119);
					nextToken();
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \";\"");
				}
			}
		} else if (token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(117);
			nextToken();
			tipo = resto_linea2(tipoID, (EntradaTS) token.getAtributo());
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta el identificador");
		}
		
		return tipo;
	}
	
	
	/**
	 * 118.RESTO_LINEA2 → ( RESTO_METODO 
	 * 			{ 
	 * 			    if (RESTO_METODO.tipo == error_tipo)
	 * 			        RESTO_LINEA2.tipo := error_tipo
	 * 			    else
	 * 			        Completa(ID.entrada, TIPO_SEM, funcion(RESTO_METODO.tipo, TIPO.tipo))
	 * 					RESTO_LINEA2.tipo := vacio
	 * 			}
	 * 119.RESTO_LINEA2 → ;
	 * 			{ 
	 * 			    Completa(id.entrada, TIPO_SEM, TIPO.tipo)
	 * 			    RESTO_LINEA2.tipo := vacio
	 * 			}
	 * @param entrada 
	 * @param tipoID 
	 * @return RESTO_LINEA2.tipo
	 * @throws Exception 
	 */
	private ExpresionTipo resto_linea2(ExpresionTipo tipoID, EntradaTS entrada) throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getVacio();
		
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(118);
			nextToken();
			ExpresionTipo aux = resto_metodo();
			if(aux.equals(TipoBasico.error_tipo)) {
				tipo = ExpresionTipo.getError();
			} else {
				ExpresionTipo funcion = new Funcion((Producto) aux, tipoID);
				entrada.setTipo(funcion);
			}
		} else if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
			parse.add(119);
			nextToken();
			
			if(tipoID != null) { // tipoID != void
				entrada.setTipo(new Puntero(tipoID));
			} else {
				tipo = ExpresionTipo.getError();
				gestorErr.insertaErrorSemantico(linea, columna, "No puede haber una variable o campo de tipo void.");
			}
			
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \";\" o \"(\"");	
		}
		
		return tipo;
	}
	
	
	/**
	 * 120.RESTO_METODO → LISTA_PARAM ) RESTO_METODO2 ;
	 * 			{ 
	 * 				if (LISTA_PARAM.tipo != error_tipo) and  (RESTO_METODO2.tipo != error_tipo) then
	 * 					RESTO_METODO.tipo = LISTA_PARAM.tipo
	 * 		    	else
	 * 			        RESTO_METODO.tipo = error_tipo
	 * 			}
	 * @return RESTO_METODO.tipo
	 * @throws Exception 
	 */
	private ExpresionTipo resto_metodo() throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getVacio();
		
		parse.add(120);
		ExpresionTipo aux1 = lista_param();
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
			nextToken();
			ExpresionTipo aux2 = resto_metodo2();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
				nextToken();
				if(!aux1.equals(TipoBasico.error_tipo) && !aux2.equals(TipoBasico.error_tipo)) {
					tipo = aux1;
				} else {
					tipo = ExpresionTipo.getError();
				}
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \";\"");
			}
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
		}
		
		return tipo;
	}
	
	
	/**
	 * 121.RESTO_METODO2 → { CUERPO }
	 * 				{ RESTO_METODO2.tipo := CUERPO.tipo }
	 * 122.RESTO_METODO2 → lambda
	 * 				{ RESTO_METODO2.tipo := vacio }
	 * @return RESTO_METODO2.tipo
	 * @throws Exception 
	 */
	private ExpresionTipo resto_metodo2() throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getVacio();
		
		if (!token.esIgual(TipoToken.EOF)) {
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_LLAVE)) {
				parse.add(121);
				nextToken();
				tipo = cuerpo();
				if (!token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)) {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta }");
				}
				nextToken();
			} else {
				parse.add(122);
			}
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Fin de fichero inesperado");
		}
		
		return tipo;
	}
	
	/**
	 * 6. TIPO → id		
	 * 			{ TIPO.tipo := ?? }								
	 * 7. TIPO → TIPO_SIMPLE
	 * 			{ TIPO.tipo := TIPO_SIMPLE.tipo }
	 * @throws Exception 
	 */
	private ExpresionTipo tipo() throws Exception { // TODO terminar este método
		ExpresionTipo tipo_s =ExpresionTipo.getVacio();
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(6);
			tipo_s = ((EntradaTS)token.getAtributo()).getTipo();
			nextToken();
			return tipo_s;
		} else if(token.esIgual(TipoToken.PAL_RESERVADA) && gestorTS.esTipoSimple((Integer)token.getAtributo())){
			parse.add(7);
			tipo_s = ExpresionTipo.expresionTipoDeString(gestorTS.getTipoSimple((Integer)token.getAtributo()));
			nextToken();
			if(tipo_s!=null && token.getAtributo()!=null && (token.getAtributo() instanceof EntradaTS)){
				if(tipo_s.esTipoBasico())
					declaraciones.add("Declaramos "+ ((EntradaTS)token.getAtributo()).getLexema()+ " con tipo semantico: "+tipo_s.getTipoBasico().toString());
				else
					declaraciones.add("Declaramos "+ ((EntradaTS)token.getAtributo()).getLexema()+ " con tipo semantico: "+tipo_s.getTipoNoBasico().toString());
				return tipo_s;
			}else if(token.getAtributo() instanceof EntradaTS)
				return ExpresionTipo.getError();
		}
		return tipo_s;
	}
	
	private ExpresionTipo tipo_simple() throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getVacio();
		if(token.esIgual(TipoToken.PAL_RESERVADA) && gestorTS.esTipoSimple((Integer)token.getAtributo())){
			parse.add(7);
			tipo = ExpresionTipo.expresionTipoDeString(gestorTS.getTipoSimple((Integer)token.getAtributo()));
			nextToken();
		}
		return tipo;
	}


	/**	
	 *  8. COSAS → const TIPO ID = LITERAL INIC_CONST ; COSAS
	 *  			{ INIC_CONST.tipo_h := TIPO.tipo_s;
   	 *  			  if ((Tipo.tipo_s!=error_tipo) &&(INIC_CONST!=error_tipo) &&(Cosas'.tipo_s!=error_tipo))
   	 *				  then COSAS.tipo_s := vacio
   	 *				  else .COSAS.tipo := error_tipo }
	 *  9. COSAS → TIPO ID COSAS2 COSAS
	 *  			{ COSAS2.tipo_h := TIPO.tipo_s
 	 *		          if  (TIPO.tipo_s != error_tipo) and (COSAS2.tipo_h != error_tipo) and (COSAS1.tipo_s != error_tipo ) and (consulta(id.lexema) == null ) then
     * 				  COSAS.tipo_s = vacio
 	 *				  else CUERPO_ST.tipo = error_tipo  } 
	 * 10. COSAS → void ID ( LISTA_PARAM ) COSAS3 COSAS
	 * 				{ 	if (LISTA_PARAM.tipo_s != error_tipo) & (COSAS3.tipo_s != error_tipo) & (COSAS'.tipo_s != error_tipo)
	 * 					then COSAS.tipo := vacio
	 * 					else COSAS.tipo := error_tipo  } 
	 * 11. COSAS → enum ID { LISTANOMBRES } ; COSAS
	 * 				{  if ((LISTANOMBRES.tipo_s != error_tipo) &&(COSAS'.tipo_s != error_tipo) )
     *				   then COSAS.tipo_s := vacio
     *				   else COSAS.tipo_s := error_tipo  } 
	 * 12. COSAS → struct RESTO_ST COSAS
	 * 				{  if ((RESTO_ST.tipo_s != error_tipo) &&(COSAS'.tipo_s != error_tipo) )
     * 				   then COSAS.tipo_s := vacio
     *				   else COSAS.tipo_s := error_tipo  } 
	 * 13. COSAS → lambda
	 * 				{COSAS.tipo_s:=vacio}
	 * @throws Exception 
	 */
	private ExpresionTipo cosas() throws Exception {
		ExpresionTipo aux;
		if(!token.esIgual(TipoToken.EOF)) {
			if(token.esIgual(TipoToken.PAL_RESERVADA, 9 /*const*/)){
				parse.add(8);
				nextToken();
				ExpresionTipo TIPO_tipo_s=tipo();
				if(TIPO_tipo_s!=null){
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						idConst(TIPO_tipo_s); // ID = LITERAL
						ExpresionTipo INIC_CONST_tipo_h=inic_const(TIPO_tipo_s);
						if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
							gestorErr.insertaErrorSintactico(linea, columna,"Falta separador \";\"");
						} else {
							nextToken();
							ExpresionTipo COSAS1_tipo_s=cosas();
							if((!TIPO_tipo_s.equals(TipoBasico.error_tipo))&&(!INIC_CONST_tipo_h.equals(TipoBasico.error_tipo))&&(!COSAS1_tipo_s.equals(TipoBasico.error_tipo))){
								return ExpresionTipo.getVacio();
							}
							else{
								gestorErr.insertaErrorSemantico(linea, columna, "Error en la definicion de la variable");
								return ExpresionTipo.getError();
							}
						}
					} else {
						gestorErr.insertaErrorSintactico(linea, columna,"Falta el nombre de la variable");
						return null;
					}
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta tipo de la variable");
					return null;
				}
			} else if(token.esIgual(TipoToken.PAL_RESERVADA, 69 /* void */)){
				parse.add(10);
				nextToken();
				ExpresionTipo LISTA_PARAM_tipo_s,COSAS3_tipo_s,COSAS1_tipo_s;
				if(token.esIgual(TipoToken.IDENTIFICADOR)) {
					entradaTS = (EntradaTS)token.getAtributo();
					entradaTS.setConstante(false);
					ExpresionTipo id_tipo=entradaTS.getTipo();
					nextToken();
					if(token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)){
						nextToken();
						LISTA_PARAM_tipo_s = lista_param();
						if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
							nextToken();
							COSAS3_tipo_s = cosas3(LISTA_PARAM_tipo_s,id_tipo);
							COSAS1_tipo_s = cosas();
							if(LISTA_PARAM_tipo_s.getTipoBasico()!=TipoBasico.error_tipo && COSAS3_tipo_s.getTipoBasico()!=TipoBasico.error_tipo && COSAS1_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)
								return ExpresionTipo.getVacio();
							else
								gestorErr.insertaErrorSemantico(linea, columna, "Error en el tipo de la funcion");
								return ExpresionTipo.getError();
						} else {
							if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)||token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)){
								gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \")\"");
								return null;
							}
							else{
								gestorErr.insertaErrorSintactico(linea, columna,"Palabra o termino \""+token.atrString()+"\" inesperado.");
								return null;
							}
						}
					} else {
						gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \"(\"");
						return null;
					}
				} else {
					gestorErr.insertaErrorSintactico(linea, columna, "Falta nombre de la funcion");
					return null;
				}
			} else if(token.esIgual(TipoToken.PAL_RESERVADA, 23 /* enum */)){
				parse.add(11);
				nextToken();
				ExpresionTipo LISTANOMBRES_tipo_s,COSAS1_tipo_s;
				if(token.esIgual(TipoToken.IDENTIFICADOR)){
					String IDlexema = (String)token.getAtributo();
					id();
					if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)){
						lexico.setModoNoMeto(true);
						nextToken();
						LISTANOMBRES_tipo_s=listaNombres();
						if(LISTANOMBRES_tipo_s.getTipoBasico() != TipoBasico.error_tipo)
							gestorTS.buscaIdBloqueActual(IDlexema).setTipo(LISTANOMBRES_tipo_s);
						if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
							lexico.setModoNoMeto(false);////
							lexico.setModoDeclaracion(true);
							nextToken();
							if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
								gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \";\"");
							} else {
								nextToken();
								COSAS1_tipo_s=cosas();
								if(LISTANOMBRES_tipo_s.getTipoBasico()!=TipoBasico.error_tipo && COSAS1_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)
									return ExpresionTipo.getVacio();
								else
									gestorErr.insertaErrorSemantico(linea, columna, "Error al definir las componentes del enumerado");
									return ExpresionTipo.getError();
							}
						} else{
							gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \"}\"");
							return null;
						}
					} else{
						gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \"{\"");
						return null;
					}
				} else{
					gestorErr.insertaErrorSintactico(linea, columna, "Falta nombre de lista");	
					return null;
				}
			} else if(token.esIgual(TipoToken.PAL_RESERVADA, 54 /* struct */)){
				parse.add(12);				
				ExpresionTipo RESTO_ST_tipo_s,COSAS1_tipo_s;
				nextToken();
				RESTO_ST_tipo_s=resto_st();
				COSAS1_tipo_s=cosas();
				if(RESTO_ST_tipo_s.getTipoBasico()!=TipoBasico.error_tipo && COSAS1_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)
					return ExpresionTipo.getVacio();
				else
					gestorErr.insertaErrorSemantico(linea, columna, "Error al definir el struct");
					return ExpresionTipo.getError();
			} else if (!(aux=tipo()).equals(TipoBasico.vacio)){
				parse.add(9);
				lexico.setModoDeclaracion(true);
				if(token.esIgual(TipoToken.IDENTIFICADOR)) {
					String lexema_id=(String)token.getAtributo();
					id();
					gestorTS.buscaIdBloqueActual(lexema_id).setTipo(aux);
					ExpresionTipo COSAS2_tipo_h,COSAS1_tipo_s;	
					ExpresionTipo id_tipo=entradaTS.getTipo();
					COSAS2_tipo_h=aux;
					cosas2(COSAS2_tipo_h,id_tipo);
					COSAS1_tipo_s=cosas();
					if((aux.getTipoBasico()!=TipoBasico.error_tipo)&&(COSAS2_tipo_h.getTipoBasico()!=TipoBasico.error_tipo)&&(COSAS1_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)){
						return ExpresionTipo.getVacio();
					}
					else{
						gestorErr.insertaErrorSemantico(linea, columna, "Error en la definicion de la funcion");
						return ExpresionTipo.getError();
					}
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el nombre de la variable");
					return null;
				}
			} else {
				parse.add(13);
				return ExpresionTipo.getVacio();
			}
		} 
		System.out.println("cosas");
		return ExpresionTipo.getVacio(); //TODO: completar con las expresiones de tipo correspondientes
		
	}
	
	
		
	/** 
	 * 14. LISTANOMBRES → ID RESTO_ListaNombres
	 * 						{ LISTANOMBRES.tipo_h = Vector(ID)
	 * 						  LISTANOMBRES.tipo_s:=RESTO_LISTANOMBRES.tipo_s)}
	 * 15. LISTANOMBRES → lambda
	 *						{ LISTANOMBRES.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo listaNombres() throws Exception {
		if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(14);
			id();
			ExpresionTipo tipo_h = new Enumerado(entradaTS.getLexema());
			ExpresionTipo resto_ln = resto_ln(tipo_h);
			if(resto_ln.getTipoBasico() == TipoBasico.error_tipo)
				gestorErr.insertaErrorSemantico(linea, columna, "Identificador repetido dentro del enumerado");
			return resto_ln; 
		}
		else{
			parse.add(15);
			return ExpresionTipo.getVacio();
		}
	}
	
	/**	102. RESTO_ListaNombres → , ID RESTO_ListaNombres
	 * {  if( consulta(id.lexema) == null //OJO El id no tiene que estar declarado ni dentro del enumerado ni fuera!!!! 
     			RESTO_ListaNombes1.tipo_h = RESTO_ListaNombres.tipo_h
     			if(RESTO_ListaNombres1.tipo != error) then
     				ponElemento(RESTO_listaNombres1,IDlexema)
     				RESTO_ListaNombres.tipo = RESTO_ListaNombres1.tipo
     			else RESTO_ListaNombres.tipo = error
     		else RESTO_ListaNombres.tipo = error_tipo }"

		103. RESTO_ListaNombres → lambda
		{ 
			RESTO_ListaNombres.tipo = tipo_h
		 }

	 * @throws Exception 
	*/
	private ExpresionTipo resto_ln(ExpresionTipo tipo_h) throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(102);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				entradaTS = (EntradaTS)token.getAtributo();
				entradaTS.setConstante(true);
				entradaTS.setTipo(new ExpresionTipo(TipoBasico.entero));
				String IDlexema = token.atrString();
				nextToken();
				ExpresionTipo RESTO_listaNombres1 = resto_ln(tipo_h);
				if(RESTO_listaNombres1.getTipoBasico() != TipoBasico.error_tipo)
				{
					if(!((Enumerado)RESTO_listaNombres1).ponElemento(IDlexema)) {
						gestorErr.insertaErrorSemantico(linea, columna, "Identificador "+IDlexema+" repetido dentro del enumerado");
						return ExpresionTipo.getError();
					}	
					else {
						return RESTO_listaNombres1;
					}
				}
				else return ExpresionTipo.getError();
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna, "Falta identificador de lista");
				return null;
			}
		}
		else{
			parse.add(103);
			return tipo_h;
		}
	}

	/**	
	 * 16. COSAS2 → ( LISTA_PARAM ) COSAS3
	 * 				{ if ((LISTA_PARAM.tipo_s != error_tipo) && (COSAS3.tipo_s != error_tipo))
     *				  then COSAS2.tipo_s := vacio
     *				  else COSAS2.tipo_s := error_tipo  } 
	 * 17. COSAS2 → INICIALIZACION  DECLARACIONES ;
	 * 				{ INICIALIZACION.tipo_h := COSAS2.tipo_h;
   	 *				  DECLARACIONES.tipo_h := COSAS2.tipo_h;
   	 *				  if ((DECLARACIONES.tipo_s!=error_tipo) &&(INICIALIZACION.tipo_s!=error_tipo))
   	 *				  then COSAS2.tipo_s := vacio
   	 *				  else .COSAS2.tipo := error_tipo }
	 * @param cosas2_tipo_h 
	 * @throws Exception 
	 */
	private ExpresionTipo cosas2(ExpresionTipo COSAS2_tipo_h,ExpresionTipo tipo_id) throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) {
			ExpresionTipo LISTA_PARAM_tipo_s,COSAS3_tipo_s;
			parse.add(16);
			//abre nuevo bloque en la TS
			gestorTS.abreBloque();
			nextToken();
			LISTA_PARAM_tipo_s=lista_param();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
				nextToken();
				estamosEnFuncion = true;
				COSAS3_tipo_s=cosas3(LISTA_PARAM_tipo_s,tipo_id);
				estamosEnFuncion = false;
				if(LISTA_PARAM_tipo_s.getTipoBasico()!=TipoBasico.error_tipo && COSAS3_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)
					return ExpresionTipo.getVacio();
				else{
					gestorErr.insertaErrorSemantico(linea, columna, "Identificadores erróneos en la funcion");
					return ExpresionTipo.getError();
				}
			} else {
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)||token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)){
					gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \")\"");
					return null;
				}
				else{
					gestorErr.insertaErrorSintactico(linea, columna,"Palabra o termino \""+token.atrString()+"\" inesperado.");
					return null;
				}
			}
		} 
		else {
			ExpresionTipo INICIALIZACION_tipo_h,DECLARACIONES_tipo_h;
			parse.add(17);
			INICIALIZACION_tipo_h=inicializacion(COSAS2_tipo_h); //TODO
			DECLARACIONES_tipo_h=declaraciones(COSAS2_tipo_h); //TODO
			if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				gestorErr.insertaErrorSintactico(linea, columna,"Palabra o termino \""+token.atrString()+"\" inesperado. Falta separador \";\"");
				return null;
			}else{
				if(INICIALIZACION_tipo_h.getTipoBasico()!=TipoBasico.error_tipo && DECLARACIONES_tipo_h.getTipoBasico()!=TipoBasico.error_tipo)
					return ExpresionTipo.getVacio();
				else{
					gestorErr.insertaErrorSemantico(linea, columna, "Inicialización o declaración de variable errónea");
					return ExpresionTipo.getError();
				}
			}
		}
	}
	

	/**	18. COSAS3 → ;
	 * 				{ COSAS.tipo := vacio }
		19. COSAS3 → { CUERPO }
	 * 				{ COSAS.tipo := CUERPO.tipo }
	 * @throws Exception 
	*/
	private ExpresionTipo cosas3(ExpresionTipo params,ExpresionTipo tipo_id) throws Exception {
		ExpresionTipo CUERPO_tipo_s = ExpresionTipo.getVacio();
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
			parse.add(18);
			gestorTS.eliminaBloque();
			nextToken();
			return ExpresionTipo.getVacio();
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			parse.add(19);
			nextToken();
			inicializaMarcadoresGoto();
			CUERPO_tipo_s = cuerpo();
			ExpresionTipo aux = compruebaEtiquetasGoto();
			if(aux.equals(TipoBasico.error_tipo)) {
				CUERPO_tipo_s = ExpresionTipo.getError();
			}
			if (!token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)) {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta }");
				return null;
			}
			if((ExpresionTipo.sonEquivComp(params, CUERPO_tipo_s, OpComparacion.IGUALDAD))!=null){
				ExpresionTipo funcion = new Funcion((Producto) params, tipo_id);
				CUERPO_tipo_s = funcion; 
			}
			gestorTS.cierraBloque();//se termina la funcion, cerramos el bloque
			return CUERPO_tipo_s;
		}
		else {
			gestorErr.insertaErrorSintactico(linea, columna,"Se esperaba \";\" o \"{\" ");
			return null;
		}
	}

	/**20. LISTA_PARAM → CONSTANTE TIPO ID PASO RESTO_LISTA
	 * 					{	if (CONSTANTE.tipo_s != error_tipo) & (TIPO_tipo_s != error_tipo) & 
	 * 						   (PASO_tipo_s != error_tipo) & (RESTO_LISTA_tipo_s != error_tipo) 
	 * 						then LISTA_PARAM.tipo := vacio
	 * 						else LISTA_PARAM.tipo := error_tipo  } 
	 * 21. LISTA_PARAM → lambda
	 * 					{ LISTA_PARAM.tipo := vacio }
	 * @throws Exception 
	 */	private ExpresionTipo lista_param() throws Exception {
		
		 if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {///////////////////////////////////////
				parse.add(21);
				//return new Producto();
				return ExpresionTipo.getVacio();
			}else{
				parse.add(20);
				ExpresionTipo TIPO_tipo_s,PASO_tipo_s,RESTO_LISTA_tipo_s;
				boolean constante = constante();
				TIPO_tipo_s = tipo();
				if(!TIPO_tipo_s.getTipoBasico().equals(TipoBasico.vacio)){
					PASO_tipo_s = paso();
					
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						entradaTS = (EntradaTS)token.getAtributo();
						entradaTS.setConstante(constante);
									
						if(PASO_tipo_s.getTipoNoBasico() == TipoNoBasico.puntero)
							entradaTS.setTipo(new Puntero(TIPO_tipo_s));
						else if(PASO_tipo_s.getTipoBasico() == TipoBasico.vacio) 
							entradaTS.setTipo(TIPO_tipo_s);
						else {
							TIPO_tipo_s.setPasoReferencia(true);
							entradaTS.setTipo(TIPO_tipo_s);
						}	
						
						String IDlexema = token.atrString();
						nextToken();					
						RESTO_LISTA_tipo_s = restoLista();
						if(RESTO_LISTA_tipo_s.getTipoBasico()!=TipoBasico.error_tipo){
							((Producto)RESTO_LISTA_tipo_s).ponProducto(IDlexema, RESTO_LISTA_tipo_s);
							return RESTO_LISTA_tipo_s;}
						else
							return ExpresionTipo.getError();
					}
					else{
						gestorErr.insertaErrorSintactico(linea, columna, "Falta identificador de lista de parametros");
						return null;
					}
				}else{
					gestorErr.insertaErrorSintactico(linea, columna, "Falta tipo de lista de parametros");
					return null;
				}
			}
		} 
	
	/**
	 * 259. CONSTANTE → const
	 * 				{ CONSTANTE.tipo := vacio }
	 * 260. CONSTANTE → lambda
	 * 				{ CONSTANTE.tipo := vacio }
	 * @throws Exception 
	 */
	private boolean constante() throws Exception {
		if(token.esIgual(TipoToken.PAL_RESERVADA, 9 /* const */)){
			parse.add(259);
			return true;
		} else {
			parse.add(260);
			return false;
		}
		//return ExpresionTipo.getVacio();
	}

	/** 123.PASO → & 
	 * 			{ PASO.tipo := vacio }
	 *	124.PASO → * 
	 * 			{ PASO.tipo := vacio }
	 *  125.PASO → lambda 
	 * 			{ PASO.tipo := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo paso() throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.BIT_AND)) {
			parse.add(123);
		/*	Object valor = token.getAtributo(); 
			System.out.println("Paso parametro: " + valor); */
			nextToken();
			return null; //si devuelve null se trata de paso por referencia.
		}
		if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.MULTIPLICACION)) {
			parse.add(124);
		/*	Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("Paso parametro: " + valor); */
			nextToken();
			return new ExpresionTipo(TipoNoBasico.puntero);
		}
		else
			parse.add(125);
		return ExpresionTipo.getVacio();
	}


	/** 
	 * 22. RESTO_LISTA → , LISTA_PARAM
	 * 					{ RESTO_LISTA.tipo := LISTA_PARAM.tipo } 
	 * 23. RESTO_LISTA → lambda
	 * 					{ RESTO_LISTA.tipo := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo restoLista() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(22);
			nextToken();
			return lista_param();
		}
		else{
			parse.add(23);
			//return new Producto();
			return ExpresionTipo.getVacio();
		}
	}


	/**	
	 * 24. DIMENSION → [ NUM_ENTERO ] DIMENSION
	 * 					{ DIMENSION.tipo_s := TIPOSIMPLE.entero }
	 * 25. DIMENSION → lambda
	 * 					{ DIMENSION.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo dimension() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_CORCHETE)) {
			parse.add(24);
			nextToken();
			if(token.esIgual(TipoToken.NUM_ENTERO)){
				nextToken();
				if(token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_CORCHETE)){
					nextToken();
					ExpresionTipo tipoSimple= new ExpresionTipo(TipoBasico.entero);
					dimension();
					return tipoSimple;
				}
				else{
					gestorErr.insertaErrorSintactico(linea, columna,"Falta separador \"]\"");
					return null;
				}
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna,"Se esperaba un numero entero");
				return null;
			}
		}
		else{
			parse.add(25);
			return ExpresionTipo.getVacio();
		}
	}

	/**	
	 * 26. INIC_DIM → = INIC_DIM2
	 * 					{ INIC_DIM_tipo_s := INIC_DIM2.tipo_s }
	 * 27. INIC_DIM → lambda
	 * 					{ INIC_DIM.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo inicDim() throws Exception {
		if(token.esIgual(TipoToken.OP_ASIGNACION, OpAsignacion.ASIGNACION)){
			parse.add(26);
			nextToken();
			//inicDim2();
			ExpresionTipo INIC_DIM_tipo_s=inicDim2();
		}
		else{
			parse.add(27);
			return ExpresionTipo.getVacio();
		}
		return ExpresionTipo.getVacio();
	}


	/**	28. INIC_DIM2 → { INIC_DIM3 }
	 * 					{ INIC_DIM2.tipo_s := INIC_DIM3.tipo_s }
	 * @throws Exception 
	 */
	private ExpresionTipo inicDim2() throws Exception {
		if(!token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_LLAVE)){
			gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \"{\"");
			return ExpresionTipo.getError();
			//ruptura=parse.size();
		}
		else{
			parse.add(28);
			nextToken();
			//inicDim3();
			ExpresionTipo INIC_DIM2_tipo_s=inicDim3();
			if(!token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)){
				gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \"}\"");
				return ExpresionTipo.getError();
				//ruptura=parse.size();
			}
			else{
				nextToken();
			}
		}
		return ExpresionTipo.getVacio();
	}

	/**	29. INIC_DIM3 → LITERAL INIC_DIM4 
	 * 					{  if ((LITERAL.tipo_s != error_tipo) && (INIC_DIM4.tipo_s != error_tipo))                                                                                                    {  if ((LITERAL.tipo_s != error_tipo) && (INIC_DIM4.tipo_s != error_tipo))
     *					   then INIC_DIM3.tipo_s := vacio
     *					   else INIC_DIM3.tipo_s := error_tipo  } 
	 *	30. INIC_DIM3 → INIC_DIM2 INIC_DIM5
	 *					{  if((INIC_DIM2.tipo_s != error_tipo) && (INIC_DIM5.tipo_s != error_tipo)) 
     *					   then INIC_DIM3.tipo_s := vacio
     *					   else INIC_DIM3.tipo_s := error_tipo  } 
	 * @throws Exception 
	 */
	private ExpresionTipo inicDim3() throws Exception {
		if(!token.esIgual(TipoToken.EOF)){
			ExpresionTipo LITERAL_TIPOSIMPLE=literal();
			if(LITERAL_TIPOSIMPLE != null){ // El metodo literal() lee el siguiente token
				parse.add(29);
				//inicDim4();
				ExpresionTipo INIC_DIM4_tipo_s=inicDim4();
				if(LITERAL_TIPOSIMPLE.getTipoBasico()!=TipoBasico.error_tipo && INIC_DIM4_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)
					return ExpresionTipo.getVacio();
				else
					return ExpresionTipo.getError();
			}
			else{
				parse.add(30);
				//inicDim2();
				//inicDim5();
				ExpresionTipo INIC_DIM2_tipo_s,INIC_DIM5_tipo_s;
				INIC_DIM2_tipo_s=inicDim2();
				INIC_DIM5_tipo_s=inicDim5();
				if(INIC_DIM2_tipo_s.getTipoBasico()!=TipoBasico.error_tipo && INIC_DIM5_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)
					return ExpresionTipo.getVacio();
				else
					return ExpresionTipo.getError();
			}
		}
		else {
			gestorErr.insertaErrorSintactico(linea, columna,"Fin de fichero inesperado");
			return ExpresionTipo.getError();
			//ruptura=parse.size();
		}
	}

	/**	31. INIC_DIM4 → , LITERAL INIC_DIM4 
	 * 					{  if ((LITERAL.tipo_s != error_tipo) && (INIC_DIM4.tipo_s != error_tipo))
     *					   then INIC_DIM4.tipo_s := vacio
     *					   else INIC_DIM4.tipo_s := error_tipo  } 
	 *	32. INIC_DIM4 → lambda
	 *					{ INIC_DIM4.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo inicDim4() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.COMA)){
			parse.add(31);
			nextToken();
			ExpresionTipo LITERAL_TIPOSIMPLE=literal();
			if(LITERAL_TIPOSIMPLE == null){
				gestorErr.insertaErrorSintactico(linea, columna,"Falta token literal");
				return ExpresionTipo.getError();
				//ruptura=parse.size();
			}
			else{
				//inicDim4();
				ExpresionTipo INIC_DIM4_tipo_s=inicDim4();
				if(LITERAL_TIPOSIMPLE.getTipoBasico()!=TipoBasico.error_tipo && INIC_DIM4_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)
					return ExpresionTipo.getVacio();
				else
					return ExpresionTipo.getError();
			}
		}
		else{
			parse.add(32);
			return ExpresionTipo.getVacio();
		}
		
	}

	/**	33. INIC_DIM5 → , INIC_DIM2 INIC_DIM5 
	 *					{  if((INIC_DIM2.tipo_s != error_tipo) && (INIC_DIM5.tipo_s != error_tipo)) 
     *					   then INIC_DIM3.tipo_s := vacio
     *					   else INIC_DIM3.tipo_s := error_tipo  } 
	 *	34. INIC_DIM5 → lambda 
	 *					{ INIC_DIM5.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo inicDim5() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.COMA)){
			parse.add(33);
			nextToken();
			//inicDim2();
			//inicDim5();
			ExpresionTipo INIC_DIM2_tipo_s,INIC_DIM5_tipo_s;
			INIC_DIM2_tipo_s=inicDim2();
			INIC_DIM5_tipo_s=inicDim5();
			if(INIC_DIM2_tipo_s.getTipoBasico()!=TipoBasico.error_tipo && INIC_DIM5_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)
				return ExpresionTipo.getVacio();
			else
				return ExpresionTipo.getError();
		}
		else{
			parse.add(34);
			return ExpresionTipo.getVacio();
		}
	}

	/**
	 * 35. INIC_CONST → , ID = LITERAL INIC_CONST
	 * 					{ INIC_CONST1.tipo_s := LITERAL.TIPOSIMPLE } 
	 *   				  if ((INIC_CONST1.tipo_s != error_tipo))                                                                                                     {  if ((LITERAL.tipo_s != error_tipo) && (INIC_CONST.tipo_s != error_tipo))
     *					  then INIC_CONST.tipo_s := vacio
     *					  else INIC_CONST.tipo_s := error_tipo  } 
	 * 36. INIC_CONST → lambda
	 * @throws Exception 
	 */
	private ExpresionTipo inic_const(ExpresionTipo tipo) throws Exception {
//		System.out.println("declaracion constante " + entradaTS.getLexema());
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(35);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				idConst(tipo); // = LITERAL
				inic_const(tipo);
				if((!tipo.equals(TipoBasico.error_tipo))){
					return ExpresionTipo.getVacio();
				}
				else{
					return ExpresionTipo.getError();
				}
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,
						"Falta un identificador");
				//ruptura=parse.size();
			}	
		}
		else { ///Si es lambda
			parse.add(36);
			System.out.println("36");//TODO
			return ExpresionTipo.getVacio();
		}
		return ExpresionTipo.getVacio();	
	}

	
	/**
	 * 37. DECLARACIONES → , ID INICIALIZACION DECLARACIONES
	 * 						{ INICIALIZACION.tipo_h := DECLARACIONES.tipo_h;
   	 *						  DECLARACIONES'.tipo_h := DECLARACIONES.tipo_h;
   	 *						  if (ID.tipo_s == DECLARACIONES.tipo_h) && (INICIALIZACION.tipo_s != error_tipo) && (DECLARACIONES.tipo_s != error_tipo)
   	 *						  then DECLARACIONES.tipo_s := vacio
   	 *						  else DECLARACIONES.tipo_s := error_tipo }
	 * 38. DECLARACIONES → lambda
	 * 			{ DECLARACIONES.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo declaraciones(ExpresionTipo tipo_h) throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(37);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				id();
				//inicializacion(); 
				ExpresionTipo INICIALIZACION_tipo_h=inicializacion(tipo_h);
				//declaraciones();
				ExpresionTipo DECLARACIONES_tipo_h=declaraciones(tipo_h);
//EXCEPCION		//ExpresionTipo tipo_s = new Objeto(((EntradaTS)token.getAtributo()).getLexema());
/*MIRAR*/		if(/*(tipo_s.getTipoNoBasico()==DECLARACIONES_tipo_h.getTipoNoBasico())&&**/(INICIALIZACION_tipo_h.getTipoBasico()!=TipoBasico.error_tipo)&&(DECLARACIONES_tipo_h.getTipoBasico()!=TipoBasico.error_tipo))
					return ExpresionTipo.getVacio();
				else
					return ExpresionTipo.getError();
				
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el identificador. Palabra o termino \""+token.atrString()+"\" inesperado.");
				return ExpresionTipo.getError();
				//ruptura=parse.size();
			}		
		} else {
			parse.add(38);
			return ExpresionTipo.getVacio();
		}
	}
	
	/**
	 * 39. INICIALIZACION → OP_ASIGNACION ASSIGNMENT_EXPRESSION
	 * 						{ if (ASSIGNMENT_EXPRESSION.tipo_s == INICIALIZACION.tipo_s) 
   	 *						  then INICIALIZACION.tipo_s := vacio
   	 *						  else INICIALIZACION.tipo_s := error_tipo }
	 * 40. INICIALIZACION → [NUM_ENTERO] DIMENSION INIC_DIM
	 * 						{ if((NUM_ENTERO_tipo!=error_tipo)&&(DIMENSION_tipo!=error_tipo)&&(INIC_DIM_tipo_s!=error_tipo))
	 * 						  then INICIALIZACION.tipo_s:=vacio
	 * 						  else INICIALIZACION.tipo_s:=error_tipo}
	 * 41. INICIALIZACION → lambda
	 * 						{ INICIALIZACION.tipo := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo inicializacion(ExpresionTipo tipo_h) throws Exception {
		if(token.esIgual(TipoToken.OP_ASIGNACION)) {
			parse.add(39);
			nextToken();
			//assignment_expression();
			ExpresionTipo ASSIGNMENT_EXPRESSION_tipo_s=assignment_expression();
			if(ASSIGNMENT_EXPRESSION_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)
				return ExpresionTipo.getVacio();
			else 
				return ExpresionTipo.getError();			
		}
		else if(token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_CORCHETE)){
			parse.add(40);
			nextToken();
			if(token.esIgual(TipoToken.NUM_ENTERO )){
				nextToken();
				if(token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_CORCHETE)){
					nextToken();
					ExpresionTipo tipoSimple= new ExpresionTipo(TipoBasico.entero);
					ExpresionTipo DIMENSION_tipo=dimension();
					ExpresionTipo INIC_DIM_tipo_s=inicDim();
					if((tipoSimple.getTipoBasico()!=TipoBasico.error_tipo)&&(DIMENSION_tipo.getTipoBasico()!=TipoBasico.error_tipo)&&(INIC_DIM_tipo_s.getTipoBasico()!=TipoBasico.error_tipo))
						return ExpresionTipo.getVacio();
					else 
						return ExpresionTipo.getError();
				}
				else{
					gestorErr.insertaErrorSintactico(linea, columna,"Falta separador \"]\"");
					//ruptura=parse.size();
				}
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna,
						"Se espera un numero entero (tamaño del array)");
				//ruptura=parse.size();
			}
		} else {
			parse.add(41);
			return ExpresionTipo.getVacio();
		}
		return null;
	}

	/**
	 * 42. INSTRUCCION → ID :
	 *  				 {  INSTRUCCION_tipo_s:=vacio }
	 * 43. INSTRUCCION → struct RESTO_ST
	 * 					 {	if RESTO_ST_tipo_s!=error_tipo 
	 * 						then INSTRUCCION_tipo_s:=vacio
	 * 						else INSTRUCCION_tipo_s:=error_tipo}
	 * 44. INSTRUCCION → cin INS_LECT 
	 * 					 {	if INS_LECT_tipo_s!=error_tipo 
	 * 						then INSTRUCCION_tipo_s:=vacio
	 * 						else INSTRUCCION_tipo_s:=error_tipo}
	 * 45. INSTRUCCION → cout INS_ESC
	 * 					 {	if INS_ESC_tipo_s!=error_tipo 
	 * 						then INSTRUCCION_tipo_s:=vacio
	 * 						else INSTRUCCION_tipo_s:=error_tipo}
	 * 46. INSTRUCCION → const INS_DEC
	 * 					 {	if INS_DEC_tipo_s!=error_tipo 
	 * 						then INSTRUCCION_tipo_s:=vacio
	 * 						else INSTRUCCION_tipo_s:=error_tipo}
	 * 47. INSTRUCCION → TIPO INS_DEC2
	 * 					{ INS_DEC2.tipo := TIPO.tipo;
	 * 					  if (TIPO.tipo != error_tipo) & INS_DEC2.tipo != error_tipo)
	 * 					  then INSTRUCCION.tipo := vacio
	 * 					  else INSTRUCCION.tipo := error_tipo }
	 * 48. INSTRUCCION → ;
	 * 133.INSTRUCCION → EXPRESSION_OPT ;
	 * 					{ INSTRUCCION.tipo := EXPRESSION_OPT.tipo }
	 * @throws Exception 
	 */
	
	private ExpresionTipo instruccion() throws Exception {
		
		if(token.esIgual(TipoToken.PAL_RESERVADA, 54 /*struct*/ )){ //INS_REG
			parse.add(43);
			nextToken();
			//resto_st();
			ExpresionTipo RESTO_ST_tipo_s=resto_st();
			if(RESTO_ST_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)
				return ExpresionTipo.getVacio();
			else
				return ExpresionTipo.getError();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA, 74 /*cin*/ )){ //INS_LECT
			parse.add(44);
			nextToken();
			//ins_lect();
			ExpresionTipo INS_LECT_tipo_s=ins_lect();
			if(INS_LECT_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)
				return ExpresionTipo.getVacio();
			else
				return ExpresionTipo.getError();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA, 75 /*cout*/ )){ //INS_ESC
			parse.add(45);
			nextToken();
			//ins_esc(); 
			ExpresionTipo INS_ESC_tipo_s=ins_esc();
			if(INS_ESC_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)
				return ExpresionTipo.getVacio();
			else
				return ExpresionTipo.getError();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA, 9 /*const*/ )){ //INS_DEC
			parse.add(46); 
			nextToken();
			//ins_dec();
			ExpresionTipo INS_DEC_tipo_s=ins_dec();
			if(INS_DEC_tipo_s.getTipoBasico()!=TipoBasico.error_tipo)
				return ExpresionTipo.getVacio();
			else
				return ExpresionTipo.getError();
		}
		else{
			ExpresionTipo TIPO_tipo = tipo();
			lexico.setModoNoMeto(true);
			if(tokenAnterior.esIgual(TipoToken.IDENTIFICADOR) && token.esIgual(TipoToken.SEPARADOR, Separadores.DOS_PUNTOS)) {
				parse.add(42); 
				String etiqueta = (String) tokenAnterior.getAtributo();
				this.etiquetasSinGoto.put(etiqueta, etiqueta);
			}
			lexico.setModoNoMeto(false);
			lexico.setModoDeclaracion(true);
			if(TIPO_tipo.getTipoBasico() != TipoBasico.vacio) {
				if(token.esIgual(TipoToken.IDENTIFICADOR)){
					parse.add(47); 
					ExpresionTipo INS_DEC2_tipo = ins_dec2(TIPO_tipo);
					if(TIPO_tipo.getTipoBasico() != TipoBasico.error_tipo && INS_DEC2_tipo.getTipoBasico() != TipoBasico.error_tipo)
						return ExpresionTipo.getVacio();
					else
						return ExpresionTipo.getError();
				} else {
					ventana.add(tokens.lastElement()); // el ultimo token
					token = tokens.get(tokens.size()-2); // el penultimo token (tipo (id))
					parse.add(133); // creo que deberia añadir otra regla
					
					expression();
					if(token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
						nextToken();
					}else{
						//error
						gestorErr.insertaErrorSintactico(linea, columna,
								"Falta separador \";\"");
						//ruptura=parse.size();
					}
				}
			} 
			else if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) { //INS_VACIA
				parse.add(48);
				nextToken();
				return ExpresionTipo.getVacio();
			} else{
				parse.add(133);
				ExpresionTipo aux1 = expressionOpt();
				if(token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
					nextToken();
					return aux1;
				}else{
					return ExpresionTipo.getError();
				}
			}
		}
		return ExpresionTipo.getVacio();
	}
	
	
	/**
	 * 51. INS_DEC → TIPO PUNT ID OpAsignacion LITERAL INIC_CONST ;
	 * @throws Exception 
	 */
	private ExpresionTipo ins_dec() throws Exception {
		ExpresionTipo aux1,aux2;
		aux1= tipo();
		//if (tipo()) {
		if(aux1!=null){
			parse.add(51);
			//nextToken();
			//aux2=punt();
			if(punt())
			//if(aux2!=null)
				nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
				nextToken();
				if (token.esIgual(TipoToken.OP_ASIGNACION, OpAsignacion.ASIGNACION)) {
					nextToken();
					if (esLiteral()) {
						nextToken();
					//	inic_const(); //no lo tengo muy claro
						if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
							nextToken();
						} else {
							//error
							gestorErr.insertaErrorSintactico(linea, columna, 
									"Falta separador \";\"");
							//ruptura=parse.size();
						}
					} else {
						//error
						gestorErr.insertaErrorSintactico(linea, columna, 
								"Falta valor literal");
						//ruptura=parse.size();
					}
				} else {
					gestorErr.insertaErrorSintactico(linea, columna, 
							"Falta operador \"=\"");
					//ruptura=parse.size();
				}
			} else {
				// error
				gestorErr.insertaErrorSintactico(linea, columna, 
						"Falta identificador");
				//ruptura=parse.size();
			}
		}
		return null;
		
	}
	
	
	/**
	 * 49.PUNT → *
	 * 50.PUNT → lambda
	 */
	private boolean punt() {
		if (token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.MULTIPLICACION)) {
			parse.add(49);
			return true;
		} else {
			parse.add(50);
			return false;
		}
	}
	
	/**
	 * 52. INS_DEC2 → PUNT ID MAS_COSAS
	 *  //El id no tiene que estar declarado 
	 *               
	 * 				{ if(PUNT.tipo != vacio) 
	 * 				  then MAS_COSAS.tipo_h := puntero(INS_DEC2.tipo_h)
	 * 				  else MAS_COSAS.tipo_h := INS_DEC2.tipo_h;
	 * 				  INS_DEC2.tipo_s := MAS_COSAS.tipo_s 
	 *  }
	 * @throws Exception 
	 */
	private ExpresionTipo ins_dec2(ExpresionTipo tipo_h) throws Exception {
		ExpresionTipo aux1,aux2,aux3;
		aux1 = aux2 = aux3 = null;
		if(punt()){
		//aux1 = punt();
		//if(aux1!=null){
			nextToken();
			aux2 = new Puntero(tipo_h);
		}
		if(token.esIgual(TipoToken.IDENTIFICADOR)){
			//System.out.println("Si es identificador (repetimos)...");
			parse.add(52);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			//TODO: completar la TS con aux2 / tipo_h segun sea 
			
			/*if(aux2!=null)
				System.out.println("En linea 1412 completaremos con tipo: "+aux2.getTipoNoBasico().toString());
			else
				System.out.println("En linea 1412 completaremos con tipo: "+tipo_h.getTipoBasico().toString());*/
			
			nextToken();
			
			/*aux3 = mas_cosas(tipo_h);
			if(aux1.getTipoBasico()!=TipoBasico.error_tipo && aux3.getTipoBasico()!=TipoBasico.error_tipo)
				return ExpresionTipo.getVacio();
			else
				return ExpresionTipo.getError();*/
			
			return mas_cosas(tipo_h);
		} else {
			// error
			gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba un identificador");
			return null;
			//ruptura=parse.size();
		}
	}
	
	/**
	 * 53. MAS_COSAS --> INICIALIZACION DECLARACIONES ;
	 * 						{ INICIALIZACION.tipo_h := MAS_COSAS.tipo_h;
	 * 						  DECLARACIONES.tipo_h := MAS_COSAS.tipo_h;
	 * 						  if (INICIALIZACION.tipo != error_tipo) & DECLARACIONES.tipo != error_tipo)
	 * 						  then MAS_COSAS.tipo := vacio
	 * 						  else MAS_COSAS.tipo := error_tipo }
	 * ............ANTERIOR:........ 53. MAS_COSAS --> = EXPRESSION ;
	 * @throws Exception 
	 */
	private ExpresionTipo mas_cosas(ExpresionTipo tipo_h) throws Exception {
		ExpresionTipo aux1,aux2;
		parse.add(53);
		aux1 = inicializacion(tipo_h);
		aux2 = declaraciones(tipo_h);
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
			nextToken();
			if(aux1.getTipoBasico()!=TipoBasico.error_tipo && aux2.getTipoBasico()!=TipoBasico.error_tipo)
				return ExpresionTipo.getVacio();
			else
				return ExpresionTipo.getError();
		}else{
			gestorErr.insertaErrorSintactico(linea, columna,"Palabra o termino \""+token.atrString()+"\" inesperado.");//+lexico.getLexema()+"\" inesperado.");
			return null;
		}
	}
	
	/**
	 * 60. RESTO_ST → ID { CUERPO_ST } ID NOMBRES
	 * { 
	 * 	 if  (CUERPO_ST.tipo != error_tipo )
       	 NOMBRES.tipo_h  = Registro(CUERPO_ST.tipo) 
         	if (NOMBRES.tipo != error_tipo) and  (consulta(id1.lexema) == null //El id1 no tiene que estar declarado
           		and consulta(id.lexema) != Registro //El id puede estar declarado pero no como registro) then 
           		RESTO_ST.tipo = vacio 
           		inserta(id.entrada, TIPO_SEM, Registro) // Este id identifica al registro pero no se puede usar
           		inserta(id1.entrada,TIPO_SEM, Registro(CUERPO_ST.tipo) )
         	else RESTO_ST.tipo = error_tipo
         else RESTO_ST.tipo = error_tipo 
        } 
	 * 61. RESTO_ST → { CUERPO_ST } ID NOMBRES
	 * {
	 *  if  (CUERPO_ST.tipo != error_tipo )
      		NOMBRES.tipo_h  = Registro(CUERPO_ST.tipo) 
       		if (NOMBRES.tipo != error_tipo) and  (consulta(id.lexema) == null // El id no tiene que estar declarado) then 
           		RESTO_ST.tipo = vacio 
           		inserta(id.entrada,TIPO_SEM, Registro(CUERPO_ST.tipo) )
      		else RESTO_ST.tipo = error_tipo
 		else RESTO_ST.tipo = error_tipo  
 		} 

	 * @throws Exception 
	 **/
	private ExpresionTipo resto_st() throws Exception {
		ExpresionTipo CUERPO_ST_tipo;
		ExpresionTipo NOMBRES_tipo_h;
		ExpresionTipo NOMBRES_tipo;
		if(token.esIgual(TipoToken.IDENTIFICADOR)){ 
			String IDlexema = token.atrString();
			parse.add(60);
			//TIPO DEFINIDO No se puede usar
			gestorTS.buscaIdBloqueActual(IDlexema).setTipo(new ExpresionTipo(TipoNoBasico.registro));
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)){
				lexico.setModoNoMeto(true);
				nextToken();
				CUERPO_ST_tipo = cuerpo_st();
				if(CUERPO_ST_tipo.getTipoNoBasico() == TipoNoBasico.producto)
					NOMBRES_tipo_h = new Registro((Producto)CUERPO_ST_tipo);
				else if (CUERPO_ST_tipo.getTipoBasico() == TipoBasico.vacio)
					NOMBRES_tipo_h = new Registro(null);
				else return ExpresionTipo.getError();
				
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)){
					lexico.setModoDeclaracion(true);
					nextToken();
					if(token.esIgual(TipoToken.IDENTIFICADOR)){
						String IDlexema1 = token.atrString();
						gestorTS.buscaIdBloqueActual(IDlexema1).setTipo(NOMBRES_tipo_h);
						nextToken();
						NOMBRES_tipo = nombres(NOMBRES_tipo_h);
						if(NOMBRES_tipo.getTipoBasico() != TipoBasico.error_tipo)
				        { 
				           	return ExpresionTipo.getVacio(); 
				        }
				        else {
							return ExpresionTipo.getError();	
				        }	
					} else {
						gestorErr.insertaErrorSintactico(linea, columna, "Falta la identificacion de la estructura");
					}
				} else{
					gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador cierra llave }");
				}
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador abre llave {");
			}
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)){
			parse.add(61);
			lexico.setModoNoMeto(true);
			nextToken();
			CUERPO_ST_tipo = cuerpo_st();
				
			if(CUERPO_ST_tipo.getTipoNoBasico() == TipoNoBasico.producto)
				NOMBRES_tipo_h = new Registro((Producto)CUERPO_ST_tipo);
			else if (CUERPO_ST_tipo.getTipoBasico() == TipoBasico.vacio)
				NOMBRES_tipo_h = new Registro(null);
			else { 
				return ExpresionTipo.getError();	
			}
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)){
				lexico.setModoDeclaracion(true);
				nextToken();
				if(token.esIgual(TipoToken.IDENTIFICADOR)){
					String IDlexema1 = token.atrString();
					gestorTS.buscaIdBloqueActual(IDlexema1).setTipo(NOMBRES_tipo_h);
					nextToken();
					NOMBRES_tipo = nombres(NOMBRES_tipo_h);
					if (NOMBRES_tipo.getTipoBasico() != TipoBasico.error_tipo)  
			           	return ExpresionTipo.getVacio(); 
			        else
			        	return ExpresionTipo.getError();
				} else {
					gestorErr.insertaErrorSintactico(linea, columna, "Falta la identificacion de la estructura");
				}
			} else {
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador cierra llave }");
			}
		} else{
			gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador abre llave { o identificacion de la estructura");
		}
		return null;
	}
	
	
	/**
	 * 62. CUERPO_ST → TIPO ID RESTO_VAR CUERPO_ST
	 * {
	 *  RESTO_VAR.tipo_h = TIPO.tipo
 		if  (TIPO.tipo != error_tipo) and (RESTO_VAR.tipo != error_tipo) and (CUERPO_ST1.tipo != error_tipo ) and (consulta(id.lexema) == null // El id no tiene que estar declarado  ) then
     		CUERPO_ST.tipo = Producto(RESTO_VAR.tipo, CUERPO_ST1.tipo)
 		else CUERPO_ST.tipo = error_tipo  
 		} 
	 * 63. CUERPO_ST → lambda
	 * {
	 * 	RESTO_TIPO.tipo = vacio 
	 * }
	 * @throws Exception 
	 */
	private ExpresionTipo cuerpo_st() throws Exception {
		ExpresionTipo TIPO_tipo = tipo(); 
		ExpresionTipo RESTO_VAR_tipo;
		ExpresionTipo CUERPO_ST_tipo;
		String IDlexema;
		if(TIPO_tipo.getTipoBasico() != TipoBasico.vacio){
			parse.add(62);
			if (token.esIgual(TipoToken.IDENTIFICADOR)) {
				IDlexema = (String)token.getAtributo();
				nextToken();
				RESTO_VAR_tipo = resto_var(TIPO_tipo);
				CUERPO_ST_tipo = cuerpo_st();
				if(RESTO_VAR_tipo.getTipoBasico() != TipoBasico.error_tipo && CUERPO_ST_tipo.getTipoBasico() != TipoBasico.error_tipo )
				{ 
					try{
					if(RESTO_VAR_tipo.getTipoNoBasico() == TipoNoBasico.producto && CUERPO_ST_tipo.getTipoNoBasico() == TipoNoBasico.producto){	
						(((Producto)CUERPO_ST_tipo)).ponProductos(((Producto)RESTO_VAR_tipo).getTablaProd());
						((Producto)CUERPO_ST_tipo).ponProducto(IDlexema, TIPO_tipo);
						return  CUERPO_ST_tipo;
					}
					if(RESTO_VAR_tipo.getTipoNoBasico() == TipoNoBasico.producto) {
							((Producto)RESTO_VAR_tipo).ponProducto(IDlexema, TIPO_tipo);
							return RESTO_VAR_tipo;
					}					
					if(CUERPO_ST_tipo.getTipoNoBasico() == TipoNoBasico.producto) {
							((Producto)CUERPO_ST_tipo).ponProducto(IDlexema, TIPO_tipo);
							return CUERPO_ST_tipo;
					}	
					return new Producto(IDlexema,TIPO_tipo);
					}
					catch(Exception e) { //Si algun id esta repetido en el struct Producto lanza excepcion y se mete por aqui
						gestorErr.insertaErrorSemantico(linea, columna, "Multiple declaracion de "+IDlexema+" en el struct");    
						return ExpresionTipo.getError();
					}
				}	
				else 
					return ExpresionTipo.getError();
				
			}
			else {
				gestorErr.insertaErrorSintactico(linea, columna, "Los atributos deben estar identificados");
				return null;
			}
		} else
			parse.add(63);
			return ExpresionTipo.getVacio();
	}
	
	
	/**
	 * 64. RESTO_VAR → , ID RESTO_VAR
	 * { 
	 *    RESTO_VAR1.tipo_h = RESTO_VAR.tipo_h
 		  if (RESTO_VAR1.tipo_s != error_tipo) and  (consulta(id.lexema) == null // El id no tiene que estar declarado  ) then 
 		  	RESTO_VAR.TIPO = Producto(Producto(id.lexema,RESTO_VAR.tipo_h), RESTO_VAR1.tipo)
 		  else RESTO_VAR.TIPO = error_tipo 
 		}
 	 * 65. RESTO_VAR → ;
 	 * { 
 	 *     RESTO_VAR.tipo = vacio
 	 *  }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_var(ExpresionTipo tipo_h) throws Exception {
		ExpresionTipo RESTO_VAR1_tipo_h = tipo_h;
		ExpresionTipo RESTO_VAR1_tipo;
		String IDlexema;
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)){ 
			parse.add(64);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				IDlexema = (String)token.getAtributo();
				nextToken();
				RESTO_VAR1_tipo = resto_var(RESTO_VAR1_tipo_h);
				
				if(RESTO_VAR1_tipo.getTipoBasico() == TipoBasico.vacio)
					 return new Producto(IDlexema,tipo_h);
				else if( RESTO_VAR1_tipo.getTipoNoBasico() == TipoNoBasico.producto) {
					try {
						((Producto)RESTO_VAR1_tipo).ponProducto(IDlexema, tipo_h);
						return RESTO_VAR1_tipo;
					}
					catch(Exception e){
						gestorErr.insertaErrorSemantico(linea, columna, "Multiple declaracion de "+IDlexema+" en el struct");    
						return ExpresionTipo.getError();
					}
				}
				return ExpresionTipo.getError();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna, 
						"Se deben identificar todos los atributos");
				return null;
			}
			
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
			parse.add(65);
			nextToken();
			return ExpresionTipo.getVacio();
		} else {
			gestorErr.insertaErrorSintactico(linea, columna, 
					"Se esperaba un separador coma (,) o punto_coma (;)");
			return null;
		}
	}
	
	
	/**
	 * 66. NOMBRES → , ID NOMBRES
	 * { 
	 *    NOMBRES1.tipo_h = NOMBRES.tipo_h     
		  if (NOMBRES1.tipo != error_tipo) and (consulta(id.lexema) == null //El id no tiene que estar declarado
          inserta(ID.entrada,TIPO_SEM, NOMBRES.tipo_h)  
          NOMBRES.tipo = vacio
          else NOMBRES.tipo = error_tipo 
        }
	 * 67. NOMBRES → ;
	 * { 
	 *  NOMBRES.tipo = vacio
	 * }
	 * @param nombres_tipo_h 
	 * @throws Exception 
	 */
	private ExpresionTipo nombres(ExpresionTipo nombres_tipo_h) throws Exception {
		ExpresionTipo NOMBRES1_tipo_h = nombres_tipo_h;
		ExpresionTipo NOMBRES1_tipo;
		String IDlexema;
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)){ 
			parse.add(66);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				IDlexema = token.atrString();
				gestorTS.buscaIdBloqueActual(IDlexema).setTipo(nombres_tipo_h);
				nextToken();
				NOMBRES1_tipo = nombres(NOMBRES1_tipo_h);
				if(NOMBRES1_tipo.getTipoBasico() != TipoBasico.error_tipo)
					return ExpresionTipo.getVacio();
				else
					return ExpresionTipo.getError();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,
						"Se deben identificar todas las variables de la estructura");
			}
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
			parse.add(67);
			nextToken();
			return ExpresionTipo.getVacio();
		} else {
			gestorErr.insertaErrorSintactico(linea, columna, 
					"Se esperaba un separador coma (,) o punto_coma (;)");
		}
		return null;
	}
	
		
	/**
	 * 68. INS_LECT → >>  RESTO_LECT
	 * { INS_LECT.tipo = RESTO_LECT.tipo } 
	 * @throws Exception 
	 */
	private ExpresionTipo ins_lect() throws Exception { 
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MAYORES)){
			parse.add(68);
			lexico.setModoDeclaracion(false); lexico.setModoNoMeto(false);//comienza el modo "uso de variables"
			nextToken();
			return resto_lect();
		}
		else{
			gestorErr.insertaErrorSintactico(linea, columna,
					"Lectura incorrecta, se esperaba el operador \">>\"");
			return null;
		}
	}
	
	/**
	 * 69. RESTO_LECT → ID ;
	 * { if (consulta(id.lexema) != null) then   //el id tiene que estar declarado
     			RESTO_LECT.tipo = vacio
		   else
     			RESTO_LECT.tipo = error_tipo }
	 * 70. RESTO_LECT → LITERAL  ;
	 * { RESTO_LECT.tipo = vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_lect() throws Exception {
		if(token.esIgual(TipoToken.IDENTIFICADOR)){ 
			parse.add(69);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
				lexico.setModoDeclaracion(true);//termina el modo "uso variables"
				nextToken();
				return ExpresionTipo.getVacio();
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna,
						"Lectura terminada incorrectamente, falta ';'" +
						"Palabra o termino "+token.atrString()+" inseperado.");
				return null;
			}
		}
		else if(esLiteral()){
			parse.add(70);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
				lexico.setModoDeclaracion(true);//termina modo "uso variables"
				nextToken();
				return  ExpresionTipo.getVacio();
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna,
						"Lectura terminada incorrectamente, falta ';'");
				return null;
			}
		}
		else{
			gestorErr.insertaErrorSintactico(linea, columna,
					"Lectura incorrecta, se esperaba un literal o una variable");
			return null;
		}
	}
	
	/**
	 * 71. INS_ESC → << RESTO_ESC
	 * { INS_ESC.tipo = RESTO_ESC.tipo }
	 * @throws Exception 
	 */
	private ExpresionTipo ins_esc() throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MENORES)){
			parse.add(63);
			lexico.setModoDeclaracion(false); lexico.setModoNoMeto(false);//comienza el modo "uso de variables"
			nextToken();
			return resto_esc();
		}
		else{
			gestorErr.insertaErrorSintactico(linea, columna,
					"Escritura incorrecta, se esperaba el operador \"<<\"");
			return null;
		}
	}
	
	/**
	 * 72. RESTO_ESC →  LITERAL INS_ESC2
	 *  {  RESTO_ESC.tipo = INS_ESC2.tipo }
	 * 73. RESTO_ESC →  ID INS_ESC2
           RESTO_ESC.tipo = INS_ESC2.tipo }
	 * 74. RESTO_ESC →  endl INS_ESC2
	 *  {  RESTO_ESC.tipo = INS_ESC2.tipo }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_esc() throws Exception {
		if(esLiteral()){
			parse.add(72);
			nextToken();
			return ins_esc2();
		}
		else if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(73);
			nextToken();
			return ins_esc2();		
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,76 /*endl*/)){ 
			parse.add(74);
			nextToken();
			return ins_esc2();
		}
		else{
			gestorErr.insertaErrorSintactico(linea, columna,
					"Escritura incorrecta, se esperaba un literal, una variable o la palabra reservada \"endl\"");
			return null;
		}
	}
	
	/**
	 * 75. INS_ESC2 →  << RESTO_ESC
	 * { INS_ESC2.tipo = RESTO_ESC.tipo }
	 * 76. INS_ESC2 →  ;
	 * { INS_ESC.tipo = vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo ins_esc2() throws Exception{
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MENORES)){
			parse.add(75);
			nextToken();
			return resto_esc();
		}
		else if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
			parse.add(76);
			lexico.setModoDeclaracion(true);//termina el modo uso de variables
			nextToken();
			return ExpresionTipo.getVacio();
		}
		else{
			gestorErr.insertaErrorSintactico(linea, columna,
					"Escritura terminada incorrectamente, falta ';'");
			return null;
		}
	}

	/**
	 * 81. CUERPO --> for RESTO_FOR CUERPO
	 * { if (RESTO_FOR.tipo != error_tipo) then 
     	     CUERPO.tipo = CUERPO1.tipo
		 else CUERPO.tipo = error_tipo 
	   }

	 * 82. CUERPO --> do RESTO_DO CUERPO
	 * {
	 *  if (RESTO_DO.tipo != error_tipo) then 
     	CUERPO.tipo = CUERPO1.tipo
		else CUERPO.tipo = error_tipo
		}

	 * 83. CUERPO --> while RESTO_WHILE CUERPO
	 * {
	 *  if (RESTO_WHILE.tipo != error_tipo) then 
		  CUERPO.tipo = CUERPO1.tipo
		else CUERPO.tipo = error_tipo
		}

	 * 84. CUERPO --> if RESTO_IF CUERPO
	 * { 
	 *   if (RESTO_IF.tipo != error_tipo) then
		      CUERPO.tipo = CUERPO1.tipo 
		  else CUERPO.tipo = error_tipo 
		 }

	 * 85. CUERPO --> switch RESTO_CASE CUERPO
	 * { 
	 *  if (RESTO_CASE.tipo != error_tipo) then 
     	CUERPO.tipo = CUERPO1.tipo
		else CUERPO.tipo = error_tipo 
		}

	 * 86. CUERPO --> lambda
	 * 	{
	 *  CUERPO.tipo := vacio 
	 *  }
	 *  
	 * 104. CUERPO → { CUERPO } CUERPO
	 * { if(CUERPO1.tipo != error_tipo) and  (CUERPO2 != error_tipo) then 
       	 	CUERPO.tipo = vacio
		 else CUERPO.tipo = error_tipo
	   }
	 * 128. CUERPO → break ; CUERPO
	 * 			{ if (estamosEnBucle o estamosEnSwitch) CUERPO.tipo := CUERPO1.tipo
	 * 			  else CUERPO.tipo := error_tipo }
	 * 129. CUERPO → continue ; CUERPO
	 * 			{ if (estamosEnBucle) CUERPO.tipo := CUERPO1.tipo
	 * 			  else CUERPO.tipo := error_tipo }
	 * 130. CUERPO → return EXPRESSIONOPT; CUERPO
	 * 131. CUERPO → goto ID ; CUERPO
	 * 132. CUERPO → INSTRUCCION CUERPO
	 * 				{ if (INSTRUCCION.tipo != error.tipo) & (CUERPO'.tipo != error.tipo)
	 * 				  then CUERPO.tipo := vacio
	 * 				  else CUERPO.tipo := error_tipo } 
	 * @throws Exception 
	 */
	private ExpresionTipo cuerpo() throws Exception
	{
		if(token.esIgual(TipoToken.PAL_RESERVADA,29 /*for*/))
		{
			ExpresionTipo RESTO_FOR_tipo;
			parse.add(81);
			nextToken();
			RESTO_FOR_tipo = resto_for();
			if(RESTO_FOR_tipo.getTipoBasico() != TipoBasico.error_tipo)
				return cuerpo();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,19 /*do*/))
		{
			parse.add(82);
			nextToken();
			ExpresionTipo RESTO_DO_tipo = resto_do();
			if(RESTO_DO_tipo.getTipoBasico() != TipoBasico.error_tipo)
				return cuerpo();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,72 /*while*/))
		{
			parse.add(83);
			nextToken();
			ExpresionTipo RESTO_WHILE_tipo = resto_while();
			if(RESTO_WHILE_tipo.getTipoBasico() != TipoBasico.error_tipo)
				return cuerpo();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,32 /*if*/))
		{
			parse.add(84);
			nextToken();
			ExpresionTipo RESTO_IF_tipo = resto_if();
			if(RESTO_IF_tipo.getTipoBasico() != TipoBasico.error_tipo)
				return cuerpo();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,55 /*switch*/))
		{
			parse.add(85);
			nextToken();
			ExpresionTipo RESTO_CASE_tipo = resto_case();
			if(RESTO_CASE_tipo.getTipoBasico() != TipoBasico.error_tipo)
				return cuerpo();
//		}
//		else if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
//			parse.add(86);
//			nextToken();
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			parse.add(104);
			nextToken();
			ExpresionTipo CUERPO1_tipo = cuerpo();
//			cuerpo();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
				nextToken();
				ExpresionTipo CUERPO2_tipo = cuerpo();
				if(CUERPO1_tipo.getTipoBasico() != TipoBasico.error_tipo &&
					CUERPO2_tipo.getTipoBasico() != TipoBasico.error_tipo)
					return ExpresionTipo.getVacio();
				else
					return ExpresionTipo.getError();
				
			} else {
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \"}\"");
			}
		} else if(token.esIgual(TipoToken.PAL_RESERVADA,5 /*break*/)) {
			parse.add(128);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				nextToken();
				if(estamosEnBucle || estamosEnSwitch) 
					return cuerpo();
				else {
					gestorErr.insertaErrorSemantico(linea, columna, "No está permitido usar break fuera de un bucle o switch.");
					return ExpresionTipo.getError();
				}
			} else {
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
			}
		} else if(token.esIgual(TipoToken.PAL_RESERVADA, 12 /*continue*/)) {
			parse.add(129);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				nextToken();
				if(estamosEnBucle) 
					return cuerpo();
				else {
					gestorErr.insertaErrorSemantico(linea, columna, "No está permitido usar continue fuera de un bucle.");
					return ExpresionTipo.getError();
				}
			} else { 
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
			}
		} else if(token.esIgual(TipoToken.PAL_RESERVADA, 47 /*return*/)) {
			parse.add(130);
			nextToken();
			ExpresionTipo aux = expressionOpt();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				nextToken();
				ExpresionTipo tipo = null;
				if(estamosEnFuncion && aux.equals(TipoBasico.vacio)) {
					gestorErr.insertaErrorSemantico(linea, columna, "Falta instrucción de retorno.");
					tipo = ExpresionTipo.getError();
				} else if(!estamosEnFuncion && !aux.equals(TipoBasico.vacio)) {
					gestorErr.insertaErrorSemantico(linea, columna, "No se puede devolver nada. Solo se puede usar 'return;'.");
					tipo = ExpresionTipo.getError();
				}
				aux = cuerpo();
				return tipo.equals(TipoBasico.error_tipo) ? tipo : aux; 
			} else { 
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
			}
		}  else if(token.esIgual(TipoToken.PAL_RESERVADA, 31 /*goto*/)) {
			lexico.setModoNoMeto(true);
			parse.add(128);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				nextToken();
				if(token.getAtributo() instanceof String) {
					etiquetasConGoto.add((String) token.getAtributo());
				}
				lexico.setModoNoMeto(false);
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
					nextToken();
					cuerpo();
				} else { 
					gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
				}
			} else { 
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el identificador");
			}
		} else {
			parse.add(132);
			ExpresionTipo aux1, aux2;
			aux1 = instruccion();
			//if(instruccion())
			//if(aux1!=null){
			if(aux1.getTipoBasico() != TipoBasico.error_tipo){
				//System.out.println("Llamada recursiva a cuerpo...");
				aux2 = cuerpo();
				if(aux1.getTipoBasico() != TipoBasico.error_tipo && aux2.getTipoBasico() != TipoBasico.error_tipo)
					return ExpresionTipo.getVacio();
				else
					return ExpresionTipo.getError();
			}else{
				 if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE) 
						 || token.esIgual(TipoToken.PAL_RESERVADA,6 /*case*/)
						 || token.esIgual(TipoToken.PAL_RESERVADA,17 /*default*/)
						 || token.esIgual(TipoToken.EOF)) {
					parse.add(86); //lambda
					return ExpresionTipo.getVacio();
				 } else {
					gestorErr.insertaErrorSintactico(linea, columna, "Token inesperado.");
					return null;
				 }
			}
		}	
		return ExpresionTipo.getVacio();
	}

	/** 
	 * 87. CUERPO2 --> while RESTO_WHILE
	 * { 
	 *   CUERPO2.tipo =  RESTO_WHILE.tipo 
	 * }
	 * 88. CUERPO2 --> for RESTO_FOR
	 * { 
	 * CUERPO2.tipo =  RESTO_FOR.tipo
	 * }
	 * 89. CUERPO2 --> do RESTO_DO
	 * {
	 *  CUERPO2.tipo =  RESTO_DO.tipo 
	 * }
	 * 90. CUERPO2 --> if RESTO_IF 
	 * { 
	 * CUERPO2.tipo =  RESTO_IF.tipo
	 *  }
	 * 91. CUERPO2 --> switch RESTO_CASE
	 * {
	 *  CUERPO2.tipo =  RESTO_CASE.tipo 
	 *  }
	 * 92. CUERPO2 --> { CUERPO }
	 * { 
	 * CUERPO2.tipo =  CUERPO.tipo
	 * }
	 * 93. CUERPO2 --> INSTRUCCION
	 * { 
	 * CUERPO2.tipo =  INSTRUCCION.tipo 
	 * }

	 * @throws Exception 
	 */
	private ExpresionTipo cuerpo2() throws Exception {
		
		if(token.esIgual(TipoToken.PAL_RESERVADA,72 /*while*/))
		{
			parse.add(87);
			nextToken();
			return resto_while();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,29 /*for*/))
		{
			parse.add(88);
			nextToken();
			return resto_for();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,19 /*do*/))
		{
			parse.add(89);
			nextToken();
			return resto_do();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,32 /*if*/))
		{
			parse.add(90);
			nextToken();
			return resto_if();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,55 /*switch*/))
		{
			parse.add(91);
			nextToken();
			return resto_case();
		}
		else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			ExpresionTipo CUERPO_tipo; 
			nextToken();
			parse.add(92);
			CUERPO_tipo = cuerpo();
			if (!token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)) {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta }");
			}
			return CUERPO_tipo;
			//nextToken();
		}
		else {
			parse.add(93);
			return instruccion();
		}
		/*else 	
			gestorErr.insertaErrorSintactico(linea, columna,
				"Token inesperado... "); */
		
	}
	
	
	/**
	 *  94. RESTO_WHILE --> (EXPRESSION) do CUERPO2
	 *  {
	 *    if(EXPRESSION.tipo == logico) then 
       		RESTO_WHILE.tipo = CUERPO2.tipo
		   else RESTO_WHILE.tipo = error_tipo 
		}

	 * @throws Exception 
	 */
	private ExpresionTipo resto_while() throws Exception {
		ExpresionTipo EXPRESSION_tipo;
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(94);
			nextToken();
			 EXPRESSION_tipo = expression();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
				if(EXPRESSION_tipo.getTipoBasico() == TipoBasico.logico) {
					estamosEnBucle = true;
					ExpresionTipo aux = cuerpo2();
					estamosEnBucle = false;
					return aux;
				} else 
					return ExpresionTipo.getError();
			} else{
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \")\"");
			}
		} else{
			gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \"(\"");
		}
		return null;
	}		

	/**
	 * 95. RESTO_DO --> CUERPO2 while (EXPRESSION);
	 * {
	 *  if(EXPRESSION.tipo == logico) then 
       		RESTO_DO.tipo = CUERPO2.tipo
		 else RESTO_DO.tipo = error_tipo 
  	   }
	 */
	private ExpresionTipo resto_do() throws Exception {
		ExpresionTipo EXPRESSION_tipo;
		ExpresionTipo CUERPO2_tipo;
		parse.add(95);

		estamosEnBucle = true;
		CUERPO2_tipo = cuerpo2();
		estamosEnBucle = false;
		
		if(token.esIgual(TipoToken.PAL_RESERVADA,72 /*while*/))	{
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
				nextToken();
				EXPRESSION_tipo = expression();
				
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
					nextToken();
					if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
						nextToken();
						if(EXPRESSION_tipo.getTipoBasico() == TipoBasico.logico)
							return CUERPO2_tipo;
						else
							return ExpresionTipo.getError();
					}	
					else{
						gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
					}
				}
				else{
					gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \")\"");
				}
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
			}
		}
		else{
			gestorErr.insertaErrorSintactico(linea, columna,"Falta palabra \"while\"");
		}
		return null;
	}	
		
	/**
	 * 96. RESTO_FOR → ( FOR-INIT ; EXPRESSIONOPT ; EXPRESSIONOPT ) CUERPO2 
	 * { 
	 *  if(FOR_INIT.tipo != error_tipo) and (EXPRESSIONOPT.tipo == logico) and (EXPRESSIONOPT1.tipo != error_tipo)  then 
       		RESTO_FOR.tipo = CUERPO2.tipo
		 else RESTO_FOR.tipo = error_tipo
		}

	 * @throws Exception 
	 */
	private ExpresionTipo resto_for() throws Exception {
		ExpresionTipo FOR_INIT_tipo;
		ExpresionTipo EXPRESSIONOPT_tipo;
		ExpresionTipo EXPRESSIONOPT1_tipo;
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(96);
			nextToken();
			 FOR_INIT_tipo = for_init();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				nextToken();
				EXPRESSIONOPT_tipo = expressionOpt();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
					nextToken();
					EXPRESSIONOPT1_tipo = expressionOpt();
					if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
						nextToken();
						if(EXPRESSIONOPT_tipo.getTipoBasico() == TipoBasico.logico && EXPRESSIONOPT1_tipo.getTipoBasico() != TipoBasico.error_tipo 
							&& FOR_INIT_tipo.getTipoBasico() != TipoBasico.error_tipo) {
							estamosEnBucle = true;
							ExpresionTipo aux = cuerpo2();
							estamosEnBucle = false;
							return aux;
						} 
						else
							return ExpresionTipo.getError();
					} else{
						gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \")\"");
					}
				} else{
					gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
				}
			} else{
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
			}
		} else{
			gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \"(\"");
		}
		return null;
	}

	/**
	 * 135. FOR-INIT → id INICIALIZACION
	 * 			{ if(id no declarado) FOR-INIT.tipo := error_tipo
	 * 			  else FOR-INIT.tipo := INICIALIZACION.tipo
	 * 			}
	 * 136. FOR-INIT → TIPO id INICIALIZACION
	 * 			{ if(id ya declarado) FOR-INIT.tipo := error_tipo
	 * 			  else FOR-INIT.tipo := INICIALIZACION.tipo
	 * 			}
	 * 137. FOR-INIT → EXPRESSIONOPT
	 * 			{ FOR-INIT.tipo := EXPRESSION_OPT.tipo }
	 * @throws Exception 
	 */
	private ExpresionTipo for_init() throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getVacio();
		if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(135);
			nextToken();
			EntradaTS entrada = (EntradaTS) token.getAtributo();
			if(entrada == null) {
				gestorErr.insertaErrorSemantico(linea, columna, "La variable no está declarada");
				inicializacion(null); // la llamamos para que no de error sintáctico
				tipo = ExpresionTipo.getError();
			} else {
				tipo = inicializacion(entrada.getTipo());
			}
		} else {
			ExpresionTipo aux = tipo();
			if(aux != null) {
				parse.add(136);
				lexico.setModoDeclaracion(true);
				if(token.esIgual(TipoToken.IDENTIFICADOR)){
					nextToken();
					EntradaTS entrada = (EntradaTS) token.getAtributo();
					if(entrada != null) {
						entrada.setTipo(aux);
						tipo = inicializacion(aux);
					} else {
						inicializacion(aux);
						gestorErr.insertaErrorSemantico(linea, columna, "La variable ya estába declarada");
						tipo = ExpresionTipo.getError();
					}
				}
				lexico.setModoDeclaracion(false);
			} else {
				parse.add(137);
				tipo = expressionOpt();
			}
		}
		return tipo;
	}

	/** 
	 * 97. RESTO_CASE --> ( EXPRESSION ) RESTO_CASE2
	 * { if(EXPRESSION != error_tipo)  //(consulta(id.lexema) != null) then    //el id tiene que estar declarado
		     RESTO_CASE.tipo = RESTO_CASE2.tipo
		else
		     RESTO_CASE.tipo = error_tipo 
		 }

	 * @throws Exception 
	 */
	private ExpresionTipo resto_case() throws Exception {
		ExpresionTipo EXPRESSION_tipo;
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(97);
			nextToken();
			 EXPRESSION_tipo = expression(); 
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
				if(EXPRESSION_tipo.getTipoBasico() != TipoBasico.error_tipo)
					return resto_case2(EXPRESSION_tipo);
				else
					return ExpresionTipo.getError();
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna, "Falta ')'");;
			}
		}
		else{
			gestorErr.insertaErrorSintactico(linea, columna, "Falta '('");
		}
	return null;
	}
	
	/** 
	 * 261. RESTO_CASE2 --> { CUERPO_CASE }
	 * 			{ RESTO_CASE2.tipo_s := CUERPO_CASE.tipo_s } 
	 * 262. RESTO_CASE2 --> ; 
	 * 			{ RESTO_CASE2.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_case2(ExpresionTipo EXPRESSION_tipo) throws Exception {
		numDefaults = 0;
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
			parse.add(261);
			return ExpresionTipo.getVacio();
		}
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			parse.add(262);
			nextToken();
			estamosEnSwitch = true;
			ExpresionTipo aux1 = cuerpo_case(EXPRESSION_tipo);
			estamosEnSwitch = false;
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
				nextToken();
				return aux1;
			}
			else {
				gestorErr.insertaErrorSintactico(linea, columna, "Falta '}'");
				//ruptura=parse.size();
			}
		}
		else{
			gestorErr.insertaErrorSintactico(linea, columna, "Falta '{'");
			//ruptura=parse.size();
		}
	return null;	
	//return false;
	}

	/** TODO deberia funcionar con cuerpo si este no tuviese la regla cuerpo -> }
	 * 98. CUERPO_CASE --> case LITERAL : CUERPO CUERPO_CASE
	 * {
	 *  if(LITERAL_tipo es_equivalente CUERPO_CASE_tipo_h) and
	 *  (CUERPO.tipo != error_tipo) and  (CUERPO_CASE1.tipo != error_tipo) then 
       		CUERPO_CASE.tipo = vacio
		else CUERPO_CASE.tipo = error_tipo 
	   }
	 * 263. CUERPO_CASE --> default : CUERPO CUERPO_CASE'
	 * 				{ if(CUERPO.tipo_s!=error_tipo && CUERPO_CASE'.tipo_s!=error_tipo)
	 * 				  then CUERPO_CASE.tipo_s := vacio
	 * 				  else CUERPO_CASE.tipo_s := error_tipo } 
	 * @throws Exception 
	 */
	private ExpresionTipo cuerpo_case(ExpresionTipo EXPRESSION_tipo) throws Exception {
		ExpresionTipo CUERPO_tipo;
		ExpresionTipo CUERPO_CASE1_tipo;
		ExpresionTipo LITERAL_tipo;
		if(token.esIgual(TipoToken.PAL_RESERVADA,6 /*case*/)) {
			parse.add(98);
			nextToken();
			LITERAL_tipo = literal();
			if(LITERAL_tipo != null)
			{
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.DOS_PUNTOS)){
					nextToken();
					if(ExpresionTipo.sonEquivLog(EXPRESSION_tipo, LITERAL_tipo, OpLogico.AND) == null){	
						gestorErr.insertaErrorSemantico(linea, columna, "No coincide el identificador del case...");
						return new ExpresionTipo(TipoBasico.error_tipo);
					}
					CUERPO_tipo = cuerpo();
					CUERPO_CASE1_tipo = cuerpo_case(EXPRESSION_tipo);
					if(CUERPO_tipo.getTipoBasico() != TipoBasico.error_tipo &&
						CUERPO_CASE1_tipo.getTipoBasico() != TipoBasico.error_tipo)
						return ExpresionTipo.getVacio();
					else 
						return ExpresionTipo.getError();
					
				} else {
					gestorErr.insertaErrorSintactico(linea, columna, "Falta ':'");
				}
			}
			else{ 
				gestorErr.insertaErrorSintactico(linea, columna, "Falta literal");
			}
		} else if(token.esIgual(TipoToken.PAL_RESERVADA,17 /*default*/)){
			if(numDefaults < 1) {
				parse.add(263);
				numDefaults++;
				nextToken();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.DOS_PUNTOS)){
					nextToken();
					ExpresionTipo aux1 = cuerpo();
					ExpresionTipo aux2 = cuerpo_case(EXPRESSION_tipo);
					if(aux1.getTipoBasico()!=TipoBasico.error_tipo && aux2.getTipoBasico()!=TipoBasico.error_tipo)
						return ExpresionTipo.getVacio();
					else 
						return ExpresionTipo.getError();
				} else {
					gestorErr.insertaErrorSintactico(linea, columna, "Falta ':'");
				}
			} else {
				gestorErr.insertaErrorSintactico(linea, columna, "'default' aparece más de una vez.");
			}
		}
		return null;
	}
	
	
	
	/**
	 *  99. RESTO_IF --> ( EXPRESSION ) CUERPO2 SENT_ELSE
	 *  {
	 *   if(EXPRESSION.tipo == logico) and (CUERPO2.tipo != error_tipo) and  (SENT_ELSE.tipo != error_tipo) then 
        	RESTO_IF.tipo = vacio
		  else RESTO_IF.tipo = error_tipo
		 }

	 * @throws Exception 
	 */
	private ExpresionTipo resto_if() throws Exception {
		ExpresionTipo EXPRESSION_tipo;
		ExpresionTipo SENT_ELSE_tipo;
		ExpresionTipo CUERPO2_tipo;
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(99);
			nextToken();
			EXPRESSION_tipo = expression();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
				CUERPO2_tipo = cuerpo2();
				SENT_ELSE_tipo = sent_else();
				if(CUERPO2_tipo.getTipoBasico() != TipoBasico.error_tipo &&
					SENT_ELSE_tipo.getTipoBasico() != TipoBasico.error_tipo &&
					EXPRESSION_tipo.getTipoBasico() == TipoBasico.logico)
						return ExpresionTipo.getVacio();
				else
					return ExpresionTipo.getError();
				//return true;
			} else{
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \")\"");
				//ruptura=parse.size();
			}
		} else{
			gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
			//ruptura=parse.size();
		}
		//return false;
		return null;
	}
	
	
	/**
	 * 100. SENT_ELSE --> else CUERPO2 
	 * { SENT_ELSE.tipo =  CUERPO2.tipo }
	 * 101. SENT_ELSE --> lambda
	 * { SENT_ELSE.tipo = vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo sent_else() throws Exception {
		if(token.esIgual(TipoToken.PAL_RESERVADA,22 /*else*/)) {
			parse.add(100);
			nextToken();
			return cuerpo2();
		}
		else {//SENT_ELSE -> lambda
			parse.add(101);
			return ExpresionTipo.getVacio();
		}	
	}
	
	/**
	 * 144. PRIMARY-EXPRESSION → LITERAL
	 * 					{ PRIMARY-EXPRESSION.tipo := LITERAL.tipo }
	 * 145. PRIMARY-EXPRESSION → this
	 * 					{ PRIMARY-EXPRESSION.tipo := objeto(nombreClase) }
	 * 146. PRIMARY-EXPRESSION → UNQUALIFIED-ID
	 * 					{ PRIMARY-EXPRESSION.tipo := UNQUALIFIED-ID.tipo }
	 * 147. PRIMARY-EXPRESSION → ( EXPRESSION )
	 * 					{ PRIMARY-EXPRESSION.tipo := EXPRESSION.tipo }
	 * @return PRIMARY-EXPRESSION.tipo o null si no es una primary expression
	 * @throws Exception 
	 */
	private ExpresionTipo primary_expression() throws Exception {
		ExpresionTipo tipo = null;
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(147);
			nextToken();
			tipo = expression();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
				nextToken();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \")\"");
			}
		} else if (token.esIgual(TipoToken.PAL_RESERVADA, 57 /* this */)) {
			parse.add(145);
			nextToken();
			tipo = new Objeto(nombreClase);
		} else {
			ExpresionTipo aux = literal();
			if(aux != null) {
				parse.add(144);
				tipo = aux;
			} else {
				parse.add(146);
				tipo = unqualified_id();
			}
		}
		return tipo;
	}
	
	
	/**
	 * 148. UNQUALIFIED-ID → id
	 * 				{ UNQUALIFIED-ID.tipo := objeto(id.lexema) }
	 * 149. UNQUALIFIED-ID →  ~ RESTO_UNQ
	 * 				{ UNQUALIFIED-ID.tipo := RESTO_UNQ.tipo }
	 * @return 
	 * @throws Exception 
	 */
	private ExpresionTipo unqualified_id() throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getVacio();
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(148);
			tipo = new Objeto(((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
		} else if (token.esIgual(TipoToken.OP_LOGICO,OpLogico.SOBRERO)) {
			parse.add(149);
			nextToken();
			tipo = resto_unq();
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Expresion incompleta.");
		}
		return tipo;
	}
	
	
	/**
	 * 150. RESTO_UNQ → id
	 * 				{ RESTO_UNQ.tipo := vacio }
	 * 151. RESTO_UNQ → decltype ( EXPRESSION )
	 * 				{ RESTO_UNQ.tipo := EXPRESSION.tipo }
	 * @return RESTO_UNQ.tipo
	 * @throws Exception 
	 */
	private ExpresionTipo resto_unq() throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getVacio();
		
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(150);
//			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
		} else if (token.esIgual(TipoToken.PAL_RESERVADA, 16)) {
			parse.add(151);
			nextToken();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
				nextToken();
				tipo = expression();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
					nextToken();
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \")\"");
				}
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
			}
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta identificador o decltype");
		}
		
		return tipo;
	}
	
	/**
	 * 152. POSTFIX-EXPRESSION → typeid ( EXPRESSION ) RESTO_POSTFIX_EXP
	 * 					{ if(EXPRESSION.tipo = error_tipo || RESTO_POSTFIX_EXP.tipo = error_tipo)
	 * 					      RESTO_CAST.tipo := error_tipo
	 * 					  else
	 * 					      RESTO_CAST.tipo := vacio
	 * 					}
	 * 153. POSTFIX-EXPRESSION → ID RESTO_PE RESTO_POSTFIX_EXP
	 * 					{ if(RESTO_PE.tipo = error_tipo || RESTO_POSTFIX_EXP.tipo = error_tipo)
	 * 					      POSTFIX-EXPRESSION.tipo := error_tipo
	 * 					  else if(RESTO_PE.tipo = producto && ID.tipo != funcion)
	 * 					      POSTFIX-EXPRESSION.tipo := error_tipo
	 * 					  else
	 * 					      RESTO_POSTFIX_EXP.tipo_h := ID.tipo
	 * 					      POSTFIX-EXPRESSION.tipo := RESTO_POSTFIX_EXP.tipo
	 * 					}
	 * 154. POSTFIX-EXPRESSION → PRIMARY-EXPRESSION RESTO_POSTFIX_EXP
	 * 					{ if(PRIMARY-EXPRESSION.tipo = error_tipo)
	 * 					      RESTO_CAST.tipo := error_tipo
	 * 					  else
	 * 					      RESTO_CAST.tipo := RESTO_POSTFIX_EXP.tipo
	 * 					}
	 * 168. POSTFIX-EXPRESSION → TIPO_SIMPLE POSTFIX-2 RESTO_POSTFIX_EXP
	 * 					{ if (TIPO_SIMPLE.tipo_s!=error_tipo) then
	 * 						POSTFIX-2.tipo_h := TIPO_SIMPLE.tipo_s
	 * 						RESTO_POSTFIX_EXP.tipo_h := TIPO_SIMPLE.tipo_s
	 * 						POSTFIX-EXPRESSION.tipo_s := TIPO_SIMPLE.tipo_s
	 * 					else
	 * 						POSTFIX-EXPRESSION.tipo_s := error_tipo } 
	 * 170. POSTFIX-EXPRESSION →  ~ POSTFIX-EXPRESSION1
	 * 					{ POSTFIX-EXPRESSION.tipo_s := POSTFIX-EXPRESSION1.tipo_s}
	 * @return 
	 * @throws Exception 
	 */
	private ExpresionTipo postfix_expression() throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getVacio();
		
		if (token.esIgual(TipoToken.PAL_RESERVADA, 63)) {
			parse.add(152);
			nextToken();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
				nextToken();
				ExpresionTipo aux1 = expression();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
					nextToken();
					ExpresionTipo aux2 = resto_postfix_exp(aux1); // TODO creo que deberia pasar el tipo que devuelve typeid(expresion)...
					if(aux1.equals(TipoBasico.error_tipo) || aux2.equals(TipoBasico.error_tipo)){
						tipo = ExpresionTipo.getError();
					} else {
						tipo = aux2;
					}
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \")\"");
				}
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
			}
		} else if (token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(153);
			nextToken();
			
			ExpresionTipo params = resto_pe();
			ExpresionTipo tipoSem = ((EntradaTS)token.getAtributo()).getTipo();
			if(params.equals(TipoNoBasico.producto)) { // se trata de una función
				if(!tipoSem.equals(TipoNoBasico.funcion)) {
					gestorErr.insertaErrorSemantico(linea, columna, "No es una función o la función no está declarada");
					tipo = ExpresionTipo.getError();
				} else	if(!params.paramsEquivalentes(((Funcion)tipoSem).getDominio())) {
					gestorErr.insertaErrorSemantico(linea, columna, "Parametros de la funcion incorrectos");
					tipo = ExpresionTipo.getError();
				}
			}
			ExpresionTipo aux = resto_postfix_exp(tipoSem);
			
			if(tipo.equals(TipoBasico.error_tipo) || params.equals(TipoBasico.error_tipo) || aux.equals(TipoBasico.error_tipo))
				tipo = ExpresionTipo.getError();
			else
				tipo = aux;
		} else if  (token.esIgual(TipoToken.OP_LOGICO,OpLogico.SOBRERO)) {
			parse.add(170);
			nextToken();
			tipo = postfix_expression();
		} else if (!(tipo=tipo_simple()).equals(TipoBasico.vacio)){ 
			parse.add(168);
			postfix2(tipo);
			resto_postfix_exp(tipo);
		} else {
			parse.add(154);
			ExpresionTipo aux = primary_expression();
//			if(tokenAnterior.esIgual(TipoToken.IDENTIFICADOR)) //TODO (Alina) no se por que se comprueba esto, asi, haria falta una regla que deriva en lambda
				tipo = resto_postfix_exp(aux);
			if(aux.equals(TipoBasico.error_tipo))
				tipo = aux;
		}
		
		return tipo;
	}
	
	/**
	 * 164. RESTO_PE → ( POSTFIX-3
	 * 165. RESTO_PE → lambda
	 * @throws Exception 
	 */
	private ExpresionTipo resto_pe() throws Exception {
		ExpresionTipo aux = null;
		
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(164);
			nextToken();
			aux = postfix3();
		} else {
			parse.add(165);
			aux = ExpresionTipo.getVacio();
		}
		
		return aux;
	}
	
	/**
	 * 155. RESTO_POSTFIX_EXP → [ EXPRESSION ]
	 * 					{ if(RESTO_POSTFIX_EXP.tipo_h != vector && RESTO_POSTFIX_EXP.tipo_h != cadena)
	 * 					      RESTO_POSTFIX_EXP.tipo := error_tipo
	 * 					  else if( EXPRESSION.tipo != entero)
	 * 					      RESTO_POSTFIX_EXP.tipo := error_tipo
	 * 					  else
	 * 					      RESTO_POSTFIX_EXP.tipo := vacio
	 * 					}
	 * 156. RESTO_POSTFIX_EXP → -> RESTO_POSTFIX_EXP3
	 * 					{ if(RESTO_POSTFIX_EXP.tipo_h != objeto)
	 * 					      RESTO_POSTFIX_EXP.tipo := error_tipo
	 * 					  else
	 * 					      RESTO_POSTFIX_EXP.tipo := vacio
	 * 					}
	 * 157. RESTO_POSTFIX_EXP → . RESTO_POSTFIX_EXP3
	 * 					{ if(RESTO_POSTFIX_EXP.tipo_h != objeto)
	 * 					      RESTO_POSTFIX_EXP.tipo := error_tipo
	 * 					  else
	 * 					      RESTO_POSTFIX_EXP.tipo := vacio
	 * 					}
	 * 158. RESTO_POSTFIX_EXP → decremento
	 * 					{ if(RESTO_POSTFIX_EXP.tipo_h != entero && RESTO_POSTFIX_EXP.tipo_h != real)
	 * 					      RESTO_POSTFIX_EXP.tipo := error_tipo
	 * 					  else
	 * 					      RESTO_POSTFIX_EXP.tipo := RESTO_POSTFIX_EXP.tipo_h
	 * 					}
	 * 159. RESTO_POSTFIX_EXP → incremento
	 * 					{ if(RESTO_POSTFIX_EXP.tipo_h != entero && RESTO_POSTFIX_EXP.tipo_h != real)
	 * 					      RESTO_POSTFIX_EXP.tipo := error_tipo
	 * 					  else
	 * 					      RESTO_POSTFIX_EXP.tipo := RESTO_POSTFIX_EXP.tipo_h
	 * 					}
	 * 160. RESTO_POSTFIX_EXP → ( RESTO_POSTFIX_EXP2
	 * 					{ RESTO_POSTFIX_EXP.tipo_s := RESTO_POSTFIX_EXP2.tipo_s }
	 * 161. RESTO_POSTFIX_EXP → lambda
	 * 					{ RESTO_POSTFIX_EXP.tipo_s := vacio }
	 * @param exp 
	 * @return RESTO_POSTFIX_EXP.tipo
	 * @throws Exception 
	 */
	private ExpresionTipo resto_postfix_exp(ExpresionTipo exp) throws Exception {
		ExpresionTipo tipo = ExpresionTipo.getVacio();
		
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_CORCHETE)) {
			parse.add(155);
			nextToken();
			ExpresionTipo aux = expression();
			if(!exp.equals(TipoNoBasico.vector) || !exp.equals(TipoNoBasico.cadena)) {
				tipo = ExpresionTipo.getError();
				gestorErr.insertaErrorSemantico(linea, columna, "Aqui no vale lo de los [ ]"); //TODO cambiar el mensaje
			}
			if(!aux.equals(TipoBasico.entero)) {
				tipo = ExpresionTipo.getError();
				gestorErr.insertaErrorSemantico(linea, columna, "El indice tiene que ser un entero");
			}
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_CORCHETE)) {
				nextToken();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"]\"");
			}
		} else if (token.esIgual(TipoToken.OP_ASIGNACION, OpAsignacion.PUNTERO)) {
			parse.add(156);
			nextToken();
			tipo = resto_postfix_exp3();
			if(!exp.equals(TipoNoBasico.objeto)) {
				tipo = ExpresionTipo.getError();
				gestorErr.insertaErrorSemantico(linea, columna, "Aqui no vale el '->' porque no se aplica sobre un objeto"); //TODO cambiar el mensaje
			}
		} else if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO)) {
			parse.add(157);
			nextToken();
			tipo = resto_postfix_exp3();
			if(!exp.equals(TipoNoBasico.objeto)) {
				tipo = ExpresionTipo.getError();
				gestorErr.insertaErrorSemantico(linea, columna, "Aqui no vale el '.'  porque no se aplica sobre un objeto"); //TODO cambiar el mensaje
			}
		} else if (token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.DECREMENTO)) {
			if(tokenAnterior.esIgual(TipoToken.IDENTIFICADOR)){
				if(exp.getTipoBasico() != TipoBasico.entero && exp.getTipoBasico() != TipoBasico.real) {
					tipo = ExpresionTipo.getError();
					gestorErr.insertaErrorSemantico(linea, columna, "Valor invalido para decremento.");
				} else {
					tipo = new ExpresionTipo (exp.getTipoBasico());
				}
			} else {
				tipo = ExpresionTipo.getError();
				gestorErr.insertaErrorSemantico(linea, columna, "Valor invalido para decremento.");
			}
			parse.add(158);
			nextToken();
		} else if (token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.INCREMENTO)) {
			if(tokenAnterior.esIgual(TipoToken.IDENTIFICADOR)){
				if(exp.getTipoBasico() != TipoBasico.entero && exp.getTipoBasico() != TipoBasico.real) {
					tipo = ExpresionTipo.getError();
					gestorErr.insertaErrorSemantico(linea, columna, "Valor invalido para incremento.");
				} else {
					tipo = new ExpresionTipo (exp.getTipoBasico());
				}
			} else {
				tipo = ExpresionTipo.getError();
				gestorErr.insertaErrorSemantico(linea, columna, "Valor invalido para incremento.");
			}
			parse.add(159);
			nextToken();
		} else if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(160);
			nextToken(); 
			tipo = resto_postfix_exp2();
		} else {
			parse.add(161);
			tipo = ExpresionTipo.getVacio();
		}
		
		return tipo;
	}
	
	
	/**
	 * 162. RESTO_POSTFIX_EXP2 → )
	 * 							{ RESTO_POSTFIX_EXP2.tipo_s := vacio }
	 * 163. RESTO_POSTFIX_EXP2 → INITIALIZER-LIST )
	 * 							{ RESTO_POSTFIX_EXP2.tipo_s := INITIALIZER-LIST.tipo_s }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_postfix_exp2() throws Exception {
		ExpresionTipo aux = null;
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
			parse.add(162);
			nextToken();
			aux = ExpresionTipo.getVacio();
		} else {
			parse.add(163);
			aux = initializer_list();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
				nextToken();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \")\"");
				//ruptura=parse.size();
			}
		}
		return aux;
	}
	
	
	/**
	 * 166. RESTO_POSTFIX_EXP3 → ~ decltype ( EXPRESSION )
	 * 							{ RESTO_POSTFIX_EXP3.tipo_s := EXPRESSION.tipo_s }
	 * 167. RESTO_POSTFIX_EXP3 → UNQUALIFIED-ID
	 * 							{ RESTO_POSTFIX_EXP3.tipo_s := UNQUALIFIED-ID.tipo_s }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_postfix_exp3() throws Exception {
		ExpresionTipo aux = null;
		if (token.esIgual(TipoToken.OP_LOGICO,OpLogico.SOBRERO)) {	
			parse.add(166);
			nextToken();
			if (token.esIgual(TipoToken.PAL_RESERVADA, 16)) {
				nextToken();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
					nextToken();
					aux =  expression();
					if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
						nextToken();
					} else {
						gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \")\"");
						//ruptura=parse.size();
					}
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
					//ruptura=parse.size();
				}
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta la palabra \"decltype\"");
				//ruptura=parse.size();
			}
		} else {
			unqualified_id();
			aux = ExpresionTipo.getVacio();
			//aux = unqualified_id(); TODO cambiar por este
		}
		return aux;
	}
	
	
	/**
	 * 171. POSTFIX4 → (
	 * 					{POSTFIX4.tipo_s := vacio}
	 * 172. POSTFIX4 → lambda
	 * 					{POSTFIX4.tipo_s := vacio}
	 * @throws Exception 
	 */
	private ExpresionTipo postfix4() throws Exception {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(171);
			nextToken();
			return ExpresionTipo.getVacio();
		} else {
			parse.add(172);
			return ExpresionTipo.getVacio();
		}
	}
	
	
	/**
	 * 173. POSTFIX-2 → ( POSTFIX-3
	 * 					{ POSTFIX-2.tipo_s := POSTFIX-3.tipo_s }
	 * @throws Exception 0 
	 */
	private ExpresionTipo postfix2(ExpresionTipo tipo_h) throws Exception {
		ExpresionTipo aux = null;
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(173);
			nextToken();
			aux = postfix3();
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
			//ruptura=parse.size();
		}
		return aux;
	}
	
	
	/**
	 * 174. POSTFIX-3 →  )
	 * 					{ POSTFIX-3.tipo_s := vacio }
	 * 175. POSTFIX-3 → INITIALIZER-LIST )
	 * 					{ POSTFIX-3.tipo_s := INITIALIZER-LIST.tipo_s }
	 * @return 
	 * @throws Exception 
	 */
	private ExpresionTipo postfix3() throws Exception {
		ExpresionTipo aux = null;
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
			parse.add(174);
			nextToken();
			aux = ExpresionTipo.getVacio();
		} else {
			parse.add(175);
			aux = initializer_list();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
				nextToken();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \")\"");
				//ruptura=parse.size();
			}
		}
		return aux;
	}
	
	
		
	/**
	 * 176. INITIALIZER-LIST → ASSIGNMENT-EXPRESSION RESTO_INIT 
	 * 	{ if (ASSIGNMENT-EXPRESSION.tipo_s != error_tipo) then
     *  		RESTO-INIT.tipo_h := ASSIGNMENT-EXPRESSION.tipo_s;
     *           if (RESTO-INIT.tipo_s == vacio) then
     *                INITIALIZER-LIST.tipo_s := ASSIGNMENT-EXPRESSION.tipo_s
     *            else 
     *            	INITIALIZER-LIST.tipo_s := RESTO-INIT.tipo_s
     *	  else    
     *	  	INITIALIZER-LIST.tipo_s := error_tipo }
	 * @throws Exception 
	 */
	private ExpresionTipo initializer_list() throws Exception{
		ExpresionTipo aux1,aux2,aux3 = null;
		parse.add(176);
		aux1 = assignment_expression();
		if ( !aux1.getTipoBasico().equals("error_tipo")) {
			aux3 = aux1;
			aux2 = resto_init(aux3);
			if (aux2.getTipoBasico().equals("vacio")) {
				return new ExpresionTipo(aux1.getTipoBasico());
			} else {
				return new ExpresionTipo(aux2.getTipoBasico());
			}
		} else {
			gestorErr.insertaErrorSemantico(linea, columna, "LALALALALALAALALA"); // TODO cambiar el mensaje
			return ExpresionTipo.getError();
		}
	}
	
	/**
	 * 177. RESTO_INIT → , INITIALIZER_LIST
	 * 					{ RESTO_INIT.tipo_s := INITIALIZER_LIST.tipo_s }
	 * 178. RESTO_INIT → lambda
	 * 					{ RESTO_INIT.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_init(ExpresionTipo tipo_h) throws Exception{
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)){
			parse.add(177);
			nextToken();
			return initializer_list();
		}
		else {
			parse.add(178);
			return ExpresionTipo.getVacio();
		}
	}
			
	/**
	 * 179. UNARY_EXPRESSION → incremento CAST_EXPRESSION
	 * 							{ UNARY-EXPRESSION.tipo_s := CAST-EXPRESSION.tipo_s }
	 * 180. UNARY_EXPRESSION → decremento CAST_EXPRESSION
	 * 							{ UNARY-EXPRESSION.tipo_s := CAST-EXPRESSION.tipo_s }
	 * 181. UNARY_EXPRESSION → UNARY-OPERATOR CAST_EXPRESSION
	 * 							{ if (UNARY-OPERATOR.tipo_s != null) then 
	 * 								UNARY-EXPRESSION.tipo_s := CAST-EXPRESSION.tipo_s;
	 * 							else
	 * 							    UNARY-EXPRESSION.tipo_s := error_tipo }
	 * 182. UNARY_EXPRESSION → sizeof RESTO_UNARY
	 * 							{ UNARY-EXPRESSION.tipo_s := RESTO-UNARY.tipo_s }
	 * 183. UNARY_EXPRESSION → alignof (type-id)
	 * 							{ UNARY-EXPRESSION.tipo_s := TIPO.tipo_s }
	 * 184. UNARY_EXPRESSION → noexcept NOEXCEPT_EXPRESSION
	 * 							{ UNARY-EXPRESSION.tipo_s := NOEXCEPT-EXPRESSION.tipo_s }
	 * 142. UNARY_EXPRESSION → new TIPO ( RESTO_NEW
	 * 143. UNARY_EXPRESSION → delete RESTO_DELETE
	 * 185. UNARY_EXPRESSION → POSTFIX_EXPRESSION
	 * 							{ UNARY-EXPRESSION.tipo_s := POSTFIX-EXPRESSION.tipo_s }
	 * @throws Exception 
	 */
	private ExpresionTipo unary_expression() throws Exception{
		ExpresionTipo aux = null;
		if(token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.INCREMENTO)){
			parse.add(179);
			nextToken();
			aux = cast_expression();
		}
		else if(token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.DECREMENTO)){
			parse.add(180);
			nextToken();
			aux = cast_expression();
		}
		else if(unary_operator() != null){
			parse.add(181);
			aux = cast_expression();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA)
				&& (Integer)token.getAtributo()==50 /*sizeof*/ ){
			parse.add(182);
			nextToken();
			aux = resto_unary();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA)
				&& (Integer)token.getAtributo()==1 /*alignof*/ ){
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) {
				nextToken();
				if(tipo().getTipoBasico()!=TipoBasico.vacio){
					if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
						parse.add(183);
						nextToken();
						aux = tipo();
					}else{
						gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba `)` ");
					}	
				}else{
					gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba un identificador o tipo pre-definido ");
				}
			}else{
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba `(` ");
			}
		} else if (token.esIgual(TipoToken.PAL_RESERVADA)
				&& (Integer)token.getAtributo()==39 /*noexcept*/ ){
			parse.add(184);
			nextToken();
			aux = noexcept_expression();
		} else if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo()==38 /*new*/ ){
			parse.add(142);
			nextToken();
			if(tipo().getTipoBasico()!=TipoBasico.vacio){
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) {
					nextToken();
					resto_new();
				}else{
					gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba `)` ");
				}
			}else{
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba un identificador o tipo pre-definido ");
			}
		}else if(token.esIgual(TipoToken.PAL_RESERVADA)	&& (Integer)token.getAtributo()==18 /*delete*/ ){
			parse.add(143);
			nextToken();
			resto_delete();
		}else{
			parse.add(185);
			aux = postfix_expression();
		}
		return aux;
	}
	
	/**
	 * 186. RESTO_UNARY → ( TIPO )
	 * 						{ RESTO_UNARY.tipo_s  := TIPO.tipo_s }
	 * 187. RESTO_UNARY → UNARY-EXPRESSION
	 * 						{ RESTO_UNARY.tipo_s := UNARY-EXPRESSION.tipo_s }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_unary() throws Exception{
		ExpresionTipo aux = null;
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)){
			nextToken();
			//if(tipo()){
			if(tipo().getTipoBasico()!=TipoBasico.vacio){
				if(token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)){
					parse.add(186);
					nextToken();
					aux = tipo();
				}else{
					// error
					gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba `)` ");
					//ruptura=parse.size();
				}
			}else{
				// error
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba un identificador o tipo pre-definido ");
				//ruptura=parse.size();
			}
		}else{
			aux = unary_expression();
		}
		return aux;
	}
	
	/**
	 * 188. UNARY-OPERATOR → *
	 * 						{ UNARY-OPERATOR.tipo_s := vacio }
	 * 189. UNARY-OPERATOR → &
	 * 						{ UNARY-OPERATOR.tipo_s := vacio }
	 * 190. UNARY-OPERATOR → +
	 * 						{ UNARY-OPERATOR.tipo_s := vacio }
	 * 191. UNARY-OPERATOR → !
	 * 						{ UNARY-OPERATOR.tipo_s := vacio }
	 * 192. UNARY-OPERATOR → sombrero
	 * 						{ UNARY-OPERATOR.tipo_s := vacio }
	 * 138. UNARY-OPERATOR → -
	 * 						{ UNARY-OPERATOR.tipo_s := vacio }
	 * ahora en la regla 181 pregunto if(unary_operator() != null) en lugar de preguntar por el valor booleano
	 * @throws Exception 
	**/
	private ExpresionTipo unary_operator() throws Exception{
		ExpresionTipo aux = null;
		if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.MULTIPLICACION)){
			parse.add(188);
			nextToken();
			aux = ExpresionTipo.getVacio();
		}else if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.BIT_AND)){
			parse.add(189);
			nextToken();
			aux = ExpresionTipo.getVacio();
		}else if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.SUMA)){
			parse.add(190);
			nextToken();
			aux = ExpresionTipo.getVacio();
		}else if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.NOT)){
			parse.add(191);
			nextToken();
			aux = ExpresionTipo.getVacio();
		}else if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.SOBRERO)){
			parse.add(192);
			nextToken();
			aux = ExpresionTipo.getVacio();
		}else if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.RESTA)){
			parse.add(138);
			nextToken();
			aux = ExpresionTipo.getVacio();
		}
		return aux;
	}

	/**
	 * 251.RESTO_NEW → )
	 * 				{ RESTO_NEW.tipo_s := vacio }
	 * 252.RESTO_NEW → EXPRESSION_LIST )
	 * 				{ RESTO_NEW.tipo_s := EXPRESSION_LIST.tipo_s } 
	 * @throws Exception 
	 */
	private ExpresionTipo resto_new() throws Exception{
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
			parse.add(251);
			nextToken();
			return ExpresionTipo.getVacio();
		}else{
			parse.add(252);
			ExpresionTipo aux1 = expression_list();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
			}else{
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba ')' ");
			}
			return aux1;
		}
		
	}
	
	/**
	 * 253. EXPRESSION_LIST → EXPRESSION RESTO_LISTA_EXP
	 * 						{ if(EXPRESSION.tipo_s != error_tipo)
	 * 						  then EXPRESSION_LIST.tipo_s := RESTO_LISTA_EXP.tipo_s
	 * 						  else EXPRESSION_LIST.tipo_s := error_tipo }
	 * @throws Exception 
	 */
	private ExpresionTipo expression_list() throws Exception{
		parse.add(253);
		ExpresionTipo aux1 = expression();
		ExpresionTipo aux2 = resto_lista_exp();
		if(aux1.getTipoBasico()!=TipoBasico.error_tipo && aux2.getTipoBasico()!=TipoBasico.error_tipo)
			return ExpresionTipo.getVacio();
		else
			return ExpresionTipo.getError();
	}
	
	/**
	 * 254. RESTO_LISTA_EXP → , EXPRESSION_LIST
	 * 						{ RESTO_LISTA_EXP.tipo_s := EXPRESSION_LIST.tipo_s }
	 * 255. RESTO_LISTA_EXP → lambda
	 * 						{ RESTO_LISTA_EXP.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_lista_exp() throws Exception{
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)){
			parse.add(254);
			nextToken();
			return expression_list();
		}else{
			parse.add(255);
			return ExpresionTipo.getVacio();
		}
	}
	
	/**
	 * 256. RESTO_DELETE → ID
	 * 257. RESTO_DELETE → [ ] ID 
	 * @throws Exception 
	 */
	private void resto_delete() throws Exception{
		if (token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(256);
			nextToken();
		}else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_CORCHETE)){
			parse.add(257);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_CORCHETE)){
				nextToken();
			}else{
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba ']' ");
			}			
		}else{
			gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba '[' ");
		}
	}
	
	
	/**
	 * 193. NOEXCEPT-EXPRESSION → ( EXPRESSION ) 
	 * 							{ NOEXCEPT-EXPRESSION.tipo_s := EXPRESSION.tipo_s }
	 * @throws Exception 
	 */
	private ExpresionTipo noexcept_expression() throws Exception{
		ExpresionTipo aux = null;
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			nextToken();
			aux = expression();
			if(token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)){
				parse.add(193);
				nextToken();
			}else{
				// error
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba `)` ");
				//ruptura=parse.size();
			}
		}else{
			// error
			gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba `(` ");
			//ruptura=parse.size();
		}
		return aux;
	}
	
	/**
	 * 194. CAST-EXPRESSION → UNARY-EXPRESSION
	 * 						{ CAST-EXPRESSION.tipo_s := UNARY-EXPRESSION.tipo_s }
	 * 195. CAST-EXPRESSION → ( RESTO_CAST
	 * 						{ CAST-EXPRESSION.tipo_s := RESTO_CAST.tipo_s }

	 * @return 
	 * @throws Exception 
	 */
	private ExpresionTipo cast_expression() throws Exception{
		ExpresionTipo aux = null;
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(195);
			nextToken();
			aux = resto_cast();
		} else {
			parse.add(194);
			aux = unary_expression();
		}
		return aux;
	}
	
	/**
	 * 139. RESTO_CAST → TIPO_SIMPLE ) CAST_EXPRESSION
	 * 				{ if(TIPO_SIMPLE.tipo = error_tipo || CAST_EXPRESSION.tipo = error_tipo)
	 * 				      RESTO_CAST.tipo := error_tipo
	 * 				  else
	 * 				      RESTO_CAST.tipo := vacio
	 * 				}
	 * 140. RESTO_CAST → EXPRESSION )
	 * 				{ RESTO_CAST.tipo := EXPRESSION.tipo }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_cast() throws Exception{
		ExpresionTipo tipo = tipo_simple();
		if(tipo != null){
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
				parse.add(139);
				ExpresionTipo aux = cast_expression(); //TODO a lo mejor deberia pasarle el tipo
				if(!tipo.equals(TipoBasico.error_tipo)){
					tipo = aux;
				}
			}else{ 
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba ')' ");
			}
		}else{
			parse.add(140);
			tipo = expression();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
			} else{
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba ')' ");
			}
		}
		return tipo;
	}
	
	
	/**
	 * 196. PM-EXPRESSION → CAST-EXPRESSION
	 * 						{ PM-EXPRESSION.tipo_s := CAST-EXPRESSION.tipo_s }
	 * 197. PM-EXPRESSION → . * CAST-EXPRESSION
	 * 198. PM-EXPRESSION → -> * CAST-EXPRESSION
	 * @throws Exception 
	 */
	private ExpresionTipo pm_expression() throws Exception{
		ExpresionTipo aux = null;
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO)){
			nextToken();
			if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.MULTIPLICACION)){
				nextToken();
				parse.add(197);
				//Esto es mas complicado
				aux = cast_expression();
			}else{
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba `*` ");
			}
		} else if(token.esIgual(TipoToken.OP_ASIGNACION,OpAsignacion.PUNTERO)){
			nextToken();
			if (token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.MULTIPLICACION)){
				parse.add(198);
				nextToken();
				//Esto es mas complicado
				aux = cast_expression();
			}else{
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba `*` ");
			}
		} else{
			parse.add(196);
			aux = cast_expression();
		}
		return aux;
	}
	
	
	/**
	 * 199. MULTIPLICATIVE-EXPRESSION → PM-EXPRESSION RESTO-MULT
	 *   { if (PM_EXPRESSION.tipo_s != error_tipo) then   
	 *   		RESTO-MULT.tipo_h := PM-EXPRESSION.tipo_s;
     *          if (RESTO-MULT.tipo_s == vacio) then 
     *          	MULTIPLICATIVE-EXPRESSION.tipo_s == PM-EXPRESSION.tipo_s
     *          else MULTIPLICATIVE-EXPRESSION.tipo_s := RESTO-MULT.tipo_s
     *     else
     *         MULTIPLICATIVE-EXPRESSION.tipo_s := error_tipo }
	 *
	 * @throws Exception 
	 */
	private ExpresionTipo multiplicative_expression() throws Exception{
		ExpresionTipo aux1,aux2,aux3=null;
		parse.add(199);
		aux1 = pm_expression();
		if ( !aux1.getTipoBasico().equals("error_tipo")) {
			aux3 = aux1;
			aux2 = resto_mult(aux3);
			if (aux2.getTipoBasico().equals("vacio")) {
				return new ExpresionTipo(aux1.getTipoBasico());
			} else {
				return new ExpresionTipo(aux2.getTipoBasico());
			}
		} else {
			gestorErr.insertaErrorSemantico(linea, columna, "ERROR DE TIPOS"); //TODO CAMBIAR MENSAJE
			return ExpresionTipo.getError();
		}
	}
	
	/**
	 * 200. RESTO-MULT → * MULTIPLICATIVE-EXPRESSION
	 *   { if (RESTO_MULT.tipo_h == PM-EXPRESSION.tipo_s) then
	 *   		if ((RESTO-MULT.tipo_h := entero) & (MULTIPLICATIVE-EXPRESSION.tipo_s := entero) then 
	 *   			RESTO-MULT.tipo_s := entero;
	 *   		else if ((RESTO-MULT.tipo_h := entero) & (MULTIPLICATIVE-EXPRESSION.tipo_s := real) then 
	 *   			RESTO-MULT.tipo_s := real;
	 *   		else if ((RESTO-MULT.tipo_h := real) & (MULTIPLICATIVE-EXPRESSION.tipo_s := entero) then 
	 *   			RESTO-MULT.tipo_s := real;
	 *   		else if ((RESTO-MULT.tipo_h := real) & (MULTIPLICATIVE-EXPRESSION.tipo_s := real) then 
	 *   			RESTO-MULT.tipo_s := real;
	 *     else    
	 *     		RESTO-MULT.tipo_s := error_tipo }
	 * 201. RESTO-MULT → / MULTIPLICATIVE-EXPRESSION
	 *   { if (RESTO_MULT.tipo_h == PM-EXPRESSION.tipo_s) then
	 *   		if ((RESTO-MULT.tipo_h := entero) & (MULTIPLICATIVE-EXPRESSION.tipo_s := entero) then 
	 *   			RESTO-MULT.tipo_s := entero;
	 *   		else if ((RESTO-MULT.tipo_h := entero) & (MULTIPLICATIVE-EXPRESSION.tipo_s := real) then 
	 *   			RESTO-MULT.tipo_s := real;
	 *   		else if ((RESTO-MULT.tipo_h := real) & (MULTIPLICATIVE-EXPRESSION.tipo_s := entero) then 
	 *   			RESTO-MULT.tipo_s := real;
	 *   		else if ((RESTO-MULT.tipo_h := real) & (MULTIPLICATIVE-EXPRESSION.tipo_s := real) then 
	 *   			RESTO-MULT.tipo_s := real;
	 *     else    
	 *     		RESTO-MULT.tipo_s := error_tipo }
	 * 202. RESTO-MULT → % MULTIPLICATIVE-EXPRESSION
	 *   { if (RESTO_MULT.tipo_h == PM-EXPRESSION.tipo_s) then
	 *   		if ((RESTO-MULT.tipo_h := entero) & (MULTIPLICATIVE-EXPRESSION.tipo_s := entero) then 
	 *   			RESTO-MULT.tipo_s := entero;
	 *   		else if ((RESTO-MULT.tipo_h := entero) & (MULTIPLICATIVE-EXPRESSION.tipo_s := real) then 
	 *   			RESTO-MULT.tipo_s := real;
	 *   		else if ((RESTO-MULT.tipo_h := real) & (MULTIPLICATIVE-EXPRESSION.tipo_s := entero) then 
	 *   			RESTO-MULT.tipo_s := real;
	 *   		else if ((RESTO-MULT.tipo_h := real) & (MULTIPLICATIVE-EXPRESSION.tipo_s := real) then 
	 *   			RESTO-MULT.tipo_s := real;
	 *     else    
	 *     		RESTO-MULT.tipo_s := error_tipo }
	 * 203. RESTO-MULT → lambda
	 * 	{RESTO-MULT.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_mult(ExpresionTipo tipo_h/*RESTO-MULT.tipo_h*/) throws Exception{
		ExpresionTipo aux1,aux2 = null; //MULTIPLICATIVE-EXPRESSION.tipo_s
		if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.MULTIPLICACION)){
			nextToken();
			parse.add(200);
			aux1 = multiplicative_expression();
			aux2 = ExpresionTipo.sonEquivArit(aux1,tipo_h,OpAritmetico.MULTIPLICACION);
		}else if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.DIVISION)){
			nextToken();
			parse.add(201);
			aux1 = multiplicative_expression();
			aux2 = ExpresionTipo.sonEquivArit(aux1,tipo_h,OpAritmetico.DIVISION);
		}else if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.PORCENTAJE)){
			nextToken();
			parse.add(202);
			aux1 = multiplicative_expression();
			aux2 = ExpresionTipo.sonEquivArit(aux1,tipo_h,OpAritmetico.PORCENTAJE);
		} else{
			parse.add(203);
			//lambda
			return ExpresionTipo.getVacio();
		}
		/*
		// { if (RESTO_MULT.tipo_h == PM-EXPRESSION.tipo_s) then
		if (tipo_h.getTipoBasico().equals("tengo que coger el valor de PM expression")) { //Supuestamente me viene en la misma variable ¿?¿?¿?¿?
			if (aux.getTipoBasico().equals("entero") && tipo_h.getTipoBasico().equals("entero")) {
				return new ExpresionTipo(TipoBasico.entero);
			} else if ((aux.getTipoBasico().equals("real") && tipo_h.getTipoBasico().equals("entero")) ||
						(aux.getTipoBasico().equals("entero") && tipo_h.getTipoBasico().equals("real")) ||
							((aux.getTipoBasico().equals("real") && tipo_h.getTipoBasico().equals("real")))) {
				return new ExpresionTipo(TipoBasico.real);
			} else {
				gestorErr.insertaErrorSemantico(linea, columna, "ERROR DE TIPOS"); //TODO CAMBIAR MENSAJE
				return ExpresionTipo.getError();
			}	
		}else {
			gestorErr.insertaErrorSemantico(linea, columna, "ERROR DE TIPOS"); //TODO CAMBIAR MENSAJE
			return ExpresionTipo.getError();
		}*/
		return aux2;
	}
	
	/**
	 * 204. ADDITIVE_EXPRESSION → MULTIPLICATIVE-EXPRESSION  RESTO _ADD
	 *   { if (MULTIPLICATIVE-EXPRESSION.tipo_s != error_tipo) then
	 *      RESTO_ADD.tipo_h := MULTIPLICATIVE-EXPRESSION.tipo_s;
	 *              if (RESTO_ADD.tipo_s == vacio) then 
	 *                  ADDITIVE_EXPRESSION.tipo_s == MULTIPLICATIVE-EXPRESSION.tipo_s
	 *              else ADDITIVE_EXPRESSION.tipo_s := RESTO_ADD.tipo_s
	 *     else    ADDITIVE_EXPRESSION.tipo_s := error_tipo }
	 * @throws Exception 
	 */
	private ExpresionTipo additive_expression() throws Exception{
		ExpresionTipo aux1,aux2,aux3=null;
		parse.add(204);
		aux1 = multiplicative_expression();
		if (!aux1.getTipoBasico().equals("error_tipo")) {
			aux3 = aux1;
			aux2 = resto_add(aux3);
			if (aux2.equals("vacio")) {
				return new ExpresionTipo(aux1.getTipoBasico());
			} else {
				return new ExpresionTipo(aux2.getTipoBasico());
			}
		} else {
			gestorErr.insertaErrorSemantico(linea, columna, "ERROR DE TIPOS"); //TODO CAMBIAR MENSAJE
			return ExpresionTipo.getError();
		}
	}
	
	/**
	 * 205. RESTO_ADD → + ADDITIVE_EXPRESSION
	 *   { if (RESTO_ADD.tipo_h == ADDITIVE_EXPRESSION.tipo_s) then
	 *    	if ((RESTO_ADD.tipo_h := entero) & ADDITIVE_EXPRESSION.tipo_s := entero) then 
	 *  	  		RESTO_ADD.tipo_s := entero;
	 * 		else if ((RESTO_ADD.tipo_h := entero) & (ADDITIVE_EXPRESSION.tipo_s := real) then 
	 *    			RESTO_ADD.tipo_s := real;
	 *    	else if ((RESTO_ADD.tipo_h := real) & (ADDITIVE_EXPRESSION.tipo_s := entero) then 
	 *    			RESTO_ADD.tipo_s := real;
	 *    	else if ((RESTO_ADD.tipo_h := real) & (ADDITIVE_EXPRESSION.tipo_s := real) then 
	 *    			RESTO_ADD.tipo_s := real;
	 *    else
	 *    	RESTO_ADD.tipo_s := error_tipo }
	 *    
	 * 206. RESTO_ADD → - ADDITIVE_EXPRESSION
	 * 	{ if (RESTO_ADD.tipo_h == ADDITIVE_EXPRESSION.tipo_s) then
	 *    	if ((RESTO_ADD.tipo_h := entero) & ADDITIVE_EXPRESSION.tipo_s := entero) then 
	 *  	  		RESTO_ADD.tipo_s := entero;
	 * 		else if ((RESTO_ADD.tipo_h := entero) & (ADDITIVE_EXPRESSION.tipo_s := real) then 
	 *    			RESTO_ADD.tipo_s := real;
	 *    	else if ((RESTO_ADD.tipo_h := real) & (ADDITIVE_EXPRESSION.tipo_s := entero) then 
	 *    			RESTO_ADD.tipo_s := real;
	 *    	else if ((RESTO_ADD.tipo_h := real) & (ADDITIVE_EXPRESSION.tipo_s := real) then 
	 *    			RESTO_ADD.tipo_s := real;
	 *    else
	 *    	RESTO_ADD.tipo_s := error_tipo }
	 *    
	 * 207. RESTO_ADD → lambda
	 *	 {RESTO_ADD.tipo := vacio }
	 * 
	 * @throws Exception 
	 */
	private ExpresionTipo resto_add(ExpresionTipo tipo_h) throws Exception{
		ExpresionTipo aux1,aux2 = null;
		if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.SUMA)){
			parse.add(205);
			nextToken();
			aux1 = additive_expression();
			aux2 = ExpresionTipo.sonEquivArit(aux1,tipo_h,OpAritmetico.SUMA);
		}
		else if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.RESTA)){
			parse.add(206);
			nextToken();
			aux1 = additive_expression();
			aux2 = ExpresionTipo.sonEquivArit(aux1,tipo_h,OpAritmetico.RESTA);
		} else{
			parse.add(207);
			// lambda
			return ExpresionTipo.getVacio();
		}
		/*
		//if (RESTO_ADD.tipo_h == ADDITIVE_EXPRESSION.tipo_s) then
		if (tipo_h.getTipoBasico().equals("tengo que coger el valor de ADDITIVE expression")) { //Supuestamente me viene en la misma variable ¿?¿?¿?¿?
			if (aux.getTipoBasico().equals("entero") && tipo_h.getTipoBasico().equals("entero")) {
				return new ExpresionTipo(TipoBasico.entero);
			} else if ((aux.getTipoBasico().equals("real") && tipo_h.getTipoBasico().equals("entero")) ||
						(aux.getTipoBasico().equals("entero") && tipo_h.getTipoBasico().equals("real")) ||
							((aux.getTipoBasico().equals("real") && tipo_h.getTipoBasico().equals("real")))) {
				return new ExpresionTipo(TipoBasico.real);
			} else {
				gestorErr.insertaErrorSemantico(linea, columna, "ERROR DE TIPOS"); //TODO CAMBIAR MENSAJE
				return ExpresionTipo.getError();
			}	
		}else {
			gestorErr.insertaErrorSemantico(linea, columna, "ERROR DE TIPOS"); //TODO CAMBIAR MENSAJE
			return ExpresionTipo.getError();
		}
		*/
		return aux2;
		
	}
	
	
	/**
	 * 208. SHIFT-EXPRESSION → ADDITIVE_EXPRESSION RESTO_SHIFT
	 *   { if (ADDITIVE_EXPRESSION.tipo_s != error_tipo) then
	 *   	RESTO_SHIFT.tipo_h := ADDITIVE_EXPRESSION.tipo_s;
	 *   	if (RESTO_SHIFT.tipo_s == vacio) then
	 *   		SHIFT_EXPRESSION.tipo_s == ADDITIVE_EXPRESSION.tipo_s
	 *   	else 
	 *   		SHIFT_EXPRESSION.tipo_s := RESTO_SHIFT.tipo_s
	 *   else
	 *   	SHIFT_EXPRESSION.tipo_s := error_tipo }
	 *   
	 * @throws Exception 
	 * */
	private ExpresionTipo shift_expression() throws Exception{
		ExpresionTipo aux1,aux2,aux3=null;
		parse.add(208);
		aux1 = additive_expression();
		if (!aux1.getTipoBasico().equals("error_tipo")) {
			aux3 = aux1;
			aux2 = resto_shift(aux3);
			if (aux2.equals("vacio")) {
				return new ExpresionTipo(aux1.getTipoBasico());
			} else {
				return new ExpresionTipo(aux2.getTipoBasico());
			}
		} else {
			gestorErr.insertaErrorSemantico(linea, columna, "ERROR DE TIPOS"); //TODO CAMBIAR MENSAJE
			return ExpresionTipo.getError();
		}
		
	}

	/** 209. RESTO_SHIFT →  <<  SHIFT-EXPRESSION
	 *   { if (RESTO_SHIFT.tipo_h == SHIFT_EXPRESSION.tipo_s) then
	 *   	 if ((RESTO_SHIFT.tipo_h := entero) & SHIFT_EXPRESSION.tipo_s := entero) then 
	 *   		RESTO_SHIFT.tipo_s := entero;
	 *       else
	 *          RESTO_SHIFT.tipo_s := error_tipo
	 *     else 
	 *   	RESTO_SHIFT.tipo_s := error_tipo }
	 *          
	 * 210. RESTO_SHIFT →  >> SHIFT-EXPRESSION
	 * { if (RESTO_SHIFT.tipo_h == SHIFT_EXPRESSION.tipo_s) then
	 *   	 if ((RESTO_SHIFT.tipo_h := entero) & SHIFT_EXPRESSION.tipo_s := entero) then 
	 *   		RESTO_SHIFT.tipo_s := entero;
	 *       else
	 *          RESTO_SHIFT.tipo_s := error_tipo 
	 *   else 
	 *   	RESTO_SHIFT.tipo_s := error_tipo }
	 * 211. RESTO_SHIFT → lambda
	 * 					{ RESTO_SHIFT.tipo_s := vacio }
	 * @throws Exception 
	 * */
	
	private ExpresionTipo resto_shift(ExpresionTipo tipo_h) throws Exception {
		ExpresionTipo aux1,aux2 = null;
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.DOS_MENORES)){
			parse.add(209);
			nextToken();
			aux1 = shift_expression();
			aux2 = ExpresionTipo.sonEquivLog(aux1,tipo_h,OpLogico.DOS_MENORES);
		}
		else if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.DOS_MAYORES)){
			parse.add(210);
			nextToken();
			aux1 = shift_expression();
			aux2 = ExpresionTipo.sonEquivLog(aux1,tipo_h,OpLogico.DOS_MAYORES);
		}
		else{
			parse.add(211);
			return ExpresionTipo.getVacio();
		}
		/*
		if (aux.getTipoBasico().equals(tipo_h)) {
			//¿Cual es el tipo que tengo que devolver??
			return null;
		} else {
			gestorErr.insertaErrorSemantico(linea, columna, "ERROR DE TIPOS"); //TODO CAMBIAR MENSAJE
			return ExpresionTipo.getError();
		}
		*/
		return aux2;
			
	}
	
	/**	212. RELATIONAL-EXPRESSION → SHIFT-EXPRESSION RESTO-RELATIONAL
	 * 							  { if (SHIFT_EXPRESSION.tipo_s != error_tipo)
	 * 								then   RESTO_RELATIONAL.tipo_h := SHIFT_EXPRESSION.tipo_s;
	 * 										if (RESTO_RELATIONAL.tipo_s == vacio) 
	 * 										then RELATIONAL_EXPRESSION.tipo_s := SHIFT_EXPRESSION.tipo_s
	 * 										else RELATIONAL_EXPRESSION.tipo_s := RESTO_RELATIONAL.tipo_s    
	 * 								else   RELATIONAL_EXPRESSION.tipo_s := error_tipo }
	 * @throws Exception */
	
	private ExpresionTipo relational_expression() throws Exception{
		ExpresionTipo aux1,aux2;		
		parse.add(212);
		aux1=shift_expression();
		aux2=resto_relational(aux1);
		if(aux2.equals(TipoBasico.vacio))
			return aux1;
		else
			return aux2;
	}

	/**	213. RESTO-RELATIONAL → < RESTO2-RELATIONAL
	 * 							  { if (RESTO_RELATIONAL.tipo_h == SHIFT_EXPRESSION.tipo_s)
	 * 								then   RESTO_RELATIONAL.tipo_s := logico
	 * 								else    RESTO_RELATIONAL.tipo_s := error_tipo } 
	 * 214. RESTO-RELATIONAL → > RESTO2-RELATIONAL
	 * 							  { BIS }
	 * 216. RESTO-RELATIONAL → >= RESTO2-RELATIONAL //TODO cambiar las reglas en la memoria
	 * 							  { BIS }
	 * 217. RESTO-RELATIONAL → <= RESTO2-RELATIONAL //TODO cambiar las reglas en la memoria
	 * 							  { BIS }
	 * 215. RESTO-RELATIONAL → lambda
	 * 							  { RESTO_RELATIONAL.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_relational(ExpresionTipo tipo_h) throws Exception {
		if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MENOR)){
			parse.add(213);
			nextToken();
			ExpresionTipo aux1 = shift_expression();
			ExpresionTipo aux2 = ExpresionTipo.sonEquivComp(aux1, tipo_h, OpComparacion.MENOR);
			if(aux2!=null)
				return aux2;
			else{
				gestorErr.insertaErrorSemantico(linea, columna,"Tipos no compatibles para comparacion logica");
				return ExpresionTipo.getError();
			}
		}
		else if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MAYOR)){
			parse.add(214);
			nextToken();
			ExpresionTipo aux1 = shift_expression();
			ExpresionTipo aux2 = ExpresionTipo.sonEquivComp(aux1, tipo_h, OpComparacion.MAYOR);
			if(aux2!=null)
				return aux2;
			else{
				gestorErr.insertaErrorSemantico(linea, columna,"Tipos no compatibles para comparacion logica");
				return ExpresionTipo.getError();
			}
		}
		else if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MAYOR_IGUAL)){
			parse.add(216);
			nextToken();
			ExpresionTipo aux1 = shift_expression();
			ExpresionTipo aux2 = ExpresionTipo.sonEquivComp(aux1, tipo_h, OpComparacion.MAYOR_IGUAL);
			if(aux2!=null)
				return aux2;
			else{
				gestorErr.insertaErrorSemantico(linea, columna,"Tipos no compatibles para comparacion logica");
				return ExpresionTipo.getError();
			}
		}
		else if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MENOR_IGUAL)){
			parse.add(217);
			nextToken();
			ExpresionTipo aux1 = shift_expression();
			ExpresionTipo aux2 = ExpresionTipo.sonEquivComp(aux1, tipo_h, OpComparacion.MENOR_IGUAL);
			if(aux2!=null)
				return aux2;
			else{
				gestorErr.insertaErrorSemantico(linea, columna,"Tipos no compatibles para comparacion logica");
				return ExpresionTipo.getError();
			}
		}
		else{
			parse.add(215);
			return ExpresionTipo.getVacio();
		}
		/*if(aux1.equals(tipo_h)) // TODO cambiar por llamada a SonEquivalentes(...)
			return new ExpresionTipo(TipoBasico.logico);
		else{
			// TODO insertar error: "tipos no compatibles para comparacion logica" 
			return ExpresionTipo.getError();
		}*/
			
	}
	
//	/**	216. RESTO2-RELATIONAL → = SHIFT-EXPRESSION
//		217. RESTO2-RELATIONAL → SHIFT-EXPRESSION
//	 * @throws Exception 
//	*/
//	
//	private void resto2_relational() throws Exception {
//		if(token.esIgual(TipoToken.OP_ASIGNACION, OpAsignacion.ASIGNACION)){
//			parse.add(216);
//			nextToken();
//			shift_expression();
//		}
//		else{
//			parse.add(217);
//			shift_expression();
//		}
//		
//	}
	
	/** 218. EQUALITY-EXPRESSION → RELATIONAL-EXPRESSION RESTO_EQUALITY
	 * 							  { if (RELATIONAL_EXPRESSION.tipo_s != error_tipo)
	 * 								then   RESTO_EQUALITY.tipo_h := RELATIONAL_EXPRESSION.tipo_s;
	 * 										if (RESTO_EQUALITY.tipo_s == vacio) 
	 * 										then EQUALITY_EXPRESSION.tipo_s := RELATIONAL_EXPRESSION.tipo_s
	 * 										else EQUALITY_EXPRESSION.tipo_s := RESTO_EQUALITY.tipo_s    
	 * 								else   EQUALITY_EXPRESSION.tipo_s := error_tipo }
	 * @throws Exception */
	
	private ExpresionTipo equality_expression() throws Exception{
		ExpresionTipo aux1,aux2;		
		parse.add(218);
		aux1=relational_expression(); //Debe leer el siguiente token
		aux2=resto_equality(aux1);
		if(aux2.equals(TipoBasico.vacio))
			return aux1;
		else
			return aux2;
	}

	
	/**	219. RESTO-EQUALITY → igualdad EQUALITY-EXPRESSION
	 * 						  { if (RESTO_EQUALITY.tipo_h == EQUALITY_EXPRESSION.tipo_s)
	 * 							then RESTO_EQUALITY.tipo_s := logico
	 * 							else RESTO_EQUALITY.tipo_s := error_tipo } 
	 * 	220. RESTO-EQUALITY → distinto EQUALITY-EXPRESSION
	 * 						  { BIS }
	 *  169 .RESTO-EQUALITY → lambda
	 *  					  { RESTO_EQUALITY.tipo_s := vacio }
	 * @throws Exception 
	 */
	
	private ExpresionTipo resto_equality(ExpresionTipo tipo_h) throws Exception {
		if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.IGUALDAD)){
			parse.add(219);
			nextToken();
			ExpresionTipo aux1 = equality_expression();
			ExpresionTipo aux2 = ExpresionTipo.sonEquivComp(aux1, tipo_h, OpComparacion.IGUALDAD);
			if(aux2!=null)
				return aux2;
			else{
				gestorErr.insertaErrorSemantico(linea, columna,"Tipos no compatibles para comparacion logica");
				return ExpresionTipo.getError();
			}
		}
		else if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.DISTINTO)){
			parse.add(220);
			nextToken();
			ExpresionTipo aux1 = equality_expression();
			ExpresionTipo aux2 = ExpresionTipo.sonEquivComp(aux1, tipo_h, OpComparacion.DISTINTO);
			if(aux2!=null)
				return aux2;
			else{
				gestorErr.insertaErrorSemantico(linea, columna,"Tipos no compatibles para comparacion logica");
				return ExpresionTipo.getError();
			}
		}
		else {
			parse.add(169);
			return ExpresionTipo.getVacio();
		}
		/*if(aux1.equals(tipo_h)) // TODO cambiar por llamada a SonEquivalentes(...)
			return new ExpresionTipo(TipoBasico.logico);
		else{
			// TODO insertar error: "tipos no compatibles para comparacion logica" 
			return ExpresionTipo.getError();
		}*/
		
	}
		
	/**	221. AND-EXPRESSION → EQUALITY-EXPRESSION RESTO_AND
	 * 							  { if (EQUALITY_EXPRESSION.tipo_s != error_tipo)
	 * 								then   RESTO_AND.tipo_h := EQUALITY_EXPRESSION.tipo_s;
	 * 										if (RESTO_AND.tipo_s == vacio) 
	 * 										then AND_EXPRESSION.tipo_s := EQUALITY_EXPRESSION.tipo_s
	 * 										else AND_EXPRESSION.tipo_s := RESTO_AND.tipo_s    
	 * 								else   AND_EXPRESSION.tipo_s := error_tipo }
	 * @throws Exception */
	private ExpresionTipo and_expression() throws Exception{
		ExpresionTipo aux1,aux2;		
		parse.add(221);
		aux1=equality_expression(); //Debe leer el siguiente token
		aux2=resto_and(aux1);
		if(aux2.equals(TipoBasico.vacio))
			return aux1;
		else
			return aux2;
	}
	
	/**	222. RESTO-AND → & AND-EXPRESSION
	 * 						  { if (RESTO_AND.tipo_h == AND_EXPRESSION.tipo_s)
	 * 							then RESTO_AND.tipo_s := logico
	 * 							else RESTO_AND.tipo_s := error_tipo } 
	 *	223. RESTO-AND → lambda
	 *						  { RESTO_AND.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_and(ExpresionTipo tipo_h) throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.BIT_AND)){
			parse.add(222);
			nextToken();
			ExpresionTipo aux1 = and_expression();
			ExpresionTipo aux2 = ExpresionTipo.sonEquivLog(aux1, tipo_h, OpLogico.BIT_AND);
			if(aux2!=null)
				return aux2;
			else{
				gestorErr.insertaErrorSemantico(linea, columna,"Tipos no compatibles para operacion logica binaria");
				return ExpresionTipo.getError();
			}
		}
		else{
			parse.add(223);
			return ExpresionTipo.getVacio();
		}		
	}
	
	/**	224. EXCLUSIVE-OR-EXPRESSION → AND-EXPRESSION RESTO-EXCLUSIVE
	 * 							  { if (AND_EXPRESSION.tipo_s != error_tipo)
	 * 								then   RESTO_EXCLUSIVE.tipo_h := AND_EXPRESSION.tipo_s;
	 * 										if (RESTO_EXCLUSIVE.tipo_s == vacio) 
	 * 										then EXCLUSIVE_OR_EXPRESSION.tipo_s := AND_EXPRESSION.tipo_s
	 * 										else EXCLUSIVE_OR_EXPRESSION.tipo_s := RESTO_EXCLUSIVE.tipo_s    
	 * 								else   EXCLUSIVE_OR_EXPRESSION.tipo_s := error_tipo }
	 * @throws Exception */
	private ExpresionTipo exclusive_or_expression() throws Exception{
		ExpresionTipo aux1,aux2;		
		parse.add(224);
		aux1=and_expression(); //Debe leer el siguiente token
		aux2=resto_exclusive(aux1);
		if(aux2.equals(TipoBasico.vacio))
			return aux1;
		else
			return aux2;
	}

	/**	225. RESTO-EXCLUSIVE → ^ EXCLUSIVE-OR-EXPRESSION
	 * 						  { if (RESTO_EXCLUSIVE.tipo_h == EXCLUSIVE_OR_EXPRESSION.tipo_s)
	 * 							then RESTO_EXCLUSIVE.tipo_s := logico
	 * 							else RESTO_EXCLUSIVE.tipo_s := error_tipo } 
	 * 226. RESTO-EXCLUSIVE → lambda
	 * 						  { RESTO_EXCLUSIVE.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_exclusive(ExpresionTipo tipo_h) throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.CIRCUNFLEJO)){
			parse.add(225);
			nextToken();
			ExpresionTipo aux1 = exclusive_or_expression();
			System.out.println("izquierda: "+tipo_h.getTipoBasico().toString()+"\nderecha"+aux1.getTipoBasico().toString());
			ExpresionTipo aux2 = ExpresionTipo.sonEquivLog(aux1, tipo_h, OpLogico.CIRCUNFLEJO);
			if(aux2!=null)
				return aux2;
			else{
				gestorErr.insertaErrorSemantico(linea, columna,"Tipos no compatibles para operacion logica binaria");
				return ExpresionTipo.getError();
			}
		}
		else{
			parse.add(226);
			return ExpresionTipo.getVacio();
		}		
	}
	
	/**	227. INCL-OR-EXPRESSION → EXCLUSIVE_OR_EXPRESSION RESTO_INCL_OR
	 * 							  { if (EXCLUSIVE_OR_EXPRESSION.tipo_s != error_tipo)
	 * 								then   RESTO_INCL_OR.tipo_h := EXCLUSIVE_OR_EXPRESSION.tipo_s;
	 * 										if (RESTO_INCL_OR.tipo_s == vacio) 
	 * 										then INCL_OR_EXPRESSION.tipo_s := EXCLUSIVE_OR_EXPRESSION.tipo_s
	 * 										else INCL_OR_EXPRESSION.tipo_s := RESTO_INCL_OR.tipo_s    
	 * 								else   INCL_OR_EXPRESSION.tipo_s := error_tipo }
	 * @throws Exception */
	private ExpresionTipo incl_or_expression() throws Exception{
		ExpresionTipo aux1,aux2;		
		parse.add(227);
		aux1=exclusive_or_expression(); //Debe leer el siguiente token
		aux2=resto_incl_or(aux1);
		if(aux2.equals(TipoBasico.vacio))
			return aux1;
		else
			return aux2;
	}

	/**	228. RESTO-INCL-OR → | INCL-OR-EXPRESSION
	 * 						  { if (RESTO_INCL_OR.tipo_h == INCL_OR_EXPRESSION.tipo_s)
	 * 							then RESTO_INCL_OR.tipo_s := logico
	 * 							else RESTO_INCL_OR.tipo_s := error_tipo } 
	 * 229. RESTO-INCL-OR → lambda
	 * 						  { RESTO_INCL_OR.tipo_s := vacio }
	 * @throws Exception 
	*/
	private ExpresionTipo resto_incl_or(ExpresionTipo tipo_h) throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.BIT_OR)){
			parse.add(228);
			nextToken();
			ExpresionTipo aux1 = incl_or_expression();
			ExpresionTipo aux2 = ExpresionTipo.sonEquivLog(aux1, tipo_h, OpLogico.BIT_OR);
			if(aux2!=null)
				return aux2;
			else
				return ExpresionTipo.getError();// TODO insertar error: "tipos no compatibles para operacion logica binaria"
		}
		else{
			parse.add(229);
			return ExpresionTipo.getVacio();
		}
	}
	
	/**	230. LOG-AND-EXPRESSION → INCL-OR-EXPRESSION RESTO_LOG-AND
	 * 							  { if (INCL_OR_EXPRESSION.tipo_s != error_tipo)
	 * 								then   RESTO_LOG_AND.tipo_h := INCL_OR_EXPRESSION.tipo_s;
	 * 										if (RESTO_LOG_AND.tipo_s == vacio) 
	 * 										then LOG_AND_EXPRESSION.tipo_s := INCL_OR_EXPRESSION.tipo_s
	 * 										else LOG_AND_EXPRESSION.tipo_s := RESTO_LOG_AND.tipo_s    
	 * 								else   LOG_AND_EXPRESSION.tipo_s := error_tipo }
	 * @throws Exception */
	private ExpresionTipo log_and_expression() throws Exception{
		ExpresionTipo aux1,aux2;		
		parse.add(230);
		aux1=incl_or_expression(); //Debe leer el siguiente token
		aux2=resto_log_and(aux1);
		if(aux2.equals(TipoBasico.vacio))
			return aux1;
		else
			return aux2;
	}
	
	/**	231. RESTO-LOG-AND → && LOG-AND-EXPRESSION
	 * 						  { if (RESTO_LOG_AND.tipo_h == LOG_AND_EXPRESSION.tipo_s)
	 * 							then RESTO_LOG_AND.tipo_s := logico
	 * 							else RESTO_LOG_AND.tipo_s := error_tipo } 
	 * 232. RESTO-LOG-AND → lambda
	 * 						  { RESTO_LOG_AND.tipo_s := vacio }
	 * @throws Exception */
	private ExpresionTipo resto_log_and(ExpresionTipo tipo_h) throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.AND)){
			parse.add(231);
			nextToken();
			ExpresionTipo aux1 = log_and_expression();
			ExpresionTipo aux2 = ExpresionTipo.sonEquivLog(aux1, tipo_h, OpLogico.AND);
			if(aux2!=null)
				return aux2;
			else{
				gestorErr.insertaErrorSemantico(linea, columna,"Tipos no compatibles para operacion logica");
				return ExpresionTipo.getError();
			}
		}
		else{
			parse.add(232);
			return ExpresionTipo.getVacio();
		}
	}
	
	/**	233. LOG-OR-EXPRESSION → LOG-AND-EXPRESSION RESTO_LOG-OR
	 * 							  { if (LOG_AND_EXPRESSION.tipo_s != error_tipo)
	 * 								then   RESTO_LOG_OR.tipo_h := LOG_AND_EXPRESSION.tipo_s;
	 * 										if (RESTO_LOG_OR.tipo_s == vacio) 
	 * 										then LOG_OR_EXPRESSION.tipo_s := LOG_AND_EXPRESSION.tipo_s
	 * 										else LOG_OR_EXPRESSION.tipo_s := RESTO_LOG_OR.tipo_s    
	 * 								else   LOG_OR_EXPRESSION.tipo_s := error_tipo }
	 * @throws Exception */
	private ExpresionTipo log_or_expression() throws Exception{
		ExpresionTipo aux1,aux2;		
		parse.add(233);
		aux1=log_and_expression(); //Debe leer el siguiente token
		aux2=resto_log_or(aux1);
		if(aux2.equals(TipoBasico.vacio))
			return aux1;
		else
			return aux2;
	}

	/**	234. RESTO-LOG-OR → || LOG-OR-EXPRESSION
	 * 						  { if (RESTO_LOG_OR.tipo_h == LOG_OR_EXPRESSION.tipo_s)
	 * 							then RESTO_LOG_OR.tipo_s := logico
	 * 							else RESTO_LOG_OR.tipo_s := error_tipo } 
	 * 235. RESTO-LOG-OR → lambda
	 * 						  { RESTO_LOG_OR.tipo_s := vacio }
	 * @throws Exception 
	*/
	private ExpresionTipo resto_log_or(ExpresionTipo tipo_h) throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.OR)){
			parse.add(234);
			nextToken();
			ExpresionTipo aux1 = log_or_expression();
			ExpresionTipo aux2 = ExpresionTipo.sonEquivLog(aux1, tipo_h, OpLogico.OR);
			if(aux2!=null)
				return aux2;
			else{
				gestorErr.insertaErrorSemantico(linea, columna,"Tipos no compatibles para operacion logica");
				return ExpresionTipo.getError();
			}
		}
		else{
			parse.add(235);
			return ExpresionTipo.getVacio();
		}
	}
	
	//Este metodo nunca se usa /////
	//deberia llamarse desde assignment_expression(), pero no lo hace ya que se ha factorizado por la izquierda
	//ya que tanto conditional_expresion como assignment_expression tienen la regla log_or_expression
//	/**	236. CONDITIONAL_EXPRESION → LOGICAL_OR_EXPRESSION RESTO_CONDITIONAL*/
//	private void conditional_expression(){///////////////////////////////////////
//		parse.add(236);
//		log_or_expression(); 
//		resto_conditional();
//	}
	
	/**	
	 * 238. RESTO_CONDITIONAL →  ? EXPRESSION  :  ASSIGNMENT_EXPRESSION
	 * 						  { if (RESTO_CONDITIONAL.tipo_h == logico)
	 * 							then   	if (EXPRESSION.tipo_s == ASSIGNMENT_EXPRESSION.tipo_s)
	 * 									then RESTO_CONDITIONAL.tipo_s := EXPRESSION.tipo_s
	 * 									else RESTO_CONDITIONAL.tipo_s := error_tipo 
	 * 							else RESTO_CONDITIONAL.tipo_s := error_tipo }
	 * 239. RESTO_CONDITIONAL → lambda
	 * 						  { RESTO_CONDITIONAL.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_conditional(ExpresionTipo tipo_h) throws Exception {//////////////////////////////////////////////
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.INTEROGACION)){
			parse.add(238);
			nextToken();
			ExpresionTipo aux1 = expression();
			if(token.esIgual(TipoToken.SEPARADOR, Separadores.DOS_PUNTOS)){
				nextToken();
				ExpresionTipo aux2 = assignment_expression();
				if(ExpresionTipo.sonEquivLog(tipo_h, new ExpresionTipo(TipoBasico.logico), OpLogico.AND)!=null){
					ExpresionTipo aux3 = ExpresionTipo.sonEquivArit(aux1, aux2, OpAritmetico.SUMA); 
					// Realmente retorna el tipo del resultado?????
					if(aux3!=null)
						return aux3; 
					else{
						gestorErr.insertaErrorSemantico(linea, columna,"Tipos no compatibles para asignacion");
						return ExpresionTipo.getError();
					}
				}
				else{
					gestorErr.insertaErrorSemantico(linea, columna,"Tipo no compatibles con booleano");
					return ExpresionTipo.getError();
				}
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna,"Se esperaba \":\"");
				//ruptura=parse.size();
				return ExpresionTipo.getVacio();
			}
		}
		else{
			parse.add(239);
			return ExpresionTipo.getVacio();
		}		
	}
	
	/**
	 * 241. ASSIGNMENT_EXPRESSION → LOG_OR_EXPRESSION RESTO_ASSIG
	 * 							  { if (LOG_OR_EXPRESSION.tipo_s != error_tipo)
	 * 								then   RESTO_ASSIG.tipo_h := LOG_OR_EXPRESSION.tipo_s;
	 * 										if (RESTO_ASSIG.tipo_s == vacio) 
	 * 										then ASSIGNMENT_EXPRESSION.tipo_s := LOG_OR_EXPRESSION.tipo_s
	 * 										else ASSIGNMENT_EXPRESSION.tipo_s := RESTO_ASSIG.tipo_s    
	 * 								else   ASSIGNMENT_EXPRESSION.tipo_s := error_tipo }
	 * @throws Exception 
	 */
	private ExpresionTipo assignment_expression() throws Exception {
		ExpresionTipo aux1,aux2;		
		parse.add(241);
		aux1=log_or_expression(); //Debe leer el siguiente token
		aux2=resto_assig(aux1);
		if(aux2.equals(TipoBasico.vacio))
			return aux1;
		else
			return aux2;
	}
	
	/**
	 * 242. RESTO_ASSIG → RESTO_CONDITIONAL
	 * 						  { RESTO_CONDITIONAL.tipo_h := RESTO_ASSIG.tipo_h;
	 * 							RESTO_ASSIG.tipo_s := RESTO_CONDITIONAL.tipo_s }
	 * 243. RESTO_ASSIG → op_asignacion ASSIGNMENT_EXPRESSION
	 * 						  { if (RESTO_ASSIG.tipo_h == ASSIGNMENT_EXPRESSION.tipo_s)
	 * 							then RESTO_ASSIG.tipo_s := RESTO_ASSIG.tipo_h
	 * 							else RESTO_ASSIG.tipo_s := error_tipo }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_assig(ExpresionTipo tipo_h) throws Exception {
		if(token.esIgual(TipoToken.OP_ASIGNACION)){
			parse.add(243);
			nextToken();
			ExpresionTipo aux1 = assignment_expression();
			ExpresionTipo aux2 = ExpresionTipo.sonEquivAsig(aux1, tipo_h, OpAsignacion.ASIGNACION);
			if(aux2!=null)
				return aux2;
			else{
				gestorErr.insertaErrorSemantico(linea, columna,"Tipos no compatibles para asignacion");
				return ExpresionTipo.getError();
			}
				
		}
		else{
			parse.add(242);
			return resto_conditional(tipo_h);// Porque resto_conditional implementa las dos reglas que faltan: 238. RESTO_ASSIG →  ? EXPRESSION  :  ASSIGNMENT_EXPRESSION y 239. RESTO_ASSIG → lambda
		}		
	}

	/**
	 * 246. EXPRESSION → ASSIGNMENT-EXPRESSION RESTO_EXP
	 * 					{ 	if (ASSIGNMENT-EXPRESSION.tipo_s != error_tipo)
	 * 						then	if (RESTO_EXP.tipo_s == vacio)
	 * 								then EXPRESSION.tipo_s := ASSIGNMENT-EXPRESSION.tipo_s
	 * 								else EXPRESSION.tipo_s := RESTO_EXP.tipo_s 
	 * 						else EXPRESSION.tipo_s := error_tipo }  
	 * @throws Exception 
	 */
	private ExpresionTipo expression() throws Exception {
		parse.add(246);
		ExpresionTipo aux1 = assignment_expression();
		ExpresionTipo aux2 = resto_exp();
		if(aux1.equals(TipoBasico.error_tipo))
			return aux1;
		else{
			if(aux2.equals(TipoBasico.vacio))
				return aux1;
			else
				return aux2;
		}
	}
	
	
	/**
	 * 247. RESTO_EXP → , EXPRESSION
	 * 					{ RESTO_EXP.tipo_s := EXPRESSION.tipo_s } 
	 * 248. RESTO_EXP→ lambda
	 * 					{ RESTO_EXP.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo resto_exp() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.COMA)){
			parse.add(247);
			nextToken();
			return expression();
		}	
		else {
			parse.add(248);
			return ExpresionTipo.getVacio();
		}
	}
	
	/**
	 * 249. EXPRESSIONOPT → EXPRESSION 
	 * 						{ EXPRESSIONOPT.tipo_s := EXPRESSION.tipo_s } 
	 * 250. EXPRESSIONOPT → lambda
	 * 						{ EXPRESSIONOPT.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo expressionOpt() throws Exception {
		if(primeroDeExpression()) {
			parse.add(249);
			return expression();
		}
		else{
			parse.add(250);
			return ExpresionTipo.getVacio();
		}
			
		
	}
	
	/**
	 * Metodo que comprueba si el token actual corresponde a un terminal de expression
	 */
	private boolean primeroDeExpression() {
		return (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)
			 ||	token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.MULTIPLICACION)
			 ||	token.esIgual(TipoToken.OP_LOGICO,OpLogico.BIT_AND)
			 ||	token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.SUMA)
			 ||	token.esIgual(TipoToken.OP_LOGICO,OpLogico.NOT)
			 ||	token.esIgual(TipoToken.OP_LOGICO,OpLogico.SOBRERO)
			 ||	token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.RESTA)
			 || token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.INCREMENTO)	
			 || token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.DECREMENTO)
			 || token.esIgual(TipoToken.PAL_RESERVADA, 50) //sizeof
			 || token.esIgual(TipoToken.PAL_RESERVADA, 1) //alignof		
			 || token.esIgual(TipoToken.PAL_RESERVADA, 39) //noexcept
			 || token.esIgual(TipoToken.PAL_RESERVADA, 63) //typeid
			 || (token.esIgual(TipoToken.PAL_RESERVADA) && 
						gestorTS.esTipoSimple((Integer)token.getAtributo()))
			 || token.esIgual(TipoToken.PAL_RESERVADA, 57) //this
			 || esLiteral()
			 || token.esIgual(TipoToken.IDENTIFICADOR));
	}


}