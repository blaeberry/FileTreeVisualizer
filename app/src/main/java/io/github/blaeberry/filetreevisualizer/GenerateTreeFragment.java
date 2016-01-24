package io.github.blaeberry.filetreevisualizer;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Evan on 1/22/2016.
 */
public class GenerateTreeFragment extends Fragment {
    public static final String PATH_KEY = "original_path";
    private String rootPath = "TEST NO PASS";
    private RelativeLayout layout;

    public static GenerateTreeFragment newInstance(String path) {
        //Log.d(MainActivity.MAIN_TAG, "PAth: " + path);
        Bundle args = new Bundle();
        args.putString(PATH_KEY, path);

        GenerateTreeFragment fragment = new GenerateTreeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootPath = getArguments().getString(PATH_KEY, null);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView
            (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = (RelativeLayout)inflater.inflate(R.layout.generate_tree, container, false);
        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new performTreeGeneration().execute(rootPath);

    }

    private class performTreeGeneration extends
            AsyncTask<String, performTreeGeneration.DirectoryContainer, Void> {

        @Override
        protected Void doInBackground(String... params) {
            if(params.length == 0 || params[0] == null || params[0].length() == 0) {
                Log.d(MainActivity.MAIN_TAG, "Passed path is null");
                return null;
            }
            String path = params[0];
            File currentDirectory = new File(path);
            Queue<File> directories = new LinkedList<>();
            File[] children;
            directories.add(currentDirectory);
            while(!directories.isEmpty()) {
                currentDirectory = directories.remove();
                children = currentDirectory.listFiles();
                if(children.length != 0)
                    directories.addAll(Arrays.asList(children));
                //Generate view from currentDirectory. Also: need to keep up with row for coloring
            }

            return null;

//            String text = currentDirectory.getName();
//            publishProgress(new DirectoryContainer(1, text, 1.f));
//
//
//            File currentDirectory = new File(path);
//            directories.add(currentDirectory);
        }

        @Override
        protected void onProgressUpdate(DirectoryContainer... values) {
            DirectoryView dv = new DirectoryView(getActivity());
            dv.setText(values[0].text);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams
                    (RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            dv.setLayoutParams(lp);
            layout.addView(dv);
        }

        private RelativeLayout.LayoutParams determineNodeLocation(DirectoryNode dn) {
            DirectoryView dv = dn.getContents();
            RelativeLayout.LayoutParams lp =
                    (RelativeLayout.LayoutParams) dv.getLayoutParams();
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            //TODO convert to dp
            lp.setMargins(0, 30, 0, 0);
            if (dn.getParent() != null) lp.addRule(RelativeLayout.BELOW, dv.getId());
            return lp;
        }

        public class DirectoryContainer {
            int row;
            String text;
            float proportion;

            public DirectoryContainer(int row, String text, float proportion) {
                this.row = row;
                this.text = text;
                this.proportion = proportion;
            }
        }
    }
}
