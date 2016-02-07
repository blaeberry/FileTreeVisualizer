package io.github.blaeberry.filetreevisualizer;

import android.os.Bundle;
import android.os.Process;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

/**
 * Created by Evan on 1/22/2016.
 */

//TODO switch to android.animation for less lag
public class GenerateTreeFragment extends ScrollFragment {
    public static final String ORIGINAL_PATH_KEY = "original_path";
    private String rootPath = "TEST, NO PATH";
    private RelativeLayout layout;
    private DisplayNodeHandler handler;
    private ProcessQueue processQueue;

    public static GenerateTreeFragment newInstance(String path) {
        //Log.d(MainActivity.MAIN_TAG, "Path: " + path);
        Bundle args = new Bundle();
        args.putString(ORIGINAL_PATH_KEY, path);

        GenerateTreeFragment fragment = new GenerateTreeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootPath = getArguments().getString(ORIGINAL_PATH_KEY, null);
        //setRetainInstance(true);
    }

    @Override
    public View onCreateView
            (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = (RelativeLayout) inflater.inflate(R.layout.generate_tree, container, false);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(3000, 6000);
        layout.setLayoutParams(lp);

        processQueue = new ProcessQueue();
        handler = new DisplayNodeHandler(Looper.getMainLooper(), processQueue, layout);
        processQueue.setHandler(handler);

        //after the layout has been created and is ready to add child views
        layout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        //TODO these might cause some sort of problem on config change
                        layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        GenerateTreeFragment.this.setupScroll(layout);
                        ReadFilesToQueue rftq = new ReadFilesToQueue(rootPath, processQueue);
                        rftq.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                        rftq.start();
                    }
                });
        return layout;
    }
}
