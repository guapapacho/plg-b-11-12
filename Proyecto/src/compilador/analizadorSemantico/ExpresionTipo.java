package compilador.analizadorSemantico;

public class ExpresionTipo {
	private boolean basico;
	private TipoBasico tipoBasico;
	private TipoNoBasico tipoNoBasico;
	
	public enum TipoBasico{logico, caracter, entero, real, error_tipo, vacio}; 
	public enum TipoNoBasico{vector, producto, registro, union, puntero, funcion}
	
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
		if (s.equals("String")) return new Vector(0,new ExpresionTipo(TipoBasico.caracter)); //longitud 0??
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
	
}
