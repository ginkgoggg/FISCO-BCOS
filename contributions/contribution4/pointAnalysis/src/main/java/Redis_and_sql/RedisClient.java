package Redis_and_sql;

import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.*;
import Filter.Filter;
import java.util.Properties;

import java.io.*;

import com.alibaba.fastjson.*;

public class RedisClient {

    private static int DEFAULT_TIMEOUT = 5000;

    private static boolean ENABLE_LOOP = false;

    // public static JedisCluster clientforsub;

    // public static JedisCluster client;

    public static String logtask;

    public static String logkey;

    public static String logvalue;

    public static String changevalue;

    public static Jedis jedis;

    public static Jedis jedis1;

    // 连接redis群组做的准备
    

    static  {
        Properties prop=new Properties();

        String [] message=new String[5];{}

        try{
            String mypath = System.getProperty("redispath");
            InputStream in = new BufferedInputStream(new FileInputStream(mypath));
            prop.load(in);
            message[0]=prop.getProperty("redis-server");

            // message[1]=prop.getProperty("redis-server2");

            // message[2]=prop.getProperty("redis-server3");

            in.close();
        }catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(60000);
        config.setMaxIdle(1000);
        config.setTestOnBorrow(true);
        Set<HostAndPort> nodes = new HashSet<>();


        
        // nodes.add(new HostAndPort(message[0], 6379));
        // nodes.add(new HostAndPort(message[0], 6380));
        // nodes.add(new HostAndPort(message[1], 6379));
        // nodes.add(new HostAndPort(message[1], 6380));
        // nodes.add(new HostAndPort(message[2], 6379));
        // nodes.add(new HostAndPort(message[2], 6380));
        // client = new JedisCluster(nodes, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, config);
        // clientforsub = new JedisCluster(nodes, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, config);
        jedis = new Jedis(message[0],6379);
        jedis1 = new Jedis(message[0],6379);
    }

    

    public static void loopUpdate(String chan)throws Exception {
        RedisClient.jedis.subscribe(new JedisPubSub() {
         @Override
         public void onMessage(String channel, String message) {
             System.out.println("收到了" + channel + "频道发来消息：" +  message);
             logtask = "TASK:ANALYSIS:" + message; //作为key去redis里寻找value
             System.out.println(channel + ":" + logtask); //message要作为数据库里的task_id字段
             logvalue = getvalue(logtask); //value,json
             System.out.println("sth:" + logvalue);                    
             try {
                 HandleJson handlejson = new HandleJson(logvalue, message);
                 handlejson.match();
             } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
 
         @Override
         public void onSubscribe(String channel, int subscribedChannels) {
             System.out.println("订阅了" + channel + "频道");
             System.out.println(channel + ":" + subscribedChannels);
         }
     }, chan);
     }

    // 获取value
    public static String getvalue(String str) {
        String a = jedis1.get(str);
        // String a = client.get(str);
        return a;
    }

    // 修改value
    public static void renewredis(String strkey, String strvalue) {
        jedis1.getSet(strkey, strvalue);
        // client.getSet(strkey, strvalue);
    }



    public static void main(String[] args) throws Exception{
        RedisClient.loopUpdate("analysis");
    }   
}

