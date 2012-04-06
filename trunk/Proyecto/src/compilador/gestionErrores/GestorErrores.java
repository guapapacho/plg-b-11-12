package compilador.gestionErrores;

import java.util.ArrayList;


public class GestorErrores {
	
	/** Lista de los distintos errores existentes */
	private ArrayList<String> lista;
	/** Instancia unica del gestor de errores */
	private static GestorErrores instance;
	/** Array que contiene la lista de errores */
    private ArrayList<TError> errors;
    /** Array que contiene la lista de warnings */
    private ArrayList<TWarning> warnings;
	
	public enum TipoError {
		LEXICO("léxico"), SINTACTICO("sintáctico"), SEMANTICO("semántico");
		
		private String description;
		private TipoError(String desc) { description = desc; };
		public String toString() { return description; };
	}
	
		/**
		 * Triplete sencillo con el que insertamos errores
		 */
		public class TError {
			private TipoError tipo;
			private int linea;
			private int columna;
			private String mensaje;
		
			public int getLinea() {
				return linea;
			}
		
			public int getColumna() {
				return columna;
			}
		
			public String toString() {
				return "Error "+tipo+" en L: "+linea+" C: "+columna+" - "+mensaje;
			}
		
			public TError(TipoError tipo, String string, int linea, int columna){
				this.tipo = tipo;
				this.mensaje = string;
				this.linea = linea;
				this.columna = columna;
			}
		}
		
		/**
		 * Triplete sencillo con el que insertamos warnings
		 */
		public class TWarning {
			private int linea;
			private int columna;
			private String mensaje;
		
			public int getLinea() {
				return linea;
			}
		
			public int getColumna() {
				return columna;
			}
		
			public String toString() {
				return "Error en L: "+linea+" C: "+columna+" - "+mensaje;
			}
		
			public TWarning(String string, int linea, int columna){
				this.mensaje = string;
				this.linea = linea;
				this.columna = columna;
			}
		}

    
    public static GestorErrores getGestorErrores() {
    	if(instance == null)
    		instance = new GestorErrores();
    	return instance;
    }
    
    private GestorErrores() {
    	lista = new ArrayList<String>();
    	lista.add("elemento del léxico ya insertado");
    	lista.add("error de entrada salida");
    	lista.add("caracter no valido para formar token");
    	lista.add("Imposible emparejar el terminal con el token");
    	lista.add("token de entrada invalido");
    	lista.add("pila vacia no esperada");
        errors = new ArrayList<TError>();
    }

    public void insertaErrorLexico(int er, int l, int n) {
        errors.add(new TError(TipoError.LEXICO, lista.get(er), l, n));
    }

    public void insertaErrorSintactico(int er, int l, int n) throws Exception {
        errors.add(new TError(TipoError.SINTACTICO, lista.get(er), l, n));
        throw new Exception("Error sintáctico!");
    }

    public void insertaErrorSintactico(int l, int n,String mensaje) throws Exception {
        errors.add(new TError(TipoError.SINTACTICO, mensaje, l, n));
        throw new Exception("Error sintáctico!");
    }
    
    public void insertaErrorSemantico(int er, int l, int n) {
        errors.add(new TError(TipoError.SEMANTICO, lista.get(er), l, n));
    }

    public void insertaErrorSemantico(int l, int n,String mensaje) {
        errors.add(new TError(TipoError.SEMANTICO, mensaje, l, n));
    }
    
    public void insertaWarning(int l, int n, String mensaje) {
        warnings.add(new TWarning(mensaje, l, n));
    }
    
    public ArrayList<TError> devuelveErrores(){
    	return errors;
    }
    
    public void resetErrores(){
    	errors.clear();
    }
    
    /*
     * Imprime los errores que han ocurrido y devuelve el numero que hubo
     */
    public String muestraListaErrores(){
    	String salida;
    	salida= "\n";
    	if (errors.size()==0){
	    	salida=salida+"No hubo errores\n";
	    	return salida;
    	}
    	else {
    		salida = salida + errors.get(0);
    		return salida;
    	}
    }

}

