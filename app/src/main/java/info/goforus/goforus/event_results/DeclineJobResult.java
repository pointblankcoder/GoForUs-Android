package info.goforus.goforus.event_results;

import info.goforus.goforus.models.jobs.Job;

public class DeclineJobResult {
    private final Job mJob;

    public DeclineJobResult(Job job) {
        mJob = job;
    }

    public Job getJob() {
        return mJob;
    }
}