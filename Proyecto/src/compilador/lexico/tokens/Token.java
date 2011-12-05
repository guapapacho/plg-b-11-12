package compilador.lexico.tokens;

public class Token {
    
    public enum Tipo {
        EOF, LIT_CARACTER, LIT_CADENA, PAL_RESERVADA, 
        IDENTIFICADOR, NUM_REAL, NUM_ENTERO, SEPARADOR, 
        OP_ARITMETICO, OP_LOGICO, OP_COMPARACION, OP_ASIGNACION
    };
    
    private Tipo tipo;
    private Object argumento;
}
