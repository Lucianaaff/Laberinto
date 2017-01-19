import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

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
    VISITADO
};

class Casilla {
    int x, y;

    Casilla(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class Panel extends JPanel {
    // Atributos privados.
    private int w, h, fila, columna;
    private final int columnas = 16;
    private final int filas    = columnas;
    private Casilla actual;

    // Atributos públicos.
    public Casilla inicio;
    public Casilla fin;
    public final Estado cuadricula[][] = new Estado[columnas][filas];

    Panel() {
        super();
        int i, j;

        // Hacer que la cuadrilla esté en estado vacío al inicio.
        for (i = 0; i < columnas; i++) {
            for (j = 0; j < filas; j++) {
                cuadricula[i][j] = Estado.VACIO;
            }
        }

        inicio = fin = null;
        actual = new Casilla(-1, -1);
        bordes();
    }

    // Función que es llamada por sí misma para pintar el panel.
    public void paint(Graphics g) {
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
                    g.setColor(Color.gray);
                    g.fillRect((columna * i) + 1, (fila * j) + 1,
                                columna - 1, fila - 1);
                }

                // Dibujar el inicio.
                if (inicio != null) {
                    g.setColor(Color.red);
                    g.fillRect((columna * inicio.x) + 1, (fila * inicio.y) + 1,
                                columna - 1, fila - 1);
                }

                if (fin != null) {
                    g.setColor(Color.green);
                    g.fillRect((columna * fin.x) + 1, (fila * fin.y) + 1,
                                columna - 1, fila - 1);
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
                g.setColor(Color.white);
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

        actual.x = c;
        actual.y = f;

        // Cuando se haga clic izquierdo.
        if (SwingUtilities.isLeftMouseButton(ev)) {
            if (cuadricula[c][f] == Estado.OBSTACULO && !es_borde(c,f)) {
                cuadricula[c][f] = Estado.VACIO;
            } else if (es_borde(c, f)) {
                if (cuadricula[c][f] == Estado.OBSTACULO) {
                    cuadricula[c][f] = Estado.ENTRADA;
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
                if (cuadricula[c][f] == Estado.OBSTACULO) {
                    cuadricula[c][f] = Estado.SALIDA;
                    fin = new Casilla(c, f);

                    // Si la salida es igual que la entrada:
                    // reemplazar por la salida.
                    if (inicio != null) {
                        if (fin.x == inicio.x && fin.y == inicio.y) {
                            inicio = null;
                        }
                    }
                } else {
                    cuadricula[c][f] = Estado.OBSTACULO;
                    fin = null;
                }
            }
            repaint();
        }

    }
}

class Laberinto {
    private LinkedList<Casilla> caminos = new LinkedList<Casilla>();

    private boolean solucionar(Estado cuadricula[][], Casilla casilla) {
        if (cuadricula[casilla.x][casilla.y] == Estado.SALIDA) {
            JOptionPane.showMessageDialog(null, "¡Has llegado a la salida!");
        }

        return false;
    }

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
        JMenuItem correr_menu_item = new JMenuItem("Correr");
        JMenuItem salir_menu_item = new JMenuItem("Salir");

        // Agregar los elementos a los menús y a la barra de menú.
        menubar.add(laberinto_menu);
        laberinto_menu.add(correr_menu_item);
        laberinto_menu.addSeparator();
        laberinto_menu.add(salir_menu_item);
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

        // Mostrar el formulario.
        f.setVisible(true);
    }
}
