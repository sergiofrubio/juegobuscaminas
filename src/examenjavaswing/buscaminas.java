package examenjavaswing;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.*;

public class buscaminas {

    int minas, puntuacion, id;
    JButton[] button;
    Set<Integer> indicesBombas = new HashSet<>();

    public buscaminas() {
        JFrame frame = new JFrame("Buscaminas");
        JPanel contentPane = (JPanel) frame.getContentPane();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("Menú");
        JMenuItem i1, i2;
        i1 = new JMenuItem("Guardar");
        i2 = new JMenuItem("Salir");

        i2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        menu.add(i1);
        menu.add(i2);
        mb.add(menu);

        // FORMULARIO
        JPanel formulario = new JPanel();
        formulario.setLayout(new GridLayout(3, 1));
        JTextField textfieldid = new JTextField("ID", 10);
        JTextField numerominas = new JTextField("Numero de minas", 10);
        JButton enviar = new JButton("Empezar juego");

        formulario.add(textfieldid);
        formulario.add(numerominas);
        formulario.add(enviar);

        // CÓDIGO BUSCAMINAS
        JPanel panelbuscaminas = new JPanel(new GridLayout(10, 10));

        button = new JButton[100];
        for (int i = 0; i < 100; i++) {
            button[i] = new JButton();
            button[i].setName(Integer.toString(i)); // Asigna el nombre al botón con el índice
            button[i].addActionListener(new BotonListener());
            panelbuscaminas.add(button[i]);
        }

        enviar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                id = Integer.parseInt(textfieldid.getText());
                minas = Integer.parseInt(numerominas.getText());

                if (id > 0 && minas > 0) {
                    // Comprobar la existencia del ID
                    if (comprobarExistenciaId(id)) {
                        // El ID existe, puedes continuar con el juego
                        asignarMinas(minas);
                    } else {
                        JOptionPane.showMessageDialog(null, "El ID no existe en la base de datos.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Por favor, completa todos los campos.");
                }
            }
        });

        contentPane.add(panelbuscaminas);
        panel.add(mb, BorderLayout.NORTH);
        panel.add(formulario, BorderLayout.WEST);
        panel.add(panelbuscaminas, BorderLayout.EAST);
        contentPane.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(480, 400);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private void asignarMinas(int numerobombas) {
        Random random = new Random();
        for (int i = 0; i < numerobombas; i++) {
            int indice = random.nextInt(100); // 100 es el número total de botones
            while (indicesBombas.contains(indice)) {
                indice = random.nextInt(100);
            }
            indicesBombas.add(indice);
        }
    }

    private class BotonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JButton boton = (JButton) e.getSource();
            puntuacion = 0;
            mostrarMina(boton);
        }
    }

    private void mostrarMina(JButton button) {
        button.setEnabled(false); // Deshabilita el botón clicado

        // Verifica si este botón contiene una mina
        if (contieneMina(button)) {
            // Muestra todas las minas y deshabilita los botones
            for (int i = 0; i < 100; i++) {
                if (contieneMina(this.button[i])) {
                    this.button[i].setEnabled(false);
                    this.button[i].setText("B");
                }
            }

            // Guarda la partida
            guardarPartida();

            JOptionPane.showMessageDialog(null, "¡Has encontrado una mina! La partida ha finalizado.");
        } else {
            // No es una mina, puedes realizar otras acciones si lo deseas
            puntuacion++;
        }
    }

    private boolean contieneMina(JButton button) {
        int indice = Integer.parseInt(button.getName());
        return indicesBombas.contains(indice);
    }

    private boolean comprobarExistenciaId(int id) {
        boolean existeId = false;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost/buscaminas", "root", "");
            String sql = "SELECT * FROM usuarios WHERE id = ?";

            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setInt(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    existeId = resultSet.next(); // Si existe algún resultado, significa que el ID existe
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al conectar con la base de datos.");
        }

        return existeId;
    }

    private void guardarPartida() {
        JOptionPane.showMessageDialog(null, "La partida ha finalizado.");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost/buscaminas", "root", "");
            //Statement sentencia = conexion.createStatement();
            String sql = "UPDATE usuarios SET puntuacion = '" + puntuacion + "' WHERE id='" + id + "' ";
            PreparedStatement statement = conexion.prepareStatement(sql);
            statement.executeUpdate();
        } catch (ClassNotFoundException | SQLException cn) {
            cn.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new buscaminas();
            }
        });
    }
}
