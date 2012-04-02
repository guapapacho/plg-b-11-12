/**
 * 
 */
package compilador.gestionTablasSimbolos;

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
	 * Metodo devuelve el elemento de la cima de la pila sin eliminarlo.
	 */
    public Object cima() {

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
	 * Metodo quita elementos de la pila.
	 */
    public void desapilar(){
    	try {
            if(pilaVacia())
                throw new ErrorPilaVacia();
            else {
            	elementos.remove(cima());
            	size--;
            }
        } catch(ErrorPilaVacia error) {
            System.out.println("ERROR: la pila esta vacía");
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
