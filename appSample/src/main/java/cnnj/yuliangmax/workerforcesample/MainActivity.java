package cnnj.yuliangmax.workerforcesample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cnnj.yuliangmax.workerforce.Foreman;


public class MainActivity extends FragmentActivity {

    Foreman foreman;
    int mScrollState;
    List<TestTask> taskList = new ArrayList<>();

    private Button btn_Task_Add1;
    private Button btn_Task_Add2;
    private Button btn_Task_Add5;
    private Button btn_Task_Add10;

    private Button btn_Worker_Add1;
    private Button btn_Worker_Sub1;

    private Button btn_New;
    private Button btn_Go;
    private Button btn_Clear;
    private Button btn_KnockOff;

    private TextView tv_Message;
    private ListView lv_List;

    private TestTaskAdapter adapter;

    private View.OnClickListener addTaskClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int c = 0;
            switch (v.getId()) {
                case R.id.btn_Task_Add1:
                    c = 1;
                    break;
                case R.id.btn_Task_Add2:
                    c = 2;
                    break;
                case R.id.btn_Task_Add5:
                    c = 5;
                    break;
                case R.id.btn_Task_Add10:
                    c = 10;
                    break;
                default:
                    break;
            }
            for (int i = 0; i < c; i++) {
                TestTask t = new TestTask("Task-" + i);
                taskList.add(t);
                foreman.addTask(t);
            }
            refreshView();
        }
    };

    private View.OnClickListener addWorkerClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int c = foreman.getWorkersNumber();
            switch (v.getId()) {
                case R.id.btn_Worker_Add1:
                    c++;
                    break;
                case R.id.btn_Worker_Sub1:
                    c--;
                    break;
                default:
                    break;
            }
            if (c <= 0) c = 1;
            foreman.setWorkersNumber(c);
            refreshView();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    @Override
    protected void onResume() {
        refreshView();
        super.onResume();
    }

    void refreshView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn_Go.setEnabled(taskList.size() > 0);
                adapter.notifyDataSetChanged();
                tv_Message.setText(String.format(getString(R.string.message),
                                taskList.size(),
                                foreman == null ? 0 : foreman.getNotTerminatedTaskNumber(),
                                foreman == null ? 0 : foreman.getWorkersNumber(),
                                foreman == null ? 0 : foreman.getBusyWorkersNumber(),
                                foreman == null ? 0 : foreman.getIdleWorkersNumber()
                        )
                );
            }
        });
    }

    private void initView() {
        btn_Task_Add1 = (Button) findViewById(R.id.btn_Task_Add1);
        btn_Task_Add2 = (Button) findViewById(R.id.btn_Task_Add2);
        btn_Task_Add5 = (Button) findViewById(R.id.btn_Task_Add5);
        btn_Task_Add10 = (Button) findViewById(R.id.btn_Task_Add10);

        btn_Worker_Add1 = (Button) findViewById(R.id.btn_Worker_Add1);
        btn_Worker_Sub1 = (Button) findViewById(R.id.btn_Worker_Sub1);

        btn_New = (Button) findViewById(R.id.btn_New);
        btn_Go = (Button) findViewById(R.id.btn_Go);
        btn_Clear = (Button) findViewById(R.id.btn_Clear);
        btn_KnockOff = (Button) findViewById(R.id.btn_KnockOff);

        tv_Message = (TextView) findViewById(R.id.tv_Message);

        btn_Task_Add1.setOnClickListener(addTaskClickListener);
        btn_Task_Add2.setOnClickListener(addTaskClickListener);
        btn_Task_Add5.setOnClickListener(addTaskClickListener);
        btn_Task_Add10.setOnClickListener(addTaskClickListener);

        btn_Worker_Add1.setOnClickListener(addWorkerClickListener);
        btn_Worker_Sub1.setOnClickListener(addWorkerClickListener);

        btn_New.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foreman = new Foreman();
                foreman.setAutoStart(false);

                btn_Task_Add1.setEnabled(true);
                btn_Task_Add2.setEnabled(true);
                btn_Task_Add5.setEnabled(true);
                btn_Task_Add10.setEnabled(true);

                btn_Worker_Add1.setEnabled(true);
                btn_Worker_Sub1.setEnabled(true);

                btn_New.setEnabled(false);
                btn_Clear.setEnabled(false);
                btn_KnockOff.setEnabled(true);

                refreshView();
            }
        });

        btn_Go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (taskList.size() > 0) {
                    foreman.gogogo();
                    btn_Clear.setEnabled(true);
                    refreshView();
                }
            }
        });

        btn_Clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foreman.clearTasks();
                taskList.clear();

                btn_Clear.setEnabled(false);

                refreshView();
            }
        });

        btn_KnockOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskList.clear();
                foreman.knockOff();
                foreman = null;

                btn_Task_Add1.setEnabled(false);
                btn_Task_Add2.setEnabled(false);
                btn_Task_Add5.setEnabled(false);
                btn_Task_Add10.setEnabled(false);

                btn_Worker_Add1.setEnabled(false);
                btn_Worker_Sub1.setEnabled(false);

                btn_New.setEnabled(true);
                btn_Clear.setEnabled(false);
                btn_KnockOff.setEnabled(false);

                refreshView();
            }
        });

        lv_List = (ListView) findViewById(R.id.lv_List);
        adapter = new TestTaskAdapter(this);
        lv_List.setAdapter(adapter);
        lv_List.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        btn_Task_Add1.setEnabled(false);
        btn_Task_Add2.setEnabled(false);
        btn_Task_Add5.setEnabled(false);
        btn_Task_Add10.setEnabled(false);

        btn_Worker_Add1.setEnabled(false);
        btn_Worker_Sub1.setEnabled(false);

        btn_New.setEnabled(true);
        btn_Go.setEnabled(false);
        btn_Clear.setEnabled(false);
        btn_KnockOff.setEnabled(false);
    }

}
