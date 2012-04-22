package compilador.analizadorLexico;

import compilador.gestionTablasSimbolos.EntradaTS;
import compilador.gestionTablasSimbolos.GestorTablasSimbolos;

/**
 * 
 * @author Grupo 1 
 * 
 */
public class Token {
    

    /**
     * Tipos de tokens
     */
	public enum TipoToken {
        EOF, LIT_CARACTER, LIT_CADENA, PAL_RESERVADA, 
        IDENTIFICADOR, NUM_REAL, NUM_REAL_EXPO, NUM_ENTERO, SEPARADOR, 
        OP_ARITMETICO, OP_LOGICO, OP_COMPARACION, OP_ASIGNACION,
        COMENT_LINEA, COMENT_LARGO, ERROR, ETIQUETA, CARACTER, TIPODEFINIDO
    }
    
    /**
     * Separadores
     */
    public enum Separadores {
    	PUNTO("."),DOS_PUNTOS(":"),PUNTO_COMA(";"), ABRE_LLAVE("{"), CIERRA_LLAVE("}"), ABRE_CORCHETE("["),
    	CIERRA_CORCHETE("]"), ALMOHADILLA("#"), DOBLE_ALMOHADILLA("##"), ABRE_PARENTESIS("("),
    	CIERRA_PARENTESIS(")"), DOBLE_DOSPUNTOS("::"), COMA(","), INTEROGACION("?");
    	
    	private String description;
    	private Separadores(String desc) { description = desc; };
    	public String getDesc() { return description; };
    }
    
    /**
     * Operadores aritmeticos
     */
    public enum OpAritmetico {
    	SUMA("+"), RESTA("-"), INCREMENTO("++"), DECREMENTO("--"), MULTIPLICACION("*"), DIVISION("/"), PORCENTAJE("%");
     	
    	private String description;
    	private OpAritmetico(String desc) { description = desc; };
    	public String getDesc() { return description; };
    }
    
    /**
     * Operadores de comparacion
     */
    public enum OpComparacion {
    	IGUALDAD("=="), DISTINTO("!="), MENOR("<"), MAYOR(">"), MENOR_IGUAL("<="), MAYOR_IGUAL(">=");
    	
    	private String description;
    	private OpComparacion(String desc) { description = desc; };
    	public String getDesc() { return description; };
    }
    
    /**
     * Operadores logicos
     */
    public enum OpLogico {
    	SOBRERO("~"), CIRCUNFLEJO("ˆ"), DOS_MENORES("<<"), DOS_MAYORES(">>"),			
    	AND("&&"), ANDEQ(""), BIT_AND("&"), BIT_OR("|"), COMPL("~"), NOT("!"), NOT_EQ(""), OR("||"), OR_EQ(""), XOR("^"), XOR_EQ("");			
    
    	private String description;
    	private OpLogico(String desc) { description = desc; };
    	public String getDesc() { return description; };
    
    }
    
    /**
     * Operadores de asignacion
     */
    public enum OpAsignacion {
    	ASIGNACION("="), MAS_IGUAL("+="), MENOS_IGUAL("-="), POR_IGUAL("*="), DIV_IGUAL("/="), PORCENTAJE_IGUAL("%="),
    	CIRCUNFLEJO_IGUAL("^="), AND_IGUAL("&="), OR_IGUAL("|="), MAYOR_MAYOR_IGUAL(">>="), MENOR_MENOR_IGUAL("<<="),
    	PUNTERO("*");
    	
    	private String description;
    	private OpAsignacion(String desc) { description = desc; };
    	public String getDesc() { return description; };
    }
    
    /**
     * Atributo de la clase que identifica el tipo de token segun el enumerado asociado
     */
    private TipoToken tipo;
    
    /**
     * Atributo de la clase que identifica el atributo del token
     */
    private Object atributo;
    
    /**
     * Atributo de la clase que guarda si lo hay el comentario que precede 
     * al token
     */
    private String comentario;
    
    
    /**
     * Constructora de la clase
     * @param tipo
     * @param atributo
     */
	public Token(TipoToken tipo, Object atributo, String comentario) {
		this.tipo = tipo;
		this.atributo = atributo;
		this.comentario = comentario;
	}
	
    /**
     * Getter token
     * @return el tipo del token
     */
	public TipoToken getTipo() {
		return tipo;
	}
	
	/**
	 * Getter token
	 * @return el atributo del token
	 */
	public Object getAtributo() {
		return atributo;
	}
	
	/**
	 * Getter token
	 * @return el comentario del token
	 */
	public String getComentario() {
		return comentario;
	}
	
	public boolean esIgual(TipoToken t, Object ob) {
		return tipo.equals(t) && atributo.equals(ob);		
	}
	
	public boolean esIgual(TipoToken t) {
		return tipo.equals(t);		
	}
	
	public String atrString(){
		String s=new String("");
		GestorTablasSimbolos g = GestorTablasSimbolos.getGestorTS();
		
		switch(tipo){
		case EOF:	s="EOF";
		 break;
		case LIT_CARACTER:	s=(String)atributo;
		 break;
		case LIT_CADENA:	s=(String)atributo;
		 break;
		case PAL_RESERVADA:	s=g.dameNombrePalRes((Integer)atributo); 
		 break;
		case IDENTIFICADOR:	s=((EntradaTS)atributo).getLexema()+""; 
		 break;
		case NUM_REAL:		s=String.valueOf((Double)atributo);
		 break;
		case NUM_REAL_EXPO:	s=(String)atributo; //TODO:<<--CORREGIR!!
		 break;
		case NUM_ENTERO:	s=String.valueOf((Integer)atributo);
		 break;
		case SEPARADOR:		s=((Separadores)atributo).getDesc();
		 break;		
		case OP_ARITMETICO: s=((OpAritmetico)atributo).getDesc();
		 break;
		case OP_LOGICO: s=((OpLogico)atributo).getDesc();
		 break;
		case OP_COMPARACION: s=((OpComparacion)atributo).getDesc();
		 break;
		case OP_ASIGNACION: s=((OpAsignacion)atributo).getDesc();
		 break;
		case COMENT_LINEA: s=(String)(atributo+"");
		 break;
		case COMENT_LARGO: s=(String)(atributo+"");
		 break;
		case ERROR: s="";
		 break;
		default:  s="";
		 break;
		}
		
		return s;
	}
	
}
