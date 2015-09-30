package jp.hanatoya.training.util;

import android.net.Uri;

import java.text.DecimalFormat;

public class UrlBuilder {

    public static final String URL_ACRA = "https://www.martiply.com/acra";
	public static final String URL_LOCAL = "http://%s.%s:8080";
	public static final String PING = "/ping";
	public static final String TEST = "/test";
	public static final String READY = "/ready";
	public static final String START = "/start";
	public static final String STOP = "/stop";
	public static final String FILE ="/file";
	public static final String RESTART = "/restart";
	public static final String STREAM_RAW = "/sraw";
	public static final String STREAM_PITCH = "/spitch";
	public static final String POST_RUN = "/postrun";
	public static final String PARAM_X = "x";
	public static final String PARAM_Y = "y";
	public static final String PARAM_Z = "z";
	public static final String PARAM_P = "p";
	public static final String PARAM_STEP= "step";
	public static final String PARAM_DT = "dt";
	public static final String PARAM_COACH_IP3 = "coachIp3";
	public static final String PARAM_DISTANCE = "distance";
	public static final String PARAM_TIME = "t";
	public static final String PARAM_SPEED = "speed";
	public static final String PARAM_PITCH = "pitch";
	public static final String PARAM_STRIDE = "stride";




	public static String ping(String firstDigits, String runnerIp3){
		Uri.Builder b = Uri.parse(String.format(URL_LOCAL, firstDigits, runnerIp3)).buildUpon();
		b.path(PING);
		b.build();
		return b.toString();
	}

	public static String getReadyToRunUrl(String firstDigits, String runnerIp3, String coachIp3, int distance){
		Uri.Builder b = Uri.parse(String.format(URL_LOCAL, firstDigits, runnerIp3)).buildUpon();
		b.path(READY);
		b.appendQueryParameter(PARAM_COACH_IP3, coachIp3);
		b.appendQueryParameter(PARAM_DISTANCE, String.valueOf(distance));
		b.build();
		return b.toString();
	}

	public static String getStartRunUrl(String firstDigits, String runnerIp3){
		Uri.Builder b = Uri.parse(String.format(URL_LOCAL, firstDigits, runnerIp3)).buildUpon();
		b.path(START);
		b.build();
		return b.toString();
	}

	public static String getRestartRunUrl(String firstDigits, String runnerIp3){
		Uri.Builder b = Uri.parse(String.format(URL_LOCAL, firstDigits, runnerIp3)).buildUpon();
		b.path(RESTART);
		b.build();
		return b.toString();
	}

	public static String getStopRunUrl(String firstDigits, String runnerIp3, long  msStopTime){
		Uri.Builder b = Uri.parse(String.format(URL_LOCAL, firstDigits, runnerIp3)).buildUpon();
		b.path(STOP);
		b.appendQueryParameter(PARAM_TIME, String.valueOf(msStopTime));
		b.build();
		return b.toString();
	}

	public static String getFileUrl(String firstDigits, String runnerIp3){
		Uri.Builder b = Uri.parse(String.format(URL_LOCAL, firstDigits, runnerIp3)).buildUpon();
		b.path(FILE);
		b.build();
		return b.toString();
	}

	public static String getSendRawToCoachUrl(String firstDigits, String coachIp3, int t, float x, float y, float  z, double p ){
		Uri.Builder b = Uri.parse(String.format(URL_LOCAL, firstDigits, coachIp3)).buildUpon();
		b.path(STREAM_RAW);
		b.appendQueryParameter(PARAM_TIME, String.valueOf(t));
		b.appendQueryParameter(PARAM_X, new DecimalFormat("#.###").format(x));
		b.appendQueryParameter(PARAM_Y, new DecimalFormat("#.###").format(y));
		b.appendQueryParameter(PARAM_Z, new DecimalFormat("#.###").format(z));
		b.appendQueryParameter(PARAM_P, new DecimalFormat("#.###").format(p));
		b.build();
		return b.toString();
	}

	public static String getSendPitchToCoachUrl(String firstDigits, String coachIp3, int t, int step, long dt){
		Uri.Builder b = Uri.parse(String.format(URL_LOCAL, firstDigits, coachIp3)).buildUpon();
		b.path(STREAM_PITCH);
		b.appendQueryParameter(PARAM_TIME, String.valueOf(t));
		b.appendQueryParameter(PARAM_STEP, String.valueOf(step));
		b.appendQueryParameter(PARAM_DT, String.valueOf(dt));
		b.build();
		return b.toString();
	}

	public static String getPostRunToCoahUrl(String firstDigits, String coachIp3, int step, float speed, float pitch, float stride){
		Uri.Builder b = Uri.parse(String.format(URL_LOCAL, firstDigits, coachIp3)).buildUpon();
		b.path(POST_RUN);
		b.appendQueryParameter(PARAM_STEP, String.valueOf(step));
		b.appendQueryParameter(PARAM_SPEED, new DecimalFormat("#.###").format(speed));
		b.appendQueryParameter(PARAM_PITCH, new DecimalFormat("#.###").format(pitch));
		b.appendQueryParameter(PARAM_STRIDE, new DecimalFormat("#.###").format(stride));
		b.build();
		return b.toString();
	}


}
