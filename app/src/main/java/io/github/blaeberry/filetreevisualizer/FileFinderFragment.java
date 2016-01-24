package io.github.blaeberry.filetreevisualizer;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.devpaul.filepickerlibrary.FilePickerActivity;

import java.io.File;

/**
 * Created by Evan on 1/22/2016.
 */

//TODO make sure the use of 'FilePickerLibrary' abides by the Apache License 2.0
public class FileFinderFragment extends Fragment implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView
            (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.file_finder, container, false);

        Button finderButton = (Button) v.findViewById(R.id.finder_button);
        finderButton.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finder_button:
                String s = getActivity().getFilesDir().getPath();
                ((MainActivity)getActivity()).startTreeGeneration(s);
//                Environment.getExternalStorageDirectory().mkdirs();
//                Intent filePickerIntent = new Intent(getActivity(), FilePickerActivity.class);
//                //filePickerIntent.putExtra(FilePickerActivity.THEME_TYPE, ThemeType.DIALOG);
//                filePickerIntent.putExtra(FilePickerActivity.REQUEST_CODE,
//                        FilePickerActivity.REQUEST_DIRECTORY);
//                startActivityForResult(filePickerIntent, FilePickerActivity.REQUEST_DIRECTORY);
                break;
            default:
                Log.e(MainActivity.MAIN_TAG, "Shouldn't be here. (onClick in FFFrag");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FilePickerActivity.REQUEST_DIRECTORY
                && resultCode == FilePickerActivity.RESULT_OK) {
            String rootPath = data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);
            if (rootPath != null) ((MainActivity)getActivity()).startTreeGeneration(rootPath);
            else Toast.makeText(getActivity(), "Path does not exist!", Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
