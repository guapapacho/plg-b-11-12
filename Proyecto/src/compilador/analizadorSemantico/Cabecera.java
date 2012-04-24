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
	
	public String toString () {
		String aux="";
		if (dominio.esVacio()) {
			aux += "Cabecera que no recibe parametros";
		} else {
			aux += "Cabecera que recibe los parametros: " + dominio.toString(true);
		}
		if (imagen != null) {
			aux += " y devuelve: " + imagen;
		}
		return aux;
	}
}
