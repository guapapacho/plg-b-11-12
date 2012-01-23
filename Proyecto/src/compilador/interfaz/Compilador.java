package compilador.interfaz;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
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

import compilador.analizadorLexico.AnalizadorLexico;
import compilador.analizadorSintactico.AnalizadorSintactico;
import compilador.gestionErrores.GestorErrores;

/**
 * @author Grupo 1
 *
 */
@SuppressWarnings({ "serial", "deprecation" })
public class Compilador extends JFrame {

	/**
	 * @param args
	 */
	private JPanel panelPrincipal_1;
	private JMenuBar barraMenu=null;
	private JMenu ficheroMenu=null;
	private JMenuItem abrir=null;
	private JMenuItem guardar=null;
	private JTextArea ta1=null;
	private JTextArea ta2=null;
	private JScrollPane sp1 =null;
	private JScrollPane sp2 =null;
	private JLabel l0=null;
	private JLabel l1=null;
	private JLabel l2=null;
	private JButton botonTokens=null;
	private File archivo=null;
	private FileReader fr=null;
	private BufferedReader br=null;
	private InputStream in=null;
	private java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	private int puntox1=screenSize.width/32;
	private int puntoy1=screenSize.height/16;
	private int puntox2=(int) (screenSize.width / (3.5));
	private int puntoy2=puntoy1*13;
	private int puntox3= screenSize.width / 3;
	private int puntoy3=puntoy1*6;
	private int puntox4= (int) (screenSize.width /(2.25));
	private int puntox5=(screenSize.width/4)*2;
	private int puntox6=screenSize.width/10;
	private int puntox7=(screenSize.width/3)*2;
	private int puntoy4= screenSize.height/32;
	
	
	/**
	 * Constructora de la clase
	 */
	public Compilador(){
		super();
		crearInterfaz();
	}
	
