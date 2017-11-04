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
   private int port =7890;//对于信息接口的监听和分发
   private int udpPort=4567;//客户端上传文件到服务器端所使用的端口号
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
      //deliver infomation to each client 进行信息的转发
       DatagramSocket ds = null;//bug源头
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
		   //进行消息的转发
		   sendToAllClients(content);
	   }else if(content.startsWith("download")){
		   //客户端希望下载服务器端的文件
		   System.out.println(content);
		   String[] info=content.split("/");
		   String filename=info[1];
		   String ip=info[2];
		   int port=Integer.parseInt(info[3]);
		   System.out.println("客户端想要下载"+filename);
		   downPath="T:/workspaceForJava/Port_to_Port/Receive/"+filename;
		   DataInputStream serverFile=null;
		   DatagramSocket dataout=null;
		   DatagramPacket dataPack=null;
		   byte[] outbytes=new byte[10240];
		   try {
			   //SERVER读取文件的长度等信息然后加上准许命令发送给客户端
			   serverFile=new DataInputStream(new BufferedInputStream(new FileInputStream(downPath)));
			   int fileLen=serverFile.available();
			   
			   DataOutputStream downout=new DataOutputStream(only.getOutputStream());
			   downout.writeUTF("ALLOW"+"/"+fileLen);
			   downout.flush();
			   
			  //send the data to client
			   dataout=new DatagramSocket();
			   System.out.println("我是服务器，我已经开始发送文件了");
			   
			   int c=0;
			   while((c=serverFile.read(outbytes))!=-1){
				   dataPack=new DatagramPacket(outbytes, c,new InetSocketAddress(ip,port));
				   dataout.send(dataPack);
			   }
			  System.out.println("客户端需要的文件已经发送完毕");
			
	    	} catch (Exception e) {
			// TODO: handle exception
	    		e.printStackTrace();
		   }
	   }else{
		   //客户端希望上传文件到服务器端口
		   //此處處理的是文件信息需要返回YES信息並且開始使用udp來接收對應的文件內容
		   String headInfo[]=content.split("/");
		   String filename=headInfo[0];
		   String fileLen=headInfo[1];
		   int LEN=Integer.parseInt(fileLen);
		   path="T:/workspaceForJava/Port_to_Port/Receive/"+filename;
		try {
			DataOutputStream dOUT=new DataOutputStream(only.getOutputStream());
			dOUT.writeUTF("YES");
			dOUT.flush();//向文件的发送方发送允许命令
			
			fileOut=new DataOutputStream(new FileOutputStream(path));
			System.out.println("我是接收器，已經在接受數據了");
			int times=LEN/dataLen;
			int left=LEN%dataLen;
			System.out.println(LEN);
			for(int i=0;i<times;i++){
				System.out.println("服務器已經開始接受數據了");
				rP=new DatagramPacket(inbuff, inbuff.length);
				ds.receive(rP);
				fileOut.write(inbuff,0,rP.getLength());
				fileOut.flush();
				rP=null;
			}
			if(left!=0){
				System.out.println("我有剩餘1");
				rP=new DatagramPacket(inbuff,inbuff.length);
				System.out.println("我有剩餘2");
				ds.receive(rP);
				System.out.println("我有剩餘3");
				fileOut.write(inbuff,0,rP.getLength());
				System.out.println("我有剩餘4");
				fileOut.flush();
				System.out.println("我有剩餘5");
			}
			    fileOut.close();
			    //向所有的客户端更新文件列表
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