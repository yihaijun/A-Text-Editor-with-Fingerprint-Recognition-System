package TextEditor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.StyledEditorKit;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class GUI extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    public static ArrayList<Fingerprint> fingerprintList = new ArrayList<Fingerprint>();
    private static int tabNumber = 0;
    private static ArrayList<File> fileList = new ArrayList<File>();
    private static ArrayList<Boolean> saveBoolList = new ArrayList<Boolean>();
    private static ArrayList<UndoManager> undoManagerList = new ArrayList<UndoManager>();
    private final JMenuBar menuBar;
    private final JTabbedPane pane;
    private final JMenu menuFile, menuEdit, menuFind, menuAbout;
    private final JMenuItem newFile, openFile, saveFile, saveFileAs, saveAllFile, exit,
            undo, redo, cut, copy, paste, delete, selectAll,
            find,
            about;
    private final JToolBar mainToolbar;
    private final JButton newButton, openButton, saveButton, saveAllButton, fingerPrintButton,
            deleteButton, boldButton, italicButton, underlineButton,
            findButton;
    private final JComboBox<String> comboBoxFont, comboBoxSize, comboBoxColor;
    //private final JComboBox fontName;
    private final Action selectAllAction;
    private final ImageIcon newIcon = new ImageIcon("icons/new.png");
    private final ImageIcon openIcon = new ImageIcon("icons/open.png");
    private final ImageIcon saveIcon = new ImageIcon("icons/save.png");
    private final ImageIcon saveAllIcon = new ImageIcon("icons/saveAll.png");
    private final ImageIcon fpIcon = new ImageIcon("icons/fp.png");
    private final ImageIcon boldIcon = new ImageIcon("icons/bold.png");
    private final ImageIcon italicIcon = new ImageIcon("icons/italic.png");
    private final ImageIcon underlineIcon = new ImageIcon("icons/underline.png");
    private final ImageIcon deleteIcon = new ImageIcon("icons/delete.png");
    private final ImageIcon findIcon = new ImageIcon("icons/find.png");
    JFileChooser open = null;
    SerialFile serialFile = null;
    private JTextPane textArea;
    private String fontType = "Calibri";
    private int fontSize = 16;
    private OnValueChangedListener documentListener = new OnValueChangedListener();

    GUI() {
        setSize(800, 600);
        setTitle(TextEditor.NAME);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 200));

        pane = new JTabbedPane();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pane);

        /////////////////////////////////////////////////////////////////////////////

        menuFile = new JMenu("File");
        menuEdit = new JMenu("Edit");
        menuFind = new JMenu("Search");
        menuAbout = new JMenu("About");

        newFile = new JMenuItem("New");
        openFile = new JMenuItem("Open");
        saveFile = new JMenuItem("Save");
        saveFileAs = new JMenuItem("Save As");
        saveAllFile = new JMenuItem("Save All");
        exit = new JMenuItem("Exit");

        undo = new JMenuItem("Undo");
        redo = new JMenuItem("Redo");
        delete = new JMenuItem("Delete");

        find = new JMenuItem("Find");

        about = new JMenuItem("About TextEditor");

        ///////////////////////////////////////////////////////////////////////////////

        menuBar = new JMenuBar();
        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuFind);
        menuBar.add(menuAbout);
        this.setJMenuBar(menuBar);

        selectAllAction = new SelectAllAction("Select All", "Select all text", KeyEvent.VK_A);

        newFile.addActionListener(this);
        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        menuFile.add(newFile);

        openFile.addActionListener(this);
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        menuFile.add(openFile);

        menuFile.addSeparator();

        saveFile.addActionListener(this);
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        menuFile.add(saveFile);

        saveFileAs.addActionListener(this);
        menuFile.add(saveFileAs);

        saveAllFile.addActionListener(this);
        menuFile.add(saveAllFile);

        exit.addActionListener(this);
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
        menuFile.add(exit);

        undo.addActionListener(this);
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        menuEdit.add(undo);

        redo.addActionListener(this);
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
        menuEdit.add(redo);
        menuEdit.addSeparator();

        cut = new JMenuItem(new DefaultEditorKit.CutAction());
        cut.setText("Cut");
        cut.setToolTipText("Cut");
        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
        menuEdit.add(cut);

        copy = new JMenuItem(new DefaultEditorKit.CopyAction());
        copy.setText("Copy");
        copy.setToolTipText("Copy");
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        menuEdit.add(copy);

        paste = new JMenuItem(new DefaultEditorKit.PasteAction());
        paste.setText("Paste");
        paste.setToolTipText("Paste");
        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
        menuEdit.add(paste);

        menuEdit.addSeparator();

        selectAll = new JMenuItem(selectAllAction);
        selectAll.setText("Select All");
        selectAll.setToolTipText("Select All");
        selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        menuEdit.add(selectAll);

        delete.addActionListener(this);
        delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));
        menuEdit.add(delete);

        find.addActionListener(this);
        find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
        menuFind.add(find);

        about.addActionListener(this);
        about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        menuAbout.add(about);

        //////////////////////////////////////////////////////////////////////////////////////

        mainToolbar = new JToolBar();
        this.add(mainToolbar, BorderLayout.NORTH);

        newButton = new JButton(newIcon);
        newButton.setToolTipText("New");
        newButton.addActionListener(this);
        mainToolbar.add(newButton);

        openButton = new JButton(openIcon);
        openButton.setToolTipText("Open");
        openButton.addActionListener(this);
        mainToolbar.add(openButton);

        saveButton = new JButton(saveIcon);
        saveButton.setToolTipText("Save");
        saveButton.addActionListener(this);
        mainToolbar.add(saveButton);

        saveAllButton = new JButton(saveAllIcon);
        saveAllButton.setToolTipText("Save All");
        saveAllButton.addActionListener(this);
        mainToolbar.add(saveAllButton);
        mainToolbar.addSeparator();

        fingerPrintButton = new JButton(fpIcon);
        fingerPrintButton.setToolTipText("Add/Delete Fingerprint");
        fingerPrintButton.addActionListener(this);
        mainToolbar.add(fingerPrintButton);
        mainToolbar.addSeparator();

        comboBoxFont = new JComboBox<String>();
        comboBoxFont.setMaximumSize(new Dimension(120, 50));
        comboBoxFont.setModel(new DefaultComboBoxModel<String>(new String[]{
                "Calibri", "Cambria", "Century Gothic", "Consolas", "Courier New",
                "黑体", "楷体", "宋体", "仿宋", "微软雅黑"
        }));
        comboBoxFont.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Object sel = e.getItem();
                Action action = null;
                if (sel.equals("Calibri")) {
                    action = new StyledEditorKit.FontFamilyAction("Calibri", "Calibri");
                } else if (sel.equals("Cambria")) {
                    action = new StyledEditorKit.FontFamilyAction("Cambria", "Cambria");
                } else if (sel.equals("Century Gothic")) {
                    action = new StyledEditorKit.FontFamilyAction("Century Gothic", "Century Gothic");
                } else if (sel.equals("Consolas")) {
                    action = new StyledEditorKit.FontFamilyAction("Consolas", "Consolas");
                } else if (sel.equals("Courier New")) {
                    action = new StyledEditorKit.FontFamilyAction("Courier New", "Courier New");
                } else if (sel.equals("黑体")) {
                    action = new StyledEditorKit.FontFamilyAction("黑体", "黑体");
                } else if (sel.equals("楷体")) {
                    action = new StyledEditorKit.FontFamilyAction("楷体", "楷体");
                } else if (sel.equals("宋体")) {
                    action = new StyledEditorKit.FontFamilyAction("宋体", "宋体");
                } else if (sel.equals("仿宋")) {
                    action = new StyledEditorKit.FontFamilyAction("仿宋", "仿宋");
                } else if (sel.equals("微软雅黑")) {
                    action = new StyledEditorKit.FontFamilyAction("微软雅黑", "微软雅黑");
                }
                comboBoxFont.setAction(action);
            }
        });
        comboBoxFont.setSelectedIndex(0);
        mainToolbar.add(comboBoxFont);
        mainToolbar.addSeparator();

        comboBoxSize = new JComboBox<String>();
        comboBoxSize.setMaximumSize(new Dimension(60, 50));
        comboBoxSize.setModel(new DefaultComboBoxModel<String>(new String[]{
                "8", "9", "10", "11", "12", "14", "16", "18", "20", "24", "28", "32", "36", "40"
        }));
        comboBoxSize.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Object size = e.getItem();
                comboBoxSize.setAction(new StyledEditorKit.FontSizeAction(size.toString(), Integer.parseInt(size.toString())));
            }
        });
        comboBoxSize.setSelectedIndex(6);
        mainToolbar.add(comboBoxSize);
        mainToolbar.addSeparator();

        comboBoxColor = new JComboBox<String>();
        comboBoxColor.setMaximumSize(new Dimension(80, 50));
        comboBoxColor.setModel(new DefaultComboBoxModel<String>(new String[]{
                "Black", "Blue", "Cyan", "Gray", "Green",
                "Magenta", "Orange", "Pink", "Red", "Yellow"
        }));
        comboBoxColor.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Object color = e.getItem();
                Action action = null;
                if (color.equals("Black")) {
                    action = new StyledEditorKit.ForegroundAction("Black", Color.BLACK);
                } else if (color.equals("Blue")) {
                    action = new StyledEditorKit.ForegroundAction("Blue", Color.BLUE);
                } else if (color.equals("Cyan")) {
                    action = new StyledEditorKit.ForegroundAction("Cyan", Color.CYAN);
                } else if (color.equals("Gray")) {
                    action = new StyledEditorKit.ForegroundAction("Gray", Color.GRAY);
                } else if (color.equals("Green")) {
                    action = new StyledEditorKit.ForegroundAction("Green", Color.GREEN);
                } else if (color.equals("Magenta")) {
                    action = new StyledEditorKit.ForegroundAction("Magenta", Color.MAGENTA);
                } else if (color.equals("Orange")) {
                    action = new StyledEditorKit.ForegroundAction("Orange", Color.ORANGE);
                } else if (color.equals("Pink")) {
                    action = new StyledEditorKit.ForegroundAction("Pink", Color.PINK);
                } else if (color.equals("Red")) {
                    action = new StyledEditorKit.ForegroundAction("Red", Color.RED);
                } else if (color.equals("Yellow")) {
                    action = new StyledEditorKit.ForegroundAction("Yellow", Color.YELLOW);
                }
                comboBoxColor.setAction(action);
            }
        });
        comboBoxColor.setSelectedIndex(0);
        mainToolbar.add(comboBoxColor);
        mainToolbar.addSeparator();

        boldButton = new JButton(new StyledEditorKit.BoldAction());
        boldButton.setIcon(boldIcon);
        boldButton.setText("");
        boldButton.setToolTipText("Bold");
        boldButton.addActionListener(this);
        mainToolbar.add(boldButton);

        italicButton = new JButton(new StyledEditorKit.ItalicAction());
        italicButton.setIcon(italicIcon);
        italicButton.setText("");
        italicButton.setToolTipText("Italic");
        italicButton.addActionListener(this);
        mainToolbar.add(italicButton);

        underlineButton = new JButton(new StyledEditorKit.UnderlineAction());
        underlineButton.setIcon(underlineIcon);
        underlineButton.setText("");
        underlineButton.setToolTipText("Underline");
        underlineButton.addActionListener(this);
        mainToolbar.add(underlineButton);
        mainToolbar.addSeparator();

        deleteButton = new JButton(deleteIcon);
        deleteButton.setToolTipText("Delete");
        deleteButton.addActionListener(this);
        mainToolbar.add(deleteButton);
        mainToolbar.addSeparator();

        findButton = new JButton(findIcon);
        findButton.setToolTipText("Find");
        findButton.addActionListener(this);
        mainToolbar.add(findButton);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitAction();
                super.windowClosing(e);
            }
        });
    }

    public static void closeTab() {
        tabNumber--;
    }

    public static void newTab() {
        tabNumber++;
    }

    public static ArrayList<Boolean> getSaveBoolList() {
        return GUI.saveBoolList;
    }

    public static ArrayList<File> getFileList() {
        return GUI.fileList;
    }

    public static ArrayList<UndoManager> getUndoManagerList() {
        return GUI.undoManagerList;
    }

    public JTabbedPane getPane() {
        return pane;
    }

    private JTextPane getEditor() {
        int index = pane.getSelectedIndex();
        if (index == -1)
            return null;
        else {
            JScrollPane tmp1 = (JScrollPane) pane.getComponentAt(index);
            return (JTextPane) tmp1.getViewport().getView();
        }
    }

    private int getTabNumber() {
        return tabNumber;
    }

    private void newFileAction() {
        textArea = new JTextPane();
        textArea.setFont(new Font(fontType, Font.PLAIN, fontSize));
        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (e.isPopupTrigger()) {
                        JPopupMenu pop = new JPopupMenu();
                        JMenuItem undo = new JMenuItem("Undo");
                        undo.addMouseListener(new MouseAdapter() {
                            public void mouseReleased(MouseEvent e) {
                                undoAction();
                            }
                        });
                        pop.add(undo);

                        JMenuItem redo = new JMenuItem("Redo");
                        redo.addMouseListener(new MouseAdapter() {
                            public void mouseReleased(MouseEvent e) {
                                redoAction();
                            }
                        });
                        pop.add(redo);
                        pop.addSeparator();

                        JMenuItem cut = new JMenuItem(new DefaultEditorKit.CutAction());
                        cut.setText("Cut");
                        pop.add(cut);

                        JMenuItem copy = new JMenuItem(new DefaultEditorKit.CopyAction());
                        copy.setText("Copy");
                        pop.add(copy);

                        JMenuItem paste = new JMenuItem(new DefaultEditorKit.PasteAction());
                        paste.setText("Paste");
                        pop.add(paste);
                        pop.addSeparator();

                        JMenuItem selectAll = new JMenuItem(selectAllAction);
                        selectAll.setText("Select All");
                        pop.add(selectAll);

                        JMenuItem delete = new JMenuItem("Delete");
                        delete.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                deleteAction();
                            }
                        });
                        pop.add(delete);

                        pop.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        pane.addTab("*" + TextEditor.NEWTAB + (getTabNumber() + 1), scrollPane);
        ButtonTabComponent buttonTabComponent = new ButtonTabComponent(pane, "*" + TextEditor.NEWTAB + (getTabNumber() + 1));
        pane.setTabComponentAt(getTabNumber(), buttonTabComponent);
        GUI.newTab();
        saveBoolList.add(false);
        fileList.add(null);
        fingerprintList.add(null);
        getUndoManagerList().add(new UndoManager());
        textArea.getStyledDocument().addUndoableEditListener(getUndoManagerList().get(getUndoManagerList().size() - 1));
        textArea.getStyledDocument().addDocumentListener(documentListener);
    }

    private void openFileAction() {
        open = new JFileChooser();
        open.setDialogTitle("Open");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("TextEditor File (*.edt)", "edt");
        open.setFileFilter(filter);
        int option = open.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            for (int i = 0; i < fileList.size(); ++i) {
                if (fileList.get(i) != null && fileList.get(i).getAbsolutePath().equalsIgnoreCase(open.getSelectedFile().getAbsolutePath())) {
                    JOptionPane.showMessageDialog(this, "This file has been opened!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            if (!open.getSelectedFile().exists()) {
                JOptionPane.showMessageDialog(this, "File does not exist !", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            serialFile = new SerialFile(open.getSelectedFile().getAbsolutePath());
            if (serialFile.getFlag()) {
                new FingerprintFrame(FingerprintFrame.FUNCTION_VERIFY, serialFile.getFingerprint());
            } else {
                postOpenFileAction();
            }
        }
    }

    public void postOpenFileAction() {
        newFileAction();
        saveBoolList.set(getTabNumber() - 1, true);
        fileList.set(getTabNumber() - 1, open.getSelectedFile());
        fingerprintList.set(getTabNumber() - 1, serialFile.getFingerprint());
        pane.setTitleAt(getTabNumber() - 1, open.getSelectedFile().getName());
        textArea.setStyledDocument(serialFile.getStyledDocument());
        textArea.getStyledDocument().addUndoableEditListener(getUndoManagerList().get(getUndoManagerList().size() - 1));
        textArea.getStyledDocument().addDocumentListener(documentListener);
        pane.setSelectedIndex(getTabNumber() - 1);
    }

    public void saveFile(int index) {
        saveFileAction(index);
    }

    private void saveFileAction(int index) {
        if (fileList.get(index) == null) {
            saveAsAction(index);
        } else {
            File file = fileList.get(index);
            pane.setTitleAt(index, file.getName());
            JScrollPane tmp1 = (JScrollPane) pane.getComponentAt(index);
            JTextPane tmp2 = (JTextPane) tmp1.getViewport().getView();
            tmp2.getStyledDocument().removeDocumentListener(documentListener);
            tmp2.getStyledDocument().removeUndoableEditListener(getUndoManagerList().get(index));
            if (fingerprintList.get(index) != null) {
                new SerialFile(file.getAbsolutePath(), tmp2.getStyledDocument(), fingerprintList.get(index));
            } else {
                new SerialFile(file.getAbsolutePath(), tmp2.getStyledDocument(), null);
            }
            tmp2.getStyledDocument().addDocumentListener(documentListener);
            tmp2.getStyledDocument().addUndoableEditListener(getUndoManagerList().get(index));
            saveBoolList.set(index, true);
        }
    }

    private void saveAsAction(int index) {
        JFileChooser fileChoose = new JFileChooser();
        fileChoose.setDialogTitle("Save - " + pane.getTitleAt(index).substring(1));
        fileChoose.setSelectedFile(new File(fileChoose.getCurrentDirectory() + "/" + pane.getTitleAt(index).substring(1) + ".edt"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("TextEditor File (*.edt)", "edt");
        fileChoose.setFileFilter(filter);
        int option = fileChoose.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChoose.getSelectedFile();
            if (file.exists()) {
                switch (JOptionPane.showConfirmDialog(this, "File already exists!\nDo you wish to replace it ?", "Attention", JOptionPane.YES_NO_OPTION)) {
                    case JOptionPane.YES_OPTION:
                        break;
                    case JOptionPane.NO_OPTION:
                        return;
                    default:
                }
            }
            pane.setTitleAt(index, file.getName());
            textArea.getStyledDocument().removeDocumentListener(documentListener);
            textArea.getStyledDocument().removeUndoableEditListener(getUndoManagerList().get(index));
            if (fingerprintList.get(index) != null) {
                new SerialFile(file.getAbsolutePath(), textArea.getStyledDocument(), fingerprintList.get(index));
            } else {
                new SerialFile(file.getAbsolutePath(), textArea.getStyledDocument(), null);
            }
            textArea.getStyledDocument().addDocumentListener(documentListener);
            textArea.getStyledDocument().addUndoableEditListener(getUndoManagerList().get(index));
            saveBoolList.set(index, true);
            fileList.set(index, file);
        }
    }

    private void saveAllAction() {
        for (int i = 0; i < saveBoolList.size(); ++i) {
            Boolean flag = saveBoolList.get(i);
            if (!flag)
                saveFileAction(i);
        }
    }

    private void exitAction() {
        for (int i = 0; i < saveBoolList.size(); ++i) {
            if (!saveBoolList.get(i)) {
                switch (JOptionPane.showConfirmDialog(this, "Do you want to save " + pane.getTitleAt(i).substring(1) + " ?", "Attention", JOptionPane.YES_NO_CANCEL_OPTION)) {
                    case JOptionPane.YES_OPTION:
                        saveFileAction(i);
                        break;
                    case JOptionPane.NO_OPTION:
                        break;
                    case JOptionPane.CANCEL_OPTION:
                        return;
                    default:
                }
            }
        }
        System.exit(0);
    }

    private void deleteAction() {
        if (textArea != null) {
            int length = textArea.getDocument().getLength();
            try {
                textArea.getDocument().remove(0, length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void undoAction() {
        if (pane.getSelectedIndex() >= 0) {
            if (getUndoManagerList().get(pane.getSelectedIndex()).canUndo()) {
                getUndoManagerList().get(pane.getSelectedIndex()).undo();
            }
        }
    }

    private void redoAction() {
        if (pane.getSelectedIndex() >= 0) {
            if (getUndoManagerList().get(pane.getSelectedIndex()).canRedo()) {
                getUndoManagerList().get(pane.getSelectedIndex()).redo();
            }
        }
    }

    private void fingerPrintAction() {
        if (pane.getSelectedIndex() >= 0) {
            if (fingerprintList.get(pane.getSelectedIndex()) == null) {
                new FingerprintFrame(FingerprintFrame.FUNCTION_ENROLL, null);
            } else {
                switch (JOptionPane.showConfirmDialog(this, "Do you wish to delete the fingerprint ?", "Attention", JOptionPane.YES_NO_OPTION)) {
                    case JOptionPane.YES_OPTION:
                        fingerprintList.set(pane.getSelectedIndex(), null);
                        JOptionPane.showMessageDialog(this, "Fingerprint has been deleted!", "Information", JOptionPane.INFORMATION_MESSAGE);
                        int index = pane.getSelectedIndex();
                        if (index >= 0 && saveBoolList.get(index))
                            pane.setTitleAt(index, "*" + pane.getTitleAt(index));
                        saveBoolList.set(index, false);
                        break;
                    default:
                }
            }
        }
    }

    private void findAction() {
        if (textArea != null) {
            new Find(textArea);
        }
    }

    private void aboutAction() {
        new About().software();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.textArea = this.getEditor();
        if (e.getSource() == newFile || e.getSource() == newButton) {
            newFileAction();
        } else if (e.getSource() == openFile || e.getSource() == openButton) {
            openFileAction();
        } else if (e.getSource() == saveFile || e.getSource() == saveButton) {
            if (pane.getSelectedIndex() >= 0) {
                saveFileAction(pane.getSelectedIndex());
            }
        } else if (e.getSource() == saveFileAs) {
            if (pane.getSelectedIndex() >= 0) {
                saveAsAction(pane.getSelectedIndex());
            }
        } else if (e.getSource() == saveAllFile || e.getSource() == saveAllButton) {
            saveAllAction();
        } else if (e.getSource() == exit) {
            exitAction();
        } else if (e.getSource() == fingerPrintButton) {
            fingerPrintAction();
        } else if (e.getSource() == delete || e.getSource() == deleteButton) {
            deleteAction();
        } else if (e.getSource() == undo) {
            undoAction();
        } else if (e.getSource() == redo) {
            redoAction();
        } else if (e.getSource() == find || e.getSource() == findButton) {
            findAction();
        } else if (e.getSource() == about) {
            aboutAction();
        }
    }

    private class SelectAllAction extends AbstractAction implements Serializable {
        private static final long serialVersionUID = 1L;

        SelectAllAction(String text, String desc, Integer mnemonic) {
            super(text);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (textArea != null) {
                textArea.selectAll();
            }
        }
    }

    private class OnValueChangedListener implements DocumentListener, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public void insertUpdate(DocumentEvent e) {
            int index = pane.getSelectedIndex();
            if (index >= 0 && saveBoolList.get(index))
                pane.setTitleAt(index, "*" + pane.getTitleAt(index));
            saveBoolList.set(index, false);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            int index = pane.getSelectedIndex();
            if (index >= 0 && saveBoolList.get(index))
                pane.setTitleAt(index, "*" + pane.getTitleAt(index));
            saveBoolList.set(index, false);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            int index = pane.getSelectedIndex();
            if (index >= 0 && saveBoolList.get(index))
                pane.setTitleAt(index, "*" + pane.getTitleAt(index));
            saveBoolList.set(index, false);
        }
    }
}
