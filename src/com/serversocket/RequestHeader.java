package com.serversocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class RequestHeader {
    private String requestStatus;
    private final HashMap<String, String> requestHeader;
    private final BufferedReader bufferedReader;

    public RequestHeader(BufferedReader bufferedReader) throws IOException {
        this.bufferedReader = bufferedReader;
        this.requestHeader = new HashMap<>();
        setRequestStatus();
        setAllRequestHeaders();
    }

    /**
     * Retrieves the request status line.
     *
     * @return The request status line.
     */
    public String getRequestStatus() {
        return requestStatus;
    }

    /**
     * Sets the request status by reading the status line from the input buffer.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void setRequestStatus() throws IOException {
        do {
            requestStatus = bufferedReader.readLine();
        } while (requestStatus == null || Objects.equals(requestStatus, ""));
    }

    /**
     * Retrieves the requested file from the request status line.
     *
     * @return The requested file path.
     */
    public String getRequestedFile() {
        String[] parsedRequestStatus = requestStatus.split(" ");
        return (parsedRequestStatus[1].equals("/")) ? "" : parsedRequestStatus[1].substring(1).replaceAll("%20", " ");
    }

    /**
     * Sets all the request headers by reading lines from the input buffer.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void setAllRequestHeaders() throws IOException {
        String request;
        do {
            request = bufferedReader.readLine();
        } while (appendToHash(request));
    }

    /**
     * Appends a request header line to the requestHeader HashMap.
     *
     * @param line The request header line to be appended.
     * @return true if the line was successfully appended, false otherwise.
     */
    private boolean appendToHash(String line) {
        int colonIndex = line.indexOf(":");
        if (colonIndex != -1) {
            requestHeader.put(line.substring(0, colonIndex), line.substring(colonIndex + 2));
            return true;
        }
        return false;
    }

    /**
     * Retrieves the value of a specific header key.
     *
     * @param key The header key.
     * @return The value associated with the key, or null if the key does not exist.
     */
    public String getHeaderWithKey(String key) {
        return requestHeader.get(key);
    }

    /**
     * Checks if a specific header key exists in the request headers.
     *
     * @param key The header key to check.
     * @return true if the key exists, false otherwise.
     */
    public boolean doesHeaderHaveKey(String key) {
        return requestHeader.containsKey(key);
    }

    /**
     * Retrieves the range values specified in the "Range" header.
     *
     * @return A HashMap containing the unit, startIndex, and endIndex values, or null if the "Range" header is not present.
     */
    public HashMap<String, String> getRangeValues() {
        if (!doesHeaderHaveKey("Range")) {
            return null;
        }
        HashMap<String, String> rangeValues = new HashMap<>();

        // Get unit
        String rangeHeader = getHeaderWithKey("Range");
        String[] parsedRange = rangeHeader.split("=");
        rangeValues.put("unit", parsedRange[0]);

        // Get start range and end range
        String range = parsedRange[1].split(",")[0];
        int separatorIdx = range.indexOf("-");
        rangeValues.put("startIndex", range.substring(0, separatorIdx));
        rangeValues.put("endIndex", range.substring(separatorIdx + 1));
        return rangeValues;
    }

    /**
     * Checks if the range values specified in the "Range" header are valid for a given file length.
     *
     * @param fileLength The length of the file.
     * @return true if the range values are valid, false otherwise.
     */
    public boolean validRangeValues(long fileLength) {
        if (!doesHeaderHaveKey("Range")) {
            return true;
        }
        return validRangeValues(getRangeValues(), fileLength);
    }

    /**
     * Checks if the range values specified in the given rangeValues HashMap are valid for a given file length.
     *
     * @param rangeValues The HashMap containing the range values.
     * @param fileLength  The length of the file.
     * @return true if the range values are valid, false otherwise.
     */
    public boolean validRangeValues(HashMap<String, String> rangeValues, long fileLength) {
        if (!Objects.equals(rangeValues.get("unit"), "bytes")
                || !rangeValues.containsKey("startIndex")
                || !rangeValues.containsKey("endIndex")) {
            return false;
        }
        long startIndex = 0;
        if (!rangeValues.get("startIndex").equals("")) {
            startIndex = Long.parseLong(rangeValues.get("startIndex"));
        }
        long endIndex = fileLength - 1;
        if (!rangeValues.get("endIndex").equals("")) {
            endIndex = Long.parseLong(rangeValues.get("endIndex"));
        }
        if (startIndex < 0 || startIndex > endIndex || endIndex >= fileLength) {
            return false;
        }
        return true;
    }
}
