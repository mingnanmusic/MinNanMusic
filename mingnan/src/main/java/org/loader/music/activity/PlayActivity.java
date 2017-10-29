package org.loader.music.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.loader.music.R;
import org.loader.music.application.App;
import org.loader.music.fragment.NetSearchFragment;
import org.loader.music.pojo.Music;
import org.loader.music.service.MusicIntentReceiver;
import org.loader.music.service.PlayService;
import org.loader.music.ui.CDView;
import org.loader.music.ui.LrcView;
import org.loader.music.ui.PagerIndicator;
import org.loader.music.utils.ImageTools;
import org.loader.music.utils.MusicIconLoader;
import org.loader.music.utils.MusicUtils;
import org.loader.music.utils.PlayBgShape;
import org.loader.music.utils.PlayPageTransformer;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.ringdroid.RingdroidEditActivity;

/**
 * 2015年8月15日 16:34:37
 *  博文地址：http://blog.csdn.net/u010156024
 */
public class PlayActivity extends BaseActivity implements OnClickListener {
	private Bitmap bgBitmap=null;
	private LinearLayout mPlayContainer;
	private ImageView mPlayBackImageView; // back button
	private TextView mMusicTitle; // music title
	private ViewPager mViewPager; // cd or lrc
	private CDView mCdView; // cd
	private SeekBar mPlaySeekBar; // seekbar
	private ImageButton mStartPlayButton; // start or pause
	private TextView mSingerTextView; // singer
	private LrcView mLrcViewOnFirstPage; // single line lrc
	private LrcView mLrcViewOnSecondPage; // 7 lines lrc
	private PagerIndicator mPagerIndicator; // indicator
	private  ImageView  more_imageView;
	private AdView mAdView;
	// cd view and lrc view
	private ArrayList<View> mViewPagerContent = new ArrayList<View>(2);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		View view = getLayoutInflater().inflate(R.layout.play_activity_layout,null);
		setContentView(view);
		setupViews();
		loadBannerAd(view);
		//注册广播
		intentReceiver  = new PlayMusicIntentReceiver();
		intentFilter  = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
		registerReceiver(intentReceiver,intentFilter);
		registMediaKey(PlayMusicIntentReceiver.class.getName());
		showPopAd();
	}
	private  PlayMusicIntentReceiver intentReceiver;
	private  IntentFilter intentFilter;
	@Override
	protected void onDestroy() {
		//取消广播
		if (intentReceiver != null) {
			unregisterReceiver(intentReceiver);
		}
		if (mRemoteControlClientReceiverComponent!=null  && audioManager!=null){
			audioManager.unregisterMediaButtonEventReceiver(mRemoteControlClientReceiverComponent);
		}
//		showPopAd();
		super.onDestroy();

	}
	@Override
	public void onResume() {
		super.onResume();
		allowBindService();

	}

	@Override
	public void onPause() {
		allowUnbindService();
		super.onPause();
	}
	/**
	 * 初始化view
	 */
	private void setupViews() {
		more_imageView    = findViewById(R.id.more_imageView);
		mPlayContainer = (LinearLayout) findViewById(R.id.ll_play_container);
		mPlayBackImageView = (ImageView) findViewById(R.id.iv_play_back);
		mMusicTitle = (TextView) findViewById(R.id.tv_music_title);
		mViewPager = (ViewPager) findViewById(R.id.vp_play_container);
		mPlaySeekBar = (SeekBar) findViewById(R.id.sb_play_progress);
		mStartPlayButton = (ImageButton) findViewById(R.id.ib_play_start);
		mPagerIndicator = (PagerIndicator) findViewById(R.id.pi_play_indicator);

		// 动态设置seekbar的margin
		MarginLayoutParams p = (MarginLayoutParams) mPlaySeekBar
				.getLayoutParams();
		p.leftMargin = (int) (App.sScreenWidth * 0.1);
		p.rightMargin = (int) (App.sScreenWidth * 0.1);

		mPlaySeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		setDefaultBG();
		initViewPagerContent();
		// 设置viewpager的切换动画
		mViewPager.setPageTransformer(true, new PlayPageTransformer());
		mPagerIndicator.create(mViewPagerContent.size());
		mViewPager.setOnPageChangeListener(mPageChangeListener);
		mViewPager.setAdapter(mPagerAdapter);
		more_imageView.setOnClickListener(this);
		mPlayBackImageView.setOnClickListener(this);



	}


	private OnPageChangeListener mPageChangeListener =
			new OnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			try{
				if (position == 0) {
					if (mPlayService.isPlaying())
						mCdView.start();
				} else {
					mCdView.pause();
				}
				mPagerIndicator.current(position);
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};

	/**
	 * 拖动进度条
	 */
	private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener =
			new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int progress = seekBar.getProgress();
			mPlayService.seek(progress);
			mLrcViewOnFirstPage.onDrag(progress);
			mLrcViewOnSecondPage.onDrag(progress);
		}
	};

	private PagerAdapter mPagerAdapter = new PagerAdapter() {
		@Override
		public int getCount() {
			return mViewPagerContent.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		/**
		 * 该方法是PagerAdapter的预加载方法，系统调用 当显示第一个界面时，
		 * 第二个界面已经预加载，此时调用的就是该方法。
		 */
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mViewPagerContent.get(position));
			return mViewPagerContent.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}
	};

	/**
	 * 初始化viewpager的内容
	 */
	private void initViewPagerContent() {
		View cd = View.inflate(this, R.layout.play_pager_item_1, null);
		mCdView = (CDView) cd.findViewById(R.id.play_cdview);
		mSingerTextView = (TextView) cd.findViewById(R.id.play_singer);
		mLrcViewOnFirstPage = (LrcView) cd.findViewById(R.id.play_first_lrc);

		View lrcView = View.inflate(this, R.layout.play_pager_item_2, null);
		mLrcViewOnSecondPage = (LrcView) lrcView
				.findViewById(R.id.play_first_lrc_2);

		mViewPagerContent.add(cd);
		mViewPagerContent.add(lrcView);
	}

	@SuppressWarnings("deprecation")
	private void setBackground(int position) {
		if(position==0){
			return ;
		}

		if(MusicUtils.sMusicList.size()!=0){
			Music currentMusic = MusicUtils.sMusicList.get(position);
			bgBitmap = MusicIconLoader.getInstance().load(
					currentMusic.getImage());
		}
		setDefaultBG();
	}
	private void setDefaultBG(){
		if (bgBitmap == null) {
			bgBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.trans);
		}
		mPlayContainer.setBackgroundDrawable(
				new ShapeDrawable(new PlayBgShape(bgBitmap)));
	}

	/**
	 * 上一曲
	 * 
	 * @param view
	 */
	public void pre(View view) {
		mPlayService.pre(); // 上一曲
	}

	/**
	 * 播放 or 暂停
	 * 
	 * @param view
	 */
	public void play(View view) {
		if (MusicUtils.sMusicList.isEmpty()) {
		//	Toast.makeText(PlayActivity.this, "当前手机没有MP3文件", Toast.LENGTH_LONG).show();
			return ;
		}
		if (mPlayService.isPlaying()) {
			mPlayService.pause(); // 暂停
			mCdView.pause();
			mStartPlayButton
					.setImageResource(R.drawable.player_btn_play_normal);
		} else {
			onPlay(mPlayService.resume()); // 播放
		}
	}

	/**
	 * 上一曲
	 * 
	 * @param view
	 */
	public void next(View view) {
		mPlayService.next(); // 上一曲
	}

	private  String currentFilename;
	private  int currentPosition;
	/**
	 * 播放时调用 主要设置显示当前播放音乐的信息
	 * 
	 * @param position
	 */
	private void onPlay(int position) {
		this.currentPosition  = position;
		Bitmap bmp=null;
		if(!MusicUtils.sMusicList.isEmpty() &&mPlayService!=null&&mPlayService.getPlayer()!=null){
			MusicUtils.sMusicList.get(position).setLength(mPlayService.getPlayer().getDuration());
			Music music = MusicUtils.sMusicList.get(position);
			currentFilename   =  music.getUri();
			mMusicTitle.setText(music.getTitle());
			mSingerTextView.setText(music.getArtist());
			mPlaySeekBar.setMax(music.getLength());
			bmp = MusicIconLoader.getInstance().load(music.getImage());
		}
		if (bmp == null)
			bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_launcher);
		mCdView.setImage(ImageTools.scaleBitmap(bmp,
				(int) (App.sScreenWidth * 0.8)));

		if (mPlayService.isPlaying()) {
			mCdView.start();
			mStartPlayButton
					.setImageResource(R.drawable.player_btn_pause_normal);
		} else {
			mCdView.pause();
			mStartPlayButton
					.setImageResource(R.drawable.player_btn_play_normal);
		}
	}

	private void setLrc(int position) {
		if(MusicUtils.sMusicList.size()!=0){
			Music music = MusicUtils.sMusicList.get(position);
			String lrcPath = MusicUtils.getLrcDir()  +music.getTitle()+ ".lrc";
			Log.i("setLrc: ",lrcPath);
			mLrcViewOnFirstPage.setLrcPath(lrcPath);
			mLrcViewOnSecondPage.setLrcPath(lrcPath);
		}
	}

	@Override
	public void onPublish(int progress) {
		mPlaySeekBar.setProgress(progress);
		if (mLrcViewOnFirstPage.hasLrc())
			mLrcViewOnFirstPage.changeCurrent(progress);
		if (mLrcViewOnSecondPage.hasLrc())
			mLrcViewOnSecondPage.changeCurrent(progress);
	}

	@Override
	public void onChange(int position) {
		setBackground(position);
		onPlay(position);
		setLrc(position);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		judgeBack();

	}
	public void judgeBack(){
		if(isExsitMianActivity(MainActivity.class)){//存在这个类
			//进行操作
			finish();
		}else{//不存在这个类
			//进行操作
			startActivity(new Intent(PlayActivity.this,MainActivity.class));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_play_back:
			//判断当前栈只有当前这个activity
			judgeBack();

			break;
			case  R.id.more_imageView:

				showDialog();
				break;
		default:
			break;
		}
	}
	private   void showDialog(){
		AlertDialog  alertDialog  = new AlertDialog.Builder(PlayActivity.this)
				.setItems(getResources().getStringArray(R.array.playdialog),
						new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
					if (i==0){
						makeRing();
					}else if (i==1){//好评
						Uri uri = Uri.parse("market://details?id=" + PlayActivity.this.getPackageName());
						Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
						try {
							startActivity(goToMarket);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(PlayActivity.this, "Couldn't launch the market !", Toast.LENGTH_SHORT).show();
						}
					}else if (i==2){//分享app
						Intent textIntent = new Intent(Intent.ACTION_SEND);
						textIntent.setType("text/plain");
						textIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_message)+"《"+getAppName(PlayActivity.this)+"》");
						startActivity(Intent.createChooser(textIntent, getResources().getString(R.string.share)));
					}else if (i==3){//更多app
						Intent viewIntent = new Intent("android.intent.action.VIEW",
								Uri.parse("http://market.android.com/search?q=pub:飘落的叶子"));
						startActivity(viewIntent);
					}
			}
		}).create();
		alertDialog.show();
	}

	public static String getAppName(Context context)
	{
		try
		{
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			int labelRes = packageInfo.applicationInfo.labelRes;
			return context.getResources().getString(labelRes);
		} catch (PackageManager.NameNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	private   void makeRing(){
		if (!TextUtils.isEmpty(currentFilename)){
			Intent intent = new Intent(Intent.ACTION_EDIT,
					Uri.parse(currentFilename));
			intent.setClass(PlayActivity.this,RingdroidEditActivity.class);
			startActivity(intent);
			//暂停播放
			Intent intent1 = new Intent(PlayService.class.getSimpleName());
			intent1.putExtra("BUTTON_NOTI",2);
			sendBroadcast(intent1);
			onPlay(currentPosition);
		}

	}

	/**
	 * 判断某一个类是否存在任务栈里面
	 * @return
	 */
	private boolean isExsitMianActivity(Class<?> cls){
		Intent intent = new Intent(this, cls);
		ComponentName cmpName = intent.resolveActivity(getPackageManager());
		boolean flag = false;
		if (cmpName != null) { // 说明系统中存在这个activity
			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(10);
			for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
				if (taskInfo.baseActivity.equals(cmpName)) { // 说明它已经启动了
					flag = true;
					break;  //跳出循环，优化效率
				}
			}
		}
		return flag;
	}






	/**
	 * Receives broadcasted intents. In particular, we are interested in the
	 * android.media.AUDIO_BECOMING_NOISY and android.intent.action.MEDIA_BUTTON intents, which is
	 * broadcast, for example, when the user disconnects the headphones. This class works because we are
	 * declaring it in a &lt;receiver&gt; tag in AndroidManifest.xml.
	 */
	public class PlayMusicIntentReceiver extends BroadcastReceiver {
		public static final String ACTION_TOGGLE_PLAYBACK =
				"com.example.android.musicplayer.action.TOGGLE_PLAYBACK";
		public static final String ACTION_PLAY = "com.example.android.musicplayer.action.PLAY";
		public static final String ACTION_PAUSE = "com.example.android.musicplayer.action.PAUSE";
		public static final String ACTION_STOP = "com.example.android.musicplayer.action.STOP";
		public static final String ACTION_SKIP = "com.example.android.musicplayer.action.SKIP";
		public static final String ACTION_REWIND = "com.example.android.musicplayer.action.REWIND";
		public static final String ACTION_URL = "com.example.android.musicplayer.action.URL";
		@Override
		public void onReceive(Context context, Intent intent) {
			Intent sendIntent = new Intent(PlayService.class.getSimpleName());
			if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
				Toast.makeText(context, "Headphones disconnected.", Toast.LENGTH_SHORT).show();

				// send an intent to our MusicService to telling it to pause the audio
				context.startService(new Intent(ACTION_PAUSE));

			} else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
				KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
				if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
					return;

				switch (keyEvent.getKeyCode()) {
					case KeyEvent.KEYCODE_HEADSETHOOK:
					case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
						onChange(currentPosition);
						//  context.startService(new Intent(ACTION_TOGGLE_PLAYBACK));
						break;
					case KeyEvent.KEYCODE_MEDIA_PLAY:
//						sendIntent.putExtra("BUTTON_NOTI", 2);
						//   context.startService(new Intent(ACTION_PLAY));
						onChange(currentPosition);
						break;
					case KeyEvent.KEYCODE_MEDIA_PAUSE:
//						sendIntent.putExtra("BUTTON_NOTI", 2);
						onChange(currentPosition);
						//  context.startService(new Intent(ACTION_PAUSE));
						break;
					case KeyEvent.KEYCODE_MEDIA_STOP:  //退出
						//   context.startService(new Intent(ACTION_STOP));
//						sendIntent.putExtra("BUTTON_NOTI", 4);

						break;
					case KeyEvent.KEYCODE_MEDIA_NEXT://下一个
//						sendIntent.putExtra("BUTTON_NOTI", 3);
						if ((currentPosition+1)<MusicUtils.sMusicList.size()){
							onChange(currentPosition+1);
						}
						// context.startService(new Intent(ACTION_SKIP));
						break;
					case KeyEvent.KEYCODE_MEDIA_PREVIOUS:  // 上一个
						// TODO: ensure that doing this in rapid succession actually plays the
						// previous song
						//   context.startService(new Intent(ACTION_REWIND));
//						sendIntent.putExtra("BUTTON_NOTI", 1);
						if ((currentPosition-1)>=0){
							onChange(currentPosition-1);
						}
						break;
				}

			}
			//context.sendBroadcast(sendIntent);
		}
	}

}
