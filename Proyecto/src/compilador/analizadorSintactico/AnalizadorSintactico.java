package compilador.analizadorSintactico;


import java.io.StringBufferInputStream;
import java.util.Vector;

import compilador.analizadorLexico.*;
import compilador.analizadorLexico.Token.TipoToken;
import compilador.analizadorLexico.Token.*;
import compilador.gestionErrores.GestorErrores;
import compilador.tablaSimbolos.*;
import compilador.tablaSimbolos.EntradaTS.Tipo;

@SuppressWarnings("deprecation")
public class AnalizadorSintactico {

	/** Token actual obtenido mediante el Analizador lexico */
	private Token token;
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
		token = lexico.scan();
		gestorTS = GestorTablasSimbolos.getGestorTS();
		gestorErr = GestorErrores.getGestorErrores();
		tipo = null;
		entradaTS = null;
		programa();
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
			token = lexico.scan();
			if(token.esIgual(TipoToken.IDENTIFICADOR) && 
					"include".equals( ((EntradaTS)token.getAtributo()).getLexema() )) {
				token = lexico.scan();
				parse.add(2);
				resto_libreria();
			}
			else {
			//error sintactico
				
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
			token = lexico.scan();
			parse.add(4);
			System.out.println("libreria con lit cadena");
			libreria();
		} 
		else if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MENOR))
		{
			token = lexico.scan();
			if(token.esIgual(TipoToken.IDENTIFICADOR))
			{	
				token = lexico.scan();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO)) {
					token = lexico.scan();
					
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						token = lexico.scan();
						
						if(token.esIgual(TipoToken.OP_COMPARACION, OpComparacion.MAYOR)) {
							token = lexico.scan();
							parse.add(4);
							System.out.println("libreria con angulos");
							libreria();
						}	
						else //error
						{}	
					}
					else //error
					{}
				}	
				else //error
				{}
			}
			else //error
			{}	
		}
		else //error
		{System.err.print(" error 3 libreria ");}	
		
	}
	
	
	/**
	 * 6. TIPO → ID										
	 * 7. TIPO → TIPO_SIMPLE
	 */
	private boolean tipo() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(6);
			tipo = null;//((EntradaTS)token.getAtributo()).getLexema();//TOFIX obtener enumerado tipo de la variable declarada
			token = lexico.scan();
			return true;
		} else if(token.esIgual(TipoToken.PAL_RESERVADA)){
			parse.add(7);
			switch((Integer)token.getAtributo()){
			// no se si hace falta mirar si es un tipo valido
			}
			token = lexico.scan();
			return true;
		} else {
			// error
			System.err.print(" error 7 ");
			return false;
		}
	}
	
	
	/**
	 Metodo que devuelve el LITERAL y lee el siguiente token
	 */
	private boolean literal() {
		entradaTS = (EntradaTS)token.getAtributo();
		entradaTS.setTipo(tipo);
		entradaTS.setConstante(false);
		
		if(token.esIgual(TipoToken.LIT_CADENA)) {
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("LITERAL CADENA: " + valor);
			token = lexico.scan();
			return true;
		}
		else if(token.esIgual(TipoToken.LIT_CARACTER)){
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("LITERAL CARACTER: " + valor);
			token = lexico.scan();
			return true;
		} 
		else if (token.esIgual(TipoToken.NUM_ENTERO)){
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("NUMERO ENTERO: " + valor);
			token = lexico.scan();
			return true;
		}
		else if (token.esIgual(TipoToken.NUM_REAL)){
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("NUMERO REAL: " + valor);
			token = lexico.scan();
			return true;
		}
		else if (token.esIgual(TipoToken.NUM_REAL_EXPO)){
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("NUMERO REAL EXPO: " + valor);
			token = lexico.scan();
			return true;
		}
		else {
			//error
			System.err.print(" error const ");
			return false;
		}		
	}
	
	private void principal() {

		
	}

	/**	8. COSAS → const TIPO ID = LITERAL INIC_CONST ; COSAS
 		9. COSAS → TIPO ID COSAS2 COSAS
		10. COSAS → VOID ID ( LISTA_PARAM ) COSAS3 COSAS
		11. COSAS → ℷ
		
	 	*/
	private void cosas() {
		boolean esLiteral=false;
		
		if(!token.esIgual(TipoToken.EOF)) {
			if (!token.esIgual(null)){ //Y esto?? Yo creo que si es lambda no tiene por que ser token null. Puede que este usando otra regla, no? (Cris)
				
				// 8. COSAS → const TIPO ID = LITERAL INIC_CONST ; COSAS
				if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 9 /*const*/) {
					parse.add(8);
					token = lexico.scan();
					tipo(); /// Para el siguiente true el metodo tipo() debria devolver si es un tipo valido
					//if(true) {//TODO tipo correcto -> NO HACE FALTA, ES EL SEMANTICO EL QUE COMPRUEBA SI ES CORRECTO EL TIPO
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						//idConst(); // ID = valor
						id();
						if (token.esIgual(TipoToken.OP_ASIGNACION, OpAsignacion.ASIGNACION)){
							if(esLiteral=literal()){  // ID = LITERAL
								inic_const();
							}
						}else{
							System.err.print("Regla 8");
						}
						if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
							//error
							System.err.print(" error 8 ");
						} else {
							token = lexico.scan();
							cosas();
						}
					}
					//}
				}/////////////////// PAL_RESERVADA
				//10. COSAS → VOID ID ( LISTA_PARAM ) COSAS3 COSAS
				else if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 69 /*void*/){
					parse.add(10);
					token = lexico.scan();
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						token = lexico.scan();
						if(token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS)){
							token = lexico.scan();
							lista_param();
							if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
								token = lexico.scan();
								cosas3();
								cosas();
							}
						}
					}
				}
				else {// 9. COSAS → TIPO ID COSAS2 COSAS
				tipo();
				//if(true) {//TODO tipo correcto
					parse.add(9);
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						id();
						cosas2();	
						cosas();			
					} else {
						// error sintáctico
						System.err.print(" error 9 ");
					}
				}
					
			}//////////////////////////////////////////////// !lambda
			else{//Si es lambda
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
			token = lexico.scan();
			lista_param();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
				token = lexico.scan();
				cosas3();
			} else {
				//error
				System.err.print(" error 12 ");
			}
		} 
		else if(token.esIgual(TipoToken.SEPARADOR, Separadores.ABRE_CORCHETE)){
			parse.add(13);
			token = lexico.scan();
			if(token.esIgual(TipoToken.NUM_ENTERO )){
				token = lexico.scan();
				if(token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_CORCHETE)){
					token = lexico.scan();
					dimension();
					inicDim();
					token = lexico.scan();
					if(token.esIgual(TipoToken.SEPARADOR, Separadores.PUNTO_COMA))
						token = lexico.scan();
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
			token = lexico.scan();
		}
	}
	

	/**	15. COSAS3 → ;
		16. COSAS3 → { CUERPO }
	*/
	private void cosas3() {
		
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
			parse.add(15);
			token = lexico.scan();
			System.out.println("cabecera funcion " + entradaTS.getLexema());
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			parse.add(16);
			token = lexico.scan();
			cuerpo();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
				System.out.println("funcion con cuerpo " + entradaTS.getLexema());
				token = lexico.scan();
			} else {
				// error
				System.err.print(" error 16 ");
			}
		}
		
	}

	/**	17. LISTA_PARAM → TIPO ID RESTO_LISTA 
	 * 	18. LISTA_PARAM → lambda
	 */
	private void lista_param() {
		if(!token.esIgual(null)){
			parse.add(17);
			tipo();
			token = lexico.scan();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				id();
				token = lexico.scan();
				restoLista();
			}
		}
		else{
			parse.add(18);
		}
	} 

	
	/** 19. RESTO_LISTA → , LISTA_PARAM 
	 	20. RESTO_LISTA → lambda
	 */
	private void restoLista() {
		if(!token.esIgual(null)){
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
				parse.add(19);
				token = lexico.scan();
				lista_param();
			}
			else{
				System.err.print("REegla 19");
			}
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
				token = lexico.scan();
				if(token.esIgual(TipoToken.NUM_ENTERO)){
					token = lexico.scan();
					if(token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_CORCHETE)){
						token = lexico.scan();
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
				token = lexico.scan();
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
			token = lexico.scan();
			inicDim3();
			//token = lexico.scan();
			if(!token.esIgual(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE)){
				System.err.print("Regla 25");
			}
			else{
				token = lexico.scan();
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
				token = lexico.scan();
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
				token = lexico.scan();
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
	 Hay que cambiar valor por LITERAL
	 * 30. INIC_CONST → , ID = LITERAL INIC_CONST
	 * 31. INIC_CONST → lambda
	 */
	private void inic_const() {
		System.out.println("declaracion constante " + entradaTS.getLexema());
		if (!token.esIgual(null)){ // Si no es lambda
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
				parse.add(30);
				token = lexico.scan();
				if(token.esIgual(TipoToken.IDENTIFICADOR)) {
					//idConst();
					literal();  // ID = LITERAL
					inic_const();
				} else {
				// error
					System.err.print(" error 30 ");
				}	
			}
			else{
				System.err.print(" error 30 ");
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
			token = lexico.scan();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				id();
				inicializacion();
				declaraciones();
			} else {
				// error
				System.err.print(" error 32 ");
			}		
		} else {
			parse.add(33);
		}
	}
	
	
	private void idConst() {
		entradaTS = (EntradaTS)token.getAtributo();
		entradaTS.setTipo(tipo);
		entradaTS.setConstante(true);
		token = lexico.scan();
		if(token.esIgual(TipoToken.OP_ASIGNACION,OpAsignacion.ASIGNACION)) {
			token = lexico.scan();
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("inicializacion constante " + entradaTS.getLexema() + " con " + valor);
			token = lexico.scan();
		} else {
			//error
			System.err.print(" error const ");
		}
	}


	private void id() {
		entradaTS = (EntradaTS)token.getAtributo();
		entradaTS.setTipo(tipo);
		token = lexico.scan();
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
			token = lexico.scan();
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("inicializacion variable " + entradaTS.getLexema() + " con " + valor);
			token = lexico.scan();
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

	private void instruccion() {
		parse.add(23);
		ins_fun();
		ins_reg();
		ins_lect(); 
		ins_esc();
		ins_asig();
		ins_decl();
		ins_vacia();
	}
	
	
	/**
	 * 44. INS_FUNCION → ID (LISTA_ATB);
	 */
	private void ins_fun() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(44);
			tipo = null;//((EntradaTS)token.getAtributo()).getLexema();//TOFIX obtener enumerado tipo de la variable declarada
			token = lexico.scan();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) {
				parse.add(44);
				token = lexico.scan();
				lista_atb();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
					System.out.println("llamada a funcion " + entradaTS.getLexema());
					token = lexico.scan();
				} else {
					// error
					System.err.print(" error 44 ");
				}
			}
		} else {
			// error
			System.err.print(" error 44 ");
		}
		
	}
	
	/**
	 * 45. LISTA_ATB → ATRIBUTO RESTO_ATB
	 * 46. LISTA_ATB → lambda
	 */
	private void lista_atb() {
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
	private void resto_atb() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(47);
			token = lexico.scan();
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
			tipo = null;//((EntradaTS)token.getAtributo()).getLexema();//TOFIX obtener enumerado tipo de la variable declarada
			token = lexico.scan();
		} else if(token.esIgual(TipoToken.LIT_CADENA)){ //NO SERIA ESTE!!!!! HABRIA QUE VER QUE PONER...
			parse.add(49);
			//no se si hay que hacer algo mas...
			token = lexico.scan();
		} else {
			// error
			System.err.print(" error 29 ");
		}
	}
	
	
	
	/**
	 * 
	 */
	private void ins_reg() {
		
	}
	
	/**
	 * 60. INS_LECTURA → cin >>  RESTO_LECT 
	 */
	private void ins_lect() {
		if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 74){
			parse.add(60);
			token = lexico.scan();
			if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MAYORES)){
				token = lexico.scan();
				resto_lect();
			}
			else{
				// error
				System.err.print(" error 60 ");
			}
		}
		else{
			// error
			System.err.print(" error 60 ");
		}
	}
	
	/**
	 * 61. RESTO_LECT → ID ;
	 * 62. RESTO_LECT → LITERAL  ;
	 */
	private void resto_lect() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)){ //FALTA ALGO MAS AQUI???
			parse.add(61);
			token = lexico.scan();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
				token = lexico.scan();
			}
			else{
				// error
				System.err.print(" error 61 ");
			}
		}
		else if(esLiteral()){
			parse.add(62);
			token = lexico.scan();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
				token = lexico.scan();	
			}
			else{
				// error
				System.err.print(" error 62 ");
			}
		}
		else{
			// error
			System.err.print(" error 62 ");
		}
	}
	
	/**
	 * 63. INS_ESCRITURA → cout << RESTO_ESC
	 */
	private void ins_esc() {
		if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 75){
			parse.add(63);
			token = lexico.scan();
			if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MENORES)){
				token = lexico.scan();
				resto_esc();
			}
			else{
				// error
				System.err.print(" error 63 ");
			}
		}
		else{
			// error
			System.err.print(" error 63 ");
		}
	}
	
	/**
	 * 64. RESTO_ESC →  LITERAL INS_ESCRITURA2
	 * 65. RESTO_ESC →  ID INS_ESCRITURA2
	 * 66. RESTO_ESC → endl INS_ESCITURA2
	 */
	private void resto_esc() {
		if(esLiteral()){
			parse.add(64);
			token = lexico.scan();
			ins_esc2();
		}
		else if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(65);
			token = lexico.scan();
			ins_esc2();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 76 /* endl */){
			parse.add(66);
			token = lexico.scan();
			ins_esc2();
		}
		else{
			// error
			System.err.print(" error 66 ");
		}
	}
	
	/**
	 * 67. INS_ESCRITURA2 →  << RESTO_ESC2
	 * 68. INS_ESCRITURA2 →  ;
	 */
	private void ins_esc2(){
		if(token.esIgual(TipoToken.OP_LOGICO,OpLogico.DOS_MENORES)){
			parse.add(67);
			token = lexico.scan();
			resto_esc2();
		}
		else if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)){
			parse.add(68);
			token = lexico.scan();
		}
		else{
			// error
			System.err.print(" error 68 ");
		}
	}
	
	
	/**
	 * 69. RESTO_ESC2 → LITERAL  INS_ESCRITURA2 
	 * 70. RESTO_ESC2 → ID  INS_ESCRITURA2  
	 * 71. RESTO_ESC2 → endl INS_ESCRITURA2 
	 * 72. RESTO_ESC2 → ℷ
	 */
	private void resto_esc2() {
		if(esLiteral()){
			parse.add(69);
			token = lexico.scan();
			ins_esc2();
		}
		else if(token.esIgual(TipoToken.IDENTIFICADOR)){
			parse.add(70);
			token = lexico.scan();
			ins_esc2();
		}
		else if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 76 /* endl */){
			parse.add(71);
			token = lexico.scan();
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
			token = lexico.scan();
			//expresion();
		} 
		
	}
	/**
	 * 
	 * 	74. CUERPO → INSTRUCCION CUERPO 
		75. CUERPO → SENT_BUCLE CUERPO
		76. CUERPO → SENT_IF CUERPO
		77. CUERPO → SENT_CASE CUERPO
		78. CUERPO → ℷ
	 */
	private void cuerpo()
	{
		/*if(instruccion())
		{
			parse.add(74);
			cuerpo();
		}
		else*/ if(sent_bucle())
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
		/*if(instruccion())
		{
			parse.add(79);
		}
		else*/ if(sent_bucle())
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
			token = lexico.scan();
			cuerpo();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
				parse.add(83);
				token = lexico.scan();
			}
			else
				System.out.println("ERROR EN REGLA 83. FALTA PARENTESIS DE CIERRE");
		}
		
	}
	
	
	private boolean sent_case() {
		return true;
	}


	private boolean sent_if() {
		return true;
	}


	private boolean sent_bucle() {
		return true;
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
				|| token.esIgual(TipoToken.NUM_REAL) || token.esIgual(TipoToken.NUM_REAL_EXPO));
	}
	
	public Vector<Integer> getParse() {
		return parse;
	}	
	
	/**
	 * Main de pruebas.
	 */
	public static void main(String args[])
	{
		String entrada = "#include <Alina.h> \n  #include \"cris.h\" " +
				"int a=2,b,c; const bool i=3, k=true; int f(); " +
				"float g=3; double h(){}";
		AnalizadorLexico a = new AnalizadorLexico(new StringBufferInputStream(entrada));
		AnalizadorSintactico s = new AnalizadorSintactico(a);
		for (int i : s.getParse()) 
			System.out.print(i+" ");
		
	}
	
}
