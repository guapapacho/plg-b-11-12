package compilador.analizadorSemantico;

import compilador.analizadorLexico.Token.OpLogico;

public class ExpresionTipo {
	private boolean basico;
	private TipoBasico tipoBasico;
	private TipoNoBasico tipoNoBasico;
	
	public enum TipoBasico{logico, caracter, entero, real, error_tipo, vacio}; 
	public enum TipoNoBasico{vector, producto, registro, union, puntero, funcion, objeto, cadena}
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
	
	public static ExpresionTipo sonEquivLog(ExpresionTipo e1, ExpresionTipo e2, OpLogico op){
		switch(op){
		case CIRCUNFLEJO: case XOR: case XOR_EQ: case DOS_MENORES: case DOS_MAYORES: case BIT_AND: case ANDEQ: case BIT_OR: case OR_EQ:
			switch(e1.getTipoBasico()){
			case logico: case caracter: case entero:
				switch (e2.getTipoBasico()){
				case logico: case caracter: case entero: return new ExpresionTipo(TipoBasico.entero);
				default: return new ExpresionTipo(TipoBasico.error_tipo);
				}
			default: return new ExpresionTipo(TipoBasico.error_tipo);
			}
		 case AND: case OR: 
			switch(e1.getTipoBasico()){
			case logico: 
				switch (e2.getTipoBasico()){
				case logico: return e1;
				case caracter: case entero: case real: return new ExpresionTipo(TipoBasico.entero); 
				default: return new ExpresionTipo(TipoBasico.error_tipo);
				}
			case caracter: case entero: case real:
				switch (e2.getTipoBasico()){
				case logico: case caracter: case entero: case real: return new ExpresionTipo(TipoBasico.entero);
				default: return new ExpresionTipo(TipoBasico.error_tipo);
				}
			default: return new ExpresionTipo(TipoBasico.error_tipo);
			}
		default: return new ExpresionTipo(TipoBasico.error_tipo);
		}			
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
	
	public TipoBasico getTipoBasico() {
		return tipoBasico;
	}
	
	public TipoNoBasico getTipoNoBasico() {
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
		if(this.equals(TipoNoBasico.producto))
			return false;
		//TODO terminar este método. habría que ver si los parametros tienen el mismo tipo y si en c se permite pasar menos params
		return true;
	}
	
}
