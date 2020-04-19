package com.jakebarnby.filemanager.models

enum class ViewType(val value: Int) {
    LIST(0),
    DETAILED_LIST(1),
    GRID(2);

    companion object {
        fun getFromValue(value: Int): ViewType? = values()[value]
    }
}