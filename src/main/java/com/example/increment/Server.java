package com.example.increment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Server extends Application {
    private int counter = 0;
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
    scene.getStylesheets().add(getClass().getResource("dark_theme.css").toExternalForm());
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
        List<OutputStream> closedStreams = new ArrayList<>();
    
        for (OutputStream clientOutputStream : clientOutputStreams) {
            try {
                clientOutputStream.write(message.getBytes());
                clientOutputStream.flush();
            } catch (IOException e) {
                // Ошибка записи, возможно, сокет был закрыт клиентом
                closedStreams.add(clientOutputStream);
            }
        }
    
        // Удалить закрытые потоки из списка
        clientOutputStreams.removeAll(closedStreams);
    
        // Обновить собственный счетчик и метку на сервере
        updateCounterLabel();
    
        // Отправить обновленное значение счетчика клиентам
        String updatedCounterMessage = "UpdatedCounter: " + counter;
        for (OutputStream clientOutputStream : clientOutputStreams) {
            try {
                clientOutputStream.write(updatedCounterMessage.getBytes());
                clientOutputStream.flush();
            } catch (IOException e) {
                // Обработка ошибок записи, если необходимо
            }
        }
    }
    
    

    private synchronized void handleClientConnection(Socket clientSocket) {
    try {
        OutputStream outputStream = clientSocket.getOutputStream();

        // Добавить outputStream только один раз при создании сокета для клиента
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
                // Ошибка чтения, возможно, сокет был закрыт клиентом
                clientOutputStreams.remove(outputStream);
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