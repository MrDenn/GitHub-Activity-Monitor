package io.github.MrDenn.githubactionmonitor;

import io.github.MrDenn.githubactionmonitor.exception.IncorrectUserArgumentsException;
import io.github.MrDenn.githubactionmonitor.model.Event;
import io.github.MrDenn.githubactionmonitor.model.WorkflowRun;
import io.github.MrDenn.githubactionmonitor.util.HttpRequestSender;
import io.github.MrDenn.githubactionmonitor.util.JacksonParser;
import io.github.MrDenn.githubactionmonitor.util.PersistenceManager;

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
    private static PersistenceManager saver;

    private static List<WorkflowRun> workflowRuns;
    private static List<Event> events;

    private static boolean isFirstIteration = true;
    private static Instant lastTimestamp;
    private static String repoPath;

    public static void main(String[] args) {
        String repo;
        String token;

        if (args.length != 2 && args.length != 3) {
            System.out.println("Input arguments incorrect. Proper usage (either of the following):");
            System.out.println("java -jar [utility].jar <owner/repository combination> <GitHub access token>");
            System.out.println("java -jar [utility].jar <owner> <repository> <GitHub access token>");
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
        saver = new PersistenceManager();

        workflowRuns = new ArrayList<>();
        events = new ArrayList<>();

        repoPath = repo;
        saver.load();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(GithubActionMonitor::monitorRepositoryEvents,
                0, 1, TimeUnit.SECONDS);
    }

    private static void monitorRepositoryEvents () {
        try {

            if (isFirstIteration) {
                getWorkflowRunsNonCompleted();
                lastTimestamp = Instant.now();
                isFirstIteration = false;
            } else {
                getWorkflowRunsAfterTimestamp(lastTimestamp);
                Instant newTimestamp = Instant.now();

                removeStuckQueuedWorkflowRuns();
                updateStatusOfWorkflowRuns();
                updateJobsInWorkflowRuns();


                for (WorkflowRun run : workflowRuns) {
                    events.addAll(run.getEvents(lastTimestamp, newTimestamp));
                }
                events.sort(Comparator.comparing(Event::timestamp));

                for (Event event : events) {
                    System.out.println(event);
                }
                saver.update(repoPath, newTimestamp);
                saver.save();

                events.clear();

                removeCompletedWorkflowRuns(lastTimestamp);
                lastTimestamp = newTimestamp;
            }

        } catch (Exception e) {
            if (e instanceof IOException) {
                System.out.println("I/O exception: " + e.getMessage());
            } else if (e instanceof IncorrectUserArgumentsException) {
                System.out.println(e.getMessage());
                System.exit(1);
            } else if (e instanceof RuntimeException) {
                System.out.println("Runtime exception: " + e.getMessage());
            } else {
                System.out.println("Unexpected exception: " + e.getMessage());
            }
        }
    }

    private static void getWorkflowRunsAfterTimestamp(Instant timestamp)
            throws IOException, InterruptedException, IncorrectUserArgumentsException {

        InputStream data = requester.getWorkflowRunsWithParameter(
                "created", "%3E" + timestamp.toString());

        workflowRuns.addAll(parser.parseWorkflowDetails(data));
    }

    private static void getWorkflowRunsNonCompleted()
            throws IOException, InterruptedException, IncorrectUserArgumentsException {

        InputStream dataQueued = requester.getWorkflowRunsWithParameter("status",
                "queued&created=%3E" + Instant.now().minusSeconds(86400).toString());
        InputStream dataInProgress = requester.getWorkflowRunsWithParameter("status",
                "in_progress");

        workflowRuns.addAll(parser.parseWorkflowDetails(dataQueued));
        workflowRuns.addAll(parser.parseWorkflowDetails(dataInProgress));
    }

    /**
     * Adds the last 100 workflow runs that have been created since the termination of the last
     * time this utility was monitoring the same repository.
     *
     * @throws IOException if an I/O error occurs when sending or receiving,
     * or the client has {@linkplain ##closing shut down}
     * @throws InterruptedException if the operation is interrupted
     * @throws IncorrectUserArgumentsException if the arguments provided by the user are incorrect
     */
    private static void getWorkflowRunsFromSavedTimestamp()
            throws IOException, InterruptedException, IncorrectUserArgumentsException {

        saver.load();
        Instant timestamp = saver.get(repoPath);

        if (timestamp.isAfter(Instant.now())) {
            System.out.println("Repository haven't been queried before, starting from scratch");
        } else {
            System.out.println(
                    "Repository has been queried before, retrieving all events after last run");

            InputStream data = requester.getWorkflowRunsWithParameter("created",
                    "%3E" + timestamp.toString() + "&per_page=100");
            workflowRuns.addAll(parser.parseWorkflowDetails(data));
        }
    }

    private static void getWorkflowRunPreviousAttempts()
            throws IOException, InterruptedException {

        for (int i = 0; i < workflowRuns.size(); i++) {
            if (workflowRuns.get(i).getAttemptNumber() > 1){
                InputStream dataPrev = requester.getHttpResponse(workflowRuns.get(i).getPreviousAttemptUrl());
                workflowRuns.add(parser.parseSingleWorkflowRunDetails(dataPrev));
            }
        }
    }

    private static void updateJobsInWorkflowRuns()
            throws IOException, InterruptedException, IncorrectUserArgumentsException {

        for (WorkflowRun workflowRun : workflowRuns) {
            if (!workflowRun.getStatus().equals("queued")) {
                InputStream data = requester.getJobs(workflowRun.getRunId());
                workflowRun.setJobs(parser.parseJobDetails(data));
            }
        }
    }

    private static void updateStatusOfWorkflowRuns()
            throws IOException, InterruptedException, IncorrectUserArgumentsException {

        for (WorkflowRun existingRun : workflowRuns) {
            if (!existingRun.getStatus().equals("completed")) {
                InputStream data = requester.getSingleWorkflowRun(existingRun.getRunId(),
                        existingRun.getAttemptNumber());
                WorkflowRun newRun = parser.parseSingleWorkflowRunDetails(data);
                existingRun.updateStatus(newRun);
            }
        }

    }

    /**
     * Removes all workflow runs from the WorkflowRuns List that have been completed before
     * [cutoffTimestamp]. The cutoffTimestamp is to ensure that all events have already been
     * reported for the workflow runs being deleted.
     *
     * @param cutoffTimestamp timestamp, such that all events that occurred before it have been
     *                        processed and printed
     */
    private static void removeCompletedWorkflowRuns(Instant cutoffTimestamp) {
        workflowRuns.removeIf(run -> (run.getStatus().equals("completed")
                && run.getUpdatedAt().isBefore(cutoffTimestamp)));
    }

    /**
     * Removes all workflow runs from the WorkflowRuns List that are still queued after 24 hours
     * since being created (very likely stale or "stuck").
     */
    private static void removeStuckQueuedWorkflowRuns() {
        workflowRuns.removeIf(run -> run.getCreatedAt().isBefore(Instant.now().minusSeconds(86400)));
    }
}
