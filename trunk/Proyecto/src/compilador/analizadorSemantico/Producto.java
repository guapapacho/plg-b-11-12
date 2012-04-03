package compilador.analizadorSemantico;

import java.util.Enumeration;
import java.util.Hashtable;

public class Producto extends ExpresionTipo {
	private ExpresionTipo tipo1;
	private ExpresionTipo tipo2; 
	
	/** Para simpliicar codigo se crea una tabla hash que contiene el lexema de cada 
	 * identificador y el tipo semantico **/
	private Hashtable<String,ExpresionTipo> tablaProd;
	
	public Producto(String s, ExpresionTipo e)
	{
		super(TipoNoBasico.producto);
		this.tablaProd = new Hashtable<String,ExpresionTipo>();
		this.tablaProd.put(s, e);
	}

	public Producto(ExpresionTipo tipo1, ExpresionTipo tipo2) {
		super(TipoNoBasico.producto);
		this.tipo1 = tipo1;
		this.tipo2 = tipo2;
	}
	
	public void ponProducto(String s, ExpresionTipo e) throws Exception {
		if(this.tablaProd.containsKey(s))
			throw new Exception("id repetido");
		this.tablaProd.put(s, e);
	}
	
	public void ponProductos(Hashtable<String,ExpresionTipo> e) throws Exception {
		 for (Enumeration<String> f = e.keys(); f.hasMoreElements();)
		 {	 
		       String d = f.nextElement();
		       if(this.tablaProd.containsKey(d))
		    	   throw new Exception("id repetido");
		       else
		    	   this.tablaProd.put(d, e.get(d));
		}	
	}
		
	public Hashtable<String, ExpresionTipo> getTablaProd() {
		return tablaProd;
	}

	public void setTabla(Hashtable<String, ExpresionTipo> tabla) {
		this.tablaProd = tabla;
	}
	
	public ExpresionTipo getTipo1() {
		return tipo1;
	}
	
	public ExpresionTipo getTipo2() {
		return tipo2;
	}

}
