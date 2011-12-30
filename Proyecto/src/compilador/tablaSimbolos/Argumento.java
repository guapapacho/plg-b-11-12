package compilador.tablaSimbolos;

/**
 * Clase que guarda el tipo y el modo en el que se
 * paso el argumento de una función
 * @author Grupo 1
 *
 */
public class Argumento {
	
	/**
	 * Enumerado para el tipo de argumento
	 */
	public enum TipoArg {
		ENTERO, REAL, BOOL; //TODO añadir los demas tipos
	}

	/**
	 * Enumerado para el paso de argumento
	 */
	public enum PasoArg {
		REFERENCIA, VALOR;
	}
	
	/** Tipo del argumento */
	private TipoArg tipo;
	/** Paso del argumento */
	private PasoArg paso;
	
	public Argumento(TipoArg tipo, PasoArg paso) {
		this.tipo = tipo;
		this.paso = paso;
	}

	public TipoArg getTipo() {
		return tipo;
	}

	public PasoArg getPaso() {
		return paso;
	}

}
