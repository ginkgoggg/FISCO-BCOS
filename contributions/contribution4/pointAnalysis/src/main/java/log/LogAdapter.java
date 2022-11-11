package log;

public class LogAdapter implements Abstract_LogIF {
    Specific_LogIF specific_Log;

    public LogAdapter(String LogType, String log_text) {
        if (LogType.equalsIgnoreCase("Syslog_linux")) {
            specific_Log = new Syslog_linux(log_text);
        } else if (LogType.equalsIgnoreCase("DNSlog_win")) {
            specific_Log = new DNSlog_win(log_text);
        }
    }

    public String get(String LogType, String LogProperty) {
        if (LogType.equalsIgnoreCase("Syslog_linux")) {
            return specific_Log.get_Syslog_linux(LogProperty);
        } else if (LogType.equalsIgnoreCase("DNSlog_win")) {
            return specific_Log.get_DNSlog_win(LogProperty);
        } else {
            return null;
        }
    }


    public void set(String LogType, String LogProperty, String newvalue) {
        if (LogType.equalsIgnoreCase("Syslog_linux")) {
            specific_Log.set_Syslog_linux(LogProperty, newvalue);
        } else if (LogType.equalsIgnoreCase("DNSlog_win")) {
            specific_Log.set_DNSlog_win(LogProperty, newvalue);
        }
    }

    public String print(String LogType) {
        if (LogType.equalsIgnoreCase("Syslog_linux")) {
            return specific_Log.print_Syslog_linux();
        } else if (LogType.equalsIgnoreCase("DNSlog_win")) {
            return specific_Log.print_DNSlog_win();
        } else {
            return null;
        }
    }
}
