package compilador.lexico.tokens;

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
        IDENTIFICADOR, NUM_REAL, NUM_ENTERO, SEPARADOR, 
        OP_ARITMETICO, OP_LOGICO, OP_COMPARACION, OP_ASIGNACION,
        COMENTARIO, ERROR
    }
    
    /**
     * Separadores
     */
    public enum Separadores {
    	PUNTO("."),PUNTO_COMA(";"), ABRE_LLAVE("{"), CIERRA_LLAVE("}"), ABRE_CORCHETE("["),
    	CIERRA_CORCHETE("]"), ALMOHADILLA("#"), DOBLE_ALMOHADILLA("##"), ABRE_PARENTESIS("("),
    	CIERRA_PARENTESIS(")"), MENOS_DOSPUNTOS("<:"), DOSPUNTOS_MAYOR(":>"), MENOR_PORCENTAJE("<%"),
    	PORCENTAJE_MAYOR("%>"), PORCENTAJE_DOSPUNTOS("%:"), PORCENTAJE_DOSPUNTOS_BIS("%:%:");
    	
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
    	IGUAL, DISTINTO, MENOR, MAYOR, MENOR_IGUAL, MAYOR_IGUAL
    }
    
    /**
     * Operadores logicos
     */
    public enum OpLogico {
    	Y, O, NO, Y_BIT, O_BIT, SOBRERO, CIRCUNFLEJO, DOS_MENORES, DOS_MAYORES,			//Revisar!!!! no se si se podrian poner menos porque
    	AND, ANDEQ, BIT_AND, BIT_OR, COMPL, NOT, NOT_EQ, OR, OR_EQ, XOR, XOR_EQ			//sea el mismo significado...
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
     * Numero de linea en el que se encuentra el token
     */
    private int numlinea = 1;
    
    /**
     * Numero de columna en la que se encuentra el token
     */
    private int numcolumna = 0;
    
    /**
     * Constructora de la clase
     * @param tipo
     * @param atributo
     * @param numlinea
     * @param numcolumna
     */
	public Token(TipoToken tipo, Object atributo) {
		this.tipo = tipo;
		this.atributo = atributo;
	}
	
    /**
     * 
     * @return
     */
	public TipoToken getTipo() {
		return tipo;
	}

	/**
	 * 
	 * @param tipo
	 */
	public void setTipo(TipoToken tipo) {
		this.tipo = tipo;
	}
	
	/**
	 * 
	 * @return
	 */
	public Object getAtributo() {
		return atributo;
	}
	
	/**
	 * 
	 * @param argumento
	 */
	public void setAtribto(Object atributo) {
		this.atributo = atributo;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getNumlinea() {
		return numlinea;
	}
	
	/**
	 * 
	 * @param numlinea
	 */
	public void setNumlinea(int numlinea) {
		this.numlinea = numlinea;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getNumcolumna() {
		return numcolumna;
	}
	
	/**
	 * 
	 * @param numcolumna
	 */
	public void setNumcolumna(int numcolumna) {
		this.numcolumna = numcolumna;
	}
}
