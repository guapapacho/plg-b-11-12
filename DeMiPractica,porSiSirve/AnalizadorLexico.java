package compilador.lexico;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


import compilador.lexico.Tokens.Absoluto;
import compilador.lexico.Tokens.And;
import compilador.lexico.Tokens.Boolean;
import compilador.lexico.Tokens.Cast_char;
import compilador.lexico.Tokens.Cast_float;
import compilador.lexico.Tokens.Cast_int;
import compilador.lexico.Tokens.Cast_nat;
import compilador.lexico.Tokens.Distinto;
import compilador.lexico.Tokens.Division;
import compilador.lexico.Tokens.Dos_puntos;
import compilador.lexico.Tokens.Dos_puntos_ig;
import compilador.lexico.Tokens.FloatToken;
import compilador.lexico.Tokens.Identificador;
import compilador.lexico.Tokens.Igual;
import compilador.lexico.Tokens.In;
import compilador.lexico.Tokens.Integer;
import compilador.lexico.Tokens.LitCha;
import compilador.lexico.Tokens.LitFalse;
import compilador.lexico.Tokens.LitFlo;
import compilador.lexico.Tokens.LitNat;
import compilador.lexico.Tokens.LitTrue;
import compilador.lexico.Tokens.Mayor;
import compilador.lexico.Tokens.Mayor_ig;
import compilador.lexico.Tokens.Menor;
import compilador.lexico.Tokens.Menor_ig;
import compilador.lexico.Tokens.Modulo;
import compilador.lexico.Tokens.Multiplicacion;
import compilador.lexico.Tokens.Natural;
import compilador.lexico.Tokens.Not;
import compilador.lexico.Tokens.Or;
import compilador.lexico.Tokens.Out;
import compilador.lexico.Tokens.Parentesis_a;
import compilador.lexico.Tokens.Parentesis_c;
import compilador.lexico.Tokens.Punto_coma;
import compilador.lexico.Tokens.Separador;
import compilador.lexico.Tokens.Shl;
import compilador.lexico.Tokens.Signo_menos;
import compilador.lexico.Tokens.Suma;
import compilador.lexico.Tokens.Token;
import compilador.lexico.Tokens.character;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;

/**
 *
 * @author GRUPO 3: Gonzalo Ortiz Jaureguizar, Alicia Perez Jimenez, Laura Reyero Sainz, Hector Sanjuan Redondo, Ruben Tarancon Garijo
 */
public class AnalizadorLexico {
	private static final int INICIAL = 0;
	private static final int PARENTESIS_C = 1;
	private static final int COMENTARIO = 2;
	private static final int PARENTESIS_A = 3;
	private static final int CASTING_INT1 =4;
	private static final int CASTING_INT2 = 5;
	private static final int CASTING_INT3 =6;
	private static final int CAST_INT = 7;
	private static final int CASTING_NAT1 = 8;
	private static final int CASTING_NAT2 = 9;
	private static final int CASTING_NAT3 = 10;
	private static final int CAST_NAT = 11;
	private static final int CASTING_FLOAT1 = 12;
	private static final int CASTING_FLOAT2 = 13;
	private static final int CASTING_FLOAT3 = 14;
	private static final int CASTING_FLOAT4 = 15;
	private static final int CASTING_FLOAT5 = 16;
	private static final int CAST_FLOAT = 17;
	private static final int CASTING_CHAR1 = 18;
	private static final int CASTING_CHAR2 = 19;
	private static final int CASTING_CHAR3 = 20;
	private static final int CASTING_CHAR4 = 21;
	private static final int CAST_CHAR = 22;
	private static final int MAYOR = 23;
	private static final int SHL = 24;
	private static final int MAYOR_IG = 25;
	private static final int MENOR = 26;
	private static final int SHR = 27;
	private static final int MENOR_IG = 28;
	private static final int DOS_PUNTOS = 29;
	private static final int DOS_PUNTOS_IG = 30;
	private static final int IGUAL = 31;
	private static final int DISTINTO1 = 32;
	private static final int DISTINTO = 33;
	private static final int LIT_NAT1 = 34;
	private static final int LIT_NAT2 = 35;
	private static final int FLOAT1 = 36;
	private static final int LIT_FLO1 = 37;
	private static final int LIT_FLO2 = 38;
	private static final int FLOAT2 = 39;
	private static final int FLOAT3 = 40;
	private static final int LIT_FLO3 = 41;
	private static final int FLOAT4 = 42;
	private static final int LIT_FLO4 = 43;
	private static final int SEPARADOR =44;
	private static final int PUNTO_COMA = 45;
	private static final int SUMA = 46;
	private static final int MULTIPLICACION = 47;
	private static final int DIVISION = 48;
	private static final int MODULO = 49;
	private static final int ABSOLUTO = 50;
	private static final int CADENA = 51;
	private static final int CHAR1 = 52;
	private static final int CHAR2 = 53;
	private static final int LIT_CHA= 54;
	private static final int SIGNO_MENOS = 55;

