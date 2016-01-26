package io.github.blaeberry.filetreevisualizer;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Evan on 1/22/2016.
 */
public class GenerateTreeFragment extends Fragment {
    public static final String PATH_KEY = "original_path", ODD_SIBLINGS = "odd",
            EVEN_SIBLINGS_LEFT = "evenL", EVEN_SIBLINGS_RIGHT = "evenR";
    private String rootPath = "TEST NO PASS";
    private RelativeLayout layout;
    private int idNum = 3001;
    private final int MARGIN_SIZE = 30;

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
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(3000,3000);
        layout.setLayoutParams(lp);
        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO find better spot for this for configuration changes
        new PerformTreeGeneration().execute(rootPath);
    }

    private class PerformTreeGeneration extends
            AsyncTask<String, PerformTreeGeneration.DirectoryContainer, Void> {

        private boolean viewAdded = true;

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
                    (new File(path), new DirectoryNode(null, null), 1);
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
                        Log.d(MainActivity.MAIN_TAG, "child file: " + childFile.getName());
                        childNode = new DirectoryNode(currentNode, null);
                        currentNode.addChild(childNode);
                        directories.add(new FileNodeContainer(childFile, childNode,
                                currentRow + 1));
                    }
                }
                String text = currentFile.getName();
                //TODO probably custom thread with synchronization (or redo algo so it can be async)
                while (!viewAdded) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Log.e(MainActivity.MAIN_TAG, "Background Thread interrupted!");
                    }
                }
                publishProgress(new DirectoryContainer(currentNode, currentRow, text, 1.f));
                viewAdded = false;
            }
