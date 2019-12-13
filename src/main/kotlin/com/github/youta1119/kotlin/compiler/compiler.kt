package com.github.youta1119.kotlin.compiler

import org.jetbrains.kotlin.backend.common.phaser.invokeToplevel
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration


fun compileToDotNetByteCode(environment: KotlinCoreEnvironment, configuration: CompilerConfiguration) {
    val context = DotNetBackendContext(environment, configuration)
    toplevelPhase.invokeToplevel(context.phaseConfig, context, Unit)
}
