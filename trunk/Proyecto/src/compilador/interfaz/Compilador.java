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
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import compilador.analizadorLexico.AnalizadorLexico;
import compilador.analizadorLexico.Token.OpAritmetico;
import compilador.analizadorLexico.Token.OpAsignacion;
import compilador.analizadorLexico.Token.OpComparacion;
import compilador.analizadorLexico.Token.OpLogico;
import compilador.analizadorSemantico.ExpresionTipo;
import compilador.analizadorSemantico.ExpresionTipo.TipoBasico;
import compilador.analizadorSintactico.AnalizadorSintactico;
import compilador.gestionErrores.GestorErrores;
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
	private JMenuBar barraMenu=null;
	private JMenu ficheroMenu=null;
	private JMenuItem abrir=null;
	private JMenuItem guardar=null;
	private JTextArea ta0=null;
	private JTextArea ta1=null;
	private JTextArea ta2=null;
	private JTextArea ta3=null;
	private JScrollPane sp0 =null;
	private JScrollPane sp1 =null;
	private JScrollPane sp2 =null;
	private JScrollPane sp3 =null;
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
	private int puntoy4= screenSize.height/40;
	private int puntox8=puntox1-30;
	
	
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
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
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
		
		sp0=new JScrollPane();
		//sp0.setBounds(puntox8, puntoy1-3, 30, puntoy2+3);
		sp0.setBounds(puntox8, puntoy1, 30, puntoy2);
		sp0.setVerticalScrollBarPolicy(sp0.VERTICAL_SCROLLBAR_NEVER);
		sp0.setHorizontalScrollBarPolicy(sp0.HORIZONTAL_SCROLLBAR_NEVER);
		
		sp1=new JScrollPane();
		sp1.setBounds(puntox1, puntoy1, puntox2, puntoy2);
		sp1.setVerticalScrollBarPolicy(sp1.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp1.setHorizontalScrollBarPolicy(sp1.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		sp2=new JScrollPane();
		/** CON EL ULTIMO PARAMETRO DE ESTA FUN DISMINUYES EL TAMAÑO DEL PANEL DERECHO */
		sp2.setBounds(puntox4, puntoy1, puntox5, puntoy2);
		sp2.setVerticalScrollBarPolicy(sp2.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp2.setHorizontalScrollBarPolicy(sp2.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		sp3=new JScrollPane();
		sp3.setBounds(puntox3, puntoy3 - 50, 60, 20);
		sp3.setVerticalScrollBarPolicy(sp3.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp3.setHorizontalScrollBarPolicy(sp3.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		ta3 = new JTextArea();
		//ta3.setBounds(100, 25, 20, 20);
		ta3.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
		sp3.setViewportView(ta3);
		//ta3.setEditable(false);
		//ta3.setText("prueba");
		
		panelPrincipal_1.add(l0);
		panelPrincipal_1.add(l1);
		panelPrincipal_1.add(l2);
		panelPrincipal_1.add(getBotonTokens());
		panelPrincipal_1.add(sp1);
//		panelPrincipal_1.add(sp0);
		panelPrincipal_1.add(sp3);
		
		ta1=new JTextArea();
		ta1.setText("#include <Alina.h> \n#include \"cris.h\" " +
				"\n\nint a,b[3]={1,2,3},c=7; " +
				"\nconst bool i=true; \nconst float k=1.2; \n\nint f(int a, int b); " +
				"\n\nfloat g=3; \n\ndouble h();");
		ta1.setFont(new java.awt.Font("Verdana", Font.BOLD, 12));
		sp1.setViewportView(ta1);
		panelPrincipal_1.add(sp2);
		
		ta2=new JTextArea();
		ta2.setFont(new java.awt.Font("Verdana", Font.BOLD, 12));
		sp2.setViewportView(ta2);
		ta2.setEditable(false);
		
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
		//System.out.println("Esquinas del panel 1: "+puntox1+" "+puntox2+" "+puntoy1+" "+puntoy2);
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
		//tp0.setText(out);
		ta0.setText(out);
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
						GestorTablasSimbolos.resetTablasSimbolos();
						GestorErrores gestor = GestorErrores.getGestorErrores();
						gestor.resetErrores();
						AnalizadorLexico anLex = new AnalizadorLexico(in);						
						AnalizadorSintactico anSin = new AnalizadorSintactico(anLex);
						ta2.setText("");
						ta2.append("Tokens:");
						ta2.append(anSin.getStringTokens());
						ta2.append("\nParse:\n");
						ta2.append(anSin.getStringParse());
						ta2.append("Total:\n"+anSin.getParse().size()+" reglas aplicadas.\n");
						ta2.append("\nSemantico:\n");
//						ta2.append(anSin.muestraDeclaraciones());
						ta2.append(GestorTablasSimbolos.getGestorTS().toString());
						ta2.append("\nErrores:");
						ta2.append(gestor.muestraListaErrores());
						rellenarTP0();
					}
				}
			});
		return botonTokens;
	}

	public static void comprobarExpresiones(){
		ExpresionTipo e1,e2;
		System.out.println("------------------------------");
		System.out.println("OPERADORES LOGICOS: ");
		System.out.println("------------------------------");
		for(TipoBasico t1 : TipoBasico.values()){
			e1 = new ExpresionTipo(t1);
			for(TipoBasico t2 : TipoBasico.values()){
				e2 = new ExpresionTipo(t2);
				for(OpLogico op : OpLogico.values()){
					if(e1.getTipoBasico()!=TipoBasico.error_tipo && e1.getTipoBasico()!=TipoBasico.vacio
							&& e2.getTipoBasico()!=TipoBasico.error_tipo && e2.getTipoBasico()!=TipoBasico.vacio)
						if(ExpresionTipo.sonEquivLog(e1, e2, op)!=null)
							System.out.println(t1.toString()+" "+op.toString()+" "+t1.toString()+" --> "+ExpresionTipo.sonEquivLog(e1, e2, op).getTipoBasico().toString());
				}
			}	
		}
		System.out.println("-----------------------------");
		System.out.println();
		System.out.println("------------------------------");
		System.out.println("OPERADORES DE ASIGNACION: ");
		System.out.println("------------------------------");
		for(TipoBasico t1 : TipoBasico.values()){
			e1 = new ExpresionTipo(t1);
			for(TipoBasico t2 : TipoBasico.values()){
				e2 = new ExpresionTipo(t2);
				for(OpAsignacion op : OpAsignacion.values()){
					if(e1.getTipoBasico()!=TipoBasico.error_tipo && e1.getTipoBasico()!=TipoBasico.vacio
							&& e2.getTipoBasico()!=TipoBasico.error_tipo && e2.getTipoBasico()!=TipoBasico.vacio)
						if(ExpresionTipo.sonEquivAsig(e1, e2, op)!=null)
							System.out.println(t1.toString()+" "+op.toString()+" "+t1.toString()+" --> "+ExpresionTipo.sonEquivAsig(e1, e2, op).getTipoBasico().toString());
				}
			}	
		}
		System.out.println("-----------------------------");
		System.out.println();
		System.out.println("------------------------------");
		System.out.println("OPERADORES DE COMPARACION: ");
		System.out.println("------------------------------");
		for(TipoBasico t1 : TipoBasico.values()){
			e1 = new ExpresionTipo(t1);
			for(TipoBasico t2 : TipoBasico.values()){
				e2 = new ExpresionTipo(t2);
				for(OpComparacion op : OpComparacion.values()){
					if(e1.getTipoBasico()!=TipoBasico.error_tipo && e1.getTipoBasico()!=TipoBasico.vacio
							&& e2.getTipoBasico()!=TipoBasico.error_tipo && e2.getTipoBasico()!=TipoBasico.vacio)
						if(ExpresionTipo.sonEquivComp(e1, e2, op)!=null)
							System.out.println(t1.toString()+" "+op.toString()+" "+t1.toString()+" --> "+ExpresionTipo.sonEquivComp(e1, e2, op).getTipoBasico().toString());
				}
			}	
		}
		System.out.println("-----------------------------");
		System.out.println();
		System.out.println("------------------------------");
		System.out.println("OPERADORES ARITMETICOS: ");
		System.out.println("------------------------------");
		for(TipoBasico t1 : TipoBasico.values()){
			e1 = new ExpresionTipo(t1);
			for(TipoBasico t2 : TipoBasico.values()){
				e2 = new ExpresionTipo(t2);
				for(OpAritmetico op : OpAritmetico.values()){
					if(e1.getTipoBasico()!=TipoBasico.error_tipo && e1.getTipoBasico()!=TipoBasico.vacio
							&& e2.getTipoBasico()!=TipoBasico.error_tipo && e2.getTipoBasico()!=TipoBasico.vacio)
						if(ExpresionTipo.sonEquivArit(e1, e2, op)!=null)
							System.out.println(t1.toString()+" "+op.toString()+" "+t1.toString()+" --> "+ExpresionTipo.sonEquivArit(e1, e2, op).getTipoBasico().toString());
				}
			}	
		}
		System.out.println("-----------------------------");
		
		
	}
	
	public static void main(String[] args) {
		Compilador c = new Compilador();
		c.setEnabled(true);
		c.setVisible(true);
		
		//Compilador.comprobarExpresiones();
	}
}