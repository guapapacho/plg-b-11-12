package compilador.tablaSimbolos;

import java.util.Vector;
import compilador.tablaSimbolos.Argumento.TipoArg;

/**
 * Clase que guarda una entrada de la tabla de simbolos
 * @author Grupo 1
 */
public class EntradaTS {
	
	/**
	 * Enumerado para el tipo de un identificador
	 */
	public enum Tipo {
		PROGRAMA, PROCEDIMIENTO, FUNCION, ENTERO, REAL, BOOL; // TODO añadir el resto de tipos
	}

	/** Lexema del identificador */
	private String lexema;
	/** Tipo del identificador */
	private Tipo tipo;
	/** Argumentos del identificador si es una funcion, si no, un array vacio */
	private Vector<Argumento> args;
	/** Tipo de retorno */
	private TipoArg retorno;

	public EntradaTS(String lexema) {
		this.lexema = lexema;
		this.tipo = null;
		this.args = new Vector<Argumento>();
		this.retorno = null;
	}
	
	public EntradaTS(String lexema, Tipo tipo, Vector<Argumento> args, TipoArg retorno) {
		this.lexema = lexema;
		this.tipo = tipo;
		this.args = args;
		this.retorno = retorno;
	}
	
	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public Object getRetorno() {
		return retorno;
	}

	public void setRetorno(TipoArg retorno) {
		this.retorno = retorno;
	}

	public String getLexema() {
		return lexema;
	}

	public Vector<Argumento> getArgs() {
		return args;
	}

	public void addArg(Argumento arg) {
		args.add(arg);
	}
	
	public int getNumArgs() {
		return args.size();
	}
	
}
