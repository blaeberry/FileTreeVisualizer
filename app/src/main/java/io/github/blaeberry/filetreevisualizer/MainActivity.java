package io.github.blaeberry.filetreevisualizer;

import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String MAIN_TAG = "mAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            getFragmentManager().beginTransaction().add
                    (R.id.fragment_container, new FileFinderFragment()).commit();
        } else {
            Log.d(MAIN_TAG, "Already created");
        }
    }

    public void startTreeGeneration(String rootPath) {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, GenerateTreeFragment.newInstance(rootPath))
                .addToBackStack(null).commit();
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() != 0) getFragmentManager().popBackStack();
        else super.onBackPressed();
    }
}
