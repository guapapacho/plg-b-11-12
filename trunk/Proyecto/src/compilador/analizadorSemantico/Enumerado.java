
package compilador.analizadorSemantico;

public class Enumerado extends ExpresionTipo {

	private ExpresionTipo tipo; 
	/** vector donde guardar los elementos que contiene el tipo Vector*/
	private java.util.Vector<String> elementos;

	 
	public Enumerado(String e)
	{
		super(TipoNoBasico.enumerado);
		elementos = new java.util.Vector<String>();
		elementos.add(e);
		
	}
	
	public boolean ponElemento(String e) {
		if(elementos.contains(e))
			return false;
		else {
			elementos.add(e);
			return true;
		}
	}
	
	
	public ExpresionTipo getTipo() {
		return tipo;
	}
	
	public String toString() {
		return "Enumerado de tipo " + tipo + "con los siguientes elementos:\n"
						+ elementos;
	}

}
