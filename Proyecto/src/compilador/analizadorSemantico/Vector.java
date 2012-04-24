package compilador.analizadorSemantico;

public class Vector extends ExpresionTipo {
	private int longitud;
	private ExpresionTipo tipoElementos; 
	/** vector donde guardar los elementos que contiene el tipo Vector*/
	private java.util.Vector<String> elementos;
	
	public Vector(int longitud, ExpresionTipo tipoElementos) {
		super(TipoNoBasico.vector);
		this.longitud = longitud;
		this.tipoElementos = tipoElementos;
	}
	
	
	public boolean ponElemento(String e) {
		if(elementos.contains(e))
			return false;
		else {
			elementos.add(e);
			return true;
		}
	}
	
	public int getLongitud() {
		return longitud;
	}

	
	public ExpresionTipo getTipoElementos() {
		return tipoElementos;
	}
	
	public String toString() {
		return "Vector de " + tipoElementos + "s de dimensión " + longitud + ".";
	}

	public boolean equals(Vector v){
		return this.longitud==v.getLongitud() && this.tipoElementos.equals(v.getTipoElementos());
	}
}
