package eu.captaincode.allergywatch.ui.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.service.WidgetUpdateService;
import eu.captaincode.allergywatch.ui.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
public class SafeFoodsWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null &&
                intent.getAction().equals(WidgetUpdateService.ACTION_SAFE_FOOD_LIST_CHANGED)) {
            AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(
                    getAppWidgetIds(context), R.id.lv_safe_foods_widget_list);
        }
        super.onReceive(context, intent);
    }

    private int[] getAppWidgetIds(Context context) {
        ComponentName name = new ComponentName(context, SafeFoodsWidgetProvider.class);
        return AppWidgetManager.getInstance(context).getAppWidgetIds(name);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.safe_foods_widget);

        Intent widgetAdapterIntent = new Intent(context, ListWidgetService.class);
        views.setRemoteAdapter(R.id.lv_safe_foods_widget_list, widgetAdapterIntent);
        views.setEmptyView(R.id.lv_safe_foods_widget_list, R.id.appwidget_empty_text);

        addPendingIntentTemplate(context, views);

        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
                R.id.lv_safe_foods_widget_list);
    }

    private void addPendingIntentTemplate(Context context, RemoteViews views) {
        Intent launchProductActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent launchProductActivityPendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(launchProductActivityIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.lv_safe_foods_widget_list,
                launchProductActivityPendingIntent);
    }
    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

}
