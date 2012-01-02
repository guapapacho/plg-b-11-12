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
	 * 3. RESTO_LIBRERIA → LIT_CADENA LIBRERIA
	 * 4. RESTO_LIBRERIA → <ID.ID> LIBRERIA
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
	
	private void principal() {

		
	}

	/**
	 * 17. COSAS → const TIPO ID = valor INIC_CONST ;
	 *  5. COSAS → TIPO ID COSAS2 COSAS
	 *  6. COSAS → lambda
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
	 * 18. INIC_CONST → , ID = valor INIC_CONST
	 * 19. INIC_CONST → lambda
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


	/**
	 * 7. TIPO → ID
	 * 8. TIPO → TIPO_SIMPLE | VOID // pal reservada
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
	
	private void id() {
		entradaTS = (EntradaTS)token.getAtributo();
		entradaTS.setTipo(tipo);
		token = lexico.scan();
	}

	/**
	 *  9. COSAS2 → ( LISTA_PARAM ) COSAS3
	 * 10. COSAS2 → INICIALIZACION  DECLARACIONES ;
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
	 * 11. DECLARACIONES → , ID INICIALIZACION DECLARACIONES
	 * 12. DECLARACIONES → lambda
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

	/**
	 * 13. INICIALIZACION → = valor
	 * 14. INICIALIZACION → lambda
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

	/**
	 * 15. COSAS3 → ; 
	 * 16. COSAS3 → { CUERPO2 }
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


	private void cuerpo2() {
		// TODO Auto-generated method stub
		
	}


	private void lista_param() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 20. INSTRUCCION → INS_FUNCION
	 * 21. INSTRUCIION → INS_REGISTRO
	 * 22. INSTRUCCION → INS_LECTURA
	 * 23. INSTRUCCION → INS_ESCRITURA
	 * 24. INSTRUCCION → INS_ASIGNACION
	 * 25. INSTRUCCION → INS_DECLARACION
	 * 26. INSTRUCCION → ;
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
	 * 27. INS_FUNCION → ID (LISTA_ATB);
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
	 * 28. LISTA_ATB → ATRIBUTO RESTO_ATB
	 * 29. LISTA_ATB → lambda
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
	 * 30. RESTO_ATB → , ATRIBUTO RESTO_ATB
	 * 44. RESTO_ATB → lambda
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
	 * 45. ATRIBUTO → LITERAL
	 * 46. ATRIBUTO → ID
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
