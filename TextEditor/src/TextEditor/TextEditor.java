package TextEditor;

import javax.swing.*;

public class TextEditor extends JTextPane {
    private static final long serialVersionUID = 1L;
    public final static String NAME = "TextEditor";
    public final static String AUTHOREMAIL1 = "nathan1108@163.com";
    public final static String AUTHOREMAIL2 = "tracyking@sjtu.edu.cn";
    public final static String AUTHOREMAIL3 = "chrisliu9603@126.com";
    public final static double VERSION = 1.0;
    public final static String NEWTAB = "New Document";
    public final static GUI gui = new GUI();

    public static void main(String[] args) {
        gui.setVisible(true);
    }
}
