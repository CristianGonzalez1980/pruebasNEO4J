package ar.edu.unq.eperdemic.services.runner

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration

class SessionFactoryProvider private constructor() {

    private val sessionFactory: SessionFactory?

    init {
        val env = System.getenv()
        val user = "matias"//env.getOrDefault("USER", "root")
        val password = "pass1"//env.getOrDefault("PASSWORD", "root")
        val dataBase = env.getOrDefault("DATA_BASE", "epers_hibernate")
        val host = env.getOrDefault("HOST", "localhost")


        val configuration = Configuration()
        configuration.configure("hibernate.cfg.xml")
        configuration.setProperty("hibernate.connection.username", user)
        configuration.setProperty("hibernate.connection.password", password)
        configuration.setProperty("hibernate.connection.url", "jdbc:mysql://$host:3306/$dataBase?createDatabaseIfNotExist=true&serverTimezone=UTC")
        this.sessionFactory = configuration.buildSessionFactory()
    }

    fun createSession(): Session {
        return this.sessionFactory!!.openSession()
    }

    companion object {

        private var INSTANCE: SessionFactoryProvider? = null

        val instance: SessionFactoryProvider
            get() {
                if (INSTANCE == null) {
                    INSTANCE = SessionFactoryProvider()
                }
                return INSTANCE!!
            }

        fun destroy() {
            if (INSTANCE != null && INSTANCE!!.sessionFactory != null) {
                INSTANCE!!.sessionFactory!!.close()
            }
            INSTANCE = null
        }
    }


}