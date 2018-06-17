package jp.techacademy.sugawara.shun.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.techacademy.sugawara.shun.taskapp.Task";

    private Realm mRealm;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadListView("");
        }
    };
    private ListView mListView;
    private TaskAdapter mTaskadapter;
    private EditText mSearchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,InputActivity.class);
                startActivity(intent);
            }
        });

        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);


        mTaskadapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);
        mSearchEditText = (EditText) findViewById(R.id.search_edit_text);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());

                startActivity(intent);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final Task task = (Task) parent.getAdapter().getItem(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        RealmResults<Task> results = mRealm.where(Task.class).equalTo("id",task.getId()).findAll();

                        mRealm.beginTransaction();
                        results.deleteAllFromRealm();
                        mRealm.commitTransaction();

                        Intent resultIntent = new Intent(getApplicationContext(),TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);


                        reloadListView("");
                    }
                });
                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        mSearchEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        reloadListView(mSearchEditText.getText().toString());
                    }
                }
        );

//        addTaskForTest();

        reloadListView("");



    }

    protected void onResume(){
        super.onResume();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);

    }

    private void reloadListView(String searchword){
        //課題用に修正
        RealmResults<Task> taskRealmResults = mRealm.where(Task.class)
                .contains("category",searchword)
                .findAll()
                .sort("date", Sort.ASCENDING);
        mTaskadapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
        mListView.setAdapter(mTaskadapter);
        mTaskadapter.notifyDataSetChanged();

        //lesson ver
//        RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAll().sort("date", Sort.ASCENDING);
//        mTaskadapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
//        mListView.setAdapter(mTaskadapter);
//        mTaskadapter.notifyDataSetChanged();

//      test用
//        List<String> taskList = new ArrayList<String>();
//        taskList.add("aaa");
//        taskList.add("bbb");
//        taskList.add("ccc");
//
//        mTaskadapter.setTaskList(taskList);
//        mListView.setAdapter(mTaskadapter);
//        mTaskadapter.notifyDataSetChanged();

    }




    @Override
    protected void onDestroy(){
        super.onDestroy();
        mRealm.close();
    }

//    private void addTaskForTest(){
//        Task task = new Task();
//        task.setTitle("作業");
//        task.setContents("プログラムを叩いてプッシュする");
//        task.setDate(new Date());
//        task.setId(0);
//        mRealm.beginTransaction();
//        mRealm.copyToRealmOrUpdate(task);
//        mRealm.commitTransaction();
//    }

}
