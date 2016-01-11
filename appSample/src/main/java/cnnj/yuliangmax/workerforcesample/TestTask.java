package cnnj.yuliangmax.workerforcesample;

import android.os.SystemClock;

import cnnj.yuliangmax.workerforce.Task;

public class TestTask extends Task {

    public TestTask(String name) {
        super(name);
    }

    @Override
    protected void onRun() {
        int progress = -1;
        while (progress <= 100 && !checkState()) {
            SystemClock.sleep(50);
            publishProgress(progress);
            progress++;
        }
    }

}
