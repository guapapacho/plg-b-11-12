package compilador.analizadorSemantico;

public class Puntero extends ExpresionTipo {
	private ExpresionTipo tipo; 

	public Puntero(ExpresionTipo tipo) {
		super(TipoNoBasico.puntero);
		this.tipo = tipo;
	}

	public boolean equals(Puntero p){
		return this.tipo.equals(p.getTipo());
	}
	
	public ExpresionTipo getTipo() {
		return tipo;
	}
	
	public String toStringPascal(){
		return "^"+tipo.toStringPascal();
	}
	
	public String toString() {
		return "Puntero a " + tipo;
	}

}
