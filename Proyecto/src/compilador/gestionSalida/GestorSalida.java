package compilador.gestionSalida;

import java.util.ArrayList;
import java.util.Iterator;

import compilador.analizadorSemantico.Vector;
import compilador.gestionTablasSimbolos.EntradaTS;
import compilador.gestionTablasSimbolos.GestorTablasSimbolos;
import compilador.gestionTablasSimbolos.TablaSimbolos;

public class GestorSalida {

	public enum modoSalida {PROGRAMA,EXPRESION,FUNCION,PRINCIPAL};
	
	/** Buffer que almacena el codigo de funciones/procedimientos **/
	private ArrayList<String> bufferMetodos;
	/** Buffer que almacena el codigo de funciones/procedimientos **/
	private ArrayList<String> bufferExpresion;
	/** Buffer que almacena el codigo del main **/
	private ArrayList<String> bufferPrincipal;
	/** Instancia única de la clase */
	private static GestorSalida instance = null;
	/** Cadena de caracteres que almacena el codigo "definitivo" **/
	private String resultado;
	
	private String resultadoFinal;
	
	private modoSalida modoActual;
	
	public static GestorSalida getGestorSalida(){//String nombre) {
		if(instance == null) {
			instance = new GestorSalida();//nombre);
		}
		return instance;			
	}
		
	private GestorSalida(){//String nombre){
		bufferMetodos = new ArrayList<String>();
		resultado = "";
		resultadoFinal = "";
		//resultado = "PROGRAM "+nombre+";\n";	
		bufferExpresion = new ArrayList<String>();
		bufferPrincipal = new ArrayList<String>();
		modoActual = modoSalida.PROGRAMA;
	}
	
	/*
	public void anyadirAbuffer(String s){
		bufferMetodos.add(s);
	}
	*/
	
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
		//this.modoActual = modoSalida.FUNCION;
	}
	
	public void finalBloque(){
		resultado += "\nVAR\n";
		GestorTablasSimbolos gestorTS = GestorTablasSimbolos.getGestorTS();
		ArrayList<EntradaTS> al = gestorTS.getEntradasCompletas();
		EntradaTS entrada;
		for(Iterator<EntradaTS> i = al.iterator(); i.hasNext();){
			entrada = i.next();
			if(!entrada.esParametro()){
				resultado += entrada.getLexemaTrad()+": ";
				if(entrada.getTipo().esTipoBasico()){
					switch(entrada.getTipo().getTipoBasico()){
					case logico:
						resultado += "BOOLEAN;\n";
						break;
					case caracter:
						resultado += "CHAR;\n";
						break;
					case entero:
						resultado += "INTEGER;\n";
						break;
					case real:
						resultado += "FLOAT;\n";
						break;
					case vacio:
						resultado += "VACIO?? ;\n";
						break;
					case error_tipo: 	
						resultado += "ERROR?? ;\n";
					}
				}else{
					switch(entrada.getTipo().getTipoNoBasico()){
					case vector:
						resultado += "ARRAY [0.."+(((Vector)entrada.getTipo()).getLongitud()-1)+"] OF "+((Vector)entrada.getTipo()).getTipoElementos().toStringPascal()+"; \n";
					}
				}
			}
		}
		
		
		
		resultado += "\nBEGIN\n";
		for(Iterator<String> i = bufferMetodos.iterator(); i.hasNext();)
			resultado += i.next()+" ";
		resultado += "END;\n\n";
		
		bufferMetodos.clear();
		this.setModo(modoSalida.PROGRAMA);
	}

	public void emiteTabs(int num) {
		for(int i=0; i < num; i++) {
			resultado += "\t";
		}
	}
	
	public void emitirPrincipal(){
		resultado += "BEGIN\n";
		for(Iterator<String> i = bufferPrincipal.iterator();i.hasNext();){
			resultado += i.next();
		}
		resultado += "END.";
	}
	
	public void emite(String s) {
		//resultado += " " + s;
		switch(modoActual){
		case FUNCION:
			bufferMetodos.add(s);
			break;
		case EXPRESION:
			bufferExpresion.add(s);
			break;
		case PROGRAMA:
			resultado += s+" ";
			break;
		case PRINCIPAL:
			bufferPrincipal.add(s);
		}
		//System.out.println(s);
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
		String s1 = resultado.substring(0, pos);
		String s2 = resultado.substring(pos, resultado.length());
		resultado = s1 + s + s2;
	}

	public String getBufferExpresion(){
		String res="";
		for(Iterator<String> i=bufferExpresion.iterator();i.hasNext();)
			res += i.next();
		return res;
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
		boolean yaVARS = false;
		for(Iterator<EntradaTS> i = al.iterator(); i.hasNext();){
			if(!yaVARS)
				resultadoFinal += "VAR\n";
			yaVARS = true;
			entrada = i.next();
			if(!entrada.esParametro()){
				resultadoFinal += entrada.getLexemaTrad()+": ";
				if(entrada.getTipo().esTipoBasico()){
					switch(entrada.getTipo().getTipoBasico()){
					case logico:
						resultadoFinal += "BOOLEAN;\n";
						break;
					case caracter:
						resultadoFinal += "CHAR;\n";
						break;
					case entero:
						resultadoFinal += "INTEGER;\n";
						break;
					case real:
						resultadoFinal += "FLOAT;\n";
						break;
					case vacio:
						resultadoFinal += "VACIO?? ;\n";
						break;
					case error_tipo: 	
						resultadoFinal += "ERROR?? ;\n";
					}
				}else{
					switch(entrada.getTipo().getTipoNoBasico()){
					case vector:
						resultadoFinal += "ARRAY [0.."+(((Vector)entrada.getTipo()).getLongitud()-1)+"] OF "+((Vector)entrada.getTipo()).getTipoElementos().toStringPascal()+"; \n";
					}
				}
			}
		}
		
		TablaSimbolos t = gestorTS.dameBloqueActual().getBloquePrincipal();
		if(t!=null){
			al = t.getEntradasTrad();
			for(Iterator<EntradaTS> i = al.iterator(); i.hasNext();){
				entrada = i.next();
				if(!entrada.esParametro()){
					if(!yaVARS)
						resultadoFinal += "VAR\n";
					yaVARS = true;
					resultadoFinal += entrada.getLexemaTrad()+": ";
					if(entrada.getTipo().esTipoBasico()){
						switch(entrada.getTipo().getTipoBasico()){
						case logico:
							resultadoFinal += "BOOLEAN;\n";
							break;
						case caracter:
							resultadoFinal += "CHAR;\n";
							break;
						case entero:
							resultadoFinal += "INTEGER;\n";
							break;
						case real:
							resultadoFinal += "FLOAT;\n";
							break;
						case vacio:
							resultadoFinal += "VACIO?? ;\n";
							break;
						case error_tipo: 	
							resultadoFinal += "ERROR?? ;\n";
						}
					}else{
						switch(entrada.getTipo().getTipoNoBasico()){
						case vector:
							resultadoFinal += "ARRAY [0.."+(((Vector)entrada.getTipo()).getLongitud()-1)+"] OF "+((Vector)entrada.getTipo()).getTipoElementos().toStringPascal()+"; \n";
						}
					}
				}
			}
		}
		resultadoFinal += "\n";
	}
	
}
