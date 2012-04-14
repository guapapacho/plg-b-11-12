package compilador.analizadorSemantico;

public class Cabecera extends ExpresionTipo {
	private Producto dominio;
	private ExpresionTipo imagen;
	
	public Cabecera(Producto dominio, ExpresionTipo imagen) {
		super(TipoNoBasico.cabecera);
		this.dominio = dominio;
		this.imagen = imagen;
	}
	
	public Producto getDominio() {
		return dominio;
	}

	public ExpresionTipo getImagen() {
		return imagen;
	}
}
