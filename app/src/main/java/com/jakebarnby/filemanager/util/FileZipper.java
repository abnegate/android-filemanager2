package com.jakebarnby.filemanager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Jake on 10/14/2017.
 */

public class FileZipper {

    private List<String> mFilesToZipPaths;

    public FileZipper() {}

    public static class Builder {
        private FileZipper mZipper;

        public Builder() {
            mZipper = new FileZipper();
            mZipper.mFilesToZipPaths = new ArrayList<>();
        }

        public void addFilePath(String path) {
            mZipper.mFilesToZipPaths.add(path);
        }

        public FileZipper build() {
            return mZipper;
        }
    }

    public void zipFiles(String destZipFilePath, List<String> filesToZipPaths) throws IOException {
        if (filesToZipPaths != null) {
            mFilesToZipPaths = filesToZipPaths;
        }

        FileOutputStream fos = new FileOutputStream(destZipFilePath);
        ZipOutputStream zip = new ZipOutputStream(fos);

        for (String filePath : mFilesToZipPaths) {
            addFileToZip("", filePath, zip);
        }

        zip.close();
        fos.close();
    }

    private void addFileToZip(String folderPath, String filePath, ZipOutputStream zip) throws IOException {
        File file = new File(filePath);

        if (file.isDirectory()) {
            addFolderToZip(folderPath, filePath, zip);
        } else {
            byte[] buf = new byte[2048];
            int len;
            FileInputStream in = new FileInputStream(filePath);
            zip.putNextEntry(new ZipEntry(folderPath + "/" + file.getName()));
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
            in.close();
        }
    }

    private void addFolderToZip(String folderPath, String filePath, ZipOutputStream zip) throws IOException {
        File folder = new File(filePath);

        if (folder.list().length == 0) {
            zip.putNextEntry(new ZipEntry(folderPath + folder.getName() + "/"));
        } else {
            for (File file : folder.listFiles()) {
                addFileToZip(folderPath + folder.getName() + "/", file.getPath(), zip);
            }
        }
    }
}
