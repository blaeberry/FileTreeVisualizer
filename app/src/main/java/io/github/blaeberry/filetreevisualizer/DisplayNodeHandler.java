package io.github.blaeberry.filetreevisualizer;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Evan on 2/6/2016.
 */
public class DisplayNodeHandler extends Handler {
    private final int MARGIN_SIZE = 30, CURVE_SIZE = 50, LINE_LENGTH = 300;
    public static final String ODD_SIBLINGS = "odd", EVEN_SIBLINGS_LEFT = "evenL",
            EVEN_SIBLINGS_RIGHT = "evenR", DISPLAY_TAG = "dTag";
    ProcessQueue processQueue;
    int idNum, animeOffset, numToAnime, animeTracker;
    LinkedList<DirectoryNode> animeOrder;
    RelativeLayout layout;

    public DisplayNodeHandler(Looper looper, ProcessQueue processQueue, RelativeLayout layout) {
        super(looper);
        this.processQueue = processQueue;
        this.layout = layout;
        idNum = 3230;
        animeOffset = numToAnime = animeTracker = 0;
    }

    @Override
    public void handleMessage(Message msg) {
        //TODO have directory node store row for cleaner code
        if(msg.what == ProcessQueue.STATUS_START || msg.what == ProcessQueue.STATUS_CONTINUE) {
            FileNodeContainer fnc = processQueue.poll();
            positionSiblings(fnc.getNode(), fnc.getRow(), fnc.getText(), 1.f);
            animeSiblings();
        }
        //continue UI thread operations
        else super.handleMessage(msg);
    }

    private void positionSiblings(DirectoryNode newNode, int row, String text, float proportion) {
        //this was changed to get context from layout
        DirectoryView dv = new DirectoryView(layout.getContext());
        dv.setText(text);
        dv.setRow(row);
        dv.setId(idNum++);

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
            //this was changed to get layout context
            lv = new LineView(layout.getContext(),
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
            animeOrder = new LinkedList<>();

            if (numDisplayedSiblings == 1) {
                nodeToArrange = displayedSiblings.remove(0);
                nodeToArrange.getDirectoryView().setLayoutParams
                        (calcAnchorParams(ODD_SIBLINGS, nodeToArrange));
                Log.d(MainActivity.MAIN_TAG, "Adding single: " + text);
                animeOrder.add(nodeToArrange);

            } else if (numDisplayedSiblings % 2 == 1) {
                int middlePos = numDisplayedSiblings / 2;
                nodeToArrange = displayedSiblings.remove(middlePos);
                nodeToArrange.getDirectoryView().setLayoutParams
                        (calcAnchorParams(ODD_SIBLINGS, nodeToArrange));
                animeOrder.add(nodeToArrange);

                addLeftSiblings(middlePos - 1, animeOrder, displayedSiblings);

                addRightSiblings(animeOrder, displayedSiblings);

                Log.d(MainActivity.MAIN_TAG, "Adding list of odd# siblings");

            } else {
                //number of sibling views (including self) is even, a little more tricky
                int middleLeftPos = (numDisplayedSiblings / 2) - 1;
                nodeToArrange = displayedSiblings.remove(middleLeftPos);
                nodeToArrange.getDirectoryView().setLayoutParams(
                        calcAnchorParams(EVEN_SIBLINGS_LEFT, nodeToArrange));
                animeOrder.add(nodeToArrange);

                addLeftSiblings(middleLeftPos - 1, animeOrder, displayedSiblings);

                nodeToArrange = displayedSiblings.remove(0);
                nodeToArrange.getDirectoryView().setLayoutParams(
                        calcAnchorParams(EVEN_SIBLINGS_RIGHT, nodeToArrange));
                animeOrder.addLast(nodeToArrange);

                addRightSiblings(animeOrder, displayedSiblings);
                Log.d(MainActivity.MAIN_TAG, "Adding list of odd# siblings");
            }

            //removed the anime part...

            Log.d("line1", "ADDING LINE");
            lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lv.setLayoutParams(lp);
            lv.setVisibility(View.INVISIBLE);
            layout.addView(lv, 0);
        }
        Log.d(MainActivity.MAIN_TAG, "All sibling views correctly positioned, time to animate! ");
    }

