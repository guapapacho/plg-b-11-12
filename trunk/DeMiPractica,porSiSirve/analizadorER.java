
package compilador.lexico.Tokens;

import java.util.regex.Pattern;

/**
 *
 * Esta clase simula un analizador leximo mediante expresiones regulares (ER).
 * Debido al peor rendimiento, no se usa en el compilador, pero sirve para
 * comprobar empiricamente la corrección de las ER mediante las cuales se hace
 * el analizador lexico con automata, que si es el usado en el compilador
 * @author GRUPO 3: Gonzalo Ortiz Jaureguizar, Alicia Perez Jimenez, Laura Reyero Sainz, Hector Sanjuan Redondo, Ruben Tarancon Garijo
 */
public class analizadorER {

    public boolean buscarInt(String str) {
        return Pattern.compile("(-?[1-9][0-9]*|0)e?").matcher(str).matches();
    }

    public boolean buscarNat(String str) {
        return Pattern.compile("([1-9][0-9]*|0)n?").matcher(str).matches();
    }

    public boolean buscarFloat(String str) {
        return Pattern.compile("-?([1-9][0-9]*|0)(" + //parte natural
                "\\.0|" + //.0
                "\\.([0-9]*[1-9])|" + //o .natural
                "\\.([0-9]*[1-9])(e|E)-?[1-9][0-9]*|0|" + //o .naturalEnatural
                "(e|E)-?[1-9][0-9]*|0" + //o Enatural
                ")r?")
                .matcher(str).matches();
    }

    public boolean buscarBool(String str) {
        return Pattern.compile("true|false").matcher(str).matches();
    }

    public boolean buscarChar(String str) {
        return Pattern.compile("'[a-zA-Z0-9]'").matcher(str).matches();
    }

    public boolean buscarComentario(String str) {
        return Pattern.compile("#.*\\n").matcher(str).matches();
    }
}
