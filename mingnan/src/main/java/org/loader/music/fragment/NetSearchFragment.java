package org.loader.music.fragment;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.loader.music.R;
import org.loader.music.activity.MainActivity;
import org.loader.music.adapter.SearchResultAdapter;
import org.loader.music.engine.GetDownloadInfo;
import org.loader.music.engine.SongsRecommendation;
import org.loader.music.engine.GetDownloadInfo.OnDownloadGettedListener;
import org.loader.music.engine.SearchMusic;
import org.loader.music.pojo.DatabaseHelper;
import org.loader.music.pojo.Music;
import org.loader.music.pojo.SearchResult;
import org.loader.music.utils.Constants;
import org.loader.music.utils.MobileUtils;
import org.loader.music.utils.MusicUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class NetSearchFragment extends BaseFragment
        implements OnClickListener {
    protected static final String TAG = NetSearchFragment.class.getSimpleName();

    private MainActivity mActivity;

    private LinearLayout mSearchShowLinearLayout;
    private LinearLayout mSearchLinearLayout;
    private ImageButton mSearchButton;
    private EditText mSearchEditText;
    private ListView mSearchResultListView;
    private ProgressBar mSearchProgressBar;
    private TextView mFooterView;
    private View mPopView;

    private PopupWindow mPopupWindow;

    private SearchResultAdapter mSearchResultAdapter;
    public static ArrayList<SearchResult> mResultData = new ArrayList<SearchResult>();

    private int mPage = 0;
    private int mLastItem;
    private boolean hasMoreData = true;
    /**
     * 该类是android系统中的下载工具类，非常好用
     */
    private DownloadManager mDownloadManager;

    private boolean isFirstShown = true;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.search_music_layout, null);
        setupViews(layout);
        loadBannerAd(layout);
        mDownloadManager = (DownloadManager) mActivity
                .getSystemService(Context.DOWNLOAD_SERVICE);
        return layout;
    }

    private List<SearchResult> getFromDB() {
        DatabaseHelper helper = DatabaseHelper.getHelper(mActivity);
        List<SearchResult> list = null;
        try {
            list = helper.getSearchResultDao().queryForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 该方法实现的功能是： 当该Fragment不可见时，isVisibleToUser=false
     * 当该Fragment可见时，isVisibleToUser=true
     * 该方法由系统调用，重写该方法实现用户可见当前Fragment时再进行数据的加载
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // 当Fragment可见且是第一次加载时
        if (isVisibleToUser && isFirstShown) {
            mSearchProgressBar.setVisibility(View.VISIBLE);
            mSearchResultListView.setVisibility(View.GONE);
            List<SearchResult> list = getFromDB();
            if (list != null && list.size() > 0) {
                Log.i(TAG, "setUserVisibleHint: 本地缓存");
                mSearchProgressBar.setVisibility(View.GONE);
                mSearchResultListView
                        .setVisibility(View.VISIBLE);
                mResultData.clear();
                List<SearchResult> rslist = addBanner(list);
                mResultData.addAll(rslist);
                mSearchResultAdapter.notifyDataSetChanged();
            } else {
                SongsRecommendation
                        .getInstance()
                        .setListener(
                                new SongsRecommendation.OnRecommendationListener() {
                                    @Override
                                    public void onRecommend(
                                            ArrayList<SearchResult> results) {
                                        if (results == null || results.isEmpty())
                                            return;
                                        mSearchProgressBar.setVisibility(View.GONE);
                                        mSearchResultListView
                                                .setVisibility(View.VISIBLE);

                                        mResultData.clear();
                                        List<SearchResult> rslist = addBanner(results);
                                        mResultData.addAll(rslist);
                                        mSearchResultAdapter.notifyDataSetChanged();
                                        //把歌曲加入播放列表
                                    }
                                }).get(mActivity);
            }

            isFirstShown = false;
        }
    }

    private List<SearchResult> addBanner(List<SearchResult> results) {
        int showcount = results.size() / Constants.SHOWADPS;
        for (int i = 1; i < showcount; i++) {
            SearchResult music = new SearchResult();
            //添加空的
            results.add(i * Constants.SHOWADPS, music);
        }
        return results;
    }

    private void setupViews(View layout) {
        mSearchShowLinearLayout = (LinearLayout) layout
                .findViewById(R.id.ll_search_btn_container);
        mSearchLinearLayout = (LinearLayout) layout
                .findViewById(R.id.ll_search_container);
        mSearchButton = (ImageButton) layout.findViewById(R.id.ib_search_btn);
        mSearchEditText = (EditText) layout
                .findViewById(R.id.et_search_content);
        mSearchResultListView = (ListView) layout
                .findViewById(R.id.lv_search_result);
        mSearchProgressBar = (ProgressBar) layout
                .findViewById(R.id.pb_search_wait);
        mFooterView = buildFooterView();

        mSearchShowLinearLayout.setOnClickListener(this);
        mSearchButton.setOnClickListener(this);

        mSearchResultListView.addFooterView(mFooterView);

        mSearchResultAdapter = new SearchResultAdapter(mResultData,mActivity);
        mSearchResultListView.setAdapter(mSearchResultAdapter);
        mSearchResultListView.setOnScrollListener(mListViewScrollListener);
        mSearchResultListView.setOnItemClickListener(mResultItemClickListener);
    }

    private TextView buildFooterView() {
        TextView footerView = new TextView(mActivity);
        footerView.setText("加载下一页...");
        footerView.setGravity(Gravity.CENTER);
        footerView.setVisibility(View.GONE);
        return footerView;
    }

    /**
     * 列表中每一列的点击时间监听器
     */
    private OnItemClickListener mResultItemClickListener
            = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if (position >= mResultData.size() || position < 0)
                return;
            showDownloadDialog(position);
        }
    };

    /**
     * 底部对话框
     *
     * @param position
     */
    private void showDownloadDialog(final int position) {
        mActivity.onPopupWindowShown();

        if (mPopupWindow == null) {
            mPopView = View.inflate(mActivity, R.layout.download_pop_layout,
                    null);

            mPopupWindow = new PopupWindow(mPopView, LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(
                    Color.TRANSPARENT));
            mPopupWindow.setAnimationStyle(R.style.popwin_anim);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss() {
                    mActivity.onPopupWindowDismiss();
                }
            });
        }
        //下载按钮点击时间
        mPopView.findViewById(R.id.tv_pop_download).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = mResultData.get(position).getUrl();

                        GetDownloadInfo
                                .getInstance()
                                .setListener(mDownloadUrlListener)
                                .parse(position,
                                        url);
                        dismissDialog();
                    }
                });
        mPopView.findViewById(R.id.tv_pop_cancel).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissDialog();
                    }
                });
        /**
         * 设置对话框展示的位置
         */
        mPopupWindow.showAtLocation(mActivity.getWindow().getDecorView(),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void dismissDialog() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    private OnDownloadGettedListener mDownloadUrlListener =
            new OnDownloadGettedListener() {
                @Override
                public void onMusic(int position, String url) {
                    if (position == -1 || url == null) {
                        Toast.makeText(mActivity, "歌曲链接失效",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String musicName = mResultData.get(position).getMusicName();
                    mActivity.getDownloadService().download(position,
                            mResultData.get(position).getUrl(), musicName + ".mp3");


                }

                @Override
                public void onLrc(int position, String url) {
                    if (url == null)
                        return;

                    String musicName = mResultData.get(position).getMusicName();
                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(url));
                    Log.i(TAG, "NetSearhconLrc: " + url);
                    request.setVisibleInDownloadsUi(false);
                    request.setNotificationVisibility(Request.VISIBILITY_HIDDEN);
                    // request.setShowRunningNotification(false);
                    request.setDestinationUri(Uri.fromFile(new File(MusicUtils
                            .getLrcDir() + musicName + ".lrc")));
                    mDownloadManager.enqueue(request);
                }
            };

    private OnScrollListener mListViewScrollListener =
            new OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (mLastItem == mSearchResultAdapter.getCount() && hasMoreData
                            && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        String searchText = mSearchEditText.getText().toString().trim();
                        if (TextUtils.isEmpty(searchText))
                            return;

                        mFooterView.setVisibility(View.VISIBLE);
                        startSearch(searchText);
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    // 计算可见列表的最后一条的列表是不是最后一个
                    mLastItem = firstVisibleItem + visibleItemCount;
                }
            };

    private void search() {
        MobileUtils.hideInputMethod(mSearchEditText);
        String content = mSearchEditText.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(mActivity, "请输入关键词", Toast.LENGTH_SHORT).show();
            return;
        }
        mPage = 0;
        mSearchProgressBar.setVisibility(View.VISIBLE);
        mSearchResultListView.setVisibility(View.GONE);
        startSearch(content);
    }

    private void startSearch(String content) {
        SearchMusic.getInstance()
                .setListener(new SearchMusic.OnSearchResultListener() {
                    @Override
                    public void onSearchResult(ArrayList<SearchResult> results) {
                        if (mPage == 1) {
                            hasMoreData = true;
                            mSearchProgressBar.setVisibility(View.GONE);
                            mSearchResultListView.setVisibility(View.VISIBLE);
                        }
                        mFooterView.setVisibility(View.GONE);
                        if (results == null || results.isEmpty()) {
                            hasMoreData = false;
                            return;
                        }
                        if (mPage == 1)
                            mResultData.clear();
                        mResultData.addAll(results);
                        mSearchResultAdapter.notifyDataSetChanged();
                    }
                }).search(content, ++mPage);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_search_btn_container:
                mActivity.hideIndicator();
                mSearchShowLinearLayout.setVisibility(View.GONE);
                mSearchLinearLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.ib_search_btn:
                mActivity.showIndicator();
                mSearchShowLinearLayout.setVisibility(View.VISIBLE);
                mSearchLinearLayout.setVisibility(View.GONE);
                search();
                break;
        }
    }
}
