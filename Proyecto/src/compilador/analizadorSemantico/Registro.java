package compilador.analizadorSemantico;

public class Registro extends ExpresionTipo {
	private Producto campos;
	private String nombreRegistro;

	public Registro(Producto campos) {
		super(TipoNoBasico.registro);
		this.campos = campos;
	}
	
	public String getNombreRegistro() {
		return nombreRegistro;
	}

	public void setNombreRegistro(String nombreRegistro) {
		this.nombreRegistro = nombreRegistro;
	}

	public Producto getCampos() {
		return campos;
	}
	
	public String toString() {
		return "Registro con los siguientes campos: " + campos.toString(false);
	}

}
