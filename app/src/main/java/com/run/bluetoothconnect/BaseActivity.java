package com.run.bluetoothconnect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    /** 是否沉浸状态栏 **/
    private boolean isSetStatusBar = true;
    /** ToolBar 标题**/
    private TextView mToolbarTitle;
    /** ToolBar 子标题**/
    private TextView mToolbarSubTitle;
    /** ToolBar **/
    private Toolbar mToolbar;
    /** 是否使用默认toolbar **/
    private boolean isUseDefaultToolbar = true;
    /** 是否使用toolbar返回键 **/
    private boolean isShowBacking = true;
    /** 是否允许全屏 **/
    private boolean mAllowFullScreen = false;
    /** 是否禁止旋转屏幕 **/
    private boolean isAllowScreenRoate = false;
    /** 日志输出标志 **/
    protected final String TAG = this.getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "BaseActivity-->onCreate()");

        //初始化bundle的参数、其他参数设置
        Bundle bundle = getIntent().getExtras();
        initParams(bundle);

        //绑定视图，而不是布局
        View mView = bindView();
        //当前Activity渲染的视图View
        View mContextView;
        if (null == mView) {
            mContextView = LayoutInflater.from(this)
                    .inflate(bindLayout(), null);
        } else
            mContextView = mView;

        //设置全屏
        if (mAllowFullScreen) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        //设置沉浸状态栏
        if (isSetStatusBar) {
            steepStatusBar();
        }

        //设置活动布局
        setContentView(mContextView);

        //设置屏幕旋转
        if (!isAllowScreenRoate) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //设置TOOLBAR相关
        if (isUseDefaultToolbar) {
            mToolbar = findViewById(R.id.toolbar);
            mToolbarTitle = findViewById(R.id.toolbar_title);
            mToolbarSubTitle = findViewById(R.id.toolbar_subtitle);
            if (mToolbar != null) {
                //将Toolbar显示到界面
                setSupportActionBar(mToolbar);
                mToolbar.setPadding(mToolbar.getPaddingLeft(),
                        getStatusBarHeight(),
                        mToolbar.getPaddingRight(),
                        mToolbar.getPaddingBottom());
            }
            if (mToolbarTitle != null) {
                //getTitle()的值是activity的android:lable属性值
                mToolbarTitle.setText(getTitle());
                //设置默认的标题不显示
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }


        //初始化控件
        initView(mContextView);

        //初始化控件
        initData();

        //设置监听
        setListener();

        //开启业务
        doBusiness(this);
    }

    /**
     * [沉浸状态栏]
     */
    @SuppressLint("ObsoleteSdkInt")
    private void steepStatusBar() {
        //沉浸式状态栏，方式一
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            // 透明状态栏
//            getWindow().addFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            // 透明导航栏
//            getWindow().addFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }

        //沉浸式状态栏，方式二
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

        }

    }

    /**
     * 利用反射获取状态栏高度
     * @return
     * 状态栏高度
     */
    public int getStatusBarHeight() {
        int result = 0;
        //获取状态栏高度的资源id
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * [初始化参数]
     *
     * @param params
     * Bundle params
     */
//    public abstract void initParams(Bundle params);
    public void initParams(Bundle params){}

    /**
     * [绑定视图]
     *
     * @return
     * 绑定视图
     */
//    public abstract View bindView();
    public View bindView(){
        return null;
    }

    /**
     * [绑定布局]
     *
     * @return
     * id
     */
    public abstract int bindLayout();

    /**
     * [初始化控件]
     *
     * @param view
     * view
     */
    public abstract void initView(final View view);

    /**
     * [初始化数据]
     *
     *
     */
    public abstract void initData();

    /**
     * [绑定控件]
     *
     * @param resId
     * 资源ID
     * @return
     * 对应view
     */
    protected    <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }

    /**
     * [设置监听]
     */
    public void setListener(){}


    /**
     * [View点击]
     */
    public void widgetClick(View v){}

    @Override
    public void onClick(View v) {
        widgetClick(v);
    }

    /**
     * [业务操作]
     *
     * @param mContext
     * Context
     */
    public abstract void doBusiness(Context mContext);



    /**
     * [页面跳转]
     *
     * @param clz
     * 类名
     */
    public void startActivity(Class<?> clz) {
        startActivity(new Intent(BaseActivity.this,clz));
    }

    /**
     * [携带数据的页面跳转]
     *
     * @param clz
     * 类名
     * @param bundle
     * 数据
     */
    public void startActivity(Class<?> clz, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * [含有Bundle通过Class打开编辑界面]
     *
     * @param cls
     * 类名
     * @param bundle
     * 数据
     * @param requestCode
     * 请求码
     */
    public void startActivityForResult(Class<?> cls, Bundle bundle,
                                       int requestCode) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

    /**
     * 获取头部标题的TextView
     * @return  TextView
     */
    public TextView getToolbarTitle(){
        return mToolbarTitle;
    }

    /**
     * 获取头部子标题的TextView
     * @return TextView
     */
    public TextView getSubTitle(){
        return mToolbarSubTitle;
    }

    /**
     * this Activity of tool bar.
     * 获取头部.
     * @return androidx.appcompat.widget.Toolbar
     */
    public Toolbar getToolbar() {
        return mToolbar;
    }

    /**
     * 版本号小于21的后退按钮图片
     */
    private void showBack(){
        //setNavigationIcon必须在setSupportActionBar(toolbar);方法后面加入
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(null != mToolbar && isShowBacking){
            showBack();
        }
        Log.d(TAG, "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    /**
     * [简化Toast]
     * @param msg
     * string
     */
    protected void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    /**
     * [简化Toast]
     * @param id
     * 资源ID
     */
    protected void showToast(int id){
        Toast.makeText(this,id,Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置toolbar,提供对默认toolbar的代替
     * @param mToolbar
     * toolbar
     */
    public void setmToolbar(Toolbar mToolbar) {
        this.mToolbar = mToolbar;
        if (this.mToolbar != null) {
            //将Toolbar显示到界面
            setSupportActionBar(this.mToolbar);
            this.mToolbar.setPadding(this.mToolbar.getPaddingLeft(),
                    getStatusBarHeight(),
                    this.mToolbar.getPaddingRight(),
                    this.mToolbar.getPaddingBottom());
            if (isShowBacking){
                showBack();
            }
        }
    }

    /**
     * 设置是否显示默认toolbar
     * @param useDefaultToolbar
     * true or false
     */
    public void setUseDefaultToolbar(boolean useDefaultToolbar) {
        isUseDefaultToolbar = useDefaultToolbar;
    }

    /**
     * 设置返回键
     * @param showBacking
     * true or false
     */
    public void setShowBacking(boolean showBacking) {
        isShowBacking = showBacking;
    }

    /**
     * [是否允许全屏]
     *
     * @param allowFullScreen
     * true or false
     */
    public void setAllowFullScreen(boolean allowFullScreen) {
        this.mAllowFullScreen = allowFullScreen;
    }

    /**
     * [是否设置沉浸状态栏]
     *
     * @param isSetStatusBar
     * true or false
     */
    public void setSteepStatusBar(boolean isSetStatusBar) {
        this.isSetStatusBar = isSetStatusBar;
    }

    /**
     * [是否设置沉浸状态栏]
     *
     * @param isSetStatusBar
     * true or false
     */
    public void setShowToolBar(boolean isSetStatusBar) {
        this.isSetStatusBar = isSetStatusBar;
    }

    /**
     * [是否允许屏幕旋转]
     *
     * @param isAllowScreenRoate
     * true or false
     */
    public void setScreenRoate(boolean isAllowScreenRoate) {
        this.isAllowScreenRoate = isAllowScreenRoate;
    }


    //私有化权限接口
    private PermissionListener mPermissionListener;

    /** 权限申请
     * @param permissions
     * 权限
     * @param listener
     * 监听
     */
    protected void requestRunTimePermission(String[] permissions, PermissionListener listener) {


        this.mPermissionListener = listener;

        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if(ContextCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED){
                permissionList.add(permission);
            }
        }
        if(!permissionList.isEmpty()){
            ActivityCompat.requestPermissions(this,permissionList.toArray(new String[permissionList.size()]),1);
        }else{
            listener.onGranted();
        }
    }

    /** 申请结果
     * @param requestCode
     * 请求码
     * @param permissions
     * 权限组
     * @param grantResults
     * 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    List<String> deniedPermissions = new ArrayList<>();
                    List<String> grantedPermissions = new ArrayList<>();
                    for (int i = 0; i < grantResults.length; i++) {
                        int grantResult = grantResults[i];
                        if (grantResult != PackageManager.PERMISSION_GRANTED){
                            String permission = permissions[i];
                            deniedPermissions.add(permission);
                        }else{
                            String permission = permissions[i];
                            grantedPermissions.add(permission);
                        }
                    }

                    if (deniedPermissions.isEmpty()){
                        mPermissionListener.onGranted();
                    }else{
                        mPermissionListener.onDenied(deniedPermissions);
                        mPermissionListener.onGranted(grantedPermissions);
                    }
                }
                break;
        }
    }


    //权限接口
    public interface PermissionListener {
        //授权成功
        void onGranted();
        // 授权部分
        void onGranted(List<String> grantedPermission);
        // 拒绝授权
        void onDenied(List<String> deniedPermission);
    }
}