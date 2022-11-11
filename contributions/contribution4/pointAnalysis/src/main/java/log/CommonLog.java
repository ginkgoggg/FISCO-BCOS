package log;

public class CommonLog implements Abstract_LogIF {
    private String msg;
    LogAdapter logAdapter;

    public CommonLog(String LogType, String log_text) {
        switch (LogType) {
            case "commonlog":
                this.msg = log_text;
                break;
            case "Syslog_linux" :
                this.logAdapter = new LogAdapter(LogType, log_text);
                break;
            case "DNSlog_win" :
                this.logAdapter = new LogAdapter(LogType, log_text);
                break;
            default:
                break;
        }
    }

    public String get(String LogType, String LogProperty) {
        switch (LogType) {
            case "commonlog":
                if (LogProperty.equalsIgnoreCase("msg")) {
                    return this.msg;
                }
            case "Syslog_linux":
                return this.logAdapter.get(LogType, LogProperty);
            case "DNSlog_win":
                return this.logAdapter.get(LogType, LogProperty);
            default:
                return String.format("Invalid media. " + LogType + " format not supported");
        }
    }

    public void set(String LogType, String LogProperty, String newvalue) {
        switch (LogType) {
            case "commonlog":
                if (LogProperty.equalsIgnoreCase("msg")) {
                    this.msg = newvalue;
                }
            case "Syslog_linux":
                this.logAdapter.set(LogType, LogProperty, newvalue);
            case "DNSlog_win":
                this.logAdapter.set(LogType, LogProperty, newvalue);
            default:
                ;
        }
    }

    public String print(String LogType) {
        switch (LogType) {
            case "commonlog":
                return String.format("msg: %s\n", this.msg);
            case "Syslog_linux":
                return this.logAdapter.print(LogType);
            case "DNSlog_win":
                return this.logAdapter.print(LogType);
            default:
                return null;
        }
    }
}
