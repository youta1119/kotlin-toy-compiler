package com.github.youta1119.kotlin.cli

import com.github.youta1119.kotlin.compiler.DotNetConfigurationKeys
import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.CommonCompilerPerformanceManager
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.utils.KotlinPaths

class K2DotNetCompiler : CLICompiler<K2DotNetCompilerArguments>() {

    override val performanceManager: CommonCompilerPerformanceManager by lazy {
        K2DotNetCompilerPerformanceManager()
    }

    override fun doExecute(
        arguments: K2DotNetCompilerArguments,
        configuration: CompilerConfiguration,
        rootDisposable: Disposable,
        paths: KotlinPaths?
    ): ExitCode {
        return ExitCode.OK
    }

    override fun setupPlatformSpecificArgumentsAndServices(
        configuration: CompilerConfiguration,
        arguments: K2DotNetCompilerArguments,
        services: Services
    ) {
        val commonSources = arguments.commonSources?.toSet().orEmpty()
        arguments.freeArgs.forEach {
            configuration.addKotlinSourceRoot(it, it in commonSources)
        }
        with(DotNetConfigurationKeys) {
            with(configuration) {
                arguments.outputName?.let { put(OUTPUT_NAME, it) }
            }
        }
    }

    override fun createArguments(): K2DotNetCompilerArguments =
        K2DotNetCompilerArguments()

    override fun createMetadataVersion(versionArray: IntArray): BinaryVersion =
        K2DotNetMetadataVersion()

    override fun executableScriptFileName(): String = "kotlinc-dotnet"

    class K2DotNetCompilerPerformanceManager : CommonCompilerPerformanceManager("Kotlin to .Net Compiler")
    class K2DotNetMetadataVersion(vararg numbers: Int) : BinaryVersion(*numbers) {
        override fun isCompatible(): Boolean = false
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            doMain(K2DotNetCompiler(), args)
        }
    }
}
