package compilador.analizadorSintactico;

 
import java.util.Vector;

import compilador.analizadorLexico.*;
import compilador.analizadorLexico.Token.*;
import compilador.gestionErrores.GestorErrores;
import compilador.tablaSimbolos.*;
import compilador.tablaSimbolos.Tipo.EnumTipo;

public class AnalizadorSintactico {

	/** Token actual obtenido mediante el Analizador lexico */
	private Token token;
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
	
	private Tipo tipo;
	private EntradaTS entradaTS;
	
	public AnalizadorSintactico(AnalizadorLexico lexico){
		this.lexico = lexico;
		parse = new Vector<Integer>();
		tokens = new Vector<Token>();
		nextToken();
		gestorTS = GestorTablasSimbolos.getGestorTS();
		gestorErr = GestorErrores.getGestorErrores();
		tipo = null;
		entradaTS = null;
		programa();
	}
	
	public void nextToken() {
		token = lexico.scan();
		tokens.add(token);
	}
	
	private void idConst() {
		entradaTS = (EntradaTS)token.getAtributo();
		entradaTS.setTipo(tipo);
		entradaTS.setConstante(true);
		nextToken();
		if(token.esIgual(TipoToken.OP_ASIGNACION,OpAsignacion.ASIGNACION)) {
			nextToken();
			if(literal()) {
				Object valor = token.getAtributo(); // TODO depende del tipo... a ver que se hace con el...
				System.out.println("inicializacion constante " + entradaTS.getLexema() + " con " + valor);
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Constante mal inicializada");
			}
			
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Constante no inicializada");
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
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("LITERAL CADENA: " + valor);
			nextToken();
			return true;
		}
		else if(token.esIgual(TipoToken.LIT_CARACTER)){
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("LITERAL CARACTER: " + valor);
			nextToken();
			return true;
		} 
		else if (token.esIgual(TipoToken.NUM_ENTERO)){
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("NUMERO ENTERO: " + valor);
			nextToken();
			return true;
		}
		else if (token.esIgual(TipoToken.NUM_REAL)){
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("NUMERO REAL: " + valor);
			nextToken();
			return true;
		}
		else if (token.esIgual(TipoToken.NUM_REAL_EXPO)){
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("NUMERO REAL EXPO: " + valor);
			nextToken();
			return true;
		}
		else if (token.esIgual(TipoToken.PAL_RESERVADA) &&
				( (Integer)token.getAtributo() == 27 /*false*/ || 
				  (Integer)token.getAtributo() == 60 /*true*/)){
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("BOOLEANO: " + valor);
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
		for(Integer regla : parse) 
			string += regla + " ";
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
	 * 1. PROGRAMA → LIBRERIA RESTO_PROGRAMA
	 */
	private void programa() {
		parse.add(1);
		libreria();
		resto_programa();
	}
	
	/**
	 * 105. RESTO_PROGRAMA → class ID { CUERPO_CLASE } ;
	 * 106. RESTO_PROGRAMA → COSAS
	 */
	private void resto_programa() {
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
						} else {
							gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \";\"");
						}
					} else {
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \"}\"");
					}
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \"{\"");
				}
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el identificador");
			}
		} else {
			cosas();
		}
	}
	
	
	/**
	 * 107.CUERPO_CLASE → friend RESTO_FRIEND
	 * 108.CUERPO_CLASE → public : LISTA_CLASE CUERPO_CLASE
	 * 109.CUERPO_CLASE → private : LISTA_CLASE CUERPO_CLASE
	 * 110.CUERPO_CLASE → protected : LISTA_CLASE CUERPO_CLASE
	 * 111.CUERPO_CLASE → lambda
	 */
	private void cuerpo_clase() {
		if (!token.esIgual(TipoToken.EOF)) {
			if (token.esIgual(TipoToken.PAL_RESERVADA, 30)) {
				parse.add(107);
				nextToken();
				resto_friend();
			} else if (token.esIgual(TipoToken.PAL_RESERVADA, 44)) {
				parse.add(108);
				nextToken();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.DOS_PUNTOS)) {
					lista_clase();
					cuerpo_clase();
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \":\"");
				}
			} else if (token.esIgual(TipoToken.PAL_RESERVADA, 42)) {
				parse.add(109);
				nextToken();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.DOS_PUNTOS)) {
					lista_clase();
					cuerpo_clase();
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \":\"");
				}
			} else if (token.esIgual(TipoToken.PAL_RESERVADA, 43)) {
				parse.add(110);
				nextToken();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.DOS_PUNTOS)) {
					lista_clase();
					cuerpo_clase();
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \":\"");
				}
			} else {
				parse.add(111);
			}
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Fin de fichero inesperado");
		}
	}
	
	
	/**
	 * 112.RESTO_FRIEND → void RESTO_FRIEND2
	 * 113.RESTO_FRIEND → TIPO RESTO_FRIEND2
	 */
	private void resto_friend() {
		if (token.esIgual(TipoToken.PAL_RESERVADA, 69)) {
			parse.add(112);
			nextToken();
			resto_friend2();
		} else if (tipo()) {
			parse.add(113);
			nextToken();
			resto_friend2();
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta tipo de retorno (o void)");
		}
	}
	
	
	/**
	 * 114.RESTO_FRIEND2 → ID ( LISTA_PARAM ) ; CUERPO_CLASE
	 */
	private void resto_friend2() {
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
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \";\"");
					}
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \")\"");
				}	
			} else  {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \"(\"");
			}
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el identificador");
		}
	}
	
	
	/**
	 * 115.LISTA_CLASE → void RESTO_METODO LISTA_CLASE
	 * 116.LISTA_CLASE → TIPO RESTO_LINEA
	 */
	private void lista_clase() {
		if (token.esIgual(TipoToken.PAL_RESERVADA, 69)) {
			parse.add(115);
			nextToken();
			resto_metodo();
			lista_clase();
		} else if (tipo()) {
			parse.add(116);
			nextToken();
			resto_linea();
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta tipo de retorno (o void)");
		}
	}
	
	
	/**
	 * 117.RESTO_LINEA → ID RESTO_LINEA2
	 */
	private void resto_linea() {
		if (token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(117);
			nextToken();
			resto_linea2();
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el identificador");
		}
	}
	
	
	/**
	 * 118.RESTO_LINEA2 → ( RESTO_METODO 
	 * 119.RESTO_LINEA2 → ; LISTA_CLASE
	 */
	private void resto_linea2() {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(118);
			nextToken();
			resto_metodo();
		} else if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
			parse.add(119);
			nextToken();
			lista_clase();
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \";\" o \"(\"");
		}
	}
	
	
	/**
	 * 120.RESTO_METODO → LISTA_PARAM ) RESTO_METODO2 ; LISTA_CLASE
	 */
	private void resto_metodo() {
		parse.add(120);
		lista_param();
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			nextToken();
			resto_metodo2();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
				nextToken();
				lista_clase();
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \";\"");
			}
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \"(\"");
		}
	}
	
	
	/**
	 * 121.RESTO_METODO2 → { CUERPO
	 * 122.RESTO_METODO2 → lambda
	 */
	private void resto_metodo2() {
		if (!token.esIgual(TipoToken.EOF)) {
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_LLAVE)) {
				parse.add(121);
				nextToken();
				cuerpo();
			} else {
				parse.add(122);
			}
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Fin de fichero inesperado");
		}
	}
	
	/**
	 *  2. LIBRERIA → #include RESTO_LIBRERIA
	 *  3. LIBRERIA -> lambda
	 */
	private void libreria() {
		if (!token.esIgual(TipoToken.EOF)) {
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ALMOHADILLA)) {
				parse.add(2);
				nextToken();
				if(token.esIgual(TipoToken.PAL_RESERVADA, 77)) {
					//token.esIgual(TipoToken.IDENTIFICADOR) && 
					//"include".equals( ((EntradaTS)token.getAtributo()).getLexema() )) {
					nextToken();
					resto_libreria();
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta palabra \"include\""); 
				}
					//gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						//"Falta la palabra reservada \"include\"");				
				//}
			} else {
				parse.add(3);
			}
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Fin de fichero inesperado");
		}
	}

	/**
	 * 4. RESTO_LIBRERIA → LIT_CADENA LIBRERIA
	 * 5. RESTO_LIBRERIA → <ID.ID> LIBRERIA
	 */
	private void resto_libreria() {
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
							gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta separador \">\"");			
						}
					} else {
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta extension de libreria");			
					}
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \".\"");				
				}
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta la libreria");				
			}	
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta separador \"<\" o \"___\"");				
		}
		
	}
	
	
	/**
	 * 6. TIPO → ID										
	 * 7. TIPO → TIPO_SIMPLE
	 */
	private boolean tipo() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(6);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
			return true;
		} else if(token.esIgual(TipoToken.PAL_RESERVADA) && 
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
	 * 11. COSAS → enum ID { LISTANOMBRES } ; COSAS
	 * 12. COSAS → struct RESTO_ST COSAS
	 */
	private void cosas() {
		if(!token.esIgual(TipoToken.EOF)) {
			if(token.esIgual(TipoToken.PAL_RESERVADA, 9)){
				parse.add(8);
				nextToken();
				if(tipo()) {
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						idConst(); // ID = LITERAL
						inic_const();
						if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
							gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta separador \";\"");
						} else {
							nextToken();
							cosas();
						}
					} else {
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el nombre de la variable");
					}
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta tipo de la variable");	
				}
			} else if(tipo()) {
				parse.add(9);
				if(token.esIgual(TipoToken.IDENTIFICADOR)) {
					id();
					cosas2();	
					cosas();			
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el nombre de la variable");
				}
			} else if(token.esIgual(TipoToken.PAL_RESERVADA, 69)){
				parse.add(10);
				nextToken();
				if(token.esIgual(TipoToken.IDENTIFICADOR)) {
					nextToken();
					if(token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)){
						nextToken();
						lista_param();
						if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
							nextToken();
							cosas3();
							cosas();
						} else {
							gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta separador \")\"");
						}
					} else {
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta separador \"(\"");
					}
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta nombre de la funcion");	
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
								gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta separador \";\"");
							} else {
								nextToken();
								cosas();
							}
						} else{
							gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta separador \"}\"");
						}
					} else{
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta separador \"{\"");
					}
				} else{
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta nombre de lista");	//No me gusta... :(
				}
			} else if(token.esIgual(TipoToken.PAL_RESERVADA, 54)){
				parse.add(12);
				nextToken();
				resto_st();
				cosas();
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "");
			}
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Fin de fichero inesperado");
		}
	}
	
	
		
	/** 
	 * 14. LISTANOMBRES → ID RESTO_ListaNombres
	 * 15. LISTANOMBRES → lambda
	 */
	private void listaNombres() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(14);
			id();
			resto_ln();
		}
		else{
			parse.add(15);
		}
	}
	
	/**	102. RESTO_ListaNombres → , ID LISTANOMBRES
		103. RESTO_ListaNombres → lambda
	*/
	private void resto_ln() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(102);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				id();
				listaNombres();
			}
			else{
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta identificador de lista");
			}
		}
		else{
			parse.add(103);
		}
	}

	/**	
	 * 16. COSAS2 → ( LISTA_PARAM ) COSAS3
	 * 17. COSAS2 → INICIALIZACION  DECLARACIONES ;
	 */
	private void cosas2() {
		//16. COSAS2 → ( LISTA_PARAM ) COSAS3
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) {
			parse.add(16);
			nextToken();
			lista_param();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
				nextToken();
				cosas3();
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta separador \")\"");
			}
		} 
		//17. COSAS2 → INICIALIZACION  DECLARACIONES ;
		else {
			parse.add(17);
			inicializacion();
			declaraciones();		
			if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta separador \";\"");
			}
			nextToken();
		}
	}
	

	/**	18. COSAS3 → ;
		19. COSAS3 → { CUERPO 
	*/
	private void cosas3() {
		
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
			parse.add(18);
			nextToken();
			System.out.println("cabecera funcion " + entradaTS.getLexema());
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			parse.add(19);
			nextToken();
			cuerpo();
		}
		else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Se esperaba \";\" o \"{\" ");
		}
		
	}

	/**20. LISTA_PARAM → TIPO ID PASO RESTO_LISTA
	 * 21. LISTA_PARAM → lambda
	 */
	private void lista_param() {
		if(tipo()){
			parse.add(20);
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				id();
				if(paso()){
					restoLista();
				}
				else{
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Paso de parametros incorrecto");
				}
			}
			else{
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta identificador de lista de parametros");	
			}
		}
		else{
			parse.add(21);
		}
	} 

	/** 123.PASO → & 
	 *	124.PASO → *
	 *  125.PASO → lambda
	 */
	private boolean paso() {
		// TODO Auto-generated method stub
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.AND)) {
			parse.add(123);
			Object valor = token.getAtributo(); 
			System.out.println("Paso parametro: " + valor);
			nextToken();
			return true;
		}
		if(token.esIgual(TipoToken.OP_ARITMETICO,OpAritmetico.MULTIPLICACION)) {
			parse.add(124);
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("Paso parametro: " + valor);
			nextToken();
			return true;
		}
		else{
			parse.add(125);
			nextToken();
			return true;
		}
	}


	/** 
	 * 22. RESTO_LISTA → , LISTA_PARAM 
	 * 23. RESTO_LISTA → lambda
	 */
	private void restoLista() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(22);
			nextToken();
			lista_param();
		}
		else{
			parse.add(23);
		}
	}


	/**	
	 * 24. DIMENSION → [ NUM_ENTERO ] DIMENSION
	 * 25. DIMENSION → lambda
	 */
	private void dimension() {
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
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta separador \"]\"");
				}
			}
			else{
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Se esperaba un numero entero");
			}
		}
		else{
			parse.add(25);
		}
		
	}

	/**	
	 * 26. INIC_DIM → = INIC_DIM2
	 * 27. INIC_DIM → lambda
	 */
	private void inicDim() {
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
	 */
	private void inicDim2() {
		if(!token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_LLAVE)){
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta separador \"{\"");
		}
		else{
			parse.add(28);
			nextToken();
			inicDim3();
			if(!token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)){
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta separador \"}\"");
			}
			else{
				nextToken();
			}
		}
	}

	/**	29. INIC_DIM3 → LITERAL INIC_DIM4 
		30. INIC_DIM3 → INIC_DIM2 INIC_DIM5
	 */
	private void inicDim3() {
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
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Fin de fichero inesperado");
		}
	}

	/**	31. INIC_DIM4 → , LITERAL INIC_DIM4 
		32. INIC_DIM4 → lambda
	 */
	private void inicDim4() {
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.COMA)){
			parse.add(31);
			nextToken();
			if(!literal()){
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Inicializacion de array incorrecta, se esperaba token literal");
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
	 */
	private void inicDim5() {
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
	 */
	private void inic_const() {
		System.out.println("declaracion constante " + entradaTS.getLexema());
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(35);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				idConst(); // = LITERAL
				inic_const();
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Declaracion de constantes incorrecta, se espera un identificador");
			}	
		}
		else { ///Si es lambda
			parse.add(36);
		}	
	}

	
	/**
	 * 37. DECLARACIONES → , ID INICIALIZACION DECLARACIONES
	 * 38. DECLARACIONES → lambda
	 */
	private void declaraciones() {
		System.out.println("declaracion variable " + entradaTS.getLexema());
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(37);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				id();
				inicializacion();
				declaraciones();
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Falta el identificador");;
			}		
		} else {
			parse.add(38);
		}
	}
	
	
	

	/**
	 * 39. INICIALIZACION → = LITERAL
	 * 40. INICIALIZACION → [NUM_ENTERO] DIMENSION INIC_DIM
	 * 41. INICIALIZACION → lambda
	 */
	private void inicializacion() {
		//39. INICIALIZACION → = LITERAL
		if(token.esIgual(TipoToken.OP_ASIGNACION,OpAsignacion.ASIGNACION)) {
			parse.add(39);
			nextToken();
			if(esLiteral()){
				nextToken();
			}
			else{
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Inicialización incorrecta, se espera un literal");
			}
			
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
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta separador \"]\"");
				}
			}
			else{
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Se espera un numero entero (tamaño del array)");
			}
		} else {
			parse.add(41);
		}
	}

	/**
	 * 42. INSTRUCCION → ID INSTRUCCION2
	 * 43. INSTRUCCION → struct RESTO_ST
	 * 44. INSTRUCCION → cin INS_LECT
	 * 45. INSTRUCCION → cout INS_ESC
	 * 46. INSTRUCCION → const INS_DEC
	 * 47. INSTRUCCION → TIPO INS_DEC2
	 * 48. INSTRUCCION → ;
	 */
	
	private boolean instruccion() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)) { 
			parse.add(42);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
			return instruccion2();
			}
		else if(token.esIgual(TipoToken.PAL_RESERVADA, 54 /*struct*/ )){ //INS_REG
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
		else if (tipo()) { //INS_DEC2
			parse.add(47); 
			nextToken();
			ins_dec2();
		} 
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) { //INS_VACIA
			parse.add(48);
			nextToken();
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
			"Falta separador \";\"");
			return false;
		}
		
		return true;
	}
	
	
	/** 
	 * 49. INSTRUCCION2 → ( LISTA_ATB ) ; 
	 * 50. INSTRUCCION2 → OP_ASIGNACION EXPRESION
	 */
	private boolean instruccion2(){
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) { //INS_FUNCION
			parse.add(49);
			nextToken();
			lista_atb();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
				System.out.println("llamada a funcion " + entradaTS.getLexema());
				nextToken();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
					nextToken();
					return true;
				}
				else{
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), 
					"Falta separador \";\"");
				}
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), 
						"Falta separador \")\"");
				return false;
			}
		} else if (token.esIgual(TipoToken.OP_ASIGNACION)) { //INS_ASIG
			parse.add(50);
			nextToken();
			//llamada a EXPRESION
		} 
		else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
			"Se espera \"(\" o un operador de asignacion");
			return false;
		}
		return true;
	}
	
	/**
	 * 51. INS_DEC → TIPO ID = LITERAL INIC_CONST ;
	 */
	private void ins_dec() {
		if (tipo()) {
			parse.add(51);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
				nextToken();
				if (token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.IGUALDAD)) {
					nextToken();
					if (esLiteral()) {
						nextToken();
						inic_const(); //no lo tengo muy claro
						if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) {
							nextToken();
						} else {
							//error
							gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), 
									"Se esperaba un separador \";\"");
						}
					} else {
						//error
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), 
								"Se esperaba un valor literal");
					}
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), 
							"Se esperaba el operador de comparacion \"=\"");
				}
			} else {
				// error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), 
						"Se esperaba un identificador");
			}
		}
		
	}
	
	/**
	 * 52. INS_DEC2 → ID MAS_COSAS
	 */
	private void ins_dec2() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(52);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
			mas_cosas();
		} else {
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Se esperaba un identificador");
		}
	}
	
	/**
	 * 53. MAS_COSAS → INICIALIZACION  DECLARACIONES
	 */
	private void mas_cosas() {
		parse.add(53);
		inicializacion();
		declaraciones();
	}
	
	
	
	/**
	 * 54. LISTA_ATB → ATRIBUTO RESTO_ATB
	 * 55. LISTA_ATB → lambda
	 */
	private void lista_atb() {							//TOFIX aqui no habria que meter algun error?
		if(!token.esIgual(TipoToken.EOF)) {
			parse.add(54);
			atributo();
			resto_atb();
		} else {
			// 55. LISTA_ATB → lambda 
			parse.add(55);
		}
	}
	
	/**
	 * 56. RESTO_ATB → , ATRIBUTO RESTO_ATB
	 * 57. RESTO_ATB → lambda
	 */
	private void resto_atb() {							//TOFIX aqui no habria que meter algun error?
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(56);
			nextToken();
			atributo();
			resto_atb();
		} else {
			parse.add(57);
		}
	}
	
	/**
	 * 58. ATRIBUTO → LITERAL
	 * 59. ATRIBUTO → ID
	 */
	private void atributo() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(59);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
		} else if(esLiteral()){
			parse.add(58);
			//no se si hay que hacer algo mas...
			nextToken();
		} else {
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), 
					"Falta identificador o literal que identifique al atributo");
		}
	}
	
	
	
	/**
	 * 60. RESTO_ST → ID { CUERPO_ST } ID NOMBRES
	 * 61. RESTO_ST → { CUERPO_ST } ID NOMBRES
	 */
	private void resto_st() {
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
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta la identificacion de la estructura");
					}
				} else
					// error
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador cierra llave }");
			}
			else{
				// error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador abre llave {");
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
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta la identificacion de la estructura");
				}
			} else {
				// error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador cierra llave }");
			}
		} else{
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador abre llave { o identificacion de la estructura");
		}
	}
	
	
	/**
	 * 62. CUERPO_ST → TIPO ID RESTO_VAR CUERPO_ST
	 * 63. CUERPO_ST → lambda
	 */
	private void cuerpo_st() {
		if(tipo()) {
			parse.add(62);
			nextToken();
			if (token.esIgual(TipoToken.IDENTIFICADOR)) {
				tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
				nextToken();
				resto_var();
				cuerpo_st();
			}
			else {
				//error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Los atributos deben estar identificados");
			}
		} else
			parse.add(63);
		
	}
	
	
	/**
	 * 64. RESTO_VAR → , ID RESTO_VAR
	 * 65. RESTO_VAR → ;
	 */
	private void resto_var() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)){ 
			parse.add(64);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
				nextToken();
				resto_var();
			} else {
				// error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), 
						"Se deben identificar todos los atributos");
			}
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
			parse.add(65);
			nextToken();
		} else {
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), 
					"Se esperaba un separador coma (,) o punto_coma (;)");
		}
	}
	
	
	/**
	 * 66. NOMBRES → , ID NOMBRES
	 * 67. NOMBRES → ;
	 */
	private void nombres() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)){ 
			parse.add(66);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
				nextToken();
				nombres();
			} else {
				// error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Se deben identificar todas las variables de la estructura");
			}
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
			parse.add(67);
			nextToken();
		} else {
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), 
					"Se esperaba un separador coma (,) o punto_coma (;)");
		}
	}
	
		
	/**
	 * 68. INS_LECT → >>  RESTO_LECT 
	 */
	private void ins_lect() { 
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MAYORES)){
			parse.add(68);
			nextToken();
			resto_lect();
		}
		else{
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Lectura incorrecta, se esperaba el operador \">>\"");
		}
	}
	
	/**
	 * 69. RESTO_LECT → ID ;
	 * 70. RESTO_LECT → LITERAL  ;
	 */
	private void resto_lect() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)){ //FALTA ALGO MAS AQUI???
			parse.add(69);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
				nextToken();
			}
			else{
				// error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Lectura terminada incorrectamente, falta ';'");
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
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Lectura terminada incorrectamente, falta ';'");
			}
		}
		else{
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Lectura incorrecta, se esperaba un literal o una variable");
		}
	}
	
	/**
	 * 71. INS_ESC → << RESTO_ESC
	 */
	private void ins_esc() {
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MENORES)){
			parse.add(63);
			nextToken();
			resto_esc();
		}
		else{
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Escritura incorrecta, se esperaba el operador \"<<\"");
		}
	}
	
	/**
	 * 72. RESTO_ESC →  LITERAL INS_ESC2
	 * 73. RESTO_ESC →  ID INS_ESC2
	 * 74. RESTO_ESC →  endl INS_ESC2
	 */
	private void resto_esc() {
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
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Escritura incorrecta, se esperaba un literal, una variable o la palabra reservada \"endl\"");
		}
	}
	
	/**
	 * 75. INS_ESC2 →  << RESTO_ESC2
	 * 76. INS_ESC2 →  ;
	 */
	private void ins_esc2(){
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
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Escritura terminada incorrectamente, falta ';'");
		}
	}
	
	
	/**
	 * 77. RESTO_ESC2 → LITERAL  INS_ESC2 
	 * 78. RESTO_ESC2 → ID  INS_ESC2  
	 * 79. RESTO_ESC2 → endl INS_ESC2 
	 * 80. RESTO_ESC2 → lamdba
	 */
	private void resto_esc2() {
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
	 * 
	 */
	private void ins_asig() {
		
	}
	
	
	/**
	 * 81. CUERPO --> for RESTO_FOR CUERPO
	 * 82. CUERPO --> do RESTO_DO CUERPO
	 * 83. CUERPO --> while RESTO_WHILE CUERPO
	 * 84. CUERPO --> if RESTO_IF CUERPO
	 * 85. CUERPO --> switch RESTO_CASE CUERPO
	 * 86. CUERPO --> }
	 * 104. CUERPO → { CUERPO } CUERPO
	 * 128. CUERPO → break ; CUERPO
	 * 129. CUERPO → continue ; CUERPO
	 * 130. CUERPO → return EXPRESSIONOPT; CUERPO
	 * 131. CUERPO → goto ID ; CUERPO
	 * 132. CUERPO → INSTRUCCION CUERPO
	 */
	private void cuerpo()
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
		}
		else if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
			parse.add(86);
			nextToken();
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			parse.add(104);
			nextToken();
			cuerpo();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
				nextToken();
				cuerpo();
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \"}\"");
			}
		} else if(token.esIgual(TipoToken.PAL_RESERVADA,5 /*break*/)) {
			parse.add(128);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				nextToken();
				cuerpo();
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \";\"");
			}
		} else if(token.esIgual(TipoToken.PAL_RESERVADA, 12 /*continue*/)) {
			parse.add(129);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				nextToken();
				cuerpo();
			} else { 
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \";\"");
			}
		} else if(token.esIgual(TipoToken.PAL_RESERVADA, 47 /*return*/)) {
			parse.add(130);
			nextToken();
			expressionOpt();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				nextToken();
				cuerpo();
			} else { 
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \";\"");
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
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \";\"");
				}
			} else { 
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el identificador");
			}
		} else {
			parse.add(132);
			if(instruccion())
				cuerpo();
			else
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Token inesperado... ");
		}	
	}

	/** 
	 * 87. CUERPO2 --> while RESTO_WHILE
	 * 88. CUERPO2 --> for RESTO_FOR
	 * 89. CUERPO2 --> do RESTO_DO
	 * 90. CUERPO2 --> if RESTO_IF 
	 * 91. CUERPO2 --> switch RESTO_CASE
	 * 92. CUERPO2 --> { CUERPO 
	 */
	private void cuerpo2() {
		
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
		}
		else {
			parse.add(93);
			instruccion();
		}
		/*else 	
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
				"Token inesperado... "); */
		
	}
	
	
	/**
	 *  94. RESTO_WHILE --> (EXPRESSION) do CUERPO2
	 */
	private void resto_while() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(94);
			nextToken();
			expression();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
				cuerpo2();
			} else
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \")\"");
		} else
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \"(\"");
	}		

	/**
	 * 95. RESTO_DO --> CUERPO2 while (EXPRESSION);
	 */
	private void resto_do() {
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
					else
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \";\"");
				}
				else
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \")\"");
			}
			else
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \"(\"");
		}
		else
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta palabra \"while\"");		
	}	
		
	/**
	 * 96. RESTO_FOR → ( FOR-INIT ; EXPRESSIONOPT ; EXPRESSIONOPT ) CUERPO2 
	 */
	private void resto_for() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(96);
			nextToken();
			for_init();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				nextToken();
				expression();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
					nextToken();
					expression();
					if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
						nextToken();
						cuerpo2();
					} else
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \")\"");
				} else
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \";\"");
			} else
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \";\"");
		} else
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \"(\"");
	}

	/**
	 * 135. FOR-INIT → id INICIALIZACION
	 * 136. FOR-INIT → TIPO id INICIALIZACION
	 * 137. FOR-INIT → EXPRESSIONOPT
	 */
	private void for_init() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(135);
			nextToken();
			inicializacion();
		} else if(tipo()) {
			parse.add(136);
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				nextToken();
				inicializacion();
			}
		} else {
			parse.add(137);
			expressionOpt();
		}
	}

	/** TODO ¡¡¡¡¡SIN TERMINAR!!!
	 * 97. RESTO_CASE --> ( ID ) { CUERPO_CASE }
	 */
	private boolean resto_case() {
		
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(97);
			nextToken();
			 if(token.esIgual(TipoToken.IDENTIFICADOR)){
					nextToken();
					if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
						nextToken();
						if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_CORCHETE)) {
							nextToken();
							cuerpo_case();
							if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_CORCHETE)) {
								nextToken();
								return true;
							}
							else 
								gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
								"Lectura terminada incorrectamente, falta '}'");
						}
						else
							gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
							"Lectura terminada incorrectamente, falta '{'");
					}
					else
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Lectura terminada incorrectamente, falta ')'");
				}
			 else
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Lectura terminada incorrectamente, falta identificador");
			
		}
		else
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
			"Lectura terminada incorrectamente, falta ')'");
		
	
	return false;
	}

	/** TODO ¡¡¡¡¡SIN TERMINAR!!!
	 * 98. CUERPO_CASE --> case LITERAL: CUERPO2 CUERPO_CASE
	 * mas expresiones: defalut: break; goto identifier ; continue ;
	 * se usa solo CUERPO2 al haber cambiado la estructura de CUERPO!!! (falta otro caso válido)
	 */
	private void cuerpo_case() {
		if(token.esIgual(TipoToken.PAL_RESERVADA,6 /*case*/)) {
			parse.add(98);
			nextToken();
			if(literal()) /** TODO si literal lanza error (alomejor deberiamos tratar errores con excepciones), deberia seguir? si no entonces esto no habria que comprobarlo.. (Cris)*/
			{
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.DOS_PUNTOS)){
					nextToken();
					cuerpo2();// Aqui podriamos hacer una especie de cuerpo2 que admita break y mas de una instruccuon sin parentesis
					cuerpo_case();
				}	
			}
			else 
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
				"Lectura terminada incorrectamente, falta literal");
		}
	}
	/**
	 *  99. RESTO_IF --> ( EXPRESSION ) CUERPO2 SENT_ELSE
	 */
	private boolean resto_if() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(99);
			nextToken();
			expression();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
				cuerpo2();
				sent_else();
				return true;
			} else
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador \")\"");
		} else
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \"(\"");
		return false;
	}
	
	
	/**
	 * 100. SENT_ELSE --> else CUERPO2 
	 * 101. SENT_ELSE --> lambda
	 */
	private void sent_else() {
		if(token.esIgual(TipoToken.PAL_RESERVADA,22 /*else*/)) {
			parse.add(100);
			nextToken();
			cuerpo2();
		}
		else //SENT_ELSE -> lambda
			parse.add(101);
	}
	
	// PARA BORRAR:
	
	/**
	 * 44. INS_FUNCION → ID (LISTA_ATB);
	 */
