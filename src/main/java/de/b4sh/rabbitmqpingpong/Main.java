package de.b4sh.rabbitmqpingpong;

import de.b4sh.rabbitmqpingpong.client.MetricsJob;
import de.b4sh.rabbitmqpingpong.client.ReceiverJob;
import de.b4sh.rabbitmqpingpong.client.SenderJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(final String[] args) throws SchedulerException {
        final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        if (RuntimeConfiguration.MODE.getValue().equals("sender")) {
            final JobKey sender = new JobKey("sender", "rppc");
            final JobDetail senderJob = JobBuilder.newJob(SenderJob.class).withIdentity(sender).build();
            final Trigger senderTrigger = TriggerBuilder.newTrigger().withIdentity("senderTrigger", "rppc")
                    .withSchedule(CronScheduleBuilder.cronSchedule(RuntimeConfiguration.SENDER_CRON.getValue())).build();
            scheduler.scheduleJob(senderJob, senderTrigger);
        } else if (RuntimeConfiguration.MODE.getValue().equals("receiver")) {
            final JobKey receiver = new JobKey("receiver", "rppc");
            final JobDetail receiverJob = JobBuilder.newJob(ReceiverJob.class).withIdentity(receiver).build();
            scheduler.addJob(receiverJob, true, true);
            scheduler.triggerJob(receiver);
        } else if (RuntimeConfiguration.MODE.getValue().equals("metrics")) {
            final JobKey metrics = new JobKey("metrics", "rppc");
            final JobDetail metricsJob = JobBuilder.newJob(MetricsJob.class).withIdentity(metrics).build();
            scheduler.addJob(metricsJob, true, true);
            scheduler.triggerJob(metrics);
        } else {
            log.log(Level.WARNING, "DEFAULT: please check mode configuration. Support modes for client.mode: sender, receiver, metrics");
        }
    }
}