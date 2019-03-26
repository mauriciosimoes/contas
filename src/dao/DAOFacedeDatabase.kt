package com.example.dao

import com.example.model.Conta
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.io.Closeable
import java.io.File

/**
 * A DAO Facade interface for the Database. This allows to provide several implementations.
 *
 * In this case this is used to provide a Database-based implementation using Exposed,
 * and a cache implementation composing another another DAOFacade.
 */
interface DAOFacade : Closeable {

    /**
     * Initializes all the required data.
     * In this case this should initialize the Users and Kweets tables.
     */
    fun init()

    fun contaByText(text: String, usuario: String): Conta?

    fun createConta(text: String, isDefault: Boolean, usuario: String): Int

//    fun conta(contaId: Int): Conta?
//
//    fun conta(): ListIterator<Conta>

//    fun createConta(text: String, isDefaut: Boolean): Int

    // TODO retornar varios.Com o efeito de like
//    fun contaByText(text: String): Conta?

//

}

/**
 * Database implementation of the facade.
 * Uses Exposed, and either an in-memory H2 database or a file-based H2 database by default.
 * But can be configured.
 */
class DAOFacadeDatabase(val db: Database = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")):
        DAOFacade {
    constructor(dir: File) : this(Database.connect("jdbc:h2:file:${dir.canonicalFile.absolutePath}", driver = "org.h2.Driver"))

    override fun init() {
        db.transaction {
            create(Contas)
        }
    }

    override fun contaByText(text: String, usuario: String) = db.transaction {
        Contas.select { (Contas.text.eq(text)) and (Contas.usuario.eq(usuario)) }
            .map { Conta(it[Contas.id], text, it[Contas.isDefault], it[Contas.usuario]) }
            .singleOrNull()
    }

    override fun createConta(text: String, isDefault: Boolean, usuario: String): Int = db.transaction {
        Contas.insert {
                it[Contas.text] = text
                it[Contas.isDefault] = isDefault
                it[Contas.usuario] = usuario}
            .generatedKey ?: error("Erro na geração da chave de 'Contas'.")
    }
//
    override fun close() {
    }
}
