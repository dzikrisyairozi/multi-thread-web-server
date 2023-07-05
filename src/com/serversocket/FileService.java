package com.serversocket;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class FileService {
    private final int BUFFER_SIZE = 1024;

    private String fetchedFilePath;
    private String contentType;
    private String contentDisposition;

    private long fileLength;
    private byte[] fileData;

    public boolean fileExists;

    public FileService(String domain, int port, String root, String path, String defaultPath, boolean fileExists) throws IOException {
        this.fileExists = fileExists;
        this.fileData = null;
        String fullPath = root + path;

        if (!isDirectory(fullPath)) {
            // If the given path is a file, then initialize using the fetched file.
            this.initializeByFetchedFilePath(fullPath);
            return;
        }

        String defaultFilePath = fullPath + "/" + defaultPath;
        if (fileExists(defaultFilePath)) {
            // If the given path is a directory and has the default file inside it, then initialize using the default file.
            this.initializeByFetchedFilePath(defaultFilePath);
            return;
        }

        // List all contents in the given directory and generate the list in HTML.
        ArrayList<HashMap<String, String>> files = getAllDirectoryContents(root, path);
        ListBuilder listBuilder = new ListBuilder(domain, port, root, files, (path.equals(defaultPath)) ? "" : path);

        this.contentType = "text/html";
        this.contentDisposition = "inline";
        this.fileData = listBuilder.getHtml().getBytes("UTF-8");
        this.fileLength = this.fileData.length;
    }

    /**
     * Retrieves all the contents (files and folders) within a given directory.
     *
     * @param root The root directory.
     * @param path The path to the directory.
     * @return An ArrayList of HashMaps containing the metadata of each file/folder.
     */
    private ArrayList<HashMap<String, String>> getAllDirectoryContents(String root, String path) {
        ArrayList<HashMap<String, String>> files = new ArrayList<>();

        File folder = new File(root + path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            return files;
        }

        String rootPath = "/" + path + (path.equals("") ? "" : "/");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        // Get each file/folder metadata
        for (File file : listOfFiles) {
            HashMap<String, String> data = new HashMap<>();
            long sizeInByte = (file.isFile()) ? file.length() : 0;

            data.put("name", file.getName());
            data.put("path", rootPath + file.getName());
            data.put("lastModified", sdf.format(file.lastModified()));
            data.put("type", (file.isFile()) ? "file" : "folder");
            data.put("size", Long.toString(sizeInByte)); // in bytes

            files.add(data);
        }
        return files;
    }

    /**
     * Checks if a file exists at the given path.
     *
     * @param path The path to the file.
     * @return true if the file exists, false otherwise.
     */
    public static boolean fileExists(String path) {
        return (new File(path)).exists();
    }

    /**
     * Checks if the given path corresponds to a directory.
     *
     * @param path The path to check.
     * @return true if the path is a directory, false otherwise.
     */
    public static boolean isDirectory(String path) {
        File file = new File(path);
        return file.isDirectory();
    }

    /**
     * Calculates the size of a directory.
     *
     * @param dir The directory.
     * @return The size of the directory in bytes.
     */
    public static long getDirectorySize(File dir) {
        long length = 0;
        File[] files = dir.listFiles();
        if (files == null) {
            return length;
        }

        for (File file : files) {
            long adder = (file.isFile()) ? file.length() : getDirectorySize(file);
            length += adder;
        }
        return length;
    }

    /**
     * Initializes the FileService by setting the fetched file path and its related properties.
     *
     * @param path The path to the fetched file.
     * @throws IOException If an I/O error occurs.
     */
    private void initializeByFetchedFilePath(String path) throws IOException {
        this.fetchedFilePath = path;

        this.setFileLength();
        this.setContentType();
        this.setContentDisposition();
    }

    /**
     * Sets the length of the fetched file.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void setFileLength() throws IOException {
        this.fileLength = Files.size(Path.of(this.fetchedFilePath));
    }

    /**
     * Sets the content type of the fetched file.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void setContentType() throws IOException {
        String type = Files.probeContentType(Path.of(this.fetchedFilePath));

        // Handle Javascript type and set default type to text/plain if mime type isn't found.
        if (type == null || type.equals("")) {
            File file = new File(this.fetchedFilePath);
            String filename = file.getName();

            int idx = filename.lastIndexOf(".");
            type = (filename.substring(idx + 1).equals("js")) ? "application/javascript" : "text/plain";
        }
        this.contentType = type;
    }

    /**
     * Writes the file data to the output stream.
     *
     * @param bos The BufferedOutputStream to write the data to.
     * @throws IOException If an I/O error occurs.
     */
    public void writeFileData(BufferedOutputStream bos) throws IOException {
        // Write directory list
        if (this.fileData != null) {
            bos.write(this.fileData, 0, (int) this.fileLength);
            bos.flush();
            return;
        }
        writeFileData(bos, 0, this.fileLength - 1);
    }

    /**
     * Writes a range of file data to the output stream.
     *
     * @param bos        The BufferedOutputStream to write the data to.
     * @param startIndex The starting index of the data range.
     * @param endIndex   The ending index of the data range.
     * @throws IOException If an I/O error occurs.
     */
    public void writeFileData(BufferedOutputStream bos, long startIndex, long endIndex) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(this.fetchedFilePath))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            long contentLength = endIndex - startIndex + 1;
            int bytesRead;

            bis.skip(startIndex);

            while (contentLength > 0) {
                bytesRead = bis.read(buffer);
                contentLength -= bytesRead;
                bos.write(buffer, 0, bytesRead);
            }
        }
        bos.flush();
    }

    /**
     * Sets the content disposition based on the content type.
     */
    public void setContentDisposition() {
        this.contentDisposition = (this.contentType.split("/")[0].equals("text")) ? "inline" : "attachment";
    }

    /**
     * Retrieves the content disposition.
     *
     * @return The content disposition.
     */
    public String getContentDisposition() {
        return this.contentDisposition;
    }

    /**
     * Retrieves the content type.
     *
     * @return The content type.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Retrieves the length of the file.
     *
     * @return The length of the file in bytes.
     */
    public long getFileLength() {
        return this.fileLength;
    }
}
