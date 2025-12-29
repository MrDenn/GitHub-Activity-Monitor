# GitHub Action Monitor

A lightweight command-line tool that monitors GitHub Actions: creation, stating and completion of workflow runs, the jobs contained within them, down to the small steps inside each job, and reports them to stdout, one line per event.

## Features

- **Real-time monitoring** with 1-second polling intervals
- **Comprehensive event reporting**: workflow queuing/starts/completions, job starts/completions, step starts/completions
- **Rich event context**: timestamps, branch names, commit SHAs, and unique IDs for traceability
- **Graceful termination**: cleanly stop monitoring with Ctrl+C
- **Flexible repository input**: supports both `owner/repo` and `owner repo` formats

## Output Format

Each event is printed as a single line with fixed-width columns for easy parsing and log integration:

```
TIMESTAMP | EVENT_TYPE | NAME | RESULT | RUN_ID | JOB_ID | STEP_NUM | BRANCH | SHA
```

### Example Output

```
2025-12-29T09:30:15Z | RUN Queued    | Build and Test      |          | Run ID: 12345678910 |                     |           | Branch: main      | SHA: abc1234567890def
2025-12-29T09:30:18Z | RUN started   | Build and Test      |          | Run ID: 12345678910 |                     |           | Branch: main      | SHA: abc1234567890def
2025-12-29T09:30:22Z | Job started   | Build               |          | Run ID: 12345678910 | Job ID: 10987654321 |           | Branch: main      | SHA: abc1234567890def
2025-12-29T09:30:45Z | Step started  | Set up JDK          |          | Run ID: 12345678910 | Job ID: 10987654321 | Step:   1 | Branch: main      | SHA: abc1234567890def
2025-12-29T09:30:50Z | Step completed| Set up JDK          | success  | Run ID: 12345678910 | Job ID: 10987654321 | Step:   1 | Branch: main      | SHA: abc1234567890def
2025-12-29T09:30:45Z | Step started  | Update configs      |          | Run ID: 12345678910 | Job ID: 10987654321 | Step:   2 | Branch: main      | SHA: abc1234567890def
2025-12-29T09:30:50Z | Step completed| Update configs      | success  | Run ID: 12345678910 | Job ID: 10987654321 | Step:   2 | Branch: main      | SHA: abc1234567890def
2025-12-29T09:31:25Z | Job completed | Build               | success  | Run ID: 12345678910 | Job ID: 10987654321 |           | Branch: main      | SHA: abc1234567890def
2025-12-29T09:31:27Z | RUN completed | Build and Test      | success  | Run ID: 12345678910 |                     |           | Branch: main      | SHA: abc1234567890def
```

## Quick Start

### Prerequisites

- Java 21
- GitHub Personal Access Token (read-only, no special scopes needed)
- Address of the GitHub repository to monitor

### Building

```bash
mvn clean package
```

### Running

```bash
# Format 1: owner/repo as single argument
java -jar target/github-action-monitor.jar "owner/repo" "ghp_your_token_here"

# Format 2: owner and repo as separate arguments
java -jar target/github-action-monitor.jar "owner" "repo" "ghp_your_token_here"
```
For read-only monitoring, your GitHub Personal Access Token needs only:
- `public_repo` (if monitoring a public repository)
- `repo` (if monitoring a private repository)
- No write scopes are required

### Monitoring Behaviour

- Queries for all currently queued and in-progress workflows (last 24 hours)
- Does **not** report completed workflows from before the tool started
- Records the last retrieved and reported timestamp in `timestamps.json`

### Graceful Shutdown

Press `Ctrl+C` to stop monitoring. The tool will:
- Complete the current polling cycle
- Save the final timestamp
- Exit cleanly

## Behavior Details

### Polling & Event Loop

- Polls GitHub Actions API every **1 second** (configurable in code)
- Caches in-progress workflow runs in memory to track status changes
- Each polling cycle:
    1. Fetches new workflow runs
    2. Updates status of non-completed runs
    3. Fetches job details for non-queued runs
    4. Extracts events between last timestamp and current timestamp
    5. Prints events in chronological order
    6. Saves new timestamp

### Cleanup Strategy

- Removes completed workflow runs older than the **last reported timestamp** from memory
- Removes queued runs that are **older than 24 hours** (likely abandoned)
- This prevents unbounded memory growth during long monitoring sessions

## Incomplete Features

### Persistence
 
As implemented, upon terminating a session the state is saved to `timestamps.json` in the current working directory:
```json
{
  "repositories": [
    {
      "repository": "owner/repo",
      "timestamp": "2025-12-29T09:31:27.901234Z"
    }
  ]
}
```

Multiple repositories can be monitored independently; each maintains its own last-reported timestamp. This data was meant
to be used to retrieve all events that have occurred between the stored timestamp and the start of the utility. However,
multiple things got in the way:
- **Workflow runs started before last session was terminated**: main reason why this implementation falls flat - it's impossible to filter and retrieve workflow runs that "were in_progress at a certian time in the past"
- **Workflow runs started before the start of this session**: would normally be queried by getting all non-completed events, but adding all workflow runs that were running in between the stored timestamp and the start of this iteration results in duplication

Both of these issues can be solved by storing workflow runs alongside the timestamp for each repository, but I did not have time left to implement that.

