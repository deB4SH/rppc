package de.b4sh.rabbitmqpingpong.client;

import com.rabbitmq.client.DeliverCallback;
import de.b4sh.rabbitmqpingpong.RabbitMQService;
import de.b4sh.rabbitmqpingpong.RuntimeConfiguration;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ReceiverJob implements Job {

    private static final Logger log = Logger.getLogger(ReceiverJob.class.getName());
    private final RabbitMQService rabbitMQService;

    public ReceiverJob() throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        this.rabbitMQService = RabbitMQService.getInstance();
    }

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        final DeliverCallback cb = ((consumerTag, message) -> {
            final String unboxedMessage = new String(message.getBody(), StandardCharsets.UTF_8);
            log.log(Level.FINE, String.format("Received message: %s", unboxedMessage));
            this.rabbitMQService.sendMessageToQueue(RuntimeConfiguration.QUEUE_RECEIVER.getValue(), this.createReceiverMessage(unboxedMessage));
        });
        try {
            this.rabbitMQService.consumeQueue(RuntimeConfiguration.QUEUE_SENDER.getValue(), cb);
        } catch (final IOException e) {
            log.log(Level.WARNING, String.format("Received IO Exception. Check Stacktrace: %s", e.getCause()));
            throw new RuntimeException(e);
        }
    }

    private final String createReceiverMessage(final String input) {
        return String.format("%s-%s", input, System.currentTimeMillis());
    }
}
