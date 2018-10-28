package eu.captaincode.allergywatch.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.service.WidgetUpdateService;
import eu.captaincode.allergywatch.ui.ProductActivity;

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


    private static void addPendingIntentTemplate(Context context, RemoteViews views) {
        Intent launchProductActivityIntent = new Intent(context, ProductActivity.class);
        PendingIntent launchProductActivityPendingIntent = PendingIntent.getActivity(context, 0,
                launchProductActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.lv_safe_foods_widget_list,
                launchProductActivityPendingIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager,
                                        int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.safe_foods_widget);

        Intent widgetAdapterIntent = new Intent(context, ListWidgetService.class);
        views.setRemoteAdapter(R.id.lv_safe_foods_widget_list, widgetAdapterIntent);
        views.setEmptyView(R.id.lv_safe_foods_widget_list, R.id.appwidget_empty_text);

        addPendingIntentTemplate(context, views);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}

