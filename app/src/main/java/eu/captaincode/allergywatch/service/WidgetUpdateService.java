package eu.captaincode.allergywatch.service;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.ui.widget.SafeFoodsWidgetProvider;

public class WidgetUpdateService extends IntentService {
    public static final String ACTION_SAFE_FOOD_LIST_CHANGED =
            "eu.captaincode.allergywatch.action.SAFE_FOOD_LIST_CHANGED";
    public static final String EXTRA_RECIPE = "recipe";

    public WidgetUpdateService() {
        super("WidgetUpdateService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null && intent.getAction() != null &&
                intent.getAction().equals(ACTION_SAFE_FOOD_LIST_CHANGED)) {
            sendUpdateWidgetBroadcast();
        }
    }

    private void sendUpdateWidgetBroadcast() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                SafeFoodsWidgetProvider.class));
        //Trigger data update to handle the GridView widgets and force a data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_safe_foods_widget_list);
        SafeFoodsWidgetProvider.updateAppWidgets(this, appWidgetManager, appWidgetIds);
        Intent updateWidgetIntent = new Intent(getApplicationContext(),
                SafeFoodsWidgetProvider.class);
        updateWidgetIntent.setAction(ACTION_SAFE_FOOD_LIST_CHANGED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateWidgetIntent);
    }


}
