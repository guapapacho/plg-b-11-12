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
import compilador.analizadorLexico.Token;
import compilador.analizadorLexico.Token.TipoToken;
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
		this.setSize(930, 630);
		this.setLocation(150, 100);
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
		l0.setBounds(370, 11, 185, 34);
		l0.setFont(new java.awt.Font("Verdana", Font.BOLD, 18));
		l0.setText("Analizador léxico");
		l1=new JLabel();
		l1.setBounds(145, 26, 163, 34);
		l1.setText("Código de entrada");
		l1.setFont(new java.awt.Font("Verdana", Font.BOLD, 14));
		l2=new JLabel();
		l2.setBounds(640, 26, 146, 34);
		l2.setText("Código de salida");
		l2.setFont(new java.awt.Font("Verdana", Font.BOLD, 14));
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
		botonTokens.setBounds(399, 275, 116, 50);
		botonTokens.setText("Lista Tokens");
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
								GestorErrores gestor = new GestorErrores();
								AnalizadorLexico analizador = new AnalizadorLexico(in);
								Token token = analizador.scanner();
								ta2.setText("");
								while(token.getTipo() != TipoToken.EOF) {
									ta2.append("TOKEN: "+token.getTipo()+"\t ATRIBUTO: "+token.getAtributo()+"\n");
									token = analizador.scanner();
								}
								ta2.append("TOKEN: "+token.getTipo()+"\t ATRIBUTO: "+token.getAtributo()+"\n");
								gestor.setErrores(analizador.devuelveErrorLex());
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