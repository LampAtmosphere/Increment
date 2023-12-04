package com.example.increment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Client extends Application {
    private int counter = 0;
    private Socket socket;
    private Label counterLabel;
    private String serverIP = "192.168.1.119"; // Replace with the IP address of your server

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {  
    FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view2.fxml"));
    VBox root = loader.load();
    Scene scene = new Scene(root);
    scene.getStylesheets().add(getClass().getResource("dark_theme.css").toExternalForm());
    primaryStage.setScene(scene);
    primaryStage.setTitle("Client");
    primaryStage.show();

        // Bind counterLabel from FXML to the counterLabel field in the controller
        counterLabel = (Label) loader.getNamespace().get("lableid");

        Button clickButton = (Button) loader.getNamespace().get("btn");
        clickButton.setOnAction(e -> {
            incrementCounter();
            updateCounterLabel();
            sendCounterValueToServer();
        });

        try {
            socket = new Socket(serverIP, 41242);
    
            // Создать поток для чтения данных от сервера
            new Thread(() -> {
                try {
                    InputStream inputStream = socket.getInputStream();
                    int response;
                    byte[] buffer = new byte[1024];
                    while ((response = inputStream.read(buffer)) != -1) {
                        handleServerMessage(new String(buffer, 0, response));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void incrementCounter() {
        counter++;
    }

    private void updateCounterLabel() {
        Platform.runLater(() -> counterLabel.setText(String.valueOf(counter)));
    }

    private void sendCounterValueToServer() {
        try {
            // Отправляем значение счетчика на сервер
            OutputStream outputStream = socket.getOutputStream();
            String counterValue = Integer.toString(counter);
            outputStream.write(counterValue.getBytes());
            outputStream.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    

    // You might need to implement this method based on your requirements
    private void handleServerMessage(String message) {
        if (message.startsWith("Counter: ")) {
            // Обработка сообщения с текущим значением счетчика (как ранее)
            // ...
        } else if (message.startsWith("UpdatedCounter: ")) {
            // Обработка сообщения с обновленным значением счетчика
            String[] parts = message.split(": ");
            if (parts.length == 2) {
                int updatedCounterValue = Integer.parseInt(parts[1]);
                counter = updatedCounterValue;
                updateCounterLabel();
            }
        }
    }
    
}