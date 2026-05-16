package com.adel.common.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Executes a suspending database operation inside an Exposed transaction
 * on the IO dispatcher.
 *
 * Usage:
 *   suspend fun findById(id: Long) = dbQuery {
 *       MyTable.selectAll().where { MyTable.id eq id }.singleOrNull()
 *   }
 */
suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }