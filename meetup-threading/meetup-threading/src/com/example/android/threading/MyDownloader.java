package com.example.android.threading;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;

import com.example.android.threading.MyDownloadTask.MyDownloadException;
import com.example.android.threading.MyDownloadTaskManager.OnDownloadCompleteListener;
import com.example.android.threading.utils.MyLog;
import com.example.android.threading.utils.MyUri;

// TODO:(pv) Replace Dialog with DialogFragment

/**
 * Original idea(s) came from:
 *  http://stackoverflow.com/questions/4538338/progressdialog-in-asynctask
 */
public class MyDownloader implements OnDownloadCompleteListener
{
    private static final String TAG                      = MyLog.TAG(MyDownloader.class);

    public static final int     DIALOG_DOWNLOAD_ERROR    = 1;
    public static final int     DIALOG_DOWNLOAD_COMPLETE = 2;

    private static MyDownloader downloader;

    public static MyDownloader get()
    {
        return downloader;
    }

    public static MyDownloader create()
    {
        if (downloader != null)
        {
            throw new IllegalStateException("MyDownloader.create(..) must only be called once");
        }

        downloader = new MyDownloader();
        return downloader;
    }

    private Activity              activity;
    private MyDownloadTaskManager downloadTaskManager;

    private MyDownloader()
    {
    }

    private MyApplication getApplication()
    {
        return (MyApplication) activity.getApplication();
    }

    public void attach(Activity activity)
    {
        this.activity = activity;

        if (downloadTaskManager != null)
        {
            MyLog.i(TAG, "Attaching previous MyDownloadTask to new Activity");
            downloadTaskManager.attach(activity);
        }
    }

    public void detach()
    {
        if (downloadTaskManager != null)
        {
            downloadTaskManager.detach();
        }
    }

    public void start(MyUri uri)
    {
        try
        {
            MyLog.i(TAG, "+start(" + uri + ")");

            if (downloadTaskManager != null && downloadTaskManager.isWorking())
            {
                MyLog.w(TAG, "Download in progress; onDownloadCancelled will auto-restart after dialog response.");
                return;
            }

            stop();

            downloadTaskManager = MyDownloadTaskManager.create(activity, MyDownloader.this);
            downloadTaskManager.execute(uri, activity.getFilesDir());
        }
        catch (Exception e)
        {
            MyLog.e(TAG, "start", e);
        }
        finally
        {
            MyLog.i(TAG, "-start(" + uri + ")");
        }
    }

    /**
     * @return false if MyDownloader was already stopped, true if MyDownloader was not already stopped
     */
    public boolean stop()
    {
        if (downloadTaskManager == null || !downloadTaskManager.isWorking())
        {
            return false;
        }

        downloadTaskManager.cancel();
        downloadTaskManager = null;

        return true;
    }

    public Dialog onCreateDialog(int id, Bundle args)
    {
        switch (id)
        {
            case DIALOG_DOWNLOAD_ERROR:
                return createDownloadErrorDialog(args);
            case DIALOG_DOWNLOAD_COMPLETE:
                return createDownloadCompleteDialog(args);
            default:
                return null;
        }
    }

    private Dialog createDownloadErrorDialog(Bundle args)
    {
        final String message = (args != null) ? args.getString("message") : "UNKNOWN";

        return new AlertDialog.Builder(activity) //
        .setTitle(R.string.download_error_downloading) //
        .setMessage(message) //
        .setPositiveButton(R.string.button_ok, new OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                onDownloadCancelled();
            }
        }) //
        .create();
    }

    private Dialog createDownloadCompleteDialog(Bundle args)
    {
        final String filePath = (args != null) ? args.getString("filePath") : null;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.download_complete);
        builder.setMessage(R.string.download_launch_now);
        builder.setPositiveButton(R.string.button_yes, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //File file = new File(filePath);
                Uri fileUri = Uri.parse(filePath);//.fromFile(file);

                String extension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
                String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, mimetype);

                // TODO:(pv) Find way to detect SUCCESS or CANCEL result of app installer... 
                //int requestCode = 0;
                //activity.startActivityForResult(intent, requestCode);
                // ...until then:
                activity.startActivity(intent);

                // TODO:(pv) Exit the app?
                onDownloadCancelled();
            }
        });
        builder.setNegativeButton(R.string.button_no, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                onDownloadCancelled();
            }
        });
        return builder.create();
    }

    public void onDownloadCancelled()
    {
        if (!stop())
        {
            return;
        }
    }

    /*
    private void showDialog(int id)
    {
        showDialog(id, null);
    }
    */

    private void showDialog(int id, Bundle args)
    {
        if (activity != null)
        {
            try
            {
                activity.showDialog(id, args);
            }
            catch (Exception e)
            {
                // ignore?
            }
        }
    }

    @Override
    public void onDownloadComplete(MyDownloadTask task)
    {
        final boolean handled;

        if (task != null && !task.isCancelled())
        {
            Object result;
            try
            {
                result = task.get();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                result = null;
            }

            if (result instanceof String)
            {
                Bundle args = new Bundle();
                args.putString("filePath", (String) result);
                // ONE LAST BUG! If you background the app and let the download complete, when the app is foregrounded this dialog doesn't show.
                showDialog(DIALOG_DOWNLOAD_COMPLETE, args);
                handled = true;
            }
            else
            {
                final String message;

                if (result instanceof MyDownloadException)
                {
                    message = getGenericRetryOrDebugErrorMessage(result.toString());
                }
                else
                {
                    message =
                        getGenericRetryOrDebugErrorMessage("UNEXPECTED result: "
                                        + ((result == null) ? "null" : result.toString()));
                }

                Bundle args = new Bundle();
                args.putString("message", message);
                showDialog(DIALOG_DOWNLOAD_ERROR, args);
                handled = true;
            }
        }
        else
        {
            handled = false;
        }

        if (!handled)
        {
            onDownloadCancelled();
        }
    }

    private String getGenericRetryOrDebugErrorMessage(String messageDebug)
    {
        return getGenericOrDebugErrorMessage(activity.getString(R.string.download_please_retry_later), messageDebug);
    }

    private String getGenericOrDebugErrorMessage(String messageGeneric, String messageDebug)
    {
        return (getApplication().isDebugEnabled()) ? messageDebug : messageGeneric;
    }
}