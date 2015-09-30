package jp.hanatoya.training.util;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;


@ReportsCrashes(
        formUri = UrlBuilder.URL_ACRA,
        httpMethod = HttpSender.Method.POST,
        reportType = HttpSender.Type.FORM,
        formKey = "", // This is required for backward compatibility but not used
        customReportContent = {
                ReportField.REPORT_ID,
                ReportField.INSTALLATION_ID,
                ReportField.APP_VERSION_NAME,
                ReportField.APP_VERSION_CODE,
                ReportField.PACKAGE_NAME,
                ReportField.PHONE_MODEL,
                ReportField.BRAND,
                ReportField.PRODUCT,
                ReportField.ANDROID_VERSION,
                ReportField.BUILD,
                ReportField.TOTAL_MEM_SIZE,
                ReportField.AVAILABLE_MEM_SIZE,
                ReportField.STACK_TRACE,
                ReportField.INITIAL_CONFIGURATION,
                ReportField.CRASH_CONFIGURATION,
                ReportField.DEVICE_FEATURES
        },
//        disableSSLCertValidation = true,
        mode = ReportingInteractionMode.SILENT)
public class MyApplication extends Application{

	private static Context context;


    @Override
    public void onCreate() {
        ACRA.init(this);
        context = getApplicationContext();
        VolleyUtils.init(this);
        super.onCreate();
    }


    public static Context getAppContext() {
        return MyApplication.context;
    }

}
