import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class SimpleNotepad extends JFrame {
    private JTextArea textArea;
    private JFileChooser fileChooser;
    private File currentFile;
    private JLabel statusBar;

    public SimpleNotepad() {
        fileChooser = new JFileChooser();
        initUI();
    }

    private void initUI() {
        setTitle("Simple Notepad - Untitled");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(createMenuItem("New", KeyEvent.VK_N, e -> newFile()));
        fileMenu.add(createMenuItem("Open", KeyEvent.VK_O, e -> openFile()));
        fileMenu.add(createMenuItem("Save", KeyEvent.VK_S, e -> saveFile()));
        fileMenu.add(createMenuItem("Save As", KeyEvent.VK_A, e -> saveAsFile()));
        fileMenu.addSeparator();
        fileMenu.add(createMenuItem("Exit", KeyEvent.VK_X, e -> System.exit(0)));
        menuBar.add(fileMenu);

        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.add(createMenuItem("Cut", KeyEvent.VK_X, e -> textArea.cut()));
        editMenu.add(createMenuItem("Copy", KeyEvent.VK_C, e -> textArea.copy()));
        editMenu.add(createMenuItem("Paste", KeyEvent.VK_V, e -> textArea.paste()));
        editMenu.addSeparator();
        editMenu.add(createMenuItem("Select All", KeyEvent.VK_A, e -> textArea.selectAll()));
        menuBar.add(editMenu);

        // Format Menu
        JMenu formatMenu = new JMenu("Format");
        formatMenu.add(createCheckMenuItem("Word Wrap", true, e -> toggleWordWrap()));
        formatMenu.add(createMenuItem("Font", KeyEvent.VK_F, e -> changeFont()));
        menuBar.add(formatMenu);

        setJMenuBar(menuBar);

        // Text Area
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Status Bar
        statusBar = new JLabel("Ready | Ln 1, Col 1");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setPreferredSize(new Dimension(0, 20));

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        add(mainPanel);

        // Listeners
        textArea.addCaretListener(e -> updateStatusBar());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (hasUnsavedChanges()) {
                    int choice = JOptionPane.showConfirmDialog(null, 
                        "Save changes before closing?", "Unsaved Changes", 
                        JOptionPane.YES_NO_CANCEL_OPTION);
                    if (choice == JOptionPane.YES_OPTION) saveFile();
                    else if (choice == JOptionPane.CANCEL_OPTION) return;
                }
                System.exit(0);
            }
        });
    }

    private JMenuItem createMenuItem(String text, int mnemonic, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.setMnemonic(mnemonic);
        item.addActionListener(listener);
        return item;
    }

    private JCheckBoxMenuItem createCheckMenuItem(String text, boolean selected, ActionListener listener) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(text, selected);
        item.addActionListener(listener);
        return item;
    }

    // File Operations
    private void newFile() {
        if (hasUnsavedChanges()) {
            int choice = JOptionPane.showConfirmDialog(this, "Save before creating new file?", 
                "Unsaved Changes", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) saveFile();
        }
        textArea.setText("");
        currentFile = null;
        setTitle("Simple Notepad - Untitled");
    }

    private void openFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentFile = fileChooser.getSelectedFile();
                BufferedReader reader = new BufferedReader(new FileReader(currentFile));
                textArea.read(reader, null);
                reader.close();
                setTitle("Simple Notepad - " + currentFile.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error opening file!");
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveAsFile();
            return;
        }
        try {
            PrintWriter writer = new PrintWriter(currentFile);
            textArea.write(writer);
            writer.close();
            setTitle("Simple Notepad - " + currentFile.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving file!");
        }
    }

    private void saveAsFile() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            saveFile();
        }
    }

    private boolean hasUnsavedChanges() {
        return textArea.getText().trim().length() > 0;
    }

    private void toggleWordWrap() {
        textArea.setLineWrap(!textArea.getLineWrap());
        textArea.setWrapStyleWord(!textArea.getWrapStyleWord());
    }

    private void changeFont() {
        Object[] options = {"Monospaced 12", "Monospaced 14", "Arial 12", "Arial 14", "Cancel"};
        Object selection = JOptionPane.showOptionDialog(this, 
            "Choose Font:", "Font", JOptionPane.DEFAULT_OPTION, 
            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        
        if (selection != null && selection != options[4]) {
            String[] parts = ((String) selection).split(" ");
            String family = parts[0];
            int size = Integer.parseInt(parts[1]);
            textArea.setFont(new Font(family, Font.PLAIN, size));
        }
    }

    private void updateStatusBar() {
        try {
            int pos = textArea.getCaretPosition();
            int line = 1;
            int col = 1;
            
            String text = textArea.getText();
            for (int i = 0; i < pos; i++) {
                if (text.charAt(i) == '\n') {
                    line++;
                    col = 1;
                } else {
                    col++;
                }
            }
            statusBar.setText("Ready | Ln " + line + ", Col " + col);
        } catch (Exception e) {
            statusBar.setText("Ready");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new SimpleNotepad().setVisible(true);
        });
    }
}
