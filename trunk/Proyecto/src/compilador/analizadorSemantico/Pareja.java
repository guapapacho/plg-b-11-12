package compilador.analizadorSemantico;

public class Pareja<E,F> {

	E prim;
	F seg;
	
	public Pareja(E prim, F seg){
		this.prim = prim;
		this.seg = seg;
	}

	public E getPrim() {
		return prim;
	}

	public void setPrim(E prim) {
		this.prim = prim;
	}

	public F getSeg() {
		return seg;
	}

	public void setSeg(F seg) {
		this.seg = seg;
	}
	
	
}
