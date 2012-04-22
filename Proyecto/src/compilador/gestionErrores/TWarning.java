package compilador.gestionErrores;

public class TWarning {
	private int linea;
	private int columna;
	private String mensaje;

	public int getLinea() {
		return linea;
	}

	public int getColumna() {
		return columna;
	}

	public String toString() {
		return "Warning en L: "+linea+" C: "+columna+" - "+mensaje;
	}

	public TWarning(String string, int linea, int columna){
		this.mensaje = string;
		this.linea = linea;
		this.columna = columna;
	}
}
