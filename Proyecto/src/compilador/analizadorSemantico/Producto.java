package compilador.analizadorSemantico;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

public class Producto extends ExpresionTipo {
	/** Para simpliicar codigo se crea una tabla hash que contiene el lexema de cada 
	 * identificador y el tipo semantico **/
	private Hashtable<String,ExpresionTipo> tablaProd;
	
	public Producto()
	{
		super(TipoNoBasico.producto);
		this.tablaProd = new Hashtable<String,ExpresionTipo>();
	}
	
	public Producto(String s, ExpresionTipo e)
	{
		super(TipoNoBasico.producto);
		this.tablaProd = new Hashtable<String,ExpresionTipo>();
		this.tablaProd.put(s, e);
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
	
	public ExpresionTipo getTipoCampo(String campo) {
		return tablaProd.get(campo);
	}
		
	public Hashtable<String, ExpresionTipo> getTablaProd() {
		return tablaProd;
	}

	public void setTabla(Hashtable<String, ExpresionTipo> tabla) {
		this.tablaProd = tabla;
	}
	
	public boolean esVacio() {
		return tablaProd.size() == 0;
	}
	
	public String toString () {
		String s = "";
		Collection<ExpresionTipo> colection = tablaProd.values(); 
		ArrayList<ExpresionTipo> lista = new ArrayList<ExpresionTipo>();
		for(ExpresionTipo tipo: colection) {
			lista.add(tipo);
		}
		Collections.reverse(lista);
		for(ExpresionTipo tipo: lista) {
			s += tipo.toString();
			s += tipo.isPasoReferencia() ?  " (referencia)" : " (valor)";
			s += ", ";
		}
		return s.substring(0, s.length()-2);
	}

}
