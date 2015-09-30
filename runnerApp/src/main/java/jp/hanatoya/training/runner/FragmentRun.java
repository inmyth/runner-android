package jp.hanatoya.training.runner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.androidquery.AQuery;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;
import com.squareup.otto.Subscribe;

import java.util.Locale;

import jp.hanatoya.training.R;
import jp.hanatoya.training.coach.FragmentStopWatch;
import jp.hanatoya.training.json.RunnerData;
import jp.hanatoya.training.util.BusProvider;
import jp.hanatoya.training.util.GsonUtils;
import jp.hanatoya.training.util.RunnerMath;

public class FragmentRun extends Fragment {
	public static final String TAG = "FragmentRun";

	private final static String LOAD_RUNNER_DATA = "LOAD_RUNNER_DATA";
	private AQuery a;
	private ViewFlipper flip;

	private RunnerData runnerData;

	private GraphViewSeries xS, yS, zS, pS, pitchSeries;
	public static final GraphViewDataInterface[] startPad = new GraphViewData[] { new GraphViewData(-10.0d, 0.0d), new GraphViewData(-8.2d, 0.0d), new GraphViewData(-5.0d, 0.0d), new GraphViewData(-2.0d, 0.0d), new GraphViewData(-1.0d, 0.0d) };
	public static final GraphViewDataInterface[] startPad2 = new GraphViewData[] { };

	public static FragmentRun newInstance(RunnerData runnerData) {
		FragmentRun fragmentRun = new FragmentRun();
		Bundle bundle = new Bundle();
		bundle.putString(LOAD_RUNNER_DATA, GsonUtils.toJson(runnerData));
		fragmentRun.setArguments(bundle);
		return fragmentRun;
	}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runnerData = GsonUtils.toBean(getArguments().getString(LOAD_RUNNER_DATA), RunnerData.class);
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		BusProvider.getInstance().register(this);

