package Filter;

import file_object.File;
import log.CommonLog;
import log.LogAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Filter {
    public CommonLog commonLog;
    File file;

    public Filter(String LogType, String log_text,String _file_object_name,String _file_object_operations) {
        this.commonLog = new CommonLog(LogType,log_text);
        this.file = new File(_file_object_name,_file_object_operations);
    }
    public boolean filter_by_name() {
        if (this.commonLog.get("Syslog_linux","msg").contains(file.getFile_name())) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<String> filter_by_dataobj_operates() {
        if (!this.filter_by_name()) {
            return null;
        } else {
            ArrayList<String> ret = new ArrayList<String>();
            for (int i = 0;i<this.file.getOperates().length;i++) {
                if (this.commonLog.get("Syslog_linux","msg").contains(file.getOperates()[i])) {
                    ret.add(this.file.getOperates()[i]);
                }
            }
            return ret;
        }
    }
}
