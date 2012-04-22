package compilador.gestionErrores;


public class TError {

	public enum TipoError {
		LEXICO("léxico"), SINTACTICO("sintáctico"), SEMANTICO("semántico");
		
		private String description;
		private TipoError(String desc) { description = desc; };
		public String toString() { return description; };
	}
	
	private TipoError tipo;
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
		return "Error "+tipo+" en L: "+linea+" C: "+columna+" - "+mensaje;
	}

	public TError(TipoError tipo, String string, int linea, int columna){
		this.tipo = tipo;
		this.mensaje = string;
		this.linea = linea;
		this.columna = columna;
	}
}
