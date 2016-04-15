package info.goforus.goforus.event_results;

import info.goforus.goforus.models.jobs.Job;

public class AcceptJobResult {

    private final Job mJob;

    public AcceptJobResult(Job job) {
        mJob = job;
    }

    public Job getJob() {
        return mJob;
    }
}