	private static final HashMap<String,Token> palabrasReservadas = new HashMap<String,Token>();
	static{
		palabrasReservadas.put("true",new LitTrue());
		palabrasReservadas.put("false",new LitFalse());
		palabrasReservadas.put("integer",new Integer());
		palabrasReservadas.put("natural",new Natural());
		palabrasReservadas.put("boolean",new Boolean());
		palabrasReservadas.put("float",new FloatToken());
		palabrasReservadas.put("character",new character());
		palabrasReservadas.put("and",new And());
		palabrasReservadas.put("or",new Or());
		palabrasReservadas.put("not",new Not());
		palabrasReservadas.put("in",new In());
		palabrasReservadas.put("out",new Out());
	}


	private char buff;
	private String lex;
	private int estado;
	private Reader reader = null;
	private ArrayList<Token> arrayTokens;
    private static int numLinea;
    private PrintWriter writer;
    private boolean errorLexico;

	public AnalizadorLexico(String src) throws FileNotFoundException, IOException{
		reader = new StringReader(src);
		lex="";
		estado = 0;
		buff = sigCar();//inicioScan() de los apuntes
		arrayTokens = new ArrayList<Token>();
        writer = new PrintWriter(System.out,true);
        numLinea=1;
        errorLexico=false;
		scanner();
	}

    public AnalizadorLexico(Reader reader) throws FileNotFoundException, IOException{
		this.reader = reader;
		lex="";
		estado = 0;
		buff = sigCar();//inicioScan() de los apuntes
		arrayTokens = new ArrayList<Token>();
        numLinea=1;
        writer = new PrintWriter(System.out,true);
        errorLexico=false;
		scanner();
	}

	public char sigCar() throws IOException{
		char res;
		if(reader==null) throw new IOException("No creaste ningun Reader para la clase!");
		res = (char) reader.read();
		return res;
	}

	public void transita(int est) throws IOException{
		if (buff!='\uffff')
			lex = lex + buff;
		buff = sigCar();
		estado = est;
	}

        public void terminaEstado(){
		lex="";
		estado=INICIAL;
	}

        public ArrayList<Token> getArrayTokens(){
		return arrayTokens;
	}

        public PrintWriter getWriter(){
            return writer;
        }

