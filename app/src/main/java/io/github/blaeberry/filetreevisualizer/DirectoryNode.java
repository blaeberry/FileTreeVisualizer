package io.github.blaeberry.filetreevisualizer;

import android.graphics.Rect;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evan on 1/22/2016.
 */

//Not sure if nodes should be unidirectional or not yet...
public class DirectoryNode {
    private DirectoryNode parent;
    private DirectoryView directoryView;
    private LineView line;
    private List<DirectoryNode> children;
    private long totalStorage;

    public DirectoryNode(DirectoryNode parent, DirectoryView directoryView,
                         List<DirectoryNode> children) {
        this.parent = parent;
        this.directoryView = directoryView;
        this.children = children;

    }

    public DirectoryNode(DirectoryNode parent, DirectoryView directoryView) {
        this.parent = parent;
        this.directoryView = directoryView;
        this.children = new ArrayList<>();
    }

    public DirectoryNode addChild(DirectoryNode node) {
        children.add(node);
        return node;
    }

    public DirectoryNode addChildAt(int i, DirectoryNode node) {
        children.add(i, node);
        return node;
    }

    public DirectoryNode removeChildAt(int i) {
        return children.remove(i);
    }

    public void clearChildren() {
        children.clear();
    }

    public void setDirectoryView(DirectoryView directoryView) {
        this.directoryView = directoryView;
    }

    public void setChildren(List<DirectoryNode> children) {
        this.children = children;
    }

    public void setTotalStorage(long totalStorage) {
        this.totalStorage = totalStorage;
    }

    public void setLine(LineView line) {
        this.line = line;
    }

    public DirectoryNode getParent() {
        return parent;
    }

    public DirectoryView getDirectoryView() {
        return directoryView;
    }

    public int getNumChildren() {
        return children.size();
    }

    public DirectoryNode getChildAt(int i) {
        return children.get(i);
    }

    public List<DirectoryNode> getChildren() {
        return children;
    }

    public LineView getLine() {
        return line;
    }

    public long getTotalStorage() {
        return totalStorage;
    }
}
