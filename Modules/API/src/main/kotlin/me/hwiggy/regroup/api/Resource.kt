package me.hwiggy.regroup.api

import java.io.File
import java.net.JarURLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * Pertinent information for loading and saving resources
 */
interface Resource<Kind> {
    val extension: String
    val loader: Loader<Kind>
    val saver: Saver<Kind>

    /**
     * A means of loading a [Resource] from a File
     */
    fun interface Loader<Resource> {
        fun load(path: Path): Resource
    }

    /**
     * A means of saving a [Resource] to a File
     */
    fun interface Saver<Resource> {
        fun save(resource: Resource, path: Path)
    }

    /**
     * Similar to ResourceBundle, but more flexible in nature and not contingent on the classloader
     */
    open class Group<Kind>(
        private val folder: Path,
        private val resource: Resource<Kind>
    ) {
        private val cache = IdentityHashMap<Locale, Kind>()

        /**
         * Searches the resource folder for a resource for a specific locale
         * Iterates the [localeFilenameVariants] to search for a matching key
         * If a resource does not exist for a given locale, returns the default, [Locale.ENGLISH]
         * The result is cached, meaning a resource will only be loaded once
         */
        operator fun get(locale: Locale = Locale.ENGLISH): Kind = cache.computeIfAbsent(locale) {
            var found: File? = null
            for (variant in localeFilenameVariants(locale)) {
                val filename = variant + resource.extension
                val filePath = folder.resolve(filename)
                val file = filePath.toFile()
                if (!file.exists()) continue
                found = file
                break
            }
            if (found != null) return@computeIfAbsent resource.loader.load(found.toPath())
            if (locale == Locale.ENGLISH) throw IllegalStateException("Could not load default resource!")
            this[Locale.ENGLISH]
        }

        /**
         * Returns a list of locale keys to be searched, in order of the list, for a specific resource file
         * Default implementation is a singleton list of the fully qualified locale key
         */
        open fun localeFilenameVariants(locale: Locale) = listOf(locale.toString())
    }

    /**
     * Facilitates a means of saving resources and resource groups from the JAR as well as loading from disk
     */
    open class Manager protected constructor(
        private val mainClass: Class<*>,
        private val dataFolder: Path
    ) {
        /**
         * Exports a path from the JAR to a target path on the disk
         */
        private fun exportFromJar(sourcePath: Path, targetPath: Path) {
            val className: String = mainClass.name.run {
                val idx = lastIndexOf('.')
                (if (idx == -1) this else this.substring(idx + 1)) + ".class"
            }
            val jarPath = mainClass.getResource(className) ?: return
            if (!jarPath.toString().startsWith("jar:file:")) return
            val connection = jarPath.openConnection() as? JarURLConnection ?: return
            val archive = connection.jarFile ?: return
            val entries = archive.entries()
            val pathStr = sourcePath.toString()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name
                if (!name.startsWith(pathStr)) continue
                var entryTail = name.substring(pathStr.length)
                if (entryTail.startsWith("/")) entryTail = entryTail.substring(1)
                val absolute = dataFolder.resolve(targetPath).resolve(entryTail)
                if (entry.isDirectory) {
                    absolute.toFile().mkdirs()
                    continue
                }
                Files.copy(archive.getInputStream(entry), absolute)
            }
        }

        /**
         * Obtains a resource group of a specific type from the specified path
         */
        fun <Kind> group(resource: Resource<Kind>, targetPath: Path): Group<Kind> {
            val absPath = dataFolder.resolve(targetPath)
            val file = absPath.toFile()
            if (!file.exists()) exportFromJar(targetPath, targetPath)
            return Group(absPath, resource)
        }

        /**
         * Loads a resource from the JAR, returns null if not present
         */
        fun <Kind> loadFromJar(
            resource: Resource<Kind>, jarPath: Path, targetPath: Path = jarPath
        ): Kind? {
            val absolute = dataFolder.resolve(targetPath)
            if (!Files.exists(absolute)) {
                Files.createDirectories(absolute.parent)
                exportFromJar(jarPath, targetPath)
            }
            return resource.loader.load(absolute)
        }

        /**
         * Loads a resource from the JAR, throws if not present
         */
        fun <Kind> loadFromJarThrowing(
            resource: Resource<Kind>,
            sourcePath: Path,
            targetPath: Path = sourcePath
        ) = loadFromJar(resource, sourcePath, targetPath) ?: throw IllegalStateException("Could not load resource!")

        /**
         * Saves a resource to the disk
         */
        fun <Kind> save(
            resource: Resource<Kind>,
            toSave: Kind,
            targetPath: Path
        ) {
            val absolute = dataFolder.resolve(targetPath)
            if (!Files.exists(absolute)) {
                Files.createDirectories(absolute.parent)
                Files.createFile(absolute)
            }
            resource.saver.save(toSave, absolute)
        }
    }
}