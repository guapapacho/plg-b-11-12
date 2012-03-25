package compilador.analizadorSemantico;

public class Vector extends ExpresionTipo {
	private int longitud;
	private ExpresionTipo tipo; 

	public Vector(int longitud, ExpresionTipo tipo) {
		super(TipoNoBasico.vector);
		this.longitud = longitud;
		this.tipo = tipo;
	}
	
	public int getLongitud() {
		return longitud;
	}
	
	public ExpresionTipo getTipo() {
		return tipo;
	}

}
