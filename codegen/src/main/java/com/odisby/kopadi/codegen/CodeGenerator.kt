package com.odisby.kopadi.codegen

import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import org.kodein.di.DI
import javax.inject.Inject

class CodeGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName.orEmpty())
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { it.isConstructor() }
            .filter(KSNode::validate)

        symbols.forEach { symbol ->
            if (symbol.parentDeclaration is KSClassDeclaration) {
                val classDeclaration = symbol.parentDeclaration as KSClassDeclaration
                generateForClass(classDeclaration, symbol)
            }
        }
        return emptyList()
    }

    private fun generateForClass(
        classDeclaration: KSClassDeclaration,
        constructor: KSFunctionDeclaration
    ) {
        val className = classDeclaration.simpleName.asString()

        val constructorParameters = constructor.parameters
        val packageName = classDeclaration.packageName.asString()
        val classNameModule = "${className}_Module"


        val hasConstructorParameters = constructorParameters.isNotEmpty()

        /*
            Example of the code below:

            DI.Module(prefix = "com.example.app.home", name = "com.example.app.home.SomeViewModel_Module"
            ) {
                bind<SomeViewModel>() with provider {
                    hasConstructor:
                        SomeViewModel(
                            /\* homeRepository: com.example.app.home.data.HomeRepository *\/ instance(),
                            /\* homeRepository2: com.example.app.home.data.HomeRepository *\/ instance(),
                        )
                    else:
                        SomeViewModel()
                }
              }
        */
        val moduleCode = buildString {
            appendLine("DI.Module(\nprefix = \"$packageName\",\nname = \"$packageName.$className\"")
            appendLine(") {")
            appendLine("\tbind<${className}>() with provider {")

            if (hasConstructorParameters) {
                appendLine("\t\t${className}(")
                constructorParameters.forEach { parameter ->
                    appendLine("\t\t\t/* ${parameter.name?.asString()}: ${parameter.type.resolve().declaration.qualifiedName?.asString() ?: "Unknown"} */ instance(),")
                }
                appendLine("\t\t)")
            } else {
                appendLine("\t\t${className}()")
            }

            appendLine("\t}")
            appendLine("}")
        }

        val fileSpec = FileSpec.builder(packageName, classNameModule)
            .addFileComment("Generated code by Kopadi. Do not modify.", arrayOf<Any>())
            .addImport("org.kodein.di", "bind")
            .addImport("org.kodein.di", "provider")
            .addImport("org.kodein.di", "instance")
            .addProperty(
                PropertySpec.builder(classNameModule, DI.Module::class)
                    .initializer(moduleCode)
                    .build()
            )
            .build()

        codeGenerator.createNewFile(
            Dependencies(false, classDeclaration.containingFile!!),
            packageName,
            classNameModule
        ).use { outputStream ->
            outputStream.writer().use { writer ->
                fileSpec.writeTo(writer)
            }
        }
    }
}
