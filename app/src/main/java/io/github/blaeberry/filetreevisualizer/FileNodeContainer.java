package io.github.blaeberry.filetreevisualizer;

import java.io.File;

/**
 * Created by Evan on 2/6/2016.
 */
public class FileNodeContainer {
    private File file;
    private DirectoryNode node;
    private int row;
    private String text;

    public FileNodeContainer(File file, DirectoryNode node, int row) {
        this.file = file;
        this.node = node;
        this.row = row;
        this.text = file.getName();
    }

    public void setFile(File file) {
        this.file = file;
        text = file.getName();
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

    public String getText() {
        return text;
    }
}
