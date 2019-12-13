package com.github.youta1119.kotlin.compiler

import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.checkers.ExpectedActualDeclarationChecker
import org.jetbrains.kotlin.resolve.jvm.checkers.SuperCallWithDefaultArgumentsChecker
import org.jetbrains.kotlin.storage.StorageManager


object DotNetPlatformConfigurator : PlatformConfiguratorBase(
    additionalDeclarationCheckers = listOf(
        ExpectedActualDeclarationChecker(
            ModuleStructureOracle.SingleModule,
            emptyList()
        )
    ),
    additionalCallCheckers = listOf(SuperCallWithDefaultArgumentsChecker())
) {
    override fun configureModuleComponents(container: StorageComponentContainer) {
    }
}

object DotNetPlatformAnalyzerServices : PlatformDependentAnalyzerServices() {
    override val platformConfigurator: PlatformConfigurator =
        DotNetPlatformConfigurator

    override fun computePlatformSpecificDefaultImports(
        storageManager: StorageManager,
        result: MutableList<ImportPath>
    ) {
        //no-op
    }
}
