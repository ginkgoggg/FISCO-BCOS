package Filter;

import log.CommonLog;
import log.LogAdapter;

import java.io.*;
import java.util.ArrayList;

public class FilterTestDemo {
    public void test(String LogType, String log_text,String _file_object_name,String _file_object_operations) {
        Filter filter = new Filter(LogType, log_text,_file_object_name,_file_object_operations);
        System.out.println(filter.filter_by_name());
    }

    public void match() throws Exception {
        String filepath = "F:\\研究生\\testlog.txt";
        FileInputStream inputStream = new FileInputStream(filepath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        int sum_line = getLineNumber(filepath);
        int count = 0;
//        System.out.println(sum_line);
        String log_context = "";
        while ((log_context = bufferedReader.readLine()) != null) {
            //LogType(this.gettype()) = "Syslog_linux"
//            CommonLog log1 = new CommonLog(this.gettype(), log_context);
            String LogType = "Syslog_linux";
//            String log_text = "May  5 10:20:57 iZbp1a65x3r1vhpe94fi2qZ systemd: file2.pdf create";
            String file_object_name = "file1.pdf";
            String file_object_operations = "create,open";
            Filter filter = new Filter(LogType, log_context,file_object_name,file_object_operations);
            if (filter.filter_by_name()) {
                ArrayList<String> objset = filter.filter_by_dataobj_operates();
                String[] obj = file_object_operations.trim().split(",");
                if(objset != null && objset.size() > 0){
                    for (int i = 0; i < objset.size(); i++){
                        for(int j = 0; j < obj.length; j++){
                            if (objset.get(i).equals(obj[j])){
                                System.out.println(filter.commonLog.print(LogType));
                                System.out.printf("log: %s\n", log_context);
                                System.out.println(objset.get(i));
                            }
                        }
                    }
                }
            }    
            count ++;
            System.out.println((double) count/sum_line);
        }
        inputStream.close();
        bufferedReader.close();
}


    public int getLineNumber(String filepath) {
        File file = new File(filepath);
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
                lineNumberReader.skip(Long.MAX_VALUE);
                int lines = lineNumberReader.getLineNumber() + 1;
                fileReader.close();
                lineNumberReader.close();
                return lines;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static void main(String[] args) throws Exception {
        FilterTestDemo filterTestDemo = new FilterTestDemo();
        filterTestDemo.match();
    }
}
