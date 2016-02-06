package io.github.blaeberry.filetreevisualizer;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Evan on 1/22/2016.
 */

//TODO switch to android.animation for less lag
//TODO BIGGEST TODO: go thru steps for generation then make into thread class
public class GenerateTreeFragment extends ScrollFragment {
    public static final String PATH_KEY = "original_path", ODD_SIBLINGS = "odd",
            EVEN_SIBLINGS_LEFT = "evenL", EVEN_SIBLINGS_RIGHT = "evenR";
    private String rootPath = "TEST NO PASS";
    private RelativeLayout layout;
    private int idNum = 3001, offset = 0;
    private final int MARGIN_SIZE = 30, CURVE_SIZE = 50, LINE_LENGTH = 300;

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
        layout = (RelativeLayout) inflater.inflate(R.layout.generate_tree, container, false);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(3000, 6000);
        layout.setLayoutParams(lp);

        layout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        //TODO these might cause some sort of problem on config change
                        layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        GenerateTreeFragment.this.setupScroll(layout);
                        Log.wtf(MainActivity.MAIN_TAG, "width: " + layout.getWidth()
                                + "|height:  " + layout.getHeight());
                        new PerformTreeGeneration().execute(rootPath);

                    }
                });
        return layout;
    }


    private class PerformTreeGeneration extends
            AsyncTask<String, PerformTreeGeneration.DirectoryContainer, Void> {

        private boolean addingView = false;
        FileNodeContainer GodFileNodeContainer;

        @Override
        protected Void doInBackground(String... params) {
            Log.d(MainActivity.MAIN_TAG, "running do in background");
            if (params.length == 0 || params[0] == null || params[0].length() == 0) {
                Log.d(MainActivity.MAIN_TAG, "Passed path is null");
                return null;
            }

            long totalSize = 0, fileSize;
            float proportion;

            String path = params[0];
            GodFileNodeContainer = new FileNodeContainer
                    (new File(path), new DirectoryNode(null, null), 0);
            FileNodeContainer currentFileNodeContainer = GodFileNodeContainer;
            Queue<FileNodeContainer> directories = new LinkedList<>();
            File currentFile;
            DirectoryNode currentNode;
            int currentRow;
            File[] childFiles;

            directories.add(currentFileNodeContainer);

            while (!directories.isEmpty()) {
                currentFileNodeContainer = directories.remove();
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
                        directories.add(new FileNodeContainer(childFile, childNode,
                                currentRow + 1));
                    }
                }
                String text = currentFile.getName();
                Log.d(MainActivity.MAIN_TAG, text + " is now waiting for previous view to be added");
                //TODO probably custom thread with synchronization (or redo algo so it can be async)
                while (addingView) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Log.e(MainActivity.MAIN_TAG, "Background Thread interrupted!");
                    }
                }
                Log.d(MainActivity.MAIN_TAG, "Going to make view from: " + text);
                publishProgress(new DirectoryContainer(currentNode, currentRow, text, 1.f));
            }
