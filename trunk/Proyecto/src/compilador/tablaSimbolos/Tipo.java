package compilador.tablaSimbolos;

public class Tipo {
	
	/**
	 * Enumerado para el tipo de un identificador
	 */
	public enum EnumTipo {
		PROGRAMA, PROCEDIMIENTO, FUNCION, SIMPLE, DEFINIDO, ENTERO, REAL, LOGICO, CARACTER, PUNTERO_ENTERO, PUNTERO_REAL, PUNTERO_LOGICO, PUNTERO_CARACTER; // TODO añadir otros tipos
	}
	
	private EnumTipo tipo;
	private String nombre;

	public Tipo(EnumTipo tipo, String nombre) {
		this.tipo = tipo;
		this.nombre = null;
		if(!EnumTipo.PROGRAMA.equals(tipo) && !EnumTipo.PROCEDIMIENTO.equals(tipo))
			this.nombre = nombre;
	}

	public void setTipo(EnumTipo tipo) {
		this.tipo = tipo;
	}

	public EnumTipo getTipo() {
		return tipo;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public String getNombre() {
		return nombre;
	}
	
}
