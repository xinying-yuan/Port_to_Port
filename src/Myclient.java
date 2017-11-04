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
	private int connPort=7890;//client端进行一般消息传输的端口号
	private int downLoadPort=9876;//client端下载服务器端文件的端口号
	private int sendFile=4567;//client端上传文件需要使用的端口号
	private String ip="127.0.0.1";//client端的ip号 YOU NEED TO CHANGE HERE
	private String serverIp="127.0.0.1";//服务器端的ip号
	Myclient cMyclient=null;
	String savePath="";
	public static DataInputStream fileinput=null;
 	public static DatagramSocket dlSocket=null;//client端口接收从服务器端传来的文件的socket
	public Myclient(String name){
		//construc the graphics
		 this.setSize(450,350);
		 textArea =new JTextArea(12,12);
		 field=new JTextField(12);
		 button=new JButton("发送");
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
		 JLabel title=new JLabel("文件列表");
		 title.setHorizontalAlignment(SwingConstants.CENTER);
		 files=new JPanel();
		 files.setLayout(new GridLayout(7, 1));
		 uploadFile=new JButton("上传文件");
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
		 this.setTitle(name+"的小框框");
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
					//处理文件上传
					upload();
				}else if(answer.startsWith("filename")){
					//处理文件刷新
				  String [] file=answer.split("/");
				  String filename=file[1];
				  System.out.println("client接收到的filename"+filename);
				  //在右侧的filelist中显示可以供下载的文件名称
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
					//开始文件下载
					System.out.println(answer);
					int len=Integer.parseInt(answer.split("/")[1]);
					downloadFile(len);
				}else{
					//处理消息请求
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
    	private DatagramPacket pack;//数据包
    	private DatagramSocket sock;//tubu
    	private String filePath;
    	private String location;
      	byte[] buf=new byte[10240];
		@Override
		public void actionPerformed(ActionEvent e) {

			FileDialog dialog=new FileDialog(cMyclient, "请选择文件", FileDialog.LOAD);
			dialog.setVisible(true);
			filePath=dialog.getDirectory()+dialog.getFile();
			System.out.println("文件名稱"+dialog.getFile());
			location=filePath.replaceAll("\\\\", "/");
			
			try {
				fileinput =new DataInputStream(new BufferedInputStream(new FileInputStream(location)));
				int fileLen=fileinput.available();
				System.out.println(fileLen+"文件长度");
				//创建一个Tcp操作告知服务器端文件的一个基本信息是什么
				
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
    	DataOutputStream dOutputStream=null;//发送请求
    	
    	public DownLoad(String filename) {
			// TODO Auto-generated constructor stub
    		 this.filename=filename;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			//TODO Auto-generated method stub
			FileDialog dialog=new FileDialog(cMyclient, "请选择文件保存位置", FileDialog.SAVE);
			dialog.setVisible(true);
			Path=dialog.getDirectory()+dialog.getFile();
			location=Path.replaceAll("\\\\", "/");
			savePath=location;
			System.out.println("文件保存位置1"+location);
			try {
				 if (dlSocket==null){
						dlSocket=new DatagramSocket(downLoadPort);
						dlSocket.setSoTimeout(5000);
						System.out.println("初始化完成");
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
    	System.out.println("我是文件发送器，我已经开始发送文件了");
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
			     System.out.println("文件发送完毕");
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
		     System.out.println("我已经开始接收文件了");
		     for(int i=0;i<times;i++){
		    	 filePack=new DatagramPacket(temdata, temdata.length);
		    	 dlSocket.receive(filePack);
		    	 fileout.write(temdata,0,filePack.getLength());
		    	 fileout.flush();
		     }
		     
		     if(rest!=0){
		    	 System.out.println("文件长度不足或者剩余");
		    	 filePack=new DatagramPacket(temdata, temdata.length);
		    	 System.out.println("剩余cleint1");
		    	 dlSocket.receive(filePack);
		    	 System.out.println("剩余client2");
		    	 fileout.write(temdata,0,filePack.getLength());
		    	 fileout.flush();
		     }
		     
		     fileout.close();
		     System.out.println("文件写入完毕");
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
			//持续不断的对这个端口进行监听
			socket=new Socket(serverIp, connPort);
			new MyThread(socket).start();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//label.setText(JOptionPane.showInputDialog(this,"请输入您的姓名"));
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
