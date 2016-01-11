package cnnj.yuliangmax.workerforce;

/**
 * Created by YULIANGMAX.
 */
public interface TaskListener {

    void onStart(Task task);

    void onResume(Task task);

    void onPause(Task task);

    void onStop(Task task);

    void onRestart(Task task);

    void onWait(Task task);

    void onProgress(Task task, int progress);

}
