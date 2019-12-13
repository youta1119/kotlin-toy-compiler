package com.github.youta1119.kotlin.cli

import com.github.youta1119.kotlin.compiler.compileToDotNetByteCode
import com.github.youta1119.kotlin.compiler.toplevelPhase
import com.github.youta1119.kotlin.config.DotNetConfigurationKeys
import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.cli.common.*
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.cli.common.messages.OutputMessageUtil
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.codegen.CompilationException
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
        val environment = KotlinCoreEnvironment.createForProduction(
            rootDisposable,
            configuration,
            EnvironmentConfigFiles.NATIVE_CONFIG_FILES
        )
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY) ?: MessageCollector.NONE
        configuration.put(
            CLIConfigurationKeys.PHASE_CONFIG,
            createPhaseConfig(toplevelPhase, arguments, messageCollector)
        )
        if (environment.getSourceFiles().isEmpty()) {
            if (arguments.version) return ExitCode.OK

            messageCollector.report(CompilerMessageSeverity.ERROR, "No source files")
            return ExitCode.COMPILATION_ERROR
        }
        return try {
            compileToDotNetByteCode(environment, configuration)
            ExitCode.OK
        } catch (e: CompilationException) {
            messageCollector.report(
                CompilerMessageSeverity.EXCEPTION,
                OutputMessageUtil.renderException(e),
                MessageUtil.psiElementToMessageLocation(e.element)
            )
            ExitCode.INTERNAL_ERROR
        }
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
