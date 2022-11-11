package log;

import log.CommonLog;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class LogTestDemo {
    public void test1() {
        String LogType = "Syslog_linux";
        String log_context = "May  5 10:20:57 iZbp1a65x3r1vhpe94fi2qZ systemd: Started System Logging Service.";
        CommonLog log = new CommonLog(LogType, log_context);
        System.out.println(log.print("Syslog_linux"));
    }
    public void test2() throws Exception {
        String filepath = "E:\\NSTL实验室\\9院区块链项目\\dns.txt";
        FileInputStream inputStream = new FileInputStream(filepath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String str = "";
        while ((str = bufferedReader.readLine()) != null) {
            CommonLog log1 = new CommonLog("DNSlog_win", str);
            System.out.println(log1.print("DNSlog_win"));
            System.out.printf("log: %s\n", str);
        }
        inputStream.close();
        bufferedReader.close();
    }
    // public static void main(String[] args) throws Exception {
    //     LogTestDemo logTestDemo = new LogTestDemo();
    //     logTestDemo.test1();
    // }
}
