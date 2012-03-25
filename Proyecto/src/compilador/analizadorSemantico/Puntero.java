package compilador.analizadorSemantico;

public class Puntero extends ExpresionTipo {
	private ExpresionTipo tipo; 

	public Puntero(ExpresionTipo tipo) {
		super(TipoNoBasico.puntero);
		this.tipo = tipo;
	}

	public ExpresionTipo getTipo() {
		return tipo;
	}

}
