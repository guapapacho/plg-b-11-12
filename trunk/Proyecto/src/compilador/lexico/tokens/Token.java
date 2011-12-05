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
    private int numlinea;
    
    /**
     * Numero de columna en la que se encuentra el token
     */
    private int numcolumna;
    
    /**
     * Constructora de la clase
     * @param tipo
     * @param atributo
     * @param numlinea
     * @param numcolumna
     */
	public Token(Tipo tipo, Object atributo, int numlinea, int numcolumna) {
		this.tipo = tipo;
		this.atributo = atributo;
		this.numlinea = numlinea;
		this.numcolumna = numcolumna;
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
	public Object getArgumento() {
		return argumento;
	}
	
	/**
	 * 
	 * @param argumento
	 */
	public void setArgumento(Object argumento) {
		this.argumento = argumento;
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