	/**
	 * Metodo que configura la interfaz de usuario
	 */
	public void crearInterfaz() {
		//this.setSize(1250, 630);
		//this.setLocation(50, 50);
		this.setSize(screenSize.width, screenSize.height);
		this.setJMenuBar(getBarraMenu());
		this.setContentPane(getPanelPrincipal());
		this.setTitle("Traductor C++ - Pascal");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);	
	}

	/**
	 * Metodo que configura la barra de menu de la interfaz de usuario
	 * @return Objeto de tipo JMenuBar
	 */
	public JMenuBar getBarraMenu(){
		if (barraMenu==null){
			barraMenu=new JMenuBar();
			barraMenu.add(getFicherosMenu());
		} 
		return barraMenu;
	}
	
	/**
	 * Metodo que implementa el submenu "Ficheros"
	 * @return Objeto de tipo JMenu
	 */
	public JMenu getFicherosMenu() {
		if (ficheroMenu==null){
			ficheroMenu=new JMenu();
			ficheroMenu.setText("Ficheros"); 
			ficheroMenu.add(getLeerItem());
			ficheroMenu.add(getGuardarItem());
		}
		return ficheroMenu;
	}

	/**
	 * Metodo que implementa el item "Guardar" dentro del submenu "Ficheros"
	 * @return Objeto de tipo JMenuItem
	 */
	private JMenuItem getGuardarItem() {
		if (guardar==null){
			guardar = new JMenuItem();
			guardar.setText("Guardar");
			guardar.addActionListener(
					new ActionListener(){
						@SuppressWarnings("static-access")
						public void actionPerformed(ActionEvent e) {
							JFileChooser seleccion = new JFileChooser();
							int num=seleccion.showSaveDialog(Compilador.this);
							try {
								if (num == seleccion.APPROVE_OPTION){
									archivo = seleccion.getSelectedFile();
									PrintWriter fichSal= new PrintWriter(new FileOutputStream(archivo));
									String contenido=new String(ta1.getText());
									StringTokenizer st=new StringTokenizer(contenido,"\n");
									while(st.hasMoreTokens()){
										String linea=st.nextToken();
										fichSal.print(linea+"\n");
									}
									ta1.setText(null);
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

	/**
	 * Metodo que implementa el item "Abrir" dentro del submenu "Ficheros"
	 * @return Objeto de tipo JMenuItem
	 */
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
									ta1.setText("");
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

	/**
	 * Metodo que configura el panel contenido dentro del Frame principal
	 * @return Objeto de tipo JPanel
	 */
	@SuppressWarnings("static-access")
	private JPanel getPanelPrincipal(){
		panelPrincipal_1=new JPanel();
		panelPrincipal_1.setLayout(null);
		l0=new JLabel();
		l0.setBounds(puntox2, 7, 250, 34);
		l0.setFont(new java.awt.Font("Verdana", Font.BOLD, 18));
		l0.setText("Analizador sintáctico");
		l1=new JLabel();
		l1.setBounds(puntox6, puntoy4, 163, 34);
		l1.setText("Código de entrada");
		l1.setFont(new java.awt.Font("Verdana", Font.BOLD, 14));
		l2=new JLabel();
		l2.setBounds(puntox7, puntoy4, 146, 34);
		l2.setText("Código de salida");
		l2.setFont(new java.awt.Font("Verdana", Font.BOLD, 14));
		sp1=new JScrollPane();
		sp1.setBounds(puntox1, puntoy1, puntox2, puntoy2);
		sp1.setVerticalScrollBarPolicy(sp1.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp1.setHorizontalScrollBarPolicy(sp1.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp2=new JScrollPane();
		/** CON EL ULTIMO PARAMETRO DE ESTA FUN DISMINUYES EL TAMAÑO DEL PANEL DERECHO */
		sp2.setBounds(puntox4, puntoy1, puntox5, puntoy2);
		sp2.setVerticalScrollBarPolicy(sp2.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp2.setHorizontalScrollBarPolicy(sp2.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		panelPrincipal_1.add(l0);
		panelPrincipal_1.add(l1);
		panelPrincipal_1.add(l2);
		panelPrincipal_1.add(getBotonTokens());
		panelPrincipal_1.add(sp1);
		
		ta1=new JTextArea();
		ta1.setText("#include <Alina.h> \n#include \"cris.h\" " +
				"int a=2,b[3]={1,2,3},c; " +
				"\nconst bool i=3; \nconst float k=true; \nint f(int a, int b); " +
				"\nfloat g=3; \ndouble h();");
		ta1.setFont(new java.awt.Font("Verdana", Font.BOLD, 12));
		sp1.setViewportView(ta1);
		panelPrincipal_1.add(sp2);
		
		ta2=new JTextArea();
		ta2.setFont(new java.awt.Font("Verdana", Font.BOLD, 12));
		sp2.setViewportView(ta2);
		ta2.setEditable(false);
		panelPrincipal_1.validate();
		return panelPrincipal_1;
	}
	
	/**
	 * Metodo que implementa el boton "Lista Tokens"
	 * @return Objeto de tipo JButton
	 */
	public JButton getBotonTokens() {
		botonTokens=new JButton();
		botonTokens.setBounds(puntox3, puntoy3, puntox1*3, 50);
		botonTokens.setText("Parse");
		botonTokens.setFont(new java.awt.Font("Verdana", Font.BOLD, 11));
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
						GestorErrores gestor = GestorErrores.getGestorErrores();
						gestor.resetErrores();
						AnalizadorLexico anLex = new AnalizadorLexico(in);						
						AnalizadorSintactico anSin = new AnalizadorSintactico(anLex);
						ta2.setText("");
						ta2.append("Tokens:");
						ta2.append(anSin.getStringTokens());
						ta2.append("\nParse:\n");
						ta2.append(anSin.getStringParse());
						ta2.append("\nErrores:");
						ta2.append(gestor.muestraListaErrores());
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