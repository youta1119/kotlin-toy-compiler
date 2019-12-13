package com.github.youta1119.kotlin.cli

import org.jetbrains.kotlin.cli.common.arguments.Argument
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments

class K2DotNetCompilerArguments: CommonCompilerArguments() {
    @Argument(value = "-output", shortName = "-o", valueDescription = "<name>", description = "Output name")
    var outputName: String? = null
}