/*	private void ins_fun() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(44);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) {
				parse.add(44);
				nextToken();
				lista_atb();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
					System.out.println("llamada a funcion " + entradaTS.getLexema());
					nextToken();
				} else {
					// error
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador abre llave {");				
				}
			} else {
				//error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador cierra llave }");
			}
		} else {
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta un nombre que identifique a la funcion");
		}
		
	}*/
	
	/**
	 * 51. INS_REGISTRO → struct RESTO_ST
	 */
	/*private void ins_reg() {
		//if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 54){
			parse.add(51);
			nextToken();
			resto_st();
	//	} else {
			// error
	//		gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta la palabra reservada \"struct\"");
		//}
	}*/
	
	/**
	 * 73. INS_ASIGNACION ? ID  OP_ASIGNACION EXPRESION
	 */
/*	private void ins_asignacion()
	{
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(73);
			//tipo = null;//((EntradaTS)token.getAtributo()).getLexema();//TOFIX obtener enumerado tipo de la variable declarada
			nextToken();
			//expresion();
		} 
		
	}*/

	private void additive_expression() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 144. PRIMARY-EXPRESSION → LITERAL
	 * 145. PRIMARY-EXPRESSION → this
	 * 146. PRIMARY-EXPRESSION → UNQUALIFIED-ID
	 * 147. PRIMARY-EXPRESSION → ( EXPRESSION )
	 */
	private boolean primary_expression() {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(147);
			nextToken();
			expression();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
				nextToken();
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \")\"");
				return false;
			}
		} else if (token.esIgual(TipoToken.PAL_RESERVADA, 57)) {
			parse.add(145);
			nextToken();
		} else if(literal()) {
			parse.add(144);
			nextToken();
		} else {
			parse.add(146);
			unqualified_id();
		}
		return true;
	}
	
	
	/**
	 * 148. UNQUALIFIED-ID → id
	 * 149. UNQUALIFIED-ID →  ~ RESTO_UNQ
	 */
	private void unqualified_id() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(148);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
		} else if (token.esIgual(TipoToken.OP_LOGICO,OpLogico.SOBRERO)) {
			parse.add(149);
			resto_unq();
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta identificador u op_logico \"~\"");
		}
	}
	
	
	/**
	 * 150. RESTO_UNQ → id
	 * 151. RESTO_UNQ → decltype ( EXPRESSION )
	 */
	private void resto_unq() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(150);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
		} 
		// decltype
		else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta identificador o decltype");
		}
	}
	
	
	/**
	 * 152. POSTFIX-EXPRESSION → typeid ( EXPRESSION ) RESTO_POSTFIX_EXP
	 * 153. POSTFIX-EXPRESSION → TIPO POSTFIX-2 RESTO_POSTFIX_EXP
	 * 154. POSTFIX-EXPRESSION → PRIMARY-EXPRESSION RESTO_POSTFIX_EXP
	 *
	 * 168. POSTFIX-EXPRESSION → SIMPLE-TYPE-SPECIFIER ( POSTFIX-2
	 * 170. POSTFIX-EXPRESSION →  ~ POSTFIX-EXPRESSION
	 */
	private void postfix_expression() {
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
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \")\"");
				}
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \"(\"");
			}
		} else if (tipo()) {
			parse.add(153);
			postfix2();
			resto_postfix_exp();
		} else if  (token.esIgual(TipoToken.OP_LOGICO,OpLogico.SOBRERO)) {
			parse.add(170);
			nextToken();
			postfix_expression();
		}
		//168
		else {
			primary_expression();
			resto_postfix_exp();
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
	 */
	private void resto_postfix_exp() {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_CORCHETE)) {
			parse.add(155);
			nextToken();
			expression();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_CORCHETE)) {
				nextToken();
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \"]\"");
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
	 */
	private void resto_postfix_exp2() {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
			parse.add(162);
			nextToken();
		} else {
			parse.add(163);
			initializer_list();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
				nextToken();
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \")\"");
			}
		}
	}
	
	
	/**
	 * 166. RESTO_POSTFIX_EXP3 → ~ decltype ( EXPRESSION )
	 * 167. RESTO_POSTFIX_EXP3 → UNQUALIFIED-ID
	 */
	private void resto_postfix_exp3() {
		if (token.esIgual(TipoToken.OP_LOGICO,OpLogico.SOBRERO)) {	
			parse.add(166);
			nextToken();
			//decltype
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
				nextToken();
				expression();
				if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
					nextToken();
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \")\"");
				}
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \"(\"");
			}
		} else {
			unqualified_id();
		}
	}
	
	
	/**
	 * 173. POSTFIX-2 → ( POSTFIX-3
	 */
	private void postfix2() {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)) {
			parse.add(173);
			nextToken();
			postfix3();
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \"(\"");
		}
	}
	
	
	/**
	 * 174. POSTFIX-3 →  )
	 * 175. POSTFIX-3 → INITIALIZER-LIST )
	 */
	private void postfix3() {
		if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
			parse.add(174);
			nextToken();
		} else {
			parse.add(175);
			initializer_list();
			if (token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)) {
				nextToken();
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta el separador \")\"");
			}
		}
	}
	
	
		
	//TOMAS, esta es tuyaaa :) pero asi no hay errores...
	private void initializer_list() {
		
	}

		
		
		
		
	/**208. SHIFT-EXPRESSION → ADDITIVE_EXPRESSION RESTO_SHIFT*/
	
	private void shift_expression(){
		parse.add(208);
		additive_expression(); //Debe leer el siguiente token
		resto_shift();
	}

	/** 209. RESTO_SHIFT →  <<  SHIFT-EXPRESSION
		210. RESTO_SHIFT →  >> SHIFT-EXPRESSION
		211. RESTO_SHIFT → lambda
	*/
	
	private void resto_shift() {
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
	
	/**	212. RELATIONAL-EXPRESSION → SHIFT-EXPRESSION RESTO-RELATIONAL*/
	
	private void relational_expression(){
		parse.add(208);
		shift_expression(); //Debe leer el siguiente token
		resto_relational();
	}

	/**	213. RESTO-RELATIONAL → < RESTO2-RELATIONAL
		214. RESTO-RELATIONAL → > RESTO2-RELATIONAL
		215. RESTO-RELATIONAL → lambda
	 */
	private void resto_relational() {
		if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MENOR)){
			parse.add(213);
			nextToken();
			resto2_relational();
		}
		else if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MAYOR)){
			parse.add(214);
			nextToken();
			resto2_relational();
		}
		else{
			parse.add(215);
		}
		
	}
	
	/**	216. RESTO2-RELATIONAL → = SHIFT-EXPRESSION
		217. RESTO2-RELATIONAL → SHIFT-EXPRESSION
	*/
	
	private void resto2_relational() {
		if(token.esIgual(TipoToken.OP_ASIGNACION, OpAsignacion.ASIGNACION)){
			parse.add(216);
			nextToken();
			shift_expression();
		}
		else{
			parse.add(217);
			shift_expression();
		}
		
	}
	
	/** 218. EQUALITY-EXPRESSION → RELATIONAL-EXPRESSION RESTO_EQUALITY*/
	
	private void equality_expression(){
		parse.add(218);
		relational_expression(); //Debe leer el siguiente token
		resto_equality();
	}

	
	/**	219. RESTO-EQUALITY → igualdad EQUALITY-EXPRESSION
	 * 	220. RESTO-EQUALITY → distinto EQUALITY-EXPRESSION
	 */
	
	private void resto_equality() {
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
		
	}
		
	/**	221. AND-EXPRESSION → EQUALITY-EXPRESSION RESTO_AND*/
	private void and_expression(){
		parse.add(208);
		equality_expression(); //Debe leer el siguiente token
		resto_and();
	}
	
	/**	222. RESTO-AND → & AND-EXPRESSION
	 *	223. RESTO-AND → lambda
	 */
	private void resto_and() {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.BIT_AND)){
			parse.add(222);
			nextToken();
			and_expression();
		}
		else{
			parse.add(223);
		}		
	}
	
	/**	224. EXCLUSIVE-OR-EXPRESSION → AND-EXPRESSION RESTO-EXCLUSIVE*/
	private void exclusive_or_expression(){
		parse.add(224);
		and_expression(); 
		resto_exclusive();
	}

	/**	225. RESTO-EXCLUSIVE → ^ EXCLUSIVE-OR-EXPRESSION
		226. RESTO-EXCLUSIVE → lambda
	 */
	private void resto_exclusive() {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.CIRCUNFLEJO)){
			parse.add(225);
			nextToken();
			exclusive_or_expression();
		}
		else{
			parse.add(226);
		}		
	}
	
	/**	227. INCL-OR-EXPRESSION → EXCL-OR-EXPRESSION RESTO_INCL-OR*/
	private void incl_or_expression(){
		parse.add(227);
		exclusive_or_expression(); 
		resto_incl_or();
	}

	/**	228. RESTO-INCL-OR → | INCL-OR-EXPRESSION
		229. RESTO-INCL-OR → lambda
	*/
	private void resto_incl_or() {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.BIT_OR)){
			parse.add(228);
			nextToken();
			incl_or_expression();
		}
		else{
			parse.add(229);
		}
	}
	
	/**	230. LOG-AND-EXPRESSION → INCL-OR-EXPRESSION RESTO_LOG-AND*/
	private void log_and_expression(){
		parse.add(230);
		incl_or_expression(); 
		resto_log_and();
	}
	
	/**	231. RESTO-LOG-AND → && LOG-AND-EXPRESSION
		232. RESTO-LOG-AND → lambda*/
	private void resto_log_and() {
		if(token.esIgual(TipoToken.OP_LOGICO, OpLogico.AND)){
			parse.add(231);
			nextToken();
			log_and_expression();
		}
		else{
			parse.add(232);
		}
	}
	
	/**	233. LOG-OR-EXPRESSION → LOG-AND-EXPRESSION RESTO_LOG-OR*/
	private void log_or_expression(){
		parse.add(233);
		log_and_expression(); 
		resto_log_or();
	}

	/**	234. RESTO-LOG-OR → || LOG-OR-EXPRESSION
		235. RESTO-LOG-OR → lambda
	*/
	private void resto_log_or() {
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
	/**	236. CONDITIONAL_EXPRESION → LOGICAL_OR_EXPRESSION RESTO_CONDITIONAL*/
	private void conditional_expression(){///////////////////////////////////////
		parse.add(236);
		log_or_expression(); 
		resto_conditional();
	}
	
	/**	
		238. RESTO_CONDITIONAL →  ? EXPRESSION  :  ASSIGNMENT_EXPRESSION
		239. RESTO_CONDITIONAL → lambda
	 */
	private void resto_conditional() {//////////////////////////////////////////////
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.INTEROGACION)){
			parse.add(238);
			nextToken();
			expression();
			if(token.esIgual(TipoToken.SEPARADOR, Separadores.DOS_PUNTOS)){
				nextToken();
				assignment_expression();
			}
			else{
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Se esperaba \":\"");
			}
		}
		else{
			parse.add(239);
		}		
	}
	
	/**
	 * 241. ASSIGNMENT_EXPRESSION → LOGICAL-OR-EXPRESSION RESTO_ASSIG
	 */
	private void assignment_expression() {
		parse.add(241);
		log_or_expression();
		resto_asig();
	}
	
	/**
	 * (( 242. RESTO_ASSIG → ? EXPRESSION : ASSIGNMENT_EXPRESSION
	 *    244. RESTO_ASSIG → lambda ))
	 * 
	 * 242. RESTO_ASSIG → RESTO_CONDITIONAL
	 * 243. RESTO_ASSIG → op_asignacion ASSIGNMENT_EXPRESSION
	 */
	private void resto_asig() {
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
	 */
	private void expression() {
		parse.add(247);
		assignment_expression();
		resto_exp();
	}
	
	
	/**
	 * 247. RESTO_EXP → , EXPRESSION
	 * 248. RESTO_EXP→ lambda
	 */
	private void resto_exp() {
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.COMA)){
			parse.add(249);
			nextToken();
			expression();
		}	
		else {
			parse.add(250);
		}
	}
	
	/**
	 * 249. EXPRESSIONOPT → EXPRESSION 
	 * 250. EXPRESSIONOPT → lambda
	 */
	private void expressionOpt() {
		if(primeroDeExpression()) {
			parse.add(251);
			expression();
		}
		else
		{
			parse.add(252);
		}
			
		
	}
	
	/**
	 * Metodo que comprueba si el token actual corresponde a un terminal de expression
	 */
	private boolean primeroDeExpression() {
		return (token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)
			 || token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.MULTIPLICACION)
			 || token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.SUMA)
			 || token.esIgual(TipoToken.OP_LOGICO, OpLogico.BIT_AND)
			 || token.esIgual(TipoToken.OP_LOGICO, OpLogico.NOT)
			 || token.esIgual(TipoToken.OP_LOGICO, OpLogico.SOBRERO)
			 || token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.INCREMENTO)	
			 || token.esIgual(TipoToken.OP_ARITMETICO, OpAritmetico.DECREMENTO)
			 || token.esIgual(TipoToken.PAL_RESERVADA, 50) //sizeof
			 || token.esIgual(TipoToken.PAL_RESERVADA, 1) //alignof		
			 || token.esIgual(TipoToken.PAL_RESERVADA, 39) //noexcept
			 || token.esIgual(TipoToken.PAL_RESERVADA, 63) //typeid
			 || tipo()
			 || primary_expression()); 
	}



	
//	/**
//	 * Main de pruebas.
//	 */
//	public static void main(String args[])
//	{
//		String entrada = "#include <Alina.h> \n  #include \"cris.h\" " +
//				"int a=2,b,c; const bool i=3, k=true; int f(); " +
//				"float g=3; double h(){}";
//				String entrada2 = "cout << 4 << \"lo que sea\"<< endl;";
//				String entrada3 = "cin >> b;";
//		AnalizadorLexico a = new AnalizadorLexico(new StringBufferInputStream(entrada));
//		AnalizadorSintactico s = new AnalizadorSintactico(a);
//		for (int i : s.getParse()) 
//			System.out.print(i+" ");
//		
//	}


}