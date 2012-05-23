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
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import compilador.analizadorLexico.AnalizadorLexico;
import compilador.analizadorSintactico.AnalizadorSintactico;
import compilador.gestionErrores.GestorErrores;
import compilador.gestionSalida.GestorSalida;
import compilador.gestionTablasSimbolos.GestorTablasSimbolos;

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
	private JTabbedPane panelPestanas;
	private JMenuBar barraMenu=null;
	private JMenu ficheroMenu=null;
	private JMenuItem abrir=null;
	private JMenuItem guardar=null;
	private JTextArea ta0=null;
	private JTextArea ta1=null;
	private JTextArea ta2a=null;
	private JTextArea ta2b=null;
	private JTextArea ta3=null;
	private JScrollPane sp0 =null;
	private JScrollPane sp1 =null;
	private JScrollPane sp2a =null;
	private JScrollPane sp2b =null;
	private JScrollPane sp3 =null;
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
	private int puntox5=(screenSize.width/17)*9;
	private int puntox6=screenSize.width/10;
	private int puntox7=(screenSize.width/3)*2;
	private int puntoy4= screenSize.height/90;
	private int puntox8=puntox1-30;
	
	
	/**
	 * Constructora de la clase
	 */
	public Compilador(){
		super();
	}
	
	/**
	 * Metodo que configura la interfaz de usuario
	 */
	public void crearInterfaz() {
		this.setSize(screenSize.width, screenSize.height);
		this.setJMenuBar(getBarraMenu());
		this.setContentPane(getPanelPrincipal());
		this.setTitle("Traductor C++ - Pascal");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);	
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setEnabled(true);
		this.setVisible(true);
	}

	/**
	 * Metodo que configura la barra de menu de la interfaz de usuario
	 * @return Objeto de tipo JMenuBar
	 */
	private JMenuBar getBarraMenu(){
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
	private JMenu getFicherosMenu() {
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
		
		panelPestanas = new JTabbedPane();
		l1=new JLabel();
		l1.setBounds(puntox6, puntoy4, 163, 34);
		l1.setText("Código de entrada");
		l1.setFont(new java.awt.Font("Verdana", Font.BOLD, 14));
		l2=new JLabel();
		l2.setBounds(puntox7, puntoy4, 146, 34);
		l2.setText("Código de salida");
		l2.setFont(new java.awt.Font("Verdana", Font.BOLD, 14));
		
		sp0=new JScrollPane();
		sp0.setBounds(puntox8, puntoy1, 30, puntoy2);
		sp0.setVerticalScrollBarPolicy(sp0.VERTICAL_SCROLLBAR_NEVER);
		sp0.setHorizontalScrollBarPolicy(sp0.HORIZONTAL_SCROLLBAR_NEVER);
		
		sp1=new JScrollPane();
		sp1.setBounds(puntox1, puntoy1, puntox2, puntoy2);
		sp1.setVerticalScrollBarPolicy(sp1.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp1.setHorizontalScrollBarPolicy(sp1.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		sp2a=new JScrollPane();
		sp2a.setVerticalScrollBarPolicy(sp2a.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp2a.setHorizontalScrollBarPolicy(sp2a.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		sp2b=new JScrollPane();
		sp2b.setVerticalScrollBarPolicy(sp2b.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp2b.setHorizontalScrollBarPolicy(sp2b.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		panelPestanas.setBounds(puntox4, puntoy1, puntox5, puntoy2);
		panelPestanas.add("Traduccción a Pascal", sp2b);
		panelPestanas.add("Salidas Analizadores", sp2a);
		
		sp3=new JScrollPane();
		sp3.setBounds(puntox3+30, puntoy3 - 50, 60, 20);
		sp3.setVerticalScrollBarPolicy(sp3.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp3.setHorizontalScrollBarPolicy(sp3.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		ta3 = new JTextArea();
		ta3.setBounds(puntox3+30, puntoy3 - 50, 60, 20);
		ta3.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
		sp3.setViewportView(ta3);
		

		panelPrincipal_1.add(l1);
		panelPrincipal_1.add(l2);
		panelPrincipal_1.add(getBotonTokens());
		panelPrincipal_1.add(sp1);
		panelPrincipal_1.add(sp3);
		
		ta1=new JTextArea();

		ta1.setFont(new java.awt.Font(Font.MONOSPACED, Font.BOLD, 16));
		ta1.setTabSize(3);
		sp1.setViewportView(ta1);
		
		panelPrincipal_1.add(panelPestanas);
		
		ta2a=new JTextArea();
		ta2a.setFont(new java.awt.Font("Verdana", Font.BOLD, 13));
		ta2a.setEditable(false);
		sp2a.setViewportView(ta2a);
		
		ta2b=new JTextArea();
		ta2b.setFont(new java.awt.Font(Font.MONOSPACED, Font.BOLD, 16));
		ta2b.setEditable(false);
		ta2b.setTabSize(3);
		sp2b.setViewportView(ta2b);
		
		
		ta0 = new JTextArea();
		ta0.setFont(new java.awt.Font("Verdana", Font.BOLD, 12));
		rellenarTP0();
		sp0.setViewportView(ta0);
		ta0.setEditable(false);
		
		actualizarPos(1, 1);
		ta1.addCaretListener(new CaretListener() {
            // Each time the caret is moved, it will trigger the listener and its method caretUpdate.
            // It will then pass the event to the update method including the source of the event (which is our textarea control)
            public void caretUpdate(CaretEvent e) {
                JTextArea editArea = (JTextArea)e.getSource();
                // Lets start with some default values for the line and column.
                int linenum = 1;
                int columnnum = 1;
                // We create a try catch to catch any exceptions. We will simply ignore such an error for our demonstration.
                try {
                    int caretpos = editArea.getCaretPosition();
                    linenum = editArea.getLineOfOffset(caretpos);
                    columnnum = caretpos - editArea.getLineStartOffset(linenum);
                    linenum += 1;
                    columnnum += 1;
                }
                catch(Exception ex) { }
                // Once we know the position of the line and the column, pass it to a helper function for updating the status bar.
                actualizarPos(linenum, columnnum);
            }
		});
		
		
		panelPrincipal_1.validate();
		return panelPrincipal_1;
	}

	private void actualizarPos(int x, int y){
		ta3.setText(Integer.toString(x)+" : "+Integer.toString(y));
	}
	
	private void rellenarTP0(){
		String out = ""; 
		int j = 1;
		char[] c = ta1.getText().toCharArray();
		for(int i=1; i<c.length; i++){
			if(c[i]=='\n'){
				out += j;
				out += '\n';
				j++;
			}
		}
		out += j;
		out += '\n';
		ta0.setText(out);
	}
	
	/**
	 * Metodo que implementa el boton "Lista Tokens"
	 * @return Objeto de tipo JButton
	 */
	private JButton getBotonTokens() {
		botonTokens=new JButton();
		botonTokens.setBounds(puntox3, puntoy3, puntox1*3, 50);
		botonTokens.setText("Traducir");
		botonTokens.setFont(new java.awt.Font("Verdana", Font.BOLD, 11));
		botonTokens.revalidate();
		
		botonTokens.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e){
					String contenido=null;
					contenido=new String(ta1.getText());
					if(contenido.equals("")){
						JOptionPane.showMessageDialog(null,"Debe abrir un archivo o escribir un programa en c++ \n " +
								"antes de proceder al analisis de tokens");
					}
					else{
						in=new StringBufferInputStream(contenido);
						GestorTablasSimbolos.resetTablasSimbolos();
						GestorErrores gestorE = GestorErrores.getGestorErrores();
						GestorSalida gestorS = GestorSalida.getGestorSalida();
						
						gestorE.resetErrores();
						gestorE.resetWarnings();
						gestorS.resetResultado();
						
						AnalizadorLexico anLex = new AnalizadorLexico(in);						
						AnalizadorSintactico anSin = new AnalizadorSintactico(anLex);
						
						
						ta2a.setText("");
						ta2a.append("Tokens:");
						ta2a.append(anSin.getStringTokens());
						ta2a.append("\nParse:\n");
						ta2a.append(anSin.getStringParse());
						ta2a.append("Total:\n"+anSin.getParse().size()+" reglas aplicadas.\n");
						ta2a.append("\nTablas de símbolos:\n");
						ta2a.append(GestorTablasSimbolos.getGestorTS().toString());
						ta2a.append("\nErrores:");
						ta2a.append(gestorE.muestraListaErrores());
						ta2a.append("\nWarnings:");
						ta2a.append(gestorE.muestraListaWarnings());
						
						ta2b.setText("");
						ta2b.append(gestorS.getResultadoFinal());
						
						rellenarTP0();
					}
				}
			});
		return botonTokens;
	}
	
}