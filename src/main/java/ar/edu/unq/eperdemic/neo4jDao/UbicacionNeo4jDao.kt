package ar.edu.unq.eperdemic.neo4jDao

import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import org.neo4j.driver.*

class UbicacionNeo4jDao {

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

    fun crearVector(vector: Vector) {

        driver.session().use { session ->
            session.writeTransaction {
                val query = "MERGE (vec:Vector { tipo: ${'$'}unTipo  }) "
                it.run(query, Values.parameters(
                        "unTipo", vector.tipo!!.name
                ))
            }
        }
    }

    fun existeVector(vector: Vector): Any? {

        driver.session().use { session ->
            //{nombreUbicacion: ${'$'}unaUbicacion}
            val query = """  MATCH (vec:Vector { tipo: ${'$'}unTipo  }) RETURN vec """
            val result = session.run(
                    query, Values.parameters(
                    "unTipo", vector.tipo!!.name
            )
            )
            return result.single().size() == 1
        }

    }

    fun relacionarUbicacion(vector: Vector, ubicacion: Ubicacion) {

        driver.session().use { session ->
            val query = """
                MATCH (ubi:Ubicacion {nombreUbicacion: ${'$'}unaUbicacion}) 
                MATCH (vec:Vector { tipo: ${'$'}unTipo})
                MERGE (vec)-[:ubicacionActual]->(ubi)
                
            """
            session.run(
                    query, Values.parameters(
                    "unaUbicacion", ubicacion.nombreDeLaUbicacion,
                    "unTipo", vector.tipo!!.name

            )
            )
        }

    }

    fun ubicacionesDeVector(vector: String): List<Ubicacion> {

        return driver.session().use { session ->
            val query = """
                MATCH (vec:Vector { tipo: ${'$'}unTipo})
                MATCH (vec)-[:ubicacionActual]->(ubi)
                RETURN ubi
            """
            val result = session.run(query, Values.parameters("unTipo", vector))
            result.list { record: Record ->
                val ubi = record[0]
                val nombreUbicacion = ubi["nombreUbicacion"].asString()
                Ubicacion(nombreUbicacion)
            }
        }

    }

    fun moverMasCorto(vectorId: String, nombreDeUbicacion: String) {

        var ubicaciones = this.ubicacionesDeVector(vectorId)
        var ubicacion = this.encontarUbicacionEnUbicacionMasCercana(nombreDeUbicacion,ubicaciones)



    }

    private fun encontarUbicacionEnUbicacionMasCercana(nombreDeUbicacion: String, ubicaciones: List<Ubicacion>): Ubicacion {

        var res : Ubicacion? = null
        var cantDePasos = 0
        for (ubi: Ubicacion in ubicaciones){

            if (cantDePasos > (this.cantidadDepasosHastaUbi(ubi, nombreDeUbicacion))) {
                   cantDePasos += this.cantidadDepasosHastaUbi(ubi, nombreDeUbicacion)
                   res = ubi
            }
            else cantDePasos +=1
        }
        return res!!
    }

    private fun cantidadDepasosHastaUbi(ubi: Ubicacion, nombreDeUbicacion: String): Int {
       // var ubibacionBuscada = Ubicacion(nombreDeUbicacion)
        var cant = 0
       var  ubicacionesConectadas = conectados(ubi.nombreDeLaUbicacion!!)
        for (ubicacion :Ubicacion in ubicacionesConectadas){

            if (ubicacion.nombreDeLaUbicacion != nombreDeUbicacion){

                cant += 1
            }

        }

        return cant
        }


    }







