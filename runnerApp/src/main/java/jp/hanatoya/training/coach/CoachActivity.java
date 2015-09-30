package jp.hanatoya.training.coach;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import jp.hanatoya.training.R;
import jp.hanatoya.training.json.RunnerData;
import jp.hanatoya.training.util.BusProvider;
import jp.hanatoya.training.util.GsonUtils;
import jp.hanatoya.training.util.NetworkUtils;
import jp.hanatoya.training.util.UTF8StringRequest;
import jp.hanatoya.training.util.UrlBuilder;
import jp.hanatoya.training.util.VolleyUtils;
import jp.hanatoya.training.util.YesNoDialog;

public class CoachActivity extends AppCompatActivity {
	public final static int REQUEST_CODE_QUIT = 47;
	public final static int REQUEST_CODE_RESTART = 25;
	public final static String VOLLEY_SINGLE_PING = "VOLLEY_SINGLE_PING";

	private AQuery a;
	private File dump;
	private CoachServer server;
	private IpScan ipScan;
	private String coachIp3, ipFirstDigits;
	private RequestQueue queue = VolleyUtils.getRequestQueue();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BusProvider.getInstance().register(this);
		setContentView(R.layout.activity_coach);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setNavigationIcon(R.drawable.ic_coach_wh);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		coachIp3 = getIntent().getStringExtra(NetworkUtils.KEY_IP3);
		ipFirstDigits = getIntent().getStringExtra(NetworkUtils.KEY_FIRST_DIGITS);

		a = new AQuery(this);
		ipScan = new IpScan(ipFirstDigits);

