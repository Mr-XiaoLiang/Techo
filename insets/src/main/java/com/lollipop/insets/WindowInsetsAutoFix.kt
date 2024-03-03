package com.lollipop.insets

/**
 * 黑名单模式下，会被自动注册器忽略的身份接口
 * 在白名单模式（WindowInsetsAutoFixMode.ALLOW）下会被忽略
 */
interface WindowInsetsAutoFixBlock

/**
 * 白名单模式下，会被自动设置的身份接口
 * 在黑名单模式（WindowInsetsAutoFixMode.BLOCK）下将会被忽略
 */
interface WindowInsetsAutoFixAllow

/**
 * 自动注册的WindowInsets模式
 */
enum class WindowInsetsAutoFixMode {
    /**
     * 黑名单模式
     * 这种模式下，将会默认注册所有的Activity
     * 但是会忽略实现WindowInsetsAutoFixBlock接口的Activity
     */
    BLOCK,

    /**
     * 白名单模式
     * 这种模式下，将默认不注册所有的Activity
     * 只会注册实现了WindowInsetsAutoFixAllow接口的Activity
     */
    ALLOW
}