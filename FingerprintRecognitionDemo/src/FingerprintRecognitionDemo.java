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

public class FingerprintRecognitionDemo extends JFrame {
    static final int enrollCount = 3;
    JButton buttonBegin = null;
    JButton buttonEnroll = null;
    JButton buttonVerify = null;
    JButton buttonStop = null;
    JButton buttonImg = null;
    JTextArea textArea;
    int width = 0;
    int height = 0;
    boolean verifying = false;
    boolean enrolling = false;
    boolean enrolled = false;
    int enrollIndex = 0;
    byte[][] enrollTempArray = new byte[3][2048];
    byte[] retTempArray = new byte[2048];
    MWNumericArray[] FPMatchArray = new MWNumericArray[3];
    MWNumericArray currentFPMatchArray = null;
    FingerprintSensor fingerprintSensor = null;

    public static void writeBitmap(byte[] imageBuf, int nWidth, int nHeight, String path) throws IOException {
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

    public static byte[] changeByte(int data) {
        byte b4 = (byte) (data >> 24);
        byte b3 = (byte) (data >> 16);
        byte b2 = (byte) (data >> 8);
        byte b1 = (byte) (data);
        byte[] bytes = {b1, b2, b3, b4};
        return bytes;
    }

    public static void main(String[] args) {
        new FingerprintRecognitionDemo().launchFrame();
    }

    public void launchFrame() {
        this.setLayout(null);
        buttonBegin = new JButton("Begin");
        this.add(buttonBegin);
        buttonBegin.setBounds(30, 40, 100, 40);

        buttonEnroll = new JButton("Enroll");
        this.add(buttonEnroll);
        buttonEnroll.setBounds(30, 140, 100, 40);

        buttonVerify = new JButton("Verify");
        this.add(buttonVerify);
        buttonVerify.setBounds(30, 240, 100, 40);

        buttonStop = new JButton("Stop");
        this.add(buttonStop);
        buttonStop.setBounds(30, 340, 100, 40);

        buttonImg = new JButton();
        buttonImg.setBounds(150, 5, 360, 400);
        buttonImg.setDefaultCapable(false);
        this.add(buttonImg);

        textArea = new JTextArea();
        this.add(textArea);
        textArea.setBounds(10, 440, 500, 100);
        textArea.setFont(new Font("Courier", Font.PLAIN, 16));
        textArea.setLineWrap(true);

        this.setSize(520, 580);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setTitle("FingerprintRecognitionDemo");
        this.setResizable(false);

        buttonBegin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fingerprintSensor != null) {
                    textArea.setText("Sensor was already opened!");
                    return;
                }

                fingerprintSensor = new FingerprintSensor();
                int ret = fingerprintSensor.getDeviceCount();
                if (ret < 0) {
                    textArea.setText("No devices connected!");
                    freeSensor();
                    return;
                }
                if ((ret = fingerprintSensor.openDevice(0)) != FingerprintSensorErrorCode.ERROR_SUCCESS) {
                    textArea.setText("Open device fail, ret = " + ret + "!");
                    freeSensor();
                    return;
                }

                width = fingerprintSensor.getImageWidth();
                height = fingerprintSensor.getImageHeight();
                fingerprintSensor.setFingerprintCaptureListener(new FingerprintCaptureListener() {
                    @Override
                    public void captureOK(byte[] bytes) {
                        try {
                            if (enrolling) {
                                int[] dims = {height, width};
                                FPMatchArray[enrollIndex] = MWNumericArray.newInstance(dims, MWClassID.UINT8, MWComplexity.REAL);
                                for (int i = 1; i <= height; ++i) {
                                    for (int j = 1; j <= width; ++j) {
                                        int[] index = {i, j};
                                        FPMatchArray[enrollIndex].set(index, bytes[(i - 1) * width + j - 1]);
                                    }
                                }
                                writeBitmap(bytes, width, height, "fingerprint" + enrollIndex + ".bmp");
                                buttonImg.setIcon(new ImageIcon((ImageIO.read(new File("fingerprint" + enrollIndex + ".bmp")))));
                            } else {
                                int[] dims = {height, width};
                                currentFPMatchArray = MWNumericArray.newInstance(dims, MWClassID.UINT8, MWComplexity.REAL);
                                for (int i = 1; i <= height; ++i) {
                                    for (int j = 1; j <= width; ++j) {
                                        int[] index = {i, j};
                                        currentFPMatchArray.set(index, bytes[(i - 1) * width + j - 1]);
                                    }
                                }
                                writeBitmap(bytes, width, height, "fingerprint.bmp");
                                buttonImg.setIcon(new ImageIcon((ImageIO.read(new File("fingerprint.bmp")))));
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
                        if (enrolling) {
                            if (enrollIndex > 0 && ZKFPService.MatchFP(enrollTempArray[enrollIndex - 1], bytes) <= 0) {
                                textArea.setText("Please press the same finger 3 times for the enrollment!");
                                return;
                            }
                            System.arraycopy(bytes, 0, enrollTempArray[enrollIndex], 0, 2048);
                            enrollIndex++;
                            if (enrollIndex == enrollCount) {
                                int[] retLen = new int[1];
                                retLen[0] = 2048;
                                int ret;
                                if ((ret = ZKFPService.GenRegFPTemplate(enrollTempArray[0], enrollTempArray[1], enrollTempArray[2], retTempArray, retLen)) == 0) {
                                    textArea.setText("Enroll succ!");
                                    enrolled = true;
                                } else {
                                    textArea.setText("Enroll fail, error code = " + ret);
                                }
                                enrolling = false;
                            } else {
                                textArea.setText("Please press your finger! (remaining: " + (3 - enrollIndex) + " times)");
                            }
                        } else if (verifying) {
                            long startTime = (new Date()).getTime();
                            Thread timeThread = new Thread() {
                                @Override
                                public void run() {
                                    while (true) {
                                        try {
                                            textArea.setText("Verifying (" + ((new Date()).getTime() - startTime) / 1000 + " s)\n");
                                            Thread.sleep(1000);
                                        } catch (Exception e) {
                                            break;
                                        }
                                    }
                                }
                            };
                            timeThread.start();
                            int ret = ZKFPService.MatchFP(bytes, retTempArray);
                            int[] MyScore = new int[3];
                            Thread thread1 = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Object[] FPMatchScore;
                                        FPMatch fpMatch = new FPMatch();
                                        FPMatchScore = fpMatch.main(1, currentFPMatchArray, FPMatchArray[0]);
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
                                        FPMatchScore = fpMatch.main(1, currentFPMatchArray, FPMatchArray[1]);
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
                                        FPMatchScore = fpMatch.main(1, currentFPMatchArray, FPMatchArray[2]);
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
                            if (ret > 0) {
                                textArea.setText("Verify succ, score = " + ret);
                            } else {
                                textArea.setText("Verify fail, ret = " + ret);
                            }
                            textArea.setText(textArea.getText() + "\nMyScore = " + Math.max(Math.max(MyScore[0], MyScore[1]), MyScore[2]));
                        }
                    }
                });
                if (!fingerprintSensor.startCapture()) {
                    textArea.setText("StartCapture fail!");
                    freeSensor();
                    return;
                }
                textArea.setText("Start succ!");
            }
        });

