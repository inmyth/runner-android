package jp.hanatoya.training.json;

import org.parceler.Parcel;

@Parcel
public class RunnerData extends BaseResponse {

	 String runnerName;
	 String ip3;
	 int distance;

	 float ps;
	 float pitch;
	 float stride;
	 int step;
	 long stopMsCoach;
	 float speed;

	public void restart(){
		ps = 0.0f;
		pitch = 0.0f;
		stride = 0.0f;
		step = 0;
		stopMsCoach = 0L;
		speed = 0.0f;
		message = null;
	}

	public String getIp3() {
		return ip3;
	}

	public float getPs() {
		return ps;
	}

	public void setIp3(String ip3) {
		this.ip3 = ip3;
	}

	public void setPs(float ps) {
		this.ps = ps;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getSpeed() {
		return speed;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public void setStride(float stride) {
		this.stride = stride;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getPitch() {
		return pitch;
	}

	public int getDistance() {
		return distance;
	}

	public float getStride() {
		return stride;
	}

	public void setStopMsCoach(long stopMsCoach) {
		this.stopMsCoach = stopMsCoach;
	}

	public long getStopMsCoach() {
		return stopMsCoach;
	}


	public void setRunnerName(String runnerName) {
		this.runnerName = runnerName;
	}

	public String getRunnerName() {
		return runnerName;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	@Override
	public String toString() {
		return runnerName;
	}
}
