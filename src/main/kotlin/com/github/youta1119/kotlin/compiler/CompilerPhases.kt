package com.github.youta1119.kotlin.compiler

import org.jetbrains.kotlin.backend.common.phaser.*
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.config.languageVersionSettings


internal fun createUnitPhase(
    name: String,
    description: String,
    prerequisite: Set<AnyNamedPhase> = emptySet(),
    op: DotNetBackendContext.() -> Unit
) = namedOpUnitPhase(name, description, prerequisite, op)

internal val frontendPhase = createUnitPhase(
    op = {
        val environment = environment
        val analyzerWithCompilerReport = AnalyzerWithCompilerReport(
            messageCollector,
            environment.configuration.languageVersionSettings
        )
        // Build AST and binding info.
        analyzerWithCompilerReport.analyzeAndReport(environment.getSourceFiles()) {
            TopDownAnalyzerFacadeForDotNet.analyzeFiles(
                environment.project,
                configuration,
                environment.getSourceFiles()
            )
        }
        if (analyzerWithCompilerReport.hasErrors()) {
            throw DotNetCompilationException()
        }
        moduleDescriptor = analyzerWithCompilerReport.analysisResult.moduleDescriptor
        bindingContext = analyzerWithCompilerReport.analysisResult.bindingContext
    },
    name = "Frontend",
    description = "Frontend builds AST"
)

val toplevelPhase: CompilerPhase<DotNetBackendContext, Unit, Unit> = namedUnitPhase(
    name = "Compiler",
    description = "The whole compilation process",
    lower = frontendPhase
)
