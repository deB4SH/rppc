package de.b4sh.rabbitmqpingpong.client;

import de.b4sh.rabbitmqpingpong.RabbitMQService;
import de.b4sh.rabbitmqpingpong.RuntimeConfiguration;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeoutException;

public final class SenderJob implements Job {

    private final RabbitMQService rabbitMQService;

    public SenderJob() throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        this.rabbitMQService = RabbitMQService.getInstance();
    }

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            this.rabbitMQService.sendMessageToQueue(RuntimeConfiguration.QUEUE_SENDER.getValue(), this.createSenderMessage());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final String createSenderMessage() {
        return String.format("message-%s", System.currentTimeMillis());
    }
}
