package TextEditor;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerialFile {
    private Object files[];    //files[0] = secretKey; file[1] = flag; files[2] = StyledDocument; files[3] = image;
    private String filePath;
    private EncrypDES cipher;

    SerialFile(String filePath, StyledDocument doc, Fingerprint image) {
        this.filePath = filePath;
        try {
            cipher = new EncrypDES();
            if (image != null) {
                files = new Object[]{cipher.getSecretKey(), true, cipher.Encrytor(doc), cipher.Encrytor(image)};
            } else {
                files = new Object[]{cipher.getSecretKey(), false, cipher.Encrytor(doc)};
            }
            saveFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    SerialFile(String filePath) {
        this.filePath = filePath;
        try {
            readFile();
            SecretKey deskey = getSecretKey();
            cipher = new EncrypDES(deskey);
            boolean flag = getFlag();
            files[2] = cipher.Decryptor((byte[]) files[2]);
            if (flag) {
                files[3] = cipher.Decryptor((byte[]) files[3]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveFile() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath));
            oos.writeObject(files);
            oos.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error in saving file");
        }
    }

    public void readFile() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath));
            files = (Object[]) ois.readObject();
            ois.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error in reading file");
        }

    }

    public SecretKey getSecretKey() {
        return (SecretKey) files[0];
    }

    public boolean getFlag() {
        return (boolean) files[1];
    }

    public StyledDocument getStyledDocument() {
        return (StyledDocument) files[2];
    }

    public Fingerprint getFingerprint() {
        if (files.length < 4) {
            return null;
        } else {
            return (Fingerprint) files[3];
        }
    }
}
