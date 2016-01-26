package io.github.blaeberry.filetreevisualizer;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    public static final String MAIN_TAG = "mAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fillFiles();

        if (getFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            getFragmentManager().beginTransaction().add
                    (R.id.fragment_container, new FileFinderFragment()).commit();
        } else {
            Log.d(MAIN_TAG, "Already created");
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    public void startTreeGeneration(String rootPath) {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, GenerateTreeFragment.newInstance(rootPath))
                .addToBackStack(null).commit();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() != 0) getFragmentManager().popBackStack();
        else super.onBackPressed();
    }

    public void fillFiles() {
        Log.e(MAIN_TAG, "ONLY FILL THIS ONCE YOU BASTARD!");

        String FILENAME = "hello_file";
        String string = "hello world!";
        String parentDirectory = getFilesDir().getPath();
        try {
            File subDir1 = new File(parentDirectory, "SubDir");
            subDir1.mkdirs();
            (new File(subDir1, "child1")).mkdirs();
            (new File(subDir1, "child2")).mkdirs();
            (new File(subDir1, "child3")).mkdirs();
            (new File(subDir1, "child4")).mkdirs();

            (new File(parentDirectory, "SubDir2")).mkdirs();

        } catch (Exception e) {
            Log.e(MAIN_TAG, "Failed to write files.");
        }
    }

    public class FocusChangeEvent {

    }
}

