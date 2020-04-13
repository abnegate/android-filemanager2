package com.jakebarnby.filemanager.models

enum class SortType(val value: Int) {
    NAME(0),
    TYPE(1),
    SIZE(2),
    MODIFIED_TIME(3);

    companion object {
        fun getFromValue(value: Int): SortType? =
            values().find { it.value == value }
    }
}