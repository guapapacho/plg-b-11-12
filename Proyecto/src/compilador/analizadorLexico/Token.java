package compilador.analizadorLexico;

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
        COMENT_LINEA, COMENT_LARGO, ERROR
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
    	SUMA, RESTA, INCREMENTO, DECREMENTO, MULTIPLICACION, DIVISION, PORCENTAJE
    }
    
    /**
     * Operadores de comparacion
     */
    public enum OpComparacion {
    	IGUALDAD, DISTINTO, MENOR, MAYOR, MENOR_IGUAL, MAYOR_IGUAL
    }
    
    /**
     * Operadores logicos
     */
    public enum OpLogico {
    	SOBRERO, CIRCUNFLEJO, DOS_MENORES, DOS_MAYORES,			
    	AND, ANDEQ, BIT_AND, BIT_OR, COMPL, NOT, NOT_EQ, OR, OR_EQ, XOR, XOR_EQ			
    }
    
    /**
     * Operadores de asignacion
     */
    public enum OpAsignacion {
    	ASIGNACION, MAS_IGUAL, MENOS_IGUAL, POR_IGUAL, DIV_IGUAL, PORCENTAJE_IGUAL,
    	CIRCUNFLEJO_IGUAL, AND_IGUAL, OR_IGUAL, MAYOR_MAYOR_IGUAL, MENOR_MENOR_IGUAL,
    	PUNTERO
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
		
		switch(tipo){
		case EOF:	s="EOF";
		 break;
		case LIT_CARACTER:	s=(String)atributo;
		 break;
		case LIT_CADENA:	s=(String)atributo;
		 break;
		case PAL_RESERVADA:	s="PAL_RES"; //TODO:<<--CORREGIR!!
		 break;
		case IDENTIFICADOR:	s=(String)atributo;
		 break;
		case NUM_REAL:		s=String.valueOf((Double)atributo);
		 break;
		case NUM_REAL_EXPO:	s="NUM_EXP"; //TODO:<<--CORREGIR!!
		 break;
		case NUM_ENTERO:	s=String.valueOf((Integer)atributo);
		 break;
		case SEPARADOR:		s="";//s=(String)atributo;
		 break;		
		case OP_ARITMETICO: s="";
		 break;
		case OP_LOGICO: s="";
		 break;
		case OP_COMPARACION: s="";
		 break;
		case OP_ASIGNACION: s="";
		 break;
		case COMENT_LINEA: s="";
		 break;
		case COMENT_LARGO: s="";
		 break;
		case ERROR: s="";
		 break;
		default:  s="";
		 break;
		}
		
		return s;
	}
	
}
