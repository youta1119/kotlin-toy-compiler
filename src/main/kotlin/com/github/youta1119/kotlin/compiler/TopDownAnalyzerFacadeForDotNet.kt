package com.github.youta1119.kotlin.compiler

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.cli.common.config.KotlinSourceRoot
import org.jetbrains.kotlin.cli.jvm.compiler.createSourceFilesFromSourceRoots
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.context.ContextForNewModule
import org.jetbrains.kotlin.context.ModuleContext
import org.jetbrains.kotlin.context.ProjectContext
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList

object TopDownAnalyzerFacadeForDotNet {

    @JvmStatic
    fun analyzeFiles(
        project: Project,
        configuration: CompilerConfiguration,
        files: Collection<KtFile>

    ): AnalysisResult {

        val projectContext = ProjectContext(project, "TopDownAnalyzer for DotNet")
        val builtIns = DotNetBuiltIns(projectContext.storageManager)
        val context = ContextForNewModule(
            projectContext,
            Name.special("<main>"),
            builtIns, null
        )
        val module = context.module
        builtIns.builtInsModule = module
        context.setDependencies(module)
        return analyzeFilesWithGivenTrace(files, BindingTraceContext(), context,configuration)
    }

    private fun analyzeFilesWithGivenTrace(
        files: Collection<KtFile>,
        trace: BindingTrace,
        moduleContext: ModuleContext,
        configuration: CompilerConfiguration
    ): AnalysisResult {

        // we print out each file we compile for now
        files.forEach { println(it) }

        val analyzerForKonan = createTopDownAnalyzerForDotNet(
            moduleContext, trace,
            FileBasedDeclarationProviderFactory(moduleContext.storageManager, files),
            configuration.get(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS)!!
        )

        analyzerForKonan.analyzeDeclarations(TopDownAnalysisMode.TopLevelDeclarations, files)
        return AnalysisResult.success(trace.bindingContext, moduleContext.module)
    }
}
