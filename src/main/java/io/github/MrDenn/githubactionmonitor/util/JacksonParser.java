package io.github.MrDenn.githubactionmonitor.util;

import io.github.MrDenn.githubactionmonitor.model.Job;
import io.github.MrDenn.githubactionmonitor.model.WorkflowRun;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

public class JacksonParser {
    ObjectMapper mapper;

    public JacksonParser() {
        mapper = new ObjectMapper();
    }

    public List<WorkflowRun> parseWorkflowDetails(InputStream inputStream) {
        JsonNode root = mapper.readTree(inputStream);
        JsonNode runsNode = root.path("workflow_runs");

        return mapper.readerForListOf(WorkflowRun.class).readValue(runsNode);
    }

    public WorkflowRun parseSingleWorkflowRunDetails(InputStream inputStream) {
        JsonNode root = mapper.readTree(inputStream);

        return mapper.readerFor(WorkflowRun.class).readValue(root);
    }

    public List<Job> parseJobDetails(InputStream inputStream) {
        JsonNode root = mapper.readTree(inputStream);
        JsonNode jobsNode = root.path("jobs");

        return mapper.readerForListOf(Job.class).readValue(jobsNode);
    }

}
