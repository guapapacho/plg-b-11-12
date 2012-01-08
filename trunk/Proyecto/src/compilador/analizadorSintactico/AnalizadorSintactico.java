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

	
	
	
	private void principal() {
		//TODO
		
	}

	/**	
	 *  8. COSAS → const TIPO ID = LITERAL INIC_CONST ; COSAS
	 *  9. COSAS → TIPO ID COSAS2 COSAS
	 * 10. COSAS → VOID ID ( LISTA_PARAM ) COSAS3 COSAS
	 * 11. COSAS → enum ID { LISTANOMBRES } ; COSAS
	 * 12. COSAS → struct RESTO_ST COSAS
	 * 13. COSAS → lambda
	 */
	private void cosas() {
		if(!token.esIgual(TipoToken.EOF)) {
			// 8. COSAS → const TIPO ID = LITERAL INIC_CONST ; COSAS
			//if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 9 /*const*/) {
			if(token.esIgual(TipoToken.PAL_RESERVADA, 9 /*const*/ )){
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
			//else if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 69 /*void*/){
			if(token.esIgual(TipoToken.PAL_RESERVADA, 69 /*void*/ )){
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
			//13. COSAS → lambda
			else {
				parse.add(13);
			}
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
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta ')'");
			}
		} 
		//17. COSAS2 → INICIALIZACION  DECLARACIONES ;
		else {
			parse.add(17);
			inicializacion();
			declaraciones();		
			if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta ';'");
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
			/*if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
				System.out.println("funcion con cuerpo " + entradaTS.getLexema());
				nextToken();
			} else {
				// error
				System.err.print(" error 16 ");
			}*/
		}
		else {
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Se esperaba ';' o '{' ");
		}
		
	}

	/**	
	 * 20. LISTA_PARAM → TIPO ID RESTO_LISTA 
	 * 21. LISTA_PARAM → lambda
	 */
	private void lista_param() {
		if(tipo()){
			parse.add(20);
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				id();
				restoLista();
			}
		}
		else{
			parse.add(21);
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
			//System.err.print("Regla 25");
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Inicializacion de array incorrecta, se esperaba '{' ");
		}
		else{
			parse.add(28);
			nextToken();
			inicDim3();
			if(!token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)){
				//System.err.print("Regla 25");
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Inicializacion de array incorrecta, se esperaba '}' ");
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
		
	}

	/**	31. INIC_DIM4 → , LITERAL INIC_DIM4 
		32. INIC_DIM4 → lambda
	 */
	private void inicDim4() {
		if(token.esIgual(TipoToken.SEPARADOR, Separadores.COMA)){
			parse.add(31);
			nextToken();
			if(!literal()){
				//System.err.print("Regla 28");
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
				idConst(); // ID = LITERAL
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
			nextToken();
			//Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			//System.out.println("inicializacion variable " + entradaTS.getLexema() + " con " + valor);
			if(esLiteral()){
				parse.add(39);
				nextToken();
			}
			else{
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Inicialización incorrecta, se esperaba un literal");
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
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),"Falta ']'");
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
			tipo = new Tipo(EnumTipo.DEFINIDO, ((EntradaTS)token.getAtributo()).getLexema());
			nextToken();
			return instruccion2();
			/*if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) { //INS_FUNCION
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
			}*/
		//} else if (token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 54){ //INS_REG
		}else if(token.esIgual(TipoToken.PAL_RESERVADA, 54 /*struct*/ )){ //INS_REG
			parse.add(43);
			nextToken();
			//ins_reg();
			resto_st();
		//} else if (token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 74){ //INS_LECT
		}else if(token.esIgual(TipoToken.PAL_RESERVADA, 74 /*cin*/ )){ //INS_LECT
			parse.add(44);
			nextToken();
			ins_lect();
		//} else if (token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 75){ //INS_ESC
		}else if(token.esIgual(TipoToken.PAL_RESERVADA, 75 /*cout*/ )){ //INS_ESC
			parse.add(45);
			nextToken();
			ins_esc(); 
		//} else if (token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 9){ //INS_DEC
		}else if(token.esIgual(TipoToken.PAL_RESERVADA, 9 /*const*/ )){ //INS_DEC
			parse.add(46); // MODIFICAR!!!!!
			nextToken();
			ins_dec();
		} else if (tipo()) { //INS_DEC2
			parse.add(47); 
			nextToken();
			ins_dec2();
		} if (token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA)) { //INS_VACIA
			parse.add(48);
			nextToken();
		} else {
			//error
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
			} else {
				// error
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(), 
						"Falta el separador abre llave {");
				return false;
			}
		} else if (token.esIgual(TipoToken.OP_ASIGNACION)) { //INS_ASIG
			parse.add(50);
			nextToken();
			//llamada a EXPRESION
		} else {
			//error
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
	 * 87. CUERPO --> INSTRUCCION CUERPO 
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
		else {
			parse.add(86);
			if(instruccion())
				cuerpo();
			else
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Token inesperado... ");
		}
		/*else //cuerpo --> lambda
			parse.add(80);*/
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
		else  if(token.esIgual(TipoToken.PAL_RESERVADA,32 /*if*/))
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
	
	
	/** TODO ¡¡¡¡¡SIN TERMINAR!!!
	 *  94. RESTO_WHILE --> (CONDICION) do CUERPO2
	 */
	private void resto_while() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(94);
			nextToken();
			//condicion(); !!!!!!!!!!!!!
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
				cuerpo2();
			}
			else
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Lectura terminada incorrectamente, falta ')'");
		}
		else
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Lectura terminada incorrectamente, falta '('");
	}		

	/** TODO ¡¡¡¡¡SIN TERMINAR!!!
	 * 95. RESTO_DO --> CUERPO2 while (CONDICION);
	 */
	private void resto_do() {
		parse.add(95); 
		cuerpo2();
		if(token.esIgual(TipoToken.PAL_RESERVADA,72 /*while*/))	{
			nextToken();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
				nextToken();
				//condicion(); !!!!!!!!!!!
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
					nextToken();
					if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
						nextToken();
					}	
					else
						gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
						"Lectura terminada incorrectamente, falta ';'");
					
				}
				else
					gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
					"Lectura terminada incorrectamente, falta ')'");
			}
			else
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
				"Lectura terminada incorrectamente, falta '('");
		}
		else
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
			"Lectura terminada incorrectamente, falta palabra 'while'");
		
	}	
		
	/** TODO ¡¡¡¡¡SIN TERMINAR!!!
	 * 96. RESTO_FOR --> ( INDICE; CONDICION; CAMBIO) CUERPO2 
	 */
	private void resto_for() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(96);
			nextToken();
			//indice();
			// ....
		}
		else
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
			"Lectura terminada incorrectamente, falta '('");
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
	/**  TODO ¡¡¡¡¡SIN TERMINAR!!!
	 *  99. RESTO_IF --> ( CONDICION )  CUERPO2 SENT_ELSE
	 */
	private boolean resto_if() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)){
			parse.add(99);
			nextToken();
			//expresion();   ???
			//condicion();   TODO: Poner cuando se tenga
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)){
				nextToken();
				cuerpo2();
				sent_else();
				return true;
			}
			else
				gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
				"Lectura terminada incorrectamente, falta ')'");
		}
		else
			gestorErr.insertaErrorSintactico(lexico.getLinea(), lexico.getColumna(),
			"Lectura terminada incorrectamente, falta '('");
	
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