package de.b4sh.rabbitmqpingpong.client;

import com.rabbitmq.client.DeliverCallback;
import de.b4sh.rabbitmqpingpong.RabbitMQService;
import de.b4sh.rabbitmqpingpong.RuntimeConfiguration;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

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

public class MetricsJob implements Job {

    private final Logger log = Logger.getLogger(MetricsJob.class.getName());
    private final RabbitMQService rabbitMQService;
    private final Gauge senderGauge;
    private final Gauge receiverGauge;

    public MetricsJob() throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        this.log.log(Level.INFO, "Initialize of MetricsJob");
        this.rabbitMQService = RabbitMQService.getInstance();
        this.log.log(Level.INFO, "Setup Gauges");
        if (RuntimeConfiguration.METRICS_ENVIRONMENT_NAME.getValue().isEmpty()) {
            this.senderGauge = Gauge.build().name("rabbitmq_sender_timestamp").help("Epoch Timestamp of sender").register();
            this.receiverGauge = Gauge.build().name("rabbitmq_receiver_timestamp").help("Epoch Timestamp of receiver").register();
        } else {
            this.senderGauge = Gauge.build().name(String.format("rabbitmq_sender_%s_timestamp", RuntimeConfiguration.METRICS_ENVIRONMENT_NAME.getValue().toLowerCase())).help("Epoch Timestamp of sender").register();
            this.receiverGauge = Gauge.build().name(String.format("rabbitmq_receiver_%s_timestamp", RuntimeConfiguration.METRICS_ENVIRONMENT_NAME.getValue().toLowerCase())).help("Epoch Timestamp of receiver").register();
        }
        this.log.log(Level.INFO, "Done setting up Class");
        // Expose Prometheus metrics.
        this.log.log(Level.INFO, "Setting up Prometheus Exporter");
        final CollectorRegistry registry = CollectorRegistry.defaultRegistry;
        DefaultExports.register(registry);
        new HTTPServer.Builder().withPort(8080).withRegistry(registry).build();
        this.log.log(Level.INFO, "Done with Prometheus Exporter");
    }

    @Override
    public void execute(final JobExecutionContext context) {
        this.log.log(Level.INFO, "Start of Execute");
        final DeliverCallback cb = ((consumerTag, message) -> {
            final String unboxedMessage = new String(message.getBody(), StandardCharsets.UTF_8);
            this.log.log(Level.INFO, String.format("Received message: %s", unboxedMessage));
            final String[] splits = unboxedMessage.split("-");
            this.senderGauge.set(Double.parseDouble(splits[1]));
            this.receiverGauge.set(Double.parseDouble(splits[2]));
        });
        try {
            this.log.log(Level.INFO, "Adding consume hook.");
            this.rabbitMQService.consumeQueue(RuntimeConfiguration.QUEUE_RECEIVER.getValue(), cb);
        } catch (final IOException e) {
            this.log.log(Level.WARNING, String.format("Received IO Exception. Check Stacktrace: %s", e.getCause()));
            throw new RuntimeException(e);
        }
    }
}
