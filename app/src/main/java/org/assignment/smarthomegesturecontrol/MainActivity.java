package org.assignment.smarthomegesturecontrol;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import org.assignment.smarthomegesturecontrol.ui.main.MainFragment;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String gestureArry[] = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }

        gestureArry = getResources().getStringArray(R.array.gestures_arrays);
        Spinner spin = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, gestureArry);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setSelection(0);
        spin.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i("onItemSelected", gestureArry[position]);
        Log.i("onItemSelected", "id is " + id);

        if(0 != id) {
            goToPracticeScreen(id);
        } else {
            Log.i("gesture", "none of the gesture selected");
        }
    }

    private void goToPracticeScreen(long id) {
        Intent intent = new Intent(this, PracticeActivity.class);
        intent.putExtra("optionSelected", id) ;
        startActivity(intent);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}