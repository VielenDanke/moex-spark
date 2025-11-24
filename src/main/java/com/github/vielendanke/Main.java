package com.github.vielendanke;

import org.jobrunr.configuration.JobRunr;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.InMemoryStorageProvider;

import java.util.UUID;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) {
        SparkJob sparkJob = new SparkJob("MoexSparkJob");

        SimpleJobActivator simpleJobActivator = new SimpleJobActivator(sparkJob);

        InMemoryStorageProvider inMemoryStorageProvider = new InMemoryStorageProvider();

        JobScheduler jobScheduler = JobRunr.configure()
                .useJobActivator(simpleJobActivator)
                .useStorageProvider(inMemoryStorageProvider) // Uses your DB
                .useBackgroundJobServer() // Starts background workers
                .useDashboard(8000)       // Starts dashboard at port 8000
                .initialize()
                .getJobScheduler();

        // 4. Schedule a Job
        jobScheduler.scheduleRecurrently(
                UUID.randomUUID().toString(),
                System.getenv("SPARK_JOB_CRON"),
                sparkJob::executeSparkCassandraJob
        );

        // 5. Keep JVM Alive
        // In a real service, this would be your main application loop.
        System.out.println("JobRunr Service Started. Press Enter to exit...");
        try {
            System.in.read();
        } catch (Exception e) {
            System.err.printf("Error occurred while trying to run JobRunr Service. %s\n", e);
        }
    }
}
