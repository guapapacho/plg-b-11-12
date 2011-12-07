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
	private int numlinea; //LINEA Y COLUMNA ESTAN AQUI Y EN LA CLASE TOKEN, NO DEBERIAN ESTAR SOLO AQUI??
	
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
	private final String errorVI = ", se esperaba un digito hexadecimal.";
	private final String errorV = ", numero mal formado.";
	
	
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
		float parteDecimal = 0;
		int parteExponencial = 0;
		int signo = 1;
		
		while (! fin) {//Una pregunta: ¿Dónde se reinician todas estas variables que tiene scanner (parteEntera, lexema, etc)?
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
				// Tomas: NO IBAMOS A QUITAR LO DE LOS SUFIJOS??? 
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(6);
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;
				}
				/* Tomas: he comentado esto porque no me cuadraba... (no tiene que ser el caso de error de arriba?)
				else {
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
				}*/
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
				// Tomas: NO IBAMOS A QUITAR LO DE LOS SUFIJOS???	
				} else if(preanalisis == 'l') {
					transita(8);
				} else if(preanalisis == 'L') {
					transita(5);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(6);
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;
				}
				/* Tomas: lo mismo de arriba....
				else {
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
				}*/
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
				// Tomas: NO IBAMOS A QUITAR LO DE LOS SUFIJOS??? Sí por favor!!! (Cris)	
				} else if(preanalisis == 'l') {
					transita(8);
				} else if(preanalisis == 'L') {
					transita(5);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(6);
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;
				}
				/* Tomas: lo mismo de arriba....
				else {
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
				}*/
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
					token = new Token(TipoToken.ERROR,errorGenerico+errorVI);
					return token;
				}
				//break;
			case 5:	
				if(preanalisis == 'L') {
					transita(13);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(11);
				} else if(esDelim()){
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
					// FALTA ESTO??
					//return token;
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;					
				}
				//break;
			case 6: 
				//No est�n los casos de los sufijos...	
				if(esDelim()){
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
					// FALTA ESTO??
					//return token;
				
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;					
				}
				// break;
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
				} else if(esDelim()){
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
					// FALTA ESTO??
					//return token;
				
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;					
				}
				//break;
			case 8:	
				//No est�n los casos de los sufijos...	
				if(esDelim()){
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
					// FALTA ESTO??
					//return token;
				
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;					
				}
				//break;
			case 9:	
			case 10:
				//No est�n los casos de los sufijos...	
				if(esDelim()){
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
					// FALTA ESTO??
					//return token;
				
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;					
				}
				//break;
			case 11:
				//No est�n los casos de los sufijos...	
				if(esDelim()){
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
					// FALTA ESTO??
					//return token;
				
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;					
				}
				//break;
			case 12:	
				//No est�n los casos de los sufijos...	
				if(esDelim()){
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
					// FALTA ESTO??
					//return token;
				
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;					
				}
				//break;
			case 13:	
				//No est�n los casos de los sufijos...	
				if(esDelim()){
					token = new Token(TipoToken.NUM_ENTERO, parteEntera);
					asterisco = true;
					// FALTA ESTO??
					//return token;
				
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;					
				}
				//break;
			case 14:
				digito = preanalisis -'0';
				if(digito >= 0 && digito <= 9) {
					parteEntera = parteEntera*10 + digito;
				} else if(preanalisis == '.') {
					transita(16);
				} else if(preanalisis == 'e' || preanalisis == 'E') {
					transita(17);
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;					
				}
				//break;
			case 15:	
				digito = preanalisis -'0';
				if(digito >= 0 && digito <= 9) {
					parteEntera = parteEntera*10 + digito;
					transita(16);
				} else if(esDelim2()){
					token = new Token(TipoToken.SEPARADOR, ".");
					return token;
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;					
				}
				//break;
			case 16:	
				//No est�n los casos de los sufijos...	
				digito = preanalisis -'0';
				if(digito >= 0 && digito <= 9) {
					parteEntera = parteEntera*10 + digito;
					transita(16);
				} else if(esDelim()){ 
					token = new Token(TipoToken.NUM_REAL, Math.pow(parteEntera /*+ parteDecimal*/, parteExponencial) * signo /* + sufijoReal*/);
					asterisco=true;
					// FALTA ESTO??
					//return token; 
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;					
				}	
				//break;
			case 17:	
				digito = preanalisis -'0';
				if(digito >= 0 && digito <= 9) {
					parteExponencial = parteExponencial*10 + digito;
					transita(19);
				} else if(preanalisis=='-'){
					signo=-1;
					transita(18); 
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;					
				}	
				//break;
			case 18:
				digito = preanalisis -'0';
				if(digito >= 0 && digito <= 9) {
					parteExponencial = parteExponencial*10 + digito;
					transita(19);
				} else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;
				}	
				//break;
			case 19:	
				//No est�n los casos de los sufijos...	
				digito = preanalisis -'0';
				if(digito >= 0 && digito <= 9) {
					parteExponencial = parteExponencial*10 + digito;
					transita(19);
				} else if(esDelim()){ 
					token = new Token(TipoToken.NUM_REAL, Math.pow(parteEntera /*+ parteDecimal*/, parteExponencial) * signo /* + sufijoReal*/); 
					asterisco=true;
					// FALTA ESTO??
					//return token;
				}
				else{
					token = new Token(TipoToken.ERROR,errorGenerico+errorV);
					return token;
				}		
				//break;
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
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {+,=}
					token = new Token(TipoToken.OP_ARITMETICO,"+");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
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
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {-,=}
					token = new Token(TipoToken.OP_ARITMETICO,"-");		
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
				break;
			case 49:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"*=");
					return token;
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_ARITMETICO,"*");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
				break;
			case 52:
				if (preanalisis == '>') {
					token = new Token(TipoToken.SEPARADOR,"%>");
					return token;
				} else if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"%=");
					return token;
				} else if (preanalisis == ':') {
					transita(56);
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {>,=,:}
					token = new Token(TipoToken.OP_ARITMETICO, "%");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
				break;
			case 56:
				if (preanalisis == '%') {
					transita(57);
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {%}
					token = new Token(TipoToken.SEPARADOR, "%:");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
				break;
			case 57:
				if (preanalisis == ':') {
					token = new Token(TipoToken.SEPARADOR,"%:%:");
					return token;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
			case 60:	
				if (preanalisis == '#') {
					token = new Token(TipoToken.SEPARADOR,"##");
					return token;
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {#}
					token = new Token(TipoToken.SEPARADOR,"#");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
				break;
			case 62:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"/=");
					return token;
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_ARITMETICO, "/");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
				break;
			case 64:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_COMPARACION,"==");
					return token;
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_ASIGNACION,"=");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
				break;
			case 68:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_COMPARACION,">=");
					return token;
				} else if (preanalisis == '>') {
					transita(69);
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {>,=}
					token = new Token(TipoToken.OP_COMPARACION,">");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
				break;
			case 69:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,">>=");
					return token;
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_LOGICO, ">>");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
				break;
			case 75:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_COMPARACION,"<=");
					return token;
				}else if (preanalisis == '<') {
					transita(78);
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {<,%,=,:}
					token = new Token(TipoToken.OP_COMPARACION,"<");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
				break;
			case 78:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"<<=");
					return token;
				} else if (esDelim2()) {						//valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_LOGICO,"<<");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
				break;
			case 81:
				if (preanalisis == '>') {
					token = new Token(TipoToken.SEPARADOR,":>");
					return token;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
			case 83:
				if (preanalisis == '|') {
					token = new Token(TipoToken.OP_LOGICO,"||");
					return token;
				} else if (preanalisis == '=') {
					token = new Token(TipoToken.OP_ASIGNACION,"|=");
					return token;
				} else if (esDelim()) {					//valores al preanalisis DELIM - {|,=}
					token = new Token(TipoToken.OP_LOGICO,"|");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
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
				} else if (esDelim()) {					//valores al preanalisis DELIM - {&,=}
					token = new Token(TipoToken.OP_LOGICO,"&");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
				break;
			case 91:
				if (preanalisis == '=') {
					token = new Token(TipoToken.OP_LOGICO,"^=");
					return token;
				} else if (esDelim()) {					//valores al preanalisis DELIM - {=}
					token = new Token(TipoToken.OP_LOGICO,"^");
					asterisco = true;
				} else {
					token = new Token(TipoToken.ERROR,errorGenerico);
					return token;
				}
				break;
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
//					return token;
				}
				//break;
			case 98:
			case 99: //SI NO ES CAJON DESASTRE..
				if((preanalisis != '"') && (preanalisis != '\\') && (preanalisis != '\n') )
				{
					lexema = lexema+preanalisis;
					transita(99);
				} else if(preanalisis == '"') {
					return new Token(TipoToken.LIT_CADENA,lexema); // puntero a la TS
				}
				//break;
			case 100:
			case 101:
			case 102://SI NO ES CAJON DESASTRE..
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
	

	private boolean esDelim() {
	
		if (preanalisis == ' ' || preanalisis == '\t'|| preanalisis == '\n'|| preanalisis == '\f' /*fin fichero*/ ||
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
