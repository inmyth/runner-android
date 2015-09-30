package jp.hanatoya.training.runner;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.androidquery.AQuery;

import jp.hanatoya.training.R;
import jp.hanatoya.training.json.RunnerData;
import jp.hanatoya.training.util.BusProvider;
import jp.hanatoya.training.util.NetworkUtils;
import jp.hanatoya.training.util.PreferenceUtils;
import jp.hanatoya.training.util.ViewUtils;

public class FragmentPrepare extends Fragment{
	public static final String TAG = "FragmentPrepare";
	private AQuery a;

	
	public static FragmentPrepare newInstance(){
		FragmentPrepare fragmentPrepare = new FragmentPrepare();
		return fragmentPrepare;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.fragment_runner_prep, container, false);
	    a = new AQuery(view);
	    
        String playerName = PreferenceUtils.getPlayerName(getActivity());
        if (playerName != null){
        	a.id(R.id.name).getEditText().setText(playerName);       	
        }

        a.id(android.R.id.button1).clicked(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				a.id(android.R.id.text1).invisible();
				ViewUtils.hideKeyboard(getActivity());
				
				ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (!mWifi.isConnected()) {
					a.id(android.R.id.text1).visible().text(R.string.wifi_na);
					return;
				}
				
				String ip = NetworkUtils.getLocalIpAddress();
				if (ip == null || !NetworkUtils.isLocalNetwork(ip)){
					a.id(android.R.id.text1).visible().text(R.string.wifi_na);
					return;
				}

				String playerName = a.id(R.id.name).getEditText().getText().toString();
				
				if (playerName == null || playerName.trim().length() == 0){
					a.id(R.id.name).getEditText().setError(getString(R.string.necessary));
					return;
				}

				boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
				if (!isSDPresent){
					a.id(android.R.id.text1).visible().text(R.string.sdcard_na);
					return;							
				}
		
				PreferenceUtils.setRunnerName(getActivity(), playerName);

		        RunnerData runnerData = new  RunnerData();
		        runnerData.setRunnerName(playerName);
				runnerData.setIp3(NetworkUtils.getLastDigits(ip));
				BusProvider.getInstance().post(new FinishRegistrationEvent(runnerData));
			}
		});
	    return view;

	}

	public static class FinishRegistrationEvent{
		public RunnerData runnerData;

		public FinishRegistrationEvent(RunnerData runnerData) {
			this.runnerData = runnerData;
		}
	}
	
	

}
