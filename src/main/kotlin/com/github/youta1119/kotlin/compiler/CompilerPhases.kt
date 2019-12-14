package com.github.youta1119.kotlin.compiler

import org.jetbrains.kotlin.backend.common.phaser.*
import org.jetbrains.kotlin.backend.common.serialization.target
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization
import org.jetbrains.kotlin.ir.util.getArguments
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.psi2ir.generators.GeneratorExtensions
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter


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
    description = "Frontend builds AST(PSI)"
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

internal val irToCILPhase = createUnitPhase(
    op = {
        val pw = File(tempFileName).printWriter()
        pw.println(".assembly extern mscorlib {}")
        pw.println(".assembly main {}")
        irModule.acceptChildrenVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitFunction(declaration: IrFunction) {
                assert(declaration.name.asString() == "main") {
                    "this is toy compiler. only main function can be compiled."
                }
                pw.println(".method static void Main() cil managed {")
                pw.println(".entrypoint")
                val body = declaration.body!! as IrBlockBody
                body.statements.forEach { statement ->
                    val callExpr = statement as IrCall
                    val calleeFunctionExpr = callExpr.symbol.owner.target
                    assert(calleeFunctionExpr.fqNameForIrSerialization.asString() == "kotlin.io.println") {
                        "this is toy compiler. only kotlin.io.println function can be called."
                    }


                    val args = (callExpr as IrMemberAccessExpression).getArguments()
                    args.forEach { (_, expr) ->
                        if (expr is IrConst<*>) {
                            pw.println("ldstr \"${expr.value}\"")
                        }
                    }
                    pw.println("call void [mscorlib]System.Console::WriteLine(string)")
                }
                pw.println("ret")
                pw.println("}")
            }
        })
        pw.close()
    },
    name = "IrToDotNetAsm",
    description = "IR to .Net assembly conversion"
)

internal val generateBytecodePhase = createUnitPhase(
    op = {

        val command = listOf("ilasm") + listOf("/output:${outputFileName}", tempFileName)
        val builder = ProcessBuilder(command)

        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        builder.redirectInput(ProcessBuilder.Redirect.INHERIT)
        builder.redirectError(ProcessBuilder.Redirect.INHERIT)

        val process = builder.start()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            throw DotNetCompilationException("ilasm command returned non-zero exit code: $exitCode.")
        }
    },
    name = "GenerateByteCode",
    description = "Generate .Net Bytecode from .Net assembly"
)


val toplevelPhase: CompilerPhase<DotNetBackendContext, Unit, Unit> = namedUnitPhase(
    name = "Compiler",
    description = "The whole compilation process",
    lower = frontendPhase
            then psiToIrPhase
            then irToCILPhase
            then generateBytecodePhase
)
