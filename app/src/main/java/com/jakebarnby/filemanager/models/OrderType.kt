package com.jakebarnby.filemanager.models

enum class OrderType(val value: Int) {
    ASCENDING(0),
    DESCENDING(1);

    companion object {
        fun getFromValue(value: Int): OrderType? =
            values().find { it.value == value }
    }
}