        public boolean getErrorLexico(){
            return errorLexico;
        }
        public void imprimirTokens(){

        }
	public void error() throws IOException{
		String sal="";
                String linea=" (linea "+numLinea+")";
		switch(estado){
            case INICIAL: sal="No existe el token"+ buff+linea; break;
            case CASTING_INT1:
            case CASTING_INT2:
            case CASTING_INT3: sal="Has escrito mal (int)"+linea; break;
            case CASTING_NAT1:
            case CASTING_NAT2:
            case CASTING_NAT3: sal="Has escrito mal (nat)"+linea; break;
            case CASTING_FLOAT1:
            case CASTING_FLOAT2:
            case CASTING_FLOAT3:
            case CASTING_FLOAT4:
            case CASTING_FLOAT5: sal="Has escrito mal (float)"+linea; break;
            case CASTING_CHAR1:
            case CASTING_CHAR2:
            case CASTING_CHAR3:
            case CASTING_CHAR4: sal="Has escrito mal (char)"+linea; break;
            case DISTINTO1: sal= "No exite el token" + lex+linea; break;
            case LIT_NAT1:
            case LIT_NAT2: sal= "Un numero natural no puede acabar por letra"+linea; break;
            case LIT_FLO1:
            case LIT_FLO2:
            case LIT_FLO3:
            case LIT_FLO4: sal= "Un numero real no puede acabar por letra"+linea; break;
        	case FLOAT1:
    		case FLOAT2: sal= "La parte decimal de un float no puede acabar en 0"+linea; break;
    		case FLOAT3: sal= "La parte decimal de un float solo tiene numeros"+linea; break;
    		case FLOAT4:
    			switch(buff){
    			case '0': sal= "El exponente de un float no puede empezar por cero"+linea; break;
    			default: sal= "El exponente de un float solo puede llevar numeros"+linea; break;
    			}
    			break;
    		case CHAR1: sal="Los caracteres tienen que ser de 1 letra"+linea; break;
    		case CHAR2: sal="Despues de escribir el caracter tienes que poner \'"+linea; break;
            case CADENA: sal="No puedes definir un token con nombre de palabra reservada"+linea; break;
    		default:
    		}
            transita(INICIAL);
            writer.println(sal);
            writer.println("La ejecucion se ha parado por error en el analisis lexico");
            errorLexico=true;

        }

