package com.example.dao

import com.example.model.Conta
import java.io.File
import org.ehcache.CacheManagerBuilder
import org.ehcache.config.CacheConfigurationBuilder
import org.ehcache.config.ResourcePoolsBuilder
import org.ehcache.config.persistence.CacheManagerPersistenceConfiguration
import org.ehcache.config.units.EntryUnit
import org.ehcache.config.units.MemoryUnit

/**
 * An Ehcache based implementation for the [DAOFacade] that uses a [delegate] facade and a [storagePath]
 * and perform several caching strategies for each domain operation.
 */
class DAOFacadeCache(private val delegate: DAOFacade, private val storagePath: File) : DAOFacade {

    /**
     * Build a cache manager with a cache for kweets and other for users.
     * It uses the specified [storagePath] for persistence.
     * Limits the cache to 1000 entries, 10MB in memory, and 100MB in disk per both caches.
     */
    val cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(CacheManagerPersistenceConfiguration(storagePath))
        .withCache("contaCache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder<String, Conta>()
                .withResourcePools(
                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .heap(100, EntryUnit.ENTRIES)
                        .offheap(1, MemoryUnit.MB)
                        .disk(10, MemoryUnit.MB, true)
                )
                .buildConfig(String::class.javaObjectType, Conta::class.java))
        .build(true)!!

    /**
     * Gets the cache for Conta represented by an [String] key and a  [Conta] value.
     */
    val contaCache = cacheManager.getCache("contaCache", String::class.javaObjectType, Conta::class.java)


    override fun init() {
        delegate.init()
    }

    override fun contaByText(text: String, usuario: String): Conta? {
        val contaCached = contaCache.get(text)
        if (contaCached != null) return contaCached

        val conta = delegate.contaByText(text, usuario)

        if ( conta != null) contaCache.put( montaKeyContaCache(text, usuario), conta )

        return conta
    }

    override fun createConta(text: String, isDefault: Boolean, usuario: String): Int {
        val conta = contaByText(text, usuario)

        if ( conta != null ) error("'text=${text}' da Conta já existe.")

        val id = delegate.createConta(text, isDefault, usuario)
        val contaLocal = Conta(id, text, isDefault, usuario)
        contaCache.put( montaKeyContaCache(text, usuario), contaLocal )

        return id
    }

    private fun montaKeyContaCache(text: String, usuario: String): String {
        return text + "|" + usuario
    }

    override fun close() {
        try {
            delegate.close()
        } finally {
            cacheManager.close()
        }
    }
}
