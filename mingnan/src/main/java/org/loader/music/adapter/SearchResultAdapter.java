package org.loader.music.adapter;

import java.util.ArrayList;

import org.loader.music.R;
import org.loader.music.application.App;
import org.loader.music.pojo.SearchResult;
import org.loader.music.utils.Constants;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class SearchResultAdapter extends BaseAdapter {
    private ArrayList<SearchResult> mSearchResult;
    private  Activity activity;
    public SearchResultAdapter(ArrayList<SearchResult> searchResult, Activity activity) {
        mSearchResult = searchResult;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return mSearchResult.size();
    }

    @Override
    public Object getItem(int position) {
        return mSearchResult.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(App.sContext, R.layout.search_result_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.tv_search_result_title);
            holder.artist = (TextView) convertView.findViewById(R.id.tv_search_result_artist);
            holder.album = (TextView) convertView.findViewById(R.id.tv_search_result_album);
            holder.list_item = (TextView) convertView.findViewById(R.id.list_item);
            holder.infolayout = convertView.findViewById(R.id.infolayout);
            holder.mAdView = (AdView) convertView.findViewById(R.id.ad_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position > 0 && (position % Constants.SHOWADPS == 0)) {//每7个显示一个
            holder.infolayout.setVisibility(View.GONE);
            holder.mAdView.setVisibility(View.VISIBLE);
            loadBannerAd(holder.mAdView, position);
        } else {
            holder.mAdView.setVisibility(View.GONE);
            holder.infolayout.setVisibility(View.VISIBLE);
            String artist = mSearchResult.get(position).getArtist();
            String album = mSearchResult.get(position).getAlbum();
            holder.list_item.setText((position + 1) + "");
            holder.title.setText(mSearchResult.get(position).getMusicName());

            if (!TextUtils.isEmpty(artist)) holder.artist.setText(artist);
            else holder.artist.setText(activity.getResources().getString(R.string.unkonwn_artist));

            if (!TextUtils.isEmpty(album)) holder.album.setText(album);
            else holder.album.setText(activity.getResources().getString(R.string.unkonwn_special));
        }
        return convertView;
    }

    public void loadBannerAd( AdView mAdView,  int position) {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
            }
        });

        if (!mAdView.isShown()) {
            mAdView.setVisibility(View.VISIBLE);
        }
    }


    static class ViewHolder {
        public TextView title;
        public TextView artist;
        public TextView album, list_item;
        LinearLayout infolayout;
        AdView mAdView;
    }
}
