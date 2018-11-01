package eu.captaincode.allergywatch.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import eu.captaincode.allergywatch.AllergyWatchApp;
import eu.captaincode.allergywatch.ui.widget.SafeFoodsWidgetProvider;

public class WidgetUpdateService extends IntentService {
    public static final String ACTION_SAFE_FOOD_LIST_CHANGED =
            "eu.captaincode.allergywatch.action.SAFE_FOOD_LIST_CHANGED";

    public WidgetUpdateService() {
        super("WidgetUpdateService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null && intent.getAction() != null &&
                intent.getAction().equals(ACTION_SAFE_FOOD_LIST_CHANGED)) {
            ((AllergyWatchApp) getApplication()).getRepository().refreshProducts();
            sendUpdateWidgetBroadcast();
        }
    }

    private void sendUpdateWidgetBroadcast() {
        Intent updateWidgetIntent = new Intent(getApplicationContext(),
                SafeFoodsWidgetProvider.class);
        updateWidgetIntent.setAction(ACTION_SAFE_FOOD_LIST_CHANGED);
        sendBroadcast(updateWidgetIntent);
    }
}
