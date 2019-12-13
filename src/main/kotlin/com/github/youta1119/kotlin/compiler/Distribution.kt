package com.github.youta1119.kotlin.compiler

import java.io.File

object Distribution {
    val stdlibDir = "${findDotNetHome()}/libs"
    private fun findDotNetHome(): String {
        val value = System.getProperty("kotlin.dotnet.home", System.getProperty("user.dir"))
        return File(value).absolutePath
    }
}
