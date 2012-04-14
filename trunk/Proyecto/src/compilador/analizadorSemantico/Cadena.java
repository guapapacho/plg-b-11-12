package compilador.analizadorSemantico;

public class Cadena extends ExpresionTipo {
	private int longitud;
	private ExpresionTipo tipoElementos;
	
	public Cadena() {
		this(0);
	}
	public Cadena(int longitud) {
		super(TipoNoBasico.cadena);
		this.longitud = longitud;
		this.tipoElementos = new ExpresionTipo(TipoBasico.caracter);
	}
	
	public int getLongitud() {
		return longitud;
	}
	/*
	public ExpresionTipo getTipo() {
		return tipo;
	}
	*/
	public ExpresionTipo getTipoElementos() {
		return tipoElementos;
	}
	
	public String toString() {
		return "Cadena de " + tipoElementos + ".";
	}

}
