package Redis_and_sql;

import io.minio.*;
import java.util.Properties;
import java.io.*;

public class MinioConfig {
    private static String endpoint;

    /**
     * key
     */
    private static String accessKey;

    /**
     * 密钥
     */
    private static String secret;

    /**
     * 桶名称
     */
    private static String bucketName;

    /**
     * 文件大小
     */
    private static Long fileShardSize;

    static Properties prop=new Properties();

    static String [] message=new String[5];{}

    static{
        try{
            String mypath = System.getProperty("miniopath");
            InputStream in = new BufferedInputStream(new FileInputStream(mypath));
            prop.load(in);
            message[0]=prop.getProperty("block.minio.access-key");

            message[1]=prop.getProperty("block.minio.secret");

            message[2]=prop.getProperty("block.minio.bucket-name");

            message[3]=prop.getProperty("block.minio.endpoint");

            message[4]=prop.getProperty("block.minio.file-shard-size");

            in.close();
        }catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public String getEndpoint() {
        endpoint = message[3];
        return endpoint;
    }

    public String getAccessKey() {
        accessKey = message[0];
        return accessKey;
    }

    public String getSecret() {
        secret = message[1];
        return secret;
    }

    public String getBucketName() {
        bucketName = message[2];
        return bucketName;
    }

    public Long getFileShardSize() {
        fileShardSize = Long.parseLong(message[4]);
        return fileShardSize;
    }
}