import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GithubActionMonitor {

    private static HttpRequestSender requester;
    private static JacksonParser parser;

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

        requester = new HttpRequestSender(repo, token, 15);
        parser = new JacksonParser();

        try {
            List<WorkflowRun> workflowRuns = getWorkflowRunsAfterTimestamp(Instant.now().minusMillis(60000*5));
            updateJobsInWorkflowRuns(workflowRuns);

            for (WorkflowRun workflowRun : workflowRuns) {
                System.out.println(workflowRun.toString());
            }

            List<Event> events = new ArrayList<>();
            for (WorkflowRun workflowRun : workflowRuns) {
                events.addAll(workflowRun.getEvents());
            }

            System.out.println(events);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static List<WorkflowRun> getWorkflowRunsAfterTimestamp(Instant timestamp)
            throws IOException, InterruptedException {

        InputStream data = requester.getWorkflowRunsWithParameter(
                "created", "%3E" + timestamp.toString());

        return parser.parseWorkflowDetails(data);
    }

    private static List<WorkflowRun> getWorkflowRunPreviousAttempts(List<WorkflowRun> workflowRuns)
            throws IOException, InterruptedException {

        for (int i = 0; i < workflowRuns.size(); i++) {
            if (workflowRuns.get(i).getAttemptNumber() > 1){
                InputStream dataPrev = requester.getHttpResponse(workflowRuns.get(i).getPreviousAttemptUrl());
                workflowRuns.addAll(parser.parseSingleWorkflowRunDetails(dataPrev));
            }
        }

        return workflowRuns;
    }

    private static void updateJobsInWorkflowRuns(List<WorkflowRun> workflowRuns)
            throws IOException, InterruptedException {

        for (WorkflowRun workflowRun : workflowRuns) {
            InputStream data = requester.getJobs(workflowRun.getRunId());
            workflowRun.setJobs(parser.parseJobDetails(data));
        }
    }
}
