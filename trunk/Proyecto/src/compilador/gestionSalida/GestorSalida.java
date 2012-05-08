package compilador.gestionSalida;

import java.io.File;
import java.util.ArrayList;

import compilador.gestionTablasSimbolos.GestorTablasSimbolos;

public class GestorSalida {

	/** Buffer que almacena el codigo de funciones/procedimientos **/
	private ArrayList<String> bufferMetodos;
	/** Instancia única de la clase */
	private static GestorSalida instance= null;
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
			
	}
	
	private void anyadirBuffer(String s){
		bufferMetodos.add(s);
	}
	
	// TODO: completar esto!!!
	/*private void liberarBuffer(){
		for()
	}*/
	
	private void emite(String s){
		// TODO: anyadir s al fichero de salida!!
	}
	
	
	
}
