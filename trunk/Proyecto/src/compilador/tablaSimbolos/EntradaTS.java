package compilador.tablaSimbolos;

import java.util.Vector;

import compilador.analizadorSemantico.ExpresionTipo;
import compilador.tablaSimbolos.Parametro.TipoParam;

/**
 * Clase que guarda una entrada de la tabla de simbolos
 * @author Grupo 1
 */
public class EntradaTS {
	
	/** Lexema del identificador */
	private String lexema;
	/** Tipo del identificador */
	private ExpresionTipo tipo;
	/** Si es constante o no */
	private boolean constante;
	/** Parametros del identificador si es una funcion, si no, un array vacio */
	private Vector<Parametro> params;
	/** Tipo de retorno */
	private TipoParam retorno;

	public EntradaTS(String lexema) {
		this(lexema, null, new Vector<Parametro>(), null, false);
	}
	
	public EntradaTS(String lexema, ExpresionTipo tipo, Vector<Parametro> params, TipoParam retorno, boolean constante) {
		this.lexema = lexema;
		this.tipo = tipo;
		this.params = params;
		this.retorno = retorno;
		this.constante = constante;
	}
	
	public boolean isConstante() {
		return constante;
	}

	public void setConstante(boolean constante) {
		this.constante = constante;
	}

	public ExpresionTipo getTipo() {
		return tipo;
	}

	public void setTipo(ExpresionTipo tipo) {
		this.tipo = tipo;
	}

	public Object getRetorno() {
		return retorno;
	}

	public void setRetorno(TipoParam retorno) {
		this.retorno = retorno;
	}

	public String getLexema() {
		return lexema;
	}

	public Vector<Parametro> getParams() {
		return params;
	}

	public void addParam(Parametro param) {
		params.add(param);
	}
	
	public int getNumArgs() {
		return params.size();
	}
	
}
