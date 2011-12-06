package compilador.lexico;
import java.io.InputStream;
import java.util.ArrayList;

import compilador.lexico.tokens.*;
import compilador.lexico.tokens.Token.TipoToken;
import compilador.lexico.tokens.Token.OpAritmetico;
import compilador.lexico.tokens.Token.OpAsignacion;
import compilador.lexico.tokens.Token.OpComparacion;
import compilador.lexico.tokens.Token.OpLogico;
import compilador.lexico.tokens.Token.Separadores;

/**
 * 
 * @author Grupo 1
 *
 */
public class AnalizadorLexico {

	/**
	 * Atributo que identifica el fichero con el codigo de entrada
	 */
	private InputStream fichero;
	
	/**
	 * Atributo que indica el numero de linea
	 */
	private int numlinea;
	
	/**
	 * Atributo que indica el numero de columna
	 */
	private int numcolumna;
	
	/**
	 * Array de tokens que vamos formando desde el fichero de entrada
	 */
	private ArrayList<Token> arraytokens; 
	
	/**
	 * Atributo que indica el caracter actual que leemos de la entrada
	 */
	char preanalisis;
	
	/**
	 * Atributo que indica el estado del automata en el que nos encontramos
	 */
	int estado;
	
	
	/**
	 * Constructora de la clase
	 */
	public AnalizadorLexico() {
		this.numlinea = 1;
		this.numcolumna = 0;
		this.preanalisis = ' ';
		this.estado = 0; 
	}

	/**
	 * 
	 * @return
	 */
	public Token scanner(){
		estado = 0;
		Token token = null;
		boolean fin = false;
		String lexema;
		preanalisis = getChar();
		
		while (! fin) {
			switch(estado) {
			case 0: 
				if (preanalisis == '\n') {
					numlinea++;
					preanalisis = getChar();
				} else if (preanalisis == '\t') {
					preanalisis = getChar();
				} else if (preanalisis == '\f'/*fin fichero*/) {
					token = new Token(TipoToken.EOF,null);
					return token;
				} else if (preanalisis == ' ') {
					preanalisis = getChar();
				} else if (preanalisis == '{') {
					token = new Token(TipoToken.SEPARADOR,0000);		//hay que crear en enumerado de separadores			
				} else if (preanalisis == '}') {
					token = new Token(TipoToken.SEPARADOR,0000);		//hay que crear en enumerado de separadores
				} else if (preanalisis == '~') {
					token = new Token(TipoToken.SEPARADOR,0000);		//hay que crear en enumerado de separadores
				} else if (preanalisis == '-') {
					transita(45);
				}
				break;
			case 1:
			case 2:	
			case 3:	
			case 4:	
			case 5:	
			case 6:	
			case 7:	
			case 8:	
			case 9:	
			case 10:	
			case 11:	
			case 12:	
			case 13:	
			case 14:	
			case 15:	
			case 16:	
			case 17:	
			case 18:	
			case 19:	
			case 20:	
			case 21:	
			case 22:	
			case 23:	
			case 24:	
			case 25:	
			case 26:	
			case 27:	
			case 28:	
			case 29:	
			case 30:	
			case 31:	
			case 32:	
			case 33:	
			case 34:	
			case 35:	
			case 36:	
			case 37:	
			case 38:	
			case 39:	
			case 40:	
			case 41:	
			case 42:	
			case 43:	
			case 44:		
			case 45:	

			}
		}
		return token;
		
	}


	private char getChar() {
		// TODO Auto-generated method stub
		//Incrementara las columnas
		return 0;
	}
	
	public void transita(int est){
		preanalisis = getChar();
		estado = est;
	}
    
}
