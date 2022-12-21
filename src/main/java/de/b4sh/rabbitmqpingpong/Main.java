package de.b4sh.rabbitmqpingpong;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {
    public static void main(String[] args) {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(RuntimeConfiguration.USERNAME.getValue());
        factory.setPassword(RuntimeConfiguration.PASSWORD.getValue());
        factory.setVirtualHost(RuntimeConfiguration.VIRTUALHOST.getValue());
        factory.setHost(RuntimeConfiguration.HOST.getValue());
        factory.setPort(Integer.parseInt(RuntimeConfiguration.PORT.getValue()));

        factory.se

        try {
            Connection rabbitMqConnection = factory.newConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }


    }
}