package com.example.android.threading;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

import com.example.android.threading.MyDownloadTask.MyDownloadProgressListener;
import com.example.android.threading.utils.MyUri;

public final class MyDownloadTaskManager implements MyDownloadProgressListener, OnCancelListener
{
    public interface OnDownloadCompleteListener
    {
        /**
         * success: task.get() instanceof String representing local downloaded file path<br>
         * error: task.get() not instanceof String possibly representing Exception<br>
         * cancelled: task == null or task.isCanceled() == true
         * @param task
         */
        void onDownloadComplete(MyDownloadTask task);
    }

    // TODO:(pv) Make this a singleton?
    public static MyDownloadTaskManager create(Activity activity, OnDownloadCompleteListener listener)
    {
        return new MyDownloadTaskManager(activity, listener);
    }

    private final OnDownloadCompleteListener listener;

    private Activity                         activity;
    private ProgressDialog                   dialog;
    private MyDownloadTask                   task;
    private boolean                          finished;

    private MyDownloadTaskManager(Activity activity, OnDownloadCompleteListener taskCompleteListener)
    {
        this.listener = taskCompleteListener;
        attach(activity);
    }

    public void attach(Activity activity)
    {
        this.activity = activity;

        dialog = new ProgressDialog(activity);
        dialog.setMessage(activity.getString(R.string.downloading_file));
        dialog.setCancelable(true);
        dialog.setOnCancelListener(this);
        dialog.setIndeterminate(true);
        if (task != null)
        {
            task.setProgressTracker(this);
        }
    }

    public void detach()
    {
        if (task != null)
        {
            task.setProgressTracker(null);
        }
        dialog.dismiss();
    }

    public boolean isWorking()
    {
        return task != null;
    }

    public void execute(MyUri src, File dst)
    {
        task = new MyDownloadTask(dst);
        task.setProgressTracker(this);
        task.execute(src);
    }

    public void cancel()
    {
        if (finished)
        {
            return;
        }

        finished = true;
        if (task != null)
        {
            task.cancel(true);
            task = null;
        }
        dialog.dismiss();
        listener.onDownloadComplete(null);
    }

    @Override
    public void onPreExecute()
    {
        if (finished)
        {
            return;
        }

        // Show dialog if it wasn't shown yet or was removed on configuration (rotation) change
        if (!dialog.isShowing())
        {
            dialog.show();
        }
    }

    @Override
    public void onProgress(int length, int progress)
    {
        if (finished)
        {
            return;
        }

        if (dialog.isIndeterminate() && length > 0)
        {
            dialog.dismiss();

            // TODO:(pv) I forget; Can I switch a progress dialog from indeterminite to determinate on the fly?
            dialog = new ProgressDialog(activity);
            dialog.setMessage(activity.getString(R.string.downloading_file));
            dialog.setCancelable(true);
            dialog.setOnCancelListener(this);
            dialog.setIndeterminate(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMax(length);
        }

        if (progress > 0)
        {
            dialog.setProgress(progress);
        }

        if (!dialog.isShowing())
        {
            dialog.show();
        }
    }

    @Override
    public void onPostExecute(Object result)
    {
        if (finished)
        {
            return;
        }

        finished = true;
        dialog.dismiss();
        listener.onDownloadComplete(task);
        task = null;
    }

    /**
     * Called when the task itself is canceled.
     */
    @Override
    public void onCancelled(Object result)
    {
        cancel();
    }

    /**
     * Called when the dialog itself is canceled.
     */
    @Override
    public void onCancel(DialogInterface dialog)
    {
        cancel();
    }
}