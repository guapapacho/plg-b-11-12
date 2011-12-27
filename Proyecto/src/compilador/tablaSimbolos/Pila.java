/**
 * 
 */
package compilador.tablaSimbolos;

import java.util.Vector;

/**
 * @author Grupo 1
 *
 */
@SuppressWarnings("hiding")
public class Pila <Object>{
	/**
	 * @param args
	 */
	private int     size;
    private Vector<Object>    elementos;

	/**
	 * Constructora de la clase
	 */
    public Pila() {
        super();
        elementos = new Vector<Object>();
        size = 0;
    }
    
    
    /**
	 * Metodo que devuelve true si la pila esta vacía y false si no.
	 */
    public boolean pilaVacia () {
        if (size==0) {
            return true;
        }
        return false;
    }

    
    /**
	 * Metodo añade elementos a la pila.
	 */
    public void apilar ( Object o ) {
        elementos.add(size, o);
        size++;
    }

    
    /**
	 * Metodo quita elementos de la pila.
	 */
    public Object desapilar () {

        try {
            if(pilaVacia())
                throw new ErrorPilaVacia();
            else {
                return elementos.get(--size);
            }
        } catch(ErrorPilaVacia error) {
            System.out.println("ERROR: la pila esta vacía");
            return null;
        }
    }

    
    /**
	 * Metodo que devuelve el tamaño de la pila.
	 */
    public int getSize() {
        return size;
    }
    
}

@SuppressWarnings("serial")
class ErrorPilaVacia extends Exception {
    public ErrorPilaVacia() {
        super();
    }

}
