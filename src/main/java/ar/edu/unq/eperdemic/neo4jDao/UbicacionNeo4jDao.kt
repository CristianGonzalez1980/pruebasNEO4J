package ar.edu.unq.eperdemic.neo4jDao

import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion
import org.neo4j.driver.*

class UbicacionNeo4jDao {

    private val driver: Driver

    init {
        val env = System.getenv()
        val url = env.getOrDefault("URL", "bolt://localhost:11005")
        val username = env.getOrDefault("USERe", "neo4j")
        val password = env.getOrDefault("PASSWORD", "root")

        driver = GraphDatabase.driver(url, AuthTokens.basic(username, password),
                Config.builder().withLogging(Logging.slf4j()).build()
        )
    }

    fun crearUbicacion(ubicacion: Ubicacion) {

        driver.session().use { session ->
            session.writeTransaction {
                val query = "MERGE (ubi:Ubicacion {nombreUbicacion: ${'$'}unaUbicacion}) "
                it.run(query, Values.parameters(
                        "unaUbicacion", ubicacion.nombreDeLaUbicacion
                ))
            }
        }
    }

    fun existeUbicacion(ubicacion: Ubicacion): Boolean {

        driver.session().use { session ->
            //{nombreUbicacion: ${'$'}unaUbicacion}
            val query = """  MATCH (ubi:Ubicacion {nombreUbicacion: ${'$'}unaUbicacion }) RETURN ubi """
            val result = session.run(
                    query, Values.parameters(
                    "unaUbicacion", ubicacion.nombreDeLaUbicacion
            )
            )
            return result.single().size() == 1
        }
    }

    fun conectar(ubicacion1: String, ubicacion2: String, tipoCamino: String) {

        // esto no esta terminado, es una idea porque no estoy segura lo que hace

//        var ubi1 = Ubicacion(ubicacion1)
//        var ubi2 = Ubicacion(ubicacion2)
//        this.crearUbicacion(ubi1)
//        this.crearUbicacion(ubi2)
        driver.session().use { session ->
            val query = """
                MATCH (conectar:Ubicacion {nombreUbicacion: ${'$'}unaUbicacion})
                MATCH (conectado:Ubicacion {nombreUbicacion: ${'$'}otraUbicacion})
                MERGE (conectar)-[:camino {type: ${'$'}tCamino}]->(conectado)
                MERGE (conectado)-[:camino {type: ${'$'}tCamino}]->(conectar)
            """
            session.run(
                    query, Values.parameters(
                    "unaUbicacion", ubicacion1/*ubi1.nombreDeLaUbicacion*/,
                    "otraUbicacion", ubicacion2/*ubi2.nombreDeLaUbicacion*/,
                    "tCamino", tipoCamino
            )
            )
        }
    }

    fun estaConectadaPorCamino(ubicacion1: Ubicacion, ubicacion2: Ubicacion, tipoCamino: TipoDeCamino): Boolean {
        return driver.session().use { session ->
            val query = """
                MATCH (ubi1:Ubicacion {nombreUbicacion: ${'$'}nombreUbi1})
                MATCH (ubi2:Ubicacion {nombreUbicacion: ${'$'}nombreUbi2})
                MATCH (ubi1)-[:camino {type: ${'$'}tCamino}]->(ubi2)
                RETURN ubi2
            """
            val result = session.run(query, Values.parameters(
                    "nombreUbi1", ubicacion1.nombreDeLaUbicacion,
                    "nombreUbi2", ubicacion2.nombreDeLaUbicacion,
                    "tCamino", tipoCamino.name
            ))
            return (result != null) //revisar query da siempre verde
        }
    }

    fun conectados(nombreDeUbicacion:String): List<Ubicacion> {
        return driver.session().use { session ->
            val query = """
                MATCH (ubi:Ubicacion {nombreUbicacion: ${'$'}nombreDeUbicacion}) 
                MATCH (conectada)-[:camino]->(ubi)
                RETURN conectada
            """
            val result = session.run(query, Values.parameters("nombreDeUbicacion", nombreDeUbicacion))
            result.list { record: Record ->
                val conectada = record[0]
                val nombreUbicacion = conectada["nombreUbicacion"].asString()
                Ubicacion(nombreUbicacion)
            }
        }
    }

    fun clear() {
        return driver.session().use { session ->
            session.run("MATCH (n) DETACH DELETE n")
        }
    }

}