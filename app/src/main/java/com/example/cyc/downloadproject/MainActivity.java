package com.example.cyc.downloadproject;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;


import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;

import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.example.cyc.downloadproject.Adapter.ExpandableListAdapter;
import com.example.cyc.downloadproject.Adapter.MyFragmentAdapter;
import com.example.cyc.downloadproject.Adapter.TaskAdapter;
import com.example.cyc.downloadproject.Data.AppConstant;
import com.example.cyc.downloadproject.Fragment.DoneFragment;
import com.example.cyc.downloadproject.Interface.DownloadListener;
import com.example.cyc.downloadproject.SQL.DBHelper;
import com.example.cyc.downloadproject.Service.DownloadService;
import com.example.cyc.downloadproject.URL.URLDownload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.jar.Manifest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,ViewPager.OnPageChangeListener{
    private static final int COMPUTESIZE=1;
    private static final int UPDATEVIEW=1;
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expandableListView;
    private List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private TabLayout mTabLayout;
    private ViewPager viewPager;
    private ActionBarDrawerToggle mDrawerToggle;
    private FloatingActionButton floatingActionButton;
    private CharSequence filename;
    private String URLAddress;
    private ArrayList<URLDownload> listDownloaded=new ArrayList<URLDownload>();
    private ArrayList<URLDownload> listDownloading=new ArrayList<URLDownload>();
    private DownloadService.DownloadBinder downloadBinder;
    private List<Fragment> fragments = new ArrayList<Fragment>();
    private MyReceive receiver=null;
    private PagerFragment downloadingFragment;
    private DoneFragment doneFragment;
    private Handler handler1;
    private FragmentManager fragmentManager;
    private MyFragmentAdapter fragmentPagerAdapter;
    private List<String> titles = new ArrayList<String>();
    private DBHelper dbHelper;
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBinder=(DownloadService.DownloadBinder)iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        dbhelperinit();
        initView();
        initViewPager();
        Intent intent=new Intent(this,DownloadService.class);
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        handler1=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case UPDATEVIEW:
                        if (fragmentPagerAdapter.getCurrentFragment().equals(downloadingFragment))
                        {
                            downloadingFragment.adapter.updateViewArray(listDownloading);

                        }else {
                            doneFragment.adapter.updateViewArray(listDownloaded);
                        }
                        break;
                    default:break;
                }
            }
        };


    }

    public void dbhelperinit(){
        dbHelper=new DBHelper(this);
        dbHelper.getWritableDatabase();
    }
    @Override
    protected void onDestroy() {
        unbindService(connection);
        if (receiver!=null){
            unregisterReceiver(receiver);
        }
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver=new MyReceive();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.example.cyc.progress");
        registerReceiver(receiver,filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (receiver==null){
            receiver=new MyReceive();
            IntentFilter filter=new IntentFilter();
            filter.addAction("com.example.cyc.progress");
            MainActivity.this.registerReceiver(receiver,filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void initView() {

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);

   /*     expandableListView = (ExpandableListView) findViewById(R.id.expandableView);
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expandableListView.setAdapter(listAdapter);*/


        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(R.string.app_name);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerToggle.syncState();
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        titles = new ArrayList<String>();
        titles.add("任务");
        titles.add("已下载");

        for (int i = 0; i < titles.size(); i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(i)));
        }
        listDownloading=new ArrayList<URLDownload>();
        fragmentManager = getSupportFragmentManager();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                final AlertDialog alertDialog = builder.create();
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                alertDialog.show();
                View view1 = getLayoutInflater().inflate(R.layout.dialog, null);
                Window window = alertDialog.getWindow();
                window.setBackgroundDrawable(new ColorDrawable());
                WindowManager.LayoutParams lm = window.getAttributes();
                lm.gravity = Gravity.CENTER;
                int width = getWindowManager().getDefaultDisplay().getWidth();
                lm.width = width;
                lm.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(lm);

                window.setContentView(view1);
                final EditText editText = (EditText) window.findViewById(R.id.url_edit);
                final EditText nameEdit = (EditText) window.findViewById(R.id.name_edit);
                editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if (b) {
                            alertDialog.getWindow()
                                    .clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                        }
                    }
                });
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if(charSequence!=null){
                        String s=charSequence.toString();
                           try {
                               String start =s.toString().substring(0,1);
                           }catch (Exception e){
                               e.printStackTrace();
                           }
                        filename=s;
                        nameEdit.setText(filename);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        URLAddress=editText.getText().toString();
                        if (URLAddress.contains("/")){
                        try{
                            filename=URLAddress.substring(URLAddress.lastIndexOf("/"));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        nameEdit.setText(filename);
                        }
                    }
                });

                nameEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
                final Button cancelButton = (Button) window.findViewById(R.id.cancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
                final Button downloadStartBtn= (Button) window.findViewById(R.id.start_download);
                downloadStartBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (downloadBinder==null){
                            return;
                        }
                        final String url ="http://dldir1.qq.com/music/clntupate/QQMusic72282.apk";
                                //"https://wenku.baidu.com/browse/downloadrec?doc_id=df74fb90daef5ef7ba0d3c8f&
                        // http://rpcs.myapp.com/myapp/rcps/d/10000609/com.tencent.qq.music_10000609_170926144928a.apk"
                        // http://dldir1.qq.com/music/clntupate/QQMusic72282.apk;
                         final Handler handler=new Handler(){
                            @Override
                            public void handleMessage(Message msg) {
                                if(msg.what==COMPUTESIZE) {
                                    long filesize=(long)msg.getData().getLong("contentLength");
                                    Toast.makeText(MainActivity.this,"all:"+filesize,Toast.LENGTH_SHORT).show();
                                    long oldTime=System.currentTimeMillis();
                                    start(new URLDownload(url, 1, 0, filesize,oldTime));
                                    Message message=new Message();
                                    message.what=UPDATEVIEW;
                                    handler1.sendMessage(message);
                                }
                            }
                        };
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                try {

                                    OkHttpClient client=new OkHttpClient();
                                    Request request=new Request.Builder().url(url).build();
                                Response response=client.newCall(request).execute();
                                if (response!=null){

                                    long contentL=response.body().contentLength();
                                    Message message=new Message();
                                    Bundle bundle=new Bundle();
                                    bundle.putLong("contentLength",contentL);
                                    message.what = COMPUTESIZE;
                                    message.setData(bundle);
                                    response.body().close();
                                    handler.sendMessage(message);

                                }
                            }catch (IOException e){
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        alertDialog.dismiss();


                    }
                });
                final Button edit=(Button)window.findViewById(R.id.edit);
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        downloadBinder.cancelDownload();
                    }
                });
                break;


        }

    }


    @Override
    public void onPageScrollStateChanged(int state) {
        if (state==2){
            Message message=new Message();
            message.what=UPDATEVIEW;
            handler1.sendMessage(message);
        }
        else if (state==1){
            Message message=new Message();
            message.what=UPDATEVIEW;
            handler1.sendMessage(message);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Message message=new Message();
        message.what=UPDATEVIEW;
        handler1.sendMessage(message);
    }

    private void start(URLDownload newTask){
        listDownloading.add(newTask);
        downloadingFragment.lists.add(newTask);
        downloadingFragment.adapter.notifyDataSetChanged();
        downloadBinder.startDownload(newTask.URLaddress);

    }


    private void initViewPager() {

        for (int i = 0; i < listDownloading.size(); i++) {
            URLDownload task = listDownloading.get(i);
            if (task.done) {
                listDownloading.remove(i);
                listDownloaded.add(task);
            }
        }
        downloadingFragment = PagerFragment.newInstance(listDownloading);
        fragments.add(downloadingFragment);
        doneFragment=DoneFragment.newInstance(listDownloaded);
        fragments.add(doneFragment);

        fragmentPagerAdapter = new MyFragmentAdapter(fragments, titles, fragmentManager, MainActivity.this);
        //给ViewPager设置适配器
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setOnPageChangeListener(this);
        //将TabLayout和ViewPager关联起来。
        mTabLayout.setupWithViewPager(viewPager);
        //给TabLayout设置适配器
        mTabLayout.setTabsFromPagerAdapter(fragmentPagerAdapter);
        fragmentPagerAdapter.setPrimaryItem(viewPager,0,downloadingFragment);
        if (fragmentPagerAdapter.getCurrentFragment().equals(downloadingFragment)) {

           downloadingFragment.getAdapter().setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    URLDownload task = listDownloading.get(position);
                    switch (task.state) {
                        case 1:
                            task.state = 2;
                            long currentTime = System.currentTimeMillis();
                            long downloadTime = +(currentTime - task.oldTime);
                            task.oldTime = currentTime;
                            task.setDownTime(downloadTime);     //为了后面计算速度
                            downloadBinder.pauseDownload();
                            downloadingFragment.notify();
                            downloadingFragment.adapter.notifyItemChanged(position);
                            break;
                        case 2:
                            task.state = 1;
                            long currentTim = System.currentTimeMillis();
                            task.oldTime = currentTim;
                            downloadBinder.startDownload(task.URLaddress);
                            break;
                    }
                }
            });
            Log.d("viewOnclick","fail");
        }

    }

    class MyReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("com.example.cyc.progress")) {
                Bundle bundle = intent.getExtras();
                String url = bundle.getString("Url");
                int progress = bundle.getInt("progress");

                for (int i = 0; i < listDownloading.size(); i++) {
                    URLDownload task = listDownloading.get(i);
                    if (task.URLaddress.equals(url)) {
                        task.downloadLength += progress;
                        long currentTime=System.currentTimeMillis();
                        double speed=task.downloadLength/2/(currentTime-task.oldTime);
                       downloadingFragment.adapter.updateView(url,task.downloadLength,speed);

                        if (task.downloadLength == task.fileSize*2) {
                            String directory = Environment.getExternalStoragePublicDirectory
                                    (Environment.DIRECTORY_DOWNLOADS).getPath();
                            Toast.makeText(context, "download=file", Toast.LENGTH_SHORT).show();

                            String filename = url.substring(url.lastIndexOf("/") + 1);
                            try {
                                BufferedOutputStream outputStream = new BufferedOutputStream(
                                        new FileOutputStream(directory + filename)
                                );
                                Toast.makeText(context, "wenjian整合", Toast.LENGTH_SHORT).show();
                                for (int k = 1; k <= AppConstant.THREAD_NUM; k++) {
                                    File file = new File(directory +  "_" + k+filename );
                                    BufferedInputStream inputStream = new BufferedInputStream(
                                            new FileInputStream(file)
                                    );
                                    int length = -1;
                                    long count = 0;
                                    byte[] bytes = new byte[1024];
                                    while ((length = inputStream.read(bytes)) != -1) {
                                        count += length;
                                        outputStream.write(bytes, 0, length);
                                        if ((count % 4096 == 0)) {
                                            outputStream.flush();
                                        }
                                    }
                                    outputStream.flush();
                                    inputStream.close();
                                    file.delete();
                                }
                                outputStream.flush();
                                outputStream.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            task.state=3;
                            listDownloaded.add(task);
                            listDownloading.remove(task);
                            Message message=new Message();
                            message.what=UPDATEVIEW;
                            handler1.sendMessage(message);
                        }
                    }
                }

            }
        }
    }


}
