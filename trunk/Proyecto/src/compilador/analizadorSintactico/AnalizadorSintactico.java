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
		programa();
	}
	
	
	/**
	 * Regla numero 1. 
	 * PROGRAMA → LIBRERIA COSAS MAIN COSAS
	 */
	private void programa() {
		parse.add(1);
		libreria();
		cosas();
		principal(); // TOFIX (alina) creo que no hace falta en el sintactico
		cosas();
	}
	
	/**
	 * Regla numero 2 
	 * LIBRERIA → #include RESTO_LIBRERIA
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
	 * Regla numero 3
	 * RESTO_LIBRERIA → LIT_CADENA LIBRERIA | <ID.ID> LIBRERIA
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
							parse.add(3);
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
		{System.err.print(" error 3 ");}	
		
	}
	
	private void principal() {

		
	}

	/**
	 * Regla numero 4
	 * COSAS → TIPO ID COSAS2 COSAS | ℷ
	 */
	private void cosas() {
		parse.add(4);
		if(!token.esIgual(TipoToken.EOF)) {
			tipo();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				id();
				cosas2();	
				cosas();			
			} else {
				// error sintáctico
				System.err.print(" error 4 ");
			}
		}
	}
	
	/**
	 * Regla numero 5
	 * TIPO → ID | TIPO_SIMPLE | VOID
	 */
	private void tipo() {
		parse.add(5);
		if(token.esIgual(TipoToken.IDENTIFICADOR))
			tipo = null;//((EntradaTS)token.getAtributo()).getLexema();//TOFIX obtener enumerado tipo de la variable declarada
		else if(token.esIgual(TipoToken.PAL_RESERVADA)){
			switch((Integer)token.getAtributo()){
			//TODO no se si hace falta mirar si es un tipo valido
			}
		} else {
			// error
			System.err.print(" error 5 ");
		}
		token = lexico.scan();
	}
	
	private void id() {
		entradaTS = (EntradaTS)token.getAtributo();
		entradaTS.setTipo(tipo);
		token = lexico.scan();
	}

	/**
	 * Regla numero 6
	 * COSAS2 → ( LISTA_PARAM ) COSAS3 | INICIALIZACION  DECLARACIONES ;
	 */
	private void cosas2() {
		parse.add(6);
		// COSAS2 → ( LISTA_PARAM ) COSAS3
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_PARENTESIS)) {
			token = lexico.scan();
			lista_param();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_PARENTESIS)) {
				token = lexico.scan();
				cosas3();
			} else {
				//error
				System.err.print(" error 6a ");
			}
		} else {
			// COSAS2 → INICIALIZACION  DECLARACIONES ;
			inicializacion();
			declaraciones();		
			if(!token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
				//error
				System.err.print(" error 6b ");
			}
			token = lexico.scan();
		}
	}

	/**
	 * Regla numero 7
	 * DECLARACIONES → , ID INICIALIZACION DECLARACIONES | ℷ
	 */
	private void declaraciones() {
		parse.add(7);
		System.out.println("declaracion variable " + entradaTS.getLexema());
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.COMA)) {
			token = lexico.scan();
			if(token.esIgual(TipoToken.IDENTIFICADOR)) {
				id();
				inicializacion();
				declaraciones();
			} else {
				// error
				System.err.print(" error 7 ");
			}		
		}		
	}

	/**
	 * Regla numero 8
	 * INICIALIZACION → = valor | ℷ
	 */
	private void inicializacion() {
		parse.add(8);
		if(token.esIgual(TipoToken.OP_ASIGNACION,OpAsignacion.ASIGNACION)) {
			token = lexico.scan();
			Object valor = token.getAtributo(); // TOFIX depende del tipo... a ver que se hace con el...
			System.out.println("inicializacion variable " + entradaTS.getLexema() + " con " + valor);
			token = lexico.scan();
		}		
	}

	/**
	 * Regla numero 9
	 * COSAS3 → ; | { CUERPO2 }
	 */
	private void cosas3() {
		parse.add(9);
		if(token.esIgual(TipoToken.SEPARADOR,Separadores.PUNTO_COMA)) {
			//cabecera funcion
			System.out.println("cabecera funcion " + entradaTS.getLexema());
		} else if(token.esIgual(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE)) {
			token = lexico.scan();
			cuerpo2();
			if(token.esIgual(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE)) {
				System.out.println("funcion con cuerpo " + entradaTS.getLexema());
			} else {
				// error
			}
		}
		token = lexico.scan();
	}


	private void cuerpo2() {
		// TODO Auto-generated method stub
		
	}


	private void lista_param() {
		// TODO Auto-generated method stub
		
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
				"int a=2,b,c; int f(); " +
				"float g=3; double h(){}";
		AnalizadorLexico a = new AnalizadorLexico(new StringBufferInputStream(entrada));
		AnalizadorSintactico s = new AnalizadorSintactico(a);
		for (int i : s.getParse()) 
			System.out.print(i+" ");
		
	}
	
}
