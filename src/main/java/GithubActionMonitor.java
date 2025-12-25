import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
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

            for (WorkflowRun workflowRun : workflowRuns) {
                System.out.println(workflowRun.toString());
            }

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
}
