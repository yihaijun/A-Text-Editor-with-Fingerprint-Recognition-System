#ifndef _FPSCTRL_H_
#define _FPSCTRL_H_


///////////////////////////////////////////////////////////////////////////////////////////////////
//窗口自定义消息
#define WM_NCLMESSAGE	WM_USER+120
#define FPM_DEVICE		0x01	//设备
#define FPM_PLACE		0x02	//请按手指
#define FPM_LIFT		0x03	//请抬起手指
#define FPM_CAPTURE		0x04	//采集图像完成
#define FPM_GENCHAR		0x05	//采集特征点
#define FPM_ENRFPT		0x06	//登记指纹
#define FPM_NEWIMAGE	0x07	//新的指纹图像
#define FPM_TIMEOUT		0x08
#define FPM_IMGVAL		0x09

#define FPM_ENROLLID	0x11	//登记到模块内部
#define FPM_VERIFYID	0x12	//在模块内部验证指定ID
#define FPM_IDENTIFYID	0x13	//在模块内部识别指纹,并返回ID

#define ECL_OK		0
#define ECL_FAIL	-1

#define RET_OK			1
#define RET_FAIL		0

//#define FPSAPI __stdcall
#define FPSAPI 

//从指纹图像里提取特征，建立模板,
//pTemplate	指纹数据,大小为256*288,
//pCharData-提取出的指纹特征,大小为256BYTE.
//返回0表示成功
int WINAPI CreateTemplate(BYTE* pFingerData,BYTE *pTemplate);	

//比对指纹
//返回分数，一般大于50分表示成功。也可以设置其它分数,分数越高,安全等级越高
//pSrcData，pDstData是要匹配的指纹特征，pSrcData，pDstData大小为256BYTE
int WINAPI MatchTemplate(BYTE *pSrcData,BYTE *pDstData);

//比对指纹
//返回分数，一般大于50分表示成功。也可以设置其它分数,分数越高,安全等级越高
//pSrcData,指纹特征点,大小为256BYTE
//pDstData,指纹模版,由nDstSize指定，一般为512BYTE
int WINAPI MatchTemplateOne(BYTE *pSrcData,BYTE *pDstData,int nDstSize);
int WINAPI MatchTemplateOneEx(BYTE *pSrcData,int nSrcSize,BYTE *pDstData,int nDstSize);

//比对指纹
//pSrcData，指纹特征点,pSrcData大小为256BYTE
//pDstFullData,多个指纹模版
//nDstCount,模版数
//nDstSize,模版的大小
//nThreshold,比对成功分数
//返回-1表示失败,返回>=0,表示多个指纹模版中位于该位置的模版比对成功
int WINAPI MatchTemplateaArray(BYTE *pSrcData,BYTE *pDstFullData,int nDstCount,int nDstSize,int nThreshold);


//设置消息窗口句柄
void WINAPI SetMsgMainHandle(HWND hWnd);

//打开设备
//style=1 打开串口设备,如OpenDevice(1,57600,1)
//style=0 打开USB设备,如OpenDevice(0,0,0)
int WINAPI OpenDevice(int comnum=0,int nbaud=0,int style=0);

//连接设备
int WINAPI LinkDevice();
//关闭设备
int WINAPI CloseDevice();

//获取指纹图像
int WINAPI GetFingerImage(BYTE * imagedata,int * size);
int WINAPI GetFingerImageEx(BYTE * imagedata,int * size);
//在窗口上画指纹图像
int WINAPI DrawFingerImage(BYTE * imagedata,int size,HDC hdc,int left,int top);
//在窗口上画指纹图像,图像使用SDK刚采集的.
int WINAPI DrawImage(HDC hdc,int left,int top);

//设置图像显示参数
BOOL WINAPI SetDispOption(BOOL bSet,double iZoom,COLORREF clrBack ,COLORREF clrForce,int nThick);
//保存图像到文件(Ansi)
BOOL WINAPI SaveFpImageA(LPSTR filename,int type,int quality);
//保存图像到文件(空函数,功能未实现)
BOOL WINAPI SaveFpImageW(LPWSTR filename);
//设置判断图像质量参数
BOOL WINAPI SetImageVal(BOOL bSet,int min,int max);

///////////////////////////////////////////////////////////////////////////////////////////////////////////
//操作设备内数据

//获取设备内模板数
int WINAPI GetTemplatesCount(int * count);
//清除设备内模版
int WINAPI ClearTemplates();
//在设备上登记模版
int WINAPI EnrollTemplate(int id);
//在设备上1:1识别模版
int WINAPI VerifyTemplate(int id,int* iScore);
//在设备上1:N识别模版
int WINAPI IdentifyTemplate(int * id);
//在设备上删除指纹模板
int WINAPI DeleteTemplate(int id);

//登记模版
int WINAPI EnrollToPc(BYTE * templates,int * tempsize);
//采集指纹特征点
int WINAPI GenCharToPc(BYTE * templates,int * tempsize);
//在设备上比对两个模版
int WINAPI MatchOneToOne(BYTE * templates1,int tempsize1,BYTE * templates2,int tempsize2,int* iScore);
//在设备上比对两个模版
int WINAPI MatchInDevice(BYTE * templates1,int tempsize1,BYTE * templates2,int tempsize2,int* iScore);
//在设备上查找指匹配模板
int WINAPI VerifyInDevice(BYTE * templates1,int tempsize1,int* iScore);

///////////////////////////////////////////////////////////////////////////////////////////////////////////
//线程函数,实际应用中,主要采用如下函数

//采集指纹图像
void WINAPI CaptureImage();
//采集指纹特征点
void WINAPI GenFpChar();
//登记指纹模版
void WINAPI EnrolFpChar();

//获取指纹消息
int WINAPI GetWorkMsg();
//获取返回消息
int WINAPI GetRetMsg();
//释放消息
int WINAPI ReleaseMsg();

//获取指纹图像
BOOL WINAPI GetImage(BYTE * imagedata,int * size);

//获取指纹特征点
BOOL WINAPI GetFpCharByGen(BYTE * tpbuf,int * tpsize);
//获取指纹模版
BOOL WINAPI GetFpCharByEnl(BYTE * fpbuf,int * fpsize);

//获取指纹特征点(字符串)
BOOL WINAPI GetFpStrByGen(char * tpstr);
//获取指纹模版(字符串)
BOOL WINAPI GetFpStrByEnl(char * fpstr);


//设置超时
VOID WINAPI SetTimeOut(double itime);

//设置是否上传图像
VOID WINAPI SetUpImage(BOOL bUpImage);

//设置状态灯状态
VOID WINAPI SetStatusLed(int type,int status);

//BYTE类型模板转字符串模板(ANSI)
VOID WINAPI CharByteToStr(BYTE * buf,int size,char * str);
//字符串模板转BYTE类型模板(ANSI)
VOID WINAPI StrToCharByte(char * str,BYTE * buf,int * size);
//字符串模板比对(ANSI)
int WINAPI MatchTemplateEx(char* pSrcData,char* pDstData);

//BYTE类型模板转字符串模板(UNICODE)
VOID WINAPI CharByteToUStr(BYTE * buf,int size,WCHAR * ustr);
//字符串模板转BYTE类型模板(UNICODE)
VOID WINAPI UStrToCharByte(WCHAR * ustr,BYTE * buf,int * size);
//字符串模板比对(UNICODE)
int WINAPI UMatchTemplateEx(WCHAR* puSrcData,WCHAR* puDstData);


//打印
BOOL WINAPI DevicePrint(BYTE* buffer,int size);

#endif