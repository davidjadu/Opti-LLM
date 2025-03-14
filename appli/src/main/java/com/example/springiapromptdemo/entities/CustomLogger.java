package com.example.springiapromptdemo.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomLogger {
    private String logFilePath;
    private boolean writeToFile;
    Logger logger = LoggerFactory.getLogger(CustomLogger.class);

    public CustomLogger() {
        this.writeToFile = false; // Par défaut, on log dans la console
    }

    public CustomLogger(String logFilePath) {
        this.logFilePath = logFilePath;
        this.writeToFile = true;
    }

    public String log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] : %s", timestamp, message);

        if (writeToFile) {
            writeLogToFile(logMessage);
        }
        return logMessage;
    }

    private void writeLogToFile(String message) {
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(message + "\n\n");
        } catch (IOException e) {
            logger.error("[ERROR] Impossible d'écrire dans le fichier log: " + e.getMessage());
        }
    }
}
