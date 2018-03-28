package com.tisson.fingerprint.test;
import java.io.*; 
import java.util.Timer;
import java.util.TimerTask;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import sun.misc.*;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public class FGTFPDemo  extends Frame implements ActionListener{
	
	static FGTFPDemo jc = new FGTFPDemo();

	static Button btn1=new Button("Open Device");
    static Button btn2=new Button("Close Device");
    static Button btn3=new Button("Printer Test");
    static Button btn4=new Button("Enroll Template");
    static Button btn5=new Button("Identify Template");
    static Label lab1=new Label("");
    static Panel panel1=new Panel();
    
    private final Timer timer = new Timer();
    
    private byte refbuf[]=new byte[512];
    private int refsize[]=new int[1];    
    private byte matbuf[]=new byte[512];	
    private int matsize[]=new int[1];
    
    public static void main(String[] args)//throws   IOException 
    {
		System.out.println("java.library.path="+System.getProperty("java.library.path"));

    	btn1.setSize(160,30);    	btn1.setLocation(20,40);    	jc.add(btn1);
    	btn2.setSize(160,30);    	btn2.setLocation(20,80);    	jc.add(btn2);    	
    	btn3.setSize(160,30);    	btn3.setLocation(20,120);    	jc.add(btn3);
    	btn4.setSize(160,30);    	btn4.setLocation(20,160);    	jc.add(btn4);
    	btn5.setSize(160,30);    	btn5.setLocation(20,200);    	jc.add(btn5);
    	
    	lab1.setSize(200,20);
    	lab1.setLocation(20,240);
    	jc.add(lab1);
    	
    	panel1.setSize(256,288);
    	panel1.setLocation(20, 260);
    	jc.add(panel1);
    	
    	btn1.addActionListener(jc);
    	btn2.addActionListener(jc);
    	btn3.addActionListener(jc);
    	btn4.addActionListener(jc);
    	btn5.addActionListener(jc);
    	
    	jc.setTitle("Java Fingerprint SDK Demo");
    	jc.setSize(400,600);
    	jc.setLocation(200, 200);
    	jc.show();
    	jc.addWindowListener(new WindowAdapter(){
    		@Override	public void windowClosing(WindowEvent e){
    			System.exit(0);
    			}
    	});
    	jc.start();
    }

	@Override
	public void actionPerformed(ActionEvent e)
    {
    	Object obj=e.getSource();    	
    	if(obj.equals(btn1)){   
    		int r=fpLibrary.INSTANCE.OpenDevice(0,0,0);
    		if(r==1){
    			if(fpLibrary.INSTANCE.LinkDevice()==1){
    				lab1.setText("Device Ready!");
    			}else{
    				lab1.setText("Link Device Fail");
    			}
    		}else{
    			lab1.setText("Open Device Fail");
    		}
    	}else if(obj.equals(btn2)){
    		fpLibrary.INSTANCE.CloseDevice();
    		lab1.setText("Close Device");
    	}else if(obj.equals(btn3)){
    		String s="\nPrinter Test\n";
    		byte[] buffer=s.getBytes();
    		fpLibrary.INSTANCE.DevicePrint(buffer,buffer.length);
    	}else if(obj.equals(btn4)){
    		fpLibrary.INSTANCE.EnrolFpChar();
    	}else if(obj.equals(btn5)){
    		fpLibrary.INSTANCE.GenFpChar();
    	}
    }
	
	public void start()
    {
    	timer.schedule(new TimerTask()
    	{
    		public void run()
    		{    			
    			fpsmessage();
    		}
    		private void fpsmessage()
    		{  			
    			int fpsmsg;
    			int retmsg;
    			fpsmsg=fpLibrary.INSTANCE.GetWorkMsg();
    			retmsg=fpLibrary.INSTANCE.GetRetMsg();
    			switch(fpsmsg)
    			{
    			case 0x1:
    				lab1.setText("Please Open Device");
    				break;
    			case 0x02:
    				lab1.setText("Please Place Finger");
    				break;
    			case 0x03:
    				lab1.setText("Please Lift Finger");
    				break;
    			case 0x05:
    				if(retmsg==1){
    					fpLibrary.INSTANCE.GetFpCharByGen(matbuf, matsize);
						int ret=fpLibrary.INSTANCE.MatchTemplateOne(matbuf,refbuf,refsize[0]);
						lab1.setText("Match Val : "+String.valueOf(ret));
    				}else{
    					lab1.setText("Capture Fail");
    				}    				
    				break;
    			case 0x06:
    				if(retmsg==1){
    					lab1.setText("Enrol Template OK");
    					fpLibrary.INSTANCE.GetFpCharByEnl(refbuf,refsize);
    				}else{
    					lab1.setText("Enrol Template Fail");
    				}    				
    				break;
    			case 0x07:
    				DrawImage();
    				break;
    			case 0x08:
    				lab1.setText("Time Out");
    				break;
    			}
   	 		}
    	},0,100);
    }
	
    public void DrawImage()
    {
    	int w=256;
    	int h=288;    	
    	byte [] imageraw=new byte[w*h];
    	int [] imagesize=new int[1];
    	int []	rawpic=new int[w*h];    	
    	Image img;
    	fpLibrary.INSTANCE.GetImage(imageraw,imagesize);    	
    	for(int i=0;i<w*h;i++)
    	{
    		int m=(int)(imageraw[i]&0xff);  
    		rawpic[i]=m|m<<8|m<<16|255<<24;
    	}    	
    	img = createImage(new MemoryImageSource(w, h, rawpic, 0, w));
    	Graphics g=panel1.getGraphics();
    	g.drawImage(img, 20, 260,w, h, panel1);
    }
	
	public interface fpLibrary extends Library {
		fpLibrary INSTANCE = (fpLibrary)Native.loadLibrary(("fpengine.dll"),	fpLibrary.class);
		int OpenDevice(int comnum,int nbaud,int style);
		int LinkDevice();
		int CloseDevice();
		
		int DevicePrint(byte[] buffer,int size);
		int GetImage(byte[] imagedata,int[] size);
		void GenFpChar();
		void EnrolFpChar();
		int GetWorkMsg();
		int GetRetMsg();
		
		int GetFpCharByGen(byte[] tpbuf,int[] tpsize);
		int GetFpCharByEnl(byte[] fpbuf,int[] fpsize);
		
		int ChangeTemplateType(int type,byte[] input,byte[] output);
		
		int MatchTemplateOne(byte[] pSrcData,byte[] pDstData,int nDstSize);
		int MatchTemplate(byte[] pSrcData,byte[] pDstData);
	}

}
