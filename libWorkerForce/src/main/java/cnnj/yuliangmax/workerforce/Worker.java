package cnnj.yuliangmax.workerforce;

import android.util.Log;

import java.util.UUID;

public class Worker extends Thread {

    private boolean dismissal = false;
    private boolean busy = false;

    private Foreman foreman;
    private Task task;

    //////////////////////////////////////////////////////////////

    public Worker(Foreman foreman) {
        this(foreman, UUID.randomUUID().toString());
    }

    public Worker(Foreman foreman, String name) {
        super(foreman, "Worker-" + name);
        this.foreman = foreman;
    }

    //////////////////////////////////////////////////////////////

    protected boolean isBusy() {
        return busy;
    }

    protected void setBusy(boolean busy) {
        this.busy = busy;
    }

    protected void setDismissal(boolean dismissal) {
        this.dismissal = dismissal;
    }

    protected boolean isDismissal() {
        return dismissal;
    }

    //////////////////////////////////////////////////////////////

    @Override
    public void run() {
        while (!isInterrupted()) {
            Log.i("xx", getId() + " run 循环");
            if (dismissal) {
                Log.i("xx", getName() + " dismissal 取任务前");
                break;
            }
            Task t = foreman.giveMeATask(this);
            if (t == null) {
                Log.d("xx", getId() + "未取到任务，跳出循环，线程执行完毕。");
                break;
            } else {
                Log.d("xx", getId() + "取到任务:" + t.getId() + " " + t.getTaskState());
                bindTask(t);
                startTask();
                while (isBusy()) {
                }
                taskTerminated();
            }
        }
    }

    //////////////////////////////////////////////////////////////

    protected void bindTask(Task task) {
        this.task = task;
        this.task.bindWorker(this);
    }

    private void unbindTask() {
        this.task.unbindWorker();
        this.task = null;
    }

    //////////////////////////////////////////////////////////////

    protected void startTask() {
        setBusy(true);
        if (task.getTaskState() == Task.TaskState.READY || task.getTaskState() == Task.TaskState.TERMINATED) {
            new Thread(task).start();
        } else if (task.getTaskState() == Task.TaskState.PAUSED) {
            task.resumeTask();
        }
    }

    protected void pauseTask() {
        task.pauseTask();
    }

    protected void stopTask() {
        task.stopTask();
    }

    protected void restartTask() {
        task.restartTask();
    }

    protected void taskTerminated() {
        Log.w("xx", task.getId() + " 终止，释放 " + getId());
        unbindTask();
    }

    //////////////////////////////////////////////////////////////

    protected Task getTask() {
        return task;
    }

}
