package compilador.gestionTablasSimbolos;

import compilador.analizadorSemantico.ExpresionTipo;

/**
 * Clase que guarda el tipo y el modo en el que se
 * paso el parametro de una función
 * @author Grupo 1
 *
 */
public class Parametro {

	/**
	 * Enumerado para el paso de parametro
	 */
	public enum PasoParam {
		REFERENCIA, VALOR;
	}
	
	/** Tipo del argumento */
	private ExpresionTipo tipo;
	/** Paso del argumento */
	private PasoParam paso;
	
	public Parametro(ExpresionTipo tipo, PasoParam paso) {
		this.tipo = tipo;
		this.paso = paso;
	}

	public ExpresionTipo getTipo() {
		return tipo;
	}

	public PasoParam getPaso() {
		return paso;
	}

}
