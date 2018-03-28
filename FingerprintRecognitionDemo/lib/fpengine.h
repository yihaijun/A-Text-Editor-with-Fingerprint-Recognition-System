#ifndef _FPSCTRL_H_
#define _FPSCTRL_H_


///////////////////////////////////////////////////////////////////////////////////////////////////
//�����Զ�����Ϣ
#define WM_NCLMESSAGE	WM_USER+120
#define FPM_DEVICE		0x01	//�豸
#define FPM_PLACE		0x02	//�밴��ָ
#define FPM_LIFT		0x03	//��̧����ָ
#define FPM_CAPTURE		0x04	//�ɼ�ͼ�����
#define FPM_GENCHAR		0x05	//�ɼ�������
#define FPM_ENRFPT		0x06	//�Ǽ�ָ��
#define FPM_NEWIMAGE	0x07	//�µ�ָ��ͼ��
#define FPM_TIMEOUT		0x08
#define FPM_IMGVAL		0x09

#define FPM_ENROLLID	0x11	//�Ǽǵ�ģ���ڲ�
#define FPM_VERIFYID	0x12	//��ģ���ڲ���ָ֤��ID
#define FPM_IDENTIFYID	0x13	//��ģ���ڲ�ʶ��ָ��,������ID

#define ECL_OK		0
#define ECL_FAIL	-1

#define RET_OK			1
#define RET_FAIL		0

//#define FPSAPI __stdcall
#define FPSAPI 

//��ָ��ͼ������ȡ����������ģ��,
//pTemplate	ָ������,��СΪ256*288,
//pCharData-��ȡ����ָ������,��СΪ256BYTE.
//����0��ʾ�ɹ�
int WINAPI CreateTemplate(BYTE* pFingerData,BYTE *pTemplate);	

//�ȶ�ָ��
//���ط�����һ�����50�ֱ�ʾ�ɹ���Ҳ����������������,����Խ��,��ȫ�ȼ�Խ��
//pSrcData��pDstData��Ҫƥ���ָ��������pSrcData��pDstData��СΪ256BYTE
int WINAPI MatchTemplate(BYTE *pSrcData,BYTE *pDstData);

//�ȶ�ָ��
//���ط�����һ�����50�ֱ�ʾ�ɹ���Ҳ����������������,����Խ��,��ȫ�ȼ�Խ��
//pSrcData,ָ��������,��СΪ256BYTE
//pDstData,ָ��ģ��,��nDstSizeָ����һ��Ϊ512BYTE
int WINAPI MatchTemplateOne(BYTE *pSrcData,BYTE *pDstData,int nDstSize);
int WINAPI MatchTemplateOneEx(BYTE *pSrcData,int nSrcSize,BYTE *pDstData,int nDstSize);

//�ȶ�ָ��
//pSrcData��ָ��������,pSrcData��СΪ256BYTE
//pDstFullData,���ָ��ģ��
//nDstCount,ģ����
//nDstSize,ģ��Ĵ�С
//nThreshold,�ȶԳɹ�����
//����-1��ʾʧ��,����>=0,��ʾ���ָ��ģ����λ�ڸ�λ�õ�ģ��ȶԳɹ�
int WINAPI MatchTemplateaArray(BYTE *pSrcData,BYTE *pDstFullData,int nDstCount,int nDstSize,int nThreshold);


//������Ϣ���ھ��
void WINAPI SetMsgMainHandle(HWND hWnd);

//���豸
//style=1 �򿪴����豸,��OpenDevice(1,57600,1)
//style=0 ��USB�豸,��OpenDevice(0,0,0)
int WINAPI OpenDevice(int comnum=0,int nbaud=0,int style=0);

//�����豸
int WINAPI LinkDevice();
//�ر��豸
int WINAPI CloseDevice();

//��ȡָ��ͼ��
int WINAPI GetFingerImage(BYTE * imagedata,int * size);
int WINAPI GetFingerImageEx(BYTE * imagedata,int * size);
//�ڴ����ϻ�ָ��ͼ��
int WINAPI DrawFingerImage(BYTE * imagedata,int size,HDC hdc,int left,int top);
//�ڴ����ϻ�ָ��ͼ��,ͼ��ʹ��SDK�ղɼ���.
int WINAPI DrawImage(HDC hdc,int left,int top);

