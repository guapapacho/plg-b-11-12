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
	 * Atributo que indica si el estado final anterior tenia asterisco o no,
	 * necesario para comprobar si tenemos que leer otro caracter, o procesar el último
	 */
	boolean asterisco;
	
	
	/**
	 * Constructora de la clase
	 */
	public AnalizadorLexico() {
		this.numlinea = 1;
		this.numcolumna = 0;
		this.preanalisis = ' ';
		this.estado = 0; 
		this.asterisco = false;
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
		if(!asterisco)
			preanalisis = getChar();
		asterisco = false;
		int digito;
		int parteEntera = 0;
		int parteEnteraB10 = 0; // para el caso en el que el numero empieza como un octal pero resulta ser un real
		
		while (! fin) {
			digito = preanalisis -'0';
			
			switch(estado) {
			case 0: 		
				if(digito == 0) {
					transita(2);
				} else if(digito >= 1 && digito <= 9) {
					transita(1);
				} else if(preanalisis == '.') {
					transita(15);
				} else if (preanalisis == '\n') {
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
				}else if (preanalisis == '|') {
					transita(83);					
				}else if (preanalisis == '&') {
					transita(87);
				}else if (preanalisis == '^') {
					transita(91);
				}else if (preanalisis == '~') {
					token = new Token(TipoToken.OP_LOGICO,'~');
					return token;
				}else if (preanalisis == '(') {
					token = new Token(TipoToken.SEPARADOR,'(');
					return token;
				}else if (preanalisis == ')') {
					token = new Token(TipoToken.SEPARADOR, ')');
					return token;
				}
				break;
			case 1:
				if(digito >= 0 && digito <= 9) {
					parteEntera = parteEntera*10 + digito;
					transita(1);
				} else if(preanalisis == '.') {
					transita(16);
				}  else if(preanalisis == 'e' || preanalisis == 'E') {
					transita(17);
				} else if(preanalisis == 'l') {
					transita(8);
				} else if(preanalisis == 'L') {
					transita(5);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(6);
				} else {
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
				}
			case 2:	
				if(digito >= 0 && digito <= 7) {
					parteEntera = parteEntera*8 + digito;
					parteEnteraB10 = parteEnteraB10*10 + digito;
					transita(3);
				} else if(digito == 8 || digito == 9) {
					transita(14);
				} else if(preanalisis == 'x' || preanalisis == 'X') {
					transita(4);
				} else if(preanalisis == 'l') {
					transita(8);
				} else if(preanalisis == 'L') {
					transita(5);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(6);
				} else {
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
				}
			case 3:	
				if(digito >= 0 && digito <= 7) {
					parteEntera = parteEntera*8 + digito;
					parteEnteraB10 = parteEnteraB10*10 + digito;
					transita(3);
				} else if(digito == 8 || digito == 9) {
					parteEntera = parteEnteraB10;
					parteEntera = parteEntera*10 + digito;
					transita(14);
				} else if(preanalisis == '.') {
					transita(16);
				} else if(preanalisis == 'e' || preanalisis == 'E') {
					transita(17);
				} else if(preanalisis == 'l') {
					transita(8);
				} else if(preanalisis == 'L') {
					transita(5);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(6);
				} else {
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
				}
			case 4:	
				if((digito >= 0 && digito <= 9)) {
					parteEntera = parteEntera*16 + digito;
					transita(7);
				} else if((preanalisis >= 'a' && preanalisis <= 'f') || (preanalisis >= 'A' && preanalisis <= 'F')){
					int hex = valHex(preanalisis);
					parteEntera = parteEntera*16 + hex;
					transita(7);
				} else {
					//TODO Error del tipo IV (o caracter no esperado, deberia ser un hexadecimal)
				}
			case 5:	
				if(preanalisis == 'L') {
					transita(13);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(11);
				} else {
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
				}
			case 6:	
			case 7:	
				if((digito >= 0 && digito <= 9)) {
					parteEntera = parteEntera*16 + digito;
					transita(7);
				} else if((preanalisis >= 'a' && preanalisis <= 'f') || (preanalisis >= 'A' && preanalisis <= 'F')){
					int hex = valHex(preanalisis);
					parteEntera = parteEntera*16 + hex;
					transita(7);
				} else if(preanalisis == 'l') {
					transita(8);
				} else if(preanalisis == 'L') {
					transita(5);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(6);
				} else {
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
				}
			case 8:	
			case 9:	
			case 10:	
			case 11:	
			case 12:	
			case 13:	
			case 14:
				digito = preanalisis -'0';
				if(digito >= 0 && digito <= 9) {
					parteEntera = parteEntera*10 + digito;
				} else if(preanalisis == '.') {
					transita(16);
				} else if(preanalisis == 'e' || preanalisis == 'E') {
					transita(17);
				}
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

				
				
			case 83:
				if (preanalisis == '|') {
					token = new Token(TipoToken.OP_LOGICO,"||");
					return token;
				} else if (preanalisis == 'f') {					//K5
					transita(0000); //85
				} else if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"|=");
					return token;
				}
				break;
			case 87:
				if (preanalisis == '&') {
					token = new Token(TipoToken.OP_LOGICO,"&&");
					return token;
				} else if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"&=");
					return token;
				} else if (preanalisis == 'f') {					//K4
					transita(0000); //90
				}
				break;
			case 91:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_LOGICO,"^=");
					return token;
				} else if (preanalisis == 'f') {					//K7
					transita(0000); //93
				}
				break;
			}
		}
		return token;
		
	}


	private int valHex(char car) {
		int hex = 0;
		if(car >= 'a' && car <= 'f')
			hex = 10 + car-'a';
		if(car >= 'A' && car <= 'F')
			hex = 10 + car-'A';
		return hex;
	}

	private char getChar() {
		// TODO Auto-generated method stub
		//Incrementara las columnas
		return 0;
	}
	
	private void transita(int est){
		preanalisis = getChar();
		estado = est;
	}
    
}
