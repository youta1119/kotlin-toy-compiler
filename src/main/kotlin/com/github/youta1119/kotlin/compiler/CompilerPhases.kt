package com.github.youta1119.kotlin.compiler

import org.jetbrains.kotlin.backend.common.phaser.*
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.psi2ir.generators.GeneratorExtensions


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

internal val psiToIrPhase = createUnitPhase(
    op = {
        val translator = Psi2IrTranslator(environment.configuration.languageVersionSettings, Psi2IrConfiguration(false))
        val symbolTable = SymbolTable()
        val generatorContext = translator.createGeneratorContext(
            moduleDescriptor,
            bindingContext,
            symbolTable,
            GeneratorExtensions()
        )
        val module = translator.generateModuleFragment(generatorContext, environment.getSourceFiles())
        irModule = module
    },
    name = "Psi2Ir",
    description = "Psi to IR conversion"
)


internal val dumpIrPhase = createUnitPhase(
    op = {
       println(irModule.dump())
    },
    name = "DumpIr",
    description = "dump ir"
)

val toplevelPhase: CompilerPhase<DotNetBackendContext, Unit, Unit> = namedUnitPhase(
    name = "Compiler",
    description = "The whole compilation process",
    lower = frontendPhase
            then psiToIrPhase
            then dumpIrPhase
)
