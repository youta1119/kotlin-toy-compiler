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

internal val noOpPhase = createUnitPhase(
    op = {
       println("no op")
    },
    name = "NoOp",
    description = "no-op"
)

val toplevelPhase: CompilerPhase<DotNetBackendContext, Unit, Unit> = namedUnitPhase(
    name = "Compiler",
    description = "The whole compilation process",
    lower = noOpPhase
)
