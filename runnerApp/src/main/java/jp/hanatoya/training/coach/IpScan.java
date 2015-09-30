package jp.hanatoya.training.coach;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import jp.hanatoya.training.util.BusProvider;
import jp.hanatoya.training.util.UTF8StringRequest;
import jp.hanatoya.training.util.UrlBuilder;
import jp.hanatoya.training.util.VolleyUtils;

/**
 * Created by Martin on 2015/05/24.
 */
public class IpScan {
    private static final String TAG = "SCAN";
    private static int TIMEOUT_MILLIS = 250;
    public static int RANGE_END_EXCLUSIVE = 255;
    public static int RANGE_START = 2;
    public static int LEVELS = RANGE_END_EXCLUSIVE - RANGE_START;
    public int level = 0;
    private String ipFirstDigits;

    private RequestQueue queue = VolleyUtils.getRequestQueue();

    public IpScan(String ipFirstDigits) {
        this.ipFirstDigits = ipFirstDigits;
    }

    public void start() {
        this.level = 0;
        for (int i = RANGE_START; i < RANGE_END_EXCLUSIVE; i++) {
            UTF8StringRequest request = new UTF8StringRequest(Request.Method.GET, UrlBuilder.ping(ipFirstDigits, String.valueOf(i)), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    step();
                    BusProvider.getInstance().post(new ScanResponseEvent(response));
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    step();
                }

            });
            request.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MILLIS, 0, 0f));
            request.setTag(TAG);
            queue.add(request);
        }
    }

    private void step() {
        level++;
        BusProvider.getInstance().post(new ScanProgressEvent(level * 100 / LEVELS));
        if (level >= LEVELS)
            BusProvider.getInstance().post(new ScanCompleteEvent());
    }

    public void cancel() {
        queue.cancelAll(TAG);
    }

    public static class ScanResponseEvent{
        public String response;

        public ScanResponseEvent(String response) {
            this.response = response;
        }
    }

    public static class ScanProgressEvent {
        public int percent;

        public ScanProgressEvent(int percent) {
            this.percent = percent;
        }
    }

    public static class ScanCompleteEvent {
    }

    public static class ScanEvent {
    }

    public static class ScanCancelEvent{}

}
