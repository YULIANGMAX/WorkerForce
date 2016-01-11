package cnnj.yuliangmax.workerforce;

import android.util.Log;

import java.util.UUID;

/**
 * Created by YULIANGMAX.
 */
public abstract class Task implements Runnable {

    public enum TaskState {
        READY,
        RUNNABLE,
        PAUSED,
        TERMINATED
    }

    //////////////////////////////////////////////////////////////

    private UUID id = UUID.randomUUID();
    private String name;
    private TaskState taskState;

    private final Object lock = new Object();

    private Worker worker;

    private boolean waiting = true;
    private boolean pauseFlag = false;
    private boolean stopFlag = false;
    private boolean restartFlag = false;

    //////////////////////////////////////////////////////////////

    public Task() {
        this(null);
    }

    public Task(String name) {
        this(name, TaskState.READY);
    }

    public Task(String name, TaskState taskState) {
        this.id = UUID.randomUUID();
        if (name == null) {
            this.name = "Task-" + id;
        } else {
            this.name = name;
        }
        this.taskState = taskState;
    }

    //////////////////////////////////////////////////////////////////

    final public UUID getId() {
        return id;
    }

    final public String getName() {
        return name;
    }

    final public void setName(String name) {
        this.name = name;
    }

    final public void setRestartFlag(boolean restartFlag) {
        this.restartFlag = restartFlag;
    }

    final public TaskState getTaskState() {
        return taskState;
    }

    //////////////////////////////////////////////////////////////////

    final protected void startTask() {
        if (taskState == TaskState.READY) {
            onStart();
        }
    }

    protected abstract void onRun();

    @Override
    final public void run() {
        Log.e("xx", getId() + " run ");
        restartFlag = true;
        while (restartFlag) {
            pauseFlag = false;
            stopFlag = false;
            restartFlag = false;
            if (taskState != TaskState.READY) {
                onRestart();
            }
            startTask();
            onRun();
            onStop();
        }
        if (worker != null) {
            worker.setBusy(false);
        }
    }

    final protected void pauseTask() {
        if (taskState == TaskState.RUNNABLE) {
            pauseFlag = true;
        }
    }

    final protected void resumeTask() {
        Log.i("xx", "resumeTask stopFlag = " + stopFlag);
        if (taskState == TaskState.PAUSED) {
            pauseFlag = false;
            synchronized (lock) {
                lock.notify();
            }
            if (restartFlag) {
                restartTask();
            }
            if (!stopFlag) {
                onResume();
            }
        }
    }

    final protected void stopTask() {
        if (taskState != TaskState.TERMINATED) {
            stopFlag = true;
            resumeTask();
        }
    }

    final protected void restartTask() {
        restartFlag = true;
        stopTask();
    }

    final protected void waitWorker() {
        onWait();
    }

    //////////////////////////////////////////////////////////////////

    private void onBind() {
        waiting = false;
    }

    protected void onStart() {
        taskState = TaskState.RUNNABLE;
        progress = 0;
        if (listener != null) listener.onStart(this);
    }

    protected void onResume() {
        taskState = TaskState.RUNNABLE;
        if (listener != null) listener.onResume(this);
    }

    protected void onPause() {
        taskState = TaskState.PAUSED;
        if (listener != null) listener.onPause(this);
    }

    protected void onStop() {
        taskState = TaskState.TERMINATED;
        if (progress < max) {
            progress = 0;
            publishProgress(progress);
        }
        if (listener != null) listener.onStop(this);
    }

    protected void onRestart() {
        taskState = TaskState.READY;
        progress = 0;
        if (listener != null) listener.onRestart(this);
    }

    protected void onWait() {
        waiting = true;
        if (listener != null) listener.onWait(this);
    }

    private void onUnBind() {
    }

    //////////////////////////////////////////////////////////////////

    final public long getWorkerId() {
        return worker.getId();
    }

    final public String getWorkerName() {
        return worker.getName();
    }

    //////////////////////////////////////////////////////////////////

    final protected void bindWorker(Worker worker) {
        this.worker = worker;
        onBind();
    }

    final protected void unbindWorker() {
        this.worker = null;
        onUnBind();
    }

    //////////////////////////////////////////////////////////////////

    public boolean isWaiting() {
        return waiting;
    }

    final protected boolean checkState() {
        if (worker != null && pauseFlag) {
            onPause();
            worker.setBusy(false);
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
//                    e.printStackTrace();
                }
            }
        }
        return stopFlag;
    }


    //////////////////////////////////////////////////////////////////

    private TaskListener listener;

    public void setTaskListener(TaskListener listener) {
        this.listener = listener;
    }

    //////////////////////////////////////////////////////////////////

    private int progress = 0;
    private int max = 100;

    protected void publishProgress(int progress) {
        this.progress = progress;
        if (listener != null) listener.onProgress(this, getProgress());
    }

    public int getProgress() {
        return progress;
    }

    protected int toPercent(double x, double total) {
        return (int) (x / total * 100);
    }

}
