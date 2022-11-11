package Redis_and_sql;

import java.sql.*;
import java.io.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class InsertInfo {
    public  InsertInfo(String _text, String str) throws Exception{
        //String get = "9963\ndwasn\nthats all\nyss\nwho.pdf\nread\n213123123\n56435435\n213\nkey4";
        //id设置为传入的参数之一,或者说id是自增长的，那就可以不用传入
        //Integer id = num;
        String a = _text.trim();
        System.out.println(a);
        String[] matcheddata = a.split("\n");
        Long analyid = Long.parseLong(matcheddata[0]); //analysisid
        Long oid = Long.parseLong(matcheddata[1]); //opid
        // String datausername = matcheddata[3];
        String somemsg = matcheddata[2];
        Integer mtype = Integer.parseInt(matcheddata[3]);
        // String driver = "com.mysql.cj.jdbc.Driver";

        // String url="jdbc:mysql://localhost:3306/test?useSSL=false&serverTimezone=GMT%2B8";
    
        // String username="root";
    
        // String userpwd="123456";
    
        // String sql = "insert into t_analysis_result_copy(task_id, analysis_id, op_id, msg, msg_type) values (?,?,?,?,?)";
        Properties prop=new Properties();

        String [] message=new String[5];

        try{
            String mypath = System.getProperty("anamysqlpath");
            InputStream in = new BufferedInputStream(new FileInputStream(mypath));
            prop.load(in);
            message[0]=prop.getProperty("driver");

            message[1]=prop.getProperty("url");

            message[2]=prop.getProperty("username");

            message[3]=prop.getProperty("userpwd");

            message[4]=prop.getProperty("sql");

            in.close();
        }catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        try {

    
            //加载驱动程序
            Class.forName(message[0]);
            //创建连接
            Connection conn = DriverManager.getConnection(message[1],message[2],message[3]);
    
            PreparedStatement stmt = conn.prepareStatement(message[4]);

            // Class.forName(driver);
            // //创建连接
            // Connection conn = DriverManager.getConnection(url,username,userpwd);
    
            // PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, str);
            stmt.setLong(2, analyid);
            stmt.setLong(3, oid);
            stmt.setString(4, somemsg);
            stmt.setInt(5, mtype);
            // stmt.setString(8, str); //taskid
    
            stmt.executeUpdate();

            conn.close();
    
    
    
        } catch (ClassNotFoundException e) {
    
            // TODO Auto-generated catch block
    
            e.printStackTrace();
    
        } catch (SQLException e) {
    
            // TODO Auto-generated catch block
    
            e.printStackTrace();
    
        }
    
    }

    public static void main(String[] args){
        String get = "732\n35\n29\ncreate,open\n2";
        // System.out.println(get);
        String a = "TASK:12";
        try {
            InsertInfo insert = new InsertInfo(get, a);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
