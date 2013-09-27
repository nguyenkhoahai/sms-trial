package com.vsm.radio18;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.FrameLayout;

import com.vsm.radio18.ProgressUpdater.IProgressListener;
import com.vsm.radio18.data.ReqListImages;
import com.vsm.radio18.media_player.PlayerService;
import com.vsm.radio18.media_player.PlayerService.IPlayerListener;
import com.vsm.radio18.media_player.PlayerService.PlayerServiceBinder;
import com.vsm.radio18.ui.Topbar;

import dtd.phs.lib.data_framework.IDataListener;
import dtd.phs.lib.data_framework.IRequest;
import dtd.phs.lib.data_framework.RequestWorker;
import dtd.phs.lib.utils.Helpers;
import dtd.phs.lib.utils.Logger;

public class ActDetails extends BaseActivity {

	private static final int FRAME_LOADING = 0;
	private static final int FRAME_DATA = 1;
	private static final int FRAME_RETRY = 2;
	private Topbar topbar;
	private FrameLayout mainFrames;
	private DetailsBottomBar bottomBar;
	protected PlayerService playerService;
	private ProgressUpdater progressUpdater;
	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_details);
		initTopbar();
		initSlideshowFrames();
		initBottombar();
	}

	private void initBottombar() {
		bottomBar = new DetailsBottomBar(findViewById(R.id.details_bottom));
		bottomBar.onCreate();
	}

	private void initSlideshowFrames() {
		mainFrames = (FrameLayout) findViewById(R.id.main_frames);
		Helpers.showOnlyView(mainFrames, FRAME_LOADING);
	}

	private void initTopbar() {
		topbar = new Topbar(findViewById(R.id.top_bar));
		topbar.onCreate();
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean succ = bindService(new Intent(this, PlayerService.class),
				playerServiceConenction, Context.BIND_AUTO_CREATE);
		if (!succ) {
			Helpers.showToast(this, R.string.Error_pls_retry_later);
			finish();
		}
		startProgressUpdater();
		bottomBar.onResume();
	}

	@Override
	protected void onPause() {
		// TODO: release images
		releaseImages();
		bottomBar.onPause();
		unbindService(playerServiceConenction);
		stopProgressUpdater();
		super.onPause();
	};

	
	private void releaseImages() {
		Logger.logError("TODO: Release image previewer");
	}

	private void startLoadingImages(long articleId) {
		IDataListener listener = new IDataListener() {
			
			@Override
			public void onError(Exception e) {
				Logger.logError(e);
				Helpers.showOnlyView(mainFrames, FRAME_RETRY);
			}
			
			@Override
			public void onCompleted(Object data) {
				ArrayList<String> urls = (ArrayList<String>) data;
				if ( urls != null ) {
					for(int i = 0 ; i < urls.size() ; i++) {
						Logger.logInfo("ImageURL: " + urls.get(i));
					}
				} else {
					onError(new RuntimeException("Null data returned"));
				}
			}
		};
		IRequest request = new ReqListImages(articleId);
		RequestWorker.addRequest(request, listener, handler);
	}
	
	private void startProgressUpdater() {
		IProgressListener progressListener = new IProgressListener() {
			
			@Override
			public void onProgressChanged(int progress, final int currentTime, final int duration) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						bottomBar.onTimeChanged(currentTime, duration);
					}
				});
				
			}
		};
		progressUpdater = new ProgressUpdater(progressListener);
		progressUpdater.start();
	}



	private void stopProgressUpdater() {
		if (progressUpdater != null) {
			progressUpdater.stopUpdate();
		}
	}

	ServiceConnection playerServiceConenction = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bottomBar.removePlayerService();
			progressUpdater.removePlayerService();
			playerService = null;
			Logger.logError("Weird, this method should never be called");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			PlayerServiceBinder binder = (PlayerServiceBinder) service;
			playerService = binder.getService();
			playerService.addListener(playerServiceListener);
			bottomBar.setPlayerService(playerService);
			if (playerService.isASongSelected()) {
				bottomBar.setSongName(playerService.getCurrentItem().getName());
				startLoadingImages(playerService.getCurrentItem().getId());
			} else {
				Helpers.showToast(ActDetails.this,
						R.string.Error_pls_retry_later);
				finish();
				return;
			}
			if ( progressUpdater != null ) {
				progressUpdater.setPlayerService(playerService);
			}

		}
	};

	IPlayerListener playerServiceListener = new IPlayerListener() {
		@Override
		public void onPlayerStateChanged(PlayerServiceStates state, Object data) {
			switch (state) {
			case UNINIT:
			case SONG_SELECTED:
			case PLAYING:
			case PAUSED:
			case STOP_N_WAIT:
			case DEAD:
			default:
				break;
			}
		}
	};

}
