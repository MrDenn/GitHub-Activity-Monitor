import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

public class GithubActionMonitor {

    public static void main(String[] args) {
        HttpRequestSender requester = new HttpRequestSender();
        JacksonParser parser = new JacksonParser();

        try {
            InputStream response = requester.SendWorkflowRequest("https://api.github.com/repos/JetBrains/compose-multiplatform/actions/runs", 15000);
            List<WorkflowRun> workflowRuns = parser.parseJson(response);
            System.out.println(workflowRuns.toString());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