### Run Attempt Handling

GitHub Actions support workflow run retries, which relaunches a workflow with an identical ID, but updated "run_attempt" field.
As implemented, the tool:
- Tracks `run_id` and `run_attempt` separately
- Reports only events for the first attempt of a run

This behaviour is not optimal, but I settled in this compromise due to how GitHub reports new attempts:
- When querying the same ID after a new attempt started, fields will contain updated information except for "started_at", which will retain the value of the very first attempt
- When querying runs that were created after a certain timestamp, new attempts created after that timestamp will not be reported if the original run has been created earlier than the timestamp

This behaviour of the REST API gets in the way of how workflow run caching is optimized in this tool, meaning that if new events are to be tracked, ALL previously completed runs would have to be stored and checked for new attempts indefinitely in order not to miss any new attempts.

Since re-tries of existing runs through the "new attempt" functionality was rare enough that I was often unable to retrieve recent examples of such events in order to test any connected functionality, I decided that ignoring new attempts of existing runs is a worthwhile compromise.


## Design Decisions & Architecture

### 1. REST API Approach: Polling vs. Webhooks

**Decision**: Periodic polling of GitHub REST API was used instead of webhooks.
- **Rationale**: polling does not require server infrastructure to implement, and so I decided it would be more appropriate for a CLI tool to work on periodic local generation of HTTP requests to the REST API
- **Tradeoff**: implementation through webhooks would have been superior both in terms of overhead and proper Event processing (deduplication and accurate timestamp tracking)

---

### 2. Multi-Level Event Extraction: Run, Job, Step

**Decision**: Jobs are stored within workflow runs, steps are stored within jobs, and events are gathered by iterating through these Lists at each level.
- **Rationale**: Storing jobs and runs hierarchically allows for the event generation to happen in a very intuitive order, and for details to be easily passed up and down throughout the data structure (mostly for clean output formatting)

---

### 3. In-Memory Workflow Cache

**Decision**: Keep active workflow runs in memory, updating them each cycle rather than full re-fetch.
- **Rationale**: Ensures no workflow runs go unnoticed; Easy to optimise update calls based on status and timestamps
- **Tradeoff**: Requires more API calls compared to re-fetching all running processes every time

---

### 4. Event Model: Sealed Interfaces + Records

**Decision**: Java's `sealed interface` with record implementations is used for the event hierarchy.
- **Rationale**: Allows for completely customized fields and output functions for each event type; Little overhead for adding new event types
- **Tradeoff**: Took more time to implement vs. single universal class, more "bulky"

---

### 5. Timestamp-Based Event Filtering

**Decision**: Events are filtered by a strict time window between previous iteration and current iteration
- **Rationale**: Eliminates duplication and ensures strictly consecutive printing

---

### 7. Persistence: Local JSON File

**Decision**: Last timestamp outputted is stored per repository in a simple `timestamps.json` file.
- **Rationale**: It's a much simpler solution than an entire database; It seemed like timestamps alone would be sufficient for the required functionality
- **Tradeoff**: This structure did not allow for proper persistence, and would need adjustments to allow for fetching of events in recurring sessions

---

## Known Limitations

### Incomplete functionality relative to the requirements
- **Retrieval of all events on a recurring session**: Not implemented.
- **Retrieval and processing of workflow re-runs**: Not implemented.

### Implementation limitations
- **API Rate Limiting**: There is no complex functionality to conform to GitHub rate limits, which may result in failures if polling rate is increased.
- **Workflow Run Pagination**: Only the first 30 runs are fetched per query. Repositories with many simultaneous runs may miss some events.
- **Details**: Not all fields from GitHub's API are exposed in events; a lot of data is emitted.
- **Initial 24-Hour Lookback**: Queued runs are only queried from the last 24 hours, which may not always be caused by a stale workflow run.

## Future Enhancements

- Configurable polling interval
- Smart backoff algorithm to conform to API rate limiting 
- JSON Lines output format for programmatic consumption
- Event output filtering (by workflow, depth level, branch, actor)
- Detailed error reporting
- Webhook support as an alternative to polling
- SQLite backend for full persistence across sessions and machines

## Building & Testing

```bash
# Build the JAR
mvn clean package

# Run the JAR
java -jar target/github-action-monitor.jar {owner/repo} {GitHub token}

# For testing, I suggest either of these repositories:
#   - rust-lang/rust
#   - NixOS/nixpkgs
# Cross-reference with GitHub Actions tab
# Check that timestamps.json was created/updated
```

## Architecture Overview

```
[Package] main/java/io/github/MrDenn/githubactionmonitor
  ├── GithubActionMonitor (MAIN class)
  ├── [Package] utils
  │   ├── HttpRequestSender (REST API communication)
  │   │   └── HttpClient (Java 11+)
  │   ├── JacksonParser (JSON deserialization)
  │   │   └── Jackson 3.x library
  │   └── PersistenceManager (Timestamp storage)
  │       └── timestamps.json
  └── [Package] model
      ├── Event (sealed interface)
      ├── WorkflowRun
      ├── Job
      └── Step
```

---

**Last updated**: December 2025
**Java Version**: 21
**Dependencies**: Jackson 3.x, Java 11+ HttpClient
