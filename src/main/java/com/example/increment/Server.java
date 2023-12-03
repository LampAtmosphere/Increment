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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Application {
    private int counter = 0;
    @FXML
    private Button btn;
    @FXML
    private Label lableid;
    private List<OutputStream> clientOutputStreams = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        VBox root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Server");
        primaryStage.show();

        Button clickButton = (Button) loader.getNamespace().get("btn");
        clickButton.setOnAction(e -> {
            incrementCounter();
            updateCounterLabel();
            sendCounterValueToClients();
        });

        // Привязка lableid из FXML к полю lableid в контроллере
        lableid = (Label) loader.getNamespace().get("lableid");

        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(41242);
                System.out.println("Сервер ждет клиента...");

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Новое соединение: " + clientSocket.getInetAddress().toString());
                    handleClientConnection(clientSocket);
                    sendCounterValueToClients();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private synchronized void updateCounterLabel() {
        Platform.runLater(() -> lableid.setText(String.valueOf(counter)));
    }

    private synchronized void sendCounterValueToClients() {
        String message = "Counter: " + counter;
        for (OutputStream clientOutputStream : clientOutputStreams) {
            try {
                clientOutputStream.write(message.getBytes());
                clientOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void incrementCounter() {
        counter++;
    }
}