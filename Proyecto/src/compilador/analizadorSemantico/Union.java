package compilador.analizadorSemantico;

public class Union extends ExpresionTipo {
	private Producto campos;

	public Union(Producto campos) {
		super(TipoNoBasico.union);
		this.campos = campos;
	}
	
	public Producto getCampos() {
		return campos;
	}
	
	public String toString() {
		return "Union con los siguientes campos:\n" + campos;
	}

}
