package compilador.lexico;




import java.util.ArrayList;



public class GestorErrores {
	
	public enum Errores{

		lexelem("elemento del léxico ya insertado"), invalidchar("caracter no valido"),
		io("error de entrada salida"), pareja("Imposible emparejar el terminal con el token"),
		token("token de entrada invalido"), pila("pila vacia no esperada");

		private String descr;

		Errores(String descr){
		this.descr = descr;}

		public String getDescr(){
		return this.descr;}
		 
		}

		public class TError{
			private Errores error;

			private int linea;

			private int columna;
			
		public Errores getError() {
				return error;
			}

			public void setError(Errores error) {
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


			public TError(Errores error, int linea, int columna){
			this.error = error;
			this.linea = linea;
			this.columna = columna;
				
			}
			}

    private ArrayList<TError> errors;
    private int cuenta;
    public GestorErrores() {
        errors = new ArrayList<TError>();
        cuenta=0;
    }

    public void insertaError(TError error) {
        errors.add(error);
        cuenta=cuenta++;
    }

    public void setErrores(ArrayList<TError> copia) {
        errors = copia;
        cuenta=copia.size();
    }
    public int muestraListaErrores(){
    	if (cuenta==0){
    	System.out.println("No hubo errores\n");
    	return 0;
    		   	}
    	else {
    		System.out.println("Hubo: "+cuenta+" errores\n" );
    		for (int i=0; i<cuenta+1; i++){
    			System.out.println("Error: "+errors.get(i).getError().getDescr()+" En la linea"+errors.get(i).getLinea()+" Y la columna"+errors.get(i).getColumna());
    		}
    		return cuenta;
    		}
    		 
    		
    	  }
    }
