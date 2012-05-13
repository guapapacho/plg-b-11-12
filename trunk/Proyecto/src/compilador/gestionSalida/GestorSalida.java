package compilador.gestionSalida;

import java.util.ArrayList;
import java.util.Iterator;

import compilador.gestionTablasSimbolos.EntradaTS;
import compilador.gestionTablasSimbolos.GestorTablasSimbolos;

public class GestorSalida {

	/** Buffer que almacena el codigo de funciones/procedimientos **/
	private ArrayList<String> bufferMetodos;
	/** Instancia única de la clase */
	private static GestorSalida instance = null;
	/** Cadena de caracteres que almacena el codigo "definitivo" **/
	private String resultado;
	
	public static GestorSalida getGestorSalida() {
		if(instance == null) {
			instance = new GestorSalida();
		}
		return instance;			
	}
		
	private GestorSalida(){
		bufferMetodos = new ArrayList<String>();
		resultado = "";	
	}
	
	/*
	public void anyadirAbuffer(String s){
		bufferMetodos.add(s);
	}
	*/
	
	public void finalBloque(){
		resultado += "\nVAR\n";
		GestorTablasSimbolos gestorTS = GestorTablasSimbolos.getGestorTS();
		ArrayList<EntradaTS> al = gestorTS.getEntradasBloqueActual();
		EntradaTS entrada;
		for(Iterator<EntradaTS> i = al.iterator(); i.hasNext();){
			entrada = i.next();
			resultado += entrada.getLexemaTrad()+": ";
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
		}
		
		resultado += "\nBEGIN\n";
		for(Iterator<String> i = bufferMetodos.iterator(); i.hasNext();)
			resultado += " "+i.next();
		resultado += "END;\n\n";
		
		bufferMetodos.clear();
	}

	public void emiteTabs(int num) {
		for(int i=0; i < num; i++) {
			resultado += "\t";
		}
	}
	
	public void emite(String s) {
		//resultado += " " + s;
		bufferMetodos.add(s);
	}
	
	public void emiteEnPos(int pos, String s) {
		String s1 = resultado.substring(0, pos);
		String s2 = resultado.substring(pos, resultado.length());
		resultado = s1 + s + s2;
	}

	
	public String getResultado(){
		return resultado;
	}

	public void resetResultado() {
		resultado = "";
	}
	
}
