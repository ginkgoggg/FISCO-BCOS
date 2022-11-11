package file_object;

import java.lang.reflect.Array;
import java.util.Arrays;

public class File {
    private String file_name;
    private String[] operates;

    public File(String _file_object_name,String _file_object_operations) {
        this.file_name = _file_object_name;
        this.operates = _file_object_operations.split(",");
    }

    public String getFile_name() {
        return this.file_name;
    }

    public String[] getOperates() {
        return this.operates;
    }

    public void show_file_info() {
        System.out.printf("file info: \n file_name: %s \n operates: %s\n",this.getFile_name(), Arrays.toString(this.getOperates()));
    }
}
