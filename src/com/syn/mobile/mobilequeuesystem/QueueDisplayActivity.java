package com.syn.mobile.mobilequeuesystem;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.QueueDisplayInfo;
import syn.pos.data.model.QueueInfo;

import com.j1tth4.mobile.core.util.MediaManager;
import com.j1tth4.mobile.core.util.DotNetWebServiceTask;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
@SuppressLint("NewApi")
public class QueueDisplayActivity extends Activity implements
		OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener,
		OnVideoSizeChangedListener, SurfaceHolder.Callback {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	private static final String TAG = "QueueDisplayVDO";
	private SurfaceView surface;
	private SurfaceHolder holder;
	private MediaPlayer mPlayer;
	private MediaPlayer mSoundPlayer;
	
	private int mVideoWidth;
	private int mVideoHeight;	
	private boolean mIsVideoSizeKnown = false;
	private boolean mIsVideoReadyToBePlayed = false;
	
	private String deviceCode;
	private GlobalVar globalVar;
	private MediaManager vdoManager;
	private ArrayList<HashMap<String, String>> vdoList = 
			new ArrayList<HashMap<String, String>>();
	private List<String> queueSoundList = 
			new ArrayList<String>();
	
	private int currSoundIndex = 0;
	private int currVdoIndex = 0;

	private ListView lvGroupA;
	private ListView lvGroupB;
	private ListView lvGroupC;
	private TextView tvDate;
	private TextView tvCalledA;
	private TextView tvCalledB;
	private TextView tvCalledC;
	private TextView tvSumGroupA;
	private TextView tvSumGroupB;
	private TextView tvSumGroupC;
	private TextView tvMarquee;
	
	private boolean isRun = true;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_queue_display);

		deviceCode = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);
		
		globalVar = new GlobalVar(QueueDisplayActivity.this);
		
		LinearLayout contentView = (LinearLayout) findViewById(R.id.ContentLayout);
		lvGroupA = (ListView) findViewById(R.id.listViewGroupA);
		lvGroupB = (ListView) findViewById(R.id.listViewGroupB);
		lvGroupC = (ListView) findViewById(R.id.listViewGroupC);
		tvDate = (TextView) findViewById(R.id.textViewDate);
		tvCalledA = (TextView) findViewById(R.id.textViewCalledA);
		tvCalledB = (TextView) findViewById(R.id.textViewCalledB);
		tvCalledC = (TextView) findViewById(R.id.textViewCalledC);
		tvSumGroupA = (TextView) findViewById(R.id.textViewSumGroupA);
		tvSumGroupB = (TextView) findViewById(R.id.textViewSumGroupB);
		tvSumGroupC = (TextView) findViewById(R.id.textViewSumGroupC);
		tvMarquee = (TextView) findViewById(R.id.textViewMarquee);
		
		tvMarquee.setAnimation(scrollingText());
		
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.US);
		tvDate.setText(dateFormat.format(date));
		
		surface = (SurfaceView) findViewById(R.id.surfaceView1);
		holder = surface.getHolder();
		holder.addCallback(this);

		mPlayer = new MediaPlayer();
		//mPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

		mPlayer.setScreenOnWhilePlaying(true);
		mPlayer.setOnBufferingUpdateListener(this);
		mPlayer.setOnCompletionListener(this);
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnVideoSizeChangedListener(this);
		//mPlayer.setVolume(0, 0);
		//mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		// read playlist
		readPlaylist();

		mSoundPlayer = new MediaPlayer();
		mSoundPlayer.setOnCompletionListener(new OnCompletionListener(){

			@Override
			public void onCompletion(MediaPlayer mp) {
				mPlayer.setVolume(1, 1);
				if(currSoundIndex < (queueSoundList.size() - 1)){
					currSoundIndex++;
				}else{
					currSoundIndex = 0;
				}
				playSound();
			}
			
		});
		mSoundPlayer.setOnPreparedListener(new OnPreparedListener(){

			@Override
			public void onPrepared(MediaPlayer mp) {
				mPlayer.setVolume(0, 0);
				mSoundPlayer.start();
			}
			
		});
		// update queue
		handler = new Handler();
		handler.post(updateQueueList);
				
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.

							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}

						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});
	}

	// thread call service
	private Runnable updateQueueList = new Runnable() {

		@Override
		public void run() {
			if (isRun) {
				try {
					new GetQueueTask(QueueDisplayActivity.this, globalVar.getShopId(),
							deviceCode,
							"WSiQueue_JSON_GetCurrentAllQueueDisplay")
							.execute(globalVar.getServiceUrl());

					handler.postDelayed(this, globalVar.getRefreshTime());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.itemSetting:
			intent = new Intent(QueueDisplayActivity.this,
					SettingActivity.class);
			QueueDisplayActivity.this.startActivity(intent);
			QueueDisplayActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	private class QueueAdapter extends BaseAdapter {
		protected List<QueueDisplayInfo.QueueInfo> queueList;
		protected LayoutInflater inflater;

		public QueueAdapter(List<QueueDisplayInfo.QueueInfo> queueList) {
			this.queueList = queueList;
			inflater = LayoutInflater.from(QueueDisplayActivity.this);
		}

		@Override
		public int getCount() {
			return queueList.size();
		}

		@Override
		public QueueInfo getItem(int position) {
			return queueList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			QueueDisplayInfo.QueueInfo queue = queueList.get(position);
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.queue_template, null);
				holder = new ViewHolder();
				holder.tv1 = (TextView) convertView
						.findViewById(R.id.textView1);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv1.setText(queue.getSzQueueName());

			return convertView;
		}

		protected class ViewHolder {
			TextView tv1;
		}

	}

	private class GetQueueTask extends DotNetWebServiceTask {

		public GetQueueTask(Context c, int shopId, String deviceCode,
				String method) {
			super(c, method);
			
			property = new PropertyInfo();
			property.setName("iShopID");
			property.setValue(shopId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szDeviceCode");
			property.setValue(deviceCode);
			property.setType(String.class);
			soapRequest.addProperty(property);
			
		}

		@Override
		protected void onPostExecute(String result) {
			GsonDeserialze gdz = new GsonDeserialze();
			try {
				QueueDisplayInfo queueDisplayInfo = gdz
						.deserializeQueueDisplayInfoJSON(result);

				QueueAdapter adapter = new QueueAdapter(filterQueueGroup(
						queueDisplayInfo.xListQueueInfo, 1));
				lvGroupA.setAdapter(adapter);
				
				tvSumGroupA.setText("A=" + String.valueOf(adapter.getCount()));

				adapter = new QueueAdapter(filterQueueGroup(queueDisplayInfo.xListQueueInfo, 2));
				lvGroupB.setAdapter(adapter);
				tvSumGroupB.setText("B=" + String.valueOf(adapter.getCount()));

				adapter = new QueueAdapter(filterQueueGroup(queueDisplayInfo.xListQueueInfo, 3));
				lvGroupC.setAdapter(adapter);
				tvSumGroupC.setText("C=" + String.valueOf(adapter.getCount()));
                
				MediaManager mediaManager = new MediaManager(QueueDisplayActivity.this, "QueueSound");
				
				queueSoundList = new ArrayList<String>();
				tvCalledA.setText("");
				if(queueDisplayInfo.getSzCurQueueGroupA() != "")
				{
					tvCalledA.setText(queueDisplayInfo.getSzCurQueueGroupA());
					queueSoundList.add(mediaManager.getPathFile(queueDisplayInfo.getSzCurQueueGroupA()));
				}
				
				tvCalledB.setText("");
				if(queueDisplayInfo.getSzCurQueueGroupB() != ""){
					tvCalledB.setText(queueDisplayInfo.getSzCurQueueGroupB());
					queueSoundList.add(mediaManager.getPathFile(queueDisplayInfo.getSzCurQueueGroupB()));
				}
				
				tvCalledC.setText("");
				if(queueDisplayInfo.getSzCurQueueGroupC() != ""){
					tvCalledC.setText(queueDisplayInfo.getSzCurQueueGroupC());
					queueSoundList.add(mediaManager.getPathFile(queueDisplayInfo.getSzCurQueueGroupC()));
				}

				//playSound();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

	}

	private List<QueueDisplayInfo.QueueInfo> filterQueueGroup(List<QueueDisplayInfo.QueueInfo> queueList,
			int groupId) {
		List<QueueDisplayInfo.QueueInfo> filterGroupList = new ArrayList<QueueDisplayInfo.QueueInfo>();
		if (queueList.size() > 0) {
			for (QueueDisplayInfo.QueueInfo queue : queueList) {
				if (queue.getiQueueGroupID() == groupId) {
					filterGroupList.add(queue);
				}
			}
		}
		return filterGroupList;
	}

	private void playSound(){
		if(queueSoundList.size() > 0){
			try {
				mSoundPlayer.reset();
				mSoundPlayer.setDataSource(queueSoundList.get(currSoundIndex));
				mSoundPlayer.prepare();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void playVideo() {
		doCleanUp();
		if(vdoList.size() > 0){
			try {
				mPlayer.reset();
				mPlayer.setDataSource(vdoList.get(currVdoIndex).get("vdoPath"));
				//mPlayer.setDataSource(Uri.parse("rtsp://stream1.tv.jai-d.com:1935/edge4/c03").toString());
				mPlayer.setDisplay(holder);
				mPlayer.prepare();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			new AlertDialog.Builder(QueueDisplayActivity.this)
			.setTitle("Error")
			.setMessage("Not found video file.")
			.setNeutralButton("Close", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surfaceChanged called");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated called");
		playVideo();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed called");
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		Log.v(TAG, "onVideoSizeChanged called");
		if (width == 0 || height == 0) {
			Log.e(TAG, "invalid video width(" + width + ") or height(" + height
					+ ")");
			return;
		}
		mIsVideoSizeKnown = true;
		mVideoWidth = width;
		mVideoHeight = height;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(TAG, "onPrepared called");
		mIsVideoReadyToBePlayed = true;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG, "surfaceDestroyed called");
		
		if(currVdoIndex < (vdoList.size() - 1)){
			currVdoIndex++;
			playVideo();
		}else{
			currVdoIndex = 0;
			
			// reload playlist
			readPlaylist();
			
			playVideo();
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.d(TAG, "onBufferingUpdate percent:" + percent);
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
        doCleanUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        doCleanUp();
    }

    private void releaseMediaPlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
	
    private void doCleanUp() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		mIsVideoReadyToBePlayed = false;
		mIsVideoSizeKnown = false;
	}

	private void startVideoPlayback() {
		Log.v(TAG, "startVideoPlayback");
		holder.setFixedSize(mVideoWidth, mVideoHeight);
		holder.setKeepScreenOn(true);
		mPlayer.start();
		
		Toast.makeText(getApplicationContext(), vdoList.get(currVdoIndex).get("vdoTitle"), Toast.LENGTH_SHORT).show();
		tvMarquee.setText(vdoList.get(currVdoIndex).get("vdoTitle"));
		tvMarquee.setSelected(true);
	}
	
	private void readPlaylist(){
		vdoManager = new MediaManager(QueueDisplayActivity.this, globalVar.getVdoPath());
		vdoList = vdoManager.getPlayList();
	}
	
	public Animation scrollingText(){
		LinearLayout view = (LinearLayout) findViewById(R.id.LinearLayoutMarquee);

	    float width = view.getMeasuredWidth();
	    float screenWidth = ((Activity) QueueDisplayActivity.this).getWindowManager().getDefaultDisplay().getWidth();
	    float toXDelta = width - screenWidth;
	    
	    Animation mAnimation = new TranslateAnimation(screenWidth, toXDelta, 0, 0);
	    mAnimation.setDuration(30000); 
	    mAnimation.setRepeatMode(Animation.RESTART);
	    mAnimation.setRepeatCount(Animation.INFINITE);

	    return mAnimation;
	}
}
