package compilador.analizadorSemantico;

public class Objeto extends ExpresionTipo {
	private String nombreClase; 

	public Objeto(String nombreClase) {
		super(TipoNoBasico.objeto);
		this.nombreClase = nombreClase;
	}

	public String getNombreClase() {
		return nombreClase;
	}

}
