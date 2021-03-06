package org.es4j.audit.logeventgenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import org.es4j.audit.dto.AuditEvent;
import org.es4j.audit.dto.RequestContext;
import org.es4j.audit.logeventgenerator.MockEventsSupplier;

import java.util.List;
import java.util.Random;


public class LoggingApp {

    private static Logger logger = LogManager.getLogger(LoggingApp.class);
    
    private Random           ran = new Random();
    private List<AuditEvent> events;

    public static void main(String[] args) {
        String member = "fakemember";
        if (args.length == 1) {
            member = args[0];
        }
        LoggingApp app = new LoggingApp(member);
        app.runApp(member);
        System.out.println("Job ended");
    }

    public LoggingApp(String member) {

        ThreadContext.clear();

        RequestContext.setSessionId("session1234");
        RequestContext.setIpAddress("127.0.0.1");
        RequestContext.setClientId("02121");
        RequestContext.setProductName("IB");
        RequestContext.setProductVersion("4.18.1");
        RequestContext.setLocale("en_US");
        RequestContext.setRegion("prod");

        if (events == null) {
            events = MockEventsSupplier.getAllEvents(member);
        }
    }

    public void runApp(String member) {
        Worker worker = new Worker(member);
        
        worker.start();
        sleep(30000);
        worker.shutdown();
        
        sleep(5000);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            //
        }
    }

    public class Worker extends Thread {

        private final String member;
        private boolean shutdown = false;

        public Worker(String member) {
            this.member = member;
        }

        @Override
        public void run() {
            System.out.println("STARTING..................");

            while (!shutdown) {
                // Generate rand number between 1 to 10
                int rand = ran.nextInt(9) + 1;

                // Sleep for rand seconds
                try {
                    Thread.sleep(rand * 1000);
                } catch (InterruptedException e) {
                    logger.warn("WARN", e);
                }

                // Write rand number of logs
                for (int i = 0; i < rand; i++) {
                    int eventIndex = (Math.abs(ran.nextInt())) % events.size();
                    AuditEvent event = events.get(eventIndex);
                    RequestContext.setUserId(member);
                    event.logEvent();

                    if ((rand % 4) == 1) {
                        logger.debug("DEBUG level logging.....");
                    } else if ((rand % 4) == 2) {
                        logger.info("INFO level logging.....");
                    } else if ((rand % 4) == 3) {
                        logger.warn("WARN level logging.....");
                    } else {
                        logger.error("ERROR level logging.....");
                    }
                }
            }
        }

        public void shutdown() {
            this.shutdown = true;
            try {
                this.join();
            } catch (InterruptedException ie) {
                //
            }
            System.out.println("SHUTDOWN.......................");
        }
    }
}
