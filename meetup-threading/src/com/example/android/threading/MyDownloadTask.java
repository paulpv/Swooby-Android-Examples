package com.example.android.threading;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;

import com.example.android.threading.utils.MyConnection;
import com.example.android.threading.utils.MyHttpConnectionPlatform;
import com.example.android.threading.utils.MyLog;
import com.example.android.threading.utils.MyString;
import com.example.android.threading.utils.MyUri;

/**
 * Logically separated from any Activity so that it can progress 
 * while the Activity is destroyed and recreated during orientation changes.
 * 
 * Main idea came from:
 *  http://www.codeproject.com/KB/android/asynctask_progressdialog.aspx
 * Some other ideas came from:
 *  https://github.com/ddewaele/AndroidProgressDialogSample/blob/master/src/com/ecs/android/listview/sample/AsyncTaskComplex.java
 *  http://blog.doityourselfandroid.com/2010/11/14/handling-progress-dialogs-and-screen-orientation-changes/
 *  http://www.brighthub.com/mobile/google-android/articles/43168.aspx
 *  http://www.eigo.co.uk/News-Article.aspx?NewsArticleID=82
 *  http://stackoverflow.com/questions/1111980/how-to-handle-screen-orientation-change-when-progress-dialog-and-background-threa
 *  http://stackoverflow.com/questions/4538338/progressdialog-in-asynctask
 *  http://stackoverflow.com/questions/2702695/android-async-task-progressdialog-isnt-showing-until-background-thread-finishes
 *  http://stackoverflow.com/questions/2487690/progressdialog-not-working-in-external-asynctask
 */
public class MyDownloadTask extends AsyncTask<MyUri, Integer, Object>
{
    private static final String TAG = MyLog.TAG(MyDownloadTask.class);

    public interface MyDownloadProgressListener
    {
        void onPreExecute();

        void onProgress(int length, int progress);

        void onPostExecute(Object result);

        void onCancelled(Object result);
    }

    public class MyDownloadException extends Exception
    {
        private static final long serialVersionUID = -3921454473792465069L;

        public final Exception    innerException;

        public MyDownloadException(String message)
        {
            super(message);
            this.innerException = null;
        }

        public MyDownloadException(Exception innerException)
        {
            this.innerException = innerException;
        }

        public String toString()
        {
            String message = super.getMessage();

            if (MyString.isNullOrEmpty(message))
            {
                message = (innerException != null) ? innerException.toString() : "UNKNOWN";
            }
            else
            {
                if (innerException != null)
                {
                    message += ": " + innerException.toString();
                }
            }

            return message;
        }
    }

    private MyDownloadProgressListener listener;
    private Object                     result;
    private boolean                    canceled;
    private int                        length   = 0;
    private int                        progress = 0;

    public MyDownloadTask(File downloadDir)
    {
        ApplicationContext.setFilesDir(downloadDir);
    }

    public File getDownloadDir()
    {
        // TODO:(pv) Don't download to private folder; get the phone's actual download directory
        return ApplicationContext.getFilesDir();
    }

    /**
     * Copied from Android source code .../frameworks/base/core/java/android/os/FileUtils.java 
     */
    private static class FileUtils
    {
        public static final int S_IRWXU = 00700;
        public static final int S_IRUSR = 00400;
        public static final int S_IWUSR = 00200;
        public static final int S_IXUSR = 00100;

        public static final int S_IRWXG = 00070;
        public static final int S_IRGRP = 00040;
        public static final int S_IWGRP = 00020;
        public static final int S_IXGRP = 00010;

        public static final int S_IRWXO = 00007;
        public static final int S_IROTH = 00004;
        public static final int S_IWOTH = 00002;
        public static final int S_IXOTH = 00001;

        /**
         * Copied from:
         *  http://code.google.com/p/nethack-android/source/browse/trunk/nethack-3.4.3/sys/android/NetHackApp/src/com/nethackff/NetHackFileHelpers.java
         * 
         * @param file
         * @param mode
         * @param uid
         * @param gid
         * @return 0 == success, not 0 == failure
         */
        public static int setPermissions(String file, int mode, int uid, int gid)
        {
            try
            {
                Class<?> fileUtils = Class.forName("android.os.FileUtils");
                Method setPermissions = fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
                int r = (Integer) setPermissions.invoke(null, file, mode, uid, gid);
                if (r != 0)
                {
                    MyLog.i(TAG, "android.os.FileUtils.setPermissions() returned " + r + " for '" + file
                                    + "', probably didn't work.");
                }
                return r;
            }
            catch (ClassNotFoundException e)
            {
                MyLog.i(TAG, "android.os.FileUtils.setPermissions() failed - ClassNotFoundException.");
            }
            catch (IllegalAccessException e)
            {
                MyLog.i(TAG, "android.os.FileUtils.setPermissions() failed - IllegalAccessException.");
            }
            catch (InvocationTargetException e)
            {
                MyLog.i(TAG, "android.os.FileUtils.setPermissions() failed - InvocationTargetException.");
            }
            catch (NoSuchMethodException e)
            {
                MyLog.i(TAG, "android.os.FileUtils.setPermissions() failed - NoSuchMethodException.");
            }
            return -1;
        }
    }

    /**
     * Copied from Android source code ApplicationContext.java
     */
    static class ApplicationContext
    {
        private static File mFilesDir;

        private static void setFilesDir(File filesDir)
        {
            mFilesDir = filesDir;
        }

