package compilador.analizadorSemantico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

public class Producto extends ExpresionTipo {
	/** Para simplificar codigo se crea una tabla hash que contiene el lexema de cada 
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

	public boolean equals(Producto p){
		return this.toString(true).equals(p.toString(true));
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
	
	public String toStringPascal(){
		ExpresionTipo e;
		String res = "";
		for(String s: tablaProd.keySet()){
			e = tablaProd.get(s);
			res += s+" : "+e.toStringPascal()+";\n";
		}
		return res;
	}
	
	public String toString (boolean funcion) {
		String s = "";
		Set<String> colection = tablaProd.keySet(); 
		if(funcion) {
			ArrayList<String> lista = new ArrayList<String>();
			for(String tipo: colection) {
				lista.add(tipo);
			}
			Collections.reverse(lista);
			for(String nombre: lista) {
				ExpresionTipo tipo = tablaProd.get(nombre);
				s += nombre + "::" + tipo.toString();
				s += tipo.isPasoReferencia() ?  " (referencia)" : " (valor)";
				s += ", ";
			}
		} else {
			for(String nombre: colection) {
				ExpresionTipo tipo = tablaProd.get(nombre);
				s += nombre + " " + tipo.toString();
				s += ", ";
			}
		}
		if(s.length() > 2)
			s = s.substring(0, s.length()-2);
		return s;
	}

}
