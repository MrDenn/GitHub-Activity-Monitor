import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GithubActionMonitor {

    private static HttpRequestSender requester;
    private static JacksonParser parser;

    private static List<WorkflowRun> workflowRuns;
    private static List<Event> events;
    private static Map<WorkflowRunAttemptKey, Instant> lastAccessTimestamps;

    private static boolean isFirstIteration = true;
    private static Instant lastTimestamp;


//    public record WorkflowRunAttemptKey(long runId, int attempt) {}

    public static void main(String[] args) {
        String repo;
        String token;

        if (args.length != 2 && args.length != 3) {
            System.out.println("Input arguments incorrect. Proper usage:");
            System.out.println("java GithubActionMonitor <owner/repository combination> <GitHub access token>");
            System.out.println("java GithubActionMonitor <owner> <repository> <GitHub access token>");
            return;
        } else if (args.length == 2) {
            repo = args[0];
            token = args[1];
        } else {
            repo = args[0] + "/" + args[1];
            token = args[2];
        }

        requester = new HttpRequestSender(repo, token, 60);
        parser = new JacksonParser();

        workflowRuns = new ArrayList<>();
        events = new ArrayList<>();
        lastAccessTimestamps = new HashMap<>();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> monitorRepositoryEvents(), 10, 10, TimeUnit.SECONDS);


//        try {
//            List<WorkflowRun> workflowRuns = getWorkflowRunsAfterTimestamp(Instant.now().minusMillis(60000*3));
//            updateJobsInWorkflowRuns(workflowRuns);
//
//            for (WorkflowRun workflowRun : workflowRuns) {
//                System.out.println(workflowRun.toString());
//            }
//
//            List<Event> events = new ArrayList<>();
//            for (WorkflowRun workflowRun : workflowRuns) {
//                events.addAll(workflowRun.getEvents());
//            }
//
//            System.out.println(events);
//
//        } catch (Exception e) {}
    }

    private static void monitorRepositoryEvents () {
        try {

            if (isFirstIteration) {
                getWorkflowRunsNonCompleted(workflowRuns);
                lastTimestamp = Instant.now();
                isFirstIteration = false;
            } else {
                getWorkflowRunsAfterTimestamp(workflowRuns, lastTimestamp);
                updateJobsInWorkflowRuns(workflowRuns);

//                for (WorkflowRun workflowRun : workflowRuns) {
//                    System.out.println(workflowRun.toString());
//                }

                List<Event> eventsOfRun;
                Instant lastAccessedTimestamp;

                for (WorkflowRun run : workflowRuns) {
                    if (lastAccessTimestamps.get(run.getRunAttemptKey()) == null) {
                        lastAccessedTimestamp = lastTimestamp;
                    } else {
                        lastAccessedTimestamp = lastAccessTimestamps.get(run.getRunAttemptKey());
                    }
                    eventsOfRun = run.getEvents(lastAccessedTimestamp);
                    if (eventsOfRun.size() > 0) {
                        lastAccessedTimestamp = eventsOfRun.getLast().getTimestamp();
                    } else {
                        lastAccessedTimestamp = lastTimestamp;
                    }
                    lastAccessTimestamps.put(run.getRunAttemptKey(), lastAccessedTimestamp);
                    events.addAll(eventsOfRun);
                }
                events.sort(Comparator.comparing(Event::getTimestamp));

                System.out.println(events);
                events.clear();
            }

        } catch (Exception e) {
            if (e instanceof IOException) {
                System.out.println("I/O exception: " + e.getMessage());
            } else if (e instanceof RuntimeException) {
                System.out.println("Runtime exception: " + e.getMessage());
            } else {
                System.out.println("Unexpected exception: " + e.getMessage());
            }
        }
    }

    private static void getWorkflowRunsAfterTimestamp(List<WorkflowRun> workflowRuns, Instant timestamp)
            throws IOException, InterruptedException {

        InputStream data = requester.getWorkflowRunsWithParameter(
                "created", "%3E" + timestamp.toString());

        workflowRuns.addAll(parser.parseWorkflowDetails(data));
    }

    private static void getWorkflowRunsNonCompleted(List<WorkflowRun> workflowRuns)
            throws IOException, InterruptedException {

        InputStream dataQueued = requester.getWorkflowRunsWithParameter("status",
                "queued");
        InputStream dataInProgress = requester.getWorkflowRunsWithParameter("status",
                "in_progress");

        workflowRuns.addAll(parser.parseWorkflowDetails(dataQueued));
        workflowRuns.addAll(parser.parseWorkflowDetails(dataInProgress));
    }

    private static void getWorkflowRunPreviousAttempts(List<WorkflowRun> workflowRuns)
            throws IOException, InterruptedException {

        for (int i = 0; i < workflowRuns.size(); i++) {
            if (workflowRuns.get(i).getAttemptNumber() > 1){
                InputStream dataPrev = requester.getHttpResponse(workflowRuns.get(i).getPreviousAttemptUrl());
                workflowRuns.addAll(parser.parseSingleWorkflowRunDetails(dataPrev));
            }
        }
    }

    private static void updateJobsInWorkflowRuns(List<WorkflowRun> workflowRuns)
            throws IOException, InterruptedException {

        for (WorkflowRun workflowRun : workflowRuns) {
            InputStream data = requester.getJobs(workflowRun.getRunId());
            workflowRun.setJobs(parser.parseJobDetails(data));
        }
    }
}
