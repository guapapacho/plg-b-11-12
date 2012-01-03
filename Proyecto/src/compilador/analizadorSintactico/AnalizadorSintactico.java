package compilador.analizadorSintactico;


import java.io.StringBufferInputStream;
import java.util.Vector;

import compilador.analizadorLexico.*;
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
	 * 2. LIBRERIA → #include RESTO_LIBRERIA
	 */
	// Falta 3. LIBRERIA -> lambda creo
	private void libreria() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ALMOHADILLA))
		{
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
			parse.add(3);
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
	 * 7. TIPO → TIPO_SIMPLE | VOID // pal reservada --> Ahora ya no hay VOID en esta reglas
	 */
	private void tipo() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(7);
			tipo = null;//((EntradaTS)token.getAtributo()).getLexema();//TOFIX obtener enumerado tipo de la variable declarada
			token = lexico.scan();
		} else if(token.esIgual(TipoToken.PAL_RESERVADA)){
			parse.add(8);
			switch((Integer)token.getAtributo()){
			//TODO no se si hace falta mirar si es un tipo valido
			}
			token = lexico.scan();
		} else {
			// error
			System.err.print(" error 8 ");
		}
	}
	
	private void principal() {

		
	}

	/**
	 * 17. COSAS → const TIPO ID = valor INIC_CONST ;
	 *  5. COSAS → TIPO ID COSAS2 COSAS
	 *  6. COSAS → lambda
	 */
	/* 	8. COSAS → const TIPO ID = LITERAL INIC_CONST ;
 		9. COSAS → TIPO ID COSAS2 COSAS
		10. COSAS → VOID ID ( LISTA_PARAM ) COSAS3
		11. COSAS → ℷ
		
	 	*/
	private void cosas() {
		if(!token.esIgual(TipoToken.EOF)) {
			if(token.esIgual(TipoToken.PAL_RESERVADA) && (Integer)token.getAtributo() == 9 /*const*/) {
				// 17. COSAS → const TIPO ID = valor INIC_CONST ;
				parse.add(17);
				token = lexico.scan();
				tipo();
				if(true) {//TODO tipo correcto
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						idConst(); // ID = valor
						inic_const();
						if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
							//error
							System.err.print(" error 17 ");
						} else {
							token = lexico.scan();
						}
					}
				}
			} else {
				tipo();
				if(true) {//TODO tipo correcto
					// 5. COSAS → TIPO ID COSAS2 COSAS
					parse.add(5);
					if(token.esIgual(TipoToken.IDENTIFICADOR)) {
						id();
						cosas2();	
						cosas();			
					} else {
						// error sintáctico
						System.err.print(" error 5 ");
					}
				} else {
					// 6. COSAS → lambda
					parse.add(6);
				}
			}
		}
	}
	
	/**
	 * 12. COSAS2 → ( LISTA_PARAM ) COSAS3
	 * 14. COSAS2 → INICIALIZACION  DECLARACIONES ;
	 */
	/*	12. COSAS2 → ( LISTA_PARAM ) COSAS3
		13. COSAS2 → [NUM_ENTERO] DIMENSION INIC_DIM
		14. COSAS2 → INICIALIZACION  DECLARACIONES ;
	*/
	private void cosas2() {
		// COSAS2 → ( LISTA_PARAM ) COSAS3
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) {
			parse.add(9);
			token = lexico.scan();
			lista_param();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
				token = lexico.scan();
				cosas3();
			} else {
				//error
				System.err.print(" error 9 ");
			}
		} else {
			// COSAS2 → INICIALIZACION  DECLARACIONES ;
			parse.add(10);
			inicializacion();
			declaraciones();		
			if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				//error
				System.err.print(" error 10 ");
			}
			token = lexico.scan();
		}
	}
	
	/**
	 * 15. COSAS3 → ; 
	 * 16. COSAS3 → { CUERPO2 }
	 */
	/*	15. COSAS3 → ;
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
			cuerpo2();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
				System.out.println("funcion con cuerpo " + entradaTS.getLexema());
				token = lexico.scan();
			} else {
				// error
				System.err.print(" error 16 ");
			}
		}
		
	}
	
	/**
	 Hay que cambiar valor por LITERAL
	 * 30. INIC_CONST → , ID = LITERAL INIC_CONST
	 * 31. INIC_CONST → lambda
	 */
	private void inic_const() {
		System.out.println("declaracion constante " + entradaTS.getLexema());
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(18);
			token = lexico.scan();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				idConst();
				inic_const();
			} else {
				// error
				System.err.print(" error 18 ");
			}		
		} else {
			parse.add(19);
		}
		
	}

	
	/**
	 * 32. DECLARACIONES → , ID INICIALIZACION DECLARACIONES
	 * 33. DECLARACIONES → lambda
	 */
	private void declaraciones() {
		System.out.println("declaracion variable " + entradaTS.getLexema());
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(11);
			token = lexico.scan();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				id();
				inicializacion();
				declaraciones();
			} else {
				// error
				System.err.print(" error 11 ");
			}		
		} else {
			parse.add(12);
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
			parse.add(13);
			token = lexico.scan();
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("inicializacion variable " + entradaTS.getLexema() + " con " + valor);
			token = lexico.scan();
		} else {
			parse.add(14);
		}
	}


	private void cuerpo2() {
		// TODO Auto-generated method stub
		
	}


	private void lista_param() {
		// TODO Auto-generated method stub
		
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
			parse.add(24);
			tipo = null;//((EntradaTS)token.getAtributo()).getLexema();//TOFIX obtener enumerado tipo de la variable declarada
			token = lexico.scan();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) {
				parse.add(24);
				token = lexico.scan();
				lista_atb();
				if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
					System.out.println("llamada a funcion " + entradaTS.getLexema());
					token = lexico.scan();
				} else {
					// error
					System.err.print(" error 24 ");
				}
			}
		} else {
			// error
			System.err.print(" error 24 ");
		}
		
	}
	
	/**
	 * 45. LISTA_ATB → ATRIBUTO RESTO_ATB
	 * 46. LISTA_ATB → lambda
	 */
	private void lista_atb() {
		if(!token.esIgual(TipoToken.EOF)) {
			parse.add(25);
			atributo();
			resto_atb();
		} else {
			// 26. LISTA_ATB → lambda 
			parse.add(26);
		}
	}
	
	/**
	 * 47. RESTO_ATB → , ATRIBUTO RESTO_ATB
	 * 48. RESTO_ATB → lambda
	 */
	private void resto_atb() {
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			parse.add(27);
			token = lexico.scan();
			atributo();
			resto_atb();
		} else {
			parse.add(28);
		}
	}
	
	/**
	 * 49. ATRIBUTO → LITERAL
	 * 50. ATRIBUTO → ID
	 */
	private void atributo() {
		if(token.esIgual(TipoToken.IDENTIFICADOR)) {
			parse.add(30);
			tipo = null;//((EntradaTS)token.getAtributo()).getLexema();//TOFIX obtener enumerado tipo de la variable declarada
			token = lexico.scan();
		} else if(token.esIgual(TipoToken.LIT_CADENA)){ //NO SERIA ESTE!!!!! HABRIA QUE VER QUE PONER...
			parse.add(29);
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
	 * 
	 */
	private void ins_lect() {
		
	}
	
	/**
	 * 
	 */
	private void ins_esc() {
		
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
	

	public Vector<Integer> getParse() {
		return parse;
	}	
	
	/**
	 * Clase main de pruebas.
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
