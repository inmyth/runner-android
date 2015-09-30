package jp.hanatoya.training.coach;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.androidquery.AQuery;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.squareup.otto.Subscribe;

import java.util.Locale;

import jp.hanatoya.training.R;
import jp.hanatoya.training.json.RunnerData;
import jp.hanatoya.training.runner.AlternatingColorStyle;
import jp.hanatoya.training.runner.FragmentRun;
import jp.hanatoya.training.util.BusProvider;
import jp.hanatoya.training.util.GsonUtils;
import jp.hanatoya.training.util.RunnerMath;

public class FragmentStopWatch extends Fragment {
	public static final String TAG = "FragmentStopWatch";

	public static final String RUNNER_DATA = "RUNNER_DATA";
	private AQuery a;
	private long startTime = 0L;
	private Handler myHandler = new Handler();
	private long timeInMillies = 0L;
	private long timeSwap = 0L;
	private long finalTime = 0L;
	private RunnerData runnerData;
	private GraphViewSeries xS, yS, zS, pS, pitchSeries;

	public static FragmentStopWatch newInstance(RunnerData runnerData) {
		FragmentStopWatch fragmentStopWatch = new FragmentStopWatch();
		Bundle args = new Bundle();
		args.putString(RUNNER_DATA, GsonUtils.toJson(runnerData));
		fragmentStopWatch.setArguments(args);
		return fragmentStopWatch;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		BusProvider.getInstance().register(this);
		View view = inflater.inflate(R.layout.fragment_coach_sw, container, false);
		a = new AQuery(view);
		runnerData = GsonUtils.toBean(getArguments().getString(RUNNER_DATA), RunnerData.class);
		a.id(R.id.name).text(getString(R.string.name_n, runnerData.getRunnerName()));
		a.id(R.id.distance).text(getString(R.string.distance_n, runnerData.getDistance()));
		a.id(R.id.id).text(getString(R.string.bracket_id, runnerData.getIp3()));
		a.id(android.R.id.button1).clicked(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (startTime == 0L) {
					BusProvider.getInstance().post(new RunStartEvent(runnerData.getIp3()));
				} else {
					timeSwap += timeInMillies;
					myHandler.removeCallbacks(updateTimerMethod);
					a.id(android.R.id.button1).clicked(null);
					BusProvider.getInstance().post(new RunStopEvent(runnerData.getIp3(), finalTime));
				}
			}
		});
		a.id(R.id.end).clicked(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BusProvider.getInstance().post(new EndSessionEvent());
			}
		});
		a.id(R.id.send).clicked(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BusProvider.getInstance().post(new RequestEmailResultEvent(runnerData, getStopWatchTime(finalTime)));
			}
		});
		a.id(R.id.restart).clicked(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BusProvider.getInstance().post(new RequestRestartDialogEvent());
			}
		});
		a.id(R.id.title1).text(Html.fromHtml("加速度ログ<br />加速度(m/s<sup>2</sup>) x 時間(s)"));
		xS = new GraphViewSeries("X", new GraphViewSeriesStyle(Color.BLUE, 1), FragmentRun.startPad);
		yS = new GraphViewSeries("Y", new GraphViewSeriesStyle(Color.GREEN, 1), FragmentRun.startPad);
		zS = new GraphViewSeries("Z", new GraphViewSeriesStyle(Color.WHITE, 1), FragmentRun.startPad);
		pS = new GraphViewSeries("P", new GraphViewSeriesStyle(Color.RED, 3), FragmentRun.startPad);
		FragmentRun.initMainGraph(a, getActivity(), xS, yS, zS, pS);
		a.id(R.id.title2).text("ピッチ ログ\nピッチ(Hz) x 時間(s)");
		GraphViewSeriesStyle pitchStyle = new GraphViewSeriesStyle();
		pitchStyle.setValueDependentColor(new AlternatingColorStyle());
		pitchSeries = new GraphViewSeries("", pitchStyle, FragmentRun.startPad2);
		FragmentRun.initPitchGraph(a, getActivity(), pitchSeries, getResources().getColor(android.R.color.white));
		return view;
	}

	private void showItemsStart() {
		a.id(R.id.title1).visible();
		a.id(R.id.graw).visible();
		a.id(R.id.title2).visible();
		a.id(R.id.gpitch).visible();
		a.id(R.id.border1).visible();
		a.id(R.id.border2).visible();
	}

	private void showItemFinish() {
		a.id(android.R.id.button1).gone();
		a.id(R.id.stats).visible();
		a.id(R.id.border3).visible();
		a.id(R.id.send).visible();
		a.id(R.id.end).visible();
		a.id(R.id.restart).visible();
	}

	private void startStopWatch() {
		a.id(android.R.id.button1).text(getString(R.string.stop)).background(R.drawable.btn_circle_orange);
		startTime = SystemClock.uptimeMillis();
		myHandler.postDelayed(updateTimerMethod, 0);
	}

	@Subscribe
	public void onStartRunResponded (CoachActivity.RunStartRepliedEvent event){
		startStopWatch();
		showItemsStart();
	}

	@Subscribe
	public void onStopRunResponded(CoachActivity.StopRunRespondedEvent event){
		BusProvider.getInstance().post(new RequestFileTransferEvent(runnerData.getIp3(), runnerData.getRunnerName()));
		showItemFinish();
		runnerData.setSpeed(event.speed);
		runnerData.setPitch(event.pitch);
		runnerData.setStride(event.stride);
		runnerData.setStep(event.step);
		a.id(R.id.pitch).text(String.format(Locale.JAPAN, "%.2f", event.pitch));
		a.id(R.id.stride).text(String.format(Locale.JAPAN, "%.2f",event.stride));
		a.id(R.id.time).text(getStopWatchTime(finalTime));
		a.id(R.id.speed).text(String.format(Locale.JAPAN, "%.2f", event.speed));
		a.id(R.id.n_step).text(String.valueOf(event.step));
		a.id(R.id.ps).text((String.format(Locale.JAPAN, "%.2f", RunnerMath.pitchPerStride(event.pitch, event.stride))));

	}

	@Subscribe
	public void onPitch(CoachActivity.PitchEvent event){
		try {
			pitchSeries.appendData(new GraphViewData((float) event.t / 1000, (float) 1000 / event.dt), true, 100);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Subscribe
	public void onRaw(CoachActivity.RawEvent event){
		double tf = event.t / (double) 1000.0d;
		try {
			xS.appendData(new GraphViewData(tf, event.x), true, 1000);
			yS.appendData(new GraphViewData(tf, event.y), true, 1000);
			zS.appendData(new GraphViewData(tf,event. z), true, 1000);
			// p is sum of all squares so we need to root square it to scale with the rest
			pS.appendData(new GraphViewData(tf, Math.sqrt(event.p)), true, 1000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CoachActivity.REQUEST_CODE_QUIT && resultCode == Activity.RESULT_OK){
			BusProvider.getInstance().post(new ForceQuitEvent());
		}else if (requestCode == CoachActivity.REQUEST_CODE_RESTART && resultCode == Activity.RESULT_OK ){
			BusProvider.getInstance().post(new RestartEvent(runnerData.getIp3()));
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		BusProvider.getInstance().unregister(this);
	}

	private Runnable updateTimerMethod = new Runnable() {
		public void run() {
			timeInMillies = SystemClock.uptimeMillis() - startTime;
			finalTime = timeSwap + timeInMillies;
			a.id(R.id.timer).text(getStopWatchTime(finalTime));
			myHandler.postDelayed(this, 0);
		}
	};

	public static String getStopWatchTime(long time) {
		int seconds = (int) (time / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;
		int milliseconds = (int) ((time % 1000) / 10);
		return "" + minutes + ":" + String.format("%02d", seconds) + ":" + String.format("%02d", milliseconds);
	}

	public static class RunStartEvent {
		public String runnerIp3;

		public RunStartEvent(String runnerIp3) {
			this.runnerIp3 = runnerIp3;
		}
	}

	public static class RunStopEvent{
		public String runnerIp3;
		public long time;

		public RunStopEvent(String runnerIp3, long time) {
			this.runnerIp3 = runnerIp3;
			this.time = time;
		}
	}

	public static class ForceQuitEvent{}

	public static class RequestEmailResultEvent{
		public RunnerData postRunData;
		public String  stopTimeStopWatch;

		public RequestEmailResultEvent(RunnerData postRunData, String stopTimeStopWatch) {
			this.postRunData = postRunData;
			this.stopTimeStopWatch = stopTimeStopWatch;
		}
	}
	public static class EndSessionEvent{}
	public static class RestartEvent{
		public String runnerIp3;

		public RestartEvent(String runnerIp3) {
			this.runnerIp3 = runnerIp3;
		}
	}

	public static class RequestRestartDialogEvent{}

	public static class RequestFileTransferEvent{
		public String runnerIp3;
		public String runnerName;

		public RequestFileTransferEvent(String runnerIp3, String runnerName) {
			this.runnerIp3 = runnerIp3;
			this.runnerName = runnerName;
		}
	}

}