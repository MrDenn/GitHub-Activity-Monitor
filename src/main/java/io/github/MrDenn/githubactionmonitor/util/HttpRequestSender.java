package io.github.MrDenn.githubactionmonitor.util;

import io.github.MrDenn.githubactionmonitor.exception.IncorrectUserArgumentsException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpRequestSender {
    private static final String USER_AGENT = "ActionTrackingTool/1.0";
    private static final String API_VERSION = "2022-11-28";

    private final String repo;
    private final String authorisation;

    private final HttpClient httpClient;

    /**
     * Constructor
     *
     * @param repo Address of desired repository within GitHub in [owner/repo] String form
     * @param token GitHub personal access token in String form (can have no write permissions)
     * @param timeout Http timeout given in seconds
     */
    public HttpRequestSender(String repo, String token, int timeout) {
        this.repo = repo;
        this.authorisation = "Bearer " + token;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeout))
                .build();
    }

    /**
     * Retrieves all details of workflow runs for a given repository that meet the given parameters
     *
     * @param paramName name of parameter used to filter workflow runs
     * @param paramValue value of parameter used to filter workflow runs
     * @return All details received from REST API in raw InputStream form
     * @throws IOException if an I/O error occurs when sending or receiving,
     * or the client has {@linkplain ##closing shut down}
     * @throws InterruptedException if the operation is interrupted
     * @throws IncorrectUserArgumentsException if the arguments provided by the user are incorrect
     */
    public InputStream getWorkflowRunsWithParameter(String paramName, String paramValue)
            throws IOException, InterruptedException, IncorrectUserArgumentsException {

        return getHttpResponse("https://api.github.com/repos/" + this.repo + "/actions/runs?" +
                paramName + "=" + paramValue);
    }

    /**
     * Retrieves all details of a single given workflow run
     *
     * @param workflowRunId Run ID of the workflow run to be queried
     * @param workflowRunAttempt Sequential attempt number of the workflow run attempt to be queried
     * @return All details received from REST API in raw InputStream form
     * @throws IOException if an I/O error occurs when sending or receiving,
     * or the client has {@linkplain ##closing shut down}
     * @throws InterruptedException if the operation is interrupted
     * @throws IncorrectUserArgumentsException if the arguments provided by the user are incorrect
     */
    public InputStream getSingleWorkflowRun(long workflowRunId, long workflowRunAttempt)
            throws IOException, InterruptedException, IncorrectUserArgumentsException {

        return getHttpResponse("https://api.github.com/repos/" + this.repo + "/actions/runs/" +
                workflowRunId + "/attempts/" + workflowRunAttempt);
    }

    /**
     * Retrieves all details of all jobs for a given repository and workflow
     *
     * @param workflowId id of the parent workflow run
     * @return All details received from REST API in raw InputStream form
     * @throws IOException if an I/O error occurs when sending or receiving,
     * or the client has {@linkplain ##closing shut down}
     * @throws InterruptedException if the operation is interrupted
     * @throws IncorrectUserArgumentsException if the arguments provided by the user are incorrect
     */
    public InputStream getJobs(long workflowId)
            throws IOException, InterruptedException, IncorrectUserArgumentsException {

        return getHttpResponse("https://api.github.com/repos/" + this.repo + "/actions/runs/" +
                workflowId + "/jobs");
    }

    /**
     * Retrieves a JSON response by the REST API to HTTP GET command with the appropriate URI
     *
     * @param uri URI, to which the HTTP response it to be sent
     * @return All details received from REST API in raw InputStream form
     * @throws IOException if an I/O error occurs when sending or receiving,
     * or the client has {@linkplain ##closing shut down}
     * @throws InterruptedException if the operation is interrupted
     * @throws IncorrectUserArgumentsException if the arguments provided by the user are incorrect
     */
    public InputStream getHttpResponse(String uri)
            throws IOException, InterruptedException, IncorrectUserArgumentsException {

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uri))
                .header("User-Agent", USER_AGENT)
                .header("Authorization", authorisation)
                .header("X-GitHub-Api-Version", API_VERSION)
                .header("Accept", "application/vnd.github+json")
                .build();

        HttpResponse<InputStream> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else if (response.statusCode() == 404) {
            throw new IncorrectUserArgumentsException("repository address");
        } else if (response.statusCode() == 401) {
            throw new IncorrectUserArgumentsException("GitHub token");
        } else if (response.statusCode() == 403 || response.statusCode() == 429) {
            System.out.println("WARNING: Rate limit exceeded, retrying with delay");
            return this.getHttpResponse(uri);
        } else {
            System.out.println("WARNING: Unusual HTTP Code received: " + response.statusCode());
            return response.body();
        }
    }
}
