package log;

public interface Specific_LogIF {
    public String get_Syslog_linux(String LogProperty);

    public String get_DNSlog_win(String LogProperty);

    public void set_Syslog_linux(String LogProperty, String newvalue);

    public void set_DNSlog_win(String LogProperty, String newvalue);

    public String print_Syslog_linux();

    public String print_DNSlog_win();
}
