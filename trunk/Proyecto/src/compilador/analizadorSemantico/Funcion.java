package compilador.analizadorSemantico;

public class Funcion extends ExpresionTipo {
	private Producto dominio;
	private ExpresionTipo imagen;
	
	public Funcion(Producto dominio, ExpresionTipo imagen) {
		super(TipoNoBasico.funcion);
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