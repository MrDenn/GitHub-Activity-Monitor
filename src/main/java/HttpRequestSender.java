import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpRequestSender {
    private static final String USER_AGENT = "ActionTrackingTool/1.0";
    private static final String AUTHORIZATION = "Bearer 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";
    private static final String API_VERSION = "2022-11-28";

    private HttpClient httpClient;

    /**
     * Constructor, initialises the httpClient variable with default values to be later used when
     * sending HTTP requests
     */
    public HttpRequestSender(){
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();;
    }

    /**
     * Constructs and sends an HTTP GET request to query all Workflow updates within the last
     * 15 000 milliseconds (or amount of time that corresponds to the update frequency)
     */
    public InputStream SendWorkflowRequest(String uri, int time) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uri))
                .header("User-Agent", USER_AGENT)
                .header("Authorization", AUTHORIZATION)
                .header("X-GitHub-Api-Version", API_VERSION)
                .header("Accept", "application/vnd.github+json")
                .build();

        HttpResponse<InputStream> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        return response.body();

        //String[] responses = response.body().split(",");
        //System.out.println("ID: " + responses[0] + " | Name: " + responses[1] + " | SHA: " + responses[4]);
    }
}
