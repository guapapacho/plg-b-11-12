package compilador.tablaSimbolos;

import java.util.ArrayList;
import java.util.Hashtable;

public class Ambito {
	
	private int continente;

	
	private Hashtable<String, ArrayList<Object>> filaAmbito;


	public Hashtable<String, ArrayList<Object>> getFilaAmbito() {
		return filaAmbito;
	}

	public void setFilaAmbito(Hashtable<String, ArrayList<Object>> filaAmbito) {
		this.filaAmbito = filaAmbito;
	}



	public int getContinente() {
		return continente;
	}

	public void setContinente(int continente) {
		this.continente = continente;
	}

	

}
