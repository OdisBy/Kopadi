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
import com.squareup.kotlinpoet.ksp.writeTo
import org.kodein.di.DI
import java.util.Locale
import javax.inject.Inject

class CodeGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val modulesByPackage = mutableMapOf<String, MutableList<String>>()
    private val modulesCreated = mutableSetOf<String>()

    private var isProcessed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
//
//        if (isProcessed) return emptyList()
//        isProcessed = true

        val symbols = resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName.orEmpty())
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { it.isConstructor() }
            .filter(KSNode::validate)

        if (!symbols.iterator().hasNext()) return emptyList()

        symbols.forEach { symbol ->
            if (symbol.parentDeclaration is KSClassDeclaration) {
                val classDeclaration = symbol.parentDeclaration as KSClassDeclaration
                generateForClass(classDeclaration, symbol)
            }
        }

        generateForPackages(modulesByPackage)

        generateAllModules(modulesCreated)

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
        modulesByPackage.getOrPut(packageName) { mutableListOf() }.add(classNameModule)

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
            .addFileComment("Generated code by Kopadi. Do not modify. ", arrayOf<Any>())
            .addImport("org.kodein.di", "bind")
            .addImport("org.kodein.di", "provider")
            .addImport("org.kodein.di", "instance")
            .addProperty(
                PropertySpec.builder(classNameModule, DI.Module::class)
                    .initializer(moduleCode)
                    .build()
            )
            .build()

//        writeFile(packageName, classNameModule, fileSpec)
        fileSpec.writeTo(codeGenerator, Dependencies(true, classDeclaration.containingFile!!))
    }

    private fun generateForPackages(modulesByPackage: Map<String, List<String>>) {
        modulesByPackage.forEach { (packageName, moduleNames) ->
            val lastPackagePart = packageName.substringAfterLast('.')
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            val packageModuleName = "${lastPackagePart}_Modules"

            val fileSpecBuilder = FileSpec.builder(packageName, packageModuleName)
                .addFileComment("Generated code by Kopadi. Do not modify.", arrayOf<Any>())
                .addImport("org.kodein.di", "DI")

            moduleNames.forEach { moduleName ->
                fileSpecBuilder.addImport(packageName, moduleName)
            }

            val moduleCode = buildString {
                appendLine("DI.Module(\nprefix = \"$packageName\",\nname = \"$packageName.$packageModuleName\"\n) {")
                moduleNames.forEach { moduleName ->
                    appendLine("\timport(${moduleName})")
                }
                appendLine("}")
            }

            fileSpecBuilder.addProperty(
                PropertySpec.builder(packageModuleName, DI.Module::class)
                    .initializer(moduleCode)
                    .build()
            )

            modulesCreated.add("$packageName.$packageModuleName")

            val fileSpec = fileSpecBuilder.build()

            fileSpec.writeTo(codeGenerator, Dependencies(true))
//            writeFile(packageName, packageModuleName, fileSpec)
        }
    }


    private fun generateAllModules(modulesCreated: MutableSet<String>) {
        val fileSpecBuilder = FileSpec.builder("com.odisby.kopadi.sample.ui.test", "allModules")
            .addFileComment("Generated code by Kopadi. Do not modify. ", arrayOf<Any>())
            .addImport("org.kodein.di", "DI")

        modulesCreated.forEach { moduleName ->
            fileSpecBuilder.addImport("", moduleName)
        }

        val moduleCode = buildString {
            appendLine("DI.Module(\nprefix = \"com.odisby.kopadi.sample.ui.test\",\nname = \"com.odisby.kopadi.sample.ui.test.allModules\"\n) {")
            modulesCreated.forEach { moduleName ->
                appendLine("\timport(${moduleName})")
            }
            appendLine("}")
        }

        fileSpecBuilder.addProperty(
            PropertySpec.builder("allModules", DI.Module::class)
                .initializer(moduleCode)
                .build()
        )

        val fileSpec = fileSpecBuilder.build()

//        writeFile("com.odisby.kopadi.sample.ui.test", "allModules", fileSpec)
        fileSpec.writeTo(codeGenerator, Dependencies(true))
    }

    private fun CodeGenerator.generatedFileExists(filePath: String): Boolean {
        return try {
            this::class.java.classLoader.getResource(filePath.replace('.', '/') + ".kt") != null
        } catch (e: Exception) {
            false
        }
    }
}