        private static File getFilesDir()
        {
            return mFilesDir;
        }

        private static void setFilePermissionsFromMode(String name, int mode, int extraPermissions)
        {
            int perms = FileUtils.S_IRUSR | FileUtils.S_IWUSR | FileUtils.S_IRGRP | FileUtils.S_IWGRP | extraPermissions;
            if ((mode & Context.MODE_WORLD_READABLE) != 0)
            {
                perms |= FileUtils.S_IROTH;
            }
            if ((mode & Context.MODE_WORLD_WRITEABLE) != 0)
            {
                perms |= FileUtils.S_IWOTH;
            }
            if (false)
            {
                MyLog.i(TAG,
                                "File " + name + ": mode=0x" + Integer.toHexString(mode) + ", perms=0x"
                                                + Integer.toHexString(perms));
            }
            FileUtils.setPermissions(name, perms, -1, -1);
        }

        /**
         * Copied from Android source code ApplicationContext.java
         */
        private static File makeFilename(File base, String name)
        {
            if (name.indexOf(File.separatorChar) < 0)
            {
                return new File(base, name);
            }
            throw new IllegalArgumentException("File " + name + " contains a path separator");
        }

        /**
         * Copied from Android source code ApplicationContext.java
         */
        public static FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException
        {
            final boolean append = (mode & Context.MODE_APPEND) != 0;
            File f = makeFilename(getFilesDir(), name);
            try
            {
                FileOutputStream fos = new FileOutputStream(f, append);
                setFilePermissionsFromMode(f.getPath(), mode, 0);
                return fos;
            }
            catch (FileNotFoundException e)
            {
            }

            File parent = f.getParentFile();
            parent.mkdir();
            FileUtils.setPermissions(parent.getPath(), FileUtils.S_IRWXU | FileUtils.S_IRWXG | FileUtils.S_IXOTH, -1, -1);
            FileOutputStream fos = new FileOutputStream(f, append);
            setFilePermissionsFromMode(f.getPath(), mode, 0);
            return fos;
        }
    }

    public synchronized void setProgressTracker(MyDownloadProgressListener progressTracker)
    {
        listener = progressTracker;
        if (listener != null)
        {
            if (result == null)
            {
                //listener.onPreExecute();
                listener.onProgress(length, progress);
            }
            else
            {
                if (canceled)
                {
                    listener.onCancelled(result);
                }
                else
                {
                    listener.onPostExecute(result);
                }
            }
        }
    }

    @Override
    protected synchronized void onPreExecute()
    {
        if (listener != null)
        {
            listener.onPreExecute();
        }
    }

    @Override
    protected synchronized void onProgressUpdate(Integer... progress)
    {
        //this.length = progress[0];
        //this.progress = progress[1];
        if (listener != null)
        {
            listener.onProgress(this.length, this.progress);
        }
    }

    @Override
    protected Object doInBackground(MyUri... params)
    {
        try
        {
            MyUri uri = params[0];
            URL url = new URL(uri.toString());
            MyLog.i(TAG, "Downloading: " + url.toString());

            MyHttpConnectionPlatform conn = (MyHttpConnectionPlatform) MyConnection.open(uri);
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.connect();

            int responseCode = conn.getResponseCode();
            String responseMessage = conn.getResponseMessage();
            MyLog.d(TAG, "responseCode=" + responseCode + ", responseMessage=" + responseMessage);

            if (responseCode != HttpURLConnection.HTTP_OK)
            {
                throw new MyDownloadException(responseCode + ": \"" + responseMessage + "\"");
            }

            int lengthContent = conn.getContentLength();
            MyLog.d(TAG, "lengthContent=" + lengthContent);

            if (lengthContent != -1)
            {
                length = lengthContent;
                // Tollerable hack to allow background thread to re-display dialog w/ determinate max level
                publishProgress();//lengthContent, 0);
            }

            InputStream input = new BufferedInputStream(conn.openInputStream());

            String fileName = url.getFile();
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

            File filePath = new File(getDownloadDir(), fileName);

            FileOutputStream output = ApplicationContext.openFileOutput(fileName, Context.MODE_WORLD_READABLE);

            int count;
            byte data[] = new byte[8192];
            while ((count = input.read(data)) != -1)
            {
                progress += count;
                //WtcLog.info(TAG, "downloaded " + progress + " bytes");

                if (lengthContent != -1)
                {
                    publishProgress();//lengthContent, total);
                }
                output.write(data, 0, count);

                // DEBUG: Simulate slow download to help in debugging issues...
                //Thread.sleep(500);
            }

            output.flush();
            output.close();
            input.close();

            MyLog.d(TAG, "Downloaded " + filePath.getAbsolutePath());
            return filePath.getAbsolutePath();
        }
        catch (Exception e)
        {
            MyLog.e(TAG, "doInBackground", e);

            if (!(e instanceof MyDownloadException))
            {
                e = new MyDownloadException(e);
            }
            return e;
        }
    }

    @Override
    protected synchronized void onPostExecute(Object result)
    {
        this.result = result;
        if (listener != null)
        {
            listener.onPostExecute(this.result);
        }
        listener = null;
    }

    @Override
    protected synchronized void onCancelled(Object result)
    {
        if (canceled)
        {
            return;
        }

        canceled = true;

        this.result = result;
        if (listener != null)
        {
            listener.onCancelled(this.result);
        }
        listener = null;
    }
}