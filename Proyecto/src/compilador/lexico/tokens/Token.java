package compilador.lexico.tokens;

/**
 * 
 * @author Grupo 1 
 * 
 */
public class Token {
    

    /**
     * 
     */
	public enum Tipo {
        EOF, LIT_CARACTER, LIT_CADENA, PAL_RESERVADA, 
        IDENTIFICADOR, NUM_REAL, NUM_ENTERO, SEPARADOR, 
        OP_ARITMETICO, OP_LOGICO, OP_COMPARACION, OP_ASIGNACION
    };
    
    /**
     * Atributo de la clase que identifica el tipo de token segun el enumerado asociado
     */
    private Tipo tipo;
    
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
	public Token(Tipo tipo, Object atributo) {
		this.tipo = tipo;
		this.atributo = atributo;
	}
	
    /**
     * 
     * @return
     */
	public Tipo getTipo() {
		return tipo;
	}

	/**
	 * 
	 * @param tipo
	 */
	public void setTipo(Tipo tipo) {
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
