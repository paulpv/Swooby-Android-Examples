package com.example.android.threading;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.threading.actionbar.ActionBarActivity;
import com.example.android.threading.utils.MyUriPlatform;

public class MainActivity extends ActionBarActivity
{
    private MyDownloader updater;

    /**
     * Retain the updater state when rotating screen.
     */
    @Override
    public Object onRetainNonConfigurationInstance()
    {
        if (isFinishing() || updater == null)
        {
            return null;
        }

        updater.detach();
        return updater;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Object temp = getLastNonConfigurationInstance();
        if (temp instanceof MyDownloader)
        {
            updater = (MyDownloader) temp;
        }

        setContentView(R.layout.main);

        EditText editTextUrl = (EditText) findViewById(R.id.editTextUrl);
        editTextUrl.setText("http://dl.google.com/android/installer_r16-windows.exe");

        findViewById(R.id.buttonDownload).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (updater != null)
                {
                    String uriString = ((EditText) findViewById(R.id.editTextUrl)).getText().toString();
                    MyUriPlatform uri = MyUriPlatform.parse(uriString);
                    updater.start(uri);
                }
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (updater == null)
        {
            updater = MyDownloader.get();
            if (updater == null)
            {
                updater = MyDownloader.create();
            }
        }
        updater.attach(this);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args)
    {
        if (updater != null)
        {
            Dialog dialog = updater.onCreateDialog(id, args);
            if (dialog != null)
            {
                return dialog;
            }
        }

        return super.onCreateDialog(id, args);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_refresh:
                Toast.makeText(this, "Fake refreshing...", Toast.LENGTH_SHORT).show();
                getActionBarHelper().setRefreshActionItemState(true);
                getWindow().getDecorView().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        getActionBarHelper().setRefreshActionItemState(false);
                    }
                }, 1000);
                break;

            case R.id.menu_search:
                Toast.makeText(this, "Tapped search", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_share:
                Toast.makeText(this, "Tapped share", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
