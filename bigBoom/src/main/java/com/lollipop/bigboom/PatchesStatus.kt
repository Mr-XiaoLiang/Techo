package com.lollipop.bigboom

enum class PatchesStatus{
    DEFAULT,
    SELECTED,
    DISABLE;

    val isDefault: Boolean
        get() {
            return this == DEFAULT
        }

    val isSelected: Boolean
        get() {
            return this == SELECTED
        }

    val isDisable: Boolean
        get() {
            return this == DISABLE
        }

}