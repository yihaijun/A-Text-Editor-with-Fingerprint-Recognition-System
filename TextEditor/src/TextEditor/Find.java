package TextEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Find extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    int startIndex = 0;
    int select_start = -1;
    JLabel lab1, lab2;
    JTextField textF, textR;
    JButton findBtn, findNext, replace, replaceAll, cancel;
    private JTextPane pane;
    private String txt;


    Find(JTextPane text) {
        this.setAlwaysOnTop(true);
        this.pane = text;
        this.txt = text.getText();
        this.txt = this.txt.replaceAll(String.valueOf((char) 13), "");

        lab1 = new JLabel("Find:");
        lab2 = new JLabel("Replace:");
        textF = new JTextField(30);
        textR = new JTextField(30);
        findBtn = new JButton("Find");
        findNext = new JButton("Find Next");
        replace = new JButton("Replace");
        replaceAll = new JButton("Replace All");
        cancel = new JButton("Cancel");

        setLayout(null);

        int labWidth = 80;
        int labHeight = 20;
        this.setResizable(false);

        lab1.setBounds(10, 10, labWidth, labHeight);
        add(lab1);
        textF.setBounds(10 + labWidth, 10, 120, 20);
        add(textF);
        lab2.setBounds(10, 10 + labHeight + 10, labWidth, labHeight);
        add(lab2);
        textR.setBounds(10 + labWidth, 10 + labHeight + 10, 120, 20);
        add(textR);

        findBtn.setBounds(225, 6, 115, 20);
        add(findBtn);
        findBtn.addActionListener(this);

        findNext.setBounds(225, 28, 115, 20);
        add(findNext);
        findNext.addActionListener(this);

        replace.setBounds(225, 50, 115, 20);
        add(replace);
        replace.addActionListener(this);

        replaceAll.setBounds(225, 72, 115, 20);
        add(replaceAll);
        replaceAll.addActionListener(this);

        cancel.setBounds(225, 94, 115, 20);
        add(cancel);
        cancel.addActionListener(this);

        int width = 360;
        int height = 160;

        setSize(width, height);

        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        setLocation(center.x - width / 2, center.y - height / 2);
        setVisible(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    private void find() {
        select_start = txt.toLowerCase().indexOf(textF.getText().toLowerCase());
        if (select_start == -1) {
            startIndex = 0;
            JOptionPane.showMessageDialog(null, "Could not find \"" + textF.getText() + "\"!");
            return;
        }
        if (select_start == txt.toLowerCase().lastIndexOf(textF.getText().toLowerCase())) {
            startIndex = 0;
        }
        int select_end = select_start + textF.getText().length();
        pane.grabFocus();
        pane.select(select_start, select_end);
    }

    private void findNext() {
        String selection = pane.getSelectedText();
        try {
            selection.equals("");
        } catch (NullPointerException e) {
            selection = textF.getText();
            try {
                selection.equals("");
            } catch (NullPointerException e2) {
                selection = JOptionPane.showInputDialog("Find:");
                textF.setText(selection);
            }
        }
        try {
            int select_start = txt.toLowerCase().indexOf(selection.toLowerCase(), startIndex);
            int select_end = select_start + selection.length();
            pane.grabFocus();
            pane.select(select_start, select_end);
            startIndex = select_end + 1;

            if (select_start == txt.toLowerCase().lastIndexOf(selection.toLowerCase())) {
                startIndex = 0;
            }
        } catch (NullPointerException e) {
        }
    }

    private void replace() {
        try {
            find();
            if (select_start != -1)
                pane.replaceSelection(textR.getText());
            pane.grabFocus();
            pane.select(select_start, select_start + textR.getText().length());
            txt = pane.getText();
            txt = txt.replaceAll(String.valueOf((char) 13), "");
        } catch (NullPointerException e) {
            System.out.print("Null Pointer Exception: " + e);
        }
    }

    private void replaceAll() {
        pane.grabFocus();
        pane.setText(txt.toLowerCase().replaceAll(textF.getText().toLowerCase(), textR.getText()));
        txt = pane.getText();
        txt = txt.replaceAll(String.valueOf((char) 13), "");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == findBtn) {
            find();
        } else if (e.getSource() == findNext) {
            findNext();
        } else if (e.getSource() == replace) {
            replace();
        } else if (e.getSource() == replaceAll) {
            replaceAll();
        } else if (e.getSource() == cancel) {
            this.setVisible(false);
        }
    }
}