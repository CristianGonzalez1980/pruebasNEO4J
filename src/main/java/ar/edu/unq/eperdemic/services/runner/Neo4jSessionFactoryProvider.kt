package ar.edu.unq.eperdemic.services.runner

import org.neo4j.driver.*

class Neo4jSessionFactoryProvider private constructor() {

    private val driver: Driver

    init {
        val env = System.getenv()
        val url = env.getOrDefault("URL", "bolt://localhost:7687")
        val username = env.getOrDefault("USERe", "neo4j")
        val password = env.getOrDefault("PASSWORD", "root")

        driver = GraphDatabase.driver(url, AuthTokens.basic(username, password),
                Config.builder().withLogging(Logging.slf4j()).build()
        )
    }

    fun createSession(): Session {
        return this.driver.session()
    }

    companion object {

        private var INSTANCE: Neo4jSessionFactoryProvider? = null

        val instance: Neo4jSessionFactoryProvider
            get() {
                if (INSTANCE == null) {
                    INSTANCE = Neo4jSessionFactoryProvider()
                }
                return INSTANCE!!
            }

        fun destroy() {
            if (INSTANCE != null && INSTANCE!!.driver != null) {
                INSTANCE!!.driver!!.close()
            }
            INSTANCE = null
        }
    }
}