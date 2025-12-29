package io.github.MrDenn.githubactionmonitor.model;

import java.time.Instant;

public sealed interface Event permits Event.RunQueued, Event.RunStarted, Event.RunCompleted,
        Event.JobStarted, Event.JobCompleted, Event.StepStarted, Event.StepCompleted {

    Instant timestamp();

    record RunQueued(Instant timestamp, String name, long runId,
                     String branch, String headSha) implements Event {
        @Override
        public String toString() {
            String output = "";

            output += timestamp;
            output += " |     RUN Queued | ";
            output += String.format("%-45s", name.substring(0, Math.min(name.length(), 45)));
            output += " |                   ";
            output += " | Run ID: " + String.format("%11d", runId);
            output += " |                    ";
            output += " |                 ";
            output += " | Branch: " + String.format("%-36s",
                    branch.substring(0, Math.min(branch.length(), 36)));
            output += " | SHA: " + headSha;

            return output;
        }
    }

    record RunStarted(Instant timestamp, String name, long runId,
                      String branch, String headSha) implements Event {
        @Override
        public String toString() {
            String output = "";

            output += timestamp;
            output += " |    RUN started | ";
            output += String.format("%-45s", name.substring(0, Math.min(name.length(), 45)));
            output += " |                   ";
            output += " | Run ID: " + String.format("%11d", runId);
            output += " |                    ";
            output += " |                 ";
            output += " | Branch: " + String.format("%-36s",
                    branch.substring(0, Math.min(branch.length(), 36)));
            output += " | SHA: " + headSha;

            return output;
        }
    }

    record RunCompleted(Instant timestamp, String name, long runId,
                        String branch, String headSha,
                        String conclusion) implements Event {
        @Override
        public String toString() {
            String output = "";

            output += timestamp;
            output += " |  RUN completed | ";
            output += String.format("%-45s", name.substring(0, Math.min(name.length(), 45)));
            output += " | Result: " + String.format("%10s", conclusion);
            output += " | Run ID: " + String.format("%11d", runId);
            output += " |                    ";
            output += " |                 ";
            output += " | Branch: " + String.format("%-36s",
                    branch.substring(0, Math.min(branch.length(), 36)));
            output += " | SHA: " + headSha;

            return output;
        }
    }


    record JobStarted(Instant timestamp, String name, long runId, long jobId,
                      String branch, String headSha) implements Event {
        @Override
        public String toString() {
            String output = "";

            output += timestamp;
            output += " |    Job started | ";
            output += String.format("%-45s", name.substring(0, Math.min(name.length(), 45)));
            output += " |                   ";
            output += " | Run ID: " + String.format("%11d", runId);
            output += " | Job ID: " + String.format("%11d", jobId);
            output += " |                 ";
            output += " | Branch: " + String.format("%-36s",
                    branch.substring(0, Math.min(branch.length(), 36)));
            output += " | SHA: " + headSha;

            return output;
        }
    }

    record JobCompleted(Instant timestamp, String name, long runId, long jobId,
                        String branch, String headSha,
                        String conclusion) implements Event {
        @Override
        public String toString() {
            String output = "";

            output += timestamp;
            output += " |  Job completed | ";
            output += String.format("%-45s", name.substring(0, Math.min(name.length(), 45)));
            output += " | Result: " + String.format("%10s", conclusion);
            output += " | Run ID: " + String.format("%11d", runId);
            output += " | Job ID: " + String.format("%11d", jobId);
            output += " |                 ";
            output += " | Branch: " + String.format("%-36s",
                    branch.substring(0, Math.min(branch.length(), 36)));
            output += " | SHA: " + headSha;

            return output;
        }
    }


    record StepStarted(Instant timestamp, String name, long runId, long jobId, long stepNumber,
                       String branch, String headSha) implements Event {
        @Override
        public String toString() {
            String output = "";

            output += timestamp;
            output += " |   Step started | ";
            output += String.format("%-45s", name.substring(0, Math.min(name.length(), 45)));
            output += " |                   ";
            output += " | Run ID: " + String.format("%11d", runId);
            output += " | Job ID: " + String.format("%11d", jobId);
            output += " | Step number: " + String.format("%3d", stepNumber);
            output += " | Branch: " + String.format("%-36s",
                    branch.substring(0, Math.min(branch.length(), 36)));
            output += " | SHA: " + headSha;

            return output;
        }
    }

    record StepCompleted(Instant timestamp, String name, long runId, long jobId, long stepNumber,
                         String branch, String headSha,
                         String conclusion) implements Event {
        @Override
        public String toString() {
            String output = "";

            output += timestamp;
            output += " | Step completed | ";
            output += String.format("%-45s", name.substring(0, Math.min(name.length(), 45)));
            output += " | Result: " + String.format("%10s", conclusion);
            output += " | Run ID: " + String.format("%11d", runId);
            output += " | Job ID: " + String.format("%11d", jobId);
            output += " | Step number: " + String.format("%3d", stepNumber);
            output += " | Branch: " + String.format("%-36s",
                    branch.substring(0, Math.min(branch.length(), 36)));
            output += " | SHA: " + headSha;

            return output;
        }
    }
}
