package compilador.analizadorSemantico;

import compilador.analizadorLexico.Token.OpAritmetico;
import compilador.analizadorLexico.Token.OpAsignacion;
import compilador.analizadorLexico.Token.OpComparacion;
import compilador.analizadorLexico.Token.OpLogico;

public class ExpresionTipo {
	/** Solo para parametros de funciones: indica si se pasa por referencia, por defecto, falso **/
	protected boolean pasoReferencia = false;
	private boolean basico;
	private TipoBasico tipoBasico;
	private TipoNoBasico tipoNoBasico;
	private static ExpresionTipo instanceVacio;
	private static ExpresionTipo instanceError;
	
	public enum TipoBasico{logico, caracter, entero, real, error_tipo, vacio}; 
	public enum TipoNoBasico{enumerado, vector, producto, registro, union, puntero, cabecera, funcion, objeto, cadena}

	/*
	switch(e1.getTipoBasico()){
	case logico:
		switch (e2.getTipoBasico()){
		case logico: return e1;
		case caracter:
		case entero: 
		case real:
		default: return new ExpresionTipo(TipoBasico.error_tipo);
		}
	case caracter:
		switch (e2.getTipoBasico()){
		case logico: 
		case caracter: return e1;
		case entero: 
		case real:
		default: return new ExpresionTipo(TipoBasico.error_tipo);
		}
	case entero:
		switch (e2.getTipoBasico()){
		case logico: 
		case caracter:
		case entero: return e1;
		case real:
		default: return new ExpresionTipo(TipoBasico.error_tipo);
		}
	case real:
		switch (e2.getTipoBasico()){
		case logico: 
		case caracter:
		case entero: 
		case real: return e1;
		default: return new ExpresionTipo(TipoBasico.error_tipo);
		}
	default: return new ExpresionTipo(TipoBasico.error_tipo);
	}
	*/
	
	/**-------------------------------------------------------------------------------------------------------------
	* SOBRERO("~"), CIRCUNFLEJO("ˆ"), DOS_MENORES("<<"), DOS_MAYORES(">>"),			
	* AND("&&"), ANDEQ(""), BIT_AND("&"), BIT_OR("|"), COMPL("~"), NOT("!"), NOT_EQ(""), OR("||"), OR_EQ(""), XOR("^"), XOR_EQ("");			
	* -------------------------------------------------------------------------------------------------------------**/	
	public static ExpresionTipo sonEquivLog(ExpresionTipo e1, ExpresionTipo e2, OpLogico op){
		if(e1!=null && e2!=null && e1.esTipoBasico() && e2.esTipoBasico()){
			switch(op){
			case CIRCUNFLEJO: case XOR: case DOS_MENORES: case DOS_MAYORES: case BIT_AND: case BIT_OR:
				switch(e1.getTipoBasico()){
				case logico: case caracter: case entero:
					switch (e2.getTipoBasico()){
					case logico: case caracter: case entero: return new ExpresionTipo(TipoBasico.entero);
					default: return null;
					}
				default: return null;
				}
			 case AND: case OR: 
				switch(e1.getTipoBasico()){
				case logico: 
					switch (e2.getTipoBasico()){
					case logico: return e1;
					case caracter: case entero: case real: return new ExpresionTipo(TipoBasico.entero); 
					default: return null;
					}
				case caracter: case entero: case real:
					switch (e2.getTipoBasico()){
					case logico: case caracter: case entero: case real: return new ExpresionTipo(TipoBasico.entero);
					default: return null;
					}
				default: return null;
				}
			 case NOT:  // Con un operador unario, solo se tiene en cuenta el primer operando (ExpresionTipo)
				 switch(e1.getTipoBasico()){
				 case logico: return e1;
				 case caracter: case entero: case real: return new ExpresionTipo(TipoBasico.entero);
				 default: return null;	
				 }
			 default: return null;
			}
		}	
		else
			return null;		
	}
	
