package compilador.gestionSalida;

import java.util.ArrayList;
import java.util.Iterator;

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
	
	public void anyadirAbuffer(String s){
		bufferMetodos.add(s);
	}
	
	public void volcarBuffer(){
		for(Iterator<String> i = bufferMetodos.iterator(); i.hasNext();)
			emite(i.next());
		bufferMetodos.clear();
	}

	public void emiteTabs(int num) {
		for(int i=0; i < num; i++) {
			resultado += "\t";
		}
	}
	
	public void emite(String s) {
		resultado += s;
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
