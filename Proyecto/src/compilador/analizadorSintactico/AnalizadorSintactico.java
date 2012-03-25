package compilador.analizadorSintactico;

 
import java.util.Vector;

import compilador.analizadorLexico.*;
import compilador.analizadorLexico.Token.*;
import compilador.analizadorSemantico.*;
import compilador.analizadorSemantico.ExpresionTipo.TipoBasico;
import compilador.analizadorSemantico.ExpresionTipo.TipoNoBasico;
import compilador.gestionErrores.GestorErrores;
import compilador.tablaSimbolos.*;
import compilador.tablaSimbolos.Tipo.EnumTipo;

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
	 * Vector para usado en los casos en los que haga falta ampliar la ventana de tokens
	 */
	private Vector<Token> ventana;
	
	private Tipo tipo;
	private EntradaTS entradaTS;
	
	private int numDefaults;
	
	/** Numero de linea del token anterior */
	private int linea;
	/** Numero de columna del siguiente al token anterior */
	private int columna;
		
	public AnalizadorSintactico(AnalizadorLexico lexico){
		this.lexico = lexico;
		parse = new Vector<Integer>();
		tokens = new Vector<Token>();
		ventana = new Vector<Token>();
		nextToken();
		gestorTS = GestorTablasSimbolos.getGestorTS();
		gestorErr = GestorErrores.getGestorErrores();
		tipo = null;
		entradaTS = null;
		try {
			programa();
		} catch (Exception e) {
			// TODO Ha habido un error
		}
	}
	
	private void nextToken() {
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
	
	private void idConst() throws Exception {
		entradaTS = (EntradaTS)token.getAtributo();
		entradaTS.setTipo(tipo);
		entradaTS.setConstante(true);
		nextToken();
		if(token.esIgual(TipoToken.OP_ASIGNACION,OpAsignacion.ASIGNACION)) {
			nextToken();
			if(literal()) {
//				Object valor = token.getAtributo(); // TODO depende del tipo... a ver que se hace con el...
//				System.out.println("inicializacion constante " + entradaTS.getLexema() + " con " + valor);
			} else {
				gestorErr.insertaErrorSintactico(linea, columna, "Constante mal inicializada");
			}
			
		} else {
			gestorErr.insertaErrorSintactico(linea, columna, "Constante no inicializada");
		}
	}


	private void id() {
		entradaTS = (EntradaTS)token.getAtributo();
		entradaTS.setTipo(tipo);
		entradaTS.setConstante(false);
		nextToken();
	}
	
	/**
	 * Metodo que devuelve el LITERAL y lee el siguiente token
	 */
	private boolean literal() {		
		//TODO hacer algo con los valores, si hace falta...
		if(token.esIgual(TipoToken.LIT_CADENA)) {
//			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
//			System.out.println("LITERAL CADENA: " + valor);
			nextToken();
			return true;
		}
		else if(token.esIgual(TipoToken.LIT_CARACTER)){
//			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
//			System.out.println("LITERAL CARACTER: " + valor);
			nextToken();
			return true;
		} 
		else if (token.esIgual(TipoToken.NUM_ENTERO)){
//			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
//			System.out.println("NUMERO ENTERO: " + valor);
			nextToken();
			return true;
		}
		else if (token.esIgual(TipoToken.NUM_REAL)){
//			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
//			System.out.println("NUMERO REAL: " + valor);
			nextToken();
			return true;
		}
		else if (token.esIgual(TipoToken.NUM_REAL_EXPO)){
//			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
//			System.out.println("NUMERO REAL EXPO: " + valor);
			nextToken();
			return true;
		}
		else if (token.esIgual(TipoToken.PAL_RESERVADA) &&
				( (Integer)token.getAtributo() == 27 /*false*/ || 
				  (Integer)token.getAtributo() == 60 /*true*/)){
//			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
//			System.out.println("BOOLEANO: " + valor);
			nextToken();
			return true;
		}
		
		return false;		
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
	
	public Vector<Token> getTokens() {
		return tokens;

	}
	
	public String getStringTokens() {
		String string = "";
		for(Token token : tokens)
			string += "\nTipo: " + token.getTipo() + " Atr: " + token.getAtributo();
		return string+"\n";
	}	
	
	/**
	 * 1. PROGRAMA → LIBRERIA RESTO_PROGRAMA eof
	 * @throws Exception 
	 */
	private void programa() throws Exception {
		parse.add(1);
		libreria();
		resto_programa();
		if (!token.esIgual(TipoToken.EOF)) {
			gestorErr.insertaErrorSintactico(linea, columna,"Palabra o termino \""+token.atrString()+"\" inesperado.");//+lexico.getLexema()+"\" inesperado.");
			//ruptura=parse.size();
		}
	}
	
	
	/**
	 *  2. LIBRERIA → #include RESTO_LIBRERIA
	 *  3. LIBRERIA -> lambda
	 *  				{ LIBRERIA.tipo := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo libreria() throws Exception {
		if (!token.esIgual(TipoToken.EOF)) {
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ALMOHADILLA)) {
				parse.add(2);
				nextToken();
				if(token.esIgual(TipoToken.PAL_RESERVADA, 77)) {
					//token.esIgual(TipoToken.IDENTIFICADOR) && 
					//"include".equals( ((EntradaTS)token.getAtributo()).getLexema() )) {
					nextToken();
					resto_libreria();
					//TODO: return resto_libreria();
					return null;
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta palabra \"include\"");
					return null;
					//ruptura=parse.size();
				}
					//gestorErr.insertaErrorSintactico(linea, columna,
						//"Falta la palabra reservada \"include\"");				
				//}
			} else {
				parse.add(3);
				return new ExpresionTipo(TipoBasico.vacio);
			}
		} else {
			//gestorErr.insertaErrorSintactico(linea, columna,"Fin de fichero inesperado"); 
			// PORQUÉ HEMOS COMENTADO LO DE ARRIBA??
			return null;
		}
	}

	/**
	 * 4. RESTO_LIBRERIA → LIT_CADENA LIBRERIA
	 * 5. RESTO_LIBRERIA → <ID.ID> LIBRERIA
	 * @throws Exception 
	 */
	private void resto_libreria() throws Exception {
		if(token.esIgual(TipoToken.LIT_CADENA)) {
			parse.add(4);
			nextToken();
			//System.out.println("libreria con lit cadena");
			libreria();
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
							//System.out.println("libreria con angulos");
							libreria();
						} else {
							gestorErr.insertaErrorSintactico(linea, columna,"Falta separador \">\"");
							//ruptura=parse.size();
						}
					} else {
						gestorErr.insertaErrorSintactico(linea, columna,"Falta extension de libreria");
						//ruptura=parse.size();
					}
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \".\"");
					//ruptura=parse.size();
				}
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta la libreria");
				//ruptura=parse.size();
			}	
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta separador \"<\" o \"___\"");
			//ruptura=parse.size();
		}
		
	}
	
	
	/**
	 * 105. RESTO_PROGRAMA → class ID { CUERPO_CLASE } ;
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
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_LLAVE)) {
					nextToken();
					cuerpo_clase();
					if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)) {
						nextToken();
						if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
							nextToken();
							return null;
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
			return cosas();
			
		}
	}
	
	
	/**
	 * 107.CUERPO_CLASE → friend RESTO_FRIEND CUERPO CLASE
	 * 108.CUERPO_CLASE → public : LISTA_CLASE CUERPO_CLASE
	 * 109.CUERPO_CLASE → private : LISTA_CLASE CUERPO_CLASE
	 * 110.CUERPO_CLASE → protected : LISTA_CLASE CUERPO_CLASE
	 * 111.CUERPO_CLASE → lambda
	 * @throws Exception 
	 */
	private void cuerpo_clase() throws Exception {
		if (!token.esIgual(TipoToken.EOF)) {
			if (token.esIgual(TipoToken.PAL_RESERVADA, 30)) {
				parse.add(107);
				nextToken();
				resto_friend();
				cuerpo_clase();
			} else if (token.esIgual(TipoToken.PAL_RESERVADA, 44)) {
				parse.add(108);
				nextToken();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.DOS_PUNTOS)) {
					nextToken();
					lista_clase();
					cuerpo_clase();
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \":\"");
					//ruptura=parse.size();
				}
			} else if (token.esIgual(TipoToken.PAL_RESERVADA, 42)) {
				parse.add(109);
				nextToken();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.DOS_PUNTOS)) {
					nextToken();
					lista_clase();
					cuerpo_clase();
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \":\"");
					//ruptura=parse.size();
				}
			} else if (token.esIgual(TipoToken.PAL_RESERVADA, 43)) {
				parse.add(110);
				nextToken();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.DOS_PUNTOS)) {
					nextToken();
					lista_clase();
					cuerpo_clase();
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \":\"");
					//ruptura=parse.size();
				}
			} else {
				parse.add(111);
			}
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Fin de fichero inesperado");
			//ruptura=parse.size();
		}
	}
	
	
	/**
	 * 112.RESTO_FRIEND → void RESTO_FRIEND2
	 * 113.RESTO_FRIEND → TIPO RESTO_FRIEND2
	 * @throws Exception 
	 */
	private void resto_friend() throws Exception {
		if (token.esIgual(TipoToken.PAL_RESERVADA, 69)) {
			parse.add(112);
			nextToken();
			resto_friend2();
		//} else if (tipo()) {
		}else if (tipo()!=null){
			parse.add(113);
			nextToken();
			resto_friend2();
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta tipo de retorno (o void)");
			//ruptura=parse.size();
		}
	}
	
	
	/**
	 * 114.RESTO_FRIEND2 → ID ( LISTA_PARAM ) ; CUERPO_CLASE
	 * @throws Exception 
	 */
	private void resto_friend2() throws Exception {
		if (token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(114);
			nextToken();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
				nextToken();
				lista_param();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
					nextToken();
					if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
						nextToken();
						cuerpo_clase();
					} else {
						gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \";\"");
						//ruptura=parse.size();
					}
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \")\"");
					//ruptura=parse.size();
				}	
			} else  {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
				//ruptura=parse.size();
			}
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta el identificador");
			//ruptura=parse.size();
		}
	}
	
	
	/**
	 * 115.LISTA_CLASE → void RESTO_LINEA LISTA_CLASE
	 * 116.LISTA_CLASE → TIPO RESTO_LINEA
	 * 141.LISTA_CLASE → lambda
	 * @throws Exception 
	 */
	private void lista_clase() throws Exception {
		if (token.esIgual(TipoToken.PAL_RESERVADA, 69)) {
			parse.add(115);
			nextToken();
			resto_linea();
			lista_clase();
		//} else if (tipo()) {
		}else if (tipo()!=null){
			parse.add(116);
			resto_linea();
		} else {
			parse.add(141);
		}
	}
	
	
	/**
	 * 117.RESTO_LINEA → PUNT ID RESTO_LINEA2
	 * @throws Exception 
	 */
	private void resto_linea() throws Exception {
		boolean puntero = punt();
		//ExpresionTipo aux = punt();
		if(puntero) { // no puede ser un metodo, tiene que ser una variable
		//if(aux!=null){
			nextToken();
			if (token.esIgual(TipoToken.IDENTIFICADOR)) {
				parse.add(117);
				nextToken();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
					parse.add(119);
					nextToken();
					lista_clase();
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \";\" o \"(\"");
					//ruptura=parse.size();
				}
			}
		} else if (token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(117);
			nextToken();
			resto_linea2();
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta el identificador");
			//ruptura=parse.size();
		}
	}
	
	
	/**
	 * 118.RESTO_LINEA2 → ( RESTO_METODO 
	 * 119.RESTO_LINEA2 → ; LISTA_CLASE
	 * @throws Exception 
	 */
	private void resto_linea2() throws Exception {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(118);
			nextToken();
			resto_metodo();
		} else if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
			parse.add(119);
			nextToken();
			lista_clase();
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \";\" o \"(\"");
			
		}
	}
	
	
	/**
	 * 120.RESTO_METODO → LISTA_PARAM ) RESTO_METODO2 ; LISTA_CLASE
	 * @throws Exception 
	 */
	private void resto_metodo() throws Exception {
		parse.add(120);
		lista_param();
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
			nextToken();
			resto_metodo2();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
				nextToken();
				lista_clase();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \";\"");
				
			}
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
			
		}
	}
	
	
	/**
	 * 121.RESTO_METODO2 → { CUERPO }
	 * 122.RESTO_METODO2 → lambda
	 * @throws Exception 
	 */
	private void resto_metodo2() throws Exception {
		if (!token.esIgual(TipoToken.EOF)) {
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_LLAVE)) {
				parse.add(121);
				nextToken();
				cuerpo();
				if (!token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)) {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta }");
				}
				nextToken();
			} else {
				parse.add(122);
			}
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Fin de fichero inesperado");
			//ruptura=parse.size();
		}
	}
	
	/**
	 * 6. TIPO → id		
	 * 			{ TIPO.tipo := ?? }								
	 * 7. TIPO → TIPO_SIMPLE
	 * 			{ TIPO.tipo := TIPO_SIMPLE.tipo }
	 */
	private ExpresionTipo tipo() {
		ExpresionTipo tipo_s;
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(6);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			//TODO: tipo_s = Tipo semantico en la TS
			nextToken();
			//return true;
			return new ExpresionTipo(TipoBasico.vacio); 
		} else if(token.esIgual(TipoToken.PAL_RESERVADA) && 
				gestorTS.esTipoSimple((Integer)token.getAtributo())){
			parse.add(7);
			tipo = new Tipo(EnumTipo.DEFINIDO, (gestorTS.getTipoSimple((Integer)token.getAtributo())));  //MAL!!!!!!
			tipo_s = gestorTS.tipoAsociado((Integer)token.getAtributo());
			nextToken();
			
			//return true;
			/*if(tipo.getTipo().equals(EnumTipo.ENTERO))
				return new ExpresionTipo(TipoBasico.entero);
			else
				return new ExpresionTipo(TipoBasico.vacio); //TODO: CORREGIR CON EL RESTO DE TIPOS!!!!*/
			if(tipo_s!=null){
				if(tipo_s.esTipoBasico())
					System.out.println("Declaramos "+ ((EntradaTS)token.getAtributo()).getLexema()+ " con tipo semantico: "+tipo_s.getTipoBasico().toString());
				else
					System.out.println("Declaramos "+ ((EntradaTS)token.getAtributo()).getLexema()+ " con tipo semantico: "+tipo_s.getTipoNoBasico().toString());
				return tipo_s;
			}else
				return new ExpresionTipo(TipoBasico.error_tipo);
		}
		//return false;
		return null;
	}
	
	private boolean tipo_simple() {
		if(token.esIgual(TipoToken.PAL_RESERVADA) && 
				gestorTS.esTipoSimple((Integer)token.getAtributo())){
			parse.add(7);
			tipo = new Tipo(EnumTipo.DEFINIDO, (gestorTS.getTipoSimple((Integer)token.getAtributo())));
			nextToken();
			return true;
		}
		return false;
	}


	/**	
	 *  8. COSAS → const TIPO ID = LITERAL INIC_CONST ; COSAS
	 *  9. COSAS → TIPO ID COSAS2 COSAS
	 * 10. COSAS → void ID ( LISTA_PARAM ) COSAS3 COSAS
	 * 				{ 	if (LISTA_PARAM.tipo != error_tipo) & (COSAS3 != error_tipo) & (COSAS' != error_tipo)
	 * 					then COSAS.tipo := vacio
	 * 					else COSAS.tipo := error_tipo  } 
	 * 11. COSAS → enum ID { LISTANOMBRES } ; COSAS
	 * 12. COSAS → struct RESTO_ST COSAS
	 * 13. COSAS → lambda
	 * @throws Exception 
	 */
	private ExpresionTipo cosas() throws Exception {
		if(!token.esIgual(TipoToken.EOF)) {
			if(token.esIgual(TipoToken.PAL_RESERVADA, 9 /*const*/)){
				parse.add(8);
				nextToken();
				//if(tipo()) {
				if(tipo()!=null){
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						idConst(); // ID = LITERAL
						inic_const();
						if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
							gestorErr.insertaErrorSintactico(linea, columna,"Falta separador \";\"");
							//ruptura=parse.size();
						} else {
							nextToken();
							cosas();
						}
					} else {
						gestorErr.insertaErrorSintactico(linea, columna,"Falta el nombre de la variable");
						//ruptura=parse.size();
					}
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta tipo de la variable");
					//ruptura=parse.size();
				}
			//} else if (tipo()) {
			}else if (tipo()!=null){
				parse.add(9);
				if(token.esIgual(TipoToken.IDENTIFICADOR)) {
					id();
					cosas2();	
					cosas();			
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el nombre de la variable");
					//ruptura=parse.size();
				}
			} else if(token.esIgual(TipoToken.PAL_RESERVADA, 69 /* void */)){
				parse.add(10);
				nextToken();
				ExpresionTipo aux1,aux2,aux3;
				if(token.esIgual(TipoToken.IDENTIFICADOR)) {
					nextToken();
					if(token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)){
						nextToken();
						aux1 = lista_param();
						if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
							nextToken();
							aux2 = cosas3();
							aux3 = cosas();
							if(aux1.getTipoBasico()!=TipoBasico.error_tipo && aux2.getTipoBasico()!=TipoBasico.error_tipo && aux3.getTipoBasico()!=TipoBasico.error_tipo)
								return new ExpresionTipo(TipoBasico.vacio);
							else
								return new ExpresionTipo(TipoBasico.error_tipo);
						} else {
							if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)||token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)){
								gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \")\"");
								return null;
								//ruptura=parse.size();
							}
							else{
								gestorErr.insertaErrorSintactico(linea, columna,"Palabra o termino \""+token.atrString()+"\" inesperado.");//"Palabra o termino \""+lexico.getLexema()+"\" inesperado.");
								return null;
								//ruptura=parse.size();
							}
						}
					} else {
						gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \"(\"");
						return null;
						//ruptura=parse.size();
					}
				} else {
					gestorErr.insertaErrorSintactico(linea, columna, "Falta nombre de la funcion");
					return null;
					//ruptura=parse.size();
				}
			} else if(token.esIgual(TipoToken.PAL_RESERVADA, 23)){
				parse.add(11);
				nextToken();
				if(token.esIgual(TipoToken.IDENTIFICADOR)){
					id();
					if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)){
						nextToken();
						listaNombres();
						if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
							nextToken();
							if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
								gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \";\"");
								//ruptura=parse.size();
							} else {
								nextToken();
								cosas();
							}
						} else{
							gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \"}\"");
							//ruptura=parse.size();
						}
					} else{
						gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \"{\"");
						//ruptura=parse.size();
					}
				} else{
					gestorErr.insertaErrorSintactico(linea, columna, "Falta nombre de lista");	//No me gusta... :(
					//ruptura=parse.size();
				}
			} else if(token.esIgual(TipoToken.PAL_RESERVADA, 54)){
				parse.add(12);
				nextToken();
				resto_st();
				cosas();
			} else {
				parse.add(13);
				//gestorErr.insertaErrorSintactico(linea, columna, "");
			}
		} 
		return null; //TODO: completar con las expresiones de tipo correspondientes
		
	}
	
	
		
	/** 
	 * 14. LISTANOMBRES → ID RESTO_ListaNombres
	 * 15. LISTANOMBRES → lambda
	 * @throws Exception 
	 */
	private void listaNombres() throws Exception {
		if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(14);
			id();
			resto_ln();
		}
		else{
			parse.add(15);
		}
	}
	
	/**	102. RESTO_ListaNombres → , ID RESTO_ListaNombres
		103. RESTO_ListaNombres → lambda
	 * @throws Exception 
	*/
	private void resto_ln() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(102);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				id();
				resto_ln();
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna, "Falta identificador de lista");
				//ruptura=parse.size();
			}
		}
		else{
			parse.add(103);
		}
	}

	/**	
	 * 16. COSAS2 → ( LISTA_PARAM ) COSAS3
	 * 17. COSAS2 → INICIALIZACION  DECLARACIONES ;
	 * @throws Exception 
	 */
	private void cosas2() throws Exception {
		//16. COSAS2 → ( LISTA_PARAM ) COSAS3
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) {
			parse.add(16);
			nextToken();
			lista_param();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
				nextToken();
				cosas3();
			} else {
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)||token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)){
					gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \")\"");
					//ruptura=parse.size();
				}
				else{
					gestorErr.insertaErrorSintactico(linea, columna,"Palabra o termino \""+token.atrString()+"\" inesperado.");
					//ruptura=parse.size();
				}
			}
		} 
		//17. COSAS2 → INICIALIZACION  DECLARACIONES ;
		else {
			parse.add(17);
			//inicializacion(); 
			inicializacion(null); //TODO: llamada con expresion de tipos correcta!!
			//declaraciones();		
			declaraciones(null); //TODO: llamada con expresion de tipos correcta!!
			if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				gestorErr.insertaErrorSintactico(linea, columna,"Palabra o termino \""+token.atrString()+"\" inesperado. Falta separador \";\"");
				//ruptura=parse.size();
			}
			nextToken();
		}
	}
	

	/**	18. COSAS3 → ;
	 * 				{ COSAS.tipo := vacio }
		19. COSAS3 → { CUERPO }
	 * 				{ COSAS.tipo := CUERPO.tipo }
	 * @throws Exception 
	*/
	private ExpresionTipo cosas3() throws Exception {
		
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
			parse.add(18);
			nextToken();
//			System.out.println("cabecera funcion " + entradaTS.getLexema());
			return new ExpresionTipo(TipoBasico.vacio);
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			parse.add(19);
			nextToken();
			ExpresionTipo aux = cuerpo();
			if (!token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)) {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta }");
			}
			nextToken();
			return aux;
		}
		else {
			gestorErr.insertaErrorSintactico(linea, columna,"Se esperaba \";\" o \"{\" ");
			return null;
			//ruptura=parse.size();
		}
		
	}

	/**20. LISTA_PARAM → CONSTANTE TIPO ID PASO RESTO_LISTA
	 * 					{ 	if (CONSTANTE.tipo != error_tipo) & (TIPO.tipo != error_tipo) & 
	 * 						   (PASO.tipo != error_tipo) & (RESTO_LISTA.tipo != error_tipo) 
	 * 						then LISTA_PARAM.tipo := vacio
	 * 						else LISTA_PARAM.tipo := error_tipo  } 
	 * 21. LISTA_PARAM → lambda
	 * 					{ LISTA_PARAM.tipo := vacio }
	 * @throws Exception 
	 */	private ExpresionTipo lista_param() throws Exception {
		
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
			parse.add(21);
			return new ExpresionTipo(TipoBasico.vacio);
		}else{
			parse.add(20);
			ExpresionTipo aux1,aux2,aux3,aux4;
			aux1 = constante();
			aux2 = tipo();
			//if(tipo()){
			if(aux2!=null){
				aux3 = paso();
				if(token.esIgual(TipoToken.IDENTIFICADOR)) {
					id();
					aux4 = restoLista();
					if(aux1.getTipoBasico()!=TipoBasico.error_tipo && aux2.getTipoBasico()!=TipoBasico.error_tipo && aux3.getTipoBasico()!=TipoBasico.error_tipo && aux4.getTipoBasico()!=TipoBasico.error_tipo)
						return new ExpresionTipo(TipoBasico.vacio);
					else
						return new ExpresionTipo(TipoBasico.error_tipo);
				}
				else{
					gestorErr.insertaErrorSintactico(linea, columna, "Falta identificador de lista de parametros");
					return null;
					//ruptura=parse.size();
				}
			}else{
				gestorErr.insertaErrorSintactico(linea, columna, "Falta tipo de lista de parametros");
				return null;
				//ruptura=parse.size();
			}
		}
	} 
	
	/**
	 * 259. CONSTANTE → const
	 * 				{ CONSTANTE.tipo := vacio }
	 * 260. CONSTANTE → lambda
	 * 				{ CONSTANTE.tipo := vacio }
	 */
	private ExpresionTipo constante() {
		if(token.esIgual(TipoToken.PAL_RESERVADA, 9 /* const */)){
			parse.add(259);
			nextToken();
		} else {
			parse.add(260);
		}
		return new ExpresionTipo(TipoBasico.vacio);
	}

	/** 123.PASO → & 
	 * 			{ PASO.tipo := vacio }
	 *	124.PASO → * 
	 * 			{ PASO.tipo := vacio }
	 *  125.PASO → lambda 
	 * 			{ PASO.tipo := vacio }
	 */
	private ExpresionTipo paso() {
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.BIT_AND)) {
			parse.add(123);
		/*	Object valor = token.getAtributo(); 
			System.out.println("Paso parametro: " + valor); */
			nextToken();
		}
		if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.MULTIPLICACION)) {
			parse.add(124);
		/*	Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("Paso parametro: " + valor); */
			nextToken();
		}
		else
			parse.add(125);
		return new ExpresionTipo(TipoBasico.vacio);
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
			return new ExpresionTipo(TipoBasico.vacio);
		}
	}


	/**	
	 * 24. DIMENSION → [ NUM_ENTERO ] DIMENSION
	 * 25. DIMENSION → lambda
	 * @throws Exception 
	 */
	private void dimension() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_CORCHETE)) {
			parse.add(24);
			nextToken();
			if(token.esIgual(TipoToken.NUM_ENTERO)){
				nextToken();
				if(token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_CORCHETE)){
					nextToken();
					dimension();
				}
				else{
					gestorErr.insertaErrorSintactico(linea, columna,"Falta separador \"]\"");
					//ruptura=parse.size();
				}
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna,"Se esperaba un numero entero");
				//ruptura=parse.size();
			}
		}
		else{
			parse.add(25);
		}
		
	}

	/**	
	 * 26. INIC_DIM → = INIC_DIM2
	 * 27. INIC_DIM → lambda
	 * @throws Exception 
	 */
	private void inicDim() throws Exception {
		if(token.esIgual(TipoToken.OP_ASIGNACION, OpAsignacion.ASIGNACION)){
			parse.add(26);
			nextToken();
			inicDim2();
		}
		else{
			parse.add(27);
		}
	}


	/**	28. INIC_DIM2 → { INIC_DIM3 }
	 * @throws Exception 
	 */
	private void inicDim2() throws Exception {
		if(!token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_LLAVE)){
			gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \"{\"");
			//ruptura=parse.size();
		}
		else{
			parse.add(28);
			nextToken();
			inicDim3();
			if(!token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)){
				gestorErr.insertaErrorSintactico(linea, columna, "Falta separador \"}\"");
				//ruptura=parse.size();
			}
			else{
				nextToken();
			}
		}
	}

	/**	29. INIC_DIM3 → LITERAL INIC_DIM4 
		30. INIC_DIM3 → INIC_DIM2 INIC_DIM5
	 * @throws Exception 
	 */
	private void inicDim3() throws Exception {
		if(!token.esIgual(TipoToken.EOF)){
			if(literal()){ // El metodo literal() lee el siguiente token
				parse.add(29);
				inicDim4();
			}
			else{
				parse.add(30);
				inicDim2();
				inicDim5();
			}
		}
		else {
			gestorErr.insertaErrorSintactico(linea, columna,"Fin de fichero inesperado");
			//ruptura=parse.size();
		}
	}

	/**	31. INIC_DIM4 → , LITERAL INIC_DIM4 
		32. INIC_DIM4 → lambda
	 * @throws Exception 
	 */
	private void inicDim4() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.COMA)){
			parse.add(31);
			nextToken();
			if(!literal()){
				gestorErr.insertaErrorSintactico(linea, columna,
						"Falta token literal");
				//ruptura=parse.size();
			}
			else{
				inicDim4();
			}
		}
		else{
			parse.add(32);
		}
		
	}

	/**	33. INIC_DIM5 → , INIC_DIM2 INIC_DIM5
		34. INIC_DIM5 → lambda 
	 * @throws Exception 
	 */
	private void inicDim5() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.COMA)){
			parse.add(33);
			nextToken();
			inicDim2();
			inicDim5();
		}
		else{
			parse.add(34);
		}
	}

	/**
	 * 35. INIC_CONST → , ID = LITERAL INIC_CONST
	 * 36. INIC_CONST → lambda
	 * @throws Exception 
	 */
	private void inic_const() throws Exception {
//		System.out.println("declaracion constante " + entradaTS.getLexema());
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(35);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				idConst(); // = LITERAL
				inic_const();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,
						"Falta un identificador");
				//ruptura=parse.size();
			}	
		}
		else { ///Si es lambda
			parse.add(36);
		}	
	}

	
	/**
	 * 37. DECLARACIONES → , ID INICIALIZACION DECLARACIONES
	 * 38. DECLARACIONES → lambda
	 * 			{ DECLARACIONES.tipo_s := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo declaraciones(ExpresionTipo tipo_h) throws Exception {
//		System.out.println("declaracion variable " + entradaTS.getLexema());
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(37);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				id();
				//inicializacion(); 
				inicializacion(tipo_h);
				//declaraciones();
				declaraciones(tipo_h);
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el identificador. Palabra o termino \""+token.atrString()+"\" inesperado.");		
				//ruptura=parse.size();
			}		
		} else {
			parse.add(38);
			return new ExpresionTipo(TipoBasico.vacio);
		}
		return null;
	}
	
	/**
	 * 39. INICIALIZACION → OP_ASIGNACION ASSIGNMENT_EXPRESSION
	 * 40. INICIALIZACION → [NUM_ENTERO] DIMENSION INIC_DIM
	 * 41. INICIALIZACION → lambda
	 * 						{ INICIALIZACION.tipo := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo inicializacion(ExpresionTipo tipo_h) throws Exception {
		if(token.esIgual(TipoToken.OP_ASIGNACION)) {
			parse.add(39);
			nextToken();
			assignment_expression();
			//expression();
			/*if(esLiteral()){
				nextToken();
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna,
						"Inicialización incorrecta, se espera un literal");
			}*/
			
		}
		//40. INICIALIZACION → [NUM_ENTERO] DIMENSION INIC_DIM
		else if(token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_CORCHETE)){
			parse.add(40);
			nextToken();
			if(token.esIgual(TipoToken.NUM_ENTERO )){
				nextToken();
				if(token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_CORCHETE)){
					nextToken();
					dimension();
					inicDim();
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
			return new ExpresionTipo(TipoBasico.vacio);
		}
		return null;
	}

	/**
	 * 42. INSTRUCCION → ID INSTRUCCION2 <-- SE PUEDE QUITAR!!
	 * 43. INSTRUCCION → struct RESTO_ST
	 * 44. INSTRUCCION → cin INS_LECT
	 * 45. INSTRUCCION → cout INS_ESC
	 * 46. INSTRUCCION → const INS_DEC
	 * 47. INSTRUCCION → TIPO INS_DEC2
	 * 					{ INS_DEC2.tipo := TIPO.tipo;
	 * 					  if (TIPO.tipo != error_tipo) & INS_DEC2.tipo != error_tipo)
	 * 					  then INSTRUCCION.tipo := vacio
	 * 					  else INSTRUCCION.tipo := error_tipo }
	 * 48. INSTRUCCION → ;
	 * 133.INSTRUCCION → EXPRESSION_OPT ;
	 * 					{ INSTRUCCION.tipo := vacio }
	 * @throws Exception 
	 */
	
	private ExpresionTipo instruccion() throws Exception {
		ExpresionTipo aux1,aux2;
		/*if(token.esIgual(TipoToken.IDENTIFICADOR)) { 
			parse.add(42);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
			return instruccion2();
			}
		else */
		if(token.esIgual(TipoToken.PAL_RESERVADA, 54 /*struct*/ )){ //INS_REG
			parse.add(43);
			nextToken();
			//ins_reg();
			resto_st();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA, 74 /*cin*/ )){ //INS_LECT
			parse.add(44);
			nextToken();
			ins_lect();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA, 75 /*cout*/ )){ //INS_ESC
			parse.add(45);
			nextToken();
			ins_esc(); 
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA, 9 /*const*/ )){ //INS_DEC
			parse.add(46); 
			nextToken();
			ins_dec();
		}
		//else if (tipo()) { //INS_DEC2
		else{
			aux1 = tipo();
			if(aux1!=null){
				//System.out.println("Si es tipo...");
				/*if(punt())
					nextToken();*/
				if(token.esIgual(TipoToken.IDENTIFICADOR)){
					//System.out.println("Si es identificador...");
					parse.add(47); 
					aux2 = ins_dec2(aux1);
					//System.out.println("aux1 :"+aux1.getTipoBasico().toString());
					//System.out.println("aux2 :"+aux2.getTipoBasico().toString());
					if(aux1.getTipoBasico() != TipoBasico.error_tipo && aux2.getTipoBasico() != TipoBasico.error_tipo)
						return new ExpresionTipo(TipoBasico.vacio);
					else
						return new ExpresionTipo(TipoBasico.error_tipo);
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
			} else{
				parse.add(133);
				aux1 = expressionOpt();
				if(token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
					nextToken();
					return aux1;
				}else{
					//error
					//gestorErr.insertaErrorSintactico(linea, columna,
					//"Falta separador \";\"");
					//ruptura=parse.size();
					
					//return false;
					return null;
				}
			}
		}
		/*
		
		else {
			gestorErr.insertaErrorSintactico(linea, columna,
			"Falta separador \";\"");
			return false;
		}
		*/
		
		//return true;
		return null;
	}
	
	
	/**
	 * 51. INS_DEC → TIPO PUNT ID OpAsignacion LITERAL INIC_CONST ;
	 * @throws Exception 
	 */
	private void ins_dec() throws Exception {
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
						inic_const(); //no lo tengo muy claro
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
		
	}
	
	
	/**
	 * 49.PUNT → *
	 * 50.PUNT → lambda
	 */
	private boolean punt() {
		if (token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.MULTIPLICACION)) {
			parse.add(49);
//			nextToken();
			return true;
			//return new ExpresionTipo(TipoBasico.vacio);
		} else {
			parse.add(50);
			return false;
			//return null;
		}
	}
	/**
	 * 52. INS_DEC2 → PUNT ID MAS_COSAS
	 * 				{ if(PUNT.tipo != vacio) 
	 * 				  then MAS_COSAS.tipo_h := puntero(INS_DEC2.tipo_h)
	 * 				  else MAS_COSAS.tipo_h := INS_DEC2.tipo_h;
	 * 				  INS_DEC2.tipo_s := MAS_COSAS.tipo_s }
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
				return new ExpresionTipo(TipoBasico.vacio);
			else
				return new ExpresionTipo(TipoBasico.error_tipo);*/
			
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
	 * 						  if (TIPO.tipo != error_tipo) & INS_DEC2.tipo != error_tipo)
	 * 						  then INSTRUCCION.tipo := vacio
	 * 						  else INSTRUCCION.tipo := error_tipo }
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
				return new ExpresionTipo(TipoBasico.vacio);
			else
				return new ExpresionTipo(TipoBasico.error_tipo);
		}else{
			gestorErr.insertaErrorSintactico(linea, columna,"Palabra o termino \""+token.atrString()+"\" inesperado.");//+lexico.getLexema()+"\" inesperado.");
			return null;
		}
	}
	
	/**
	 * 60. RESTO_ST → ID { CUERPO_ST } ID NOMBRES
	 * 61. RESTO_ST → { CUERPO_ST } ID NOMBRES
	 * @throws Exception 
	 */
	private void resto_st() throws Exception {
		if(token.esIgual(TipoToken.IDENTIFICADOR)){ 
			parse.add(60);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)){
				nextToken();
				cuerpo_st();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)){
					nextToken();
					if(token.esIgual(TipoToken.IDENTIFICADOR)){
						tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
						nextToken();
						nombres();
					} else {
						// error
						gestorErr.insertaErrorSintactico(linea, columna, "Falta la identificacion de la estructura");
						//ruptura=parse.size();
					}
				} else{
					// error
					gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador cierra llave }");
					//ruptura=parse.size();
				}
			}
			else{
				// error
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador abre llave {");
				//ruptura=parse.size();
			}
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)){
			parse.add(61);
			nextToken();
			cuerpo_st();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)){
				nextToken();
				if(token.esIgual(TipoToken.IDENTIFICADOR)){
					tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
					nextToken();
					nombres();
				} else {
					// error
					gestorErr.insertaErrorSintactico(linea, columna, "Falta la identificacion de la estructura");
					//ruptura=parse.size();
				}
			} else {
				// error
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador cierra llave }");
				//ruptura=parse.size();
			}
		} else{
			// error
			gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador abre llave { o identificacion de la estructura");
			//ruptura=parse.size();
		}
	}
	
	
	/**
	 * 62. CUERPO_ST → TIPO ID RESTO_VAR CUERPO_ST
	 * 63. CUERPO_ST → lambda
	 * @throws Exception 
	 */
	private void cuerpo_st() throws Exception {
		//if(tipo()) {
		if(tipo()!=null){
			parse.add(62);
			if (token.esIgual(TipoToken.IDENTIFICADOR)) {
				tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
				nextToken();
				resto_var();
				cuerpo_st();
			}
			else {
				//error
				gestorErr.insertaErrorSintactico(linea, columna, "Los atributos deben estar identificados");
				//ruptura=parse.size();
			}
		} else
			parse.add(63);
		
	}
	
	
	/**
	 * 64. RESTO_VAR → , ID RESTO_VAR
	 * 65. RESTO_VAR → ;
	 * @throws Exception 
	 */
	private void resto_var() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)){ 
			parse.add(64);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
				nextToken();
				resto_var();
			} else {
				// error
				gestorErr.insertaErrorSintactico(linea, columna, 
						"Se deben identificar todos los atributos");
				//ruptura=parse.size();
			}
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
			parse.add(65);
			nextToken();
		} else {
			// error
			gestorErr.insertaErrorSintactico(linea, columna, 
					"Se esperaba un separador coma (,) o punto_coma (;)");
			//ruptura=parse.size();
		}
	}
	
	
	/**
	 * 66. NOMBRES → , ID NOMBRES
	 * 67. NOMBRES → ;
	 * @throws Exception 
	 */
	private void nombres() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)){ 
			parse.add(66);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
				nextToken();
				nombres();
			} else {
				// error
				gestorErr.insertaErrorSintactico(linea, columna,
						"Se deben identificar todas las variables de la estructura");
				//ruptura=parse.size();
			}
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
			parse.add(67);
			nextToken();
		} else {
			// error
			gestorErr.insertaErrorSintactico(linea, columna, 
					"Se esperaba un separador coma (,) o punto_coma (;)");
			//ruptura=parse.size();
		}
	}
	
		
	/**
	 * 68. INS_LECT → >>  RESTO_LECT 
	 * @throws Exception 
	 */
	private void ins_lect() throws Exception { 
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MAYORES)){
			parse.add(68);
			nextToken();
			resto_lect();
		}
		else{
			// error
			gestorErr.insertaErrorSintactico(linea, columna,
					"Lectura incorrecta, se esperaba el operador \">>\"");
			//ruptura=parse.size();
		}
	}
	
	/**
	 * 69. RESTO_LECT → ID ;
	 * 70. RESTO_LECT → LITERAL  ;
	 * @throws Exception 
	 */
	private void resto_lect() throws Exception {
		if(token.esIgual(TipoToken.IDENTIFICADOR)){ //FALTA ALGO MAS AQUI???
			parse.add(69);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
				nextToken();
			}
			else{
				// error
				gestorErr.insertaErrorSintactico(linea, columna,
						"Lectura terminada incorrectamente, falta ';'");
				//ruptura=parse.size();
			}
		}
		else if(esLiteral()){
			parse.add(70);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
				nextToken();	
			}
			else{
				// error
				gestorErr.insertaErrorSintactico(linea, columna,
						"Lectura terminada incorrectamente, falta ';'");
				//ruptura=parse.size();
			}
		}
		else{
			// error
			gestorErr.insertaErrorSintactico(linea, columna,
					"Lectura incorrecta, se esperaba un literal o una variable");
			//ruptura=parse.size();
		}
	}
	
	/**
	 * 71. INS_ESC → << RESTO_ESC
	 * @throws Exception 
	 */
	private void ins_esc() throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MENORES)){
			parse.add(63);
			nextToken();
			resto_esc();
		}
		else{
			// error
			gestorErr.insertaErrorSintactico(linea, columna,
					"Escritura incorrecta, se esperaba el operador \"<<\"");
			//ruptura=parse.size();
		}
	}
	
	/**
	 * 72. RESTO_ESC →  LITERAL INS_ESC2
	 * 73. RESTO_ESC →  ID INS_ESC2
	 * 74. RESTO_ESC →  endl INS_ESC2
	 * @throws Exception 
	 */
	private void resto_esc() throws Exception {
		if(esLiteral()){
			parse.add(72);
			nextToken();
			ins_esc2();
		}
		else if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(73);
			nextToken();
			ins_esc2();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,76 /*endl*/)){ 
			parse.add(74);
			nextToken();
			ins_esc2();
		}
		else{
			// error
			gestorErr.insertaErrorSintactico(linea, columna,
					"Escritura incorrecta, se esperaba un literal, una variable o la palabra reservada \"endl\"");
			//ruptura=parse.size();
		}
	}
	
	/**
	 * 75. INS_ESC2 →  << RESTO_ESC2
	 * 76. INS_ESC2 →  ;
	 * @throws Exception 
	 */
	private void ins_esc2() throws Exception{
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MENORES)){
			parse.add(75);
			nextToken();
			resto_esc2();
		}
		else if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
			parse.add(76);
			nextToken();
		}
		else{
			// error
			gestorErr.insertaErrorSintactico(linea, columna,
					"Escritura terminada incorrectamente, falta ';'");
			//ruptura=parse.size();
		}
	}
	
	
	/**
	 * 77. RESTO_ESC2 → LITERAL  INS_ESC2 
	 * 78. RESTO_ESC2 → ID  INS_ESC2  
	 * 79. RESTO_ESC2 → endl INS_ESC2 
	 * 80. RESTO_ESC2 → lamdba
	 * @throws Exception 
	 */
	private void resto_esc2() throws Exception {
		if(esLiteral()){
			parse.add(77);
			nextToken();
			ins_esc2();
		}
		else if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(78);
			nextToken();
			ins_esc2();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,76 /*endl*/)){ 
			parse.add(79);
			nextToken();
			ins_esc2();
		}
		else{
			// Lambda...
			parse.add(80);
		}
	}
	

	
	/**
	 * 81. CUERPO --> for RESTO_FOR CUERPO
	 * 82. CUERPO --> do RESTO_DO CUERPO
	 * 83. CUERPO --> while RESTO_WHILE CUERPO
	 * 84. CUERPO --> if RESTO_IF CUERPO
	 * 85. CUERPO --> switch RESTO_CASE CUERPO
	 * 86. CUERPO --> lambda
	 * 				{ CUERPO.tipo := vacio }
	 * 104. CUERPO → { CUERPO } CUERPO
	 * 128. CUERPO → break ; CUERPO
	 * 129. CUERPO → continue ; CUERPO
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
			parse.add(81);
			nextToken();
			resto_for();
			cuerpo();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,19 /*do*/))
		{
			parse.add(82);
			nextToken();
			resto_do();
			cuerpo();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,72 /*while*/))
		{
			parse.add(83);
			nextToken();
			resto_while();
			cuerpo();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,32 /*if*/))
		{
			parse.add(84);
			nextToken();
			resto_if();
			cuerpo();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,55 /*switch*/))
		{
			parse.add(85);
			nextToken();
			resto_case();
			cuerpo();
//		}
//		else if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
//			parse.add(86);
//			nextToken();
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			parse.add(104);
			nextToken();
			cuerpo();
//			cuerpo();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
				nextToken();
				cuerpo();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \"}\"");
				//ruptura=parse.size();
			}
		} else if(token.esIgual(TipoToken.PAL_RESERVADA,5 /*break*/)) {
			parse.add(128);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				nextToken();
				cuerpo();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
				//ruptura=parse.size();
			}
		} else if(token.esIgual(TipoToken.PAL_RESERVADA, 12 /*continue*/)) {
			parse.add(129);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				nextToken();
				cuerpo();
			} else { 
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
				//ruptura=parse.size();
			}
		} else if(token.esIgual(TipoToken.PAL_RESERVADA, 47 /*return*/)) {
			parse.add(130);
			nextToken();
			expressionOpt();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				nextToken();
				cuerpo();
			} else { 
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
				//ruptura=parse.size();
			}
		}  else if(token.esIgual(TipoToken.PAL_RESERVADA, 31 /*goto*/)) {
			parse.add(128);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				nextToken();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
					nextToken();
					cuerpo();
				} else { 
					gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
					//ruptura=parse.size();
				}
			} else { 
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el identificador");
				//ruptura=parse.size();
			}
		} else {
			parse.add(132);
			ExpresionTipo aux1, aux2;
			aux1 = instruccion();
			//if(instruccion())
			if(aux1!=null){
				//System.out.println("Llamada recursiva a cuerpo...");
				aux2 = cuerpo();
				if(aux1.getTipoBasico() != TipoBasico.error_tipo && aux2.getTipoBasico() != TipoBasico.error_tipo)
					return new ExpresionTipo(TipoBasico.vacio);
				else
					return new ExpresionTipo(TipoBasico.error_tipo);
			}else{
				 if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE) 
						 || token.esIgual(TipoToken.PAL_RESERVADA,6 /*case*/)
						 || token.esIgual(TipoToken.PAL_RESERVADA,17 /*default*/)
						 || token.esIgual(TipoToken.EOF)) {
					parse.add(86); //lambda
					return new ExpresionTipo(TipoBasico.vacio);
				 } else {
					gestorErr.insertaErrorSintactico(linea, columna, "Token inesperado.");
					//ruptura=parse.size();
				 }
			}
		}	
		return null;
	}

	/** 
	 * 87. CUERPO2 --> while RESTO_WHILE
	 * 88. CUERPO2 --> for RESTO_FOR
	 * 89. CUERPO2 --> do RESTO_DO
	 * 90. CUERPO2 --> if RESTO_IF 
	 * 91. CUERPO2 --> switch RESTO_CASE
	 * 92. CUERPO2 --> { CUERPO }
	 * 93. CUERPO2 --> INSTRUCCION
	 * @throws Exception 
	 */
	private void cuerpo2() throws Exception {
		
		if(token.esIgual(TipoToken.PAL_RESERVADA,72 /*while*/))
		{
			parse.add(87);
			nextToken();
			resto_while();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,29 /*for*/))
		{
			parse.add(88);
			nextToken();
			resto_for();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,19 /*do*/))
		{
			parse.add(89);
			nextToken();
			resto_do();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,32 /*if*/))
		{
			parse.add(90);
			nextToken();
			resto_if();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,55 /*switch*/))
		{
			parse.add(91);
			nextToken();
			resto_case();
		}
		else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			nextToken();
			parse.add(92);
			cuerpo();
			if (!token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)) {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta }");
			}
			nextToken();
		}
		else {
			parse.add(93);
			instruccion();
		}
		/*else 	
			gestorErr.insertaErrorSintactico(linea, columna,
				"Token inesperado... "); */
		
	}
	
	
	/**
	 *  94. RESTO_WHILE --> (EXPRESSION) do CUERPO2
	 * @throws Exception 
	 */
	private void resto_while() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(94);
			nextToken();
			expression();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
				cuerpo2();
			} else{
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \")\"");
				//ruptura=parse.size();
			}
		} else{
			gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \"(\"");
			//ruptura=parse.size();
		}
	}		

	/**
	 * 95. RESTO_DO --> CUERPO2 while (EXPRESSION);
	 * @throws Exception 
	 */
	private void resto_do() throws Exception {
		parse.add(95); 
		cuerpo2();
		if(token.esIgual(TipoToken.PAL_RESERVADA,72 /*while*/))	{
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
				nextToken();
				expression();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
					nextToken();
					if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
						nextToken();
					}	
					else{
						gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
						//ruptura=parse.size();
					}
				}
				else{
					gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \")\"");
					//ruptura=parse.size();
				}
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
				//ruptura=parse.size();
			}
		}
		else{
			gestorErr.insertaErrorSintactico(linea, columna,"Falta palabra \"while\"");
			//ruptura=parse.size();
		}
	}	
		
	/**
	 * 96. RESTO_FOR → ( FOR-INIT ; EXPRESSIONOPT ; EXPRESSIONOPT ) CUERPO2 
	 * @throws Exception 
	 */
	private void resto_for() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(96);
			nextToken();
			for_init();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				nextToken();
				expressionOpt();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
					nextToken();
					expressionOpt();
					if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
						nextToken();
						cuerpo2();
					} else{
						gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \")\"");
						//ruptura=parse.size();
					}
				} else{
					gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
					//ruptura=parse.size();
				}
			} else{
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \";\"");
				//ruptura=parse.size();
			}
		} else{
			gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \"(\"");
		}
	}

	/**
	 * 135. FOR-INIT → id INICIALIZACION
	 * 136. FOR-INIT → TIPO id INICIALIZACION
	 * 137. FOR-INIT → EXPRESSIONOPT
	 * @throws Exception 
	 */
	private void for_init() throws Exception {
		if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(135);
			nextToken();
			//inicializacion();
			inicializacion(null);
		//} else if(tipo()) {
		} else if(tipo()!=null) {
			parse.add(136);
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				nextToken();
				//inicializacion();
				inicializacion(null);
			}
		} else {
			parse.add(137);
			expressionOpt();
		}
	}

	/** 
	 * 97. RESTO_CASE --> ( EXPRESSION ) RESTO_CASE2
	 * @throws Exception 
	 */
	private boolean resto_case() throws Exception {
		
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(97);
			nextToken();
			expression(); 
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
				resto_case2();
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna, "Falta ')'");
				//ruptura=parse.size();
			}
		}
		else{
			gestorErr.insertaErrorSintactico(linea, columna, "Falta '('");
			//ruptura=parse.size();
		}
		
	
	return false;
	}
	
	/** 
	 * TODO nuevos numeros de regla!
	 * 97aaaa. RESTO_CASE2 --> { CUERPO_CASE }
	 * 97bbbb. RESTO_CASE2 --> ; 
	 * @throws Exception 
	 */
	private boolean resto_case2() throws Exception {
		numDefaults = 0;
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
			parse.add(97); // TODO cambiar numero de regla
			return true;
		}
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			parse.add(97); // TODO cambiar numero de regla
			nextToken();
			cuerpo_case();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
				nextToken();
				return true;
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
		
	return false;
	}

	/** TODO deberia funcionar con cuerpo si este no tuviese la regla cuerpo -> }
	 * 98. CUERPO_CASE --> case LITERAL : CUERPO CUERPO_CASE
	 * TODO 98aaa. CUERPO_CASE --> default : CUERPO CUERPO_CASE 
	 * @throws Exception 
	 */
	private void cuerpo_case() throws Exception {
		if(token.esIgual(TipoToken.PAL_RESERVADA,6 /*case*/)) {
			parse.add(98);
			nextToken();
			if(literal())
			{
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.DOS_PUNTOS)){
					nextToken();
					cuerpo();
					cuerpo_case();
				} else {
					gestorErr.insertaErrorSintactico(linea, columna, "Falta ':'");
				}
			}
			else{ 
				gestorErr.insertaErrorSintactico(linea, columna, "Falta literal");
				//ruptura=parse.size();
			}
		} else if(token.esIgual(TipoToken.PAL_RESERVADA,17 /*default*/)){
			if(numDefaults < 1) {
				parse.add(98); //TODO cambiar num regla
				numDefaults++;
				nextToken();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.DOS_PUNTOS)){
					nextToken();
					cuerpo();
					cuerpo_case();
				} else {
					gestorErr.insertaErrorSintactico(linea, columna, "Falta ':'");
				}
			} else {
				gestorErr.insertaErrorSintactico(linea, columna, "'default' aparece más de una vez.");
			}
		}
	}
	
	
	
	/**
	 *  99. RESTO_IF --> ( EXPRESSION ) CUERPO2 SENT_ELSE
	 * @throws Exception 
	 */
	private boolean resto_if() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(99);
			nextToken();
			expression();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
				cuerpo2();
				sent_else();
				return true;
			} else{
				gestorErr.insertaErrorSintactico(linea, columna, "Falta el separador \")\"");
				//ruptura=parse.size();
			}
		} else{
			gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
			//ruptura=parse.size();
		}
		return false;
	}
	
	
	/**
	 * 100. SENT_ELSE --> else CUERPO2 
	 * 101. SENT_ELSE --> lambda
	 * @throws Exception 
	 */
	private void sent_else() throws Exception {
		if(token.esIgual(TipoToken.PAL_RESERVADA,22 /*else*/)) {
			parse.add(100);
			nextToken();
			cuerpo2();
		}
		else //SENT_ELSE -> lambda
			parse.add(101);
	}
	
	/**
	 * 144. PRIMARY-EXPRESSION → LITERAL
	 * 145. PRIMARY-EXPRESSION → this
	 * 146. PRIMARY-EXPRESSION → UNQUALIFIED-ID
	 * 147. PRIMARY-EXPRESSION → ( EXPRESSION )
	 * @throws Exception 
	 */
	private boolean primary_expression() throws Exception {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(147);
			nextToken();
			expression();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
				nextToken();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \")\"");
				//ruptura=parse.size();
				return false;
			}
		} else if (token.esIgual(TipoToken.PAL_RESERVADA, 57)) {
			parse.add(145);
			nextToken();
		} else if(literal()) {
			parse.add(144);
			//nextToken();
		} else {
			parse.add(146);
			unqualified_id();
		}
		return true;
	}
	
	
	/**
	 * 148. UNQUALIFIED-ID → id
	 * 149. UNQUALIFIED-ID →  ~ RESTO_UNQ
	 * @throws Exception 
	 */
	private void unqualified_id() throws Exception {
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(148);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
		} else if (token.esIgual(TipoToken.OP_LOGICO,OpLogico.SOBRERO)) {
			parse.add(149);
			nextToken();
			resto_unq();
		} else {
			//gestorErr.insertaErrorSintactico(linea, columna,"Parte derecha de la asignacion incompleta");
			gestorErr.insertaErrorSintactico(linea, columna,"Expresion incompleta.");
			//ruptura=parse.size();
		}
	}
	
	
	/**
	 * 150. RESTO_UNQ → id
	 * 151. RESTO_UNQ → decltype ( EXPRESSION )
	 * @throws Exception 
	 */
	private void resto_unq() throws Exception {
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(150);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
		} else if (token.esIgual(TipoToken.PAL_RESERVADA, 16)) {
			parse.add(151);
			nextToken();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
				nextToken();
				expression();
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
			gestorErr.insertaErrorSintactico(linea, columna,"Falta identificador o decltype");
			//ruptura=parse.size();
		}
	}
	
	/**
	 * 152. POSTFIX-EXPRESSION → typeid ( EXPRESSION ) RESTO_POSTFIX_EXP
	 * 153. POSTFIX-EXPRESSION → ID RESTO_PE RESTO_POSTFIX_EXP
	 * 154. POSTFIX-EXPRESSION → PRIMARY-EXPRESSION RESTO_POSTFIX_EXP
	 * 168. POSTFIX-EXPRESSION → TIPO_SIMPLE POSTFIX-4 POSTFIX-2 RESTO_POSTFIX_EXP
	 * 170. POSTFIX-EXPRESSION →  ~ POSTFIX-EXPRESSION
	 * @throws Exception 
	 */
	private void postfix_expression() throws Exception {
		if (token.esIgual(TipoToken.PAL_RESERVADA, 63)) {
			parse.add(152);
			nextToken();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
				nextToken();
				expression();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
					nextToken();
					resto_postfix_exp();
				} else {
					gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \")\"");
					//ruptura=parse.size();
				}
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
				//ruptura=parse.size();
			}
		} else if (token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(153);
			nextToken();
			resto_pe();
			resto_postfix_exp();
		} else if  (token.esIgual(TipoToken.OP_LOGICO,OpLogico.SOBRERO)) {
			parse.add(170);
			nextToken();
			postfix_expression();
		} else if(tipo_simple()) {
			parse.add(168);
			//nextToken();
			postfix4();
			postfix2();
			resto_postfix_exp();
		} else {
			parse.add(154);
			primary_expression();
			if(tokenAnterior.esIgual(TipoToken.IDENTIFICADOR))
				resto_postfix_exp();
		}
	}
	
	/**
	 * 164. RESTO_PE → ( POSTFIX-3
	 * 165. RESTO_PE → lambda
	 * @throws Exception 
	 */
	private void resto_pe() throws Exception {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(164);
			nextToken();
			postfix3();
		} else {
			parse.add(165);
		}	
	}
	
	/**
	 * 155. RESTO_POSTFIX_EXP → [ EXPRESSION ]
	 * 156. RESTO_POSTFIX_EXP → -> RESTO_POSTFIX_EXP3
	 * 157. RESTO_POSTFIX_EXP → . RESTO_POSTFIX_EXP3
	 * 158. RESTO_POSTFIX_EXP → decremento
	 * 159. RESTO_POSTFIX_EXP → incremento
	 * 160. RESTO_POSTFIX_EXP → ( RESTO_POSTFIX_EXP2
	 * 161. RESTO_POSTFIX_EXP → lambda
	 * @throws Exception 
	 */
	private void resto_postfix_exp() throws Exception {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_CORCHETE)) {
			parse.add(155);
			nextToken();
			expression();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_CORCHETE)) {
				nextToken();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"]\"");
				//ruptura=parse.size();
			}
		} else if (token.esIgual(TipoToken.OP_ASIGNACION, OpAsignacion.PUNTERO)) {
			parse.add(156);
			nextToken();
			resto_postfix_exp3();
		} else if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO)) {
			parse.add(157);
			nextToken();
			resto_postfix_exp3();
		} else if (token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.DECREMENTO)) {
			parse.add(158);
			nextToken();
		} else if (token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.INCREMENTO)) {
			parse.add(159);
			nextToken();
		} else if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(160);
			nextToken();
			resto_postfix_exp2();
		} else {
			parse.add(161);
		}
	}
	
	
	/**
	 * 162. RESTO_POSTFIX_EXP2 → )
	 * 163. RESTO_POSTFIX_EXP2 → INITIALIZER-LIST )
	 * @throws Exception 
	 */
	private void resto_postfix_exp2() throws Exception {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
			parse.add(162);
			nextToken();
		} else {
			parse.add(163);
			initializer_list();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
				nextToken();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \")\"");
				//ruptura=parse.size();
			}
		}
	}
	
	
	/**
	 * 166. RESTO_POSTFIX_EXP3 → ~ decltype ( EXPRESSION )
	 * 167. RESTO_POSTFIX_EXP3 → UNQUALIFIED-ID
	 * @throws Exception 
	 */
	private void resto_postfix_exp3() throws Exception {
		if (token.esIgual(TipoToken.OP_LOGICO,OpLogico.SOBRERO)) {	
			parse.add(166);
			nextToken();
			if (token.esIgual(TipoToken.PAL_RESERVADA, 16)) {
				nextToken();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
					nextToken();
					expression();
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
		}
	}
	
	
	/**
	 * 171. POSTFIX4 → (
	 * 172. POSTFIX4 → lambda
	 */
	private void postfix4() {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(171);
			nextToken();
		} else {
			parse.add(172);
		}
	}
	
	
	/**
	 * 173. POSTFIX-2 → ( POSTFIX-3
	 * @throws Exception 
	 */
	private void postfix2() throws Exception {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(173);
			nextToken();
			postfix3();
		} else {
			gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \"(\"");
			//ruptura=parse.size();
		}
	}
	
	
	/**
	 * 174. POSTFIX-3 →  )
	 * 175. POSTFIX-3 → INITIALIZER-LIST )
	 * @throws Exception 
	 */
	private void postfix3() throws Exception {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
			parse.add(174);
			nextToken();
		} else {
			parse.add(175);
			initializer_list();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
				nextToken();
			} else {
				gestorErr.insertaErrorSintactico(linea, columna,"Falta el separador \")\"");
				//ruptura=parse.size();
			}
		}
	}
	
	
		
	/**
	 * 176. INITIALIZER-LIST → ASSIGNMENT-EXPRESSION RESTO_INIT 
	 * @throws Exception 
	 */
	private void initializer_list() throws Exception{
		parse.add(176);
		assignment_expression();
		resto_init();
	}
	
	/**
	 * 177. RESTO_INIT → , INITIALIZER_LIST
	 * 178. RESTO_INIT → lambda
	 * @throws Exception 
	 */
	private void resto_init() throws Exception{
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)){
			parse.add(177);
			nextToken();
			initializer_list();
		}
		else {
			parse.add(178);
		}
	}
			
	/**
	 * 179. UNARY_EXPRESSION → incremento CAST_EXPRESSION
	 * 180. UNARY_EXPRESSION → decremento CAST_EXPRESSION
	 * 181. UNARY_EXPRESSION → UNARY-OPERATOR CAST_EXPRESSION
	 * 182. UNARY_EXPRESSION → sizeof RESTO_UNARY
	 * 183. UNARY_EXPRESSION → alignof (type-id)
	 * 184. UNARY_EXPRESSION → noexcept NOEXCEPT_EXPRESSION
	 * 142. UNARY_EXPRESSION → new TIPO ( RESTO_NEW
	 * 143. UNARY_EXPRESSION → delete RESTO_DELETE
	 * 185. UNARY_EXPRESSION → POSTFIX_EXPRESSION
	 * @throws Exception 
	 */
	private void unary_expression() throws Exception{
		if(token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.INCREMENTO)){
			parse.add(179);
			nextToken();
			cast_expression();
		}
		else if(token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.DECREMENTO)){
			parse.add(180);
			nextToken();
			cast_expression();
		}
		else if(unary_operator()){
			parse.add(181);
			//nextToken();
			cast_expression();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA)
				&& (Integer)token.getAtributo()==50 /*sizeof*/ ){
			parse.add(182);
			nextToken();
			resto_unary();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA)
				&& (Integer)token.getAtributo()==1 /*alignof*/ ){
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) {
				nextToken();
				//if(tipo()){
				if(tipo()!=null){
					if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
						parse.add(183);
						nextToken();
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
				// error
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba `(` ");
				//ruptura=parse.size();
			}
		} else if (token.esIgual(TipoToken.PAL_RESERVADA)
				&& (Integer)token.getAtributo()==39 /*noexcept*/ ){
			parse.add(184);
			nextToken();
			noexcept_expression();
		} else if(token.esIgual(TipoToken.PAL_RESERVADA)
				&& (Integer)token.getAtributo()==38 /*new*/ ){
			parse.add(142);
			nextToken();
			//if(tipo()){
			if(tipo()!=null){
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) {
					nextToken();
					resto_new();
				}else{
					//error
					gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba `)` ");
					//ruptura=parse.size();
				}
			}else{
				//error
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba un identificador o tipo pre-definido ");
				//ruptura=parse.size();
			}
		}else if(token.esIgual(TipoToken.PAL_RESERVADA)
				&& (Integer)token.getAtributo()==18 /*delete*/ ){
			parse.add(143);
			nextToken();
			resto_delete();
		}else{
			parse.add(185);
			postfix_expression();
		}
	}
	
	/**
	 * 186. RESTO_UNARY → ( TIPO )
	 * 187. RESTO_UNARY → UNARY-EXPRESSION
	 * @throws Exception 
	 */
	private void resto_unary() throws Exception{
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)){
			nextToken();
			//if(tipo()){
			if(tipo()!=null){
				if(token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)){
					parse.add(186);
					nextToken();
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
			unary_expression();
		}
	}
	
	/**
	 * 188. UNARY-OPERATOR → *
	 * 189. UNARY-OPERATOR → &
	 * 190. UNARY-OPERATOR → +
	 * 191. UNARY-OPERATOR → !
	 * 192. UNARY-OPERATOR → sombrero
	 * 138. UNARY-OPERATOR → -
	**/
	private boolean unary_operator(){
		if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.MULTIPLICACION)){
			parse.add(188);
			nextToken();
			return true;
		}else if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.BIT_AND)){
			parse.add(189);
			nextToken();
			return true;
		}else if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.SUMA)){
			parse.add(190);
			nextToken();
			return true;
		}else if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.NOT)){
			parse.add(191);
			nextToken();
			return true;
		}else if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.SOBRERO)){
			parse.add(192);
			nextToken();
			return true;
		}else if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.RESTA)){
			parse.add(138);
			nextToken();
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 251.RESTO_NEW → )
	 * 252.RESTO_NEW → EXPRESSION_LIST )
	 * @throws Exception 
	 */
	private void resto_new() throws Exception{
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
			parse.add(251);
			nextToken();
		}else{
			parse.add(252);
			expression_list();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
			}else{
				// error
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba ')' ");
				//ruptura=parse.size();
			}
				
		}
		
	}
	
	/**
	 * 253. EXPRESSION_LIST → EXPRESSION RESTO_LISTA_EXP
	 * @throws Exception 
	 */
	private void expression_list() throws Exception{
		parse.add(253);
		expression();
		resto_lista_exp();
	}
	
	/**
	 * 254. RESTO_LISTA_EXP → , EXPRESSION_LIST
	 * 255. RESTO_LISTA_EXP → lambda
	 * @throws Exception 
	 */
	private void resto_lista_exp() throws Exception{
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)){
			parse.add(254);
			nextToken();
			expression_list();
		}else{
			parse.add(255);
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
				//error
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba ']' ");
				//ruptura=parse.size();
			}			
		}else{
			//error
			gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba '[' ");
			//ruptura=parse.size();
		}
	}
	
	
	/**
	 * 193. NOEXCEPT-EXPRESSION → ( EXPRESSION ) 
	 * @throws Exception 
	 */
	private void noexcept_expression() throws Exception{
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			nextToken();
			expression();
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
	}
	
	/**
	 * 194. CAST-EXPRESSION → UNARY-EXPRESSION
	 * 195. CAST-EXPRESSION → ( RESTO_CAST
	 * @throws Exception 
	 */
	private void cast_expression() throws Exception{
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(195);
			nextToken();
			resto_cast();
		} else {
			parse.add(194);
			unary_expression();
		}
	}
	
	/**
	 * 139. RESTO_CAST → TIPO_SIMPLE ) CAST_EXPRESSION
	 * 140. RESTO_CAST → EXPRESSION )
	 * @throws Exception 
	 */
	private void resto_cast() throws Exception{
		if(tipo_simple()){
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
				parse.add(139);
				cast_expression();
			}else{
				// error
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba ')' ");
				//ruptura=parse.size();
			}
		}else{
			parse.add(140);
			expression();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
			} else{
				// error
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba ')' ");
				//ruptura=parse.size();
			}
		}
	}
	
	
	/**
	 * 196. PM-EXPRESSION → CAST-EXPRESSION
	 * 197. PM-EXPRESSION → .* CAST-EXPRESSION
	 * 198. PM-EXPRESSION → -> CAST-EXPRESSION
	 * @throws Exception 
	 */
	private void pm_expression() throws Exception{
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO)){
			nextToken();
			if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.MULTIPLICACION)){
				nextToken();
				parse.add(197);
				cast_expression();
			}else{
				// error
				gestorErr.insertaErrorSintactico(linea, columna, "Se esperaba `*` ");
				//ruptura=parse.size();
			}
		} else if(token.esIgual(TipoToken.OP_ASIGNACION,OpAsignacion.PUNTERO)){
			parse.add(198);
			nextToken();
			cast_expression();
		} else{
			parse.add(196);
			cast_expression();
		}
	}
	
	
	/**
	 * 199. MULTIPLICATIVE-EXPRESSION → PM-EXPRESSION RESTO-MULT
	 * @throws Exception 
	 */
	private void multiplicative_expression() throws Exception{
		parse.add(199);
		pm_expression();
		resto_mult();
	}
	
	/**
	 * 200. RESTO-MULT → * MULTIPLICATIVE-EXPRESSION
	 * 201. RESTO-MULT → / MULTIPLICATIVE-EXPRESSION
	 * 202. RESTO-MULT → % MULTIPLICATIVE-EXPRESSION
	 * 203. RESTO-MULT → lambda
	 * @throws Exception 
	 */
	private void resto_mult() throws Exception{
		if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.MULTIPLICACION)){
			nextToken();
			parse.add(200);
			multiplicative_expression();
		}else if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.DIVISION)){
			nextToken();
			parse.add(201);
			multiplicative_expression();
		}else if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.PORCENTAJE)){
			nextToken();
			parse.add(202);
			multiplicative_expression();
		} else{
			parse.add(203);
			//lambda
		}
	}
	
	/**
	 * 204. ADDITIVE_EXPRESSION  → MULTIPLICATIVE-EXPRESSION  RESTO _ADD
	 * @throws Exception 
	 */
	private void additive_expression() throws Exception{
		parse.add(204);
		multiplicative_expression();
		resto_add();
	}
	
	/**
	 * 205. RESTO_ADD → + ADDITIVE_EXPRESSION
	 * 206. RESTO_ADD → - ADDITIVE_EXPRESSION
	 * 207. RESTO_ADD → lambda
	 * @throws Exception 
	 */
	private void resto_add() throws Exception{
		if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.SUMA)){
			parse.add(205);
			nextToken();
			additive_expression();
		}
		else if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.RESTA)){
			parse.add(206);
			nextToken();
			additive_expression();
		} else{
			parse.add(207);
			// lambda
		}
	}
	
	
	/**
	 * 208. SHIFT-EXPRESSION → ADDITIVE_EXPRESSION RESTO_SHIFT
	 * @throws Exception 
	 * */
	private void shift_expression() throws Exception{
		parse.add(208);
		additive_expression(); //Debe leer el siguiente token
		resto_shift();
	}

	/** 209. RESTO_SHIFT →  <<  SHIFT-EXPRESSION
	 * 210. RESTO_SHIFT →  >> SHIFT-EXPRESSION
	 * 211. RESTO_SHIFT → lambda
	 * @throws Exception 
	 * */
	
	private void resto_shift() throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.DOS_MENORES)){
			parse.add(209);
			nextToken();
			shift_expression();
		}
		else if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.DOS_MAYORES)){
			parse.add(210);
			nextToken();
			shift_expression();
		}
		else{
			parse.add(211);
		}
	}
	
	/**	212. RELATIONAL-EXPRESSION → SHIFT-EXPRESSION RESTO-RELATIONAL
	 * @throws Exception */
	
	private void relational_expression() throws Exception{
		parse.add(212);
		shift_expression(); 
		resto_relational();
	}

	/**	213. RESTO-RELATIONAL → < RESTO2-RELATIONAL
		214. RESTO-RELATIONAL → > RESTO2-RELATIONAL
		216. RESTO-RELATIONAL → >= RESTO2-RELATIONAL //TODO cambiar las reglas en la memoria
		217. RESTO-RELATIONAL → <= RESTO2-RELATIONAL //TODO cambiar las reglas en la memoria
		215. RESTO-RELATIONAL → lambda
	 * @throws Exception 
	 */
	private void resto_relational() throws Exception {
		if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MENOR)){
			parse.add(213);
			nextToken();
			shift_expression();
		}
		else if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MAYOR)){
			parse.add(214);
			nextToken();
			shift_expression();
		}
		else if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MAYOR_IGUAL)){
			parse.add(216);
			nextToken();
			shift_expression();
		}
		else if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MENOR_IGUAL)){
			parse.add(217);
			nextToken();
			shift_expression();
		}
		else{
			parse.add(215);
		}
		
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
	 * @throws Exception */
	
	private void equality_expression() throws Exception{
		parse.add(218);
		relational_expression(); //Debe leer el siguiente token
		resto_equality();
	}

	
	/**	219. RESTO-EQUALITY → igualdad EQUALITY-EXPRESSION
	 * 	220. RESTO-EQUALITY → distinto EQUALITY-EXPRESSION
	 *  169 .RESTO-EQUALITY → lambda
	 * @throws Exception 
	 */
	
	private void resto_equality() throws Exception {
		if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.IGUALDAD)){
			parse.add(219);
			nextToken();
			equality_expression();
		}
		else if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.DISTINTO)){
			parse.add(220);
			nextToken();
			equality_expression();
		}
		else {
			parse.add(169);
		}
		
	}
		
	/**	221. AND-EXPRESSION → EQUALITY-EXPRESSION RESTO_AND
	 * @throws Exception */
	private void and_expression() throws Exception{
		parse.add(221);
		equality_expression(); //Debe leer el siguiente token
		resto_and();
	}
	
	/**	222. RESTO-AND → & AND-EXPRESSION
	 *	223. RESTO-AND → lambda
	 * @throws Exception 
	 */
	private void resto_and() throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.BIT_AND)){
			parse.add(222);
			nextToken();
			and_expression();
		}
		else{
			parse.add(223);
		}		
	}
	
	/**	224. EXCLUSIVE-OR-EXPRESSION → AND-EXPRESSION RESTO-EXCLUSIVE
	 * @throws Exception */
	private void exclusive_or_expression() throws Exception{
		parse.add(224);
		and_expression(); 
		resto_exclusive();
	}

	/**	225. RESTO-EXCLUSIVE → ^ EXCLUSIVE-OR-EXPRESSION
		226. RESTO-EXCLUSIVE → lambda
	 * @throws Exception 
	 */
	private void resto_exclusive() throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.CIRCUNFLEJO)){
			parse.add(225);
			nextToken();
			exclusive_or_expression();
		}
		else{
			parse.add(226);
		}		
	}
	
	/**	227. INCL-OR-EXPRESSION → EXCL-OR-EXPRESSION RESTO_INCL-OR
	 * @throws Exception */
	private void incl_or_expression() throws Exception{
		parse.add(227);
		exclusive_or_expression(); 
		resto_incl_or();
	}

	/**	228. RESTO-INCL-OR → | INCL-OR-EXPRESSION
		229. RESTO-INCL-OR → lambda
	 * @throws Exception 
	*/
	private void resto_incl_or() throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.BIT_OR)){
			parse.add(228);
			nextToken();
			incl_or_expression();
		}
		else{
			parse.add(229);
		}
	}
	
	/**	230. LOG-AND-EXPRESSION → INCL-OR-EXPRESSION RESTO_LOG-AND
	 * @throws Exception */
	private void log_and_expression() throws Exception{
		parse.add(230);
		incl_or_expression(); 
		resto_log_and();
	}
	
	/**	231. RESTO-LOG-AND → && LOG-AND-EXPRESSION
		232. RESTO-LOG-AND → lambda
	 * @throws Exception */
	private void resto_log_and() throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.AND)){
			parse.add(231);
			nextToken();
			log_and_expression();
		}
		else{
			parse.add(232);
		}
	}
	
	/**	233. LOG-OR-EXPRESSION → LOG-AND-EXPRESSION RESTO_LOG-OR
	 * @throws Exception */
	private void log_or_expression() throws Exception{
		parse.add(233);
		log_and_expression(); 
		resto_log_or();
	}

	/**	234. RESTO-LOG-OR → || LOG-OR-EXPRESSION
		235. RESTO-LOG-OR → lambda
	 * @throws Exception 
	*/
	private void resto_log_or() throws Exception {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.OR)){
			parse.add(234);
			nextToken();
			log_or_expression();
		}
		else{
			parse.add(235);
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
		238. RESTO_CONDITIONAL →  ? EXPRESSION  :  ASSIGNMENT_EXPRESSION
		239. RESTO_CONDITIONAL → lambda
	 * @throws Exception 
	 */
	private void resto_conditional() throws Exception {//////////////////////////////////////////////
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.INTEROGACION)){
			parse.add(238);
			nextToken();
			expression();
			if(token.esIgual(TipoToken.SEPARADOR, Separadores.DOS_PUNTOS)){
				nextToken();
				assignment_expression();
			}
			else{
				gestorErr.insertaErrorSintactico(linea, columna,"Se esperaba \":\"");
				//ruptura=parse.size();
			}
		}
		else{
			parse.add(239);
		}		
	}
	
	/**
	 * 241. ASSIGNMENT_EXPRESSION → LOGICAL-OR-EXPRESSION RESTO_ASSIG
	 * @throws Exception 
	 */
	private void assignment_expression() throws Exception {
		parse.add(241);
		log_or_expression();
		resto_asig();
	}
	
	/**
	 * 242. RESTO_ASSIG → RESTO_CONDITIONAL
	 * 243. RESTO_ASSIG → op_asignacion ASSIGNMENT_EXPRESSION
	 * @throws Exception 
	 */
	private void resto_asig() throws Exception {
		if(token.esIgual(TipoToken.OP_ASIGNACION)){
			parse.add(243);
			nextToken();
			assignment_expression();
		}
		else{
			parse.add(242);
			resto_conditional();// Porque resto_conditional implementa las dos reglas que faltan: 238. RESTO_ASSIG →  ? EXPRESSION  :  ASSIGNMENT_EXPRESSION y 239. RESTO_ASSIG → lambda
		}		
	}

	/**
	 * 246. EXPRESSION → ASSIGNMENT-EXPRESSION RESTO_EXP
	 * @throws Exception 
	 */
	private void expression() throws Exception {
		parse.add(246);
		assignment_expression();
		resto_exp();
	}
	
	
	/**
	 * 247. RESTO_EXP → , EXPRESSION
	 * 248. RESTO_EXP→ lambda
	 * @throws Exception 
	 */
	private void resto_exp() throws Exception {
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.COMA)){
			parse.add(247);
			nextToken();
			expression();
		}	
		else {
			parse.add(248);
		}
	}
	
	/**
	 * 249. EXPRESSIONOPT → EXPRESSION 
	 * 250. EXPRESSIONOPT → lambda
	 * 						{ EXPRESIONOPT.tipo := vacio }
	 * @throws Exception 
	 */
	private ExpresionTipo expressionOpt() throws Exception {
		if(primeroDeExpression()) {
			parse.add(249);
			expression();
			return null; //TODO: CORREGIR!!!
		}
		else{
			parse.add(250);
			return new ExpresionTipo(TipoBasico.vacio);
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