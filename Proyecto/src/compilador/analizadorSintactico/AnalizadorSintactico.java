package compilador.analizadorSintactico;

 
import java.io.StringBufferInputStream;
import java.util.Vector;

import compilador.analizadorLexico.*;
import compilador.analizadorLexico.Token.*;
import compilador.gestionErrores.GestorErrores;
import compilador.tablaSimbolos.*;
import compilador.tablaSimbolos.Tipo.EnumTipo;

@SuppressWarnings("deprecation")
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
	
	
	/**
	 * 1. PROGRAMA → LIBRERIA COSAS MAIN COSAS
	 */
	private void programa() {
		parse.add(1);
		libreria();
		cosas();
		principal(); // TOFIX (alina) creo que no hace falta en el sintactico
		cosas();
	}
	
	/**
	 *  2. LIBRERIA → #include RESTO_LIBRERIA
	 *  3. LIBRERIA -> lambda
	 */
	private void libreria() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ALMOHADILLA)) {
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR) && 
					"include".equals( ((EntradaTS)token.getAtributo()).getLexema() )) {
				nextToken();
				parse.add(2);
				resto_libreria();
			}
			else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Falta la palabra reservada \"include\"");				
			}
		} else {
			parse.add(3);
		}
	}

	/**
	 * 4. RESTO_LIBRERIA → LIT_CADENA LIBRERIA
	 * 5. RESTO_LIBRERIA → <ID.ID> LIBRERIA
	 */
	private void resto_libreria() {
		
		if(token.esIgual(TipoToken.LIT_CADENA))
		{
			nextToken();
			parse.add(4);
			System.out.println("libreria con lit cadena");
			libreria();
		} 
		else if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MENOR))
		{
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR))
			{	
				nextToken();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO)) {
					nextToken();
					
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						nextToken();
						
						if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MAYOR)) {
							nextToken();
							parse.add(4);
							System.out.println("libreria con angulos");
							libreria();
						} else {
							gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
									"Falta el cierre de la declaracion '>'");				
						}
					} else {
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
								"Falta la extension de la libreria");				
					}
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
							"Falta la extension de la libreria");				
				}
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Falta el nombre de la libreria");				
			}	
		} else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Libreria incorrecta, se espera \"libreria.ext\" o <libreria.ext>");				
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
	
	
	private void principal() {
		//TODO
		
	}

	/**	
	 *  8. COSAS → const TIPO ID = LITERAL INIC_CONST ; COSAS
	 *  9. COSAS → TIPO ID COSAS2 COSAS
	 * 10. COSAS → VOID ID ( LISTA_PARAM ) COSAS3 COSAS
	 * 11. COSAS → lambda
	 */
	private void cosas() {
		if(!token.esIgual(TipoToken.EOF)) {
			// 8. COSAS → const TIPO ID = LITERAL INIC_CONST ; COSAS
			if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 9 /*const*/) {
				parse.add(8);
				nextToken();
				if(tipo()) {
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						idConst(); // ID = LITERAL
						inic_const();
						if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
							gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
									"Declaracion de constantes terminada incorrectamente, falta ';'");
						} else {
							nextToken();
							cosas();
						}
					} else {
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
								"Falta el identificador de la constante");	
					}
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
							"Tipo de constante incorrecto");	
				}
			}
			//10. COSAS → VOID ID ( LISTA_PARAM ) COSAS3 COSAS
			else if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 69 /*void*/){
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
							gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta ')'");
						}
					} else {
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta '('");
					}
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
							"Nombre de funcion void incorrecto");	
				}
			}
			// 9. COSAS → TIPO ID COSAS2 COSAS
			else if(tipo()) {
					parse.add(9);
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						id();
						cosas2();	
						cosas();			
					} else {
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
								"Falta el identificador");
					}
			}
			//11. COSAS → lambda
			else {
				parse.add(11);
			}
		}
		
	}
	

	/**	12. COSAS2 → ( LISTA_PARAM ) COSAS3
		13. COSAS2 → [NUM_ENTERO] DIMENSION INIC_DIM ;
		14. COSAS2 → INICIALIZACION  DECLARACIONES ;
	*/
	private void cosas2() {
		// COSAS2 → ( LISTA_PARAM ) COSAS3
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) {
			parse.add(12);
			nextToken();
			lista_param();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
				nextToken();
				cosas3();
			} else {
				//error
				System.err.print(" error 12 ");
			}
		} 
		else if(token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_CORCHETE)){
			parse.add(13);
			nextToken();
			if(token.esIgual(TipoToken.NUM_ENTERO )){
				nextToken();
				if(token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_CORCHETE)){
					nextToken();
					dimension();
					inicDim();
					nextToken();
					if(token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA))
						nextToken();
					else
						System.err.println("Regla 13. Falta ';'");
				}
				else{
					System.err.print("Regla 13");
				}
			}
			else{
				System.err.print("Regla 13");
			}
		}
		else {
			// COSAS2 → INICIALIZACION  DECLARACIONES ;
			parse.add(14);
			inicializacion();
			declaraciones();		
			if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				//error
				System.err.print(" error 14 ");
			}
			nextToken();
		}
	}
	

	/**	15. COSAS3 → ;
		16. COSAS3 → { CUERPO }
	*/
	private void cosas3() {
		
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
			parse.add(15);
			nextToken();
			System.out.println("cabecera funcion " + entradaTS.getLexema());
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			parse.add(16);
			nextToken();
			cuerpo();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
				System.out.println("funcion con cuerpo " + entradaTS.getLexema());
				nextToken();
			} else {
				// error
				System.err.print(" error 16 ");
			}
		}
		
	}

	/**	
	 * 17. LISTA_PARAM → TIPO ID RESTO_LISTA 
	 * 18. LISTA_PARAM → lambda
	 */
	private void lista_param() {
		if(tipo()){
			parse.add(17);
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				id();
				restoLista();
			}
		}
		else{
			parse.add(18);
		}
	} 

	
	/** 
	 * 19. RESTO_LISTA → , LISTA_PARAM 
	 * 20. RESTO_LISTA → lambda
	 */
	private void restoLista() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(19);
			nextToken();
			lista_param();
		}
		else{
			parse.add(20);
		}
	}


	/**	21. DIMENSION → [ NUM_ENTERO ] DIMENSION
		22. DIMENSION → lambda
	 */
	private void dimension() {
		if(!token.esIgual(null)){
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_CORCHETE)) {
				parse.add(21);
				nextToken();
				if(token.esIgual(TipoToken.NUM_ENTERO)){
					nextToken();
					if(token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_CORCHETE)){
						nextToken();
						dimension();
					}
				}
			}
			else{
				System.err.print("Regla 21");
			}
		}
		else{
			parse.add(22);
		}
		
	}

	/**	23. INIC_DIM → = INIC_DIM2 
		24. INIC_DIM → lambda
	 */
	private void inicDim() {
		if(!token.esIgual(null)){
			if(token.esIgual(TipoToken.OP_ASIGNACION, OpAsignacion.ASIGNACION)){
				parse.add(23);
				nextToken();
				inicDim2();
			}
			else{
				System.err.print("Regla 24");
			}
		}
		else{
			parse.add(24);
		}
	}


	/**	25. INIC_DIM2 → { INIC_DIM3 }
	 */
	private void inicDim2() {
		if(!token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_LLAVE)){
			System.err.print("Regla 25");
		}
		else{
			parse.add(25);
			nextToken();
			inicDim3();
			//nextToken();
			if(!token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)){
				System.err.print("Regla 25");
			}
			else{
				nextToken();
			}
		}
	}

	/**	26. INIC_DIM3 → LITERAL INIC_DIM4 
		27. INIC_DIM3 → INIC_DIM2 INIC_DIM5
	 */
	private void inicDim3() {
		boolean esLiteral=false;
		
		if(!token.esIgual(TipoToken.EOF)){
			if(esLiteral=literal()){ // El metodo literal() lee el siguiente token
				parse.add(26);
				inicDim4();
			}
			else{
				parse.add(27);
				inicDim2();
				inicDim5();
			}
		}
		
	}

	/**	28. INIC_DIM4 → , LITERAL INIC_DIM4 
		29. INIC_DIM4 → lambda
	 */
	private void inicDim4() {
		boolean esLiteral = false;
		if(!token.esIgual(null)){
			if(token.esIgual(TipoToken.SEPARADOR, Separadores.COMA)){
				parse.add(28);
				nextToken();
				if(!(esLiteral=literal())){
					System.err.print("Regla 28");
				}
				else{
					inicDim4();
				}
			}
			else{
				System.err.print("Regla 28");
			}
		}
		else{
			parse.add(29);
		}
		
	}

	/**	28. INIC_DIM5 → , INIC_DIM2 INIC_DIM5
		29. INIC_DIM5 → lambda 
	 */
	private void inicDim5() {
		if(!token.esIgual(null)){
			if(token.esIgual(TipoToken.SEPARADOR, Separadores.COMA)){
				parse.add(28);
				nextToken();
				inicDim2();
				inicDim5();
			}
			else{
				System.err.print("Regla 28");
			}
		}
		else{
			parse.add(29);
		}
	}

	/**
	 * 30. INIC_CONST → , ID = LITERAL INIC_CONST
	 * 31. INIC_CONST → lambda
	 */
	private void inic_const() {
		System.out.println("declaracion constante " + entradaTS.getLexema());
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(30);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				idConst(); // ID = LITERAL
				inic_const();
			} else {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Declaracion de constantes incorrecta, se espera un identificador");
			}	
		}
		else { ///Si es lambda
			parse.add(31);
		}	
	}

	
	/**
	 * 32. DECLARACIONES → , ID INICIALIZACION DECLARACIONES
	 * 33. DECLARACIONES → lambda
	 */
	private void declaraciones() {
		System.out.println("declaracion variable " + entradaTS.getLexema());
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(32);
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
			parse.add(33);
		}
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
	 * 34. INICIALIZACION → = valor
	 * 35. INICIALIZACION → lambda
	 */
	
	/*	34. INICIALIZACION → = LITERAL
		35. INICIALIZACION → ℷ
	 */
	private void inicializacion() {
		if(token.esIgual(TipoToken.OP_ASIGNACION,OpAsignacion.ASIGNACION)) {
			parse.add(34);
			nextToken();
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("inicializacion variable " + entradaTS.getLexema() + " con " + valor);
			nextToken();
		} else {
			parse.add(35);
		}
	}

	/**
	 * 37. INSTRUCCION → INS_FUNCION
	 * 38. INSTRUCIION → INS_REGISTRO
	 * 39. INSTRUCCION → INS_LECTURA
	 * 40. INSTRUCCION → INS_ESCRITURA
	 * 41. INSTRUCCION → INS_ASIGNACION
	 * 42. INSTRUCCION → INS_DECLARACION
	 * 43. INSTRUCCION → ;
	 */

	private boolean instruccion() {
		//Esto no está bien, tengo que ver como implementarlo...
		//Yo en el caso de COSAS lo puse con ifs -> if(ins_funcion) parse.add(37) else if(..) parse.add(38) etc
		//aunque no estoy muy segura de que sea asi (Cris)
		//ya, pero no tengo muy claras las condiciones de los ifs, jeje! :) pero graciasss!!!
		if(token.esIgual(TipoToken.IDENTIFICADOR)) { 
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) { //INS_FUNCION
				parse.add(37);
				nextToken();
				lista_atb();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
					System.out.println("llamada a funcion " + entradaTS.getLexema());
					nextToken();
				} else {
					// error
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta el separador abre llave {");				
				}
			} else if (token.esIgual(TipoToken.OP_ASIGNACION)) { //INS_ASIG
				parse.add(41);
				nextToken();
				//llamada a EXPRESION
			} else {
				//error
			}
		} else if (token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 54){ //INS_REG
			parse.add(38);
			nextToken();
			ins_reg();
		} else if (token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 74){ //INS_LECT
			parse.add(39);
			nextToken();
			ins_lect(); 
		} else if (token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 75){ //INS_ESC
			parse.add(40);
			nextToken();
			ins_esc(); 
		} else if (tipo()) { //INS_DECL
			parse.add(0000);
			nextToken();
			ins_decl();
		} else if (token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 9){ //INS_DECL
			parse.add(00000);
			nextToken();
			ins_decl2();
		} else {
			//error
			return false;
		}
		
		return true;
	}
	
	
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
	 * 45. LISTA_ATB → ATRIBUTO RESTO_ATB
	 * 46. LISTA_ATB → lambda
	 */
	private void lista_atb() {							//TOFIX aqui no habria que meter algun error?
		if(!token.esIgual(TipoToken.EOF)) {
			parse.add(45);
			atributo();
			resto_atb();
		} else {
			// 26. LISTA_ATB → lambda 
			parse.add(46);
		}
	}
	
	/**
	 * 47. RESTO_ATB → , ATRIBUTO RESTO_ATB
	 * 48. RESTO_ATB → lambda
	 */
	private void resto_atb() {							//TOFIX aqui no habria que meter algun error?
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(47);
			nextToken();
			atributo();
			resto_atb();
		} else {
			parse.add(48);
		}
	}
	
	/**
	 * 49. ATRIBUTO → LITERAL
	 * 50. ATRIBUTO → ID
	 */
	private void atributo() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(50);
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
		} else if(esLiteral()){
			parse.add(49);
			//no se si hay que hacer algo mas...
			nextToken();
		} else {
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta identificador o literal que identifique al atributo");
		}
	}
	
	
	
	/**
	 * 51. INS_REGISTRO → struct RESTO_ST
	 */
	private void ins_reg() {
		//if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 54){
			parse.add(51);
			nextToken();
			resto_st();
	//	} else {
			// error
	//		gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Falta la palabra reservada \"struct\"");
		//}
	}
	
	/**
	 * 52. RESTO_ST → ID { CUERPO_ST } ID NOMBRES
	 * 53. RESTO_ST → { CUERPO_ST } ID NOMBRES
	 */
	private void resto_st() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)){ 
			parse.add(52);
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
			parse.add(53);
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
	 * 54. CUERPO_ST → TIPO ID RESTO_VAR CUERPO_ST
	 * 55. CUERPO_ST → lambda
	 */
	private void cuerpo_st() {
		if(tipo()) {
			parse.add(54);
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
			parse.add(55);
		
	}
	
	
	/**
	 * 56. RESTO_VAR → , ID RESTO_VAR
	 * 57. RESTO_VAR → ;
	 */
	private void resto_var() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)){ 
			parse.add(56);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
				nextToken();
				resto_var();
			} else {
				// error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Se deben identificar todos los atributos");
			}
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
			parse.add(57);
			nextToken();
		} else {
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Se esperaba un separador coma (,) o punto_coma (;)");
		}
	}
	
	
	/**
	 * 58. NOMBRES → , ID NOMBRES
	 * 59. NOMBRES → ;
	 */
	private void nombres() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)){ 
			parse.add(58);
			nextToken();
			if(token.esIgual(TipoToken.IDENTIFICADOR)){
				tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
				nextToken();
				nombres();
			} else {
				// error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Se deben identificar todas las variables de la estructura");
			}
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
			parse.add(59);
			nextToken();
		} else {
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), "Se esperaba un separador coma (,) o punto_coma (;)");
		}
	}
	
	
	
	
	/**
	 * 60. INS_LECTURA → >>  RESTO_LECT 
	 */
	private void ins_lect() { //TODO: he cambiado instruccion, por lo que necesito quitar de aqui la condicion del if :) el error lo contemplo arriba
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MAYORES)){
			parse.add(60);
			nextToken();
			resto_lect();
		}
		else{
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Lectura incorrecta, se esperaba el operador \">>\"");
			//System.err.print(" error 60 ");
		}
	//	}
	//	else{
			// error
	//		gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
		//			"Se esperaba la palabra reservada \"cin\"");
			//System.err.print(" error 60 ");
	//	}
	}
	
	/**
	 * 61. RESTO_LECT → ID ;
	 * 62. RESTO_LECT → LITERAL  ;
	 */
	private void resto_lect() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)){ //FALTA ALGO MAS AQUI???
			parse.add(61);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
				nextToken();
			}
			else{
				// error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Lectura terminada incorrectamente, falta ';'");
				//System.err.print(" error 61 ");
			}
		}
		else if(esLiteral()){
			parse.add(62);
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
				nextToken();	
			}
			else{
				// error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Lectura terminada incorrectamente, falta ';'");
				//System.err.print(" error 62 ");
			}
		}
		else{
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Lectura incorrecta, se esperaba un literal o una variable");
			//System.err.print(" error 62 ");
		}
	}
	
	/**
	 * 63. INS_ESCRITURA → << RESTO_ESC
	 */
	private void ins_esc() { //TODO: he cambiado instruccion, por lo que necesito quitar de aqui la condicion del if :) el error lo contemplo arriba
		//if(token.esIgual(TipoToken.PAL_RESERVADA,75)){ // Palabra reservada cout
		//	parse.add(63);
		//	nextToken();
			if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MENORES)){
				parse.add(63);
				nextToken();
				resto_esc();
			}
			else{
				// error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Escritura incorrecta, se esperaba el operador \"<<\"");
				//System.err.print(" error 63 ");
			}
		//}
	//	else{
			// error
	//		gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
		//			"Se esperaba la palabra reservada \"cout\"");
			//System.err.print(" error 63 ");
	//	}
	}
	
	/**
	 * 64. RESTO_ESC →  LITERAL INS_ESCRITURA2
	 * 65. RESTO_ESC →  ID INS_ESCRITURA2
	 * 66. RESTO_ESC → endl INS_ESCITURA2
	 */
	private void resto_esc() {
		if(esLiteral()){
			parse.add(64);
			nextToken();
			ins_esc2();
		}
		else if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(65);
			nextToken();
			ins_esc2();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,76)){ // Palabra reservada endl 
			parse.add(66);
			nextToken();
			ins_esc2();
		}
		else{
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Escritura incorrecta, se esperaba un literal, una variable o la palabra reservada \"endl\"");
			//System.err.print(" error 66 ");
		}
	}
	
	/**
	 * 67. INS_ESCRITURA2 →  << RESTO_ESC2
	 * 68. INS_ESCRITURA2 →  ;
	 */
	private void ins_esc2(){
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MENORES)){
			parse.add(67);
			nextToken();
			resto_esc2();
		}
		else if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
			parse.add(68);
			nextToken();
		}
		else{
			// error
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Escritura terminada incorrectamente, falta ';'");
			//System.err.print(" error 68 ");
		}
	}
	
	
	/**
	 * 69. RESTO_ESC2 → LITERAL  INS_ESCRITURA2 
	 * 70. RESTO_ESC2 → ID  INS_ESCRITURA2  
	 * 71. RESTO_ESC2 → endl INS_ESCRITURA2 
	 * 72. RESTO_ESC2 → lamdba
	 */
	private void resto_esc2() {
		if(esLiteral()){
			parse.add(69);
			nextToken();
			ins_esc2();
		}
		else if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(70);
			nextToken();
			ins_esc2();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA,76)){ // Palabra reservada endl 
			parse.add(71);
			nextToken();
			ins_esc2();
		}
		else{
			// Lambda...
			parse.add(72);
		}
	}
	
	
	/**
	 * 
	 */
	private void ins_asig() {
		
	}
	
	/**
	 * 
	 */
	private void ins_decl() {
		
	}
	
	/**
	 * 
	 */
	private void ins_decl2() {
		
	}
	
	
	/**
	 * 
	 */
	private void ins_vacia() {
		
	}
	

	/**
	 * 73-83 aun no estan terminadas!
	 * 73. INS_ASIGNACION → ID  OP_ASIGNACION EXPRESION
	 */
	private void ins_asignacion()
	{
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(73);
			//tipo = null;//((EntradaTS)token.getAtributo()).getLexema();//TOFIX obtener enumerado tipo de la variable declarada
			nextToken();
			//expresion();
		} 
		
	}
	/**
	 * 	74. CUERPO → INSTRUCCION CUERPO 
		75. CUERPO → SENT_BUCLE CUERPO
		76. CUERPO → SENT_IF CUERPO
		77. CUERPO → SENT_CASE CUERPO
		78. CUERPO → lambda
	 */
	private void cuerpo()
	{
		if(instruccion())
		{
			parse.add(74);
			cuerpo();
		}
		else if(sent_bucle())
		{
			parse.add(75);
			cuerpo();
		}
		else if(sent_if())
		{
			parse.add(76);
			cuerpo();
		}
		else if(sent_if())
		{
			parse.add(77);
			cuerpo();
		}
	}
	

	/** 
	 *  79. CUERPO2 → INSTRUCCION
		80. CUERPO2 → SENT_BUCLE
		81. CUERPO2 → SENT_IF
		82. CUERPO2 → SENT_CASE 
		83. CUERPO2 → { CUERPO }
	 */
	private void cuerpo2() {
		if(instruccion())
		{
			parse.add(79);
		}
		else if(sent_bucle())
		{
			parse.add(80);
		}
		else if(sent_if())
		{
			parse.add(81);
		}
		else if(sent_case())
		{
			parse.add(82);
		}
		else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			nextToken();
			cuerpo();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
				parse.add(83);
				nextToken();
			}
			else
				System.out.println("ERROR EN REGLA 83. FALTA PARENTESIS DE CIERRE");
		}
		
	}
	
	/** TODO ¡¡¡¡¡SIN TERMINAR!!!
	 * SENT_CASE → switch ( ID ) { CUERPO_CASE }
	 */
	private boolean sent_case() {
		if(token.esIgual(TipoToken.PAL_RESERVADA,55)) //Palabra reservada switch
		{
			parse.add(-1); //Añadir numero de regla cuando se sepa
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
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
									System.err.println("error en switch");
							}
							else
								System.err.println("error en switch");
						}
						else
							System.err.println("error en switch");
					}
				 else
					 System.err.println("error en switch");
				
			}
			else
				System.err.println("error en switch");
			
		}
		return false;
	}

	/** TODO ¡¡¡¡¡SIN TERMINAR!!!
	 * CUERPO_CASE  → case LITERAL: CUERPO  |  CUERPO2
	 * mas expresiones: defalut: break; goto identifier ; continue ;
	 */
	private void cuerpo_case() {
		if(token.esIgual(TipoToken.PAL_RESERVADA,6)) //Palabra reservada case
		{
			parse.add(-1); //Añadir numero de regla cuando se sepa
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
				System.err.println("error en case");
		}
	}
	/**  TODO ¡¡¡¡¡SIN TERMINAR!!!
	 *  SENT_IF → if ( EXPRESION )  CUERPO2 SENT_ELSE
	 */
	private boolean sent_if() {
		if(token.esIgual(TipoToken.PAL_RESERVADA,32)) //Palabra reservada if
		{
			parse.add(-1); //Añadir numero de regla cuando se sepa
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
				nextToken();
				expresion();// ??? Poner cuando se tenga
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
					nextToken();
					cuerpo2();
					sent_else();
					return true;
				}
				else
					System.err.print(" error en IF: Falta parentesis de cierre ");
			}
			else
				System.err.print(" error en IF: Falta parentesis de apertura ");
		}
		return false;
	}
	/** TODO ¡¡¡¡¡SIN TERMINAR!!!
	 * SENT_ELSE → else CUERPO2 | lambda
	 */
	private void sent_else() {
		if(token.esIgual(TipoToken.PAL_RESERVADA,22)) //Palabra reservada else
		{
			parse.add(-1); //añadir el numero de regla cuando se sepa
			nextToken();
			cuerpo2();
		}
		else //SENT_ELSE -> lambda
			parse.add(-1); //añadir el numero de regla cuando se sepa
		
	}

	/** TODO ¡¡¡¡¡SIN TERMINAR!!!
	 *  SENT_BUCLE → while (CONDICION) do CUERPO2
	 *  SENT_BUCLE → for ( INDICE; CONDICION; CAMBIO) CUERPO2 
	 *  SENT_BUCLE → do CUERPO2 while (CONDICION);
	 */
	private boolean sent_bucle() {
		
		/** SENT_BUCLE → while (CONDICION) do CUERPO2*/
		if(token.esIgual(TipoToken.PAL_RESERVADA,72)) //Palabra reservada while
		{
			parse.add(-1); //Añadir numero de regla cuando se sepa
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
				nextToken();
				//condicion();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
					nextToken();
					if(token.esIgual(TipoToken.PAL_RESERVADA,19)) //Palabra reservada do
					{
						nextToken();
						cuerpo2();
						return true;
					}
					else
						System.err.println("error en while");
				}
				else
					System.err.println("error en while");
			}
			else
				System.err.println("error en while");
		}	
		/** SENT_BUCLE → for ( INDICE; CONDICION; CAMBIO) CUERPO2 */
		else if(token.esIgual(TipoToken.PAL_RESERVADA,29)) //Palabra reservada for
		{
			parse.add(-1); //Añadir numero de regla cuando se sepa
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
				nextToken();
				//indice();
				// ....
			}
			else
				System.err.println("error en for");
		}
		/** SENT_BUCLE → do CUERPO2 while (CONDICION);*/
		else if(token.esIgual(TipoToken.PAL_RESERVADA,19)) //Palabra reservada do
		{
			parse.add(-1); //Añadir numero de regla cuando se sepa
			nextToken();
			cuerpo2();
			if(token.esIgual(TipoToken.PAL_RESERVADA,72)) //Palabra reservada while
			{
				nextToken();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
					nextToken();
					//condicion();
					if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
						nextToken();
						if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
							nextToken();
							return true;
						}	
						else
							System.err.println("error");
						
					}
					else
						System.err.println("error");
				}
				else
					System.err.println("error");
			}
			else
				System.err.println("error");
			
		}	
		return false;
	}
	
	/**
	 * 84. EXPRESION → EXPRESIONNiv1 RESTO_EXPR
	 * 85. RESTO_EXPR → OpNiv0 EXPRESION
	 * 86. RESTO_EXPR → lambda
	 */
	private void expresion() {
		parse.add(84);
		expresionNiv1();
		//restoExpr
		if(token.esOpNiv0()) {
			parse.add(85);
			lexico.scan();
			expresion();
		} else {
			parse.add(86);
		}
	}
	
	/**
	 * 87. EXPRESIONNiv1 → EXPRESIONNiv2 RESTO_EXPR1
	 * 88. RESTO_EXPR1 → OpNiv1 EXPRESIONNiv1
	 * 89. RESTO_EXPR1 → lambda
	 */
	private void expresionNiv1() {
		parse.add(87);
		expresionNiv2();
		//restoExpr1
		if(token.esOpNiv1()) {
			parse.add(88);
			lexico.scan();
			expresionNiv1();
		} else {
			parse.add(86);
		}
	}

	/**
	 * 90. EXPRESIONNiv2 → EXPRESIONNiv3  RESTO_EXPR2
	 * 91. RESTO_ESPR2 → OpNiv2 EXPRESIONNiv2
	 * 92. RESTO_ESPR2 → lambda
	 */
	private void expresionNiv2() {
		parse.add(90);
		expresionNiv3();
		//restoExpr2
		if(token.esOpNiv2()) {
			parse.add(91);
			lexico.scan();
			expresionNiv2();
		} else {
			parse.add(92);
		}
	}
	
	/**
	 * 93. EXPRESIONNiv3 → EXPRESIONNiv4 RESTO_EXPR3
	 * 94. RESTO_EXPR3 → OpNiv3 EXPRESIONNiv3
	 * 95. RESTO_EXPR3 → lambda
	 */
	private void expresionNiv3() {
		parse.add(93);
		expresionNiv4();
		//restoExpr3
		if(token.esOpNiv3()) {
			parse.add(94);
			lexico.scan();
			expresionNiv3();
		} else {
			parse.add(95);
		}
	}
	
	/**
	 * 96. EXPRESIONNiv4 → OpNiv4 EXPRESIONNiv4
	 * 97. EXPRESIONNiv4 → ( RESTO_EXP4
	 * 98. RESTO_EXPR4 → TIPO ) EXPRESIONNiv4
	 * 99. RESTO_EXPR4 → EXPRESION )
	 */
	private void expresionNiv4() {
		if(token.esOpNiv4()) {
			parse.add(96);
			lexico.scan();
			expresionNiv4();
		} else if(token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)){
			parse.add(97);
			lexico.scan();
			//restoExpr4
			if(tipo()) {
				parse.add(98);
				if(token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS)){
					lexico.scan();
					expresionNiv4();
				} else {
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), 
							"Expresion no terminada correctamente: falta ')'");
				}
			} else {
				parse.add(99);
				expresion();
				if(!token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS))
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), 
							"Expresion no terminada correctamente: falta ')'");
			}
			
		}
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