		View view = inflater.inflate(R.layout.fragment_runner_run, container, false);
		a = new AQuery(view);
		flip = (ViewFlipper) a.id(R.id.flip).getView();
		a.id(R.id.name).text(getString(R.string.name_n, runnerData.getRunnerName()));
		a.id(R.id.distance).text(getString(R.string.distance_n, runnerData.getDistance()));
		a.id(R.id.title1).text(Html.fromHtml("加速度ログ<br />加速度(m/s<sup>2</sup>) x 時間(s)"));
		xS = new GraphViewSeries("X", new GraphViewSeriesStyle(Color.BLUE, 1), startPad);
		yS = new GraphViewSeries("Y", new GraphViewSeriesStyle(Color.GREEN, 1), startPad);
		zS = new GraphViewSeries("Z", new GraphViewSeriesStyle(Color.WHITE, 1), startPad);
		pS = new GraphViewSeries("P", new GraphViewSeriesStyle(Color.RED, 3), startPad);
		initMainGraph(a, getActivity(), xS, yS, zS, pS);
		a.id(R.id.title2).text("ピッチ ログ\nピッチ(Hz) x 時間(s)");
		GraphViewSeriesStyle pitchStyle = new GraphViewSeriesStyle();
		pitchStyle.setValueDependentColor(new AlternatingColorStyle());
		pitchSeries = new GraphViewSeries("", pitchStyle, startPad2);
		initPitchGraph(a, getActivity(), pitchSeries, getResources().getColor(android.R.color.white));
		a.id(R.id.id).text(getString(R.string.bracket_id, runnerData.getIp3()));
		a.id(R.id.id_big).text(getString(R.string.id_n, runnerData.getIp3()));
		a.id(R.id.override).clicked(new OnClickListener() {
			@Override
			public void onClick(View v) {
                BusProvider.getInstance().post(new OverrideEvent());
			}
		});
		a.id(R.id.stats_title).text(R.string.title_stats_start);
		a.id(R.id.n_step).text("0");
		a.id(R.id.pitch).text("0.0");
		a.id(R.id.stride).text("0.0");
		a.id(R.id.time).text(getString(R.string.stopwatch_zeros));
		a.id(R.id.speed).text("0.0");
		a.id(R.id.ps).text("0");
		return view;
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		BusProvider.getInstance().unregister(this);
	}

	public static void initPitchGraph(final AQuery a, Context ctx, GraphViewSeries pitchSeries, int resolvedColorLabel) {
		BarGraphView gvPitch = new BarGraphView(ctx, "") {
			@SuppressLint("DefaultLocale")
			protected String formatLabel(double value, boolean arg1) {
				return String.format("%.2f", value);
			};
		};
		GraphViewStyle gvrStyle = new GraphViewStyle();
		gvrStyle.setVerticalLabelsColor(resolvedColorLabel);
		gvrStyle.setHorizontalLabelsColor(resolvedColorLabel);
		gvPitch.setGraphViewStyle(gvrStyle);
		gvPitch.addSeries(pitchSeries); // data
		gvPitch.setViewPort(0, 5);
		gvPitch.setScalable(true);
		gvPitch.setScrollable(true);
		gvPitch.setManualYAxisBounds(5.5d, 0.0d);
		LinearLayout lgpitch = (LinearLayout) a.id(R.id.gpitch).getView();
		lgpitch.addView(gvPitch);
	}

	public static void initMainGraph(AQuery a, Context ctx, GraphViewSeries xS, GraphViewSeries yS, GraphViewSeries zS, GraphViewSeries pS) {
		GraphView gvRaw = new LineGraphView(ctx, "") {
			@SuppressLint("DefaultLocale")
			protected String formatLabel(double value, boolean arg1) {
				return String.format("%.2f", value);
			};
		};
		gvRaw.addSeries(xS);
		gvRaw.addSeries(yS);
		gvRaw.addSeries(zS);
		gvRaw.addSeries(pS);
		gvRaw.setViewPort(0.0d, 8.0d);
		gvRaw.setScalable(true);
		gvRaw.setScrollable(true);
		gvRaw.setShowLegend(true);
		GraphViewStyle gvrStyle = new GraphViewStyle();
		gvrStyle.setLegendWidth(80);
		gvRaw.setGraphViewStyle(gvrStyle);
		gvRaw.setLegendAlign(LegendAlign.BOTTOM);
		((LineGraphView) gvRaw).setDrawBackground(false);
		LinearLayout lgRaw = (LinearLayout) a.id(R.id.graw).getView();
		lgRaw.addView(gvRaw);
	}

	private  void updateGraphRaw(int time, float x, float y, float z, double p) {
		double d = time / (double) 1000.0d;
		xS.appendData(new GraphViewData(d, x), true, 1000);
		yS.appendData(new GraphViewData(d, y), true, 1000);
		zS.appendData(new GraphViewData(d, z), true, 1000);
		// p is sum of all squares so we need to root square it to scale with the rest
		pS.appendData(new GraphViewData(d, Math.sqrt(p)), true, 1000);
		// send to coach
	}

	private void updateGraphPitch(int time, long dt) {
		// Log.e("inverseDt", String.valueOf((float)1000/dt + " " + String.valueOf((float)time/1000)));
		// dt still in millis
		float pitch = (float) 1000 / dt;
		pitchSeries.appendData(new GraphViewData((float) time / 1000, pitch), true, 100);

	}


	private void displayStepCount(int counter) {
		a.id(R.id.n_step).text(String.valueOf(counter));
	}

	@Subscribe
	public void onSensed(RunnerActivity.SenseEvent event){
		updateGraphRaw(event.t, event.x, event.y, event.z, event.p);
	}

	@Subscribe
	public void onStep(RunnerActivity.StepEvent event){
		displayStepCount(event.counter);
		updateGraphPitch(event.t, event.dt);
	}

	@Subscribe
	public void onReadySignal(RunnerActivity.SignalReadyEvent event){
		runnerData.setDistance(event.distance);
		a.id(R.id.distance).text(getString(R.string.distance_n, runnerData.getDistance()));
		flip.setDisplayedChild(1);
	}

	@Subscribe
	public void onStartSignal(RunnerActivity.SignalStartEvent event){
		Vibrator v = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(500);
		flip.setDisplayedChild(2);
	}

	@Subscribe
	public void onStopSignal(RunnerActivity.StopEvent event){
		displayStepCount(event.runnerData.getStep());
		showStats(event.runnerData);
	}

	private  void restart() {
		xS.resetData(startPad);
	}

	public void showStats(RunnerData rdEnd) {
		a.id(R.id.stats_title).text(R.string.title_stats_finish);
		a.id(R.id.pitch).text(String.format(Locale.JAPAN, "%.2f", rdEnd.getPitch()));
		a.id(R.id.stride).text(String.format(Locale.JAPAN, "%.2f", rdEnd.getStride()));
		a.id(R.id.time).text(FragmentStopWatch.getStopWatchTime(rdEnd.getStopMsCoach()));
		a.id(R.id.speed).text(String.format(Locale.JAPAN, "%.2f", rdEnd.getSpeed()));
		a.id(R.id.ps).text(String.format(Locale.JAPAN, "%.2f", RunnerMath.pitchPerStride(rdEnd.getPitch(), rdEnd.getStride())));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case RunnerActivity.REQUEST_CODE_YES_NO:
			switch (resultCode) {
			case Activity.RESULT_OK:
                BusProvider.getInstance().post(new ForceQuitEvent());
				break;
			case Activity.RESULT_CANCELED:
			default:
				break;
			}
		default:
			break;
		}
	}

    public static class OverrideEvent{}

    public static class ForceQuitEvent{}


}
