package jp.hanatoya.training.coach;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.squareup.otto.Subscribe;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import jp.hanatoya.training.R;
import jp.hanatoya.training.json.RunnerData;
import jp.hanatoya.training.util.BusProvider;
import jp.hanatoya.training.util.GsonUtils;
import jp.hanatoya.training.util.ViewUtils;

public class FragmentRegister extends Fragment{
	
	private AQuery a;
	private ProgressBar hb;
	private Adapter adapter;

	public static FragmentRegister newInstance(){
		FragmentRegister fragmentRegister = new FragmentRegister();
		return fragmentRegister;		
	}

	public static FragmentRegister newInstance(String prevRunnerIp3, int prevDistance){
		Bundle bundle = new Bundle();
		bundle.putString("prevRunnerIp3", prevRunnerIp3);
		bundle.putInt("prevDistance", prevDistance);
		FragmentRegister fragmentRegister = new FragmentRegister();
		fragmentRegister.setArguments(bundle);
		return fragmentRegister;
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		adapter = new Adapter(getActivity());
		clearAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		BusProvider.getInstance().register(this);
	    View view = inflater.inflate(R.layout.fragment_coach_reg, container, false);

	    a = new AQuery(view);		
		hb = (ProgressBar)a.id(android.R.id.progress).getProgressBar();

		a.id(R.id.scan).clicked(new OnClickListener() {
			@Override
			public void onClick(View view) {
				ViewUtils.hideKeyboard(getActivity());
				if (hb.getVisibility() == View.INVISIBLE){
					a.id(R.id.scan).text(android.R.string.cancel);
					hb.setVisibility(View.VISIBLE);
					hb.setProgress(0);
					clearAdapter();
					BusProvider.getInstance().post(new IpScan.ScanEvent());
				}else if (hb.getVisibility() == View.VISIBLE){
					BusProvider.getInstance().post(new IpScan.ScanCancelEvent());
					resetList();
				}
			}
		});

		a.id(R.id.spinner).adapter(adapter).getSpinner().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				if (i !=0 ){
					a.id(R.id.ip3).text(adapter.getItem(i).getIp3());
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		resetList();
		a.id(R.id.cancel).clicked(new OnClickListener() {
			@Override
			public void onClick(View view) {
				ViewUtils.hideKeyboard(getActivity());
				BusProvider.getInstance().post(new IpScan.ScanCancelEvent());
				resetList();
			}
		});

		a.id(R.id.ok).visible().clicked(new OnClickListener() {
			@Override
			public void onClick(View view) {
				ViewUtils.hideKeyboard(getActivity());
				BusProvider.getInstance().post(new IpScan.ScanCancelEvent());
				resetList();

				BusProvider.getInstance().post(new IpScan.ScanCancelEvent());
				if (a.id(R.id.distance).getEditable().length() < 2 || a.id(R.id.ip3).getEditable().length() == 0 ){
					Crouton.makeText(getActivity(), R.string.error_need_distance_and_runner, Style.ALERT).show();
					return;
				}
				BusProvider.getInstance().post(new PingRunnerRequestEvent(a.id(R.id.ip3).getEditable().toString(), Integer.parseInt(a.id(R.id.distance).getEditable().toString())));

			}
		});

		if (getArguments()!=null){
			a.id(R.id.distance).text(String.valueOf(getArguments().getInt("prevDistance")));
			a.id(R.id.ip3).text(getArguments().getString("prevRunnerIp3"));
		}


	    return view;
	}


	public static class Adapter extends ArrayAdapter<RunnerData>{

		public Adapter(Context context) {
			super(context, R.layout.row_fargment_register_runner);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null){
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_fargment_register_runner, parent, false);
				ViewHolder viewHolder = new ViewHolder(convertView);
				convertView.setTag(viewHolder);
			}
			ViewHolder holder = (ViewHolder)convertView.getTag();
			final RunnerData runnerData = getItem(position);
			AQuery ar = holder.a;
			ar.id(holder.name).text(runnerData.getRunnerName());
			return convertView;
		}
	}

	public static class ViewHolder{
		AQuery a;
		TextView name;
		LinearLayout root;

		public ViewHolder(View convertView){
			a = new AQuery(convertView);
			name = a.id(android.R.id.text1).getTextView();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		BusProvider.getInstance().unregister(this);
	}

	@Subscribe
	public void onScanProgress(IpScan.ScanProgressEvent event){
		hb.setProgress(event.percent);
	}

	@Subscribe
	public void onScanResponse(IpScan.ScanResponseEvent event){
		RunnerData runnerData = GsonUtils.toBean(event.response, RunnerData.class);
		adapter.add(runnerData);
	}

	@Subscribe
	public void onScanCompleted(IpScan.ScanCompleteEvent event){
		if (adapter.getCount() > 0){
			showList();
		}else{
			showEmpty();
			Crouton.makeText(getActivity(), R.string.error_runner_not_found, Style.ALERT).show();
		}
	}

	@Subscribe
	public void onUserPingFailed(PingRunnerFailedEvent event){
		Crouton.makeText(getActivity(), getString(R.string.ip_na, event.runnerIp3), Style.ALERT).show();
	}

	private void clearAdapter(){
		adapter.clear();
		RunnerData hint = new RunnerData();
		hint.setRunnerName(getString(R.string.choose_runner));
		adapter.insert(hint, 0);
	}


	private void showEmpty(){
//		a.id(R.id.spinner).invisible();
		a.id(R.id.scan).text(R.string.scan_device);
		hb.setVisibility(View.INVISIBLE);
	}

	private void showList(){
//		a.id(R.id.spinner).visible();
		a.id(R.id.scan).text(R.string.scan_device);
		hb.setVisibility(View.INVISIBLE);

	}

	private void resetList(){
//		a.id(R.id.spinner).invisible();
		hb.setVisibility(View.INVISIBLE);
		a.id(R.id.scan).text(R.string.scan_device);
	}

	public static class PingRunnerRequestEvent{
		public  String runnerIp3;
		public int distance;

		public PingRunnerRequestEvent(String runnerIp3, int distance) {
			this.runnerIp3 = runnerIp3;
			this.distance = distance;
		}
	}

	public static class PingRunnerSuccessEvent{
		public RunnerData runnerData;

		public PingRunnerSuccessEvent(RunnerData runnerData) {
			this.runnerData = runnerData;
		}
	}

	public static class PingRunnerFailedEvent{
		public String runnerIp3;

		public PingRunnerFailedEvent(String runnerIp3) {
			this.runnerIp3 = runnerIp3;
		}
	}



}
