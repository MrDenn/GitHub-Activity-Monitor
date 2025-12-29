package io.github.MrDenn.githubactionmonitor.model;

public enum EventType {
    WORKFLOW_QUEUED,
    WORKFLOW_STARTED,
    WORKFLOW_COMPLETED,
    JOB_QUEUED,
    JOB_STARTED,
    JOB_COMPLETED,
    STEP_STARTED,
    STEP_COMPLETED
}