һ����װ
    ��ѹsfip-standalone-min-1.0.6-122.zip��Ҫ��װ��Ŀ¼��˫��sfip-setup.bat��������ʾ��
 
�����������˵��
1��������÷���
    post ���� http://127.0.0.1:29093/services/tisson/FingerprintUtils/call
2���������˵��
	������{"appName":"","beanName":"getCmdPrompt","msg":""} 
	appNameĿǰ����Ϊ""
	beanName��msg���¾��������˵��

3�����ذ���˵��
	������{"responseCode":"FPU0100000000","responseContent":"","result":"(SN=3832173300917),2"}
	responseContentΪresponseCode��ԭ����˵��
	resultΪ���ذ�������
	responseCode: ��1-3λΪFPU(ָ�ƹ���),��3-4λ:00��ʾ��ѯ,01��ʾ����,03��ʾ����    ��6-7λ:00������� ,99����δ���,88���� �쳣����       
 		��6λΪ��Ӧ��
 	responseCode����
        	FPU0000000000 �ɼ����
        	FPU0000000004 �ɼ���ɺ�����ָ�ư���,��Ӱ��ɼ����
				�� һ�㲻����֣���Ϊ�յ�000000��Ӧ�������ʱ����

        	FPU0099000003 �밴ͬһ����ָ3��
        	FPU0099000002 �밴ͬһ����ָ2��
        	FPU0099000001 �밴ͬһ����ָ1��

        	FPU0088000100 û�вɼ��豸
        	FPU0088000101 �豸����
        	FPU0088000102 �豸æ
	
        	FPU0088000200 ���ڲɼ�״̬
        	FPU0088000201 ���˲�ͬ��ָ
        	FPU0088000202 ��ȡ������ʧ��
        	FPU0088000203 δ֪�Ĵ���
        	FPU0088000204���ָ�Ƶ��ڴ�ʧ��
	result:
	       responseCode��6λΪFPU0000000000 ��FPU00000000004ʱ,result�ǲɼ�����ָ�������룬��󳤶�Ϊ4096

��������˵��
1������ɼ�
    ʹ�ó���:
 	ÿ��Ҫ�ɼ�ʱ��һ��
 	������responseCodeΪFPU0100000000ʱ�Ǳ�ʾ����ɹ� ,����ֵ�Ƿ���ʧ��
 	
    ���������{"appName":"","beanName":"cmdCollection","msg":""} 
    ����ֵ��
         ������{"responseCode":"FPU0100000000","responseContent":"","result":"(SN=3832173300917),2"}

2����ȡ�ɼ�״̬
    ʹ�ó���:
 	����cmdCollection��ʼ�ɹ���responseCode=FPU0100000000����ʱ100ms��ѯһ��  
 	������responseCode��3λ��FPU0099000003,��FPU0099000002,��FPU0099000001ʱ�ǲɼ����ڽ���,
 	                                                            ��FPU0000000000��FPU0000000004ʱ�Ǳ�ʾ�ɼ���� ,����ֵ�ǲɼ�ʧ��
          
    ���������{"appName":"","beanName":"getCurrentOwnerRegTempBase64","msg":""} 
    ����ֵ��
         ����:{"responseCode":"FPU0000000000","responseContent":"","result":"libzkfp:714:S4lTUzIxAAACysk..."}
 

    