package com.jakebarnby.filemanager.util;

import com.jakebarnby.filemanager.sources.models.SourceFile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    public TreeNode<T> addChild(T data) {
        TreeNode<T> newChild = new TreeNode<>(data);
        newChild.setParent(this);
        children.add(newChild);
        return newChild;
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

    public void replaceNode(TreeNode<T> toReplace) {
        this.parent = toReplace.getParent();
        this.children = toReplace.getChildren();
    }

    public void removeChild(TreeNode<T> toRemove) {
        children.remove(toRemove);
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

    public static void sortTree(TreeNode<? extends SourceFile> rootNode, Comparator<TreeNode<? extends SourceFile>> comparator) {
        Collections.sort(rootNode.getChildren(), comparator);
        for (TreeNode<? extends SourceFile> child : rootNode.getChildren()) {
            if (child.getData().isDirectory()) {
                sortTree(child, comparator);
            }
        }
    }

    /**
     * Find parent with the given name in the given current node
     * @param currentNode    The node to start search for parent at
     * @param parentToFind   Name of the node to find as a parent of currentNode
     * @return               Node with the given name which is a parent of the given node if it exists, otherwise null
     */
    public static TreeNode<SourceFile> findParent(TreeNode<SourceFile> currentNode, String parentToFind) {
        if (currentNode.getParent() != null) {
            if (currentNode.getParent().getData().getName().equals(parentToFind)) {
                return currentNode.getParent();
            } else {
                return findParent(currentNode.getParent(), parentToFind);
            }
        } else return currentNode;
    }

    public static List<TreeNode<SourceFile>> searchTree(TreeNode<SourceFile> currentNode, String childToFind) {
        List<TreeNode<SourceFile>> results = new ArrayList<>();
        searchTree(currentNode, childToFind, results);
        return results;
    }

    private static void searchTree(TreeNode<SourceFile> currentNode, String childToFind, List<TreeNode<SourceFile>> results) {
        if (currentNode != null) {
            if (currentNode.getData().getName().contains(childToFind)) {
                results.add(currentNode);
            }

            if (currentNode.getChildren() != null) {
                for (TreeNode<SourceFile> child : currentNode.getChildren()) {
                    if (child.getData().isDirectory()) {
                        searchTree(child, childToFind, results);
                    } else {
                        if (currentNode.getData().getName().contains(childToFind)) {
                            results.add(currentNode);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof TreeNode){
            return data.equals(((TreeNode) obj).getData());
        }
        return false;
    }
}
