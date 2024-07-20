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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import org.kodein.di.DI
import javax.inject.Inject

class CodeGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

//    override fun process(resolver: Resolver): List<KSAnnotated> {
//        logger.info("Starting process method")
//
//        val symbols = resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName.orEmpty())
//            .filterIsInstance<KSFunctionDeclaration>()
//            .filter { it.isConstructor() }
//            .filter(KSNode::validate)
//
//        logger.info("Found ${symbols.count()} symbols with @Inject annotation")
//
//        symbols.forEach { symbol ->
//            if (symbol.parentDeclaration is KSClassDeclaration) {
//                logger.info("Parent Declaration: ${symbol.parentDeclaration}")
//                val classDeclaration = symbol.parentDeclaration as KSClassDeclaration
//                logger.info("Class Declaration: $classDeclaration")
//                val dependencies = symbol.parameters.map { it.type.resolve() }
//                logger.info("Dependencies: $dependencies")
//
//                dependencies.forEach {
//                    val className = classDeclaration.simpleName.asString()
//                    val dependencyName = it.declaration.simpleName.asString()
//                    println("Class: $className, Dependency: $dependencyName")
//                    logger.info("Class: $className, Dependency: $dependencyName")
//                }
//            }
//        }
//        return emptyList()
//    }


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
        val dependencies =
            constructor.parameters.map { it.type.resolve().declaration.simpleName.asString() }

        val dependenciesCode = dependencies.joinToString(", ") { it }

        logger.info("Dependencies: $dependencies")

        logger.info("Arguments: ${constructor.parameters}")

        val constructorParameters = constructor.parameters



        logger.info("------------------------------------")
        logger.info("Para o className: $className")

        constructorParameters.forEach {
            logger.info("Nome da dependencia: ${it.type.resolve().declaration.simpleName.asString()}")
            logger.info("Caminho completo: ${it.type.resolve().declaration.qualifiedName?.asString() ?: "Unknown"}")
        }

        logger.info("------------------------------------")

        val packageName = classDeclaration.packageName.asString()
        val classNameModule = "${className}_Module"


        /*

        val PaymentRepository_Module = DI.Module(prefix = "br.com.bancobari.account.repository", name = "br.com.bancobari.account.repository.PaymentRepository") {
            bind<PaymentRepository>() with provider { PaymentRepository(/* paymentsApi: br.com.bancobari.account.api.PaymentsApi */ instance()) }
        }
        Então seria o mesmo que:
        val classNameModule = DI.Module(\nprefix = \"$packageName\",\nname = \"$packageName.$classNameModule\"\n) {
            bind<${className}>() with provider {
                    $className()
            }
            bind<$className>() with provider {
                $className(
                    "${constructorParameters.type.resolve().declaration.simpleName.asString()}(/ * ${constructorParameters.name?.asString() ?: "Unknown"}: ${constructorParameters.type.resolve().declaration.qualifiedName?.asString() ?: "Unknown"} *)"
                )
            }
        }

         */

        val hasConstructorParameters = constructorParameters.isNotEmpty()

        val moduleCode = buildString {
            appendLine("DI.Module(\nprefix = \"$packageName\",\nname = \"$packageName.$classNameModule\"")
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

//        val moduleCode = buildString {
//            constructorParameters.forEach { dependency ->
//                val dependencyName = dependency.type.resolve().declaration.simpleName.asString()
//                val variableDependencyName = dependency.name?.asString() ?: "Unknown"
//                val dependencyLocation =
//                    dependency.type.resolve().declaration.qualifiedName?.asString() ?: "Unknown"
//
//                appendLine("\tbind<${dependencyName}>() with provider {")
//                appendLine("\t\t$dependencyName(/* ${variableDependencyName}: $dependencyLocation */ instance()) ")
//                appendLine("\t}")
//            }
//            appendLine("}")
//        }

        val fileSpec = FileSpec.builder(packageName, classNameModule)
            .addFileComment("Generated code. Do not modify.", arrayOf<Any>())
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

    private fun KSClassDeclaration.toClassName(): ClassName {
        val packageName = this.packageName.asString()
        val simpleName = this.simpleName.asString()
        return ClassName(packageName, simpleName)
    }
}