package compilador.gestionErrores;

import java.util.ArrayList;
import java.util.Iterator;


public class GestorErrores {
	/**
	 * lista de los distintos errores existentes
	 */
	private ArrayList<String> lista ;
	/*
	 * triplete sencillo con el que insertamos errores
	 */
		public class TError{
			private int error;

			private int linea;

			private int columna;
			
		public int getError() {
				return error;
			}

			public void setError(int error) {
				this.error = error;
			}

			public int getLinea() {
				return linea;
			}

			public void setLinea(int linea) {
				this.linea = linea;
			}

			public int getColumna() {
				return columna;
			}

			public void setColumna(int columna) {
				this.columna = columna;
			}


			public TError(int error, int linea, int columna){
			this.error = error;
			this.linea = linea;
			this.columna = columna;
				
			}
			}
 /*
  * Array que contiene la lista de errores
  */
    private ArrayList<TError> errors;
    private int cuenta;
    public GestorErrores() {
    	lista = new ArrayList<String>();
    	lista.add("elemento del léxico ya insertado");
    	lista.add("error de entrada salida");
    	lista.add("caracter no valido");
    	lista.add("Imposible emparejar el terminal con el token");
    	lista.add("token de entrada invalido");
    	lista.add("pila vacia no esperada");
        errors = new ArrayList<TError>();
        cuenta=0;
    }

    public void insertaError(int er, int l, int n) {
        errors.add(new TError(er, l, n));
        cuenta=cuenta++;
    }

    public void setErrores(ArrayList<TError> copia) {
        errors = copia;
        cuenta=copia.size();
    }
    public ArrayList<TError> devuelveErrores(){
    	return errors;
    }
    
    /*
     * Imprime los errores que han ocurrido y devuelve el numero que hubo
     */
    public String muestraListaErrores(){
    	String salida;
    	salida= " ";
    	if (cuenta==0){
    	salida=salida+"No hubo errores\n";
    	return salida;
    		   	}
    	else {
    		salida=salida+"Hubo: "+cuenta+" errores\n";
    		Iterator<TError> iterator = errors.iterator();
    		while (iterator.hasNext()){
    			TError err2;
    			err2=iterator.next();
    			salida=salida+lista.get(err2.getError())+" en: \n la linea "+err2.getLinea()+" y la columna "+err2.getColumna()+" \n";
    			
    		}
    		return salida;
    		}
    		 
    		
    	  }
    }

