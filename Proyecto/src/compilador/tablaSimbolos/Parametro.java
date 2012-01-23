package compilador.tablaSimbolos;

/**
 * Clase que guarda el tipo y el modo en el que se
 * paso el parametro de una función
 * @author Grupo 1
 *
 */
public class Parametro {
	
	/**
	 * Enumerado para el tipo de parametro
	 */
	public enum TipoParam {
		ENTERO, REAL, BOOL; //TODO añadir los demas tipos
	}

	/**
	 * Enumerado para el paso de parametro
	 */
	public enum PasoParam {
		REFERENCIA, VALOR;
	}
	
	/** Tipo del argumento */
	private TipoParam tipo;
	/** Paso del argumento */
	private PasoParam paso;
	
	public Parametro(TipoParam tipo, PasoParam paso) {
		this.tipo = tipo;
		this.paso = paso;
	}

	public TipoParam getTipo() {
		return tipo;
	}

	public PasoParam getPaso() {
		return paso;
	}

}
