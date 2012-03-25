package compilador.analizadorSemantico;

public class Registro extends ExpresionTipo {
	private Producto campos;

	public Registro(Producto campos) {
		super(TipoNoBasico.registro);
		this.campos = campos;
	}
	
	public Producto getCampos() {
		return campos;
	}

}