        buttonEnroll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fingerprintSensor == null) {
                    textArea.setText("Please begin capture first!");
                    return;
                }
                if (!enrolling) {
                    enrollIndex = 0;
                    enrolling = true;
                    verifying = false;
                    textArea.setText("Please press your finger! (remaining: 3 times)");
                }
            }
        });

        buttonVerify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fingerprintSensor == null) {
                    textArea.setText("Please begin capture first!");
                    return;
                }
                if (enrolling) {
                    enrollIndex = 0;
                    enrolling = false;
                }
                if (!enrolled) {
                    textArea.setText("Please enroll first!");
                    return;
                }
                verifying = true;
                textArea.setText("Please press your finger!");
            }
        });

        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                freeSensor();
                textArea.setText("Stop succ!");
            }
        });

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                freeSensor();
                super.windowClosing(e);
            }
        });
    }

    private void freeSensor() {
        enrollIndex = 0;
        enrolling = false;
        enrolled = false;
        verifying = false;
        if (fingerprintSensor != null) {
            fingerprintSensor.stopCapture();
            fingerprintSensor.closeDevice();
            fingerprintSensor.destroy();
            fingerprintSensor = null;
        }
        for (int i = 0; i < 3; ++i) {
            if (FPMatchArray[i] != null) {
                FPMatchArray[i].dispose();
            }
        }
        if (currentFPMatchArray != null) {
            currentFPMatchArray.dispose();
        }
    }
}