    private void animeSiblings() {

        if(animeOrder == null) {
            Log.d(MainActivity.MAIN_TAG, "Presumably the god node is being animated");

            //TODO repeated code...
            if(processQueue.peek() == null) {
                processQueue.processingViews = false;
            }

            else {
                Message startProcessing = DisplayNodeHandler.this.
                        obtainMessage(ProcessQueue.STATUS_CONTINUE);
                processQueue.processingViews = true;
                startProcessing.sendToTarget();
            }
            return;
        }

        if(animeOrder.size() == 0) {
            Log.e(MainActivity.MAIN_TAG, "No views to anime!");
            return;
        }
        numToAnime = animeOrder.size();
        DirectoryNode nodeToAnime;
        animeOffset = 0;

        for(int i = numToAnime; i > 0; i--) {
            nodeToAnime = animeOrder.remove();
            final String text = nodeToAnime.getDirectoryView().getText();
            DirectoryView directoryView = nodeToAnime.getDirectoryView();
            final DirectoryView parent = nodeToAnime.getParent().getDirectoryView();
            layout.removeView(directoryView);
            directoryView.setVisibility(View.INVISIBLE);
            layout.addView(directoryView);
            directoryView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    animeTracker++;
                    Log.d(MainActivity.MAIN_TAG, text + " layout changed, parentX: " + parent.getX()
                            + "| parentY: " + parent.getY() + "|parent name: " + parent.getText());
                    v.removeOnLayoutChangeListener(this);
                    AnimationSet budAnime = new AnimationSet(false);
                    ScaleAnimation scaleAnime = new ScaleAnimation(0, 1, 0, 1,
                            parent.getX(), parent.getY());
                    scaleAnime.setDuration(150);
                    scaleAnime.setInterpolator(new DecelerateInterpolator());
                    TranslateAnimation translateAnimation = new TranslateAnimation(parent.getX() -
                            v.getX(), 0, parent.getY() - v.getY(), 0);
                    translateAnimation.setDuration(150);
                    translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                    AlphaAnimation alphaAnime = new AlphaAnimation(0, 1);
                    alphaAnime.setDuration(150);
                    alphaAnime.setInterpolator(new LinearInterpolator());
                    budAnime.addAnimation(scaleAnime);
                    budAnime.addAnimation(translateAnimation);
                    budAnime.addAnimation(alphaAnime);
                    budAnime.setStartOffset(animeOffset);

                    //if last sibling to animate, add a delay before going on
                    if(animeTracker == numToAnime) {
                        ScaleAnimation stopAnime = new ScaleAnimation(1, 1, 1, 1,
                                parent.getX(), parent.getY());
                        stopAnime.setDuration(250);
                        stopAnime.setInterpolator(new DecelerateInterpolator());
                        stopAnime.setStartOffset(animeOffset + 150);
                        budAnime.addAnimation(stopAnime);
                    }

                        animeOffset += 150;
                    v.setVisibility(View.VISIBLE);
                    budAnime.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            //we are done animating all the siblings, time to move on
                            // (calling like this *should* prevent stack overflow)
                            if (--numToAnime == 0) {
                                Log.d(MainActivity.MAIN_TAG, "finished animating all siblings!");
                                animeTracker = 0;
                                if (processQueue.peek() == null) {
                                    processQueue.processingViews = false;
                                } else {
                                    Message startProcessing = DisplayNodeHandler.this.
                                            obtainMessage(ProcessQueue.STATUS_CONTINUE);
                                    processQueue.processingViews = true;
                                    startProcessing.sendToTarget();
                                }
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    v.startAnimation(budAnime);
                    ((DirectoryView) v).wrapperNode.getLine().setVisibility(View.VISIBLE);
                    ((DirectoryView) v).wrapperNode.getLine().startAnimation(budAnime);
                }
            });
        }
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
}
