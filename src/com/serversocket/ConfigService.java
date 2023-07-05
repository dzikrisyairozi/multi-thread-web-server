package com.serversocket;

import java.io.*;
import java.util.HashMap;

public class ConfigService {
    private String IP;
    private int port;

    private static final String CONFIG_FILE = "config.txt";
    private final String configPath;

    public static String IP_KEY = "IP";
    public static String PORT_KEY = "PORT";

    private final HashMap<String, String> configSettings;

    /**
     * Constructs a ConfigService instance.
     *
     * @throws Exception if there is an error in loading configurations.
     */
    public ConfigService() throws Exception {
        this.configPath = "./src/com/serversocket/" + CONFIG_FILE; // Update the directory path accordingly
        this.configSettings = new HashMap<>();
        loadConfigurations();
    }

    /**
     * Loads the configurations from the config file.
     *
     * @throws Exception if there is an error in loading configurations.
     */
    private void loadConfigurations() throws Exception {
        File file = new File(configPath);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            do {
                line = bufferedReader.readLine();
            } while (appendLineToSettings(line));
        }

        validateKey(IP_KEY);
        validateKey(PORT_KEY);

        IP = getSettingsWithKey(IP_KEY);
        port = Integer.parseInt(getSettingsWithKey(PORT_KEY));
    }

    /**
     * Appends a line from the config file to the settings map.
     *
     * @param line the line to be appended.
     * @return true if the line was successfully appended, false otherwise.
     */
    private boolean appendLineToSettings(String line) {
        if (line == null) {
            return false;
        }

        int colonIndex = line.indexOf(":");
        if (colonIndex != -1) {
            String key = line.substring(0, colonIndex);
            String value = line.substring(colonIndex + 2);
            configSettings.put(key, value);
            return true;
        }

        return false;
    }

    /**
     * Retrieves the IP address from the loaded configurations.
     *
     * @return the IP address.
     */
    public String getIP() {
        return IP;
    }

    /**
     * Retrieves the port number from the loaded configurations.
     *
     * @return the port number.
     */
    public int getPort() {
        return port;
    }

    /**
     * Retrieves the configuration value associated with the given key.
     *
     * @param key the configuration key.
     * @return the configuration value.
     */
    public String getSettingsWithKey(String key) {
        return configSettings.get(key);
    }

    /**
     * Checks if the loaded configurations contain the specified key.
     *
     * @param key the configuration key to check.
     * @return true if the key exists, false otherwise.
     */
    public boolean doesSettingsHaveKey(String key) {
        return configSettings.containsKey(key);
    }

    /**
     * Validates if the specified key exists in the loaded configurations.
     * Throws an exception if the key is missing.
     *
     * @param key the configuration key to validate.
     * @throws Exception if the key is missing in the configurations.
     */
    private void validateKey(String key) throws Exception {
        if (!doesSettingsHaveKey(key)) {
            throw new Exception("Config at " + configPath + " doesn't have " + key + " key");
        }
    }
}
