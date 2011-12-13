package compilador.gestionErrores;

public class ErrorLexico {
	private int numLinea;
	private int numColumna;
	private String mensaje;
	
	public ErrorLexico(int numLinea, int numColumna, String mensaje) {
		this.numLinea = numLinea;
		this.numColumna = numColumna;
		this.mensaje = mensaje;
	}
	
	public int getNumLinea() {
		return numLinea;
	}
	public int getNumColumna() {
		return numColumna;
	}
	public String getMensaje() {
		return mensaje;
	}
	public String toString() {
		return "Error en linea " + numLinea + " y columna " + numColumna + ": " + mensaje;
	}

}
