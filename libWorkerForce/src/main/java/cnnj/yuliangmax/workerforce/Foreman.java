package cnnj.yuliangmax.workerforce;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Foreman extends ThreadGroup {

    private boolean autoStart = false;

    private boolean workFlag = false;

    private List<Task> tasks = new LinkedList<>();
    private List<Worker> workers = new ArrayList<>();

    //////////////////////////////////////////////////////////////

    public Foreman() {
        this(false);
    }

    public Foreman(boolean autoStart) {
        this(1);
        this.autoStart = autoStart;
        this.workFlag = autoStart;
    }

    public Foreman(int n) {
        super(UUID.randomUUID().toString());
        setWorkersNumber(n);
    }

    //////////////////////////////////////////////////////////////

    public synchronized boolean isAutoStart() {
        return autoStart;
    }

    public synchronized void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
        this.workFlag = autoStart;
    }

    //////////////////////////////////////////////////////////////

    public synchronized void addTask(Task t) {
        tasks.add(t);
        if (isAutoStart() || workFlag) {
            notify();
        }
    }

    protected synchronized Task giveMeATask(Worker w) {
        Log.d("xx", w.getId() + "取任务");
        while (autoStart ? tasks.size() == 0 : (!workFlag || tasks.size() == 0)) {
            try {
                Log.e("xx", w.getId() + "没任务，等着。");
                wait();
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
        }
        if (w.isDismissal()) {
            Log.i("xx", w.getId() + " 已解雇，不给任务。");
            notify();
            return null;
        }
        return tasks.remove(0);
    }

    public synchronized void removeTask(Task t) {
        stop(t);
        tasks.remove(t);
    }

    public synchronized void clearTasks() {
        for (int i = tasks.size() - 1; i >= 0; i--) {
            tasks.remove(i);
        }
        for (Worker w : workers) {
            if (w.isBusy()) w.stopTask();
        }
        workFlag = false;
    }

    private synchronized void dismissalAllWorkers() {
        for (int i = workers.size() - 1; i >= 0; i--) {
            workers.remove(i).setDismissal(true);
        }
        notifyAll();
    }

    //////////////////////////////////////////////////////////////

    public synchronized void gogogo() {
        if (tasks.size() > 0) {
            workFlag = true;
            notifyAll();
        }
    }

    public synchronized void knockOff() {
        clearTasks();
        dismissalAllWorkers();
        interrupt();
    }

    //////////////////////////////////////////////////////////////

    private synchronized List<Worker> generateWorkers(int n) {
        List<Worker> workers = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            Worker w = new Worker(this);
            workers.add(w);
            w.start();
        }
        return workers;
    }

    public synchronized void setWorkersNumber(int n) {
        if (n > workers.size()) {
            int s = n - workers.size();
            workers.addAll(generateWorkers(s));
        } else if (n < workers.size()) {
            int s = workers.size() - n;
            for (int i = s; i >= 1; i--) {
                Worker w = workers.remove(i);
                w.setDismissal(true);
                Log.d("xx", w.getId() + "被解雇");
            }
        }
    }

    public synchronized int getNotTerminatedTaskNumber() {
        return tasks.size();
    }

    public synchronized int getWorkersNumber() {
        return workers.size();
    }

    public synchronized int getIdleWorkersNumber() {
        return workers.size() - getBusyWorkersNumber();
    }

    public synchronized int getBusyWorkersNumber() {
        int c = 0;
        for (Worker w : workers) {
            if (w.isBusy()) {
                c++;
            }
        }
        return c;
    }

    //////////////////////////////////////////////////////////////

    public synchronized void start(Task t) {
        if (t.getTaskState() != Task.TaskState.RUNNABLE) {
            //确保后恢复运行的任务加在其他待恢复任务的后面。
            int index = 0;
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getTaskState() != Task.TaskState.PAUSED) {
                    break;
                } else {
                    index = i + 1;
                }
            }
            tasks.add(index, t);
            t.waitWorker();
            notify();
        }
    }

    public synchronized void pause(Task t) {
        Worker w = findWorkerByTask(t);
        if (w != null) {
            w.pauseTask();
        }
    }

    public synchronized void stop(final Task t) {
        Worker w = findWorkerByTask(t);
        if (w != null) {
            w.stopTask();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    t.stopTask();
                }
            }).start();
        }
    }

    public synchronized void restart(Task t) {
        Worker w = findWorkerByTask(t);
        if (w != null) {
            w.restartTask();
        } else {
            t.setRestartFlag(true);
            tasks.add(0, t);
            t.waitWorker();
            notify();
        }
    }

    //////////////////////////////////////////////////////////////

    private Worker findWorkerByTask(Task t) {
        if (t == null) {
            return null;
        }
        for (Worker w : workers) {
            if (t.equals(w.getTask())) {
                return w;
            }
        }
        return null;
    }

}
