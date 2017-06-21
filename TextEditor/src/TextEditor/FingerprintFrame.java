package TextEditor;

import FPMatch.FPMatch;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.zkteco.biometric.FingerprintCaptureListener;
import com.zkteco.biometric.FingerprintSensor;
import com.zkteco.biometric.FingerprintSensorErrorCode;
import com.zkteco.biometric.ZKFPService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class FingerprintFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    static final int enrollCount = 3;
    public static int FUNCTION_ENROLL = 1;
    public static int FUNCTION_VERIFY = 2;
    JButton buttonRedo = null;
    JButton buttonConfirm = null;
    JButton buttonCancel = null;
    JButton buttonImg = null;
    JLabel textArea;
    JProgressBar progressBar = null;
    int width;
    int height;
    int enrollIndex = 0;
    byte[][] enrollTempArray = new byte[3][2048];
    byte[] retTempArray = new byte[2048];
    MWNumericArray[] FPMatchArray = new MWNumericArray[3];
    MWNumericArray currentFPMatchArray = null;
    FingerprintSensor fingerprintSensor = null;

    FingerprintFrame(final int function, final Fingerprint fp) {
        if (function == FUNCTION_VERIFY && fp == null) {
            JOptionPane.showMessageDialog(this, "No fingerprint provided !", "Error", JOptionPane.ERROR_MESSAGE);
            freeSensor();
            this.dispose();
            return;
        }

        this.setLayout(null);
        this.setAlwaysOnTop(true);

        if (function == FUNCTION_ENROLL) {
            buttonRedo = new JButton("Redo");
            buttonRedo.setBounds(130, 75, 100, 30);
            buttonRedo.setEnabled(false);
            buttonRedo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    enrollIndex = 0;
                    buttonRedo.setEnabled(false);
                    buttonConfirm.setEnabled(false);
                    textArea.setText("Please press your finger ! (remaining: 3 times)");
                }
            });
            this.add(buttonRedo);

            buttonConfirm = new JButton("Confirm");
            buttonConfirm.setBounds(250, 75, 100, 30);
            buttonConfirm.setEnabled(false);
            buttonConfirm.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    confirm(function);
                }
            });
            this.add(buttonConfirm);

            buttonCancel = new JButton("Cancel");
            buttonCancel.setBounds(370, 75, 100, 30);
            buttonCancel.setEnabled(true);
            buttonCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancel();
                }
            });
            this.add(buttonCancel);
        } else if (function == FUNCTION_VERIFY) {
            buttonCancel = new JButton("Cancel");
            buttonCancel.setBounds(250, 75, 100, 30);
            buttonCancel.setEnabled(true);
            buttonCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancel();
                }
            });
            this.add(buttonCancel);

            progressBar = new JProgressBar(0, 100);
            progressBar.setBounds(130, 30, 340, 20);
            progressBar.setStringPainted(true);
            progressBar.setIndeterminate(false);
            progressBar.setString("");
            progressBar.setVisible(false);
            this.add(progressBar);
        }

        buttonImg = new JButton();
        buttonImg.setBounds(20, 10, 90, 100);
        buttonImg.setDefaultCapable(false);
        buttonImg.setBackground(Color.WHITE);
        this.add(buttonImg);

        textArea = new JLabel();
        textArea.setBounds(130, 20, 340, 40);
        textArea.setFont(new Font("Courier", Font.PLAIN, 16));
        textArea.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(textArea);

        this.setSize(490, 150);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        if (function == FUNCTION_ENROLL) {
            this.setTitle("Encrypt with Fingerprint - " + TextEditor.gui.getPane().getTitleAt(TextEditor.gui.getPane().getSelectedIndex()));
        } else if (function == FUNCTION_VERIFY) {
            this.setTitle("Fingerprint Verification");
        }
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                TextEditor.gui.setEnabled(true);
                freeSensor();
                super.windowClosing(e);
            }
        });


        fingerprintSensor = new FingerprintSensor();
        int ret = fingerprintSensor.getDeviceCount();
        if (ret < 0) {
            JOptionPane.showMessageDialog(this, "No devices connected !", "Error", JOptionPane.ERROR_MESSAGE);
            freeSensor();
            this.dispose();
            return;
        }
        if ((ret = fingerprintSensor.openDevice(0)) != FingerprintSensorErrorCode.ERROR_SUCCESS) {
            JOptionPane.showMessageDialog(this, "Open device fail, ret = " + ret + " !", "Error", JOptionPane.ERROR_MESSAGE);
            freeSensor();
            this.dispose();
            return;
        }

        width = fingerprintSensor.getImageWidth();
        height = fingerprintSensor.getImageHeight();
        fingerprintSensor.setFingerprintCaptureListener(new FingerprintCaptureListener() {
            @Override
            public void captureOK(byte[] bytes) {
                try {
                    if (function == FUNCTION_ENROLL && enrollIndex < 3) {
                        int[] dims = {height, width};
                        FPMatchArray[enrollIndex] = MWNumericArray.newInstance(dims, MWClassID.UINT8, MWComplexity.REAL);
                        for (int i = 1; i <= height; ++i) {
                            for (int j = 1; j <= width; ++j) {
                                int[] index = {i, j};
                                FPMatchArray[enrollIndex].set(index, bytes[(i - 1) * width + j - 1]);
                            }
                        }
                        writeBitmap(bytes, width, height, "fingerprint" + enrollIndex + ".bmp");
                        File img = new File("fingerprint" + enrollIndex + ".bmp");
                        buttonImg.setIcon(new ImageIcon((ImageIO.read(img)).getScaledInstance(90, 100, Image.SCALE_SMOOTH)));
                        img.delete();
                    } else if (function == FUNCTION_VERIFY) {
                        int[] dims = {height, width};
                        currentFPMatchArray = MWNumericArray.newInstance(dims, MWClassID.UINT8, MWComplexity.REAL);
                        for (int i = 1; i <= height; ++i) {
                            for (int j = 1; j <= width; ++j) {
                                int[] index = {i, j};
                                currentFPMatchArray.set(index, bytes[(i - 1) * width + j - 1]);
                            }
                        }
                        writeBitmap(bytes, width, height, "fingerprint.bmp");
                        File img = new File("fingerprint.bmp");
                        buttonImg.setIcon(new ImageIcon((ImageIO.read(img)).getScaledInstance(90, 100, Image.SCALE_SMOOTH)));
                        img.delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void captureError(int i) {
            }

            @Override
            public void extractOK(byte[] bytes) {
                if (function == FUNCTION_ENROLL && enrollIndex < 3) {
                    if (enrollIndex > 0 && ZKFPService.MatchFP(enrollTempArray[enrollIndex - 1], bytes) <= 0) {
                        textArea.setText("Please press the same finger 3 times!");
                        return;
                    }
                    System.arraycopy(bytes, 0, enrollTempArray[enrollIndex], 0, 2048);
                    enrollIndex++;
                    buttonRedo.setEnabled(true);
                    if (enrollIndex == enrollCount) {
                        int[] retLen = new int[1];
                        retLen[0] = 2048;
                        int ret;
                        if ((ret = ZKFPService.GenRegFPTemplate(enrollTempArray[0], enrollTempArray[1], enrollTempArray[2], retTempArray, retLen)) == 0) {
                            textArea.setText("Done!");
                            buttonConfirm.setEnabled(true);
                        } else {
                            textArea.setText("Enroll fail, error code = " + ret);
                        }
                    } else {
                        textArea.setText("Please press your finger! (remaining: " + (3 - enrollIndex) + " times)");
                    }
                } else if (function == FUNCTION_VERIFY) {
                    textArea.setText("");
                    textArea.setVisible(false);
                    progressBar.setVisible(true);
                    progressBar.setValue(0);
                    final long startTime = (new Date()).getTime();
                    Thread timeThread = new Thread() {
                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    progressBar.setString("Verifying (" + ((new Date()).getTime() - startTime) / 1000 + " s)");
                                    progressBar.setValue(Math.min(progressBar.getValue() + 2, 100));
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                    break;
                                }
                            }
                        }
                    };
                    timeThread.start();
                    int ret = ZKFPService.MatchFP(bytes, fp.getFpTemp());
                    final int[] MyScore = new int[3];
                    Thread thread1 = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Object[] FPMatchScore;
                                FPMatch fpMatch = new FPMatch();
                                FPMatchScore = fpMatch.main(1, currentFPMatchArray, fp.getImage1());
                                MyScore[0] = Integer.parseInt(FPMatchScore[0].toString());
                                fpMatch.dispose();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    Thread thread2 = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Object[] FPMatchScore;
                                FPMatch fpMatch = new FPMatch();
                                FPMatchScore = fpMatch.main(1, currentFPMatchArray, fp.getImage2());
                                MyScore[1] = Integer.parseInt(FPMatchScore[0].toString());
                                fpMatch.dispose();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    Thread thread3 = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Object[] FPMatchScore;
                                FPMatch fpMatch = new FPMatch();
                                FPMatchScore = fpMatch.main(1, currentFPMatchArray, fp.getImage3());
                                MyScore[2] = Integer.parseInt(FPMatchScore[0].toString());
                                fpMatch.dispose();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread1.start();
                    thread2.start();
                    thread3.start();
                    try {
                        thread1.join();
                        thread2.join();
                        thread3.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    timeThread.interrupt();
                    progressBar.setVisible(false);
                    textArea.setVisible(true);
                    double finalScore = Math.max(Math.max(MyScore[0], MyScore[1]), MyScore[2]) * 0.5 + ret * 0.5;
                    if (ret <= 0 || finalScore < 60) {
                        textArea.setText("Please try again!");
                    } else {
                        confirm(function);
                    }
                }
            }
        });

        if (!fingerprintSensor.startCapture()) {
            JOptionPane.showMessageDialog(this, "StartCapture fail!", "Error", JOptionPane.ERROR_MESSAGE);
            freeSensor();
            this.dispose();
            return;
        }
        TextEditor.gui.setEnabled(false);
        if (function == FUNCTION_ENROLL) {
            textArea.setText("Please press your finger! (remaining: 3 times)");
        } else if (function == FUNCTION_VERIFY) {
            textArea.setText("Please press your finger for verification!");
        }
    }

    private static void writeBitmap(byte[] imageBuf, int nWidth, int nHeight, String path) throws IOException {
        FileOutputStream fout = new FileOutputStream(path);
        DataOutputStream dout = new DataOutputStream(fout);

        int bfType = 0x424d;
        int bfSize = 54 + 1024 + nWidth * nHeight;
        int bfReserved1 = 0;
        int bfReserved2 = 0;
        int bfOffBits = 54 + 1024;

        dout.writeShort(bfType);
        dout.write(changeByte(bfSize), 0, 4);
        dout.write(changeByte(bfReserved1), 0, 2);
        dout.write(changeByte(bfReserved2), 0, 2);
        dout.write(changeByte(bfOffBits), 0, 4);

        int biSize = 40;
        int biWidth = nWidth;
        int biHeight = nHeight;
        int biPlanes = 1;
        int biBitcount = 8;
        int biCompression = 0;
        int biSizeImage = nWidth * nHeight;
        int biXPelsPerMeter = 0;
        int biYPelsPerMeter = 0;
        int biClrUsed = 0;
        int biClrImportant = 0;

        dout.write(changeByte(biSize), 0, 4);
        dout.write(changeByte(biWidth), 0, 4);
        dout.write(changeByte(biHeight), 0, 4);
        dout.write(changeByte(biPlanes), 0, 2);
        dout.write(changeByte(biBitcount), 0, 2);
        dout.write(changeByte(biCompression), 0, 4);
        dout.write(changeByte(biSizeImage), 0, 4);
        dout.write(changeByte(biXPelsPerMeter), 0, 4);
        dout.write(changeByte(biYPelsPerMeter), 0, 4);
        dout.write(changeByte(biClrUsed), 0, 4);
        dout.write(changeByte(biClrImportant), 0, 4);

        for (int i = 0; i < 256; ++i) {
            dout.writeByte(i);
            dout.writeByte(i);
            dout.writeByte(i);
            dout.writeByte(0);
        }

        for (int i = 0; i < nHeight; ++i) {
            dout.write(imageBuf, (nHeight - 1 - i) * nWidth, nWidth);
        }

        dout.flush();
        dout.close();
        fout.close();
    }

    private static byte[] changeByte(int data) {
        byte b4 = (byte) (data >> 24);
        byte b3 = (byte) (data >> 16);
        byte b2 = (byte) (data >> 8);
        byte b1 = (byte) (data);
        byte[] bytes = {b1, b2, b3, b4};
        return bytes;
    }

    private Fingerprint getFingerprint() {
        return new Fingerprint(FPMatchArray[0], FPMatchArray[1], FPMatchArray[2], retTempArray);
    }

    private void freeSensor() {
        if (fingerprintSensor != null) {
            fingerprintSensor.stopCapture();
            fingerprintSensor.closeDevice();
            fingerprintSensor.destroy();
            fingerprintSensor = null;
        }
    }

    private void confirm(int function) {
        TextEditor.gui.setEnabled(true);
        freeSensor();
        if (function == FUNCTION_ENROLL) {
            int index = TextEditor.gui.getPane().getSelectedIndex();
            if (index != -1 && GUI.getSaveBoolList().get(index)) {
                TextEditor.gui.getPane().setTitleAt(index, "*" + TextEditor.gui.getPane().getTitleAt(index));
                GUI.getSaveBoolList().set(index, false);
            }
            GUI.fingerprintList.set(TextEditor.gui.getPane().getSelectedIndex(), getFingerprint());
        } else if (function == FUNCTION_VERIFY) {
            TextEditor.gui.postOpenFileAction();
        }
        this.dispose();
    }

    private void cancel() {
        TextEditor.gui.setEnabled(true);
        freeSensor();
        this.dispose();
    }
}
