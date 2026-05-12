import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import servicios.DocumentosServicio;
import servicios.Util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import modelos.Documento;

public class FrmOrdenamiento extends JFrame {

    private JButton btnOrdenarBurbuja;
    private JButton btnOrdenarRapido;
    private JButton btnOrdenarInsercion;
    private JToolBar tbOrdenamiento;
    private JComboBox<String> cmbCriterio;
    private JTextField txtTiempo;
    private JButton btnBuscar;
    private JTextField txtBuscar;
    private JButton btnOrdenarMezcla;

    private JTable tblDocumentos;

    private final AtomicBoolean ejecutando = new AtomicBoolean(false);

    public FrmOrdenamiento() {

        tbOrdenamiento = new JToolBar();
        btnOrdenarBurbuja = new JButton();
        btnOrdenarRapido = new JButton();
        btnOrdenarInsercion = new JButton();
        cmbCriterio = new JComboBox<>();
        txtTiempo = new JTextField();
        btnOrdenarMezcla = new JButton();

        btnBuscar = new JButton();
        txtBuscar = new JTextField();

        tblDocumentos = new JTable();
        var dtm = new DefaultTableModel(null, DocumentosServicio.getEncabezados());
        tblDocumentos.setModel(dtm);
        JScrollPane spDocumentos = new JScrollPane(tblDocumentos);

        setSize(600, 400);
        setTitle("Ordenamiento Documentos");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        btnOrdenarBurbuja.setIcon(new ImageIcon(getClass().getResource("/iconos/Ordenar.png")));
        btnOrdenarBurbuja.setToolTipText("Ordenar Burbuja");
        btnOrdenarBurbuja.addActionListener(evt -> {
            btnOrdenarBurbujaClick(evt);
        });
        tbOrdenamiento.add(btnOrdenarBurbuja);

        btnOrdenarRapido.setIcon(new ImageIcon(getClass().getResource("/iconos/OrdenarRapido.png")));
        btnOrdenarRapido.setToolTipText("Ordenar Rapido");
        btnOrdenarRapido.addActionListener(evt -> {
            btnOrdenarRapidoClick(evt);
        });
        tbOrdenamiento.add(btnOrdenarRapido);

        btnOrdenarInsercion.setIcon(new ImageIcon(getClass().getResource("/iconos/Ordenar.png")));
        btnOrdenarInsercion.setToolTipText("Ordenar Insercion");
        btnOrdenarInsercion.addActionListener(evt -> {
            btnOrdenarInsercionClick(evt);
        });
        tbOrdenamiento.add(btnOrdenarInsercion);

        btnOrdenarMezcla.setIcon(new ImageIcon(getClass().getResource("/iconos/Ordenar.png")));
        btnOrdenarMezcla.setToolTipText("Ordenar Mezcla");
        btnOrdenarMezcla.addActionListener(evt -> {
            btnOrdenarMezclaClick(evt);
        });
        tbOrdenamiento.add(btnOrdenarMezcla);

        cmbCriterio.setModel(new DefaultComboBoxModel<>(
                new String[] { "Nombre Completo, Tipo de Documento", "Tipo de Documento, Nombre Completo" }));
        tbOrdenamiento.add(cmbCriterio);
        tbOrdenamiento.add(txtTiempo);

        btnBuscar.setIcon(new ImageIcon(getClass().getResource("/iconos/Buscar.png")));
        btnBuscar.setToolTipText("Buscar");
        btnBuscar.addActionListener(evt -> {
            btnBuscarClick(evt);
        });
        tbOrdenamiento.add(btnBuscar);
        tbOrdenamiento.add(txtBuscar);

        getContentPane().add(tbOrdenamiento, BorderLayout.NORTH);
        getContentPane().add(spDocumentos, BorderLayout.CENTER);

        cargarDatos();

    }

    private void cargarDatos() {
        String nombreArchivo = System.getProperty("user.dir")
                + "/src/datos/Datos.csv";
        DocumentosServicio.cargar(nombreArchivo);
        DocumentosServicio.mostrar(tblDocumentos);
    }

    private void ejecutarOrdenamiento(int criterio, Runnable algoritmo) {
        if (cmbCriterio.getSelectedIndex() < 0) {
            return;
        }
        if (!ejecutando.compareAndSet(false, true)) {
            return;
        }
        Util.iniciarCronometro();

        // Cronometro en vivo
        new Thread(() -> {
            while (ejecutando.get()) {
                Util.pausarMilisegundos(100);
                SwingUtilities.invokeLater(() -> txtTiempo.setText(Util.getTextoTiempoCronometro()));
            }
        }).start();

        // Ordenamiento
        new Thread(() -> {
            algoritmo.run();
            ejecutando.set(false);
            SwingUtilities.invokeLater(() -> DocumentosServicio.mostrar(tblDocumentos));
        }).start();
    }

    private void btnOrdenarInsercionClick(ActionEvent evt) {
        ejecutarOrdenamiento(cmbCriterio.getSelectedIndex(),
                () -> DocumentosServicio.ordenarInsercion(cmbCriterio.getSelectedIndex()));
    }

    private void btnOrdenarBurbujaClick(ActionEvent evt) {
        ejecutarOrdenamiento(cmbCriterio.getSelectedIndex(),
                () -> DocumentosServicio.ordenarBurbuja(cmbCriterio.getSelectedIndex()));
    }

    private void btnOrdenarRapidoClick(ActionEvent evt) {
        ejecutarOrdenamiento(cmbCriterio.getSelectedIndex(),
                () -> DocumentosServicio.ordenarRapido(cmbCriterio.getSelectedIndex()));
    }

    private void btnOrdenarMezclaClick(ActionEvent evt) {
        ejecutarOrdenamiento(cmbCriterio.getSelectedIndex(),
                () -> DocumentosServicio.ordenarMezcla(cmbCriterio.getSelectedIndex()));
    }

    private void btnBuscarClick(ActionEvent evt) {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese texto para buscar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Documento> resultados = DocumentosServicio.buscar(texto);
        if (resultados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontraron resultados.", "Informacion",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        var datos = resultados.stream()
                .map(d -> new String[] {
                        d.getApellido1(),
                        d.getApellido2(),
                        d.getNombre(),
                        d.getDocumento()
                })
                .collect(Collectors.toList())
                .toArray(String[][]::new);
        var encabezadosBusqueda = new String[] { "Primer Apellido", "Segundo Apellido", "Nombres", "Documento" };
        var dtm = new DefaultTableModel(datos, encabezadosBusqueda);
        tblDocumentos.setModel(dtm);
    }

}