package io.github.blaeberry.filetreevisualizer;

import android.util.Log;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Evan on 2/6/2016.
 * Reads through selected directory and sends the files to the ProcessQueue, breadth-first.
 */
public class ReadFilesToQueue extends Thread {
    String rootPath;
    ProcessQueue processQueue;

    public ReadFilesToQueue(String rootPath, ProcessQueue processQueue) {
        super();
        this.rootPath = rootPath;
        this.processQueue = processQueue;
    }

    public void run() {
        Log.d(MainActivity.MAIN_TAG, "running do in background");
        if (rootPath.length() == 0) {
            Log.e(MainActivity.MAIN_TAG, "Passed path is null");
            return;
        }

        FileNodeContainer GodFileNodeContainer;
        long totalSize = 0, fileSize;
        float proportion;

        GodFileNodeContainer = new FileNodeContainer
                (new File(rootPath), new DirectoryNode(null, null), 0);
        FileNodeContainer currentFileNodeContainer = GodFileNodeContainer;
        Queue<FileNodeContainer> directories = new LinkedList<>();
        File currentFile;
        DirectoryNode currentNode;
        int currentRow;
        File[] childFiles;

        directories.add(currentFileNodeContainer);

        while (!directories.isEmpty()) {
            currentFileNodeContainer = directories.remove();

            //as directories are read in this thread, they are pushed to the ProcessQueue
            processQueue.add(currentFileNodeContainer);

            currentFile = currentFileNodeContainer.getFile();
            currentNode = currentFileNodeContainer.getNode();
            currentRow = currentFileNodeContainer.getRow();
            Log.d(MainActivity.MAIN_TAG, "popped directories, path: " + currentFile.getPath());
            //TODO implement sizing
//                if (currentFile.isFile()) {
//                    fileSize = currentFile.length();
//                    totalSize += fileSize;
//                    //climb upward, adding size to all parents
//                    //update proportions of rest of tree
//                }
//                else {
            childFiles = currentFile.listFiles();
            DirectoryNode childNode;
            if (childFiles != null) {
                for (File childFile : childFiles) {
                    Log.d(MainActivity.MAIN_TAG, '\t' + "child file: " + childFile.getName());
                    childNode = new DirectoryNode(currentNode, null);
                    currentNode.addChild(childNode);

                    //'row' keeps up with the depth of the directory in the tree
                    directories.add(new FileNodeContainer(childFile, childNode,
                            currentRow + 1));
                }
            }
        }
//                }
    }
}
