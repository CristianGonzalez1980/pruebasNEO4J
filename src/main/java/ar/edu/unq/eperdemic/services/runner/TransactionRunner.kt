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

object TransactionRunner {
    private var transactions:List<Transaction> = listOf(HibernateTransaction())

    fun <T> runTrx(bloque: ()->T): T {
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