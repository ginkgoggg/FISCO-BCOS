package Redis_and_sql;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.apache.commons.lang3.ObjectUtils;
import java.io.*;


import javax.annotation.Resource;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;


public class Minio {

    private static boolean checkBucket = false;

    private MinioConfig minioConfig;

    public Minio(){
        this.minioConfig = new MinioConfig();
    }

    public FilterInputStream readFile(String path) throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InsufficientDataException, InternalException {
        return this.getInstance().getObject(GetObjectArgs.builder().bucket(minioConfig.getBucketName()).object(path).build());
    }

    public FilterInputStream readFile(String path, Long offset) {
        System.out.println(minioConfig.getBucketName());
        GetObjectResponse object;
        try {
            object = this.getInstance().getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(path)
                    .length(minioConfig.getFileShardSize()).offset(offset * minioConfig.getFileShardSize())
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return object;
    }


    private MinioClient getInstance() throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecret())
                .build();
        if (!checkBucket) {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConfig.getBucketName()).objectLock(false).build());
                checkBucket = true;
            }
        }
        return minioClient;
    }

    // public static void main(String[] args)throws Exception {
    //     Minio minio = new Minio();
    //     FilterInputStream  input = minio.readFile("2022-09-06/e7fcc428-1242-4225-9688-8bf9a5be4882.txt");
    //     BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
    //     String log_context = "";
    //     while ((log_context = bufferedReader.readLine()) != null){
    //         System.out.println(log_context);
    //     }
    //     IOUtils.toString(input,"UTF-8");
    // }
}
