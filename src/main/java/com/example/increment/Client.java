package com.example.increment;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client extends Application {
    private int counter = 0;
    private Label counterLabel;
    String serverIP = "10.0.0.178"; // Replace with the IP address of your server

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view2.fxml"));
        VBox root = loader.load();
        Scene scene = new Scene(root);
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

        new Thread(() -> {
            try {
                Socket socket = new Socket(serverIP, 41242);
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                int response;
                byte[] buffer = new byte[1024];
                while ((response = inputStream.read(buffer)) != -1) {
                    handleServerMessage(new String(buffer, 0, response));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void incrementCounter() {
        counter++;
    }

    private void updateCounterLabel() {
        Platform.runLater(() -> counterLabel.setText(String.valueOf(counter)));
    }

    private void sendCounterValueToServer() {
        new Thread(() -> {
            try {
                // Устанавливаем соединение с сервером
                Socket socket = new Socket(serverIP, 41242);

                // Отправляем значение счетчика на сервер
                OutputStream outputStream = socket.getOutputStream();
                String counterValue = Integer.toString(counter);
                outputStream.write(counterValue.getBytes());
                outputStream.flush();

                // Закрываем соединение
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private synchronized void handleClientConnection(Socket clientSocket) {
        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            clientOutputStreams.add(outputStream);

            new Thread(() -> {
                try {
                    InputStream inputStream = clientSocket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        // Обработка входящих данных, если необходимо
                        incrementCounter();
                        updateCounterLabel();
                        sendCounterValueToClients();
                    }
                    // Закрываем соединение после завершения чтения
                    clientSocket.close();
                    clientOutputStreams.remove(outputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}