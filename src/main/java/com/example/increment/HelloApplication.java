package com.example.increment;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        // Создайте экземпляр сервера и запустите его в новом потоке
        new Thread(() -> {
            Server server = new Server();
            Platform.runLater(() -> {
                try {
                    server.start(new Stage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).start();

        // Задержка для убедительности в том, что сервер запущен перед клиентом
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Создайте экземпляр клиента и запустите его в новом потоке
        new Thread(() -> {
            Client client = new Client();
            Platform.runLater(() -> {
                try {
                    client.start(new Stage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }
}