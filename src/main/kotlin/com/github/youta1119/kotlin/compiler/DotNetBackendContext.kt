package com.github.youta1119.kotlin.compiler


import com.github.youta1119.kotlin.config.DotNetConfigurationKeys
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.ir.DeclarationFactory
import org.jetbrains.kotlin.backend.common.ir.Ir
import org.jetbrains.kotlin.backend.common.ir.SharedVariablesManager
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.descriptors.IrBuiltIns
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.BindingContext

class DotNetBackendContext(val environment: KotlinCoreEnvironment, override val configuration: CompilerConfiguration) :
    CommonBackendContext {
    lateinit var moduleDescriptor: ModuleDescriptor
    lateinit var bindingContext: BindingContext
    lateinit var irModule: IrModuleFragment
    val phaseConfig = configuration.get(CLIConfigurationKeys.PHASE_CONFIG)!!

    val outputName = configuration.get(DotNetConfigurationKeys.OUTPUT_NAME) ?: DEFAULT_OUTPUT_NAME

    override val builtIns: DotNetBuiltIns by lazy {
        moduleDescriptor.builtIns as DotNetBuiltIns
    }

    override val irBuiltIns: IrBuiltIns
        get() = TODO("not implemented")

    override val sharedVariablesManager: SharedVariablesManager
        get() = TODO("not implemented")

    override val declarationFactory: DeclarationFactory
        get() = TODO("not implemented")
    override var inVerbosePhase: Boolean = false

    override val internalPackageFqn: FqName
        get() = TODO("not implemented")

    override val ir: Ir<CommonBackendContext>
        get() = TODO("not implemented")

    override fun log(message: () -> String) {
        if (inVerbosePhase) {
            println(message())
        }
    }

    override fun report(element: IrElement?, irFile: IrFile?, message: String, isError: Boolean) {
        this.messageCollector.report(
            if (isError) CompilerMessageSeverity.ERROR else CompilerMessageSeverity.WARNING,
            message, null
        )
    }

    val messageCollector: MessageCollector
        get() = configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)

    companion object {
        private const val DEFAULT_OUTPUT_NAME = "program"
    }
}

