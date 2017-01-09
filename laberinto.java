import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class Form extends JFrame {
    Form(String titulo, int w, int h) {
        super(titulo);
        setSize(w, h);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

enum estado {
    VACIO,
    OBSTACULO,
    ENTRADA,
    SALIDA
};

class Panel extends JPanel {
    private int w, h, fila, columna;
    private boolean entrada, salida;
    private final int columnas = 16;
    private final int filas    = columnas;
    private final estado cuadricula[][] = new estado[columnas][filas];

    Panel() {
        super();
        int i, j;

        // Hacer que la cuadrilla esté en estado vacío al inicio.
        for (i = 0; i < columnas; i++) {
            for (j = 0; j < filas; j++) {
                cuadricula[i][j] = estado.VACIO;
            }
        }
        entrada = salida = false;
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
                g.setColor(new Color(50, 205, 255));
                if (cuadricula[i][j] == estado.OBSTACULO) {
                    g.fillRect((columna * i) + 1, (fila * j) + 1,
                               columna - 1, fila - 1);
                }
            }
        }
    }

    // Función para cambiar el estado de los bordes a `OBSTACULO`.
    private void bordes() {
        int i;

        for (i = 0; i < columnas; i++) {
            cuadricula[i][0] = estado.OBSTACULO;
            cuadricula[i][filas-1] = estado.OBSTACULO;
        }

        for (i = 0; i < filas; i++) {
            cuadricula[0][i] = estado.OBSTACULO;
            cuadricula[columnas-1][i] = estado.OBSTACULO;
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
                g.setColor(new Color(220, 220, 220));
                g.drawRect(columna * i, fila * j, columna, fila);
                g.setColor(Color.white);
                g.fillRect((columna * i) + 1, (fila * j) + 1,
                           columna - 1, fila - 1);
            }
        }
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

        // Cuando se haga clic izquierdo.
        if (SwingUtilities.isLeftMouseButton(ev)) {
            cuadricula[c][f] = estado.OBSTACULO;
            repaint();
        }

        // Cuando se haga clic derecho.
        if (SwingUtilities.isRightMouseButton(ev)) {
            cuadricula[c][f] = estado.VACIO;
            repaint();
        }
    }
}

class Laberinto {
    public static void main(String[] args) {
        // Dimensión del formulario.
        final int w = 600;
        final int h = 600;

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
        Panel panel = new Panel();
        f.add(panel);

        // Eventos.
        MouseListener panel_mouse = new MouseAdapter() {
                public void mousePressed(MouseEvent ev) {
                    panel.clicked(ev);
                }
            };

        // Agregar evento al panel principal.
        panel.addMouseListener(panel_mouse);

        // Mostrar el formulario.
        f.setVisible(true);
    }
}
