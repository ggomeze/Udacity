package com.ggomeze.myappportfolio;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Called when the user touches a button */
    public void showToast(View view) {
        String application = new String();
        switch (view.getId()){
            case R.id.spotify_streamer:
                application = getString(R.string.spotify_streamer);
                break;
            case R.id.scores_app:
                application = getString(R.string.scores);
                break;
            case R.id.library_app:
                application = getString(R.string.library);
                break;
            case R.id.build_it_bigger:
                application = getString(R.string.build_it_bigger);
                break;
            case R.id.xyz_reader:
                application = getString(R.string.xyz_reader);
                break;
            case R.id.capstone_my_own_app:
                application = getString(R.string.capstone);
                break;
        }

        Toast.makeText(this, String.format(getString(R.string.toast_this_button_will_lunch), application.toLowerCase()),Toast.LENGTH_SHORT).show();
    }
}