	/**-------------------------------------------------------------------------------------------------------------
	* IGUALDAD("=="), DISTINTO("!="), MENOR("<"), MAYOR(">"), MENOR_IGUAL("<="), MAYOR_IGUAL(">=");
	* -------------------------------------------------------------------------------------------------------------**/	
	public static ExpresionTipo sonEquivComp(ExpresionTipo e1, ExpresionTipo e2, OpComparacion op){
		if(e1!=null && e2!=null && e1.esTipoBasico() && e2.esTipoBasico()){
			switch(op){
			case IGUALDAD: case DISTINTO: case MENOR: case MAYOR: case MENOR_IGUAL: case MAYOR_IGUAL:
				switch(e1.getTipoBasico()){
				case logico: case caracter: case entero: case real:
					switch (e2.getTipoBasico()){
					case logico: case caracter: case entero: case real: return new ExpresionTipo(TipoBasico.logico);
					default: return null;
					}
				default: return null;
				}
			default: return null;
			}
		}
		else
			return null;
	}
			
	/**-------------------------------------------------------------------------------------------------------------
	* SUMA("+"), RESTA("-"), INCREMENTO("++"), DECREMENTO("--"), MULTIPLICACION("*"), DIVISION("/"), PORCENTAJE("%");
	* -------------------------------------------------------------------------------------------------------------**/	
	public static ExpresionTipo sonEquivArit(ExpresionTipo e1, ExpresionTipo e2, OpAritmetico op){
		if(e1!=null && e2!=null && e1.esTipoBasico() && e2.esTipoBasico()){
			switch(op){
			case SUMA: case RESTA: case MULTIPLICACION: case DIVISION:
				switch(e1.getTipoBasico()){
				case logico: case caracter: case entero:
					switch (e2.getTipoBasico()){
					case logico: case caracter: case entero: return new ExpresionTipo(TipoBasico.entero); 
					case real: return new ExpresionTipo(TipoBasico.real);
					default : return null;
					}
				case real:
					switch (e2.getTipoBasico()){
					case logico: case caracter: case entero: case real: return new ExpresionTipo(TipoBasico.real); 
					default : return null;
					}
				default : return null;
				}
			case PORCENTAJE:
				switch(e1.getTipoBasico()){
				case logico: case caracter: case entero:
					switch (e2.getTipoBasico()){
					case logico: case caracter: case entero: return new ExpresionTipo(TipoBasico.entero); 
					default : return null;
					}
				default : return null;
				}
			// Con los operadores unarios, solo se tiene en cuenta el primer operando (ExpresionTipo)
			case INCREMENTO: 
				switch(e1.getTipoBasico()){
				case logico: case caracter: return new ExpresionTipo(TipoBasico.entero);
				case entero: case real: return e1;
				default : return null;
				}
			case DECREMENTO:
				switch(e1.getTipoBasico()){
				case caracter: return new ExpresionTipo(TipoBasico.entero);
				case entero: case real: return e1;
				default : return null;
				}
			default : return null;
			}
		}	
		else
			return null;
	}
	
	/**
	* -------------------------------------------------------------------------------------------------------------
	* ASIGNACION("="), MAS_IGUAL("+="), MENOS_IGUAL("-="), POR_IGUAL("*="), DIV_IGUAL("/="), PORCENTAJE_IGUAL("%="),
	* CIRCUNFLEJO_IGUAL("^="), AND_IGUAL("&="), OR_IGUAL("|="), MAYOR_MAYOR_IGUAL(">>="), MENOR_MENOR_IGUAL("<<="),
	* PUNTERO("*");
	* -------------------------------------------------------------------------------------------------------------
	* En las asignaciones, independientemente del tipo de la derecha, 
	* siempre se retorna el tipo correspondiente a la variable asignada
	**/
	public static ExpresionTipo sonEquivAsig(ExpresionTipo e1, ExpresionTipo e2, OpAsignacion op){
		if(e1!=null && e2!=null && e1.esTipoBasico() && e2.esTipoBasico()){
			switch(op){
			case ASIGNACION: case MAS_IGUAL: case MENOS_IGUAL: case POR_IGUAL: case DIV_IGUAL: 
				switch(e1.getTipoBasico()){
				case logico: case caracter: case entero: case real:
					switch (e2.getTipoBasico()){
					case logico: case caracter: case entero: case real: return e1;  
					default : return null;
					}
				default : return null;
				}
			case PORCENTAJE_IGUAL: case CIRCUNFLEJO_IGUAL: case AND_IGUAL: case OR_IGUAL: 
			case MAYOR_MAYOR_IGUAL: case MENOR_MENOR_IGUAL: 
			// En estos casos el tipo real no es admitido ni a derecha ni a izquierda del operador...
				switch(e1.getTipoBasico()){
				case logico: case caracter: case entero:
					switch (e2.getTipoBasico()){
					case logico: case caracter: case entero: return e1;  
					default : return null;
					}
				default : return null;
				}
			default : return null;
			}
		}
		else
			return null;
	}
	
