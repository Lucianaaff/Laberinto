import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

class Form extends JFrame {
    Form(String titulo, int w, int h) {
        super(titulo);
        setSize(w, h);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

//Posibles estados de cada casilla
enum Estado {
    VACIO,
    OBSTACULO,
    ENTRADA,
    SALIDA,
    VISITADO,
    CAMINO,
    ACTUAL
};

class Casilla {
    int x, y;
    Casilla pariente;

    Casilla(int x, int y) {
        this.x = x;
        this.y = y;
        this.pariente = null;
    }

    Casilla(int x, int y, Casilla pariente) {
        this.x = x;
        this.y = y;
        this.pariente = pariente;
    }

    Casilla pariente() {
        return this.pariente;
    }

    void imprimir() {
        System.out.println("x = " + x + " y = " + y);
    }
}

class Panel extends JPanel implements Runnable {
    // Atributos privados.
    private int w, h, fila, columna;
    private final int columnas = 12;
    private final int filas    = columnas;
    private Casilla actual;
    private Queue<Casilla> camino;
    private BufferedImage bloque;
    private BufferedImage moneda;
    private BufferedImage castillo;
    private BufferedImage princesa;
    private BufferedImage mario;
    private boolean bloquear; // Bloquear cuando dibuje el camino.

    // Atributos públicos.
    public Casilla inicio;
    public Casilla fin;
    public Estado cuadricula[][] = new Estado[columnas][filas];
    public Thread th;

    Panel() {
        super();
        int i, j;

        setOpaque(true);
        setBackground(Color.black);
        // Hacer que la cuadrilla esté en estado vacío al inicio.
        for (i = 0; i < columnas; i++) {
            for (j = 0; j < filas; j++) {
                cuadricula[i][j] = Estado.VACIO;
            }
        }

        try {
            bloque = ImageIO.read(new File("bloque.png"));
            moneda = ImageIO.read(new File("moneda.png"));
            castillo = ImageIO.read(new File("castillo.png"));
            princesa = ImageIO.read(new File("princesa.png"));
            mario = ImageIO.read(new File("mario.png"));
        } catch (IOException e) {}

        inicio = fin = null;
        bloquear = false;
        actual = new Casilla(-1, -1);
        bordes();

    }

    public void run() {
        solucionar();
    }

    public void correr() {
        th = new Thread(this);
        th.start();
    }

    // Función que es llamada por sí misma para pintar el panel.
    public void paint(Graphics g) {
        super.paint(g);
        int i, j;

        w = getSize().width;
        h = getSize().height;
        fila = h / filas;
        columna = w / columnas;
        limpiar(g);
        for (i = 0; i < columnas; i++) {
            for (j = 0; j < filas; j++) {
                // Dibujar los obstáculos.
                if (cuadricula[i][j] == Estado.OBSTACULO) {
                    g.drawImage(bloque, columna * i, fila * j, columna, fila, null);
                } else if (cuadricula[i][j] == Estado.CAMINO) {
                    g.drawImage(moneda, columna * i, fila * j, columna, fila, null);
                } else if (cuadricula[i][j] == Estado.ENTRADA) {
                    if (bloquear)
                        g.drawImage(castillo, columna * i, fila * j, columna, fila, null);
                    else
                        g.drawImage(mario, columna * i, fila * j, columna, fila, null);
                } else if (cuadricula[i][j] == Estado.SALIDA) {
                    g.drawImage(princesa, columna * i, fila * j, columna, fila, null);
                } else if (cuadricula[i][j] == Estado.ACTUAL) {
                    g.drawImage(mario, columna * i, fila * j, columna, fila, null);
                }
            }
        }
    }

    // Función para cambiar el estado de los bordes a `OBSTACULO`.
    private void bordes() {
        int i;

        for (i = 0; i < columnas; i++) {
            cuadricula[i][0] = Estado.OBSTACULO;
            cuadricula[i][filas-1] = Estado.OBSTACULO;
        }

        for (i = 0; i < filas; i++) {
            cuadricula[0][i] = Estado.OBSTACULO;
            cuadricula[columnas-1][i] = Estado.OBSTACULO;
        }

        if (inicio != null)
            cuadricula[inicio.x][inicio.y] = Estado.ENTRADA;
        if (fin != null)
            cuadricula[fin.x][fin.y] = Estado.SALIDA;
    }

    // Función para determinar si la columna y fila es un borde.
    private boolean es_borde(int x, int y) {
        if ((x < columnas && y == 0) || (x == 0 && y < filas) ||
            (x < columnas && y == filas-1) || (x == columnas-1 && y < filas)) {
            return true;
        }

        return false;
    }

    // Dibujar la cuadrilla y pintar los rectángulos vacíos.
    public void limpiar(Graphics g) {
        int i, j;
        for (i = 0; i < columnas; i++) {
            for (j = 0; j < filas; j++) {
                g.setColor(Color.black);
                g.drawRect(columna * i, fila * j, columna, fila);
                g.fillRect((columna * i) + 1, (fila * j) + 1,
                           columna - 1, fila - 1);
            }
        }
        bordes();
    }

    // Función que es llamada cuando el evento `click` es llamado.
    public void clicked(MouseEvent ev) {
        // f: fila, c: columna.
        int f, c;

        // Obtener la columna y fila del cuadro al que se le hizo clic.
        c = ev.getX() / columna;
        f = ev.getY() / fila;

        // Salir si la columna o fila obtenida sobre pasa el límite
        // de la cuadrilla.
        if (c >= columnas || f >= filas) {
            return;
        }

        if (actual.x == c && actual.y == f) {
            return;
        }

        if (bloquear) {
            return;
        }

        reiniciar();

        actual.x = c;
        actual.y = f;

        // Cuando se haga clic izquierdo.
        if (SwingUtilities.isLeftMouseButton(ev)) {
            if (cuadricula[c][f] == Estado.OBSTACULO && !es_borde(c,f)) {
                cuadricula[c][f] = Estado.VACIO;
            } else if (es_borde(c, f)) {
                if (cuadricula[c][f] == Estado.OBSTACULO) {
                    inicio = new Casilla(c, f);

                    // Si la entrada es igual que la salida:
                    // reemplazar por la entrada.
                    if (fin != null) {
                        if (inicio.x == fin.x && inicio.y == fin.y) {
                            fin = null;
                        }
                    }
                } else if (cuadricula[c][f] == Estado.ENTRADA) {
                    cuadricula[c][f] = Estado.OBSTACULO;
                    inicio = null;
                }
            } else {
                cuadricula[c][f] = Estado.OBSTACULO;
            }

            repaint();
        }

        // Cuando se haga clic derecho.
        if (SwingUtilities.isRightMouseButton(ev)) {
            if (es_borde(c, f)) {
                fin = new Casilla(c, f);

                // Si la salida es igual que la entrada:
                // reemplazar por la salida.
                if (inicio != null) {
                    if (fin.x == inicio.x && fin.y == inicio.y) {
                        inicio = null;
                    }
                }
            }
            repaint();
        }

    }

    public void solucionar() {
        if (inicio == null && fin == null) {
            return;
        }

        reiniciar();

        camino = new LinkedList<>();
        boolean encontrado = false;

        camino.add(fin);

        Casilla tmp = null;
        while (!camino.isEmpty()) {
            tmp = camino.remove();

            if (cuadricula[tmp.x][tmp.y] == Estado.ENTRADA) {
                encontrado = true;
                break;
            }

            cuadricula[tmp.x][tmp.y] = Estado.VISITADO;
            if (es_valido(tmp.x+1, tmp.y)) {
                camino.add(new Casilla(tmp.x+1, tmp.y, tmp));
            }

            if (es_valido(tmp.x-1, tmp.y)) {
                camino.add(new Casilla(tmp.x-1, tmp.y, tmp));
            }

            if (es_valido(tmp.x, tmp.y+1)) {
                camino.add(new Casilla(tmp.x, tmp.y+1, tmp));
            }

            if (es_valido(tmp.x, tmp.y-1)) {
                camino.add(new Casilla(tmp.x, tmp.y-1, tmp));
            }
        }

        if (!encontrado) {
            JOptionPane.showMessageDialog(null, "¡Oh, no! Mario no podrá encontrarse con su princesa. \nNo hay camino posible para visitarla.");
            return;
        }

        bloquear = true;
        Casilla anterior = null;
        while (tmp.pariente() != null) {
            cuadricula[tmp.x][tmp.y] = Estado.ACTUAL;
            if (anterior != null) {
                cuadricula[anterior.x][anterior.y] = Estado.CAMINO;
            }

            anterior = tmp;
            tmp = tmp.pariente();
            repaint();
            try { Thread.sleep(200); }
            catch (InterruptedException ex) {}
        }
        bloquear = false;
    }

    public void reiniciar(){
        for(int i=0 ; i<columnas ; i++){
            for(int j=0 ; j<filas ; j++){
                if(cuadricula[i][j]== Estado.CAMINO || cuadricula[i][j]== Estado.VISITADO || cuadricula[i][j] == Estado.ACTUAL) {
                        cuadricula[i][j] = Estado.VACIO;
                }
            }
        }
        bordes();
        repaint();
    }
     public void limpiarlabe(){
        for(int i=0 ; i<columnas; i++){
            for(int j=0 ; j<filas; j++){
                if(cuadricula[i][j]== Estado.CAMINO || cuadricula[i][j]== Estado.VISITADO || cuadricula[i][j]== Estado.OBSTACULO || cuadricula[i][j] == Estado.ACTUAL) {
                        cuadricula[i][j]= Estado.VACIO;
                }
                 if(cuadricula[i][j]== Estado.ENTRADA){
                        cuadricula[i][j]= Estado.OBSTACULO;
                        inicio=null;
                }
                if(cuadricula[i][j]==Estado.SALIDA){
                        cuadricula[i][j]= Estado.OBSTACULO;
                        fin=null;
                }

            }
        }
        bordes();
        repaint();
    }

    private boolean es_valido(int x, int y) {
        if (x < 0 || x >= columnas || y < 0 || y >= filas ||
                cuadricula[x][y] == Estado.VISITADO ||
                cuadricula[x][y] == Estado.OBSTACULO) {
            return false;
        }

        return true;
    }
}

class Laberinto {
    public static void main(String[] args) {
        // Dimensión del formulario.
        final int w = 600;
        final int h = 620; // El excedente es para el menú.

        // Creación de formulario.
        Form f = new Form("Laberinto", w, h);

        // Creación de la barra de menú.
        JMenuBar menubar = new JMenuBar();

        // Creación de cada menú.
        JMenu laberinto_menu = new JMenu("Laberinto");

        // Creación de cada elemento de los menús.
        JMenuItem inf_menu_item = new JMenuItem("Instrucciones");
        JMenuItem correr_menu_item = new JMenuItem("Correr");
        JMenuItem reiniciar_menu_item = new JMenuItem("Reiniciar");
        JMenuItem limpiar_menu_item = new JMenuItem("Limpiar");


        // Agregar los elementos a los menús y a la barra de menú.
        menubar.add(laberinto_menu);
        laberinto_menu.add(inf_menu_item);
        laberinto_menu.addSeparator();
        laberinto_menu.add(correr_menu_item);
        laberinto_menu.addSeparator();
        laberinto_menu.add(reiniciar_menu_item);
        laberinto_menu.addSeparator();
        laberinto_menu.add(limpiar_menu_item);

        menubar.setBackground(Color.white);


        // Agregar la barra de menú al formulario.
        f.setJMenuBar(menubar);

        // Creación del panel.
        final Panel panel = new Panel();
        f.add(panel);

        // Eventos.
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent ev) {
                panel.clicked(ev);
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent ev) {
                panel.clicked(ev);
            }
        });

        correr_menu_item.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent ev) {
                panel.correr();
            }
        });

        reiniciar_menu_item.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent ev) {
                panel.reiniciar();
            }
         });

         limpiar_menu_item.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent ev) {
                panel.limpiarlabe();
            }
        });

        inf_menu_item.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent ev) {
                JOptionPane.showMessageDialog(null," 1)Hacer click o dejar presionado el raton para colocar los obstaculos \n 2)Presionar el boton derecho para la entrada y el izquierdo para la salida \n 3)Seleccionar correr para que se resuelva el laberinto \n 4)Seleccionar reiniciar para que se recorra nuevamente \n 5) Seleccionar limpiar para comenzar otra vez ");


             }
         });

        // Mostrar el formulario.
        f.setVisible(true);
    }
}
