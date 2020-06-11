package ar.edu.unq.eperdemic.services.runner

import org.hibernate.Session

interface Transaction {
    fun start()
    fun commit()
    fun rollback()
}

class HibernateTransaction: Transaction{
    private var transaction: org.hibernate.Transaction? = null

    companion object {
        private var session: Session? = null
        val currentSession: Session
            get() {
                if (session == null) {
                    throw RuntimeException("No hay ninguna session en el contexto")
                }
                return session!!
            }
    }

    override fun start() {
        session = HibernateSessionFactoryProvider.instance.createSession()
        transaction = session?.beginTransaction()
    }

    override fun commit() {
        transaction?.commit()
        session?.close()
    }

    override fun rollback() {
        transaction?.rollback()
        session?.close()
    }
}

class Neo4jTransaction: Transaction{
    private var transaction: org.neo4j.driver.Transaction? = null

    companion object {
        private var session: org.neo4j.driver.Session? = null
        val currentSession: org.neo4j.driver.Session
            get() {
                if (session == null) {
                    throw RuntimeException("No hay ninguna session en el contexto")
                }
                return session!!
            }
    }

    override fun start() {
        session = Neo4jSessionFactoryProvider.instance.createSession()
        transaction = session?.beginTransaction()
    }

    override fun commit() {
        transaction?.commit()
        session?.close()
    }

    override fun rollback() {
        transaction?.rollback()
        session?.close()
    }
}

enum class TransactionType {
    HIBERNATE {
        override fun getTransaction(): Transaction {
            return HibernateTransaction()
        }
    },
    NEO4J {
        override fun getTransaction(): Transaction {
            return Neo4jTransaction()
        }
    };

    abstract fun getTransaction(): Transaction
}

object TransactionRunner {
    private var transactions:List<Transaction> = listOf()

    fun <T> runTrx(bloque: ()->T, types: List<TransactionType> = listOf()): T {
        transactions = types.map { it.getTransaction() }
        try{
            transactions.forEach { it.start() }
            val result = bloque()
            transactions.forEach { it.commit() }
            return result
        } catch (exception:Throwable){
            transactions.forEach { it.rollback() }
            throw exception
        }
    }
}