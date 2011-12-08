package main;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import compilador.lexico.AnalizadorLexico;
import compilador.lexico.tokens.Token;
import compilador.lexico.tokens.Token.TipoToken;


public class Main {
	
	public static void main(String[] args) {
		InputStream in;
		
		try {
			in = new FileInputStream("Proyecto/src/main/entrada.txt");
			
			AnalizadorLexico analizador = new AnalizadorLexico(in);
			Token token = analizador.scanner();
			while(token.getTipo() != TipoToken.EOF) {
				System.out.println("TOKEN: "+token.getTipo()+"\t ATRIBUTO: "+token.getAtributo());
				token = analizador.scanner();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
