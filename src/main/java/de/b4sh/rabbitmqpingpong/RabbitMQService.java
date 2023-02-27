package de.b4sh.rabbitmqpingpong;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RabbitMQService {
    private static RabbitMQService instance;
    private static final Logger log = Logger.getLogger(RabbitMQService.class.getName());

    private final Connection connection;
    private final Channel channel;

    public static RabbitMQService getInstance() throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        if (instance == null) {
            instance = new RabbitMQService();
        }
        return instance;
    }

    private RabbitMQService() throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        this.connection = this.connectToRabbitMQ();
        this.channel = this.connectToChannel(this.connection);
        //declare queues if not already existing
        this.declareQueue(RuntimeConfiguration.QUEUE_SENDER.getValue());
        this.declareQueue(RuntimeConfiguration.QUEUE_RECEIVER.getValue());
    }

    public Connection connectToRabbitMQ() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException, TimeoutException {
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(RuntimeConfiguration.USERNAME.getValue());
        factory.setPassword(RuntimeConfiguration.PASSWORD.getValue());
        factory.setVirtualHost(RuntimeConfiguration.VIRTUALHOST.getValue());
        factory.setHost(RuntimeConfiguration.HOST.getValue());
        factory.setPort(Integer.parseInt(RuntimeConfiguration.PORT.getValue()));

        log.log(Level.INFO, String.format("Trying to connect to %s on port %s", RuntimeConfiguration.HOST.getValue(), RuntimeConfiguration.PORT.getValue()));

        if (RuntimeConfiguration.USE_TLS_CLIENTCERT.getValue().equals("true")) {
            log.log(Level.INFO, "Instance is configured to use client cert and server jks.");
            log.log(Level.INFO, String.format("Starting read pkcs12 client certificate. Path: %s", RuntimeConfiguration.KEYSTORE_CLIENTKEYSTORE_PATH.getValue()));
            final char[] clientStorePassphrase = RuntimeConfiguration.KEYSTORE_CLIENTKEYSTORE_PASSPHRASE.getValue().toCharArray();
            final KeyStore clientKs = KeyStore.getInstance("PKCS12");
            clientKs.load(new FileInputStream(RuntimeConfiguration.KEYSTORE_CLIENTKEYSTORE_PATH.getValue()), clientStorePassphrase);
            final KeyManagerFactory clientKmf = KeyManagerFactory.getInstance("SunX509");
            clientKmf.init(clientKs, clientStorePassphrase);
            log.log(Level.INFO, "Done reading pkcs12 client certificate.");
            log.log(Level.INFO, String.format("Starting reading server jks. Path: %s",RuntimeConfiguration.KEYSTORE_SERVERKEYSTORE_PATH.getValue()));
            final char[] serverStorePassphrase = RuntimeConfiguration.KEYSTORE_SERVERKEYSTORE_PASSPHRASE.getValue().toCharArray();
            final KeyStore serverKs = KeyStore.getInstance("PKCS12");
            serverKs.load(new FileInputStream(RuntimeConfiguration.KEYSTORE_SERVERKEYSTORE_PATH.getValue()), serverStorePassphrase);
            final TrustManagerFactory serverTmf = TrustManagerFactory.getInstance("SunX509");
            serverTmf.init(serverKs);
            log.log(Level.INFO, "Done reading server jsk.");
            log.log(Level.INFO, "Building SSL Context");
            final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(clientKmf.getKeyManagers(), serverTmf.getTrustManagers(), null);
            factory.useSslProtocol(sslContext);
            log.log(Level.INFO, "Done building SSL Context");
        }
        return factory.newConnection();
    }

    public Channel connectToChannel(final Connection connection) throws IOException {
        return connection.createChannel();
    }

    public void declareQueue(final String queueName) throws IOException {
        this.channel.queueDeclare(queueName, false, false, true, null);
    }

    public void sendMessageToQueue(final String queueName, final String message) throws IOException {
        this.channel.basicPublish("", queueName, null, message.getBytes());
    }

    public void consumeQueue(final String queueName, final DeliverCallback cb) throws IOException {
        this.channel.basicConsume(queueName,true,cb,consumerTag -> {});
    }
}
