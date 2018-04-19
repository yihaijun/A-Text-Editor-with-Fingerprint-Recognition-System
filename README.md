# A Text Editor with Fingerprint Recognition System

Please refer to the *User Guidance.pdf*

This software is a text editor with fingerprint recognition system. It's based on Java and need to be used with a fingerprint collector. To bring the security conception into consideration, we add the fingerprint recognition system into a rich text editor. Users can bind their fingerprints to an encrypted text files that it need correct fingerprints to open the files. Besides, the file itself is encrypted by DES and it's difficult to be cracked by outside program

我Fork这个项目后，增加了一个指纹采集及识别的子项目tisson-sfip-app-FingerprintSensorTool
它是基本于我的树状微服务sfip平台开发的指纹采集及识别服务，特点：1、bs架构采集仪不采用传统ocx, 而是通过js调用本地sfip服务; 2、采集自己优化的通用指纹识别算法;
![不用web服务器的测试页面](https://github.com/yihaijun/A-Text-Editor-with-Fingerprint-Recognition-System/blob/master/tisson-sfip-app-FingerprintSensorTool/src/test/resources/FingerprintSensorTool.png）
