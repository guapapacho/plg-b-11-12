package compilador.analizadorLexico;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import compilador.analizadorLexico.Token.*;
import compilador.gestionErrores.*;
import compilador.gestionErrores.GestorErrores.TError;
import compilador.gestionTablasSimbolos.EntradaTS;
import compilador.gestionTablasSimbolos.GestorTablasSimbolos;


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
	 * Atributo que guarda el lexema de cada token
	 */
	private String lexema;

	private String lexemaAnterior;
	
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

	private boolean hayComentario;
	private String comentario;
	
	/**
	 * Vector que guarda los modos activos del léxico
	 */
	private Vector<modo> modos;
	
	public enum modo {
		Declaracion, NoMeto, GoTo;
	}
	
	
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
		this.hayComentario = false;
		this.comentario = "";
		this.gestorErrores = GestorErrores.getGestorErrores();
		this.gestorTS = GestorTablasSimbolos.getGestorTS();
		modos = new Vector<modo>();
		activaModo(modo.Declaracion);
	}
	
	public String getLexemaAnterior(){
		return lexemaAnterior;
	}
	
	public String getLexema(){
		if(lexema!="")
			return lexema;
		else return preanalisis+"";
	}

	/**
	 * Metodo que devuelve el siguiente token
	 * @return token
	 * @throws Exception 
	 */
	public Token scan() throws Exception{
		estado = 0;
		Token token = null;
		lexemaAnterior = lexema;
		lexema = "";
		if(!asterisco)
			preanalisis = getChar();
		asterisco = false;
		if(!hayComentario)
			comentario = "";
		hayComentario = false;
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
					token = new Token(TipoToken.EOF,null, comentario);
					return token;
				} else if (preanalisis == ' ') {
					preanalisis = getChar();
				} else if (preanalisis == '}') {
					token = new Token(TipoToken.SEPARADOR, Separadores.CIERRA_LLAVE, comentario);
					return token;
				} else if (preanalisis == '~') {
					token = new Token(TipoToken.OP_LOGICO, OpLogico.SOBRERO, comentario);
					return token;
				} else if (preanalisis == ';') {
					token = new Token(TipoToken.SEPARADOR, Separadores.PUNTO_COMA, comentario);
					return token;
				} else if (preanalisis == '?') {
					token = new Token(TipoToken.SEPARADOR, Separadores.INTEROGACION, comentario);
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
					token = new Token(TipoToken.SEPARADOR, Separadores.ABRE_CORCHETE, comentario);
					return token;
				} else if (preanalisis == '{') {
					token = new Token(TipoToken.SEPARADOR, Separadores.ABRE_LLAVE, comentario);
					return token;
				} else if (preanalisis == '<') {
					transita(75);
				} else if (preanalisis == ':') {
					transita(81);
				} else if (preanalisis == ']') {
					token = new Token(TipoToken.SEPARADOR, Separadores.CIERRA_CORCHETE, comentario);
					return token;
				} else if (preanalisis == '|') {
					transita(83);					
				} else if (preanalisis == '&') {
					transita(87);
				} else if (preanalisis == '^') {
					transita(91);
				} else if (preanalisis == '(') {
					token = new Token(TipoToken.SEPARADOR, Separadores.ABRE_PARENTESIS, comentario);
					return token;
				} else if (preanalisis == ')') {
					token = new Token(TipoToken.SEPARADOR, Separadores.CIERRA_PARENTESIS, comentario);
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
					return new Token(TipoToken.SEPARADOR, Separadores.COMA, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);					
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
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR, null, comentario);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera, comentario);
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
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera, comentario);
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
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera, comentario);
				}
				break;
			case 4:	
				if(hex()){
					int hex = valHex();
					parteEntera = parteEntera*16 + hex;
					transita(7);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
				break;
			case 5:	
				if(preanalisis == 'L') {
					transita(13);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(11);
				} else if(digito() || letra()) {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera, comentario);
				}
				break;
			case 6: 
				if(preanalisis == 'l') {
					transita(12);
				} else if(preanalisis == 'L') {
					transita(10);					
				} else if(digito() || letra()) {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera, comentario);
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
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera, comentario);
				}
				break;
			case 8:	
				if(preanalisis == 'l') {
					transita(13);
				} else if(preanalisis == 'u' || preanalisis == 'U') {
					transita(11);	
				} else if(digito() || letra()) {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera, comentario);
				}
				break;	
			case 10:
				if(preanalisis == 'L') {
					transita(11);	
				} else if(digito() || letra()) {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera, comentario);
				}
				break;
			case 11:
				if(digito() || letra()) {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera, comentario);
				}
			case 12:	
				if(preanalisis == 'l') {
					transita(11);	
				} else if(digito() || letra()) {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera, comentario);
				}
				break;
			case 13:	
				if(preanalisis == 'u' || preanalisis == 'U') {
						transita(11);	
				} else if(digito() || letra()) {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				} else {
					asterisco = true;
					return new Token(TipoToken.NUM_ENTERO, parteEntera, comentario);
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
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
				break;
			case 15:	
				if(digito >= 0 && digito <= 9) {
					parteDecimal = parteDecimal + digito*pesoDecimal;
					pesoDecimal = pesoDecimal/10;
					transita(16);
				} else if(esDelim2()){
					token = new Token(TipoToken.SEPARADOR, Separadores.PUNTO, comentario);
					asterisco = true;
					return token;
				} else{
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);				
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
					return new Token(TipoToken.NUM_REAL, (parteEntera + parteDecimal)*Math.pow(10, signo*parteExponencial), comentario);
				} else{
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
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
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}	
				break;
			case 18:
				if(digito >= 0 && digito <= 9) {
					parteExponencial = parteExponencial*10 + digito;
					transita(19);
				} else{
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
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
					return new Token(TipoToken.NUM_REAL_EXPO, parteEntera+parteDecimal+"E"+s+parteExponencial, comentario);  
				} else{
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}		
				break;
			case 21:
				if(esDelim()){ 
					asterisco=true;
					return new Token(TipoToken.NUM_REAL, (parteEntera + parteDecimal)*Math.pow(10, signo*parteExponencial), comentario); 
				} else{
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
			case 23:
				if(esDelim()){ 
					asterisco=true;
					String s = (signo > 0) ? "+" : "-";
					return new Token(TipoToken.NUM_REAL_EXPO, parteEntera+parteDecimal+"E"+s+parteExponencial, comentario); 
				} else{
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
			//OPERADORES
			case 41:
				if (preanalisis == '=') { 			// +=
					token = new Token(TipoToken.OP_ASIGNACION,OpAsignacion.MAS_IGUAL, comentario);
					return token;
				} else if (preanalisis == '+') { 	// ++					
					token = new Token(TipoToken.OP_ARITMETICO,OpAritmetico.INCREMENTO, comentario);
					return token;
				} else if (esDelim2()) { 			// + //valores al preanalisis DELIM2 - {+,=}
					asterisco = true;	
					token = new Token(TipoToken.OP_ARITMETICO,OpAritmetico.SUMA, comentario);
					return token;
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
		
			case 45:
				if (preanalisis == '=') { 			// -=
					token = new Token(TipoToken.OP_ASIGNACION,OpAsignacion.MENOS_IGUAL, comentario);
					return token;
				} else if (preanalisis == '-') { 	// --					
					token = new Token(TipoToken.OP_ARITMETICO,OpAritmetico.DECREMENTO, comentario);
					return token;
				} else if (preanalisis == '>') { 	// ->					
					token = new Token(TipoToken.OP_ASIGNACION,OpAsignacion.PUNTERO, comentario);
					return token;
				} else if (esDelim2()) { 			// - //valores al preanalisis DELIM2 - {-,=}
					asterisco = true;
					return new Token(TipoToken.OP_ARITMETICO,OpAritmetico.RESTA, comentario);		
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
		
			case 49:
				if (preanalisis == '=') { 			// *=
					token = new Token(TipoToken.OP_ASIGNACION,OpAsignacion.POR_IGUAL, comentario);
					return token;
				} else if (esDelim2()) { 			// * //valores al preanalisis DELIM2 - {=}
					token = new Token(TipoToken.OP_ARITMETICO,OpAritmetico.MULTIPLICACION, comentario);
					asterisco = true;
					return token;
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
			case 52:
				if (preanalisis == '>') { 			// %> equivalente a }
					token = new Token(TipoToken.SEPARADOR,Separadores.CIERRA_LLAVE, comentario);
					return token;
				} else if (preanalisis == '=') { 	// %=
					token = new Token(TipoToken.OP_ASIGNACION,OpAsignacion.PORCENTAJE_IGUAL, comentario);
					return token;
				} else if (preanalisis == ':') {	// %:
					transita(56);
				} else if (esDelim2()) { 			// % //valores al preanalisis DELIM2 - {>,=,:}
					asterisco = true;
					return new Token(TipoToken.OP_ARITMETICO, OpAritmetico.PORCENTAJE, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
				break;
			case 56:
				if (preanalisis == '%') {			// %:%
					transita(57);
				} else if (esDelim2()) { 			// %: equivalente a #
					asterisco = true;
					return new Token(TipoToken.SEPARADOR, Separadores.ALMOHADILLA, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
				break;
			case 57:
				if (preanalisis == ':') {			// %:%: equivalente a ##
					return new Token(TipoToken.SEPARADOR, Separadores.DOBLE_ALMOHADILLA, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
			case 60:	
				if (preanalisis == '#') {			// ##
					return new Token(TipoToken.SEPARADOR, Separadores.DOBLE_ALMOHADILLA, comentario);
				} else if (esDelim2()) { 			// # //valores al preanalisis DELIM2 - {#}
					asterisco = true;
					return new Token(TipoToken.SEPARADOR, Separadores.ALMOHADILLA, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}

			case 62:
				if (preanalisis == '=') {			// !=
					return token = new Token(TipoToken.OP_COMPARACION, OpComparacion.DISTINTO, comentario);
				} else if (esDelim2()) { 			// ! //valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return token = new Token(TipoToken.OP_LOGICO, OpLogico.NOT, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}

			case 64:
				if (preanalisis == '=') {			// ==
					token = new Token(TipoToken.OP_COMPARACION, OpComparacion.IGUALDAD, comentario);
					return token;
				} else if (esDelim2()) {			// = //valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return new Token(TipoToken.OP_ASIGNACION, OpAsignacion.ASIGNACION, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
				
			case 68:
				if (preanalisis == '=') {			// >=
					token = new Token(TipoToken.OP_COMPARACION,OpComparacion.MAYOR_IGUAL, comentario);
					return token;
				} else if (preanalisis == '>') {	// >>
					transita(69);
				} else if (esDelim2()) {			// > //valores al preanalisis DELIM2 - {>,=}
					asterisco = true;
					return new Token(TipoToken.OP_COMPARACION,OpComparacion.MAYOR, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
				break;
			case 69:
				if (preanalisis == '=') {			// >>=
					return new Token(TipoToken.OP_ASIGNACION,OpAsignacion.MAYOR_MAYOR_IGUAL, comentario);
				} else if (esDelim2()) {			// >> //valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO, OpLogico.DOS_MAYORES, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
			case 75:
				if (preanalisis == '=') {			// <=
					token = new Token(TipoToken.OP_COMPARACION,OpComparacion.MENOR_IGUAL, comentario);
					return token;
				} else if (preanalisis == '<') {	// <<
					transita(78);
				} else if (preanalisis == ':') {	// <: equivalente a [
					return new Token(TipoToken.SEPARADOR,Separadores.ABRE_CORCHETE, comentario);
				} else if (preanalisis == '%') {	// <% equivalente a {
					return new Token(TipoToken.SEPARADOR,Separadores.ABRE_LLAVE, comentario);
				} else if (esDelim2()) {			// < //valores al preanalisis DELIM2 - {<,%,=,:}
					asterisco = true;
					return new Token(TipoToken.OP_COMPARACION,OpComparacion.MENOR, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
				break;
			case 78:
				if (preanalisis == '=') {			// <<=
					return new Token(TipoToken.OP_ASIGNACION,OpAsignacion.MENOR_MENOR_IGUAL, comentario);
				} else if (esDelim2()) {			// << //valores al preanalisis DELIM2 - {=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,OpLogico.DOS_MENORES, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
			case 81:
				if (preanalisis == '>') {			// :> equivalente a ]
					return new Token(TipoToken.SEPARADOR,Separadores.CIERRA_CORCHETE, comentario);
				} else if (preanalisis == ':') {	// ::
					return new Token(TipoToken.SEPARADOR,Separadores.DOBLE_DOSPUNTOS, comentario);
				} else if (esDelim2()) {			// :
					asterisco = true;
					return new Token(TipoToken.SEPARADOR,Separadores.DOS_PUNTOS, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
			case 83:
				if (preanalisis == '|') {			// ||
					return new Token(TipoToken.OP_LOGICO,OpLogico.OR, comentario);
				} else if (preanalisis == '=') {	// |=
					return new Token(TipoToken.OP_ASIGNACION,OpAsignacion.OR_IGUAL, comentario);
				} else if (esDelim2()) {			// | //valores al preanalisis DELIM - {|,=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,OpLogico.BIT_OR, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
			case 87:
				if (preanalisis == '&') {			// &&
					return new Token(TipoToken.OP_LOGICO,OpLogico.AND, comentario);
				} else if (preanalisis == '=') {	// &=
					return new Token(TipoToken.OP_ASIGNACION,OpAsignacion.AND_IGUAL, comentario);
				} else if (esDelim2()) {			// & //valores al preanalisis DELIM - {&,=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,OpLogico.BIT_AND, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
			case 91:
				if (preanalisis == '=') {			// ^=
					return new Token(TipoToken.OP_ASIGNACION,OpAsignacion.CIRCUNFLEJO_IGUAL, comentario);
				} else if (esDelim2()) {			// ^ //valores al preanalisis DELIM - {=}
					asterisco = true;
					return new Token(TipoToken.OP_LOGICO,OpLogico.CIRCUNFLEJO, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
			// RECONOCIMIENTO DE CADENAS, IDENTIFICADORES Y PALABRAS RESERVADAS
			case 97:	
				if (noDigito() || digito()) {
					lexema = lexema+preanalisis;
					transita(97);
				} else if(preanalisis == ':') { // es una etiqueta
					token = new Token(TipoToken.ETIQUETA,lexema,comentario);
					return token;
				} else if(esDelim()) { 
					 Integer indice = gestorTS.buscaPalRes(lexema);
					 if(indice == null ) //si es un identificador	
					 {	 
						 if(esModoActivo(modo.GoTo)) { //si está en modo GoTo se devuelve el token con el lexema de la etiqueta
							 token = new Token(TipoToken.IDENTIFICADOR,lexema,comentario);
						 }
						 else {
							 //Dependiendo de si se esta en modo declaracion se insertara el atributo en la TS o se consultara para comprobar su existencia. 
							 if(esModoActivo(modo.Declaracion) && !esModoActivo(modo.NoMeto)) {
								 EntradaTS puntero = gestorTS.buscaIdGeneral(lexema);
								 if (puntero == null) { //si no esta en la T.S. se inserta.
									 token = new Token(TipoToken.IDENTIFICADOR, gestorTS.insertaIdentificador(lexema), comentario);
								 }
								 else {
									 //si ya esta habria que mirar si se encuentra en el mismo ambito o no. 
									 //Si ya hay un id con ese mismo nombre en el mismo ambito deberia lanzar error y devolver el token para seguir con el analisis semantico
									 if(gestorTS.buscaIdBloqueActual(lexema) == null) {
										 token = new Token(TipoToken.IDENTIFICADOR, gestorTS.insertaIdentificador(lexema), comentario);
									 } else {
										 token = new Token(TipoToken.IDENTIFICADOR, gestorTS.buscaIdGeneral(lexema), comentario);
										 gestorErrores.insertaErrorSemantico(numlinea, numcolumna,"Multiple declaracion de "+lexema);
									 }
								}
							 }
							 else if(esModoActivo(modo.NoMeto)) {
								 token = new Token(TipoToken.IDENTIFICADOR,lexema,comentario);
							 }
							 else { // si no esta en modo declaracion ni en modo noMeto
								 EntradaTS puntero = gestorTS.buscaIdGeneral(lexema);
								 if(puntero == null) {
									 //error semantico (variable no declarada)
									 gestorErrores.insertaErrorSemantico(numlinea, numcolumna,"Identificador "+lexema+" no declarado");
									 throw new Exception("Uso de identificador no declarado");
								 }
								 else
									 token = new Token(TipoToken.IDENTIFICADOR, puntero, comentario);
							 }
						 }
					 }	
					 else { // es una palabra reservada
						 token = new Token(TipoToken.PAL_RESERVADA,indice, comentario);
					 }
					 asterisco=true;
					 return token;
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
				break;
			case 99: 
				if(preanalisis == '"') {
					return new Token(TipoToken.LIT_CADENA,lexema, comentario);
				} else	if( !esCajonDesastre() || (preanalisis == '\\') || (preanalisis == '\n'))	{
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
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
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				} else {
					lexema = lexema+preanalisis;
					transita(103);
				}
				break;
			case 103:
				if(preanalisis == '\''){ // fin de literal caaracter
					return new Token(TipoToken.LIT_CARACTER,lexema, comentario);
				} else if (preanalisis == '\\') { // barra de escape
					lexema = lexema+preanalisis;
					transita(116);
				} else if ( !esCajonDesastre() || (preanalisis == '\n')){
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				} else {
					lexema = lexema+preanalisis;
					transita(103);
				}
				break;
			case 105:
				if(preanalisis=='='){			// /=
					token = new Token(TipoToken.OP_ASIGNACION,OpAsignacion.DIV_IGUAL, comentario);
					return token;
				} else if(preanalisis=='*'){ 	// /* // empieza un comentario de varias lineas
					transita(107);
				} else if(preanalisis=='/'){	// // // empieza un comentario de una linea
					transita(110);
				} else if(esDelim() || digito() || letra()){			// /
					asterisco = true;
					return new Token(TipoToken.OP_ARITMETICO,OpAritmetico.DIVISION, comentario);
				} else {
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
				break;
			case 107:	// lee contenido de un comentario de varias lineas
				if(preanalisis=='*'){
					transita(108);
				} else if (preanalisis == '\0')  { //error ya que termina el fichero y el comentario nunca se cierra
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				} else {
					if(preanalisis=='\n') 
						numlinea++;
					lexema = lexema + preanalisis;
					transita(107);
				}	
				
				break;
			case 108:
				if(preanalisis=='/'){
					comentario += lexema;
					hayComentario = true;
					scan();
				}
				else if (preanalisis == '\0')  { //error ya que termina el fichero y el comentario nunca se cierra
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
				}
				else	
					transita(107);
				break;
			case 110:
				if(preanalisis=='\n' || preanalisis=='\0'){  //Ya que es el comentario de linea (//) acaba cuando recibe un \r\n
					comentario += lexema;
					hayComentario = true;
					scan();
				} else {
					lexema = lexema + preanalisis;
					transita(110);
				}
				break;
			case 116:
				if(esSecuenciaEscapeSimple()) {
					lexema = lexema + preanalisis;
					transita(103);
				} else { // secuencia de escape simple no valida
					//insertar en G.E.
					gestorErrores.insertaErrorLexico(2,numlinea, numcolumna);
					return new Token(TipoToken.ERROR,null, comentario);
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

	public int getLinea() {
		return numlinea;
	}

	public int getColumna() {
		return numcolumna;
	}
	
	public void activaModo(modo m) {
		if(!modos.contains(m)) //para evitar que se añadan repetidos
			modos.add(m);
	}
	
	public void desactivaModo(modo m) {
		modos.remove(m);
	}
	
	public boolean esModoActivo(modo m) {
		return modos.contains(m);
	}
	
	public Vector<modo> getModos() {
		return modos;
	}
	
	public void setModos(Vector<modo> m) {
		modos = m;
	}
    
}
