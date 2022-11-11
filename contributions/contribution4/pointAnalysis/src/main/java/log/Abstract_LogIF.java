package log;

public interface Abstract_LogIF {
    public String get(String LogType, String LogProperty);

    public void set(String LogType, String LogProperty, String newvalue);

    public String print(String LogType);
}
