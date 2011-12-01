/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compilador.lexico.Tokens;

/**
 *
 * @author GRUPO 3: Gonzalo Ortiz Jaureguizar, Alicia Perez Jimenez, Laura Reyero Sainz, Hector Sanjuan Redondo, Ruben Tarancon Garijo
 */
public class LitCha extends Token{
    public LitCha(int n){
        super(n);
    }
    public LitCha(String l, int n){
    	super(l,n);
    }

    public String toString(){
        return "Character "+lex;
    }
}
