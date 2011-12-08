package compilador.lexico;
import java.io.IOException;
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
	private InputStream entrada;
	
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
	
	private String errorGenerico = "Error en linea "+numlinea+" y columna "+numcolumna; // Este es el mismo error tipo I
	private final String errorVI = "se esperaba un digito hexadecimal.";
	private final String errorV = "numero mal formado.";
	private final String errorIII = "caracter no esperado.";
	
	
	/**
	 * Constructora de la clase
	 */
	public AnalizadorLexico(InputStream entrada) {
		this.entrada = entrada;
		this.numlinea = 1;
		this.numcolumna = 0;
		this.preanalisis = ' ';
		this.estado = 0; 
		this.asterisco = false;
	}

	/**
	 * Método que devuelve el siguiente token
	 * @return el siguiente token
	 */
	public Token scanner(){
		estado = 0;
		Token token = null;
		boolean fin = false; // parece que no lo usamos
		String lexema = "";
		if(!asterisco)
			preanalisis = getChar();
		asterisco = false;
		int digito;
		int parteEntera = 0;
		int parteEnteraB10 = 0; // para el caso en el que el numero empieza como un octal pero resulta ser un real
		double parteDecimal = 0;
		double pesoDecimal = 0.1;
		int parteExponencial = 0;
		int signo = 1;
		
		while (! fin) {
			digito = preanalisis -'0';
			
			switch(estado) {
			case 0: 
				if(digito == 0) {
					transita(2);
				} else if(digito()) {
					parteEntera = parteEntera*10 + digito;
					transita(1);
				} else if(preanalisis == '.') {
					transita(15);
				} else if (preanalisis == '\r') { // parece que el salto de linea es \r\n
					transita(0);
				} else if (preanalisis == '\n') {
					numlinea++;
					numcolumna=0;
					preanalisis = getChar();
				} else if (preanalisis == '\t') {
					preanalisis = getChar();
				} else if(preanalisis == '\0') { // fin de fichero
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
				} else if (preanalisis == '!') {
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
				} else if (preanalisis == '/') {
					transita(105);
				} else {
					return new Token(TipoToken.ERROR,errorGenerico);
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
				} else if(letra()){
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR, error);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;
			case 2:	
				if(digito >= 0 && digito <= 7) {
					parteEntera = parteEntera*8 + digito;
					parteEnteraB10 = parteEnteraB10*10 + digito;
					transita(3);
				} else if(digito == 8 || digito == 9) {
					transita(14);
				} else if(preanalisis == '.') {
					transita(16);
				} else if(preanalisis == 'x' || preanalisis == 'X') {
					transita(4);
				} else if(preanalisis == 'l') {
					transita(8);
				} else if(preanalisis == 'L') {
					transita(5);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(6);
				} else if(letra()){
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;
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
					parteEntera = parteEnteraB10;
					transita(16);
				} else if(preanalisis == 'e' || preanalisis == 'E') {
					transita(17);
				} else if(preanalisis == 'l') {
					transita(8);
				} else if(preanalisis == 'L') {
					transita(5);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(6);
				} else if(letra()) {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;
			case 4:	
				if(hex()){
					int hex = valHex();
					parteEntera = parteEntera*16 + hex;
					transita(7);
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorVI);
					return new Token(TipoToken.ERROR,error);
				}
				break;
			case 5:	
				if(preanalisis == 'L') {
					transita(13);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(11);
				} else if(digito() || letra()) {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;
			case 6: 
				if(preanalisis == 'l') {
					transita(12);
				} else if(preanalisis == 'L') {
					transita(10);					
				} else if(digito() || letra()) {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;
			case 7:
				if(hex()){
					int hex = valHex();
					parteEntera = parteEntera*16 + hex;
					transita(7);
				} else if(preanalisis == 'l') {
					transita(8);
				} else if(preanalisis == 'L') {
					transita(5);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(6);
				} else if(digito() || letra()) {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;
			case 8:	
				if(preanalisis == 'l') {
					transita(13);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(11);	
				} else if(digito() || letra()) {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;
//			case 9:	
			case 10:
				if(preanalisis == 'L') {
					transita(11);	
				} else if(digito() || letra()) {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;
			case 11:
				if(digito() || letra()) {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				//break;
			case 12:	
				if(preanalisis == 'l') {
					transita(11);	
				} else if(digito() || letra()) {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;
			case 13:	
				if(preanalisis == 'u' || preanalisis == 'U') {
						transita(11);	
				} else if(digito() || letra()) {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;
			case 14:
				if(digito >= 0 && digito <= 9) {
					parteDecimal = parteDecimal + digito*pesoDecimal;
					pesoDecimal = pesoDecimal/10;
					transita(14);
				} else if(preanalisis == '.') {
					transita(16);
				} else if(preanalisis == 'e' || preanalisis == 'E') {
					transita(17);
				} else{
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				}
				break;
			case 15:	
				if(digito >= 0 && digito <= 9) {
					parteDecimal = parteDecimal + digito*pesoDecimal;
					pesoDecimal = pesoDecimal/10;
					transita(16);
				} else if(esDelim2()){
					token = new Token(TipoToken.SEPARADOR, ".");
					return token;
				} else{
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);				
				}
				break;
			case 16:	
				if(digito >= 0 && digito <= 9) {
					parteDecimal = parteDecimal + digito*pesoDecimal;
					pesoDecimal = pesoDecimal/10;
					transita(16);
				} else if(preanalisis == 'e' || preanalisis == 'E') {
					transita(17);
				} else if(sufReal()) {
					transita(21);
				} else if(esDelim()){ 
					asterisco=true;
					return new Token(TipoToken.NUM_REAL, (parteEntera + parteDecimal)*Math.pow(10, signo*parteExponencial));
				} else{
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				}	
				break;
			case 17:	
				if(digito >= 0 && digito <= 9) {
					parteExponencial = parteExponencial*10 + digito;
					transita(19);
				} else if(preanalisis=='-'){
					signo = -1;
					transita(18); 
				} else if(preanalisis=='+'){
					signo = 1;
					transita(18); 
				} else{
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				}	
				break;
			case 18:
				if(digito >= 0 && digito <= 9) {
					parteExponencial = parteExponencial*10 + digito;
					transita(19);
				} else{
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				}	
				break;
			case 19:	
				if(digito >= 0 && digito <= 9) {
					parteExponencial = parteExponencial*10 + digito;
					transita(19);
				} else if(sufReal()) {
					transita(21);
				} else if(esDelim()){ 
					asterisco=true;
					return new Token(TipoToken.NUM_REAL, (parteEntera + parteDecimal)*Math.pow(10, signo*parteExponencial)); 
				} else{
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				}		
				break;
//			case 20:
			case 21:
				if(esDelim()){ 
					asterisco=true;
					return new Token(TipoToken.NUM_REAL, (parteEntera + parteDecimal)*Math.pow(10, signo*parteExponencial)); 
				} else{
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,error);
				}
				//break;
//			case 22:	
//			case 23:	
//			case 24:	
//			case 25:	
//			case 26:	
//			case 27:	
//			case 28:	
//			case 29:	
//			case 30:	
//			case 31:	
//			case 32:	
//			case 33:	
//			case 34:	
//			case 35:	
//			case 36:	
//			case 37:	
//			case 38:	
//			case 39:
			//OPERADORES
			case 41:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"+=");
					return token;
				} else if (preanalisis == '+') {					
					token = new Token(TipoToken.OP_ARITMETICO,"++");
					return token;
				} else if (esDelim2()) {					//valores al preanalisis DELIM2 - {+,=}
					asterisco = true;	
					token = new Token(TipoToken.OP_ARITMETICO,"+");
					return token;
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
				//break;
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
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {-,=}
					asterisco = true;
					return new Token(TipoToken.OP_ARITMETICO,"-");		
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
				//break;
			case 49:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"*=");
					return token;
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_ARITMETICO,"*");
					asterisco = true;
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
				break;
			case 52:
				if (preanalisis == '>') {
					token = new Token(TipoToken.SEPARADOR,"}");
					return token;
				} else if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"%=");
					return token;
				} else if (preanalisis == ':') {
					transita(56);
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {>,=,:}
					asterisco = true;
					return new Token(TipoToken.OP_ARITMETICO, "%");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
				break;
			case 56:
				if (preanalisis == '%') {
					transita(57);
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {%}
					asterisco = true;
					return new Token(TipoToken.SEPARADOR, "#");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
				break;
			case 57:
				if (preanalisis == ':') {
					return new Token(TipoToken.SEPARADOR,"##");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
			case 60:	
				if (preanalisis == '#') {
					return new Token(TipoToken.SEPARADOR,"##");
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {#}
					asterisco = true;
					return new Token(TipoToken.SEPARADOR,"#");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
//				break;
			case 62:
				if (preanalisis == '=') {
					return token = new Token(TipoToken.OP_COMPARACION,"!=");
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return token = new Token(TipoToken.OP_LOGICO, "!");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
//				break;
			case 64:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_COMPARACION,"==");
					return token;
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return new Token(TipoToken.OP_ASIGNACION,"=");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
//				break;
			case 68:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_COMPARACION,">=");
					return token;
				} else if (preanalisis == '>') {
					transita(69);
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {>,=}
					asterisco = true;
					return new Token(TipoToken.OP_COMPARACION,">");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
				break;
			case 69:
				if (preanalisis == '=') {
					return new Token(TipoToken.OP_ASIGNACION,">>=");
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO, ">>");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
//				break;
			case 75:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_COMPARACION,"<=");
					return token;
				} else if (preanalisis == '<') {
					transita(78);
				} else if (preanalisis == ':') {
					return new Token(TipoToken.SEPARADOR,"[");
				} else if (preanalisis == '%') {
					return new Token(TipoToken.SEPARADOR,"{");
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {<,%,=,:}
					asterisco = true;
					return new Token(TipoToken.OP_COMPARACION,"<");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
				break;
			case 78:
				if (preanalisis == '=') {
					return new Token(TipoToken.OP_ASIGNACION,"<<=");
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,"<<");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
//				break;
			case 81:
				if (preanalisis == '>') {
					return new Token(TipoToken.SEPARADOR,"]");
				} else if (preanalisis == ':') {
					return new Token(TipoToken.SEPARADOR,"::");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
			case 83:
				if (preanalisis == '|') {
					return new Token(TipoToken.OP_LOGICO,"||");
				} else if (preanalisis == '=') {
					return new Token(TipoToken.OP_ASIGNACION,"|=");
				} else if (esDelim()) {					//valores al preanalisis DELIM - {|,=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,"|");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
//				break;
			case 87:
				if (preanalisis == '&') {
					return new Token(TipoToken.OP_LOGICO,"&&");
				} else if (preanalisis == '=') {
					return new Token(TipoToken.OP_ASIGNACION,"&=");
				} else if (esDelim()) {					//valores al preanalisis DELIM - {&,=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,"&");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
//				break;
			case 91:
				if (preanalisis == '=') {
					return new Token(TipoToken.OP_LOGICO,"^=");
				} else if (esDelim()) {					//valores al preanalisis DELIM - {=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,"^");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
//				break;
			// RECONOCIMIENTO DE CADENAS, IDENTIFICADORES Y PALABRAS RESERVADAS
			case 97:	
				if (noDigito() || digito()) {
					lexema = lexema+preanalisis;
					transita(97);
				} else if(esDelim()) { //TODO Implementar TablaSimbolos
//					Token token = TablaSimbolos.getPalRes.Busca(lexema);
//					if(token == null) {	
//						token = TablaSimbolos.Busca(lexema);
//						if (token == null) //crea un token, compuesto de Identificador y un puntero a la tabla de simbolos
//							token = new Token(lexema, TablaSimbolos.Inserta(lexema)); 
//					}	
//					asterisco=true;
//					return token;
				}
				break;
//			case 98:
			case 99: //SI NO ES CAJON DESASTRE..
				/*if((preanalisis != '"') && (preanalisis != '\\') && (preanalisis != '\n') )
				{
					lexema = lexema+preanalisis;
					transita(99);
				} else if(preanalisis == '"') {
					return new Token(TipoToken.LIT_CADENA,lexema); // puntero a la TS
				}*/
				if(preanalisis == '"') {
					return new Token(TipoToken.LIT_CADENA,lexema); //TODO puntero a la TS
				} else	if((preanalisis == '\\') || (preanalisis != '\n'))	{
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				} else {	
					lexema = lexema+preanalisis;
					transita(99);
				} 
				break;
			case 100:
			case 101:
			case 102://SI NO ES CAJON DESASTRE..
				/*if((preanalisis != '\'') && (preanalisis != '\\') && (preanalisis != '\n') ) {
					lexema = lexema+preanalisis;
					transita(102);
				} else if(preanalisis == '\'') {
					return new Token(TipoToken.LIT_CARACTER,lexema);
				}*/
				if(preanalisis == '\''){
					return new Token(TipoToken.LIT_CARACTER,lexema);
				} else if ((preanalisis == '\n') || (preanalisis == '\\')){
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				} else {
					lexema = lexema+preanalisis;
					transita(102);
				}
				// Y este caso: no sería válido??
				// char c = '\\'; 
				break;
			case 105:
				if(preanalisis=='='){
					token = new Token(TipoToken.OP_ASIGNACION,"/=");
					return token;
				} else if(preanalisis=='*'){
					transita(107);
				} else if(preanalisis=='/'){
					transita(110);
				} else if(esDelim()){
					asterisco = true;
					return new Token(TipoToken.OP_ARITMETICO,"/");
				} else {
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				}
				break;
			case 107:
				if(preanalisis=='*'){
					transita(108);
				} else { //Cajon desastre....
					if(preanalisis=='\n')
						numlinea++;
					lexema = lexema + preanalisis;
					transita(107);
				}
				break;
			case 108:
				if(preanalisis=='/'){	//TODO Implementar TablaSimbolos
//					token = TablaSimbolos.Busca(lexema);
//					if (token == null) //crea un token, compuesto de Identificador y un puntero a la tabla de simbolos
//						token = new Token(TipoToken.COMENTARIO, TablaSimbolos.Inserta(lexema)); 
				}
				break;
			case 110:
				if(preanalisis=='\n'){ //TODO Implementar TablaSimbolos
//					token = TablaSimbolos.Busca(lexema);
//					if (token == null) //crea un token, compuesto de Identificador y un puntero a la tabla de simbolos
//						token = new Token(TipoToken.COMENTARIO, TablaSimbolos.Inserta(lexema));
				}
				else {
					lexema = lexema + preanalisis;
					transita(110);
				}
			} //switch
		} //while
		return token;
	}
	

	private boolean sufReal() {
		return preanalisis == 'f' || preanalisis == 'F'|| preanalisis == 'l'|| preanalisis == 'L'; 
	}

	private boolean esDelim() {	
		if (preanalisis == ' ' || preanalisis == '\t'|| preanalisis == '\r'|| preanalisis == '\n'|| preanalisis == '\0' /*fin fichero*/ ||
			preanalisis == '|' || preanalisis == ':' || preanalisis == '+' || preanalisis == '-' || preanalisis == '/' ||
			preanalisis == '*' || preanalisis == '<' || preanalisis == '>' || preanalisis == '=' || preanalisis == '&' || 
			preanalisis == '^' || preanalisis == '%' || preanalisis == '!' || preanalisis == '~' || preanalisis == '{' || 
			preanalisis == '}' || preanalisis == '[' || preanalisis == ']' || preanalisis == '(' || preanalisis == ')' ||
			preanalisis == '#' || preanalisis == ';' || preanalisis == '.' /*Este char esta en SEPARADORES pero en DELIM no Â¿Por quÃ©?*/	)
			return true;
		return false;
	}  
	
	private boolean esDelim2() {
	
		if (esDelim() || digito() || noDigito())
			return true;
		return false;
	}

	private boolean letra() {
		if(preanalisis >= 'a' && preanalisis <= 'z')
			return true;
		
		if(preanalisis >= 'A' && preanalisis <= 'Z')
			return true;
		
		return false;
		
	}
	private boolean noDigito() {
		if(letra() || preanalisis == '_')
			return true;
		
		return false;
	}

	private boolean digito() {
		if(preanalisis >= '0' && preanalisis <= '9')
			return true;
		return false;
	}
	
	private boolean hex() {
		return (preanalisis >= '0' && preanalisis <= '9') || 
				(preanalisis >= 'a' && preanalisis <= 'f') || 
				(preanalisis >= 'A' && preanalisis <= 'F');
	}
	
	private int valHex() {
		int hex = 0;
		if(preanalisis >= '0' && preanalisis <= '9')
			hex = preanalisis - '0';
		else if(preanalisis >= 'a' && preanalisis <= 'f')
			hex = 10 + preanalisis-'a';
		else if(preanalisis >= 'A' && preanalisis <= 'F')
			hex = 10 + preanalisis-'A';
		return hex;
	}

	private char getChar() {
		char caracter = 0;
		try {
			int c = entrada.read();
			if(c == -1) // EOF
				caracter = '\0';
			else
				caracter = (char)c;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		numcolumna++;
		return caracter;
	}
	
	public void transita(int est){
		preanalisis = getChar();
		estado = est;
	}
    
}
