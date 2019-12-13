package com.github.youta1119.kotlin.compiler

import org.jetbrains.kotlin.config.CompilerConfigurationKey

class DotNetConfigurationKeys {
    companion object {
        val OUTPUT_NAME: CompilerConfigurationKey<String?> = CompilerConfigurationKey.create("write name")
    }
}
