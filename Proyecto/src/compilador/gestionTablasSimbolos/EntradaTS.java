package compilador.gestionTablasSimbolos;

import compilador.analizadorSemantico.ExpresionTipo;

/**
 * Clase que guarda una entrada de la tabla de simbolos
 * @author Grupo 1
 */
public class EntradaTS {
	
	/** Lexema del identificador */
	private String lexema;
	/** Lexema del identificador para la traducción */
	private String lexemaTrad;
	/** Tipo del identificador */
	private ExpresionTipo tipo;
	/** Si es constante o no */
	private boolean constante;
	/** si es un parametro de función/procedimiento o no **/
	private boolean parametro;

	public EntradaTS(String lexema) {
		this(lexema, null, false, false);
	}
	
	public EntradaTS(String lexema, ExpresionTipo tipo, boolean constante, boolean parametro) {
		this.lexema = lexema;
		this.lexemaTrad = lexema;
		this.tipo = tipo;
		this.constante = constante;
		this.parametro = parametro;
	}
	
	public boolean isConstante() {
		return constante;
	}
	
	public void setParametro(boolean p){
		this.parametro = p;
	}

	public boolean esParametro(){
		return parametro;
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

	public String getLexema() {
		return lexema;
	}
	
	public String getLexemaTrad() {
		return lexemaTrad;
	}
	
	public void setLexemaTrad(String lexema) {
		lexemaTrad = lexema;
	}
	
}
