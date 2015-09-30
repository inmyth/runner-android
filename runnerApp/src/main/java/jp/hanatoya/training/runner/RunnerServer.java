package jp.hanatoya.training.runner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import jp.hanatoya.training.json.RunnerData;
import jp.hanatoya.training.util.GsonUtils;
import jp.hanatoya.training.util.UrlBuilder;
import jp.hanatoya.training.webserver.NanoHTTPD;
import jp.hanatoya.training.webserver.NanoHTTPD.Response.Status;

public class RunnerServer extends NanoHTTPD {

	private WebServerListener listener;
    private String dumpPath;
    
    public RunnerServer(WebServerListener listener){
        super(8080);
        this.listener = listener;
    }

	public void setDumpPath(String dumpPath) {
		this.dumpPath = dumpPath;
	}

	@Override
    public Response serve(IHTTPSession session) {
   	 if (session.getUri().equals(UrlBuilder.FILE)){
		 FileInputStream fis = null;
		 try {
			 fis = new FileInputStream(dumpPath);
		 } catch (FileNotFoundException e) {
			 e.printStackTrace();
		 }
		 return new NanoHTTPD.Response(Status.OK, "text/plain", fis); // could return null
	 }
	
		RunnerData runnerData;

		switch (session.getUri()){
			case UrlBuilder.PING:
				runnerData = listener.getRunnerData();
				break;
			case UrlBuilder.TEST:
				listener.onTest();
				runnerData = listener.getRunnerData();
				break;
			case UrlBuilder.READY:
				String coachId = session.getParms().get(UrlBuilder.PARAM_COACH_IP3);
				int distance = -1;
				try{
					distance = Integer.parseInt(session.getParms().get(UrlBuilder.PARAM_DISTANCE));
				}catch (NumberFormatException e){
					distance = -1;
				}
				listener.onReady(coachId, distance);
				runnerData = listener.getRunnerData();
				break;
			case UrlBuilder.START:
				runnerData = listener.getRunnerData();
				listener.onStartRun();
				break;

			case UrlBuilder.RESTART:
				listener.onRestart();
				runnerData = listener.getRunnerData();
				break;
			case UrlBuilder.STOP:
				String finalTime = session.getParms().get(UrlBuilder.PARAM_TIME);
				runnerData = new RunnerData();
				listener.onStopRun(finalTime);
				break;
			default:
				runnerData = new RunnerData();
				runnerData.setError(-1);
				runnerData.setMessage("Unrecognized URL");
		}
		return new NanoHTTPD.Response( GsonUtils.toJson(runnerData));
	}

    public interface WebServerListener{
    	public void onTest();
    	public RunnerData getRunnerData();
    	public void onReady(String coachId, int distance);
    	public void onStartRun();
    	public void onStopRun(String sTime);
    	public void onRestart();
    }

}
