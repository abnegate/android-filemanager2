package com.jakebarnby.filemanager.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jake on 5/31/2017.
 */

public class TreeNode<T extends Serializable> implements Serializable {
    private T                   data = null;
    private List<TreeNode<T>>   children = new ArrayList<>();
    private TreeNode<T>         parent = null;

    public TreeNode(T data) {
        this.data = data;
    }

    /**
     *
     * @param child
     */
    public void addChild(TreeNode<T> child) {
        child.setParent(this);
        this.children.add(child);
    }

    /**
     *
     * @param data
     */
    public void addChild(T data) {
        TreeNode<T> newChild = new TreeNode<>(data);
        newChild.setParent(this);
        children.add(newChild);
    }

    /**
     *
     * @param children
     */
    public void addChildren(List<TreeNode<T>> children) {
        for(TreeNode<T> t : children) {
            t.setParent(this);
        }
        this.children.addAll(children);
    }

    public void replaceNode(TreeNode<T> parent, List<TreeNode<T>> children) {
        this.parent = parent;
        this.children = children;
    }

    /**
     *
     * @param children
     */
    public void setChildren(List<TreeNode<T>> children) {
        this.children = children;
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setParent(TreeNode<T> parent) {
        this.parent = parent;
    }

    public TreeNode<T> getParent() {
        return parent;
    }
}