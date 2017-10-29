package org.loader.music.service;

import org.loader.music.R;
import org.loader.music.engine.Download;
import org.loader.music.utils.L;
import org.loader.music.utils.MusicUtils;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.SparseArray;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class DownloadService extends Service {
    private SparseArray<Download> mDownloads = new SparseArray<Download>();
    private RemoteViews mRemoteViews;

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void download(final int id, String url, final String name) {
        L.l("download", url);
        try{
            if (  url!=null){
                Download d = new Download(id, url, MusicUtils.getMusicDir() + name);
                d.setOnDownloadListener(mDownloadListener).start(false);
                mDownloads.put(id, d);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void refreshRemoteView() {
        @SuppressWarnings("deprecation")
        Notification notification = new Notification(
                android.R.drawable.stat_sys_download, "",
                System.currentTimeMillis());
        mRemoteViews = new RemoteViews(getPackageName(),
                R.layout.download_remote_layout);
        notification.contentView = mRemoteViews;

        StringBuilder builder = new StringBuilder();
        for (int i = 0, size = mDownloads.size(); i < size; i++) {
            builder.append(mDownloads.get(mDownloads.keyAt(i))
                    .getLocalFileName());
            builder.append("、");
        }

        mRemoteViews.setTextViewText(R.id.tv_download_name,
                builder.substring(0, builder.lastIndexOf("、")));

        startForeground(R.drawable.ic_launcher, notification);
    }

    private void onDownloadComplete(int downloadId) {
        mDownloads.remove(downloadId);
        if (mDownloads.size() == 0) {
            stopForeground(true);
            return;
        }

        refreshRemoteView();
    }

    /**
     * 发送广播，通知系统扫描指定的文件
     * 请参考我的博文：
     * http://blog.csdn.net/u010156024/article/details/47681851
     */
    private void scanSDCard() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 判断SDK版本是不是4.4或者高于4.4
            String[] paths = new String[]{
                    Environment.getExternalStorageDirectory().toString()};
            MediaScannerConnection.scanFile(this, paths, null, null);
        } else {
            Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
            intent.setClassName("com.android.providers.media",
                    "com.android.providers.media.MediaScannerReceiver");
            intent.setData(Uri.parse("file://" + MusicUtils.getMusicDir()));
            sendBroadcast(intent);
        }
        MusicUtils.initMusicList();
    }

    private Download.OnDownloadListener mDownloadListener =
            new Download.OnDownloadListener() {

                @Override
                public void onSuccess(int downloadId) {
                    try{
                        L.l("download", "success");
                        Toast.makeText(DownloadService.this,
                                mDownloads.get(downloadId).getLocalFileName() + DownloadService.this.getString(R.string.downloadcomlete),
                                Toast.LENGTH_SHORT).show();
                        onDownloadComplete(downloadId);
                        int downtimes=getSpInt("download");
                        downtimes++;
                        putIntData("download",downtimes);
                        if (downtimes==7){
                            Intent intent = new Intent();
                            intent.setAction("com.showDialog");
                            sendBroadcast(intent);
                        }
                        scanSDCard();
                    }catch (Exception  e){
                        e.printStackTrace();
                    }

                }

                public int getSpInt(String key) {
                    SharedPreferences sp = DownloadService.this.getApplication().getSharedPreferences("config",
                            Context.MODE_PRIVATE);
                    return sp.getInt(key, 0);
                }

                public void putIntData(String key, int value) {
                    SharedPreferences sp = DownloadService.this.getApplication().getSharedPreferences("config",
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt(key, value);
                    editor.apply();
                }

                @Override
                public void onStart(int downloadId, long fileSize) {
                    L.l("download", "start");
                    refreshRemoteView();
                    Toast.makeText(DownloadService.this, DownloadService.this.getString(R.string.start_download) +
                                    mDownloads.get(downloadId).getLocalFileName(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPublish(int downloadId, long size) {
//			L.l("download", "publish" + size);
                }

                @Override
                public void onPause(int downloadId) {
                    L.l("download", "pause");
                }

                @Override
                public void onGoon(int downloadId, long localSize) {
                    L.l("download", "goon");
                }

                @Override
                public void onError(int downloadId) {
                    L.l("download", "error");
                    Toast.makeText(DownloadService.this,
                            mDownloads.get(downloadId).getLocalFileName() + DownloadService.this.getString(R.string.downloadfail),
                            Toast.LENGTH_SHORT).show();
                    onDownloadComplete(downloadId);
                }

                @Override
                public void onCancel(int downloadId) {
                    L.l("download", "cancel");
//			Toast.makeText(DownloadService.this,
//					mDownloads.get(downloadId).getLocalFileName() + "本地已经有该歌曲",
//					Toast.LENGTH_SHORT).show();
                    onDownloadComplete(downloadId);
                }
            };
}
