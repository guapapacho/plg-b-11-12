/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compilador.lexico.Tokens;

/**
 *
 * @author GRUPO 3: Gonzalo Ortiz Jaureguizar, Alicia Perez Jimenez, Laura Reyero Sainz, Hector Sanjuan Redondo, Ruben Tarancon Garijo
 */
public class Token {
    int numLinea;
    String lex;
    public Token(String l,int n){
    	lex=l;
        numLinea = n;
    }
    
    public Token(int n){
    	lex="";
    	numLinea=n;
    }
    public Token(){
    	lex="";
    }
    
    public String getLex(){
    	return lex;
    }
    public int getLinea(){
    	return numLinea;
    }
    public void cambiaLinea(int n){
        numLinea=n;
    }
}
