package compilador.analizadorSintactico;


import java.io.StringBufferInputStream;
import java.util.Vector;

import compilador.analizadorLexico.*;
import compilador.analizadorLexico.Token.OpComparacion;
import compilador.analizadorLexico.Token.Separadores;
import compilador.analizadorLexico.Token.TipoToken;

@SuppressWarnings("deprecation")
public class AnalizadorSintactico {

	/**
	 * Token actual obtenido mediante el Analizador lexico
	 */
	private Token token;
	
	
	/**
	 * Analizador lexico 
	 */
	private AnalizadorLexico lexico;
	
	/**
	 * Vector para guardar la secuencia ordenadas de los numeros de reglas aplicadas
	 * para construir el arbol de derivacion de la cadena de tokens de entrada..
	 */
	private Vector<Integer> parse;
	
	public AnalizadorSintactico(AnalizadorLexico lexico){
		this.lexico = lexico;
		parse = new Vector<Integer>();
		token = lexico.scan();
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
		principal();
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
			// TODO include es palabra clave o identificador?  
			if(token.esIgual(TipoToken.IDENTIFICADOR))
			{
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
		{}	
		
	}
	
	private void principal() {

		
	}


	private void cosas() {

		
	}
	
	/**
	 * Clase main de pruebas.
	 */

	public static void main(String args[])
	{
		AnalizadorLexico a = new AnalizadorLexico(new StringBufferInputStream("#include <Alina.h> \n #include \"cris.h\""));
		AnalizadorSintactico s = new AnalizadorSintactico(a);
		for (int i : s.getParse()) 
			System.out.print(i+" ");
		
	}

	public Vector<Integer> getParse() {
		return parse;
	}





	
}
