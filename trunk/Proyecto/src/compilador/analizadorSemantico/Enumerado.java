
package compilador.analizadorSemantico;

public class Enumerado extends ExpresionTipo {
 
	/** vector donde guardar los elementos que contiene el tipo Vector*/
	private java.util.Vector<String> elementos;

	private String nombreEnumerado;
	 
	public Enumerado(String e)
	{
		super(TipoNoBasico.enumerado);
		elementos = new java.util.Vector<String>();
		elementos.add(e);
		
	}
	public Enumerado()
	{
		super(TipoNoBasico.enumerado);
		elementos = new java.util.Vector<String>();	
	}
	
	public String getNombreEnumerado() {
		return nombreEnumerado;
	}

	public void setNombreEnumerado(String nombreEnumerado) {
		this.nombreEnumerado = nombreEnumerado;
	}
	
	public boolean ponElemento(String e) {
		if(elementos.contains(e))
			return false;
		else {
			elementos.add(e);
			return true;
		}
	}
	
	public String toString() {
		return "Enumerado' con los siguientes elementos: " + elementos;
	}

}
