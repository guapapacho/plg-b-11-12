package compilador.gestor;




import java.util.ArrayList;


public class GestorErrores {
	/**
	 * lista de los distintos errores existentes
	 */
	private ArrayList<String> lista;
	public void listaerrores(){
	lista = new ArrayList<String>();
	lista.add("elemento del léxico ya insertado");
	lista.add("caracter no valido");
	lista.add("error de entrada salida");
	lista.add("Imposible emparejar el terminal con el token");
	lista.add("token de entrada invalido");
	lista.add("pila vacia no esperada");
}
	
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
    public int muestraListaErrores(){
    	if (cuenta==0){
    	System.out.println("No hubo errores\n");
    	return 0;
    		   	}
    	else {
    		System.out.println("Hubo: "+cuenta+" errores\n" );
    		for (int i=0; i<cuenta+1; i++){
    			System.out.println("Error: "+lista.get(errors.get(i).error)+" En la linea"+errors.get(i).getLinea()+" Y la columna"+errors.get(i).getColumna());
    		}
    		return cuenta;
    		}
    		 
    		
    	  }
    }
