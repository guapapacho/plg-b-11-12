package compilador.interfaz;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import compilador.lexico.AnalizadorLexico;
import compilador.lexico.tokens.Token;
import compilador.lexico.tokens.Token.TipoToken;
import java.awt.Button;

/**
 * @author Pilar
 *
 */
@SuppressWarnings("serial")
public class Compilador extends JFrame {

	/**
	 * @param args
	 */
	
	private JPanel panelPrincipal=null;
	private JPanel panelPrincipal_1;
	private JMenuBar barraMenu=null;
	private JMenu ficheroMenu=null;
	private JMenuItem abrir=null;
	private JMenuItem guardar=null;
	private JMenuItem def=null;
	private JTextArea ta1=null;
	private JTextArea ta2=null;
	private JScrollPane sp1 =null;
	private JScrollPane sp2 =null;
	private Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	private JLabel l0=null;
	private JLabel l1=null;
	private JLabel l2=null;
	private JButton botonTokens=null;
	private File archivo=null;
	private FileReader fr=null;
	private BufferedReader br=null;
	private InputStream in=null;
	
	public Compilador(){
		super();
		crearInterfaz();
	}
	
	public void crearInterfaz() {
		this.setSize(930, 630);
		//this.setSize(screenSize);
		this.setLocation(150, 100);
		this.setJMenuBar(getBarraMenu());
		this.setContentPane(getPanelPrincipal());
		this.setTitle("Traductor C++ - Pascal");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);	
	}

	public JMenuBar getBarraMenu(){
		if (barraMenu==null){
			barraMenu=new JMenuBar();
			barraMenu.add(getFicherosMenu());
		} 
		return barraMenu;
	}
	
	public JMenu getFicherosMenu() {
		if (ficheroMenu==null){
			ficheroMenu=new JMenu();
			ficheroMenu.setText("Ficheros"); 
			ficheroMenu.add(getLeerItem());
			ficheroMenu.add(getGuardarItem());
			ficheroMenu.add(getDefaultItem());
		}
		return ficheroMenu;
	}

	private JMenuItem getDefaultItem() {
		if (def==null){
			def = new JMenuItem();
			def.setText("Default");
			def.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							Scanner sc=null;
							try{
								sc=new Scanner (new File("Proyecto/src/main/entrada.txt"));
								while(sc.hasNextLine()){
									String linea=sc.nextLine();
									ta1.append(linea);
						        	ta1.append(System.getProperty("line.separator"));	
								}
							}catch(Exception e1){
								e1.printStackTrace();
							}
						}
					});
		}
		return def;
	}

	private JMenuItem getGuardarItem() {
		if (guardar==null){
			guardar = new JMenuItem();
			guardar.setText("Guardar");
			guardar.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							JFileChooser seleccion = new JFileChooser();
							int num=seleccion.showSaveDialog(Compilador.this);
							try {
								if (num == seleccion.APPROVE_OPTION){
									String fichero = seleccion.getSelectedFile().getName();
									PrintWriter fichSal= new PrintWriter(new FileOutputStream(fichero));
									String contenido=new String(ta1.getText());
									StringTokenizer st=new StringTokenizer(contenido,"\n");
									ta1.setText("");
									while(st.hasMoreTokens()){
										String linea=st.nextToken();
										fichSal.print(linea+"\n");
									}
									fichSal.close();
								}else{
									seleccion.cancelSelection();
								}
							
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							}
						}				
					});
		}
		return guardar;
	}

	private JMenuItem getLeerItem() {
		
		if (abrir==null){
			abrir = new JMenuItem();
			abrir.setText("Abrir");
			abrir.addActionListener(
					new ActionListener(){
						@SuppressWarnings("static-access")
						public void actionPerformed(ActionEvent e){
							JFileChooser seleccion = new JFileChooser();
							int num = seleccion.showOpenDialog(Compilador.this);
							try{
								if (num == seleccion.APPROVE_OPTION){
								   archivo = seleccion.getSelectedFile();
								   fr = new FileReader (archivo);
									br = new BufferedReader(fr);
									String linea=br.readLine();
							        while((linea)!=null){
							        	ta1.append(linea);
							        	ta1.append(System.getProperty("line.separator")); 
							        	linea = br.readLine();
							        }
							        br.close();
								}else{
									seleccion.cancelSelection();
								}
								
							}catch(Exception e1){
								e1.printStackTrace();
							}
							
						}
					});
		}
		return abrir;
	}

	private JPanel getPanelPrincipal(){
		panelPrincipal_1=new JPanel();
		panelPrincipal_1.setLayout(null);
		l0=new JLabel();
		l0.setBounds(351, 11, 185, 34);
		l0.setFont(new java.awt.Font("Verdana", Font.BOLD, 18));
		l0.setText("Analizador léxico");
		l1=new JLabel();
		l1.setBounds(122, 26, 163, 34);
		l1.setText("Código de entrada");
		l1.setFont(new java.awt.Font("Verdana", Font.PLAIN, 14));
		l2=new JLabel();
		l2.setBounds(627, 26, 146, 34);
		l2.setText("Código de salida");
		l2.setFont(new java.awt.Font("Verdana", Font.PLAIN, 14));
		sp1=new JScrollPane();
		sp1.setBounds(39, 68, 350, 466);
		sp1.setVerticalScrollBarPolicy(sp1.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp1.setHorizontalScrollBarPolicy(sp1.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp2=new JScrollPane();
		sp2.setBounds(525, 68, 350, 466);
		sp2.setVerticalScrollBarPolicy(sp2.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp2.setHorizontalScrollBarPolicy(sp2.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		panelPrincipal_1.add(l0);
		panelPrincipal_1.add(l1);
		panelPrincipal_1.add(l2);
		panelPrincipal_1.add(getBotonTokens());
		panelPrincipal_1.add(sp1);
		
		ta1=new JTextArea();
		sp1.setViewportView(ta1);
		panelPrincipal_1.add(sp2);
		
		ta2=new JTextArea();
		sp2.setViewportView(ta2);
		ta2.setEditable(false);
		panelPrincipal_1.validate();
		return panelPrincipal_1;
	}
	
	public JButton getBotonTokens() {
		botonTokens=new JButton();
		botonTokens.setBounds(399, 275, 116, 50);
		botonTokens.setText("Lista Tokens");
		botonTokens.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
		botonTokens.revalidate();
		botonTokens.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent e){
							String contenido=null;
							contenido=new String(ta1.getText());
							if(contenido.equals("")){
								JOptionPane.showMessageDialog(null,"Debe abrir un archivo o escribir un programa en c++ \n antes de proceder al analisis de tokens");
							}
							else{
								in=new StringBufferInputStream(contenido);
								AnalizadorLexico analizador = new AnalizadorLexico(in);
								Token token = analizador.scanner();
								while(token.getTipo() != TipoToken.EOF) {
									ta2.append("TOKEN: "+token.getTipo()+"\t ATRIBUTO: "+token.getAtributo()+"\n");
									token = analizador.scanner();
								}
								ta2.append("TOKEN: "+token.getTipo()+"\t ATRIBUTO: "+token.getAtributo()+"\n");
							}
						}
					});
		return botonTokens;
	}

	public static void main(String[] args) {
		Compilador c = new Compilador();
		c.setEnabled(true);
		c.setVisible(true);
	}
}