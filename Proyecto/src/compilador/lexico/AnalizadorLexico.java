package compilador.lexico;
import java.io.InputStream;
import java.util.ArrayList;

import compilador.lexico.tokens.*;
import compilador.lexico.tokens.Token.TipoToken;

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
	 * necesario para comprobar si tenemos que leer otro caracter, o procesar el ultimo
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
		String lexema = "";
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
				} else if (preanalisis == '}') {
					token = new Token(TipoToken.SEPARADOR,'}');
					return token;
				} else if (preanalisis == '~') {
					token = new Token(TipoToken.OP_LOGICO,'~');
					return token;
				} else if (preanalisis == ';') {
					token = new Token(TipoToken.SEPARADOR, ';');
					return token;
				} else if (preanalisis == '+') {
					transita(41);
				} else if (preanalisis == '-') {
					transita(45);
				} else if (preanalisis == '*') {
					transita(49);
				} else if (preanalisis == '%') {
					transita(52);
				} else if (preanalisis == '#') {
					transita(60);
				} else if (preanalisis == '/') {
					transita(62);
				} else if (preanalisis == '=') {
					transita(64);
				} else if (preanalisis == '>') {
					transita(68);
				} else if (preanalisis == '[') {
					token = new Token(TipoToken.SEPARADOR,'[');
					return token;
				} else if (preanalisis == '{') {
					token = new Token(TipoToken.SEPARADOR, '{');
					return token;
				} else if (preanalisis == '<') {
					transita(75);
				} else if (preanalisis == ':') {
					transita(81);
				} else if (preanalisis == ']') {
					token = new Token(TipoToken.SEPARADOR, ']');
					return token;
				} else if (preanalisis == '|') {
					transita(83);					
				} else if (preanalisis == '&') {
					transita(87);
				} else if (preanalisis == '^') {
					transita(91);
				} else if (preanalisis == '(') {
					token = new Token(TipoToken.SEPARADOR,'(');
					return token;
				} else if (preanalisis == ')') {
					token = new Token(TipoToken.SEPARADOR, ')');
					return token;
				} else if (preanalisis == '\'') {
					transita(102);
				} else if (preanalisis == '"') {
					transita(99);
				} else if (noDigito()) {
					lexema = lexema+preanalisis;
					transita(97);
				} else if (preanalisis == '\\') {
					transita(105);
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
				//break;
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
				//break;
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
				//break;
			case 4:	
				if((digito >= 0 && digito <= 9)) {
					parteEntera = parteEntera*16 + digito;
					transita(7);
				} else if((preanalisis >= 'a' && preanalisis <= 'f') || (preanalisis >= 'A' && preanalisis <= 'F')){
					int hex = valHex();
					parteEntera = parteEntera*16 + hex;
					transita(7);
				} else {
					//TODO Error del tipo IV (o caracter no esperado, deberia ser un hexadecimal)
				}
				//break;
			case 5:	
				if(preanalisis == 'L') {
					transita(13);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(11);
				} else {
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
				}
				//break;
			case 6:	
			case 7:
				if((digito >= 0 && digito <= 9)) {
					parteEntera = parteEntera*16 + digito;
					transita(7);
				} else if((preanalisis >= 'a' && preanalisis <= 'f') || (preanalisis >= 'A' && preanalisis <= 'F')){
					int hex = valHex();
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
				//break;
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
				//break;
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
			//OPERADORES
			case 41:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"+=");
					return token;
				} else if (preanalisis == '+') {					
					token = new Token(TipoToken.OP_ARITMETICO,"++");
					return token;
				} else if (preanalisis == 'f') {						//valores al preanalisis DELIM2 - {+,=}
					token = new Token(TipoToken.OP_ARITMETICO,"+");
					asterisco = true;
				}
				break;
			case 45:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"-=");
					return token;
				} else if (preanalisis == '-') {					
					token = new Token(TipoToken.OP_ARITMETICO,"--");
					return token;
				} else if (preanalisis == '>') {					
					token = new Token(TipoToken.OP_ASIGNACION,"->");
					return token;
				} else if (preanalisis == 'f') {						//valores al preanalisis DELIM2 - {-,=}
					token = new Token(TipoToken.OP_ARITMETICO,"-");		
					asterisco = true;
				}
				break;
			case 49:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"*=");
					return token;
				} else if (preanalisis == 'f') {						//valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_ARITMETICO,"*");
					asterisco = true;
				}
				break;
			case 52:
				if (preanalisis == '>') {
					token = new Token(TipoToken.SEPARADOR,"%>");
					return token;
				} else if (preanalisis == 'f') {						//valores al preanalisis DELIM2 - {>,=,:}
					token = new Token(TipoToken.OP_ARITMETICO, "%");
					asterisco = true;
				} else if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"%=");
					return token;
				} else if (preanalisis == ':') {
					transita(56);
				}
				break;
			case 56:
				if (preanalisis == '%') {
					transita(57);
				} else if (preanalisis == 'f') {						//valores al preanalisis DELIM2 - {%}
					token = new Token(TipoToken.SEPARADOR, "%:");
					asterisco = true;
				}
				break;
			case 57:
				if (preanalisis == ':') {
					token = new Token(TipoToken.SEPARADOR,"%:%:");
					return token;
				}
			case 60:	
				if (preanalisis == '#') {
					token = new Token(TipoToken.SEPARADOR,"##");
					return token;
				} else if (preanalisis == 'f') {						//valores al preanalisis DELIM2 - {#}
					token = new Token(TipoToken.SEPARADOR,"#");
					asterisco = true;
				}
				break;
			case 62:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"/=");
					return token;
				} else if (preanalisis == 'f') {						//valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_ARITMETICO, "/");
					asterisco = true;
				}
				break;
			case 64:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_COMPARACION,"==");
					return token;
				} else if (preanalisis == 'f') {						//valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_ASIGNACION,"=");
					asterisco = true;
				}
				break;
			case 68:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_COMPARACION,">=");
					return token;
				} else if (preanalisis == '>') {
					transita(69);
				} else if (preanalisis == 'f') {						//valores al preanalisis DELIM2 - {>,=}
					token = new Token(TipoToken.OP_COMPARACION,">");
					asterisco = true;
				}
				break;
			case 69:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,">>=");
					return token;
				} else if (preanalisis == 'f') {						//valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_LOGICO, ">>");
					asterisco = true;
				}
				break;
			case 75:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_COMPARACION,"<=");
					return token;
				}else if (preanalisis == '<') {
					transita(78);
				} else if (preanalisis == 'f') {						//valores al preanalisis DELIM2 - {<,%,=,:}
					token = new Token(TipoToken.OP_COMPARACION,"<");
					asterisco = true;
				}
				break;
			case 78:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"<<=");
					return token;
				} else if (preanalisis == 'f') {						//valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_LOGICO,"<<");
					asterisco = true;
				}
				break;
			case 81:
				if (preanalisis == '>') {
					token = new Token(TipoToken.SEPARADOR,":>");
					return token;
				}
				break;
			case 83:
				if (preanalisis == '|') {
					token = new Token(TipoToken.OP_LOGICO,"||");
					return token;
				} else if (preanalisis == 'f') {					//valores al preanalisis DELIM - {|,=}
					token = new Token(TipoToken.OP_LOGICO,"|");
					asterisco = true;
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
				} else if (preanalisis == 'f') {					//valores al preanalisis DELIM - {&,=}
					token = new Token(TipoToken.OP_LOGICO,"&");
					asterisco = true;
				}
				break;
			case 91:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_LOGICO,"^=");
					return token;
				} else if (preanalisis == 'f') {					//valores al preanalisis DELIM - {=}
					token = new Token(TipoToken.OP_LOGICO,"^");
					asterisco = true;
				}
				break;
			// RECONOCIMIENTO DE CADENAS, IDENTIFICADORES Y PALABRAS RESERVADAS
			case 97:	
				if (noDigito() || digito()) {
					lexema = lexema+preanalisis;
					transita(97);
				} else if(esDelimitador()) { //TODO Implementar TablaSimbolos
//					Token token = TablaSimbolos.getPalRes.Busca(lexema);
//					if(token == null) {	
//						token = TablaSimbolos.Busca(lexema);
//						if (token == null) //crea un token, compuesto de Identificador y un puntero a la tabla de simbolos
//							token = new Token(lexema, TablaSimbolos.Inserta(lexema)); 
//					}	
//					return token;
				}
				//break;
			case 98:
			case 99:
				if((preanalisis != '"') && (preanalisis != '\\') && (preanalisis != '\n') ) {
					lexema = lexema+preanalisis;
					transita(99);
				} else if(preanalisis == '"') {
					return new Token(TipoToken.LIT_CADENA,lexema); //OJO, segun tabla tokens indice a la TS
					//OJO, pensar cuando reinicializar lexema = ""
				}
				//break;
			case 100:
			case 101:
			case 102:	
				if((preanalisis != '\'') && (preanalisis != '\\') && (preanalisis != '\n') ) {
					lexema = lexema+preanalisis;
					transita(102);
				} else if(preanalisis == '\'') {
					return new Token(TipoToken.LIT_CARACTER,lexema);
				}
				//break;
			} //switch
		} //while
		return token;
	}

	private boolean esDelimitador() {
		/**
		 * ‘ ’ (blanco) | TAB | EOL | EOF 
		 * Separadores: ‘;’ | ‘|’ | ‘:’ | ‘+’ | ‘-’ | ‘/’ | ‘*’ | ‘<’ | ‘>’ | ‘=’ | ‘&’ | ‘^’| ‘%’ | ‘!’ | ‘~’ | ‘,‘ | ‘-’ | ‘*‘ | ‘+’ | ‘#’ | ‘(‘ | ‘)’
		 */
		
		for ( Token.Separadores i :Token.Separadores.values())
		{	
			String s = "";
			s = ""+preanalisis;
			if(s.equals(i.getDesc().charAt(0)))
				return true;
		}	
		if(preanalisis == ' ' || preanalisis == '\t' || preanalisis == '\n') /// FALTA EL DE EOG
			return true;
		
		return false;
	}

	private boolean noDigito() {

		if(preanalisis >= 'a' && preanalisis <= 'z')
			return true;
		
		if(preanalisis >= 'A' && preanalisis <= 'Z')
			return true;
		
		if(preanalisis == '_')
			return true;
		
		return false;
	}

	private boolean digito() {
		if (preanalisis >= '0' && preanalisis <= '9')
				return true;
		return false;
	}
	
	private int valHex() {
		int hex = 0;
		if(preanalisis >= 'a' && preanalisis <= 'f')
			hex = 10 + preanalisis-'a';
		if(preanalisis >= 'A' && preanalisis <= 'F')
			hex = 10 + preanalisis-'A';
		return hex;
	}

	private char getChar() {
		// TODO Auto-generated method stub
		//Incrementara las columnas
		numcolumna++;
		return 0;
	}
	
	public void transita(int est){
		preanalisis = getChar();
		estado = est;
	}
    
}
