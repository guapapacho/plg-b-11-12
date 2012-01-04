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
     * Constructora de la clase
     * @param tipo
     * @param atributo
     */
	public Token(TipoToken tipo, Object atributo) {
		this.tipo = tipo;
		this.atributo = atributo;
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
	
	public boolean esIgual(TipoToken t, Object ob) {
		return tipo.equals(t) && atributo.equals(ob);		
	}
	
	public boolean esIgual(TipoToken t) {
		return tipo.equals(t);		
	}
	
	public boolean esOpNiv0() {
		return tipo.compareTo(TipoToken.OP_COMPARACION) == 0;
	}
	
	public boolean esOpNiv1() {
		return atributo == OpAritmetico.SUMA ||
				atributo == OpAritmetico.RESTA ||
				atributo == OpLogico.OR;
	}
	
	public boolean esOpNiv2() {
		return atributo == OpAritmetico.MULTIPLICACION ||
				atributo == OpAritmetico.DIVISION ||
				atributo == OpAritmetico.PORCENTAJE ||
				atributo == OpLogico.AND;
	}
	
	public boolean esOpNiv3() {
		return atributo == OpLogico.DOS_MAYORES ||
				atributo == OpLogico.DOS_MENORES;
	}
	
	public boolean esOpNiv4() {
		return atributo == OpLogico.NOT ||
				atributo == OpAritmetico.RESTA ||
				atributo == OpAritmetico.INCREMENTO ||
				atributo == OpAritmetico.DECREMENTO;
	}
}
