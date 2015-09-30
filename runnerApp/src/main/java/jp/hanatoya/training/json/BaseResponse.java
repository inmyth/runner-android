package jp.hanatoya.training.json;

import org.parceler.Parcel;

@Parcel
public class BaseResponse {
	
	int error;
	String message;
	
	public void setError(int error) {
		this.error = error;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public int getError() {
		return error;
	}

}
