package log;

public class DNSlog_win implements Specific_LogIF {
//    private String date;
    private String time;
    private String sourceip;
    private String destinationip;
    private String type;
    private String msg;

    public DNSlog_win(String _log_text) {
        String log_text = _log_text.trim();
        //跳过
        int tmp_index = log_text.indexOf(' ');
        log_text = log_text.substring(tmp_index + 1).trim();
        //time_date
        tmp_index = log_text.indexOf(' ');
        this.time = log_text.substring(0, tmp_index);
        log_text = log_text.substring(tmp_index + 1).trim();
        //time_time
        tmp_index = log_text.indexOf(' ');
        this.time = this.time+" "+log_text.substring(0, tmp_index);
        log_text = log_text.substring(tmp_index + 1).trim();
        //sourceip
        tmp_index = log_text.indexOf(' ');
        this.sourceip = log_text.substring(0, tmp_index);
        log_text = log_text.substring(tmp_index + 1).trim();
        //跳过-》
        tmp_index = log_text.indexOf(' ');
        log_text = log_text.substring(tmp_index + 1).trim();
        //destinationip
        tmp_index = log_text.indexOf(' ');
        this.destinationip = log_text.substring(0, tmp_index);
        log_text = log_text.substring(tmp_index + 1).trim();
        //type
        tmp_index = log_text.indexOf(' ');
        this.type = log_text.substring(0, tmp_index);
        log_text = log_text.substring(tmp_index + 1).trim();
        //跳过log长度
        tmp_index = log_text.indexOf(' ');
        log_text = log_text.substring(tmp_index + 1).trim();
        //msg
        this.msg = log_text;
    }


    public String get_Syslog_linux(String LogProperty) {
        return null;
    }


    public String get_DNSlog_win(String LogProperty) {
        switch (LogProperty) {
            case "time":
                return this.time;
            case "sourceip":
                return this.sourceip;
            case "destinationip":
                return this.destinationip;
            case "type":
                return this.type;
            case "msg":
                return this.msg;
            default:
                return null;
        }
    }


    public void set_Syslog_linux(String LogProperty, String newvalue) {
    }


    public void set_DNSlog_win(String LogProperty, String newvalue) {
        switch (LogProperty) {
            case "time":
                this.time = newvalue;
                break;
            case "sourceip":
                this.sourceip = newvalue;
                break;
            case "destinationip":
                this.destinationip = newvalue;
                break;
            case "type":
                this.type = newvalue;
            case "msg":
                this.msg = newvalue;
                break;
            default:
                ;
        }
    }


    public String print_Syslog_linux() {
        return null;
    }


    public String print_DNSlog_win() {
        return String.format("log info : \n time: %s\n sourceip: %s\n destinationip: %s\n type: %s\n msg: %s\n",
                this.get_DNSlog_win("time"), this.get_DNSlog_win("sourceip"),
                this.get_DNSlog_win("destinationip"), this.get_DNSlog_win("type"),
                this.get_DNSlog_win("msg"));
    }


}
