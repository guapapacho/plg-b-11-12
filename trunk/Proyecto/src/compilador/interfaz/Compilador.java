package compilador.interfaz;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

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
	
	public Compilador(){
		super();
		crearInterfaz();
	}
	
	public void crearInterfaz() {
		this.setSize(screenSize);
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
			ficheroMenu.setText("Ficheros"); //getText
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
							seleccion.showSaveDialog(Compilador.this);
							String fichero = seleccion.getSelectedFile().getName();
							try {
								PrintWriter fichSal= new PrintWriter(new FileOutputStream(fichero));
								String contenido=new String(ta1.getText());
								StringTokenizer st=new StringTokenizer(contenido,"\n");
								ta1.setText("");
								while(st.hasMoreTokens()){
									String linea=st.nextToken();
									fichSal.print(linea+"\n");
								}
								fichSal.close();
							
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
						public void actionPerformed(ActionEvent e){
							JFileChooser seleccion = new JFileChooser();
							int num = seleccion.showOpenDialog(Compilador.this);
							try{
								if (num == seleccion.APPROVE_OPTION){
								   archivo = seleccion.getSelectedFile();
								}
								fr = new FileReader (archivo);
								br = new BufferedReader(fr);
								String linea=br.readLine();
						        while((linea)!=null){
						        	ta1.append(linea);
						        	ta1.append(System.getProperty("line.separator")); 
						        	linea = br.readLine();
						        }
						        br.close();
							}catch(Exception e1){
								e1.printStackTrace();
							}
							
						}
					});
		}
		return abrir;
	}

	private JPanel getPanelPrincipal(){
		panelPrincipal=new JPanel();
		panelPrincipal.setLayout(null);
		l0=new JLabel();
		l0.setBounds(550, 10, 400, 70);
		l0.setFont(new java.awt.Font("Verdana", Font.BOLD, 18));
		l0.setText("Analizador léxico");
		l1=new JLabel();
		l1.setBounds(250, 40, 400, 70);
		l1.setText("Código de entrada");
		l1.setFont(new java.awt.Font("Verdana", Font.BOLD, 14));
		l2=new JLabel();
		l2.setBounds(925, 40, 400, 70);
		l2.setText("Salida");
		l2.setFont(new java.awt.Font("Verdana", Font.BOLD, 14));
		
		ta1=new JTextArea();
		ta1.setBounds(100, 100, 450, 550);
		sp1=new JScrollPane();
		sp1.setBounds(100, 100, 450, 550);
		sp1.setVerticalScrollBarPolicy(sp1.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp1.setHorizontalScrollBarPolicy(sp1.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp1.getViewport().add(ta1);
		
		ta2=new JTextArea();
		ta2.setBounds(725, 100, 450, 550);
		ta2.setEditable(false);
		sp2=new JScrollPane();
		sp2.setBounds(725, 100, 450, 550);
		sp2.setVerticalScrollBarPolicy(sp2.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp2.setHorizontalScrollBarPolicy(sp2.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp2.getViewport().add(ta2);
		
		botonTokens=new JButton();
		botonTokens.setBounds(575, 300, 120, 50);
		botonTokens.setText("Lista Tokens");
		
		panelPrincipal.add(l0);
		panelPrincipal.add(l1);
		panelPrincipal.add(l2);
		panelPrincipal.add(botonTokens);
		panelPrincipal.add(sp1);
		panelPrincipal.add(sp2);
		panelPrincipal.validate();
		return panelPrincipal;
	}
	
	public static void main(String[] args) {
		Compilador c = new Compilador();
		c.setEnabled(true);
		c.setVisible(true);
	}

}