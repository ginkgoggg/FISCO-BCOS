package Redis_and_sql;

import com.alibaba.fastjson.*;

import Filter.Filter;

import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;

import java.io.*;

import java.util.ArrayList;



public class HandleJson{

    private static String sourcefilepath;

    // private static String[] sourcedataObjname = new String[100];{}

    private static String sourcedataObjname;

    private static String sourcefilehash;

    private static String sourcefileid;

    private static double process;

    private static String msgfromsourcelog;

    // private static String taskid;

    private static String analysisid;

    // private static String[] sourceop = new String[100];{}

    private static String sourceop = "";

    // private static String[] targetop = new String[100];{};

    private static String targetop = "";

    private static String targetfilepath;

    // private static String dataObjname;

    // private static String dataObjoperation = "";

    private static String targetfilehash;

    private static String targetfileid;

    // private static String[] targetdataObjname = new String[100];{};

    private static String targetdataObjname;

    private static String msgfromtargetlog;

    private static String[] opid = new String[100];{};

    private static String inserttosql;

    private static String remsg;

    private static String redisjson;

    private static int sourcelogtype;

    private static int targetlogtype;

    private static int status;
    // 从这儿开始是对json格式进行转换
    // public static String data01 = "{\"dataObj\":[{\"name\":\"测试\",\"operationList\":[\"新增\",\"更新\"]}],\"fileHash\":\"bf76f49936a4047814b62c3c4cac71e525d77168\",\"fileId\":73211634668015616,\"filePath\":\"2022-09-06/6377c6af-93cd-4407-b9c8-865fe9ce6923.xls\",\"process\":0.0,\"status\":1,\"type\":5}";

    // public static String data02 = JSON.toJSONString(data01);		 // 序列化为JSON字符串

    // public static String data03 = data02.replace("\\", "");    	 //通过 replace 去掉斜杠

    // public static String data04 = data03.substring(1, data03.length() - 1);   //去掉首尾的引号
    //对接收的json进行简单的解析
    public HandleJson(String _json_text, String redismsg){
        redisjson = _json_text;
        _json_text = _json_text.substring(1);
        _json_text = _json_text.substring(0,_json_text.length()-1);
        _json_text = _json_text.replace("\\", "");
        System.out.println(sourceop);
        System.out.println(targetop);
        JSONObject jsonObject = JSONObject.parseObject(_json_text);
        JSONArray analysisops = jsonObject.getJSONArray("analysisOps");
        // sourceop = analysisops.getJSONObject(0).getString("sourceOp"); //源对象的某操作名
        // targetop = analysisops.getJSONObject(0).getString("targetOp");
        // opid = analysisops.getJSONObject(0).getString("id");
        sourcedataObjname = analysisops.getJSONObject(0).getString("sourceObjName");
        targetdataObjname = analysisops.getJSONObject(0).getString("targetObjName");
        for(int j = 0;j < analysisops.size(); j++){
            if(j == 0 && j == analysisops.size() - 1){
                sourceop = analysisops.getJSONObject(j).getString("sourceOp");
                targetop = analysisops.getJSONObject(j).getString("targetOp");
            }else if(j == 0 && j != analysisops.size() - 1){
                sourceop = analysisops.getJSONObject(j).getString("sourceOp") + ",";
                targetop = analysisops.getJSONObject(j).getString("targetOp") + ",";
            }else if(j == analysisops.size() - 1){
                sourceop += analysisops.getJSONObject(j).getString("sourceOp");
                targetop += analysisops.getJSONObject(j).getString("targetOp");
            }else{
                sourceop += analysisops.getJSONObject(j).getString("sourceOp") + ",";
                targetop += analysisops.getJSONObject(j).getString("targetOp") + ",";
            }
            opid[j] = analysisops.getJSONObject(j).getString("id");
        }
        System.out.println(sourcedataObjname);
        System.out.println(targetdataObjname);
        System.out.println(sourceop);
        System.out.println(targetop);

        remsg = redismsg;
        sourcefilepath = jsonObject.getString("sourceFilePath");
        targetfilepath = jsonObject.getString("targetFilePath");
        System.out.println(sourcefilepath);
        System.out.println(targetfilepath);
        // taskid = jsonObject.getString("taskId");
        analysisid = jsonObject.getString("analysisId");
        process = jsonObject.getDouble("process");
        status = jsonObject.getInteger("status");
        sourcelogtype = jsonObject.getInteger("sourceLogType");
        targetlogtype = jsonObject.getInteger("targetLogType");
    }
    //更新json里的两个值,从日志匹配返回的process和status
    public static String update(String str1, double i){
        str1 = str1.substring(1);
        str1 = str1.substring(0,str1.length()-1);
        str1 = str1.replace("\\", "");
        JSONObject json = JSONObject.parseObject(str1);
        Set <String> keySet = json.keySet();
        Integer j = 0;
        if(i == 1){
            j = 4;
        }else{
            j = 2;
        }
        for(String key:keySet){
            if(key.equalsIgnoreCase("process")) {
				json.put(key, i);
			}
            if(key.equalsIgnoreCase("status")) {
                json.put(key, j);
            }
        }
        return json.toJSONString();
    }


