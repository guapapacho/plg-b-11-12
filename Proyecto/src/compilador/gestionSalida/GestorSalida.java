package compilador.gestionSalida;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;


import compilador.analizadorSemantico.ExpresionTipo;
import compilador.gestionTablasSimbolos.EntradaTS;
import compilador.gestionTablasSimbolos.GestorTablasSimbolos;
import compilador.gestionTablasSimbolos.TablaSimbolos;

public class GestorSalida {

	public enum modoSalida {PROGRAMA,EXPRESION,FUNCION,PRINCIPAL,CONSTANTE};
	
	/** Buffer que almacena el codigo de funciones/procedimientos **/
	private ArrayList<String> bufferMetodos;
	/** Buffer que almacena el codigo de funciones/procedimientos **/
	private ArrayList<String> bufferExpresion;
	/** Buffer que almacena el codigo del main **/
	private ArrayList<String> bufferPrincipal;
	/** Buffer que almacena el codigo de las constantes **/
	private ArrayList<String> bufferConstantes;
	/** Instancia única de la clase */
	private static GestorSalida instance = null;
	/** Cadena de caracteres que almacena el codigo "definitivo" **/
	private String resultado;
	
	private String resultadoFinal;
	
	private modoSalida modoActual;
	
	private int numTabs;
	
	public static GestorSalida getGestorSalida(){
		if(instance == null) {
			instance = new GestorSalida();
		}
		return instance;			
	}
		
	private GestorSalida(){
		bufferMetodos = new ArrayList<String>();
		resultado = "";
		resultadoFinal = "";
		bufferExpresion = new ArrayList<String>();
		bufferPrincipal = new ArrayList<String>();
		bufferConstantes = new ArrayList<String>();
		modoActual = modoSalida.PROGRAMA;
		numTabs=0;
	}

	
	public modoSalida getModo(){
		return modoActual;
	}
	
	public void setModo(modoSalida m){
		this.modoActual = m;
	}
	
	public ArrayList<String> vaciarBufferExpresion(){
		ArrayList<String> temp = this.bufferExpresion;
		this.bufferExpresion = new ArrayList<String>();
		return temp;
	}
	
	public void finalExpresion(boolean principal){
		if(principal){
			for(Iterator<String> i = bufferExpresion.iterator();i.hasNext();)
				bufferPrincipal.add(i.next());
		}else {
			for(Iterator<String> i = bufferExpresion.iterator();i.hasNext();)
				bufferMetodos.add(i.next());
		}
		bufferExpresion = new ArrayList<String>();
	}
	
	public ArrayList<String> getBufferMetodos(){
		return bufferMetodos;
	}
	
	public void finalBloque(){
		resultado += "\nVAR\n";
		GestorTablasSimbolos gestorTS = GestorTablasSimbolos.getGestorTS();
		ArrayList<EntradaTS> al = gestorTS.getEntradasCompletas();
		EntradaTS entrada;
		for(Iterator<EntradaTS> i = al.iterator(); i.hasNext();){
			entrada = i.next();
			if(!entrada.esParametro() && !entrada.isConstante() ){
				resultado += "   "+entrada.getLexemaTrad()+": ";
				if(entrada.getTipo().esTipoBasico()){
					resultado += entrada.getTipo().toStringPascal()+";\n";
				}else{
					resultado += entrada.getTipo().toStringPascalDec()+";\n";
				}
			}
		}		
		resultado += "\nBEGIN\n";
		sumarTab();
		for(Iterator<String> i = bufferMetodos.iterator(); i.hasNext();)
			resultado += i.next()+" ";
		restarTab();
		resultado += "END;\n\n";
		
		bufferMetodos.clear();
		this.setModo(modoSalida.PROGRAMA);
	}

	public void sumarTab(){
		numTabs++;
	}
	
	public void restarTab(){
		numTabs--;
	}
	
	public int getNumTabs(){
		return numTabs;
	}

	public void emitirPrincipal(){
		resultado += "BEGIN\n";
		sumarTab();
		for(Iterator<String> i = bufferPrincipal.iterator();i.hasNext();){
			resultado += i.next();
		}
		restarTab();
		resultado += "END.";
	}
	
	public String tabsToString(){
		String res="";
		for(int i=0; i < numTabs; i++) {
			res += "  ";
		}
		return res;
	}
	
	public void emite(String s) {
		switch(modoActual){
		case FUNCION:
			bufferMetodos.add(s);
			if(s.contains("\n"))
				bufferMetodos.add(tabsToString());
			break;
		case EXPRESION:
			bufferExpresion.add(s);
			break;
		case PROGRAMA:
			resultado += s+" ";
			if(s.contains("\n"))
				resultado += tabsToString();
			break;
		case PRINCIPAL:
			bufferPrincipal.add(s);
			if(s.contains("\n"))
				bufferMetodos.add(tabsToString());
			break;
		case CONSTANTE:
			bufferConstantes.add(s);
			if(s.contains("\n"))
				bufferMetodos.add(tabsToString());
			
		}
	}
	
