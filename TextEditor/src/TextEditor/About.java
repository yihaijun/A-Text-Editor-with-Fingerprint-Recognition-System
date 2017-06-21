package TextEditor;

import javax.swing.*;
import java.awt.*;

public class About {
    private final JFrame frame;
    private final JPanel panel;
    private String contentText;
    private final JLabel text;

    public About() {
        panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        frame = new JFrame();
        frame.setVisible(true);
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);
        text = new JLabel();
    }

    public void software() {
        frame.setTitle("About - " + TextEditor.NAME);

        contentText = "<html><body><center>" +
                "<h1 style = 'color:blue;'>" + TextEditor.NAME + "</h1>" +
                "<h3 style = 'color:green;'>Version: " + TextEditor.VERSION + "</h3>" +
                "<h2>Author</h2>" +
                "<p style = 'font-family:cambria;font-size:12px;'>Jiahao Fan &emsp Ruidong Jin &emsp Xiao Liu</p>" +
                "<h2>Contact us</h2>" +
                "<a href='mailto:" + TextEditor.AUTHOREMAIL1 + "?subject = About the TextEditor Software'>" + TextEditor.AUTHOREMAIL1 + "</a> &emsp" +
                "<a href='mailto:" + TextEditor.AUTHOREMAIL2 + "?subject = About the TextEditor Software'>" + TextEditor.AUTHOREMAIL2 + "</a> &emsp" +
                "<a href='mailto:" + TextEditor.AUTHOREMAIL3 + "?subject = About the TextEditor Software'>" + TextEditor.AUTHOREMAIL3 + "</a>" +
                "</center></body></html>";

        text.setText(contentText);
        panel.add(text);
        frame.add(panel);
    }
}