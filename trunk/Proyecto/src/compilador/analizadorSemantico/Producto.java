package compilador.analizadorSemantico;

public class Producto extends ExpresionTipo {
	private Object tipo1;
	private ExpresionTipo tipo2; 

	public Producto(Object tipo1, ExpresionTipo tipo2) {
		super(TipoNoBasico.producto);
		this.tipo1 = tipo1;
		this.tipo2 = tipo2;
	}
	
	public Object getTipo1() {
		return tipo1;
	}
	
	public ExpresionTipo getTipo2() {
		return tipo2;
	}

}
