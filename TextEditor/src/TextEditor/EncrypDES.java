package TextEditor;

import javax.crypto.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

public class EncrypDES {
    private KeyGenerator keygen;
    private SecretKey deskey;
    private Cipher c;
    private byte[] cipherByte;

    EncrypDES() throws NoSuchAlgorithmException, NoSuchPaddingException {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        keygen = KeyGenerator.getInstance("DES");
        deskey = keygen.generateKey();
        c = Cipher.getInstance("DES");
    }

    EncrypDES(SecretKey deskey) throws NoSuchAlgorithmException, NoSuchPaddingException {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        this.deskey = deskey;
        c = Cipher.getInstance("DES");
    }

    public byte[] Encrytor(Object obj) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        c.init(Cipher.ENCRYPT_MODE, deskey);
        byte[] src = toByteArray(obj);
        cipherByte = c.doFinal(src);
        return cipherByte;
    }

    public Object Decryptor(byte[] buff) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        c.init(Cipher.DECRYPT_MODE, deskey);
        cipherByte = c.doFinal(buff);
        return toObject(cipherByte);
    }

    public SecretKey getSecretKey() {
        return deskey;
    }

    public byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    public Object toObject(byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj;
    }
}


