package qdu.edu.cn.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Write {
    public static void writeLine(String path, String data) {
        try {
            Path filePath = Paths.get(path);
            // 确保目标目录存在
            if(Files.notExists(filePath.getParent())){
                Files.createDirectories(filePath.getParent());
            }
        
            // 追加写入文件，如果文件不存在则创建
            Files.writeString(filePath, data + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("写入文件失败: " + e.getMessage());
        }
    }
}
