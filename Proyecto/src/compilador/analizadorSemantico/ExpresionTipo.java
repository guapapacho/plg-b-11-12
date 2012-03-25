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
		switch (s){
		case "bool": return new ExpresionTipo(TipoBasico.logico);
		case "char": return new ExpresionTipo(TipoBasico.caracter);
		case "char_16t": return new ExpresionTipo(TipoBasico.caracter);
		case "char_32t": return new ExpresionTipo(TipoBasico.caracter);
		case "short": return new ExpresionTipo(TipoBasico.entero); //?? 
		case "long": return new ExpresionTipo(TipoBasico.entero); //?? 
		case "int": return new ExpresionTipo(TipoBasico.entero);
		case "double": return new ExpresionTipo(TipoBasico.real);
		case "float": return new ExpresionTipo(TipoBasico.real);
		case "String": return new Vector(0,new ExpresionTipo(TipoBasico.caracter)); //longitud 0??
		default: return null;
		}
	}
	
}
