package jp.hanatoya.training.runner;

import android.app.Fragment;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.androidquery.AQuery;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;

import jp.hanatoya.training.R;
import jp.hanatoya.training.accel.AccelerometerAdapter;
import jp.hanatoya.training.accel.StepListener;
import jp.hanatoya.training.json.RunnerData;
import jp.hanatoya.training.util.BusProvider;
import jp.hanatoya.training.util.NetworkUtils;
import jp.hanatoya.training.util.PreferenceUtils;
import jp.hanatoya.training.util.UTF8StringRequest;
import jp.hanatoya.training.util.UrlBuilder;
import jp.hanatoya.training.util.VolleyEmptyResponse;
import jp.hanatoya.training.util.VolleyUtils;
import jp.hanatoya.training.util.YesNoDialog;

public class RunnerActivity extends AppCompatActivity{
	public final static int REQUEST_CODE_YES_NO = 23;
	
	private AQuery a;
    private RunnerServer server;    
    private AccelerometerAdapter ad;
	private StepListener stepListener;
	private RunnerData runnerData;
    private String coachIp3;
	private String runnerIp3;
	private String ipFirstDigits;
	private File dump;
//	private boolean waitingForSmoothStop = false;
	private RequestQueue queue = VolleyUtils.getRequestQueue();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		BusProvider.getInstance().register(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_runner);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_runner_wh);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

		runnerIp3 = getIntent().getStringExtra(NetworkUtils.KEY_IP3);
		ipFirstDigits = getIntent().getStringExtra(NetworkUtils.KEY_FIRST_DIGITS);
        a = new AQuery(this);

		getFragmentManager()
		.beginTransaction()
		.add(R.id.container,  FragmentPrepare.newInstance())
		.commit();
        
		stepListener = new StepListener() {

			@Override
			public void onStep(int t, long dt, int counter) {
				BusProvider.getInstance().post(new StepEvent(t, counter, dt));
	            sendToCoach(t, counter, dt);
			}

			@Override
			public void onSensed(int t, float x, float y, float z, double p) {
				BusProvider.getInstance().post(new SenseEvent(x, y, z, p, t));
	            sendToCoach(t, x, y, z, p);
			}

			@Override
			public void onSmoothStopCompleted() {
				if (runnerData.getStopMsCoach() == 0L){runnerData.setStopMsCoach(System.currentTimeMillis());}
				String url = UrlBuilder.getPostRunToCoahUrl(ipFirstDigits, coachIp3, runnerData.getStep(), runnerData.getSpeed(), runnerData.getPitch(), runnerData.getStride());
				StringRequest request = new StringRequest(url, new VolleyEmptyResponse(), null);
				queue.add(request);
				BusProvider.getInstance().post(new StopEvent(runnerData));
			}
		};
    }

    private void sendToCoach(int t, int step, long dt){
    	if (coachIp3 == null)
    		return;

		String url = UrlBuilder.getSendPitchToCoachUrl(ipFirstDigits, coachIp3, t, step, dt);
		UTF8StringRequest request = new UTF8StringRequest(url, new VolleyEmptyResponse(), null);
		queue.add(request);
    }
    
    private void sendToCoach(int t, float x, float y, float z, double p){
    	if (coachIp3 == null)
    		return;

		String url = UrlBuilder.getSendRawToCoachUrl(ipFirstDigits, coachIp3, t, x, y, z, p);
		UTF8StringRequest request = new UTF8StringRequest(url, new VolleyEmptyResponse(), null);
		queue.add(request);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ad != null){
        	ad.forceStop();
        	ad = null;
        }
        if (server != null)
            server.stop();

		BusProvider.getInstance().unregister(this);
    }

	@Override
	public void onBackPressed() {
		Fragment fragmentRun = getFragmentManager().findFragmentByTag(FragmentRun.TAG);
		if (fragmentRun != null){
			YesNoDialog dialog = YesNoDialog.newInstance(getString(R.string.cancel_prompt), getString(R.string.cancel_message));
			dialog.setTargetFragment(fragmentRun, REQUEST_CODE_YES_NO);
			dialog.show(getFragmentManager(), "dialog");
		}else{
			super.onBackPressed();
		}
	}


	@Subscribe
	public void onRunnerRegistrationDone(FragmentPrepare.FinishRegistrationEvent event){
		dump = createFile(System.currentTimeMillis());
		this.runnerData = event.runnerData;
		this.runnerData.setIp3(runnerIp3);

		getFragmentManager()
				.beginTransaction()
				.replace(R.id.container, FragmentRun.newInstance(runnerData), FragmentRun.TAG)
				.commit();

		if (server != null && server.isAlive()){
			server.setDumpPath(dump.getAbsolutePath());
			return;
		}

		server = new RunnerServer( new RunnerServer.WebServerListener() {

			@Override
			public RunnerData getRunnerData() {
				return runnerData;
			}

			@Override
			public void onReady(String coachId, int distance) {
				if (distance == -1){
					return;
				}
				coachIp3 = coachId;
				runnerData.setDistance(distance);
				BusProvider.getInstance().post(new SignalReadyEvent(distance));
			}

			@Override
			public void onStartRun() {
				BusProvider.getInstance().post(new SignalStartEvent());
				SensorManager manager = (SensorManager) getSystemService(SENSOR_SERVICE);
				ad = new AccelerometerAdapter(manager, stepListener, runnerData, dump);
			}

			@Override
			public void onStopRun(String sTime) { // will wait until onSmoothStopCompleted callback
				Long stopTime = null;
				try{
					stopTime = Long.parseLong(sTime);
				}catch(NumberFormatException e){
					stopTime = System.currentTimeMillis();
					Log.e("RunnerActivity", "time string cannot be parsed as long");
				}
				runnerData.setStopMsCoach(stopTime);
				smoothStopRun();
			}

			@Override
			public void onRestart() {
				dump = createFile(System.currentTimeMillis());
				runnerData.restart();

				new Thread(new Runnable() {
					@Override
					public void run() {
						RunnerActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								BusProvider.getInstance().post(new FragmentPrepare.FinishRegistrationEvent(runnerData));
//								getFragmentManager()
//										.beginTransaction()
//										.replace(R.id.container, FragmentRun.newInstance(runnerData), FragmentRun.TAG)
//										.commit();
//								getFragmentManager().executePendingTransactions();
//								BusProvider.getInstance().post(new SignalStartEvent());
							}
						});
					}
				}).start();
			}

			@Override
			public void onTest() {
				if (runnerData != null && runnerData.getStep() != 0){
					runnerData.setError(-1);
					runnerData.setMessage(getString(R.string.error_runner_session_still_up)); //attempt to reconnect to this device whule session is still up will return error
				}
			}

		});
		server.setDumpPath(dump.getAbsolutePath());
		try {
			server.start();
			Log.w("Web Server", "Web server initialized.");
		} catch (IOException ioe) {
			Log.e("Web Server", "Server could not start.");
			Toast.makeText(getApplicationContext(), getString(R.string.server_cannot_start), Toast.LENGTH_LONG).show();
			finish();
		}
	}


    @Subscribe
    public void onOverrideRun(FragmentRun.OverrideEvent event){
        if (ad == null){
            BusProvider.getInstance().post(new SignalStartEvent());
            SensorManager manager = (SensorManager) getSystemService(SENSOR_SERVICE);
            ad = new AccelerometerAdapter(manager, stepListener, runnerData);
        }else{
            smoothStopRun();
            if (runnerData.getStopMsCoach() == 0L){runnerData.setStopMsCoach(System.currentTimeMillis());}
            BusProvider.getInstance().post(new StopEvent(runnerData));
        }
    }

	private void smoothStopRun(){
        if (ad != null){
			ad.smoothStop();
			runnerData.setError(-1);
			runnerData.setMessage(getString(R.string.error_runner_session_still_up)); //attempt to reconnect to this device whole session is still up will return error
			ad = null;
        }		
	}

    @Subscribe
    public void onForceQuit(FragmentRun.ForceQuitEvent event){
        if (ad != null){
            ad.forceStop();
            ad = null;
        }
        finish();
    }

	private File createFile(long tsMillis){
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath() + "/" + getString(R.string.folder_name));
		dir.mkdirs();

		File dump = new File(dir, PreferenceUtils.getPlayerName(RunnerActivity.this) + "_" +  String.valueOf(tsMillis));
		return dump;
	}

	public static class StepEvent{
		public int t, counter;
		public long dt;

		public StepEvent(int t, int counter, long dt) {
			this.t = t;
			this.counter = counter;
			this.dt = dt;
		}
	}

	public static class SenseEvent{
		public float x, y, z;
		double p;
		int t;

		public SenseEvent(float x, float y, float z, double p, int t) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.p = p;
			this.t = t;
		}
	}

	public static class StopEvent{
		public RunnerData runnerData;

		public StopEvent(RunnerData runnerData) {
			this.runnerData = runnerData;
		}
	}

	public static class SignalStartEvent{}
	public static class SignalReadyEvent{
		public int distance;

		public SignalReadyEvent(int distance) {
			this.distance = distance;
		}
	}

}