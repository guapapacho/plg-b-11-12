package compilador.gestionErrores;

import java.util.ArrayList;
import java.util.Iterator;


public class GestorErrores {
	
	/** Lista de los distintos errores existentes */
	private ArrayList<String> lista;
	/** Instancia unica del gestor de errores */
	private static GestorErrores instance;
	/** Array que contiene la lista de errores */
    private ArrayList<TError> errors;
	
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
			//	return "Error "+tipo+": "+lista.get(error)+
			//			" en: \n la linea "+linea+" y la columna "+columna;
			}
		
			public TError(TipoError tipo, String string, int linea, int columna){
				this.tipo = tipo;
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
    
    public ArrayList<TError> devuelveErrores(){
    	return errors;
    }
    
    public void resetErrores(){
    	errors.clear();;
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
    		/*
    		salida=salida+"Hubo: "+errors.size()+" errores\n";
    		Iterator<TError> iterator = errors.iterator();
    		while (iterator.hasNext()){
    			TError err2;
    			err2=iterator.next();
    			salida=salida+err2+" \n";
    			
    		}*/
    		return salida;
    	}
    }

}

