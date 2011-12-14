package compilador.lexico;
import java.io.IOException;
import java.io.InputStream;

import compilador.lexico.Token;
import compilador.lexico.Token.TipoToken;
import compilador.tablaSimbolos.TablaSimbolos;


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
//	private ArrayList<Token> arraytokens; 
	
	/**
	 * Atributo que indica el caracter actual que leemos de la entrada
	 */
	private char preanalisis;
	
	/**
	 * Atributo que indica el estado del automata en el que nos encontramos
	 */
	private int estado;
	
	/**
	 * Atributo que indica si el estado final anterior tenia asterisco o no,
	 * necesario para comprobar si tenemos que leer otro caracter, o procesar el ultimo
	 */
	private boolean asterisco;
	
	//private boolean asterisco2;
	//private Token token2;
	
	
	/**
	 * Atributo para poder buscar las palabras reservadas e identificadores en la Table de
	 * simbolos, e insertar los nuevos identificadores
	 */
	private TablaSimbolos tablaSimbolos;
	
	
//	private String errorGenerico = "Error en linea "+numlinea+" y columna "+numcolumna; // Este es el mismo error tipo I
//	private final String errorVI = "se esperaba un digito hexadecimal.";
//	private final String errorV = "numero mal formado.";
//	private final String errorIII = "caracter no esperado.";
//	private final String errorIV = "fin de comentario esperado.";
//	private final String errorI = "caracter no permitido.";
	
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
		//this.asterisco2 = false;
		this.tablaSimbolos = new TablaSimbolos();
	}

	/**
	 * M�todo que devuelve el siguiente token
	 * @return el siguiente token
	 */
	public Token scanner(){
		estado = 0;
		Token token = null;
		boolean fin = false; // parece que no lo usamos
		String lexema = "";
		/*if(asterisco2){
			return token2;
		}
		asterisco2 = false;*/
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
					token = new Token(TipoToken.SEPARADOR,"}");
					return token;
				} else if (preanalisis == '~') {
					token = new Token(TipoToken.OP_LOGICO,"~");
					return token;
				} else if (preanalisis == ';') {
					token = new Token(TipoToken.SEPARADOR, ";");
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
					token = new Token(TipoToken.SEPARADOR,"[");
					return token;
				} else if (preanalisis == '{') {
					token = new Token(TipoToken.SEPARADOR, "{");
					return token;
				} else if (preanalisis == '<') {
					transita(75);
				} else if (preanalisis == ':') {
					transita(81);
				} else if (preanalisis == ']') {
					token = new Token(TipoToken.SEPARADOR, "]");
					return token;
				} else if (preanalisis == '|') {
					transita(83);					
				} else if (preanalisis == '&') {
					transita(87);
				} else if (preanalisis == '^') {
					transita(91);
				} else if (preanalisis == '(') {
					token = new Token(TipoToken.SEPARADOR,"(");
					return token;
				} else if (preanalisis == ')') {
					token = new Token(TipoToken.SEPARADOR, ")");
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
					token = new Token(TipoToken.ERROR,null);
					//insertar en G.E.
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
				} else if(letra()){
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					token = new Token(TipoToken.ERROR, null);
					//insertar en G.E.
					return token;
				} else {
					asterisco = true;
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					return token;
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					token = new Token(TipoToken.ERROR,null);
					//insertar en G.E.
					return token;
				} else {
					asterisco = true;
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					return token;
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					token = new Token(TipoToken.ERROR,null);
					//insertar en G.E.
					return token;
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorVI);
					token = new Token(TipoToken.ERROR,null);
					//insertar en G.E.
					return token;
				}
				break;
			case 5:	
				if(preanalisis == 'L') {
					transita(13);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(11);
				} else if(digito() || letra()) {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;
			case 11:
				if(digito() || letra()) {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				//break;
			case 12:	
				if(preanalisis == 'l') {
					transita(11);	
				} else if(digito() || letra()) {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;
			case 13:	
				if(preanalisis == 'u' || preanalisis == 'U') {
						transita(11);	
				} else if(digito() || letra()) {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 15:	
				if(digito >= 0 && digito <= 9) {
					parteDecimal = parteDecimal + digito*pesoDecimal;
					pesoDecimal = pesoDecimal/10;
					transita(16);
				} else if(esDelim2()){
					token = new Token(TipoToken.SEPARADOR, ".");
					asterisco = true;
					return token;
				} else{
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);				
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
				}	
				break;
			case 18:
				if(digito >= 0 && digito <= 9) {
					parteExponencial = parteExponencial*10 + digito;
					transita(19);
				} else{
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
				}		
				break;

			case 21:
				if(esDelim()){ 
					asterisco=true;
					return new Token(TipoToken.NUM_REAL, (parteEntera + parteDecimal)*Math.pow(10, signo*parteExponencial)); 
				} else{
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorV);
					return new Token(TipoToken.ERROR,null);
				}
			
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}
		
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}
		
			case 49:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"*=");
					return token;
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_ARITMETICO,"*");
					asterisco = true; //Que pasa si ponemos * reifiriendonos a un puntero??
					return token;
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 56:
				if (preanalisis == '%') {
					transita(57);
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {%}
					asterisco = true;
					return new Token(TipoToken.SEPARADOR, "#");
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 57:
				if (preanalisis == ':') {
					return new Token(TipoToken.SEPARADOR,"##");
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}
			case 60:	
				if (preanalisis == '#') {
					return new Token(TipoToken.SEPARADOR,"##");
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {#}
					asterisco = true;
					return new Token(TipoToken.SEPARADOR,"#");
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}

			case 62:
				if (preanalisis == '=') {
					return token = new Token(TipoToken.OP_COMPARACION,"!=");
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return token = new Token(TipoToken.OP_LOGICO, "!");
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}

			case 64:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_COMPARACION,"==");
					return token;
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return new Token(TipoToken.OP_ASIGNACION,"=");
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}
				
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 69:
				if (preanalisis == '=') {
					return new Token(TipoToken.OP_ASIGNACION,">>=");
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO, ">>");
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}

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
				/*} else if (preanalisis != '\\' && preanalisis != '\n'){
					lexema = lexema+preanalisis;
					transita(111);*/
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {<,%,=,:}
					asterisco = true;
					return new Token(TipoToken.OP_COMPARACION,"<");
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 78:
				if (preanalisis == '=') {
					return new Token(TipoToken.OP_ASIGNACION,"<<=");
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,"<<");
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}

			case 81:
				if (preanalisis == '>') {
					return new Token(TipoToken.SEPARADOR,"]");
				} else if (preanalisis == ':') {
					return new Token(TipoToken.SEPARADOR,"::");
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
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
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}

			case 87:
				if (preanalisis == '&') {
					return new Token(TipoToken.OP_LOGICO,"&&");
				} else if (preanalisis == '=') {
					return new Token(TipoToken.OP_ASIGNACION,"&=");
				} else if (esDelim()) {					//valores al preanalisis DELIM - {&,=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,"&");
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}

			case 91:
				if (preanalisis == '=') {
					return new Token(TipoToken.OP_LOGICO,"^=");
				} else if (esDelim()) {					//valores al preanalisis DELIM - {=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,"^");
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}

			// RECONOCIMIENTO DE CADENAS, IDENTIFICADORES Y PALABRAS RESERVADAS
			case 97:	
				if (noDigito() || digito()) {
					lexema = lexema+preanalisis;
					transita(97);
				} else if(esDelim()) { 
					 Integer puntTS = tablaSimbolos.BuscaPalRes(lexema);
					 if(puntTS == null ) // es un identificador	
					 {	 
						String lex = tablaSimbolos.BuscaId(lexema);
						if (lex == null) { //crea un token, compuesto de Identificador y un puntero a la tabla de simbolos
							tablaSimbolos.insertaIdentificador(lexema);
							token = new Token(TipoToken.IDENTIFICADOR,lexema);
						}
						else {
							token = new Token(TipoToken.IDENTIFICADOR,lex);
						}
					 }	
					 else { // es una palabra reservada
						 token = new Token(TipoToken.PAL_RESERVADA,puntTS);
					 }
					 asterisco=true;
					 return token;
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorI);
					return new Token(TipoToken.ERROR,null);
				}
				break;
				
			case 99: 
				if(preanalisis == '"') {
					return new Token(TipoToken.LIT_CADENA,lexema);
				} else	if( !esCajonDesastre() || (preanalisis == '\\') || (preanalisis == '\n'))	{
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				} else {	
					lexema = lexema+preanalisis;
					transita(99);
				} 
				break;
			case 100:
			case 101:
			case 102:
				if(preanalisis == '\''){
					return new Token(TipoToken.LIT_CARACTER,lexema);
				} else if ( !esCajonDesastre() || (preanalisis == '\n') /*|| (preanalisis == '\\')*/){
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				} else {
					lexema = lexema+preanalisis;
					transita(102);
				}
				// Y este caso: no seria valido?? yo creo que si es valido.. (cris)
				// char c = '\\'; 
				break;
			case 105:
				if(preanalisis=='='){
					token = new Token(TipoToken.OP_ASIGNACION,"/=");
					return token;
				} else if(preanalisis=='*'){ // reconoce algo tipo Token Comentario, que puede estar seguido de / o de *
					transita(107);
				} else if(preanalisis=='/'){ // reconoce algo tipo Token Comentario de linea
					transita(110);
				} else if(esDelim()){
					asterisco = true;
					return new Token(TipoToken.OP_ARITMETICO,"/");
				} else {
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 107:
				if(preanalisis=='*'){
					transita(108);
				} else if (preanalisis == '\0')  { //error ya que termina el fichero y el comentario nunca se cierra
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIV);
					return new Token(TipoToken.ERROR,null);
				} else {
					if(preanalisis=='\n') 
						numlinea++;
					lexema = lexema + preanalisis;
					transita(107);
				}	
				
				break;
			case 108:
				if(preanalisis=='/'){
					//tablaSimbolos.inserta(lexema)
					return new Token(TipoToken.COMENT_LARGO, lexema);
					// Aqui habria que almacenar algo como la linea y la columna donde esta el comentario?
					// Tomas: Yo no creo, por parte del sintactico, se trataria igual que los demas tokens, no? 
				}
				else if (preanalisis == '\0')  { //error ya que termina el fichero y el comentario nunca se cierra
					//ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIV);
					return new Token(TipoToken.ERROR,null);
				}
				else	
					transita(107);
				break;
			case 110:
				if(preanalisis=='\n' || preanalisis=='\0'){  //Ya que es el comentario de linea (//) acaba cuando recibe un /r/n
					//tablaSimbolos.inserta(lexema)
					return new Token(TipoToken.COMENT_LINEA, lexema);//Aqui habria que almacenar algo como la linea y la columna donde esta el comentario?
				} else {
					lexema = lexema + preanalisis;
					transita(110);
				}/*
				break;
			case 111: 
				if(preanalisis == '>') {
					return new Token(TipoToken.NOM_LIBRERIA,lexema);
				} else if(esDelim()){ 
					 Integer puntTS = tablaSimbolos.BuscaPalRes(lexema);
					 if(puntTS == null ) // es un identificador	
					 {	 
						String lex = tablaSimbolos.BuscaId(lexema);
						if (lex == null) { //crea un token, compuesto de Identificador y un puntero a la tabla de simbolos
							tablaSimbolos.insertaIdentificador(lexema);
							token2 = new Token(TipoToken.IDENTIFICADOR,lexema);
						}
						else {
							token2 = new Token(TipoToken.IDENTIFICADOR,lex);
						}
					 }	
					 else { // es una palabra reservada
						 token2 = new Token(TipoToken.PAL_RESERVADA,puntTS);
					 }
					 asterisco2 = true;
					 asterisco = true;
					 return new Token(TipoToken.OP_COMPARACION,"<");
				} else if( !esCajonDesastre() || (preanalisis == '\\') || (preanalisis == '\n'))	{
					ErrorLexico error = new ErrorLexico(numlinea, numcolumna, errorIII);
					return new Token(TipoToken.ERROR,error);
				} else {	
					lexema = lexema+preanalisis;
					transita(111);
				}*/
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
			preanalisis == '#' || preanalisis == ';' || preanalisis == '.' /*Este char esta en SEPARADORES pero en DELIM no ¿Por qué?*/	)
			return true;
		return false;
	}  
	
	private boolean esDelim2() {
	
		if (esDelim() || digito() || noDigito())
			return true;
		return false;
	}
	
	private boolean esCajonDesastre() {
		
		if(letra()	||	digito() ||	preanalisis == '_'	||	preanalisis == ',' ||  preanalisis == ' '||	preanalisis== '-'||
			preanalisis=='*' ||	preanalisis=='+'||	preanalisis=='#' || preanalisis=='(' ||	preanalisis==')' ||	
			preanalisis=='<' || preanalisis=='}'||	preanalisis=='>' || preanalisis=='{' ||	preanalisis=='!' ||					
			preanalisis=='%' ||	preanalisis==':'||	preanalisis==';' ||	preanalisis=='.' ||	preanalisis== '?'||
			preanalisis=='-' ||	preanalisis=='/'||	preanalisis=='^' ||	preanalisis=='&' ||	preanalisis=='|' ||	
			preanalisis=='=' || preanalisis==','||  preanalisis=='\\'||	preanalisis=='"'||	preanalisis=='\'' || 
			preanalisis=='[' || preanalisis==']') 
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
	
	private void transita(int est){
		preanalisis = getChar();
		estado = est;
	}
    
}
