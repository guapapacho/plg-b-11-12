package compilador.tablaSimbolos;

import java.util.EnumMap;
import java.util.Hashtable;

public class Ambito {
	
	public enum Atributos {
		TIPO, 
		NUMARGS,
		TIPOARGS,
		PASOARGS,
		RETORNO,
		CONTENIDO
	}
	private int continente;

	private Hashtable<String, EnumMap<Atributos, Object>> filaAmbito;
	
//	private Hashtable<String, ArrayList<Object>> filaAmbito2;


	public int getContinente() {
		return continente;
	}

	public Hashtable<String, EnumMap<Atributos,Object>> getFilaAmbito() {
		return filaAmbito;
	}

	public void setFilaAmbito(
			Hashtable<String, EnumMap<Atributos, Object>> filaAmbito) {
		this.filaAmbito = filaAmbito;
	}

	public void setContinente(int continente) {
		this.continente = continente;
	}
	
	


	

}
