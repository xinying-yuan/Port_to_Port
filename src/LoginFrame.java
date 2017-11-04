import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
public class LoginFrame extends JFrame implements ActionListener {
	
	public static JTextField user_name=null;
	public  static JPasswordField user_pass=null;
	public  static JButton button=null;
	
	String connectionUrl = "jdbc:mysql://localhost:3306/yxynetwork?userUnicode=true&characterEncoding=UTF8&serverTimezone=UTC";
    Connection conn=null;
    Statement sta=null;
    ResultSet result=null;
	
	public LoginFrame()
	{
		JPanel upPanel=new JPanel();
		upPanel.setLayout(new FlowLayout());
		JLabel name=new JLabel("用户名：");
		name.setHorizontalAlignment(SwingConstants.LEFT);
		user_name=new JTextField(15);
		upPanel.add(name);
		upPanel.add(user_name);
		
		JPanel midPanel=new JPanel();
		midPanel.setLayout(new FlowLayout());
		JLabel pass=new JLabel("密     码：");
		pass.setHorizontalAlignment(SwingConstants.LEFT);
		user_pass=new JPasswordField(15);
		midPanel.add(pass);
		midPanel.add(user_pass);
		button=new JButton("登陆");
		button.setBackground(Color.green);
		button.setSize(300,50);
		
		JPanel container=new JPanel();
		container.setLayout(new GridLayout(2,1));
		container.add(upPanel);
		container.add(midPanel);
		
		ImageIcon background=new ImageIcon("background.jpg");
		//添加图片背景效果
		JLabel imgLabel=new JLabel(background);
		this.getLayeredPane().add(imgLabel, new Integer(Integer.MIN_VALUE));
		imgLabel.setBounds(0,0,this.getWidth(),this.getHeight());
		imgLabel.setBounds(0, 0,background.getIconWidth(), background.getIconHeight());
		
		
		ImageIcon icon=new ImageIcon("cat.png");
		JLabel label=new JLabel(icon);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		
		
		
		Container cp=this.getContentPane();
		cp.setLayout(null);
		((JPanel)cp).setOpaque(false);
		
		
		this.setLayout(new FlowLayout());
		this.add(label);
		this.add(container);
		this.add(button);
		this.setSize(300,300);
		this.setResizable(false);
		this.setTitle("欢迎登陆");
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		button.addActionListener(this);
	}
	public static void main(String []args){
		new LoginFrame();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String name=user_name.getText();
		String pass=new String(user_pass.getPassword());
		System.out.println(pass+"用户输入的密码");
		System.out.println(name);
		
		if(name.length()==0||pass.length()==0){
			return;
		}else{				
			try {
				//Class.forName("com.mysql.jdbc.Driver");
				conn=DriverManager.getConnection(connectionUrl,"root","root");
				java.sql.Statement sta=conn.createStatement();
				
				String Sql="SELECT userpass FROM yxynetwork.login WHERE username=\""+name+"\"";
				System.out.println(Sql);
				result=sta.executeQuery(Sql);
				String realPass="";
		        if(result.next()){
		          realPass=result.getString(1);
		        }
		        if(realPass.equals(pass)){
		        	this.setVisible(false);
		        	new Myclient(name);
		        }
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		}
	}

}