//                }
            return null;
        }

        //TODO move all views on collision
        @Override
        protected void onProgressUpdate(DirectoryContainer... containers) {
            addingView = true;

            DirectoryView dv = new DirectoryView(getActivity());
            dv.setText(containers[0].text);
            dv.setRow(containers[0].row);
            dv.setId(idNum++);

            DirectoryNode newNode = containers[0].node;
            newNode.setDirectoryView(dv);
            dv.wrapperNode = newNode;

            List<DirectoryNode> siblings = new ArrayList<>();
            List<DirectoryNode> displayedSiblings = new ArrayList<>();
            displayedSiblings.add(newNode);

            LineView lv = null;

            //TODO very inefficient, should make so that I have a map of all displayed nodes
            Boolean godNode = newNode.getParent() == null;
            if (godNode) {
                Log.d(MainActivity.MAIN_TAG, "View to make is GodNode");
            } else {
                siblings = newNode.getParent().getChildren();
                //Attach a line to this new view and its parent
                lv = new LineView(getActivity(),
                        newNode.getParent().getDirectoryView(), newNode.getDirectoryView());
                newNode.setLine(lv);
            }
            for (DirectoryNode sibling : siblings) {
                if (sibling.getDirectoryView() != null) {
                    if (sibling.getDirectoryView() != dv) {
                        displayedSiblings.add(sibling);
                    } else {
                        Log.d(MainActivity.MAIN_TAG, "found view to be added in list of children");
                    }
                }
            }

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams
                    (RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (godNode) {
                lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                newNode.getDirectoryView().setLayoutParams(lp);
                Log.d(MainActivity.MAIN_TAG, "Adding godNode to Layout.");
                layout.addView(newNode.getDirectoryView());
            } else {
                int numDisplayedSiblings = displayedSiblings.size();
                DirectoryNode nodeToArrange;
                LinkedList<DirectoryNode> displayOrder = new LinkedList<>();

                if (numDisplayedSiblings == 1) {
                    nodeToArrange = displayedSiblings.remove(0);
                    nodeToArrange.getDirectoryView().setLayoutParams
                            (calcAnchorParams(ODD_SIBLINGS, nodeToArrange));
                    Log.d(MainActivity.MAIN_TAG, "Adding single: " + containers[0].text);
                    displayOrder.add(nodeToArrange);

                } else if (numDisplayedSiblings % 2 == 1) {
                    int middlePos = numDisplayedSiblings / 2;
                    nodeToArrange = displayedSiblings.remove(middlePos);
                    nodeToArrange.getDirectoryView().setLayoutParams
                            (calcAnchorParams(ODD_SIBLINGS, nodeToArrange));
                    displayOrder.add(nodeToArrange);

                    addLeftSiblings(middlePos - 1, displayOrder, displayedSiblings);

                    addRightSiblings(displayOrder, displayedSiblings);

                    Log.d(MainActivity.MAIN_TAG, "Adding list of odd# siblings");

                } else {
                    //number of sibling views (including self) is even, a little more tricky
                    int middleLeftPos = (numDisplayedSiblings / 2) - 1;
                    nodeToArrange = displayedSiblings.remove(middleLeftPos);
                    nodeToArrange.getDirectoryView().setLayoutParams(
                            calcAnchorParams(EVEN_SIBLINGS_LEFT, nodeToArrange));
                    displayOrder.add(nodeToArrange);

                    addLeftSiblings(middleLeftPos - 1, displayOrder, displayedSiblings);

                    nodeToArrange = displayedSiblings.remove(0);
                    nodeToArrange.getDirectoryView().setLayoutParams(
                            calcAnchorParams(EVEN_SIBLINGS_RIGHT, nodeToArrange));
                    displayOrder.addLast(nodeToArrange);

                    addRightSiblings(displayOrder, displayedSiblings);
                    Log.d(MainActivity.MAIN_TAG, "Adding list of odd# siblings");
                }
                //Display all siblings in correct order, first adding lines to each view
                DirectoryNode nodeToShow;
                while (!displayOrder.isEmpty()) {
                    nodeToShow = displayOrder.removeFirst();
                    addViewToAnime(layout, nodeToShow, offset);
                    offset += 500;
                    Log.d(MainActivity.MAIN_TAG, nodeToShow.getDirectoryView().getText() +
                            " listening for layout change");
                }
                Log.d("line1", "ADDING LINE");
                lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                lv.setLayoutParams(lp);
                lv.setVisibility(View.INVISIBLE);
                layout.addView(lv, 0);
            }
            Log.d(MainActivity.MAIN_TAG, "View added, can pass in next view.");
            addingView = false;
        }

        private RelativeLayout.LayoutParams calcAnchorParams(String type, DirectoryNode nodeToArrange) {
            int nodeSplit = (int) ((MARGIN_SIZE + DirectoryView.MAX_SIZE) / 2);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams
                    (RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.BELOW,
                    nodeToArrange.getParent().getDirectoryView().getId());
            switch (type) {
                case ODD_SIBLINGS:
                    lp.addRule(RelativeLayout.ALIGN_LEFT,
                            nodeToArrange.getParent().getDirectoryView().getId());
                    lp.setMargins(0, LINE_LENGTH, 0, 0);
                    return lp;
                case EVEN_SIBLINGS_LEFT:
                    lp.addRule(RelativeLayout.ALIGN_RIGHT,
                            nodeToArrange.getParent().getDirectoryView().getId());
                    lp.setMargins(0, LINE_LENGTH, nodeSplit, 0);
                    return lp;
                case EVEN_SIBLINGS_RIGHT:
                    lp.addRule(RelativeLayout.ALIGN_LEFT,
                            nodeToArrange.getParent().getDirectoryView().getId());
                    lp.setMargins(nodeSplit, LINE_LENGTH, 0, 0);
                    return lp;
                default:
                    return null;
            }
        }

        private void addLeftSiblings(int startPos, LinkedList<DirectoryNode> displayOrder,
                                     List<DirectoryNode> displayedSiblings) {
            DirectoryNode nodeToArrange;
            RelativeLayout.LayoutParams lp;
            int rightNeighborId;
            for (int i = startPos; i >= 0; i--) {
                nodeToArrange = displayedSiblings.remove(i);
                rightNeighborId = displayOrder.getFirst().getDirectoryView().getId();
                displayOrder.addFirst(nodeToArrange);
                lp = new RelativeLayout.LayoutParams
                        (RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.ALIGN_BOTTOM, rightNeighborId);
                lp.addRule(RelativeLayout.LEFT_OF, rightNeighborId);
                lp.setMargins(0, 0, MARGIN_SIZE, CURVE_SIZE);
                nodeToArrange.getDirectoryView().setLayoutParams(lp);
            }
        }

        private void addRightSiblings(LinkedList<DirectoryNode> displayOrder,
                                      List<DirectoryNode> displayedSiblings) {
            DirectoryNode nodeToArrange;
            RelativeLayout.LayoutParams lp;
            int leftNeighborId;
            while (!displayedSiblings.isEmpty()) {
                nodeToArrange = displayedSiblings.remove(0);
                leftNeighborId = displayOrder.getLast().getDirectoryView().getId();
                displayOrder.addLast(nodeToArrange);
                lp = new RelativeLayout.LayoutParams
                        (RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.ALIGN_BOTTOM, leftNeighborId);
                lp.addRule(RelativeLayout.RIGHT_OF, leftNeighborId);
                lp.setMargins(MARGIN_SIZE, 0, 0, CURVE_SIZE);
                nodeToArrange.getDirectoryView().setLayoutParams(lp);
            }
        }

        private void addViewToAnime(RelativeLayout layout, DirectoryNode node, final int offset) {
            final String text = node.getDirectoryView().getText();
            DirectoryView directoryView = node.getDirectoryView();
            final DirectoryView parent = node.getParent().getDirectoryView();
            layout.removeView(directoryView);
            directoryView.setVisibility(View.INVISIBLE);
            layout.addView(directoryView);
            directoryView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    Log.d(MainActivity.MAIN_TAG, text + " layout changed, parentX: " + parent.getX()
                            + "| parentY: " + parent.getY() + "|parent name: " + parent.getText());
                    v.removeOnLayoutChangeListener(this);
                    AnimationSet budAnime = new AnimationSet(false);
                    ScaleAnimation scaleAnime = new ScaleAnimation(0, 1, 0, 1,
                            parent.getX(), parent.getY());
                    scaleAnime.setDuration(500);
                    scaleAnime.setInterpolator(new DecelerateInterpolator());
                    TranslateAnimation translateAnimation = new TranslateAnimation(parent.getX() -
                            v.getX(), 0, parent.getY() - v.getY(), 0);
                    translateAnimation.setDuration(500);
                    translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                    AlphaAnimation alphaAnime = new AlphaAnimation(0, 1);
                    alphaAnime.setDuration(500);
                    alphaAnime.setInterpolator(new LinearInterpolator());
                    budAnime.addAnimation(scaleAnime);
                    budAnime.addAnimation(translateAnimation);
                    budAnime.addAnimation(alphaAnime);
                    budAnime.setStartOffset(offset);
                    v.setVisibility(View.VISIBLE);
                    v.startAnimation(budAnime);
                    ((DirectoryView)v).wrapperNode.getLine().setVisibility(View.VISIBLE);
                    ((DirectoryView)v).wrapperNode.getLine().startAnimation(budAnime);
                }
            });
        }

        public class DirectoryContainer {
            DirectoryNode node;
            int row;
            String text;
            float proportion;

            public DirectoryContainer(DirectoryNode node, int row, String text, float proportion) {
                this.node = node;
                this.row = row;
                this.text = text;
                this.proportion = proportion;
            }
        }

        public class FileNodeContainer {
            private File file;
            private DirectoryNode node;
            private int row;

            public FileNodeContainer() {
            }

            public FileNodeContainer(File file, DirectoryNode node, int row) {
                this.file = file;
                this.node = node;
                this.row = row;
            }

            public void setFile(File file) {
                this.file = file;
            }

            public void setNode(DirectoryNode node) {
                this.node = node;
            }

            public void setRow(int row) {
                this.row = row;
            }

            public File getFile() {
                return file;
            }

            public DirectoryNode getNode() {
                return node;
            }

            public int getRow() {
                return row;
            }
        }
    }
}
