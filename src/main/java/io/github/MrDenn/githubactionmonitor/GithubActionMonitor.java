package io.github.MrDenn.githubactionmonitor;

import io.github.MrDenn.githubactionmonitor.model.Event;
import io.github.MrDenn.githubactionmonitor.model.WorkflowRun;
import io.github.MrDenn.githubactionmonitor.util.HttpRequestSender;
import io.github.MrDenn.githubactionmonitor.util.JacksonParser;

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

    private static boolean isFirstIteration = true;
    private static Instant lastTimestamp;

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

        workflowRuns = new ArrayList<>();
        events = new ArrayList<>();

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

                events.clear();

                removeCompletedWorkflowRuns(lastTimestamp);
                lastTimestamp = newTimestamp;
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

    private static void getWorkflowRunsAfterTimestamp(Instant timestamp)
            throws IOException, InterruptedException {

        InputStream data = requester.getWorkflowRunsWithParameter(
                "created", "%3E" + timestamp.toString());

        workflowRuns.addAll(parser.parseWorkflowDetails(data));
    }

    private static void getWorkflowRunsNonCompleted()
            throws IOException, InterruptedException {

        InputStream dataQueued = requester.getWorkflowRunsWithParameter("status",
                "queued");
        InputStream dataInProgress = requester.getWorkflowRunsWithParameter("status",
                "in_progress");

        workflowRuns.addAll(parser.parseWorkflowDetails(dataQueued));
        workflowRuns.addAll(parser.parseWorkflowDetails(dataInProgress));
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
            throws IOException, InterruptedException {

        for (WorkflowRun workflowRun : workflowRuns) {
            if (!workflowRun.getStatus().equals("queued")) {
                InputStream data = requester.getJobs(workflowRun.getRunId());
                workflowRun.setJobs(parser.parseJobDetails(data));
            }
        }
    }

    private static void updateStatusOfWorkflowRuns()
            throws IOException, InterruptedException {

        for (WorkflowRun existingRun : workflowRuns) {
            if (!existingRun.getStatus().equals("completed")) {
                InputStream data = requester.getSingleWorkflowRun(existingRun.getRunId(),
                        existingRun.getAttemptNumber());
                WorkflowRun newRun = parser.parseSingleWorkflowRunDetails(data);
                existingRun.updateStatus(newRun);
            }
        }

    }

    private static void removeCompletedWorkflowRuns(Instant cutoffTimestamp) {
        workflowRuns.removeIf(run -> (run.getStatus().equals("completed")
                && run.getUpdatedAt().isBefore(cutoffTimestamp)));
    }

    private static void removeStuckQueuedWorkflowRuns() {
        workflowRuns.removeIf(run -> run.getCreatedAt().isBefore(Instant.now().minusSeconds(86400)));
    }
}