		getFragmentManager()
		.beginTransaction()		
		.add(R.id.container, FragmentRegister.newInstance())
		.commit();		
	}

	public static class PitchEvent{
		public int t, step, dt;

		public PitchEvent(int t, int step, int dt) {
			this.t = t;
			this.step = step;
			this.dt = dt;
		}
	}

	public static class RawEvent{
		public int t;
		public float x,y,z;
		public double p;

		public RawEvent(int t, float x, float y, float z, double p) {
			this.t = t;
			this.x = x;
			this.y = y;
			this.z = z;
			this.p = p;
		}
	}

	private void startServer(){
		if (server != null && server.isAlive()){
			return;
		}
		server = new CoachServer(new CoachServer.CoachServerListener() {
			
			@Override
			public void onPitch(final int t, final int step, final int dt) {
				BusProvider.getInstance().post(new PitchEvent(t, step, dt));
			}

			@Override
			public void onRaw(final int t, final float x,final float y,final float z,final double p) {
				BusProvider.getInstance().post(new RawEvent(t, x, y, z, p));
			}

			@Override
			public void onPostRun(int step, float speed, float pitch, float stride) {
				BusProvider.getInstance().post(new StopRunRespondedEvent(step, speed, pitch, stride));
				stopServer();
			}
		});
		
		try {
			server.start();
		}catch (IOException e){
			e.printStackTrace();
		}
	}



	@Subscribe
	public void onRunnerConfirmed(final DialogPickRunner.RunnerConfirmedEvent event){
		startServer();
		String url = UrlBuilder.getReadyToRunUrl(ipFirstDigits, event.runnerData.getIp3(), coachIp3, event.runnerData.getDistance());
		UTF8StringRequest request = new UTF8StringRequest(url, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				getFragmentManager()
						.beginTransaction()
						.replace(R.id.container, FragmentStopWatch.newInstance(event.runnerData), FragmentStopWatch.TAG)
						.commit();
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Crouton.makeText(CoachActivity.this, R.string.error_runner_not_found, Style.ALERT).show();
			}
		});
		queue.add(request);
	}

	@Subscribe
	public void onRunStart(FragmentStopWatch.RunStartEvent event) {
		String url = UrlBuilder.getStartRunUrl(ipFirstDigits, event.runnerIp3);
		UTF8StringRequest request = new UTF8StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				BusProvider.getInstance().post(new RunStartRepliedEvent());

			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Crouton.makeText(CoachActivity.this, R.string.cannot_connect_wifi, Style.ALERT).show();
			}

		});
		queue.add(request);
	}

	@Subscribe
	public void onRestartDialogRequested(FragmentStopWatch.RequestRestartDialogEvent event){
		YesNoDialog dialog = YesNoDialog.newInstance(getString(R.string.restart), getString(R.string.cancel_message));
		FragmentStopWatch fragmentStopWatch = (FragmentStopWatch) getFragmentManager().findFragmentByTag(FragmentStopWatch.TAG);
		if (fragmentStopWatch == null){return;}
		dialog.setTargetFragment(fragmentStopWatch, REQUEST_CODE_RESTART);
		dialog.show(getFragmentManager(), "quit");
	}

	@Subscribe
	public void onRestart(FragmentStopWatch.RestartEvent event){
		String url = UrlBuilder.getRestartRunUrl(ipFirstDigits, event.runnerIp3);

		UTF8StringRequest request = new UTF8StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				RunnerData runnerData = GsonUtils.toBean(response, RunnerData.class);
				getFragmentManager().beginTransaction()
						.replace(R.id.container, FragmentRegister.newInstance(runnerData.getIp3(), runnerData.getDistance()))
						.commit();
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Crouton.makeText(CoachActivity.this, R.string.error_runner_not_found, Style.ALERT).show();
			}
		});
		queue.add(request);
	}


	@Subscribe
	public void onRunStop(final FragmentStopWatch.RunStopEvent event){
		String url = UrlBuilder.getStopRunUrl(ipFirstDigits, event.runnerIp3, event.time);
		UTF8StringRequest request = new UTF8StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Crouton.makeText(CoachActivity.this, R.string.cannot_connect_wifi, Style.ALERT).show();
			}
		});
		queue.add(request);
	}

	@Subscribe
	public void onEmailResultRequested(FragmentStopWatch.RequestEmailResultEvent event){
		RunnerData runnerData = event.postRunData;

		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent .setType("text/plain");
		emailIntent .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(dump));
		emailIntent .putExtra(Intent.EXTRA_SUBJECT, runnerData.getRunnerName() + " " + getString(R.string.running_analysis));

		String message = getString(R.string.name) + " : " + runnerData.getRunnerName()+ "\n"
				+ getString(R.string.distance) + " : " + runnerData.getDistance() + getString(R.string.m) + "\n"
				+ getString(R.string.time) +  " : " + event.stopTimeStopWatch + "\n"
				+ getString(R.string.pitch) + " : " + runnerData.getPitch() + getString(R.string.Hz) + "\n"
				+ getString(R.string.stride) + " : " + String.format("%.2f", runnerData.getStride()) + getString(R.string.m) + "\n"
				+ getString(R.string.total_step) + " : " + runnerData.getStep() + getString(R.string.unit_step) + "\n"
				+ getString(R.string.psratio)  + " : " + String.format("%.2f", runnerData.getPitch() / runnerData.getStride()) + getString(R.string.hzperm) + "\n";
		emailIntent.putExtra(Intent.EXTRA_TEXT, message);
		startActivity(Intent.createChooser(emailIntent, getString(R.string.send_with_email)));
	}

	@Subscribe
	public void onFileTransferRequested(FragmentStopWatch.RequestFileTransferEvent event){
		String fileUrl = UrlBuilder.getFileUrl(ipFirstDigits, event.runnerIp3);
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/" + getString(R.string.folder_name));
		dir.mkdirs();
		dump = new File(dir, event.runnerName + "_dl_" + String.valueOf(System.currentTimeMillis()) + ".csv");
		a.download(fileUrl, dump, new AjaxCallback<File>() {

					@Override
					public void callback(String url, File response, AjaxStatus status) {
						if (response == null) {
							Crouton.makeText(CoachActivity.this, R.string.cannot_connect_wifi, Style.ALERT).show();
							return;
						}
					}
				}
		);
	}

	@Subscribe
	public void onForceQuit(FragmentStopWatch.ForceQuitEvent event){
		stopServer();
		finish();
	}

	@Subscribe
	public void onSessionEnded(FragmentStopWatch.EndSessionEvent event){
		YesNoDialog dialog = YesNoDialog.newInstance(getString(R.string.cancel_prompt), getString(R.string.cancel_message));
		Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
		if (fragment == null){return;}
		dialog.setTargetFragment(fragment, REQUEST_CODE_QUIT);
		dialog.show(getFragmentManager(), "quit");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopServer();
        if(isTaskRoot()){
            long triggerSize = 30; 
            long targetSize = 1;     
            AQUtility.cleanCacheAsync(this, triggerSize, targetSize);
        }
		BusProvider.getInstance().unregister(this);
	}

	@Override
	public void onBackPressed() {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
		if (fragment instanceof FragmentStopWatch){
			onSessionEnded(null);
		}else{
			super.onBackPressed();
		}
	}

	private void stopServer(){
		if (server != null)
			server.stop();
	}

	@Subscribe
	public void onScan(IpScan.ScanEvent event){
		if (ipScan !=null)
			ipScan.cancel();
		ipScan.start();
	}

	@Subscribe
	public void onScanCancelled(IpScan.ScanCancelEvent event){
		ipScan.cancel();
	}

	@Subscribe
	public void onPingSingleRunnerRequested(final FragmentRegister.PingRunnerRequestEvent event){
		String url = UrlBuilder.ping(ipFirstDigits, event.runnerIp3);

		UTF8StringRequest request = new UTF8StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				RunnerData runnerData = GsonUtils.toBean(response, RunnerData.class);
				runnerData.setDistance(Integer.parseInt(a.id(R.id.distance).getEditable().toString()));
				BusProvider.getInstance().post(new FragmentRegister.PingRunnerSuccessEvent(runnerData));

				DialogPickRunner dialogPickRunner = DialogPickRunner.newInstance(runnerData);
				dialogPickRunner.show(getFragmentManager(), "dialog");

			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				BusProvider.getInstance().post(new FragmentRegister.PingRunnerFailedEvent(event.runnerIp3));
			}
		});
		request.setRetryPolicy(new DefaultRetryPolicy(500, 0, 0f));
		request.setTag(VOLLEY_SINGLE_PING);
		queue.add(request);
	}

	public static class RunStartRepliedEvent{}
	public static class StopRunRespondedEvent{
		public float speed, pitch, stride;
		int step;


		public StopRunRespondedEvent(int step, float speed, float pitch, float stride) {
			this.speed = speed;
			this.pitch = pitch;
			this.stride = stride;
			this.step = step;
		}
	}

	public static class GotPostRunDataEvent{
		public float speed, pitch, stride;

		public GotPostRunDataEvent(float speed, float pitch, float stride) {
			this.speed = speed;
			this.pitch = pitch;
			this.stride = stride;
		}
	}

}
