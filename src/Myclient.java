import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.management.loading.PrivateClassLoader;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class Myclient extends JFrame {
	private static Socket socket;
	JPanel panel;
	JTextArea textArea;
	JButton button;
	JLabel label;
	JTextField field;
	JButton uploadFile;
	JPanel files;
	private int connPort=7890;//client�˽���һ����Ϣ����Ķ˿ں�
	private int downLoadPort=9876;//client�����ط��������ļ��Ķ˿ں�
	private int sendFile=4567;//client���ϴ��ļ���Ҫʹ�õĶ˿ں�
	private String ip="127.0.0.1";//client�˵�ip�� YOU NEED TO CHANGE HERE
	private String serverIp="127.0.0.1";//�������˵�ip��
	Myclient cMyclient=null;
	String savePath="";
	public static DataInputStream fileinput=null;
 	public static DatagramSocket dlSocket=null;//client�˿ڽ��մӷ������˴������ļ���socket
	public Myclient(String name){
		//construc the graphics
		 this.setSize(450,350);
		 textArea =new JTextArea(12,12);
		 field=new JTextField(12);
		 button=new JButton("����");
		 label=new JLabel("");
		 label.setText(name);
		 button.setBackground(Color.green);
		 
		 
		 JPanel panel=new JPanel();
		 panel.setLayout(new FlowLayout());
		 panel.add(button);
		 panel.add(field);
		 //panel.add(label);
		 button.addActionListener(new SendMsg());
		 
		 
		 JPanel filePanel=new JPanel();
		 filePanel.setLayout(new BorderLayout());
		 JLabel title=new JLabel("�ļ��б�");
		 title.setHorizontalAlignment(SwingConstants.CENTER);
		 files=new JPanel();
		 files.setLayout(new GridLayout(7, 1));
		 uploadFile=new JButton("�ϴ��ļ�");
		 uploadFile.setBackground(Color.blue);
		 filePanel.add(title,BorderLayout.NORTH);
		 filePanel.add(files,BorderLayout.CENTER);
		 filePanel.add(uploadFile, BorderLayout.SOUTH);
		 uploadFile.addActionListener(new uploadFile());
		 
		 JPanel leftPanel=new JPanel();
		 leftPanel.setLayout(new BorderLayout());
		 leftPanel.add(textArea,BorderLayout.CENTER);
		 leftPanel.add(panel,BorderLayout.SOUTH);
		 
		 JPanel midPanel=new JPanel();
		 midPanel.setLayout(new GridLayout(1, 2));
		 midPanel.add(leftPanel);
		 midPanel.add(filePanel);
		 
		 this.setLayout(new BorderLayout());
		 this.add(midPanel, BorderLayout.CENTER);
		 this.setTitle(name+"��С���");
		 getMsg();
		 this.setVisible(true);
		 cMyclient=this;
	}
	class MyThread extends Thread{
		Socket socket=null;
	 public MyThread(Socket  socket) {
		// TODO Auto-generated constructor stub
		 this.socket=socket;
		}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		DataInputStream dInputStream=null;
		byte[] bytes=new byte[10240];
		try {
			   while(true){
				 dInputStream=new DataInputStream(socket.getInputStream());
				 String answer=dInputStream.readUTF();
				if(answer.equalsIgnoreCase("YES")){
					//�����ļ��ϴ�
					upload();
				}else if(answer.startsWith("filename")){
					//�����ļ�ˢ��
				  String [] file=answer.split("/");
				  String filename=file[1];
				  System.out.println("client���յ���filename"+filename);
				  //���Ҳ��filelist����ʾ���Թ����ص��ļ�����
				  JLabel serve_file=new JLabel(filename);
				  serve_file.setBackground(Color.gray);
				  JButton button=new JButton("download");
				  button.setBackground(Color.cyan);
				  JPanel panel=new JPanel();
				  panel.setLayout(new GridLayout(1, 2));
				  panel.add(serve_file);
				  panel.add(button);
				  DownLoad downloadFile=new DownLoad(serve_file.getText());
				  button.addActionListener(downloadFile);
				  files.add(panel);
				  files.validate();
				  files.repaint();  
				}else if(answer.startsWith("ALLOW")){
					//��ʼ�ļ�����
					System.out.println(answer);
					int len=Integer.parseInt(answer.split("/")[1]);
					downloadFile(len);
				}else{
					//������Ϣ����
					textArea.append(answer+"\n");
				}
				}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	}
    class uploadFile implements ActionListener{
    	private DatagramPacket pack;//���ݰ�
    	private DatagramSocket sock;//tubu
    	private String filePath;
    	private String location;
      	byte[] buf=new byte[10240];
		@Override
		public void actionPerformed(ActionEvent e) {

			FileDialog dialog=new FileDialog(cMyclient, "��ѡ���ļ�", FileDialog.LOAD);
			dialog.setVisible(true);
			filePath=dialog.getDirectory()+dialog.getFile();
			System.out.println("�ļ����Q"+dialog.getFile());
			location=filePath.replaceAll("\\\\", "/");
			
			try {
				fileinput =new DataInputStream(new BufferedInputStream(new FileInputStream(location)));
				int fileLen=fileinput.available();
				System.out.println(fileLen+"�ļ�����");
				//����һ��Tcp������֪���������ļ���һ��������Ϣ��ʲô
				
			    String headInfo=dialog.getFile()+"/"+fileLen;
			    DataOutputStream out=new DataOutputStream(socket.getOutputStream());
				out.writeUTF(headInfo);
				out.flush();
			} catch (Exception e2) {
				e2.printStackTrace();
				// TODO: handle exception
			}
		}
    	
    }
    //download the files from the server side
    class  DownLoad implements ActionListener{
    	//give the input file path and send the required file name to the server side
    	String filename="";
    	String Path="";
    	String location="";
    	DataOutputStream dOutputStream=null;//��������
    	
    	public DownLoad(String filename) {
			// TODO Auto-generated constructor stub
    		 this.filename=filename;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			//TODO Auto-generated method stub
			FileDialog dialog=new FileDialog(cMyclient, "��ѡ���ļ�����λ��", FileDialog.SAVE);
			dialog.setVisible(true);
			Path=dialog.getDirectory()+dialog.getFile();
			location=Path.replaceAll("\\\\", "/");
			savePath=location;
			System.out.println("�ļ�����λ��1"+location);
			try {
				 if (dlSocket==null){
						dlSocket=new DatagramSocket(downLoadPort);
						dlSocket.setSoTimeout(5000);
						System.out.println("��ʼ�����");
				}
				dOutputStream=new DataOutputStream(socket.getOutputStream());
				dOutputStream.writeUTF("download"+"/"+filename+"/"+ip+"/"+downLoadPort);
				dOutputStream.flush();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
    }
    public void upload(){
    	System.out.println("�����ļ������������Ѿ���ʼ�����ļ���");
    	DatagramSocket sock;
    	DatagramPacket myPacket;
    	byte[] buff=new byte[10240];
			 try {
				 sock=new DatagramSocket();
				 int c=0;
			     while ((c=fileinput.read(buff))!=-1) {
			    	 myPacket=new DatagramPacket(buff, c,new InetSocketAddress(serverIp,sendFile));
			    	 sock.send(myPacket);
				}
			     System.out.println("�ļ��������");
			     fileinput.close();
			     fileinput=null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				try{
					fileinput.close();	
				}catch (Exception e2) {
					// TODO: handle exception
					e2.printStackTrace();
				}
				e.printStackTrace();
			}
    }
    
    public void downloadFile(int len){
    	DataOutputStream fileout;
    	DatagramPacket filePack;
    	byte[] temdata=new byte[10240];
        try {
		     fileout=new DataOutputStream(new FileOutputStream(savePath));
		     int times=len/10240;
		     int rest=len%10240;
		     System.out.println("���Ѿ���ʼ�����ļ���");
		     for(int i=0;i<times;i++){
		    	 filePack=new DatagramPacket(temdata, temdata.length);
		    	 dlSocket.receive(filePack);
		    	 fileout.write(temdata,0,filePack.getLength());
		    	 fileout.flush();
		     }
		     
		     if(rest!=0){
		    	 System.out.println("�ļ����Ȳ������ʣ��");
		    	 filePack=new DatagramPacket(temdata, temdata.length);
		    	 System.out.println("ʣ��cleint1");
		    	 dlSocket.receive(filePack);
		    	 System.out.println("ʣ��client2");
		    	 fileout.write(temdata,0,filePack.getLength());
		    	 fileout.flush();
		     }
		     
		     fileout.close();
		     System.out.println("�ļ�д�����");
//		     if(dlSocket!=null){
//		    	 dlSocket.close();
//		    	 dlSocket=null;
//		     }
		 } catch (Exception e) {
		// TODO Auto-generated catch block
		    e.printStackTrace();
     	}	
    }
	public void getMsg(){
		try {
			//�������ϵĶ�����˿ڽ��м���
			socket=new Socket(serverIp, connPort);
			new MyThread(socket).start();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//label.setText(JOptionPane.showInputDialog(this,"��������������"));
	}
	class SendMsg implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
		    SimpleDateFormat format=new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		    String time=format.format(new Date());
		    try {
				DataOutputStream outputStream=new DataOutputStream(socket.getOutputStream());
				outputStream.writeUTF(time+" "+label.getText()+":"+field.getText());
				outputStream.flush();
				field.setText("");
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
		}
		
	}
}