        public void scanner() throws IOException{
		while(buff != '\uffff' || !lex.equals("")){
			switch(estado){
			case INICIAL :
				switch (buff){
				case '\n': numLinea ++;
				case '\t':
				case ' ': transita(INICIAL); lex =""; break;
				case ')': transita(PARENTESIS_C); break;
				case '#': transita(COMENTARIO); break;
				case '(': transita(PARENTESIS_A); break;
				case '>': transita(MAYOR); break;
				case '<': transita(MENOR); break;
				case ':': transita(DOS_PUNTOS); break;
				case '=': transita(IGUAL); break;
				case '&': transita(SEPARADOR); break;
				case ';': transita(PUNTO_COMA); break;
				case '+': transita(SUMA); break;
				case '*': transita(MULTIPLICACION); break;
				case '/': transita(DIVISION); break;
				case '%': transita(MODULO); break;
				case '|': transita(ABSOLUTO); break;
				case '\'' : transita(CHAR1); break;
				case '-': transita(SIGNO_MENOS); break;
				default:
					if (Character.isDigit(buff)){
						switch(buff){
						case '0': transita(LIT_NAT1); break;
						default : transita(LIT_NAT2);
						}
					}
					else{
						if(Character.isLetter(buff))
							transita(CADENA);
						else error();
					}
				}
				break;
			case PARENTESIS_C :
				arrayTokens.add(new Parentesis_c(numLinea));
				terminaEstado();
				break;
			case COMENTARIO :
				switch(buff){
				case '\n': terminaEstado(); break;
				default: transita(COMENTARIO);
				}

				break;
			case PARENTESIS_A :
				switch (buff){
				case 'i': transita(CASTING_INT1); break;
				case 'n': transita(CASTING_NAT1); break;
				case 'f': transita(CASTING_FLOAT1); break;
				case 'c': transita(CASTING_CHAR1); break;
                case ' ': transita(PARENTESIS_A); break;
				default: arrayTokens.add(new Parentesis_a(numLinea)); terminaEstado();
				}
				break;
			case CASTING_INT1 :
				switch (buff){
				case 'n': transita(CASTING_INT2); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CASTING_INT2 :
				switch (buff){
				case 't': transita(CASTING_INT3); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CASTING_INT3 :
				switch (buff){
				case ')': transita(CAST_INT); break;
                case ' ': transita(CASTING_INT3); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CAST_INT :
				arrayTokens.add(new Cast_int(numLinea));
				terminaEstado();
				break;
			case CASTING_NAT1 :
				switch (buff){
				case 'a': transita(CASTING_NAT2); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CASTING_NAT2 :
				switch (buff){
				case 't': transita(CASTING_NAT3); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CASTING_NAT3 :
				switch (buff){
				case ')': transita(CAST_NAT); break;
                case ' ': transita(CASTING_NAT3); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CAST_NAT :
				arrayTokens.add(new Cast_nat(numLinea));
				terminaEstado();
				break;
			case CASTING_FLOAT1 :
				switch (buff){
				case 'l': transita(CASTING_FLOAT2); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CASTING_FLOAT2 :
				switch (buff){
				case 'o': transita(CASTING_FLOAT3); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CASTING_FLOAT3 :
				switch (buff){
				case 'a': transita(CASTING_FLOAT4); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff)|| buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CASTING_FLOAT4 :
				switch (buff){
				case 't': transita(CASTING_FLOAT5); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CASTING_FLOAT5 :
				switch (buff){
				case ')': transita(CAST_FLOAT); break;
                case ' ': transita(CASTING_FLOAT5); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CAST_FLOAT :
				arrayTokens.add(new Cast_float(numLinea));
				terminaEstado();
				break;
			case CASTING_CHAR1 :
				switch (buff){
				case 'h': transita(CASTING_CHAR2); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')' ){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CASTING_CHAR2 :
				switch (buff){
				case 'a': transita(CASTING_CHAR3); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CASTING_CHAR3 :
				switch (buff){
				case 'r': transita(CASTING_CHAR4); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CASTING_CHAR4 :
				switch (buff){
				case ')': transita(CAST_CHAR); break;
                case ' ': transita(CASTING_CHAR4); break;
				default:
                    if(Character.isLetter(buff) || Character.isDigit(buff) || buff == ')'){
                        arrayTokens.add(new Parentesis_a(numLinea));
                        lex=lex.substring(1);
                        estado=CADENA;
                        break;
                    }
                    else
                        error();
				}
				break;
			case CAST_CHAR :
				arrayTokens.add(new Cast_char(numLinea));
				terminaEstado();
				break;
			case MAYOR :
				switch(buff){
				case '>': transita(SHL); break;
				case '=': transita(MAYOR_IG); break;
				default: arrayTokens.add(new Mayor(numLinea)); terminaEstado();
				}
				break;
			case MAYOR_IG :
				arrayTokens.add(new Mayor_ig(numLinea));
				terminaEstado();
				break;
			case SHL :
				arrayTokens.add(new Shl(numLinea));
				terminaEstado();
				break;
			case MENOR :
				switch(buff){
				case '<': transita(SHR); break;
				case '=': transita(MENOR_IG); break;
				default: arrayTokens.add(new Menor(numLinea)); terminaEstado();
				}
				break;
			case MENOR_IG :
				arrayTokens.add(new Menor_ig(numLinea));
				terminaEstado();
				break;
			case SHR:
				arrayTokens.add(new Shl(numLinea));
				terminaEstado();
				break;
			case DOS_PUNTOS :
				switch(buff){
				case '=': transita(DOS_PUNTOS_IG); break;
				default: arrayTokens.add(new Dos_puntos(numLinea)); terminaEstado();
				}
				break;
			case DOS_PUNTOS_IG :
				arrayTokens.add(new Dos_puntos_ig(numLinea));
				terminaEstado();
				break;
			case IGUAL:
				switch(buff){
				case '/': transita(DISTINTO1); break;
				default: arrayTokens.add(new Igual(numLinea)); terminaEstado();
				}
				break;
			case DISTINTO1 :
				switch(buff){
				case '=': transita(DISTINTO); break;
				default: error();
				}
				break;
			case DISTINTO:
				arrayTokens.add(new Distinto(numLinea));
				terminaEstado();
				break;
			case LIT_NAT1 :
				switch(buff){
				case '.': transita(FLOAT1); break;
				default:
                    if (Character.isLetter(buff)){
                        error();
                        break;
                    }
                    else {
                        arrayTokens.add(new LitNat(lex,numLinea));
                        terminaEstado();
                    }
				}
				break;
			case LIT_NAT2 :
				switch(buff){
				case '.': transita(FLOAT1); break;
				default:
					if (Character.isDigit(buff)){
						transita(LIT_NAT2);
                        break;
                    }
					else
						if (Character.isLetter(buff)){
                            error();
                            break;
                        }
                        else {
                            arrayTokens.add(new LitNat(lex,numLinea));
                            terminaEstado();
                        }
                }
				break;
			case FLOAT1 :
				if(Character.isDigit(buff))
					switch(buff){
					case '0': transita(LIT_FLO1); break;
					default: transita(LIT_FLO2);
					}
				else
					error();
				break;
			case LIT_FLO1 :
				switch(buff){
				case 'e':
				case 'E': transita(FLOAT3); break;
				default:
					if(Character.isDigit(buff))
						switch(buff){
						case '0': transita(FLOAT2); break;
						default: transita(LIT_FLO2);
						}
					else{
                        if (Character.isLetter(buff)){
                            error();
                            break;
                        }
                        else {
                            arrayTokens.add(new LitFlo(lex,numLinea));
                            terminaEstado();
                        }
					}
				}
				break;
			case LIT_FLO2 :
				switch(buff){
				case 'e':
				case 'E': transita(FLOAT3); break;
				default:
					if(Character.isDigit(buff))
						switch(buff){
						case '0': transita(FLOAT2); break;
						default: transita(LIT_FLO2);
						}
					else{
                        if (Character.isLetter(buff)){
                            error();
                            break;
                        }
                        else {
                            arrayTokens.add(new LitFlo(lex,numLinea));
                            terminaEstado();
                        }
					}
				}
				break;
			case FLOAT2 :
				if(Character.isDigit(buff))
					switch(buff){
					case '0': transita(FLOAT2); break;
					default: transita(LIT_FLO2);
					}
				else
					error();
				break;
			case FLOAT3 :
				switch(buff){
				case '-': transita(FLOAT4); break;
				default:
					if(Character.isDigit(buff))
						switch(buff){
						case '0': transita(LIT_FLO3); break;
						default: transita(LIT_FLO4);
						}
					else
						error();
				}
				break;
			case FLOAT4 :
				if(Character.isDigit(buff))
					switch(buff){
					case '0': error(); break;
					default: transita(LIT_FLO4);
					}
				else
					error();

			case LIT_FLO3:
                if (Character.isLetter(buff)){
                    error();
                    break;
                }
                else {
                    arrayTokens.add(new LitFlo(lex,numLinea));
                    terminaEstado();
                }
				break;
			case LIT_FLO4 :
				if (Character.isLetter(buff)){
                    error();
                    break;
                }
                else {
                    arrayTokens.add(new LitFlo(lex,numLinea));
                    terminaEstado();
                }
				break;
			case SEPARADOR:
				arrayTokens.add(new Separador(numLinea));
				terminaEstado();
				break;
			case PUNTO_COMA:
				arrayTokens.add(new Punto_coma(numLinea));
				terminaEstado();
				break;
			case SUMA:
				arrayTokens.add(new Suma(numLinea));
				terminaEstado();
				break;
			case MULTIPLICACION:
				arrayTokens.add(new Multiplicacion(numLinea));
				terminaEstado();
				break;
			case DIVISION :
				arrayTokens.add(new Division(numLinea));
				terminaEstado();
				break;
			case MODULO:
				arrayTokens.add(new Modulo(numLinea));
				terminaEstado();
				break;
			case ABSOLUTO:
				arrayTokens.add(new Absoluto(numLinea));
				terminaEstado();
				break;
			case CADENA:
				if (Character.isLetter(buff) || Character.isDigit(buff))
					transita(CADENA);
				else if (palabrasReservadas.containsKey(lex)){
                    Token t=(Token) palabrasReservadas.get(lex);
                    t.cambiaLinea(numLinea);
					arrayTokens.add(t);
					terminaEstado();
				}
				else if(lex.equals("nat") || lex.equals("int") || lex.equals("bool") || lex.equals("char") || lex.equals("float"))
                    error();
                else{
					arrayTokens.add(new Identificador(lex,numLinea));
					terminaEstado();
				}
				break;

			case CHAR1:
				if(Character.isDigit(buff) || Character.isLetter(buff))
					transita(CHAR2);
				else error();
				break;
			case CHAR2:
				switch(buff){
				case '\'': transita(LIT_CHA); break;
				default: error();
				}
				break;
			case LIT_CHA:
				arrayTokens.add(new LitCha(lex,numLinea));
				terminaEstado();
				break;
			case SIGNO_MENOS:
				arrayTokens.add(new Signo_menos(numLinea));
				terminaEstado();
				break;


			}
		}

	}
}
