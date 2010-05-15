package com.todoroo.andlib.utility;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

<<<<<<< Updated upstream:AndroidUtilities.java
import com.todoroo.android.bente.R;
import com.todoroo.android.bente.utilities.Notifications;
import com.todoroo.androidcommons.service.Autowired;
import com.todoroo.androidcommons.service.DependencyInjectionService;
import com.todoroo.androidcommons.service.ExceptionService;
=======
import com.todoroo.andlib.service.Autowired;
import com.todoroo.andlib.service.DependencyInjectionService;
import com.todoroo.andlib.service.ExceptionService;
>>>>>>> Stashed changes:AndroidUtilities.java

/**
 * Android Utility Classes
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
public class AndroidUtilities {

    private static class ExceptionHelper {
        @Autowired
        public ExceptionService exceptionService;

        public ExceptionHelper() {
            DependencyInjectionService.getInstance().inject(this);
        }
    }

    /**
     * @return true if we're connected to the internet
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null)
            return false;
        if (info.getState() != State.CONNECTED)
            return false;
        return true;
    }

    /** Fetch the image specified by the given url */
    public static Bitmap fetchImage(URL url) throws IOException {
        InputStream is = null;
        try {
            URLConnection conn = url.openConnection();
            conn.connect();
            is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, 16384);
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(bis);
                return bitmap;
            } finally {
                bis.close();
            }
        } finally {
            if(is != null)
                is.close();
        }
    }

    /**
     * Start the given intent, handling security exceptions if they arise
     *
     * @param context
     * @param intent
     */
    public static void startExternalIntent(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (SecurityException e) {
            ExceptionHelper helper = new ExceptionHelper();
            helper.exceptionService.displayAndReportError(context,
                    "start-external-intent-" + intent.toString(), //$NON-NLS-1$
                    e);
        }
    }

    /**
     * Start the given intent, handling security exceptions if they arise
     *
     * @param activity
     * @param intent
     * @param requestCode
     */
    public static void startExternalIntentForResult(
            Activity activity, Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (SecurityException e) {
            ExceptionHelper helper = new ExceptionHelper();
            helper.exceptionService.displayAndReportError(activity,
                    "start-external-intent-" + intent.toString(), //$NON-NLS-1$
                    e);
        }
<<<<<<< Updated upstream:AndroidUtilities.java
    }

    /**
     * Simple way to show a notification with the given message
     * @param message
     */
    public static void showNotification(Context context,
            int iconResource, String message) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String title = context.getResources().getString(R.string.app_name);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
        Notification notification = Notifications.createNotification(context, pendingIntent,
                    iconResource, title, message);
        notification.ledARGB = Color.BLUE;
        nm.notify(Notifications.ID_MESSAGES, notification);
=======
>>>>>>> Stashed changes:AndroidUtilities.java
    }

}
