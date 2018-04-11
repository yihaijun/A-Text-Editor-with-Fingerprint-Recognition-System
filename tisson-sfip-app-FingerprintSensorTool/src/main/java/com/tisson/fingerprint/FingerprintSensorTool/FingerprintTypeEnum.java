/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool;

import lombok.Getter;
/**
 * @author yihaijun
 *
 */
public enum FingerprintTypeEnum {
    /**
     * 中控
     */
    ZKLIB(1, "ZKLIB"),
    /**
     * 提现
     */
    TissonAFIS(2, "TissonAFIS");
    
    @Getter
    private  int code;

    @Getter
    private String desc;

    FingerprintTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