//����ͼ����ʾ����
BOOL WINAPI SetDispOption(BOOL bSet,double iZoom,COLORREF clrBack ,COLORREF clrForce,int nThick);
//����ͼ���ļ�(Ansi)
BOOL WINAPI SaveFpImageA(LPSTR filename,int type,int quality);
//����ͼ���ļ�(�պ���,����δʵ��)
BOOL WINAPI SaveFpImageW(LPWSTR filename);
//�����ж�ͼ����������
BOOL WINAPI SetImageVal(BOOL bSet,int min,int max);

///////////////////////////////////////////////////////////////////////////////////////////////////////////
//�����豸������

//��ȡ�豸��ģ����
int WINAPI GetTemplatesCount(int * count);
//����豸��ģ��
int WINAPI ClearTemplates();
//���豸�ϵǼ�ģ��
int WINAPI EnrollTemplate(int id);
//���豸��1:1ʶ��ģ��
int WINAPI VerifyTemplate(int id,int* iScore);
//���豸��1:Nʶ��ģ��
int WINAPI IdentifyTemplate(int * id);
//���豸��ɾ��ָ��ģ��
int WINAPI DeleteTemplate(int id);

//�Ǽ�ģ��
int WINAPI EnrollToPc(BYTE * templates,int * tempsize);
//�ɼ�ָ��������
int WINAPI GenCharToPc(BYTE * templates,int * tempsize);
//���豸�ϱȶ�����ģ��
int WINAPI MatchOneToOne(BYTE * templates1,int tempsize1,BYTE * templates2,int tempsize2,int* iScore);
//���豸�ϱȶ�����ģ��
int WINAPI MatchInDevice(BYTE * templates1,int tempsize1,BYTE * templates2,int tempsize2,int* iScore);
//���豸�ϲ���ָƥ��ģ��
int WINAPI VerifyInDevice(BYTE * templates1,int tempsize1,int* iScore);

///////////////////////////////////////////////////////////////////////////////////////////////////////////
//�̺߳���,ʵ��Ӧ����,��Ҫ�������º���

//�ɼ�ָ��ͼ��
void WINAPI CaptureImage();
//�ɼ�ָ��������
void WINAPI GenFpChar();
//�Ǽ�ָ��ģ��
void WINAPI EnrolFpChar();

//��ȡָ����Ϣ
int WINAPI GetWorkMsg();
//��ȡ������Ϣ
int WINAPI GetRetMsg();
//�ͷ���Ϣ
int WINAPI ReleaseMsg();

//��ȡָ��ͼ��
BOOL WINAPI GetImage(BYTE * imagedata,int * size);

//��ȡָ��������
BOOL WINAPI GetFpCharByGen(BYTE * tpbuf,int * tpsize);
//��ȡָ��ģ��
BOOL WINAPI GetFpCharByEnl(BYTE * fpbuf,int * fpsize);

//��ȡָ��������(�ַ���)
BOOL WINAPI GetFpStrByGen(char * tpstr);
//��ȡָ��ģ��(�ַ���)
BOOL WINAPI GetFpStrByEnl(char * fpstr);


//���ó�ʱ
VOID WINAPI SetTimeOut(double itime);

//�����Ƿ��ϴ�ͼ��
VOID WINAPI SetUpImage(BOOL bUpImage);

//����״̬��״̬
VOID WINAPI SetStatusLed(int type,int status);

//BYTE����ģ��ת�ַ���ģ��(ANSI)
VOID WINAPI CharByteToStr(BYTE * buf,int size,char * str);
//�ַ���ģ��תBYTE����ģ��(ANSI)
VOID WINAPI StrToCharByte(char * str,BYTE * buf,int * size);
//�ַ���ģ��ȶ�(ANSI)
int WINAPI MatchTemplateEx(char* pSrcData,char* pDstData);

//BYTE����ģ��ת�ַ���ģ��(UNICODE)
VOID WINAPI CharByteToUStr(BYTE * buf,int size,WCHAR * ustr);
//�ַ���ģ��תBYTE����ģ��(UNICODE)
VOID WINAPI UStrToCharByte(WCHAR * ustr,BYTE * buf,int * size);
//�ַ���ģ��ȶ�(UNICODE)
int WINAPI UMatchTemplateEx(WCHAR* puSrcData,WCHAR* puDstData);


//��ӡ
BOOL WINAPI DevicePrint(BYTE* buffer,int size);

#endif