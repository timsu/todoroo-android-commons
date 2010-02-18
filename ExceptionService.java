package com.todoroo.androidcommons.service;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.SocketTimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

/**
 * Exception handling utility class - reports and logs errors
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
public class ExceptionService {

    @Autowired
    public ErrorReporter[] errorReporters;

    @Autowired
    public Integer errorDialogTitleResource;

    public ExceptionService() {
        DependencyInjectionService.getInstance().inject(this);
    }

    /**
     * Report the error via registered error handlers
     *
     * @param name Internal error name. Not displayed to user
     * @param error Exception encountered. Message will be displayed to user
     */
    public void reportError(String name, Throwable error) {
        if(errorReporters == null)
            return;

        for(ErrorReporter reporter : errorReporters)
            reporter.handleError(name, error);
    }

    /**
     * Display error dialog if context is activity and report error
     *
     * @param context Application Context
     * @param name Internal error name. Not displayed to user
     * @param error Exception encountered. Message will be displayed to user
     */
    public void displayAndReportError(final Context context, String name, Throwable error) {
        if(context instanceof Activity) {
            final String messageToDisplay;

            // pretty up the message when displaying to user
            if(error == null)
                messageToDisplay = "(Unknown Error)"; //$NON-NLS-1$ // TODO
            else if(error instanceof SocketTimeoutException)
                messageToDisplay = "Couldn't connect to server."; //$NON-NLS-1$ // TODO
            else
                messageToDisplay = error.getMessage();

            ((Activity)context).runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        new AlertDialog.Builder(context)
                        .setTitle(context.getString(errorDialogTitleResource))
                        .setMessage(messageToDisplay)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                    } catch (Exception e) {
                        // suppress errors during dialog creation
                    }
                }
            });
        }

        reportError(name, error);
    }


    /**
     * Error reporter interface
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public interface ErrorReporter {
        public void handleError(String name, Throwable error);
    }

    /**
     * AndroidLogReporter reports errors to LogCat
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class AndroidLogReporter implements ErrorReporter {

        @Autowired
        public String applicationName;

        public AndroidLogReporter() {
            DependencyInjectionService.getInstance().inject(this);
        }

        /**
         * Report the error to the logs
         *
         * @param name
         * @param error
         */
        public void handleError(String name, Throwable error) {
            String tag;
            if(applicationName != null)
                tag = applicationName + "-" +  name; //$NON-NLS-1$
            else
                tag = "unknown-" + name; //$NON-NLS-1$

            if(error == null)
                Log.e(tag, "Exception: " + name); //$NON-NLS-1$
            else
                Log.e(tag, error.toString(), error);
        }
    }

    /**
     * Uncaught exception handler uses the exception utilities class to
     * report errors
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class TodorooUncaughtExceptionHandler implements UncaughtExceptionHandler {
        private UncaughtExceptionHandler defaultUEH;

        @Autowired
        protected ExceptionService exceptionService;

        public TodorooUncaughtExceptionHandler() {
            defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
            DependencyInjectionService.getInstance().inject(this);
        }

        public void uncaughtException(Thread thread, Throwable ex) {
            if(exceptionService != null)
                exceptionService.reportError("uncaught", ex); //$NON-NLS-1$
            defaultUEH.uncaughtException(thread, ex);
        }
    }

}

