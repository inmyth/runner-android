package jp.hanatoya.training.util;

public class RunnerMath {
	
	/**
	 * Get pitch
	 * @param totalTime Total time from start to finish in ms
	 * @param stepCount Total step count
	 */
	public static float pitch(long totalTime, int stepCount){
		long totalTimeSecond = totalTime / 1000;
		if (totalTimeSecond == 0) {
			return 0.0f;
		}else{
			float result = (float)stepCount / totalTimeSecond;
			return result;
		}
	}

	/**
	 * Get stride
	 * @param distance Total distance in m
	 * @param stepCount Total step count
	 * @return
	 */	
	public static float stride(int distance, int stepCount){
		if (stepCount == 0){
			return 0.0f;
		}
		float result = (float)distance / stepCount;
		return result;
	}
	
	
	public static float speed(int distance, long msTime){
		return distance / ((float) msTime / 1000);				
	}
	
	
	public static float stride (float speed, float pitch){
		if (pitch == 0.0f){
			return  0.0f;
		}
		return speed / pitch;		
	}

	public static float pitchPerStride(float pitch, float stride){
		if (stride == 0.0f){
			return 0.0f;
		}
		return pitch / stride;
	}
	
}
