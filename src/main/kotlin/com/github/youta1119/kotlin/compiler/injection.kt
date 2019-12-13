package com.github.youta1119.kotlin.compiler

import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.container.*
import org.jetbrains.kotlin.context.ModuleContext
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.frontend.di.configureModule
import org.jetbrains.kotlin.frontend.di.configureStandardResolveComponents
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.lazy.FileScopeProviderImpl
import org.jetbrains.kotlin.resolve.lazy.KotlinCodeAnalyzer
import org.jetbrains.kotlin.resolve.lazy.ResolveSession
import org.jetbrains.kotlin.resolve.lazy.declarations.DeclarationProviderFactory

fun createTopDownAnalyzerForDotNet(
    moduleContext: ModuleContext,
    bindingTrace: BindingTrace,
    declarationProviderFactory: DeclarationProviderFactory,
    languageVersionSettings: LanguageVersionSettings
): LazyTopDownAnalyzer {
    val storageComponentContainer = createContainer("TopDownAnalyzerForDotNet", DotNetPlatformAnalyzerServices) {
        configureModule(
            moduleContext,
            DotNetPlatform.defaultDotNetPlatform,
            DotNetPlatformAnalyzerServices,
            bindingTrace,
            languageVersionSettings
        )
        configureStandardResolveComponents()
        useInstance(declarationProviderFactory)
        useImpl<FileScopeProviderImpl>()

        CompilerEnvironment.configure(this)

        useInstance(LookupTracker.DO_NOTHING)
       //useInstance(languageVersionSettings)
    }.apply {
        get<ModuleDescriptorImpl>().initialize(get<KotlinCodeAnalyzer>().packageFragmentProvider)
    }
    return storageComponentContainer.get()
}
