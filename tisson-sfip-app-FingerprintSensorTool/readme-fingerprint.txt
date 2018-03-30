一、安装
    解压sfip-standalone-min-1.0.6-122.zip到要安装的目录，双击sfip-setup.bat，留意提示框
 
二、服务调用说明
1、服务调用方法
    post 请求 http://127.0.0.1:29093/services/tisson/FingerprintUtils/call
2、请求参数说明
	举例：{"appName":"","beanName":"getCmdPrompt","msg":""} 
	appName目前必须为""
	beanName和msg见下具体服务功能说明

3、返回包文说明
	举例：{"responseCode":"FPU0100000000","responseContent":"","result":"(SN=3832173300917),2"}
	responseContent为responseCode的原因体说明
	result为返回包文正文
	responseCode: 第1-3位为FPU(指纹工具),第3-4位:00表示查询,01表示命令,03表示其它    第6-7位:00操作完成 ,99操作未完成,88操作 异常结束       
 		后6位为响应码
 	responseCode举例
        	FPU0000000000 采集完成
        	FPU0000000004 采集完成后有新指纹按下,不影响采集结果
				（ 一般不会出现，因为收到000000就应该清除定时器）

        	FPU0099000003 请按同一个手指3次
        	FPU0099000002 请按同一个手指2次
        	FPU0099000001 请按同一个手指1次

        	FPU0088000100 没有采集设备
        	FPU0088000101 设备错误
        	FPU0088000102 设备忙
	
        	FPU0088000200 不在采集状态
        	FPU0088000201 按了不同手指
        	FPU0088000202 提取特征码失败
        	FPU0088000203 未知的错误
        	FPU0088000204添加指纹到内存失败
	result:
	       responseCode后6位为FPU0000000000 或FPU00000000004时,result是采集到的指纹特征码，最大长度为4096

三、服务说明
1、发起采集
    使用场景:
 	每次要采集时调一次
 	当返回responseCode为FPU0100000000时是表示发起成功 ,其它值是发起失败
 	
    请求参数：{"appName":"","beanName":"cmdCollection","msg":""} 
    返回值：
         举例：{"responseCode":"FPU0100000000","responseContent":"","result":"(SN=3832173300917),2"}

2、获取采集状态
    使用场景:
 	调用cmdCollection开始成功（responseCode=FPU0100000000）后定时100ms查询一次  
 	当返回responseCode后3位是FPU0099000003,或FPU0099000002,或FPU0099000001时是采集正在进行,
 	                                                            是FPU0000000000或FPU0000000004时是表示采集完成 ,其它值是采集失败
          
    请求参数：{"appName":"","beanName":"getCurrentOwnerRegTempBase64","msg":""} 
    返回值：
         举例:{"responseCode":"FPU0000000000","responseContent":"","result":"libzkfp:714:S4lTUzIxAAACysk..."}
 

    