package com.jakebarnby.filemanager.util;

import android.content.Context;

import com.jakebarnby.filemanager.sources.models.SourceFile;

import java.util.Comparator;

/**
 * Created by Jake on 10/3/2017.
 */

public class ComparatorUtils {
    public static Comparator<TreeNode<? extends SourceFile>> resolveComparator(Context context) {
        boolean showFoldersFirst = PreferenceUtils.getBoolean(
                context,
                Constants.Prefs.FOLDER_FIRST_KEY,
                true);

        int sortType = PreferenceUtils.getInt(
                context,
                Constants.Prefs.SORT_TYPE_KEY,
                Constants.SortTypes.NAME);

        int orderType = PreferenceUtils.getInt(
                context,
                Constants.Prefs.ORDER_TYPE_KEY,
                Constants.SortTypes.NAME);

        switch (sortType) {
            case Constants.SortTypes.NAME:
                return getComparatorNameForOrder(orderType, showFoldersFirst);
            case Constants.SortTypes.MODIFIED_TIME:
                return getComparatorTimeForOrder(orderType, showFoldersFirst);
            case Constants.SortTypes.SIZE:
                return getComparatorSizeForOrder(orderType, showFoldersFirst);
            case Constants.SortTypes.TYPE:
                return getComparatorTypeForOrder(orderType, showFoldersFirst);
            default:
                return getComparatorNameForOrder(orderType, showFoldersFirst);

        }
    }

    private static Comparator<TreeNode<? extends SourceFile>> getComparatorNameForOrder(int orderType, boolean showFoldersFirst) {
        if (orderType == Constants.OrderTypes.ASCENDING) {
            return (node1, node2) -> {
                if (showFoldersFirst) {
                    int result = Boolean.valueOf(!node1.getData().isDirectory()).compareTo(!node2.getData().isDirectory());
                    if (result == 0) {
                        result = node1.getData().getName().toLowerCase().compareTo(node2.getData().getName().toLowerCase());
                    }
                    return result;
                } else {
                    return node1.getData().getName().toLowerCase().compareTo(node2.getData().getName().toLowerCase());
                }
            };
        } else {
            return (node1, node2) -> {
                if (showFoldersFirst) {
                    int result = Boolean.valueOf(!node1.getData().isDirectory()).compareTo(!node2.getData().isDirectory());
                    if (result == 0) {
                        result = node2.getData().getName().toLowerCase().compareTo(node1.getData().getName().toLowerCase());
                    }
                    return result;
                } else {
                    return node2.getData().getName().toLowerCase().compareTo(node1.getData().getName().toLowerCase());
                }
            };
        }
    }


    private static Comparator<TreeNode<? extends SourceFile>> getComparatorTimeForOrder(int orderType, boolean showFoldersFirst) {
        if (orderType == Constants.OrderTypes.ASCENDING) {
            return (node1, node2) -> {
                if (showFoldersFirst) {
                    int result = Boolean.valueOf(!node1.getData().isDirectory()).compareTo(!node2.getData().isDirectory());
                    if (result == 0) {
                        result = Long.compare(node1.getData().getModifiedTime(), (node2.getData().getModifiedTime()));
                    }
                    return result;
                } else {
                    return Long.compare(node1.getData().getModifiedTime(), (node2.getData().getModifiedTime()));
                }
            };
        } else {
            return (node1, node2) -> {
                if (showFoldersFirst) {
                    int result = Boolean.valueOf(!node1.getData().isDirectory()).compareTo(!node2.getData().isDirectory());
                    if (result == 0) {
                        result = Long.compare(node2.getData().getModifiedTime(), (node1.getData().getModifiedTime()));
                    }
                    return result;
                } else {
                    return Long.compare(node2.getData().getModifiedTime(), (node1.getData().getModifiedTime()));
                }
            };
        }
    }

    private static Comparator<TreeNode<? extends SourceFile>> getComparatorSizeForOrder(int orderType, boolean showFoldersFirst) {
        if (orderType == Constants.OrderTypes.ASCENDING) {
            return (node1, node2) -> {
                if (showFoldersFirst) {
                    int result = Boolean.valueOf(!node1.getData().isDirectory()).compareTo(!node2.getData().isDirectory());
                    if (result == 0) {
                        result = Long.compare(node1.getData().getSize(), (node2.getData().getSize()));
                    }
                    return result;
                } else {
                    return Long.compare(node1.getData().getSize(), (node2.getData().getSize()));
                }
            };
        } else {
            return (node1, node2) -> {
                if (showFoldersFirst) {
                    int result = Boolean.valueOf(!node1.getData().isDirectory()).compareTo(!node2.getData().isDirectory());
                    if (result == 0) {
                        result = Long.compare(node2.getData().getSize(), (node1.getData().getSize()));
                    }
                    return result;
                } else {
                    return Long.compare(node2.getData().getSize(), (node1.getData().getSize()));
                }
            };
        }
    }

    private static Comparator<TreeNode<? extends SourceFile>> getComparatorTypeForOrder(int orderType, boolean showFoldersFirst) {
        if (orderType == Constants.OrderTypes.ASCENDING) {
            return (node1, node2) -> {
                if (showFoldersFirst) {
                    int result = Boolean.valueOf(!node1.getData().isDirectory()).compareTo(!node2.getData().isDirectory());
                    if (result == 0) {
                        result = Long.compare(node1.getData().getSize(), (node2.getData().getSize()));
                    }
                    return result;
                } else {
                    return Long.compare(node1.getData().getSize(), (node2.getData().getSize()));
                }
            };
        } else {
            return (node1, node2) -> {
                if (showFoldersFirst) {
                    int result = Boolean.valueOf(!node1.getData().isDirectory()).compareTo(!node2.getData().isDirectory());
                    if (result == 0) {
                        result = Long.compare(node2.getData().getSize(), (node1.getData().getSize()));
                    }
                    return result;
                } else {
                    return Long.compare(node2.getData().getSize(), (node1.getData().getSize()));
                }
            };
        }
    }
}
