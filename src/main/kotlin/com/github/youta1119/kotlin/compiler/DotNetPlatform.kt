package com.github.youta1119.kotlin.compiler

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.konan.KonanPlatform
import org.jetbrains.kotlin.platform.konan.KonanPlatforms
import org.jetbrains.kotlin.storage.StorageManager


class DotNetBuiltIns(storageManager: StorageManager) : KotlinBuiltIns(storageManager)

@Suppress("DEPRECATION_ERROR")
object DotNetPlatform {

    private object DefaultSimpleDotNetPlatform : KonanPlatform()
    @Deprecated(
        message = "Should be accessed only by compatibility layer, other clients should use 'defaultDotNetPlatform'",
        level = DeprecationLevel.ERROR
    )
    object CompatKonanPlatform : TargetPlatform(setOf(DefaultSimpleDotNetPlatform)),
        org.jetbrains.kotlin.resolve.TargetPlatform {
        override val platformName: String
            get() = "DotNet"
    }

    val defaultDotNetPlatform: TargetPlatform
        get() = KonanPlatforms.CompatKonanPlatform
}
