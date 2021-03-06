
package com.lsyiverson.switches;

import java.lang.reflect.Method;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.RemoteViews;

public class MobileDataAppWidget extends AppWidgetProvider {
    public static final String TAG = "MobileDataAppWidget";

    private static final String MOBILE_DATA_CHANGED = "com.lsyiverson.switches.MOBILE_DATA_CHANGED";

    private static final String SMC_MOBILE_DATA_CHANGED = "com.lsyiverson.smc.MOBILE_DATA_CHANGED";

    private Intent mLaunchIntent = new Intent();

    @Override
    public void onEnabled(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, MobileDataAppWidget.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onDisabled(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, MobileDataAppWidget.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive action = " + intent.getAction());
        RemoteViews view = new RemoteViews(context.getPackageName(),
                R.layout.appwidget_mobiledataswitch);

        if (MOBILE_DATA_CHANGED.equals(intent.getAction())) {
            ConnectivityManager cm = (ConnectivityManager)context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean mobileDataEnabled = getMobileDataState(cm);
            try {
                invokeBooleanArgMethod(cm, "setMobileDataEnabled", !mobileDataEnabled);
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateView(context, !mobileDataEnabled, view);
        } else if (SMC_MOBILE_DATA_CHANGED.equals(intent.getAction())) {
            updateView(context, intent.getBooleanExtra("enabled", true), view);
        } else {
            updateView(context, getMobileDataState(context), view);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, MobileDataAppWidget.class),
                view);
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews view = new RemoteViews(context.getPackageName(),
                R.layout.appwidget_mobiledataswitch);

        updateView(context, getMobileDataState(context), view);

        appWidgetManager.updateAppWidget(new ComponentName(context, MobileDataAppWidget.class),
                view);
    }

    private void updateView(Context context, boolean dataEnabled, RemoteViews view) {
        mLaunchIntent.setClass(context, MobileDataAppWidget.class);
        mLaunchIntent.setAction(MOBILE_DATA_CHANGED);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, mLaunchIntent, 0);

        view.setOnClickPendingIntent(R.id.mobiledataswitch, pi);

        if (dataEnabled) {
            view.setImageViewResource(R.id.mobiledataswitch, R.drawable.appwidget_mobiledata_on);
        } else {
            view.setImageViewResource(R.id.mobiledataswitch, R.drawable.appwidget_mobiledata_off);
        }
    }

    private boolean getMobileDataState(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isMobileDataEnable = false;
        try {
            Object[] arg = null;
            isMobileDataEnable = (Boolean)invokeMethod(cm, "getMobileDataEnabled", arg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isMobileDataEnable;
    }

    private boolean getMobileDataState(ConnectivityManager cm) {
        boolean isMobileDataEnable = false;
        try {
            Object[] arg = null;
            isMobileDataEnable = (Boolean)invokeMethod(cm, "getMobileDataEnabled", arg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isMobileDataEnable;
    }

    public Object invokeMethod(ConnectivityManager cm, String methodName, Object[] arg)
            throws Exception {
        Class ownerClass = cm.getClass();
        Class[] argsClass = null;
        if (arg != null) {
            argsClass = new Class[1];
            argsClass[0] = arg.getClass();
        }

        Method method = ownerClass.getMethod(methodName, argsClass);

        return method.invoke(cm, arg);
    }

    public Object invokeBooleanArgMethod(ConnectivityManager cm, String methodName, boolean value)
            throws Exception {
        Class ownerClass = cm.getClass();

        Class[] argsClass = new Class[1];
        argsClass[0] = boolean.class;

        Method method = ownerClass.getMethod(methodName, argsClass);

        return method.invoke(cm, value);
    }
}
