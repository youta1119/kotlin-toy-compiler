package com.github.youta1119.kotlin.config

import org.jetbrains.kotlin.config.CompilerConfigurationKey

class DotNetConfigurationKeys {
    companion object {
        val OUTPUT_NAME: CompilerConfigurationKey<String?> = CompilerConfigurationKey.create("write name")
    }
}
