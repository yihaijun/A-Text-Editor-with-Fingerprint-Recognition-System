package TextEditor;

import com.mathworks.toolbox.javabuilder.MWNumericArray;

import java.io.Serializable;

public class Fingerprint implements Serializable {
    private static final long serialVersionUID = 1L;
    private MWNumericArray image1;
    private MWNumericArray image2;
    private MWNumericArray image3;
    private byte[] fpTemp;

    Fingerprint(MWNumericArray image1, MWNumericArray image2, MWNumericArray image3, byte[] fpTemp) {
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
        this.fpTemp = fpTemp;
    }

    public MWNumericArray getImage1() {
        return image1;
    }

    public MWNumericArray getImage2() {
        return image2;
    }

    public MWNumericArray getImage3() {
        return image3;
    }

    public byte[] getFpTemp() {
        return fpTemp;
    }
}
