import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;

import javax.sound.sampled.DataLine;
import javax.xml.crypto.Data;
public class Myserver{
	
   public  static HashSet<Socket> clients=new HashSet<>();
   ServerSocket socket;
   Socket accept;
   private int port =7890;//������Ϣ�ӿڵļ����ͷַ�
   private int udpPort=4567;//�ͻ����ϴ��ļ�������������ʹ�õĶ˿ں�
   public Myserver(){
	   try {
		socket=new ServerSocket(port);
		accept=new Socket();
		while(true){
			accept=socket.accept();
			clients.add(accept);
			new serveThread(accept, clients).start();
		}
	} catch (Exception e) {
		// TODO: handle exception
		e.printStackTrace();
	}
   }
   class serveThread extends Thread{
	   Socket threadsocket;
	   HashSet<Socket> clients=new HashSet<>();
	   public serveThread(Socket socket,HashSet<Socket> clients){
		   this.threadsocket=socket;
		   this.clients=clients;
      }
	   
	   public void run(){
		   try {
			DataInputStream dInputStream=new DataInputStream(threadsocket.getInputStream());
			while(true){
				SendMsg(dInputStream.readUTF(),threadsocket);
			}
		} catch (Exception e) {
			// TODO: handle exception
		    e.printStackTrace();
		}
	   }
   }
      //deliver infomation to each client ������Ϣ��ת��
       DatagramSocket ds = null;//bugԴͷ
   public void SendMsg(String content,Socket only){
	   System.out.println(content);
	   PrintWriter pw;
	   DataOutputStream fileOut;
	   String path="";
	   int dataLen=10240;
	   String downPath="";
	   DatagramPacket rP=null;
	   byte [] inbuff=new byte[10240];
	   try {
		   if(ds==null){
				ds=new DatagramSocket(udpPort);
				System.out.println("hello world");
			}
        	} catch (Exception e) {
		// TODO: handle exception
        		e.printStackTrace();
	     } 
	   if(content.matches("[0-9]+.*")){
		   //������Ϣ��ת��
		   sendToAllClients(content);
	   }else if(content.startsWith("download")){
		   //�ͻ���ϣ�����ط������˵��ļ�
		   System.out.println(content);
		   String[] info=content.split("/");
		   String filename=info[1];
		   String ip=info[2];
		   int port=Integer.parseInt(info[3]);
		   System.out.println("�ͻ�����Ҫ����"+filename);
		   downPath="T:/workspaceForJava/Port_to_Port/Receive/"+filename;
		   DataInputStream serverFile=null;
		   DatagramSocket dataout=null;
		   DatagramPacket dataPack=null;
		   byte[] outbytes=new byte[10240];
		   try {
			   //SERVER��ȡ�ļ��ĳ��ȵ���ϢȻ�����׼������͸��ͻ���
			   serverFile=new DataInputStream(new BufferedInputStream(new FileInputStream(downPath)));
			   int fileLen=serverFile.available();
			   
			   DataOutputStream downout=new DataOutputStream(only.getOutputStream());
			   downout.writeUTF("ALLOW"+"/"+fileLen);
			   downout.flush();
			   
			  //send the data to client
			   dataout=new DatagramSocket();
			   System.out.println("���Ƿ����������Ѿ���ʼ�����ļ���");
			   
			   int c=0;
			   while((c=serverFile.read(outbytes))!=-1){
				   dataPack=new DatagramPacket(outbytes, c,new InetSocketAddress(ip,port));
				   dataout.send(dataPack);
			   }
			  System.out.println("�ͻ�����Ҫ���ļ��Ѿ��������");
			
	    	} catch (Exception e) {
			// TODO: handle exception
	    		e.printStackTrace();
		   }
	   }else{
		   //�ͻ���ϣ���ϴ��ļ����������˿�
		   //��̎̎������ļ���Ϣ��Ҫ����YES��Ϣ�K���_ʼʹ��udp����Ռ������ļ�����
		   String headInfo[]=content.split("/");
		   String filename=headInfo[0];
		   String fileLen=headInfo[1];
		   int LEN=Integer.parseInt(fileLen);
		   path="T:/workspaceForJava/Port_to_Port/Receive/"+filename;
		try {
			DataOutputStream dOUT=new DataOutputStream(only.getOutputStream());
			dOUT.writeUTF("YES");
			dOUT.flush();//���ļ��ķ��ͷ�������������
			
			fileOut=new DataOutputStream(new FileOutputStream(path));
			System.out.println("���ǽ��������ѽ��ڽ��ܔ�����");
			int times=LEN/dataLen;
			int left=LEN%dataLen;
			System.out.println(LEN);
			for(int i=0;i<times;i++){
				System.out.println("�������ѽ��_ʼ���ܔ�����");
				rP=new DatagramPacket(inbuff, inbuff.length);
				ds.receive(rP);
				fileOut.write(inbuff,0,rP.getLength());
				fileOut.flush();
				rP=null;
			}
			if(left!=0){
				System.out.println("����ʣ�N1");
				rP=new DatagramPacket(inbuff,inbuff.length);
				System.out.println("����ʣ�N2");
				ds.receive(rP);
				System.out.println("����ʣ�N3");
				fileOut.write(inbuff,0,rP.getLength());
				System.out.println("����ʣ�N4");
				fileOut.flush();
				System.out.println("����ʣ�N5");
			}
			    fileOut.close();
			    //�����еĿͻ��˸����ļ��б�
			    sendToAllClients("filename"+"/"+filename);
		} catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
		}   
	   }
   }
   public void sendToAllClients(String content){
	   Iterator out=clients.iterator();
	   DataOutputStream dout=null;
	   while(out.hasNext()){
		   Socket socket=(Socket)out.next();
		   try {
			dout=new DataOutputStream(socket.getOutputStream());
			dout.writeUTF(content);
			dout.flush();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}  
	   } 
   }
   public static void main(String[] args){
	   new Myserver();
   }
}