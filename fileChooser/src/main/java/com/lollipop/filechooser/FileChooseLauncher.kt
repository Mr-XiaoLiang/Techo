package com.lollipop.filechooser

interface FileChooseLauncher  {
    fun unregister()
    fun launch(): FileChooser.Builder
}