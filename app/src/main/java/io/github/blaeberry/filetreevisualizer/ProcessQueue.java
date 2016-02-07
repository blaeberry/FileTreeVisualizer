package io.github.blaeberry.filetreevisualizer;

import android.os.Message;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Evan on 2/6/2016.
 */
public class ProcessQueue {
    private Queue<FileNodeContainer> viewsToBeProcessed = new LinkedList<>();
    DisplayNodeHandler handler = null;
    public final static int STATUS_START = 2310, STATUS_CONTINUE = 2311;
    public boolean processingViews = false;

    public ProcessQueue(){ }

    public ProcessQueue(DisplayNodeHandler handler) {
        this.handler = handler;
    }

    public void setHandler(DisplayNodeHandler handler) {
        this.handler = handler;
    }

    public synchronized void add(FileNodeContainer fnc) {
        viewsToBeProcessed.add(fnc);
        if(!processingViews) {
            processingViews = true;
            Message startProcessing = handler.obtainMessage(STATUS_START);
            startProcessing.sendToTarget();
        }
    }

    public synchronized FileNodeContainer poll() {
        return viewsToBeProcessed.poll();
    }

    public synchronized FileNodeContainer peek() {
        return viewsToBeProcessed.peek();
    }
}
