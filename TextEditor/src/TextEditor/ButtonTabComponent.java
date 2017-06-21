package TextEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonTabComponent extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final JTabbedPane pane;
    private final JButton button;

    public ButtonTabComponent(final JTabbedPane pane, String name) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        this.pane = pane;

        setOpaque(false);

        JLabel label = new JLabel(name) {

            private static final long serialVersionUID = 1L;

            @Override
            public String getText() {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1)
                    return pane.getTitleAt(i);
                return null;
            }
        };

        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        add(label);
        ImageIcon closeTabIcon = new ImageIcon("icons/closeTab.png");
        button = new JButton(closeTabIcon);
        button.addActionListener(this);
        button.setToolTipText("Close");
        add(button);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
                if (!GUI.getSaveBoolList().get(i)) {
                    switch (JOptionPane.showConfirmDialog(TextEditor.gui, "Do you want to save " + pane.getTitleAt(i).substring(1) + " ?", "Attention", JOptionPane.YES_NO_CANCEL_OPTION)) {
                        case JOptionPane.YES_OPTION:
                            TextEditor.gui.saveFile(i);
                            break;
                        case JOptionPane.NO_OPTION:
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            return;
                        default:
                    }
                }
                pane.remove(i);
                GUI.closeTab();
                GUI.getSaveBoolList().remove(i);
                GUI.fingerprintList.remove(i);
                GUI.getFileList().remove(i);
                GUI.getUndoManagerList().remove(i);
            }
        }
    }
}
