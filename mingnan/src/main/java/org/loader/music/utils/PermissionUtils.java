package org.loader.music.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by think on 2017/5/25.
 * 6.0动态权限类
 */

public class PermissionUtils {
    private static PermissionUtils instance;

    private Activity activity;

    private static int REQUEST_CODE_PERMISSION = 0x00011;

    private PermissionCallback callback;

    private PermissionUtils(Activity activity) {
        this.activity = activity;
    }

    public static PermissionUtils getInstance(Activity activity) {
        if (instance == null) {
            synchronized (PermissionUtils.class) {
                if (instance == null)
                    instance = new PermissionUtils(activity);
            }
        }
        return instance;
    }

    /**
     * 请求权限
     *
     * @param permissions 请求的权限
     * @param requestCode 请求权限的请求码
     */
    public void requestPermission(String[] permissions, int requestCode, PermissionCallback callback) {
        this.REQUEST_CODE_PERMISSION = requestCode;
        this.callback = callback;
        if (checkPermissions(permissions)) {
            callback.permissionSuccess(REQUEST_CODE_PERMISSION);
        } else {
            List<String> needPermissions = getDeniedPermissions(permissions);
            ActivityCompat.requestPermissions(activity, needPermissions
                    .toArray(new String[needPermissions.size()]), REQUEST_CODE_PERMISSION);
        }


    }


    /**
     * 设置权限回调
     *
     * @param requestCode  请求权限的请求码
     * @param grantResults 请求权限的结果集
     */
    public void setPermissionCallBack(int requestCode, int[] grantResults) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (verifyPermissions(grantResults)) {
                callback.permissionSuccess(REQUEST_CODE_PERMISSION);
            } else {
                callback.permissionFail(REQUEST_CODE_PERMISSION);
                showTipsDialog();
            }
        }
        instance = null;
    }

    /**
     * 检测所有的权限是否都已授权
     *
     * @param permissions
     * @return
     */
    public boolean checkPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     */
    private List<String> getDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) !=
                    PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                needRequestPermissionList.add(permission);
            }
        }
        return needRequestPermissionList;
    }

    /**
     * 确认所有的权限是否都已授权
     *
     * @param grantResults
     * @return
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 显示提示对话框
     */
    private void showTipsDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("权限申请")
                .setMessage("在设置-应用-亿彩乐彩票-权限中开启对应权限，以正常使用亿彩乐彩票功能")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                }).show();
    }

    /**
     * 启动当前应用设置页面
     */
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivity(intent);
    }

    public interface PermissionCallback {
        void permissionSuccess(int requestCode);

        void permissionFail(int requestCode);
    }
}
