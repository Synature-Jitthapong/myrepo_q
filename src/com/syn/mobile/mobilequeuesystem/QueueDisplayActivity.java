package com.syn.mobile.mobilequeuesystem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.QueueDisplayInfo;
import syn.pos.data.model.QueueInfo;

import com.j1tth4.mobile.core.util.MediaManager;
import com.j1tth4.mobile.core.util.DotNetWebServiceTask;
import com.j1tth4.mobile.core.util.MyMediaPlayer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
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
/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
@SuppressLint("NewApi")
public class QueueDisplayActivity extends Activity {
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
	
	private String deviceCode;
	private GlobalVar globalVar;

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
		tvMarquee.setSelected(true);
		
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.US);
		tvDate.setText(dateFormat.format(date));
		
		surface = (SurfaceView) findViewById(R.id.surfaceView1);
		
		MyMediaPlayer mPlayer = 
				new MyMediaPlayer(QueueDisplayActivity.this, surface, 
						globalVar.getVdoPath(), new MyMediaPlayer.MediaPlayerStateListener() {
							
							@Override
							public void onPlayedFileName(String fileName) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void onError(Exception e) {
								// TODO Auto-generated method stub
								
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
				
				tvCalledA.setText("");
				if(queueDisplayInfo.getSzCurQueueGroupA() != "")
				{
					tvCalledA.setText(queueDisplayInfo.getSzCurQueueGroupA());
				}
				
				tvCalledB.setText("");
				if(queueDisplayInfo.getSzCurQueueGroupB() != ""){
					tvCalledB.setText(queueDisplayInfo.getSzCurQueueGroupB());
				}
				
				tvCalledC.setText("");
				if(queueDisplayInfo.getSzCurQueueGroupC() != ""){
					tvCalledC.setText(queueDisplayInfo.getSzCurQueueGroupC());
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
}
