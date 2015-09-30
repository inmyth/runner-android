package jp.hanatoya.training.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by Martin on 2015/03/17.
 */

public final class BusProvider {

    private static final MyBus BUS = new MyBus();
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    public static MyBus getInstance() {
        return BUS;
    }


}

