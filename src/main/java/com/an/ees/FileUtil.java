package com.an.ees;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileUtil {
    /**
     * @param filePath
     * @param content
     * @param writeType
     *            0-ignore exists<br>
     *            1-create new<br>
     *            2-append
     * @param createEmptyFile
     *            true-create file while empty content
     * @throws IOException
     */
    static void writeFile(String filePath, String content, int writeType, boolean createEmptyFile) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("Invalid filePath " + filePath);
        }
        if (content == null || content.isEmpty()) {
            if (!createEmptyFile) {
                return;
            }
        }
        String dirName = filePath.substring(0, filePath.lastIndexOf(File.separator));
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(filePath);
        if (file.exists()) {
            if (writeType == 0) {
                return;
            } else if (writeType == 1) {
                file.delete();
                file.createNewFile();
            }
        } else {
            file.createNewFile();
        }

        if (!content.isEmpty()) {
            System.out.println("Write file: " + filePath);
            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(filePath)), "GBK");) {
                osw.write(content);
                osw.flush();
            }
        }
    }
}
