package compilador.gestionSalida;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import compilador.gestionTablasSimbolos.GestorTablasSimbolos;

public class GestorSalida {

	/** Buffer que almacena el codigo de funciones/procedimientos **/
	private ArrayList<String> bufferMetodos;
	/** Instancia única de la clase */
	private static GestorSalida instance = null;
	/** Cadena de caracteres que almacena el codigo "definitivo" **/
	private String resultado;
	
	public static GestorSalida getGestorTS() {
		if(instance == null) {
			instance = new GestorSalida();
		}
		return instance;			
	}
		
	private GestorSalida(){
		bufferMetodos = new ArrayList<String>();
		resultado = "";	
	}
	
	public void anyadirAbuffer(String s){
		bufferMetodos.add(s);
	}
	
	public void volcarBuffer(){
		for(Iterator<String> i = bufferMetodos.iterator(); i.hasNext();)
			emite(i.next());
		bufferMetodos.clear();
	}
	
	public void emite(String s){
		resultado += s+" ";
	}
	
	public String getResultado(){
		return resultado;
	}
	
}