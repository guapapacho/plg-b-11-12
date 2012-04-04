package compilador.analizadorSemantico;

public class Cadena extends ExpresionTipo {
	private int longitud;
	private ExpresionTipo tipo; 
	
	public Cadena(int longitud) {
		super(TipoNoBasico.vector);
		this.longitud = longitud;
		this.tipo = new ExpresionTipo(TipoBasico.caracter);
	}
	
	public int getLongitud() {
		return longitud;
	}
	
	public ExpresionTipo getTipo() {
		return tipo;
	}

}
