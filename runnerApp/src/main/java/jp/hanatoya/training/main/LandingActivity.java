package jp.hanatoya.training.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import jp.hanatoya.training.R;
import jp.hanatoya.training.coach.CoachActivity;
import jp.hanatoya.training.runner.RunnerActivity;
import jp.hanatoya.training.util.NetworkUtils;

public class LandingActivity extends Activity {


	private AQuery a;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_landing);

		
		a = new AQuery(this);

		a.id(android.R.id.button1).clicked(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] ipComponents = getIpComponents();
				if (ipComponents == null){
					Crouton.makeText(LandingActivity.this, R.string.wifi_na, Style.ALERT).show();
					return;
				}
				Intent i = new Intent(LandingActivity.this, RunnerActivity.class);
				i.putExtra(NetworkUtils.KEY_FIRST_DIGITS, ipComponents[0]);
				i.putExtra(NetworkUtils.KEY_IP3, ipComponents[1]);
				startActivity(i);
			}
		});

		View button1 = a.id(android.R.id.button1).getView();
		ImageView bic1 = (ImageView) button1.findViewById(android.R.id.icon);
		bic1.setImageResource(R.drawable.ic_runner);
		TextView bt1a = (TextView) button1.findViewById(android.R.id.text1);
		bt1a.setText(R.string.mode_runner);
		TextView bt1b = (TextView) button1.findViewById(android.R.id.text2);
		bt1b.setText(R.string.runner);

		a.id(android.R.id.button2).clicked(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String ipComponents[] = getIpComponents();
				if (ipComponents == null){
					Crouton.makeText(LandingActivity.this, R.string.wifi_na, Style.ALERT).show();
					return;
				}
				Intent i = new Intent(LandingActivity.this, CoachActivity.class);
				i.putExtra(NetworkUtils.KEY_FIRST_DIGITS, ipComponents[0]);
				i.putExtra(NetworkUtils.KEY_IP3, ipComponents[1]);
				startActivity(i);
			}
		});

		View button2 = a.id(android.R.id.button2).getView();
		ImageView bic2 = (ImageView) button2.findViewById(android.R.id.icon);
		bic2.setImageResource(R.drawable.ic_pulse);
		TextView bt2a = (TextView) button2.findViewById(android.R.id.text1);
		bt2a.setText(R.string.mode_coach);
		TextView bt2b = (TextView) button2.findViewById(android.R.id.text2);
		bt2b.setText(R.string.coach);
		
		try {
			a.id(R.id.version).text(getResources().getString(R.string.version_n, (getPackageManager().getPackageInfo(getPackageName(), 0).versionName)));
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String[] getIpComponents(){
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (!mWifi.isConnected()) {
			return null;
		}
		String ip = NetworkUtils.getLocalIpAddress();
		if (ip == null || !NetworkUtils.isLocalNetwork(ip)) {
			return null;
		}

		return new String[]{NetworkUtils.getFirstDigits(ip), NetworkUtils.getLastDigits(ip)};
	}

}
