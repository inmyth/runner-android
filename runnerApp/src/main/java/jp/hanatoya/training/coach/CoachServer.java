package jp.hanatoya.training.coach;

import jp.hanatoya.training.json.RunnerData;
import jp.hanatoya.training.util.GsonUtils;
import jp.hanatoya.training.util.UrlBuilder;
import jp.hanatoya.training.webserver.NanoHTTPD;

public class CoachServer extends NanoHTTPD {
    private CoachServerListener listener;

	public CoachServer(CoachServerListener listener){
        super(8080);
        this.listener = listener;
    }
	 
    @Override
    public Response serve(IHTTPSession session) {
    	RunnerData runnerData = new RunnerData();
		switch (session.getUri()) {
			case UrlBuilder.STREAM_PITCH:
				String tS = session.getParms().get(UrlBuilder.PARAM_TIME);
				String sS = session.getParms().get(UrlBuilder.PARAM_STEP);
				String dtS = session.getParms().get(UrlBuilder.PARAM_DT);

				Integer t = null, step = null, dt = null;
				try {
					t = Integer.parseInt(tS);
					step = Integer.parseInt(sS);
					dt = Integer.parseInt(dtS);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				if (t != null && step != null && dt != null) {
					listener.onPitch(t, step, dt);
				}
				break;
			case UrlBuilder.STREAM_RAW:
				tS = session.getParms().get(UrlBuilder.PARAM_TIME);
				String xS = session.getParms().get(UrlBuilder.PARAM_X);
				String yS = session.getParms().get(UrlBuilder.PARAM_Y);
				String zS = session.getParms().get(UrlBuilder.PARAM_Z);
				String pS = session.getParms().get(UrlBuilder.PARAM_P);

				t = null;
				Float x = null, y = null, z = null;
				Double p = null;
				try {
					t = Integer.parseInt(tS);
					x = Float.parseFloat(xS);
					y = Float.parseFloat(yS);
					z = Float.parseFloat(zS);
					p = Double.parseDouble(pS);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				if (t != null && x != null && y != null && z != null && p != null) {
					listener.onRaw(t, x, y, z, p);
				}
				break;
			case UrlBuilder.POST_RUN:
				String speedS = session.getParms().get(UrlBuilder.PARAM_SPEED);
				String pitchS = session.getParms().get(UrlBuilder.PARAM_PITCH);
				String strideS = session.getParms().get(UrlBuilder.PARAM_STRIDE);
				String stepS = session.getParms().get(UrlBuilder.PARAM_STEP);
				Float speed = null, pitch = null, stride = null;
				step = 0;
				try {
					speed = Float.parseFloat(speedS);
					pitch = Float.parseFloat(pitchS);
					stride = Float.parseFloat(strideS);
					step = Integer.parseInt(stepS);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				if (speed != null && pitch != null && stride != null) {
					listener.onPostRun(step, speed, pitch, stride);
				}

			default:
				runnerData = new RunnerData();
				runnerData.setError(-1);
				runnerData.setMessage("Unrecognized Param");
		}
		return new NanoHTTPD.Response( GsonUtils.toJson(runnerData));
    }

    public interface CoachServerListener{
    	public void onPitch(int t, int step, int dt);
    	public void onRaw(int t, float x, float y, float z, double p);
		public void onPostRun(int step, float speed, float pitch, float stride);

    }

}
