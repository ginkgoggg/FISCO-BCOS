package log;

public class Syslog_linux implements Specific_LogIF {
//    private String date;
    private String time;
    private String PCid;
    private String process;
    private String msg;

    public Syslog_linux(String _log_text) {
        String log_text = _log_text.trim();
        //time_year
        this.time = "2022-";
        //time_month
        int tmp_index = log_text.indexOf(' ');
        String month = log_text.substring(0, tmp_index);
        switch (month) {
            case "Jan" :
                month = "01";
                break;
            case "Feb" :
                month = "02";
                break;
            case "Mar" :
                month = "03";
                break;
            case "Apr" :
                month = "04";
                break;
            case "May" :
                month = "05";
                break;
            case "Jun" :
                month = "06";
                break;
            case "Jul" :
                month = "07";
                break;
            case "Aug" :
                month = "08";
                break;
            case "Sept" :
                month = "09";
                break;
            case "Oct" :
                month = "10";
                break;
            case "Nov" :
                month = "11";
                break;
            case "Dec" :
                month = "12";
                break;
            default:
                ;
        }
        this.time += month;
        log_text = log_text.substring(tmp_index + 1).trim();
        //time_date
        tmp_index = log_text.indexOf(' ');
        this.time = this.time + "-" + log_text.substring(0, tmp_index);
        log_text = log_text.substring(tmp_index + 1).trim();
        //time_time
        tmp_index = log_text.indexOf(' ');
        this.time = this.time+" "+log_text.substring(0, tmp_index);
        log_text = log_text.substring(tmp_index + 1).trim();
        //PCid
        tmp_index = log_text.indexOf(' ');
        this.PCid = log_text.substring(0, tmp_index);
        log_text = log_text.substring(tmp_index + 1).trim();
        //process[ID]
        tmp_index = log_text.indexOf(' ');
        this.process = log_text.substring(0, tmp_index - 1);
        log_text = log_text.substring(tmp_index + 1).trim();
        //msg
        this.msg = log_text;
    }


    public String get_Syslog_linux(String LogProperty) {
        switch (LogProperty) {
            case "time":
                return this.time;
            case "PCid":
                return this.PCid;
            case "process":
                return this.process;
            case "msg":
                return this.msg;
            default:
                return null;
        }
    }


    public String get_DNSlog_win(String LogProperty) {
        return null;
    }


    public void set_Syslog_linux(String LogProperty, String newvalue) {
        switch (LogProperty) {
            case "time":
                this.time = newvalue;
                break;
            case "PCid":
                this.PCid = newvalue;
                break;
            case "process":
                this.process = newvalue;
                break;
            case "msg":
                this.msg = newvalue;
                break;
            default:
                ;
        }
    }


    public void set_DNSlog_win(String LogProperty, String newvalue) {
    }


    public String print_Syslog_linux() {
        return String.format("log info : \n time: %s\n PCid: %s\n process: %s\n msg: %s\n",
                this.get_Syslog_linux("time"), this.get_Syslog_linux("PCid"),
                this.get_Syslog_linux("process"), this.get_Syslog_linux("msg"));
    }


    public String print_DNSlog_win() {
        return null;
    }


}
