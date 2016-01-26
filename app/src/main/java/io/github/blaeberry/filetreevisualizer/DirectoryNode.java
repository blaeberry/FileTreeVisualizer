package io.github.blaeberry.filetreevisualizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evan on 1/22/2016.
 */

//Not sure if nodes should be unidirectional or not yet...
public class DirectoryNode {
    private DirectoryNode parent;
    private DirectoryView contents;
    private List<DirectoryNode> children;
    private long totalStorage;

    public DirectoryNode(DirectoryNode parent, DirectoryView contents, List<DirectoryNode> children) {
        this.parent = parent;
        this.contents = contents;
        this.children = children;
    }

    public DirectoryNode(DirectoryNode parent, DirectoryView contents) {
        this.parent = parent;
        this.contents = contents;
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

    public void setContents(DirectoryView contents) {
        this.contents = contents;
    }

    public void setChildren(List<DirectoryNode> children) {
        this.children = children;
    }

    public void setTotalStorage(long totalStorage) {
        this.totalStorage = totalStorage;
    }

    public DirectoryNode getParent() {
        return parent;
    }

    public DirectoryView getContents() {
        return contents;
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

    public long getTotalStorage() {
        return totalStorage;
    }
}