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
		String aux="";
		if (dominio.equals(TipoBasico.vacio)) {
			aux += "Funcion que no recibe parametros\n";
		} else {
			aux += "Funcion que recibe los parametros: " + dominio + "\n";
		}
		if (imagen.equals(TipoBasico.vacio)) {
			aux += "y no devuelve nada.";
		} else {
			aux += " y devuelve: " + imagen;
		}
		return aux;
	}
}