//                }
            return null;
        }

        //needs to refresh all children views (later needs to move parent on collision)
        @Override
        protected void onProgressUpdate(DirectoryContainer... containers) {
            DirectoryView dv = new DirectoryView(getActivity());
            dv.setText(containers[0].text);
            dv.setId(idNum++);

            DirectoryNode newNode = containers[0].node;
            newNode.setContents(dv);
            int row = containers[0].row;

            List<DirectoryNode> siblings = new ArrayList<>();
            List<DirectoryNode> displayedSiblings = new ArrayList<>();
            displayedSiblings.add(newNode);

            //TODO very inefficient, should make so that I have a map of all displayed nodes
            Boolean godNode = newNode.getParent() == null;
            if (godNode)
                Log.d(MainActivity.MAIN_TAG, "found GodNode!");
            if (!godNode)
                siblings = newNode.getParent().getChildren();
            for (DirectoryNode sibling : siblings) {
                if (sibling.getContents() != null) {
                    if (sibling.getContents() != dv) {
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
                newNode.getContents().setLayoutParams(lp);
                Log.d(MainActivity.MAIN_TAG, "Adding godNode to Layout!");
                layout.addView(newNode.getContents());
            } else {
                int numDisplayedSiblings = displayedSiblings.size();
                DirectoryNode nodeToArrange;

                if (numDisplayedSiblings == 1) {
                    nodeToArrange = displayedSiblings.remove(0);
                    lp.addRule(RelativeLayout.BELOW,
                            nodeToArrange.getParent().getContents().getId());
                    lp.addRule(RelativeLayout.ALIGN_LEFT,
                            nodeToArrange.getParent().getContents().getId());
                    nodeToArrange.getContents().setLayoutParams(lp);
                    Log.d(MainActivity.MAIN_TAG, "Adding single: " + containers[0].text);
                    layout.addView(nodeToArrange.getContents());

                } else if (numDisplayedSiblings % 2 == 1) {
                    LinkedList<DirectoryNode> displayOrder = new LinkedList<>();
                    int middlePos = numDisplayedSiblings / 2;
                    nodeToArrange = displayedSiblings.remove(middlePos);
                    lp.addRule(RelativeLayout.BELOW,
                            nodeToArrange.getParent().getContents().getId());
                    lp.addRule(RelativeLayout.ALIGN_LEFT,
                            nodeToArrange.getParent().getContents().getId());
                    nodeToArrange.getContents().setLayoutParams(lp);
                    displayOrder.add(nodeToArrange);

                    //Set params of all nodes left of anchoring node and keep sibling order
                    for (int i = middlePos - 1; i >= 0; i--) {
                        nodeToArrange = displayedSiblings.remove(i);
                        int rightNeighborId = displayOrder.getFirst().getContents().getId();
                        displayOrder.addFirst(nodeToArrange);

                        lp = new RelativeLayout.LayoutParams
                                (RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                        lp.addRule(RelativeLayout.ALIGN_TOP, rightNeighborId);
                        lp.addRule(RelativeLayout.LEFT_OF, rightNeighborId);
                        nodeToArrange.getContents().setLayoutParams(lp);
                    }

                    //Same as above for all nodes to the right of anchoring node
                    while (!displayedSiblings.isEmpty()) {
                        nodeToArrange = displayedSiblings.remove(0);
                        int leftNeighborId = displayOrder.getLast().getContents().getId();
                        displayOrder.addLast(nodeToArrange);

                        lp = new RelativeLayout.LayoutParams
                                (RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                        lp.addRule(RelativeLayout.ALIGN_TOP, leftNeighborId);
                        lp.addRule(RelativeLayout.RIGHT_OF, leftNeighborId);
                        nodeToArrange.getContents().setLayoutParams(lp);
                    }


                    //Display all siblings in correct order
                    while (!displayOrder.isEmpty()) {
                        layout.removeView(displayOrder.getFirst().getContents());
                        layout.addView(displayOrder.removeFirst().getContents());
                    }

                } else {
                    //number of sibling views (including self) is even, a little more tricky
                    LinkedList<DirectoryNode> displayOrder = new LinkedList<>();
                    int middleLeftPos = (numDisplayedSiblings / 2) - 1;
                    nodeToArrange = displayedSiblings.remove(middleLeftPos);
                    lp.addRule(RelativeLayout.BELOW,
                            nodeToArrange.getParent().getContents().getId());
                    lp.addRule(RelativeLayout.ALIGN_RIGHT,
                            nodeToArrange.getParent().getContents().getId());
                    lp.setMargins(0, 0, (int) ((MARGIN_SIZE + DirectoryView.MAX_SIZE) / 2), 0);
                    nodeToArrange.getContents().setLayoutParams(lp);
                    displayOrder.add(nodeToArrange);

                    //TODO make some of these into functions, a lot of copy-paste

                    for (int i = middleLeftPos - 1; i >= 0; i--) {
                        nodeToArrange = displayedSiblings.remove(i);
                        int rightNeighborId = displayOrder.getFirst().getContents().getId();
                        displayOrder.addFirst(nodeToArrange);

                        lp = new RelativeLayout.LayoutParams
                                (RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                        lp.addRule(RelativeLayout.ALIGN_TOP, rightNeighborId);
                        lp.addRule(RelativeLayout.LEFT_OF, rightNeighborId);
                        nodeToArrange.getContents().setLayoutParams(lp);
                    }

                    int middleRightPos = (numDisplayedSiblings / 2);
                    nodeToArrange = displayedSiblings.remove(0);
                    lp.addRule(RelativeLayout.BELOW,
                            nodeToArrange.getParent().getContents().getId());
                    lp.addRule(RelativeLayout.ALIGN_LEFT,
                            nodeToArrange.getParent().getContents().getId());
                    lp.setMargins((int) ((MARGIN_SIZE + DirectoryView.MAX_SIZE) / 2), 0, 0, 0);
                    nodeToArrange.getContents().setLayoutParams(lp);
                    displayOrder.addLast(nodeToArrange);

                    while (!displayedSiblings.isEmpty()) {
                        nodeToArrange = displayedSiblings.remove(0);
                        int leftNeighborId = displayOrder.getLast().getContents().getId();
                        displayOrder.addLast(nodeToArrange);

                        lp = new RelativeLayout.LayoutParams
                                (RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                        lp.addRule(RelativeLayout.ALIGN_TOP, leftNeighborId);
                        lp.addRule(RelativeLayout.RIGHT_OF, leftNeighborId);
                        nodeToArrange.getContents().setLayoutParams(lp);
                    }

                    while (!displayOrder.isEmpty()) {
                        layout.removeView(displayOrder.getFirst().getContents());
                        layout.addView(displayOrder.removeFirst().getContents());
                    }
                }
            }

            viewAdded = true;
        }

//        private RelativeLayout.LayoutParams calcAnchorParams(String type, DirectoryNode nodeToArrange) {
//            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams
//                    (RelativeLayout.LayoutParams.WRAP_CONTENT,
//                            RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//            lp.addRule(RelativeLayout.BELOW,
//                    nodeToArrange.getParent().getContents().getId());
//            switch(type) {
//                case ODD_SIBLINGS:
//                    lp.addRule(RelativeLayout.ALIGN_LEFT,
//                            nodeToArrange.getParent().getContents().getId());
//                    return lp;
//                case EVEN_SIBLINGS_LEFT:
//                    lp.addRule(RelativeLayout.ALIGN_RIGHT,
//                            nodeToArrange.getParent().getContents().getId());
//                    lp.setMargins(0, 0, (int) ((MARGIN_SIZE + DirectoryView.MAX_SIZE) / 2), 0);
//                    return lp;
//                case EVEN_SIBLINGS_RIGHT:
//                    lp.addRule(RelativeLayout.ALIGN_LEFT,
//                            nodeToArrange.getParent().getContents().getId());
//                    lp.setMargins((int) ((MARGIN_SIZE + DirectoryView.MAX_SIZE) / 2), 0, 0, 0);
//                    return lp;
//                default: return null;
//            }
//        }
//
//        private void addLeftSiblings(int startPos, ) {
//            for (int i = startPos - 1; i >= 0; i--) {
//                nodeToArrange = displayedSiblings.remove(i);
//                int rightNeighborId = displayOrder.getFirst().getContents().getId();
//                displayOrder.addFirst(nodeToArrange);
//
//                lp = new RelativeLayout.LayoutParams
//                        (RelativeLayout.LayoutParams.WRAP_CONTENT,
//                                RelativeLayout.LayoutParams.WRAP_CONTENT);
//                lp.addRule(RelativeLayout.ALIGN_TOP, rightNeighborId);
//                lp.addRule(RelativeLayout.LEFT_OF, rightNeighborId);
//                nodeToArrange.getContents().setLayoutParams(lp);
//            }
//        }

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