    public static String getsourcedataObjname(){
        return sourcedataObjname;
    }

    public static String gettargetdataObjname(){
        return targetdataObjname;
    }

    public static String getsourceop(){
        return sourceop;
    }

    public static String gettargetop(){
        return targetop;
    }

    public static String getsourcefilepath(){
        return sourcefilepath;
    }

    public static String gettargetfilepath(){
        return targetfilepath;
    }

    // public static String gettaskid(){
    //     return taskid;
    // }

    public static String getanalysisid(){
        return analysisid;
    }


    public double getprocess(){
        return process;
    }

    public static String getopid(int i){
        return opid[i];
    }

    public static String getredismsg() {
        return remsg;
    }

    public int getstatus(){
        return status;
    }

    public static int getsourcelogtype(){
        return sourcelogtype;
    }

    public static int gettargetlogtype(){
        return targetlogtype;
    }

    // public String sendtomatch(){
    //     Integer ty = HandleJson.gettype();
    //     String transt = HandleJson.getfileid() + ";" + HandleJson.getdataObjoperation() + ";" + HandleJson.getfilepath() +";" + String.valueOf(ty);
    //     return transt;
    // }

    //还需要写一个函数获取匹配到的东西并且和之前的这些信息拼接起来，用\n作为分隔符，作为输入数据库的数据
    public void match() throws Exception {
        //String filepath = "E:\\NSTL实验室\\9院区块链项目\\dns.txt";
        // FileInputStream inputStream = new FileInputStream(HandleJson.getfilepath());
        // BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        // int sum_line = getLineNumber(HandleJson.getfilepath());
        String somepath = HandleJson.getsourcefilepath();
        String anotherpath = HandleJson.gettargetfilepath();
        Minio minio = new Minio();
        // FileInputStream inputStream = new FileInputStream(minio.readFile(somepath));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(minio.readFile(somepath)));//yuan  
        int sum_line = getLineNumber(minio.readFile(somepath)) + getLineNumber(minio.readFile(anotherpath));
        int count = 0;
        String log_context = "";
        while ((log_context = bufferedReader.readLine()) != null) {
            String LogType = "";
            switch (HandleJson.getsourcelogtype()) {
                case 1:
                    LogType = "Syslog_linux";
                    break;
                case 2:
                    LogType = "DNSlog_win";
                    break;
                case 3:
                    LogType = "Syslog_linux";
                    break;
                default:
                    ;
            }
            Filter filter = new Filter(LogType, log_context,HandleJson.getsourcedataObjname(),HandleJson.getsourceop());
            count += (log_context.getBytes().length);
            if (filter.filter_by_name()) {
                ArrayList<String> objset = filter.filter_by_dataobj_operates();
                String[] obj = HandleJson.getsourceop().trim().split(",");
                if(objset != null && objset.size() > 0){
                    for (int i = 0; i < objset.size(); i++){
                        for(int j = 0; j < obj.length; j++){
                            if (objset.get(i).equals(obj[j])){ //这里的obj是源文件中操作行为的集合，其中每个元素都对应opid中的相同下标元素。这里的数据对象名正确后，看该条日志中的操作行为是否在集合里
                                String newopid = HandleJson.getopid(j); //在这个集合中，因此直接把obj中j位置对应到opid中的j位置，从而取得对应的opid
                                process = (double) count/sum_line;
                                System.out.print(process);
                                Integer a = 1;
                                inserttosql = HandleJson.getanalysisid() + "\n" + newopid + "\n" + log_context + "\n" + a + "\n";
                                System.out.print(inserttosql);
                                JSONObject json1 = JSONObject.parseObject(update(redisjson, process)); //修改json的值，作为更新redis的值
                                String strjson1 =JSON.toJSONString(json1); //转成string类型才能更新redis
                                String strjson2 = "\"" + StringEscapeUtils.escapeJson(strjson1) + "\"";
                                // System.out.println(strjson2);
                                try {
                                    InsertInfo insertInfo = new InsertInfo(inserttosql, HandleJson.getredismsg());
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                String rediskey = "TASK:ANALYSIS:" + HandleJson.getredismsg();
                                RedisClient.renewredis(rediskey, strjson2);
                            }
                        }
                    }
                }
            }
        }
        bufferedReader.close();
        BufferedReader nbufferedReader = new BufferedReader(new InputStreamReader(minio.readFile(anotherpath)));//mubiao
        while ((log_context = nbufferedReader.readLine()) != null) {
            String LogType = "";
            switch (HandleJson.gettargetlogtype()) {
                case 1:
                    LogType = "Syslog_linux";
                    break;
                case 2:
                    LogType = "DNSlog_win";
                    break;
                case 3:
                    LogType = "Syslog_linux";
                    break;
                default:
                    ;
            }
            Filter filter = new Filter(LogType, log_context,HandleJson.gettargetdataObjname(),HandleJson.gettargetop());
            count += (log_context.getBytes().length);
            if (count == sum_line){
                process = (double) count/sum_line;
                JSONObject json1 = JSONObject.parseObject(update(redisjson, process)); //修改json的值，作为更新redis的值
                String strjson1 =JSON.toJSONString(json1); //转成string类型才能更新redis
                String strjson2 = "\"" + StringEscapeUtils.escapeJson(strjson1) + "\"";
                String rediskey = "TASK:ANALYSIS:" + HandleJson.getredismsg();
                RedisClient.renewredis(rediskey, strjson2);
            }
            if (filter.filter_by_name()) {
                ArrayList<String> objset = filter.filter_by_dataobj_operates();
                String[] obj = HandleJson.gettargetop().trim().split(",");
                if(objset != null && objset.size() > 0){
                    for (int i = 0; i < objset.size(); i++){
                        for(int j = 0; j < obj.length; j++){
                            if (objset.get(i).equals(obj[j])){ 
                                String newopid = HandleJson.getopid(j);
                                process = (double) count/sum_line;
                                Integer a = 2;
                                inserttosql = HandleJson.getanalysisid() + "\n" + newopid + "\n" + log_context + "\n" + a + "\n";
                                System.out.print(inserttosql);
                                JSONObject json1 = JSONObject.parseObject(update(redisjson, process)); //修改json的值，作为更新redis的值
                                String strjson1 =JSON.toJSONString(json1); //转成string类型才能更新redis
                                String strjson2 = "\"" + StringEscapeUtils.escapeJson(strjson1) + "\"";
                                // System.out.println(strjson2);
                                try {
                                    InsertInfo insertInfo = new InsertInfo(inserttosql, HandleJson.getredismsg());
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                String rediskey = "TASK:ANALYSIS:" + HandleJson.getredismsg();
                                RedisClient.renewredis(rediskey, strjson2);
                            }
                        }
                    }
                }
            }
        }
        nbufferedReader.close();
    }

    public static int getLineNumber(FilterInputStream filterInputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(filterInputStream));
        String readtext = "";
        int lines = 0;
        while ((readtext = bufferedReader.readLine()) != null)
        {
            lines  += (readtext.getBytes().length);
        }

        bufferedReader.close();
        return lines;
        // File file = new File(filepath);
        // if (file.exists()) {
        //     try {
        //         FileReader fileReader = new FileReader(file);
        //         LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
        //         lineNumberReader.skip(Long.MAX_VALUE);
        //         int lines = lineNumberReader.getLineNumber() + 1;
        //         fileReader.close();
        //         lineNumberReader.close();
        //         return lines;
        //     } catch (IOException e) {
        //         e.printStackTrace();
        //     }
        // }
        // return 0;
    }

    // public String getLogMsg(String str) {
    //     String a = str.trim();
    //     String[] b = a.split("\n");
    //     String[] c = b[1].split(":");
    //     String d = c[1] + ":" + c[2] + ":" + c[3];

    //     return d;
    // }

    // public static void main(String[] args) throws Exception{
    //     HandleJson jt = new HandleJson(data01, "msg");
    //     String text = jt.sendtomatch(); 
    //     System.out.println(text);
    //     // Double n = 0.88;
    //     // //修改json里的值
    //     // JSONObject json1 = JSONObject.parseObject(update(data01, n));
    //     // System.out.println(json1);
    //     // jt.match();
    //     // String  data = "{\"dataObj\":[{\"name\":\"file1.pdf\",\"operationList\":[\"create\",\"open\"]}],\"fileHash\":\"0ca9d9ccb75d25620a73376a197620946e9fb115\",\"fileId\":81588831464198144,\"filePath\":\"2022-09-29/8a5fec99-93ae-40e5-a5b0-3ef382dd1c26.txt\",\"process\":0.0,\"status\":1}";
    //     // JSONObject json =  JSONObject.parseObject(data);
    //     // System.out.println(json.toJSONString());
    // }


    public static void main(String[] args) {
        String data ="{1111}";
        data = data.substring(1);
        data = data.substring(0,data.length()-1);
        System.out.println(data);
    }
}
