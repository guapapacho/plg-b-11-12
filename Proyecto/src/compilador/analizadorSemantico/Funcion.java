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
	
	public String toString () {
		String aux= (imagen == null) ? "Procedimiento " : "Función ";
		if (dominio.esVacio()) {
			aux += "que no recibe parametros";
		} else {
			aux += "que recibe los parametros: " + dominio.toString(true);
		}
		if (imagen != null) {
			aux += " y devuelve: " + imagen;
		}
		return aux;
	}
}
