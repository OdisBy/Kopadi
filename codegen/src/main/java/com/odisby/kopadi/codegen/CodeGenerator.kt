package com.odisby.kopadi.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import javax.inject.Inject

class CodeGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("Starting process method")

        val symbols = resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName.orEmpty())
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { it.isConstructor() }
            .filter(KSNode::validate)

        logger.info("Found ${symbols.count()} symbols with @Inject annotation")

        symbols.forEach { symbol ->
            if (symbol.parentDeclaration is KSClassDeclaration) {
                val classDeclaration = symbol.parentDeclaration as KSClassDeclaration
                val dependencies = symbol.parameters.map { it.type.resolve() }
                dependencies.forEach {
                    val className = classDeclaration.simpleName.asString()
                    val dependencyName = it.declaration.simpleName.asString()
                    println("Class: $className, Dependency: $dependencyName")
                    logger.info("Class: $className, Dependency: $dependencyName")
                }
            }
        }
        return emptyList()
    }

    private fun KSFunctionDeclaration.isConstructor(): Boolean {
        return this.simpleName.asString() == "<init>"
    }
}