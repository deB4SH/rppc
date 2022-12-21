package de.b4sh.rabbitmqpingpong;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeoutException;

public class RabbitMQService {

    public RabbitMQService() {
    }

    public Connection connectToRabbitMQ() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(RuntimeConfiguration.USERNAME.getValue());
        factory.setPassword(RuntimeConfiguration.PASSWORD.getValue());
        factory.setVirtualHost(RuntimeConfiguration.VIRTUALHOST.getValue());
        factory.setHost(RuntimeConfiguration.HOST.getValue());
        factory.setPort(Integer.parseInt(RuntimeConfiguration.PORT.getValue()));

        if(RuntimeConfiguration.USE_TLS_CLIENTCERT.getValue().equals("true")){
            char[] clientStorePassphrase = RuntimeConfiguration.KEYSTORE_CLIENTKEYSTORE_PASSPHRASE.getValue().toCharArray();
            KeyStore clientKs = KeyStore.getInstance("PKCS12");
            clientKs.load(new FileInputStream(RuntimeConfiguration.KEYSTORE_CLIENTKEYSTORE_PATH.getValue()), clientStorePassphrase);
            KeyManagerFactory clientKmf = KeyManagerFactory.getInstance("SunX509");
            clientKmf.init(clientKs, clientStorePassphrase);

            char[] serverStorePassphrase = RuntimeConfiguration.KEYSTORE_SERVERKEYSTORE_PASSPHRASE.getValue().toCharArray();
            KeyStore serverKs = KeyStore.getInstance("PKCS12");
            serverKs.load(new FileInputStream(RuntimeConfiguration.KEYSTORE_SERVERKEYSTORE_PATH.getValue()), serverStorePassphrase);
            TrustManagerFactory serverTmf = TrustManagerFactory.getInstance("SunX509");
            serverTmf.init(serverKs);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(clientKmf.getKeyManagers(),serverTmf.getTrustManagers(),null);
            factory.useSslProtocol(sslContext);
        }
        return factory.newConnection();
    }

    public Channel connectToChannel(final Connection connection) throws IOException {
        return connection.createChannel();
    }

    public void declareQueue(final Channel channel, final String queueName) throws IOException {
        channel.queueDeclare(queueName,false,true,true,null);
    }

    public void sendMessageToQueue(final Channel channel, final String queueName, final String message) throws IOException {
        channel.basicPublish("",queueName,null,message.getBytes());
    }
}
