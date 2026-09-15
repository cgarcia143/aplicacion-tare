import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana de solo lectura para visualizar los archivos generados. No solicita
 * datos al usuario y no declara metodo {@code main}.
 *
 * @author Cristian
 * @version 1.0
 */
public class GenerationView extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JLabel statusLabel;
    private final JTextArea salesPreviewArea;

    /**
     * Muestra la vista en el hilo de Swing despues de generar los archivos.
     *
     * @param successful {@code true} si la generacion termino bien
     * @param statusMessage mensaje de exito o error
     */
    public static void display(final boolean successful, final String statusMessage) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                applyLookAndFeel();
                GenerationView view = new GenerationView(successful, statusMessage);
                view.setVisible(true);
            }
        });
    }

    private GenerationView(boolean successful, String statusMessage) {
        super("Vista de generacion - Modulo de ventas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(920, 580));
        setLocationRelativeTo(null);

        statusLabel = new JLabel("  Archivos en la carpeta del proyecto. Cierre la ventana para salir.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(245, 247, 250));

        salesPreviewArea = new JTextArea();
        salesPreviewArea.setEditable(false);
        salesPreviewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.add(buildHeader(successful, statusMessage), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Vendedores", buildTablePanel(loadSalesmenRows(),
                new String[] { "Tipo documento", "Numero", "Nombres", "Apellidos" }));
        tabs.addTab("Productos", buildTablePanel(loadProductRows(),
                new String[] { "ID", "Nombre", "Precio por unidad" }));
        tabs.addTab("Archivos de ventas", buildSalesFilesPanel());
        tabs.addTab("Reportes", buildReportsPanel());

        rootPanel.add(tabs, BorderLayout.CENTER);
        rootPanel.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private JPanel buildHeader(boolean successful, String statusMessage) {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(new Color(20, 55, 95));
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Generacion de archivos de entrada");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel subtitle = new JLabel(statusMessage == null ? "" : statusMessage);
        subtitle.setForeground(successful ? new Color(180, 230, 190) : new Color(255, 190, 180));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        header.add(title);
        header.add(subtitle);
        return header;
    }

    private JScrollPane buildTablePanel(String[][] rows, String[] columns) {
        DefaultTableModel model = new DefaultTableModel(rows, columns) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        return new JScrollPane(table);
    }

    private JPanel buildSalesFilesPanel() {
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        File[] salesFiles = listSalesFiles();
        if (salesFiles != null) {
            for (int index = 0; index < salesFiles.length; index++) {
                listModel.addElement(salesFiles[index].getName());
            }
        }

        final JList<String> fileList = new JList<String>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    showSalesFile(fileList.getSelectedValue());
                }
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(fileList), new JScrollPane(salesPreviewArea));
        splitPane.setDividerLocation(280);
        splitPane.setResizeWeight(0.32);

        if (!listModel.isEmpty()) {
            fileList.setSelectedIndex(0);
        } else {
            salesPreviewArea.setText("No hay archivos de ventas para mostrar.");
        }

        JPanel panel = new JPanel(new BorderLayout());
        JLabel hint = new JLabel("Seleccione un archivo a la izquierda para ver su contenido.");
        hint.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        panel.add(hint, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReportsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(buildReportBox("Vendedores (mejor a peor)", AppConstants.SALESMAN_REPORT_FILE));
        panel.add(buildReportBox("Productos (por cantidad vendida)", AppConstants.PRODUCT_REPORT_FILE));
        return panel;
    }

    private JPanel buildReportBox(String title, String fileName) {
        JPanel box = new JPanel(new BorderLayout());
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setText(readFileOrMessage(fileName,
                "Aun no existe " + fileName + ".\nEjecute la clase main para crear los reportes."));
        box.add(label, BorderLayout.NORTH);
        box.add(new JScrollPane(area), BorderLayout.CENTER);
        return box;
    }

    private void showSalesFile(String fileName) {
        if (fileName == null) {
            return;
        }
        salesPreviewArea.setText(readFileOrMessage(fileName, "No se pudo leer " + fileName));
        salesPreviewArea.setCaretPosition(0);
        if (statusLabel != null) {
            statusLabel.setText("  Mostrando " + fileName);
        }
    }

    private String[][] loadSalesmenRows() {
        List<String> lines = readLinesQuiet(AppConstants.SALESMEN_INFO_FILE);
        List<String[]> rows = new ArrayList<String[]>();
        for (int index = 0; index < lines.size(); index++) {
            String[] parts = lines.get(index).split(AppConstants.DELIMITER, -1);
            if (parts.length >= 4) {
                rows.add(new String[] { parts[0], parts[1], parts[2], parts[3] });
            }
        }
        return toArray(rows, 4);
    }

    private String[][] loadProductRows() {
        List<String> lines = readLinesQuiet(AppConstants.PRODUCTS_FILE);
        List<String[]> rows = new ArrayList<String[]>();
        for (int index = 0; index < lines.size(); index++) {
            String[] parts = lines.get(index).split(AppConstants.DELIMITER, -1);
            if (parts.length >= 3) {
                rows.add(new String[] { parts[0], parts[1], parts[2] });
            }
        }
        return toArray(rows, 3);
    }

    private File[] listSalesFiles() {
        File folder = new File(".");
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File directory, String fileName) {
                return fileName.startsWith(AppConstants.SALES_FILE_PREFIX)
                        && fileName.endsWith(AppConstants.SALES_FILE_EXTENSION);
            }
        });
        return files == null ? new File[0] : files;
    }

    private List<String> readLinesQuiet(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return new ArrayList<String>();
        }
        try {
            return PlainFileUtils.readLines(file);
        } catch (IOException exception) {
            return new ArrayList<String>();
        }
    }

    private String readFileOrMessage(String fileName, String fallback) {
        File file = new File(fileName);
        if (!file.exists()) {
            return fallback;
        }
        try {
            List<String> lines = PlainFileUtils.readLines(file);
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < lines.size(); index++) {
                builder.append(lines.get(index)).append(System.lineSeparator());
            }
            return builder.toString();
        } catch (IOException exception) {
            return fallback;
        }
    }

    private String[][] toArray(List<String[]> rows, int columnCount) {
        String[][] result = new String[rows.size()][columnCount];
        for (int index = 0; index < rows.size(); index++) {
            result[index] = rows.get(index);
        }
        return result;
    }

    private static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si el look and feel del sistema no carga, se usa el predeterminado.
        }
    }
}