	public void emite(ArrayList<String> al){
		switch(modoActual){
		case FUNCION:
			for(Iterator<String> i = al.iterator();i.hasNext();)
				bufferMetodos.add(i.next());
			break;
		case EXPRESION:
			for(Iterator<String> i = al.iterator();i.hasNext();)
				bufferExpresion.add(i.next());
			break;
		case PROGRAMA:
			for(Iterator<String> i = al.iterator();i.hasNext();)
				resultado += i.next()+" ";
			break;
		case PRINCIPAL:
			for(Iterator<String> i = al.iterator();i.hasNext();)
				bufferPrincipal.add(i.next());
		}
		
			
	}
	
	public void emiteEnPos(int pos, String s) {
		pos = (pos < 0) ? 0 : pos;
		if(bufferMetodos.size() == 0)
			bufferMetodos.add(s);
		else {
			s += bufferMetodos.get(pos);
			bufferMetodos.set(pos, s);
		}
	}

	public String getBufferExpString(){
		String res="";
		for(Iterator<String> i=bufferExpresion.iterator();i.hasNext();)
			res += i.next();
		return res;
	}
	
	public ArrayList<String> getBufferExp(){
		return bufferExpresion;
	}
	
	public String getResultadoFinal(){
		return resultadoFinal;
	}

	public String getResultado(){
		return resultado;
	}
	
	public void resetResultado() {
		resultado = "";
		resultadoFinal = "";
		bufferMetodos.clear();
		bufferExpresion.clear();
		bufferPrincipal.clear();
		modoActual = modoSalida.PROGRAMA;
	}

	public void vaciarExpresion(){
		bufferExpresion.clear();
	}
	
	public void emitirRes(){
		resultadoFinal+=resultado;
		resultado="";
	}
	
	public void emitirVars() {
		GestorTablasSimbolos gestorTS = GestorTablasSimbolos.getGestorTS();
		ArrayList<EntradaTS> al = gestorTS.getEntradasBloqueActual();
		EntradaTS entrada;
		String res="";
		boolean yaVARS = false;
		sumarTab();
		for(Iterator<EntradaTS> i = al.iterator(); i.hasNext();){
			if(!yaVARS)
				resultadoFinal += "VAR\n";
			yaVARS = true;
			entrada = i.next();
			if(!entrada.esParametro() && !entrada.isConstante()){
				resultadoFinal += "   "+entrada.getLexemaTrad()+": ";
				if(entrada.getTipo().esTipoBasico()){
					res += entrada.getTipo().toStringPascal()+";\n";
				}else{
					res += entrada.getTipo().toStringPascalDec()+";\n";
				}
				resultadoFinal += res;
				res = "";
			}
		}
		
		TablaSimbolos t = gestorTS.dameBloqueActual().getBloquePrincipal();
		if(t!=null){
			al = t.getEntradasTrad();
			for(Iterator<EntradaTS> i = al.iterator(); i.hasNext();){
				entrada = i.next();
				if(!entrada.esParametro() && !entrada.isConstante()){
					if(!yaVARS)
						resultadoFinal += "VAR\n";
					yaVARS = true;
					resultadoFinal += "   "+entrada.getLexemaTrad()+": ";
					if(entrada.getTipo().esTipoBasico()){
						resultadoFinal += entrada.getTipo().toStringPascal()+";\n";
					}else{
						resultadoFinal += entrada.getTipo().toStringPascalDec()+";\n";
					}
				}
			}
		}
		restarTab();
		resultadoFinal += "\n";
	}
	
	public void emitirConsts(){
		resultadoFinal += "CONST\n";
		sumarTab();
		for(Iterator<String> i = bufferConstantes.iterator();i.hasNext();)
			resultadoFinal += i.next();
		bufferConstantes.clear();
		restarTab();
	}
	
	public void emitirTipos(){
		GestorTablasSimbolos gestorTS = GestorTablasSimbolos.getGestorTS();
		Hashtable<String,ExpresionTipo> h = gestorTS.getTablaTiposDef();
		boolean yaTYPE = false;
		ExpresionTipo e;
		sumarTab();
		for(String s: h.keySet()){
			e = h.get(s);
			if(!yaTYPE)
				resultadoFinal += "TYPE\n";
			yaTYPE = true;
			resultadoFinal += s + " = " + e.toStringPascal()+";\n";
		}
		restarTab();
		resultadoFinal += "\n";
	}
	
}
