package compilador.analizadorSemantico;
import java.util.*;

public class Vector extends ExpresionTipo {
	private int longitud;
	private ExpresionTipo tipo; 
	/** vector donde guardar los elementos que contiene el tipo Vector*/
	private java.util.Vector<String> elementos;
	
	public Vector(int longitud, ExpresionTipo tipo) {
		super(TipoNoBasico.vector);
		this.longitud = longitud;
		this.tipo = tipo;
	}
	
	
	public boolean ponElemento(String e) {
		if(elementos.contains(e))
			return false;
		else {
			elementos.add(e);
			return true;
		}
	}
	
	public int getLongitud() {
		return longitud;
	}
	
	public ExpresionTipo getTipo() {
		return tipo;
	}

}
