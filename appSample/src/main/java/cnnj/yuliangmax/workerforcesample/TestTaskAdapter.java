package cnnj.yuliangmax.workerforcesample;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import cnnj.yuliangmax.workerforce.Task;
import cnnj.yuliangmax.workerforce.TaskListener;

/**
 * Created by YULIANGMAX.
 */
public class TestTaskAdapter extends BaseAdapter {

    private static MainActivity activity;

    private static class ViewHolder {

        TextView tv_Wait;
        TextView tv_Text;
        ImageView iv_Pause;
        ImageView iv_Stop;
        ImageView iv_Restart;
        ImageView iv_Remove;
        ProgressBar pb_Task;
        TaskListener listener;

        Task task;

        public ViewHolder(View convertView) {
            this.tv_Wait = (TextView) convertView.findViewById(R.id.tv_Wait);
            this.tv_Text = (TextView) convertView.findViewById(R.id.tv_Text);
            this.iv_Pause = (ImageView) convertView.findViewById(R.id.iv_Pause);
            this.iv_Stop = (ImageView) convertView.findViewById(R.id.iv_Stop);
            this.iv_Restart = (ImageView) convertView.findViewById(R.id.iv_Restart);
            this.iv_Remove = (ImageView) convertView.findViewById(R.id.iv_Remove);
            this.pb_Task = (ProgressBar) convertView.findViewById(R.id.pb_Task);
            this.listener = new TaskListener() {
                @Override
                public void onStart(Task task) {
                    Log.i("xx", Thread.currentThread().getId() + " onStart");
                    activity.refreshView();
                }

                @Override
                public void onResume(Task task) {
                    Log.i("xx", Thread.currentThread().getId() + " onResume");
                    activity.refreshView();
                }

                @Override
                public void onPause(Task task) {
                    Log.i("xx", Thread.currentThread().getId() + " onPause");
                    activity.refreshView();
                }

                @Override
                public void onStop(Task task) {
                    Log.i("xx", Thread.currentThread().getId() + " onStop");
                    activity.refreshView();
                }

                @Override
                public void onRestart(Task task) {
                    Log.i("xx", Thread.currentThread().getId() + " onRestart");
                    activity.refreshView();
                }

                @Override
                public void onWait(Task task) {
                    Log.i("xx", Thread.currentThread().getId() + " onWait");
                    activity.refreshView();
                }

                @Override
                public void onProgress(Task task, int progress) {
                    if (activity.mScrollState != AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                        pb_Task.setProgress(progress);
                    }
                }
            };
        }

        public void bindNewTask(TestTask newTask) {
            if (task != null) task.setTaskListener(null);

            this.task = newTask;
            this.tv_Text.setText(task.getName());
            if (task.isWaiting()) {
                this.tv_Wait.setVisibility(View.VISIBLE);
                this.iv_Pause.setVisibility(View.INVISIBLE);
                this.iv_Stop.setVisibility(View.INVISIBLE);
                this.iv_Restart.setVisibility(View.INVISIBLE);
                this.iv_Remove.setVisibility(View.INVISIBLE);
                this.pb_Task.setVisibility(View.INVISIBLE);
            } else {
                this.tv_Wait.setVisibility(View.GONE);
                this.iv_Pause.setVisibility(View.VISIBLE);
                this.iv_Stop.setVisibility(View.VISIBLE);
                this.iv_Restart.setVisibility(View.VISIBLE);
                this.iv_Remove.setVisibility(View.VISIBLE);
                this.pb_Task.setVisibility(View.VISIBLE);
                this.pb_Task.setProgress(task.getProgress());
                switch (task.getTaskState()) {
                    case READY:
                        this.iv_Pause.setEnabled(false);
                        this.iv_Stop.setEnabled(false);
                        this.iv_Restart.setEnabled(false);
                        this.iv_Pause.setImageResource(R.drawable.play);
                        this.iv_Pause.setTag("play");
                        break;
                    case RUNNABLE:
                        this.iv_Pause.setEnabled(true);
                        this.iv_Stop.setEnabled(true);
                        this.iv_Restart.setEnabled(true);
                        this.iv_Pause.setImageResource(R.drawable.pause);
                        this.iv_Pause.setTag("pause");
                        break;
                    case PAUSED:
                        this.iv_Pause.setEnabled(true);
                        this.iv_Stop.setEnabled(true);
                        this.iv_Restart.setEnabled(true);
                        this.iv_Pause.setImageResource(R.drawable.play);
                        this.iv_Pause.setTag("play");
                        break;
                    case TERMINATED:
                        this.iv_Pause.setEnabled(false);
                        this.iv_Stop.setEnabled(false);
                        this.iv_Restart.setEnabled(true);
                        this.iv_Pause.setImageResource(R.drawable.play);
                        this.iv_Pause.setTag("play");
                        break;
                    default:
                        break;
                }
            }
            if (task != null) task.setTaskListener(listener);
        }

    }

    private List<TestTask> items;

    public TestTaskAdapter(MainActivity activity) {
        this.activity = activity;
        items = activity.taskList;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.list_view_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bindNewTask(items.get(position));

        holder.iv_Pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("pause".equals(v.getTag().toString())) {
                    activity.foreman.pause(items.get(position));
                    ((ImageView) v).setImageResource(R.drawable.play);
                    v.setTag("play");
                } else if ("play".equals(v.getTag().toString())) {
                    activity.foreman.start(items.get(position));
                    ((ImageView) v).setImageResource(R.drawable.pause);
                    v.setTag("pause");
                }
                activity.refreshView();
            }
        });

        holder.iv_Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.foreman.stop(items.get(position));
                activity.refreshView();
            }
        });

        holder.iv_Restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.foreman.restart(items.get(position));
                activity.refreshView();
            }
        });

        holder.iv_Remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.foreman.removeTask(items.remove(position));
                activity.refreshView();
            }
        });

        return convertView;
    }

}
