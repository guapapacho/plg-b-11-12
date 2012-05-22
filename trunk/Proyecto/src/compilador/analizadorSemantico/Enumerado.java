
package compilador.analizadorSemantico;

import java.util.Iterator;

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
	
	public String toStringPascalDec(){
		return nombreEnumerado;
	}
	
	public boolean equals(Enumerado e){
		return this.elementos.equals(e.elementos);
	}
	
	public String toStringPascal(){
		String res = "(";
		for(Iterator<String> i=elementos.iterator();i.hasNext();){
			res += i.next();
			if(i.hasNext())
				res+=",";
		}
		res+=");\n";
		return res;
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
	
	public java.util.Vector<String> elementosOrdenados(){
		java.util.Vector<String> aux= new java.util.Vector<String>();
		for(int i=elementos.size()-1;i>=0;i--){
			aux.add(elementos.get(i));
		}
		return aux;
	}
	
	public String toString() {
//		return "Enumerado con los siguientes elementos: " + elementos;
		return "Enumerado con los siguientes elementos: " + elementosOrdenados();
	}

}
