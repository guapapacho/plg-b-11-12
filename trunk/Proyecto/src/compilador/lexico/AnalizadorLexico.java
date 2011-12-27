package compilador.lexico;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import compilador.gestionErrores.*;
import compilador.gestionErrores.GestorErrores.TError;
import compilador.lexico.Token.*;
import compilador.tablaSimbolos.GestorTablasSimbolos;


/**
 * 
 * @author Grupo 1
 *
 */
public class AnalizadorLexico {

	/**
	 * Gestor de errores
	 */
	private GestorErrores gestorErrores;

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
	
	/**
	 * Atributo para poder buscar las palabras reservadas e identificadores en la Tabla de
	 * simbolos, e insertar los nuevos identificadores
	 */
	private GestorTablasSimbolos gestorTS;
	
	
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
		this.gestorTS = new GestorTablasSimbolos();
		this.gestorErrores = new GestorErrores();
	}

	/**
	 * Metodo que devuelve el siguiente token
	 * @return token
	 */
	public Token scanner(){
		estado = 0;
		Token token = null;
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
		
		while (true) {
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
					token = new Token(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE);
					return token;
				} else if (preanalisis == '~') {
					token = new Token(TipoToken.OP_LOGICO, OpLogico.SOBRERO);
					return token;
				} else if (preanalisis == ';') {
					token = new Token(TipoToken.SEPARADOR, Separadores.PUNTO_COMA);
					return token;
				} else if (preanalisis == '?') {
					token = new Token(TipoToken.SEPARADOR, Separadores.INTEROGACION);
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
					token = new Token(TipoToken.SEPARADOR, Separadores.ABRE_CORCHETE);
					return token;
				} else if (preanalisis == '{') {
					token = new Token(TipoToken.SEPARADOR, Separadores.ABRE_LLAVE);
					return token;
				} else if (preanalisis == '<') {
					transita(75);
				} else if (preanalisis == ':') {
					transita(81);
				} else if (preanalisis == ']') {
					token = new Token(TipoToken.SEPARADOR, Separadores.CIERRA_CORCHETE);
					return token;
				} else if (preanalisis == '|') {
					transita(83);					
				} else if (preanalisis == '&') {
					transita(87);
				} else if (preanalisis == '^') {
					transita(91);
				} else if (preanalisis == '(') {
					token = new Token(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS);
					return token;
				} else if (preanalisis == ')') {
					token = new Token(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS);
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
				} else if (preanalisis == ',') {
					return new Token(TipoToken.SEPARADOR, Separadores.COMA);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);					
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
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR, null);
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
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
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
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
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
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 5:	
				if(preanalisis == 'L') {
					transita(13);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(11);
				} else if(digito() || letra()) {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
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
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
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
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
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
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;	
			case 10:
				if(preanalisis == 'L') {
					transita(11);	
				} else if(digito() || letra()) {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
				break;
			case 11:
				if(digito() || letra()) {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera);
				}
			case 12:	
				if(preanalisis == 'l') {
					transita(11);	
				} else if(digito() || letra()) {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
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
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
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
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 15:	
				if(digito >= 0 && digito <= 9) {
					parteDecimal = parteDecimal + digito*pesoDecimal;
					pesoDecimal = pesoDecimal/10;
					transita(16);
				} else if(esDelim2()){
					token = new Token(TipoToken.SEPARADOR, Separadores.PUNTO);
					asterisco = true;
					return token;
				} else{
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
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
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
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
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}	
				break;
			case 18:
				if(digito >= 0 && digito <= 9) {
					parteExponencial = parteExponencial*10 + digito;
					transita(19);
				} else{
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}	
				break;
			case 19:	
				if(digito >= 0 && digito <= 9) {
					parteExponencial = parteExponencial*10 + digito;
					transita(19);
				} else if(sufReal()) {
					transita(23);
				} else if(esDelim()){ 
					asterisco=true;
					String s = (signo > 0) ? "+" : "-";
					return new Token(TipoToken.NUM_REAL_EXPO, parteEntera+parteDecimal+"E"+s+parteExponencial);  
				} else{
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}		
				break;
			case 21:
				if(esDelim()){ 
					asterisco=true;
					return new Token(TipoToken.NUM_REAL, (parteEntera + parteDecimal)*Math.pow(10, signo*parteExponencial)); 
				} else{
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
			case 23:
				if(esDelim()){ 
					asterisco=true;
					String s = (signo > 0) ? "+" : "-";
					return new Token(TipoToken.NUM_REAL_EXPO, parteEntera+parteDecimal+"E"+s+parteExponencial); 
				} else{
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
			//OPERADORES
			case 41:
				if (preanalisis == '=') { 			// +=
					token = new Token(TipoToken.OP_ASIGNACION,OpAsignacion.MAS_IGUAL);
					return token;
				} else if (preanalisis == '+') { 	// ++					
					token = new Token(TipoToken.OP_ARITMETICO,OpAritmetico.INCREMENTO);
					return token;
				} else if (esDelim2()) { 			// + //valores al preanalisis DELIM2 - {+,=}
					asterisco = true;	
					token = new Token(TipoToken.OP_ARITMETICO,OpAritmetico.SUMA);
					return token;
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
		
			case 45:
				if (preanalisis == '=') { 			// -=
					token = new Token(TipoToken.OP_ASIGNACION,OpAsignacion.MENOS_IGUAL);
					return token;
				} else if (preanalisis == '-') { 	// --					
					token = new Token(TipoToken.OP_ARITMETICO,OpAritmetico.DECREMENTO);
					return token;
				} else if (preanalisis == '>') { 	// ->					
					token = new Token(TipoToken.OP_ASIGNACION,OpAsignacion.PUNTERO);
					return token;
				} else if (esDelim2()) { 			// - //valores al preanalisis DELIM2 - {-,=}
					asterisco = true;
					return new Token(TipoToken.OP_ARITMETICO,OpAritmetico.RESTA);		
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
		
			case 49:
				if (preanalisis == '=') { 			// *=
					token = new Token(TipoToken.OP_ASIGNACION,OpAsignacion.POR_IGUAL);
					return token;
				} else if (esDelim2()) { 			// * //valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_ARITMETICO,OpAritmetico.MULTIPLICACION);
					asterisco = true;
					return token;
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
			case 52:
				if (preanalisis == '>') { 			// %> equivalente a }
					token = new Token(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE);
					return token;
				} else if (preanalisis == '=') { 	// %=
					token = new Token(TipoToken.OP_ASIGNACION,OpAsignacion.PORCENTAJE_IGUAL);
					return token;
				} else if (preanalisis == ':') {	// %:
					transita(56);
				} else if (esDelim2()) { 			// % //valores al preanalisis DELIM2 - {>,=,:}
					asterisco = true;
					return new Token(TipoToken.OP_ARITMETICO, OpAritmetico.PORCENTAJE);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 56:
				if (preanalisis == '%') {			// %:%
					transita(57);
				} else if (esDelim2()) { 			// %: equivalente a #
					asterisco = true;
					return new Token(TipoToken.SEPARADOR, Separadores.ALMOHADILLA);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 57:
				if (preanalisis == ':') {			// %:%: equivalente a ##
					return new Token(TipoToken.SEPARADOR, Separadores.DOBLE_ALMOHADILLA);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
			case 60:	
				if (preanalisis == '#') {			// ##
					return new Token(TipoToken.SEPARADOR, Separadores.DOBLE_ALMOHADILLA);
				} else if (esDelim2()) { 			// # //valores al preanalisis DELIM2 - {#}
					asterisco = true;
					return new Token(TipoToken.SEPARADOR, Separadores.ALMOHADILLA);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}

			case 62:
				if (preanalisis == '=') {			// !=
					return token = new Token(TipoToken.OP_COMPARACION, OpComparacion.DISTINTO);
				} else if (esDelim2()) { 			// ! //valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return token = new Token(TipoToken.OP_LOGICO, OpLogico.NOT);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}

			case 64:
				if (preanalisis == '=') {			// ==
					token = new Token(TipoToken.OP_COMPARACION, OpComparacion.IGUALDAD);
					return token;
				} else if (esDelim2()) {			// = //valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return new Token(TipoToken.OP_ASIGNACION, OpAsignacion.ASIGNACION);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
				
			case 68:
				if (preanalisis == '=') {			// >=
					token = new Token(TipoToken.OP_COMPARACION,OpComparacion.MAYOR_IGUAL);
					return token;
				} else if (preanalisis == '>') {	// >>
					transita(69);
				} else if (esDelim2()) {			// > //valores al preanalisis DELIM2 - {>,=}
					asterisco = true;
					return new Token(TipoToken.OP_COMPARACION,OpComparacion.MAYOR);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 69:
				if (preanalisis == '=') {			// >>=
					return new Token(TipoToken.OP_ASIGNACION,OpAsignacion.MAYOR_MAYOR_IGUAL);
				} else if (esDelim2()) {			// >> //valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO, OpLogico.DOS_MAYORES);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
			case 75:
				if (preanalisis == '=') {			// <=
					token = new Token(TipoToken.OP_COMPARACION,OpComparacion.MENOR_IGUAL);
					return token;
				} else if (preanalisis == '<') {	// <<
					transita(78);
				} else if (preanalisis == ':') {	// <: equivalente a [
					return new Token(TipoToken.SEPARADOR,Separadores.ABRE_CORCHETE);
				} else if (preanalisis == '%') {	// <% equivalente a {
					return new Token(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE);
				} else if (esDelim2()) {			// < //valores al preanalisis DELIM2 - {<,%,=,:}
					asterisco = true;
					return new Token(TipoToken.OP_COMPARACION,OpComparacion.MENOR);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 78:
				if (preanalisis == '=') {			// <<=
					return new Token(TipoToken.OP_ASIGNACION,OpAsignacion.MENOR_MENOR_IGUAL);
				} else if (esDelim2()) {			// << //valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,OpLogico.DOS_MENORES);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
			case 81:
				if (preanalisis == '>') {			// :> equivalente a ]
					return new Token(TipoToken.SEPARADOR,Separadores.CIERRA_CORCHETE);
				} else if (preanalisis == ':') {	// ::
					return new Token(TipoToken.SEPARADOR,Separadores.DOBLE_DOSPUNTOS);
				} else if (esDelim2()) {			// :
					asterisco = true;
					return new Token(TipoToken.SEPARADOR,Separadores.DOS_PUNTOS);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
			case 83:
				if (preanalisis == '|') {			// ||
					return new Token(TipoToken.OP_LOGICO,OpLogico.OR);
				} else if (preanalisis == '=') {	// |=
					return new Token(TipoToken.OP_ASIGNACION,OpAsignacion.OR_IGUAL);
				} else if (esDelim2()) {			// | //valores al preanalisis DELIM - {|,=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,OpLogico.BIT_OR);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
			case 87:
				if (preanalisis == '&') {			// &&
					return new Token(TipoToken.OP_LOGICO,OpLogico.AND);
				} else if (preanalisis == '=') {	// &=
					return new Token(TipoToken.OP_ASIGNACION,OpAsignacion.AND_IGUAL);
				} else if (esDelim2()) {			// & //valores al preanalisis DELIM - {&,=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,OpLogico.BIT_AND);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
			case 91:
				if (preanalisis == '=') {			// ^=
					return new Token(TipoToken.OP_ASIGNACION,OpAsignacion.CIRCUNFLEJO_IGUAL);
				} else if (esDelim2()) {			// ^ //valores al preanalisis DELIM - {=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,OpLogico.CIRCUNFLEJO);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
			// RECONOCIMIENTO DE CADENAS, IDENTIFICADORES Y PALABRAS RESERVADAS
			case 97:	
				if (noDigito() || digito()) {
					lexema = lexema+preanalisis;
					transita(97);
				} else if(esDelim()) { 
					 Integer puntTS = gestorTS.buscaPalRes(lexema);
					 if(puntTS == null ) //si es un identificador	
					 {	 
						puntTS = gestorTS.buscaIdGeneral(lexema);
						if (puntTS == -1) { //si no esta en la T.S. se inserta
							token = new Token(TipoToken.IDENTIFICADOR,gestorTS.insertaIdentificador(lexema));
						}
						else {
							token = new Token(TipoToken.IDENTIFICADOR,puntTS);
						}
					 }	
					 else { // es una palabra reservada
						 token = new Token(TipoToken.PAL_RESERVADA,puntTS);
					 }
					 asterisco=true;
					 return token;
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 99: 
				if(preanalisis == '"') {
					return new Token(TipoToken.LIT_CADENA,lexema);
				} else	if( !esCajonDesastre() || (preanalisis == '\\') || (preanalisis == '\n'))	{
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				} else {	
					lexema = lexema+preanalisis;
					transita(99);
				} 
				break;
			case 102:
				if (preanalisis == '\\') { // barra de escape
					lexema = lexema+preanalisis;
					transita(116);
				} else if ( !esCajonDesastre() || (preanalisis == '\n') || (preanalisis == '\'')){
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				} else {
					lexema = lexema+preanalisis;
					transita(103);
				}
				break;
			case 103:
				if(preanalisis == '\''){ // fin de literal caaracter
					return new Token(TipoToken.LIT_CARACTER,lexema);
				} else if (preanalisis == '\\') { // barra de escape
					lexema = lexema+preanalisis;
					transita(116);
				} else if ( !esCajonDesastre() || (preanalisis == '\n')){
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				} else {
					lexema = lexema+preanalisis;
					transita(103);
				}
				break;
			case 105:
				if(preanalisis=='='){			// /=
					token = new Token(TipoToken.OP_ASIGNACION,OpAsignacion.DIV_IGUAL);
					return token;
				} else if(preanalisis=='*'){ 	// /* // empieza un comentario de varias lineas
					transita(107);
				} else if(preanalisis=='/'){	// // // empieza un comentario de una linea
					transita(110);
				} else if(esDelim()){			// /
					asterisco = true;
					return new Token(TipoToken.OP_ARITMETICO,OpAritmetico.DIVISION);
				} else {
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			case 107:	// lee contenido de un comentario de varias lineas
				if(preanalisis=='*'){
					transita(108);
				} else if (preanalisis == '\0')  { //error ya que termina el fichero y el comentario nunca se cierra
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
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
					//TODO insertar en T.S.
					return new Token(TipoToken.COMENT_LARGO, lexema); 
				}
				else if (preanalisis == '\0')  { //error ya que termina el fichero y el comentario nunca se cierra
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
				else	
					transita(107);
				break;
			case 110:
				if(preanalisis=='\n' || preanalisis=='\0'){  //Ya que es el comentario de linea (//) acaba cuando recibe un \r\n
					//TODO insertar en T.S.
					return new Token(TipoToken.COMENT_LINEA, lexema);
				} else {
					lexema = lexema + preanalisis;
					transita(110);
				}
				break;
			case 116:
				if(esSecuenciaEscapeSimple()) {
					lexema = lexema + preanalisis;
					transita(103);
				} else { // secuencia de escape simple no válida
					//insertar en G.E.
					gestorErrores.insertaError(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null);
				}
				break;
			} //switch
		} //while
	}
	

	private boolean sufReal() {
		return preanalisis == 'f' || preanalisis == 'F'|| preanalisis == 'l'|| preanalisis == 'L'; 
	}

	private boolean esDelim() {	
		if (preanalisis == ' ' || preanalisis == '\t'|| preanalisis == '\r'|| preanalisis == '\n'|| preanalisis == '\0'|| /*fin fichero*/ 
			preanalisis == '|' || preanalisis == ':' || preanalisis == '+' || preanalisis == '-' || preanalisis == '/' ||
			preanalisis == '*' || preanalisis == '<' || preanalisis == '>' || preanalisis == '=' || preanalisis == '&' || 
			preanalisis == '^' || preanalisis == '%' || preanalisis == '!' || preanalisis == '~' || preanalisis == '{' || 
			preanalisis == '}' || preanalisis == '[' || preanalisis == ']' || preanalisis == '(' || preanalisis == ')' ||
			preanalisis == '#' || preanalisis == ';' || preanalisis == '.' || preanalisis == ',' || preanalisis == '?')
			return true;
		return false;
	}  
	
	private boolean esDelim2() {
	
		if (esDelim() || digito() || noDigito())
			return true;
		return false;
	}
	
	private boolean esCajonDesastre() {
		
		if(letra()	||	digito() ||	preanalisis == '_'	||	preanalisis == ' '||	preanalisis== '-'||
			preanalisis=='*' ||	preanalisis=='+'||	preanalisis=='#' || preanalisis=='(' ||	preanalisis==')' ||	
			preanalisis=='<' || preanalisis=='}'||	preanalisis=='>' || preanalisis=='{' ||	preanalisis=='!' ||					
			preanalisis=='%' ||	preanalisis==':'||	preanalisis==';' ||	preanalisis=='.' ||	preanalisis== '?'||
			preanalisis=='-' ||	preanalisis=='/'||	preanalisis=='^' ||	preanalisis=='&' ||	preanalisis=='|' ||	
			preanalisis=='=' || preanalisis==','||  preanalisis=='\\'||	preanalisis=='"'||	preanalisis=='\'' || 
			preanalisis=='[' || preanalisis==']') 
				return true;
		return false;
																														
	}
	
	private boolean esSecuenciaEscapeSimple() {
	
		if(preanalisis == '\''	  || preanalisis == '"'|| preanalisis== '?'||
				preanalisis=='\\' || preanalisis=='a'  || preanalisis=='b' || preanalisis=='f' ||
				preanalisis=='n'  || preanalisis=='r'  || preanalisis=='t' || preanalisis=='v') 
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
			e.printStackTrace();
		}
		numcolumna++;
		return caracter;
	}
	public ArrayList<TError> devuelveErrorLex(){
		return gestorErrores.devuelveErrores();
		
	}
	private void transita(int est){
		preanalisis = getChar();
		estado = est;
	}
    
}