	public ExpresionTipo(TipoNoBasico tipo){
		this.basico = false;
		this.tipoNoBasico = tipo;
		this.tipoBasico = null;
	}
	
	public ExpresionTipo(TipoBasico tipo){
		this.basico = true;
		this.tipoBasico = tipo;
		this.tipoNoBasico = null;
	}
	
	public static ExpresionTipo getError() {
		if(instanceError == null)
			instanceError = new ExpresionTipo(TipoBasico.error_tipo);
		return instanceError;
	}
	
	public static ExpresionTipo getVacio() {
		if(instanceVacio == null)
			instanceVacio = new ExpresionTipo(TipoBasico.vacio);
		return instanceVacio;
	}
	
	public TipoBasico getTipoBasico() {
		return tipoBasico;
	}
	
	public TipoNoBasico getTipoNoBasico() {
		return tipoNoBasico;
	}
	
	public Object getTipo(){
		if(basico)
			return tipoBasico;
		else
			return tipoNoBasico;
	}
	
	public boolean esTipoBasico() {
		return basico;
	}
	
	public String tipoBasicoString(){
		switch (tipoBasico){
		case logico: return "logico";
		case caracter: return "caracter";
		case entero: return "entero";
		case real: return "real";
		case error_tipo: return "error_tipo"; 
		case vacio: return "vacio";
		default: return "ninguno?";
		}
	}
	
	public static ExpresionTipo expresionTipoDeString(String s){
	
		System.out.println("El tipo es: "+ s);
		if (s.equals("bool")) return new ExpresionTipo(TipoBasico.logico);
		if (s.equals("char")) return new ExpresionTipo(TipoBasico.caracter);
		if (s.equals("char_16t")) return new ExpresionTipo(TipoBasico.caracter);
		if (s.equals("char_32t")) return new ExpresionTipo(TipoBasico.caracter);
		if (s.equals("short")) return new ExpresionTipo(TipoBasico.entero); //?? 
		if (s.equals("long")) return new ExpresionTipo(TipoBasico.entero); //?? 
		if (s.equals("int")) return new ExpresionTipo(TipoBasico.entero);
		if (s.equals("double")) return new ExpresionTipo(TipoBasico.real);
		if (s.equals("float")) return new ExpresionTipo(TipoBasico.real);
		if (s.equals("String")) return new Cadena(); //longitud 0??
		
		return null;
		
	}
	
	public boolean equals(ExpresionTipo e) {
		if(this.esTipoBasico() != e.esTipoBasico())
			return false;
		if(this.esTipoBasico()) {
			return this.getTipoBasico().equals(e.getTipoBasico());
		} else {
			return this.getTipoNoBasico().equals(e.getTipoNoBasico());
		}
	}
	
	public boolean equals(TipoBasico t) {
		if(!this.esTipoBasico())
			return false;
		else
			return this.getTipoBasico().equals(t);
	}
	
	public boolean equals(TipoNoBasico t) {
		if(this.esTipoBasico())
			return false;
		else
			return this.getTipoNoBasico().equals(t);
	}

	/**
	 * Comprueba si los dos tipos Producto representan parametros de la misma funcion
	 * @param dominio
	 * @return boolean
	 */
	public boolean paramsEquivalentes(Producto dominio) {
		if(!this.equals(TipoNoBasico.producto))
			return false;
		//TODO terminar este método. habría que ver si los parametros tienen el mismo tipo y si en c se permite pasar menos params
		return true;
	}
	

	public boolean isPasoReferencia() {
		return pasoReferencia;
	}

	public void setPasoReferencia(boolean pasoReferencia) {
		this.pasoReferencia = pasoReferencia;
	}
	
	public String toString() {
		switch (tipoBasico){
		case logico: return "Logico";
		case caracter: return "Caracter";
		case entero: return "Entero";
		case real: return "Real";
		case error_tipo: return "Error"; 
		case vacio: return "Vacio";
		default: return "ninguno?";
		}
	}
	
}
