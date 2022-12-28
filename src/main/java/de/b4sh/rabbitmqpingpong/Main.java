package de.b4sh.rabbitmqpingpong;

import de.b4sh.rabbitmqpingpong.client.ReceiverJob;
import de.b4sh.rabbitmqpingpong.client.SenderJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

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
                    .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?")).build();
            scheduler.scheduleJob(senderJob, senderTrigger);
        } else if (RuntimeConfiguration.MODE.getValue().equals("receiver")) {
            final JobKey receiver = new JobKey("receiver", "rppc");
            final JobDetail receiverJob = JobBuilder.newJob(ReceiverJob.class).withIdentity(receiver).build();
            scheduler.addJob(receiverJob,true,true);
            scheduler.triggerJob(receiver);

        } else {
            //TODO: implement overviewer with metrics.
        }
    }
}