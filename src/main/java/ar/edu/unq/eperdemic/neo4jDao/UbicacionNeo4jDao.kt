package ar.edu.unq.eperdemic.neo4jDao
import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import org.neo4j.driver.*

class UbicacionNeo4jDao {

    private val driver: Driver
    private var contadorIdVector: Int = -1 //Forma rapida de tener un control de ids para vectores creados

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
        val camino = tipoCamino
        driver.session().use { session ->
            val query = """
                MATCH (conectar:Ubicacion { nombreUbicacion:${'$'}unaUbicacion })
                MATCH (conectado:Ubicacion { nombreUbicacion:${'$'}otraUbicacion })
                MERGE (conectar)-[:""" + camino + "{ tipo:${'$'}tCamino }]->(conectado)"
            session.run(
                    query, Values.parameters(
                        "unaUbicacion", ubicacion1,
                        "otraUbicacion", ubicacion2,
                        "tCamino", tipoCamino
                    )
            )
        }
    }

    fun estanConectadasPorCamino(nombreUbicacionBase: String, nombreUbicacionDestino: String, nombreTipoCamino: String): Boolean {
         driver.session().use { session ->
            val query = """
                MATCH (ubicBase { nombreUbicacion:${'$'}nombreUbicBase })-[caminos]->(ubicDestino { nombreUbicacion:${'$'}nombreUbicDestino })
                RETURN caminos.tipo = ${'$'}tipoCaminoBuscado
            """
            val result = session.run(
                        query, Values.parameters(
                        "nombreUbicBase", nombreUbicacionBase,
                        "nombreUbicDestino", nombreUbicacionDestino,
                        "tipoCaminoBuscado", nombreTipoCamino
                    )
            )
            return (result.list { record: Record -> record[0] })[0].asBoolean()
         }
    }

    fun tipoCaminoEntre(nombreUbicacionBase: String, nombreUbicacionDestino: String): String{
        driver.session().use { session ->
            val query = """
                MATCH (ubicBase { nombreUbicacion:${'$'}nombreUbicBase })-[relacion]->(ubicDestino { nombreUbicacion:${'$'}nombreUbicDestino })
                RETURN relacion.tipo
            """
            val result = session.run(
                    query, Values.parameters(
                        "nombreUbicBase", nombreUbicacionBase,
                        "nombreUbicDestino", nombreUbicacionDestino
                    )
            )
            val resultado = (result.list { record: Record -> record[0]})[0].asString()
            return resultado
        }
    }

    fun conectados(nombreDeUbicacion:String): List<Ubicacion> {
        return driver.session().use { session ->
            val query = """
                MATCH ({ nombreUbicacion:${'$'}nombreDeUbicacion })-[]->(r)
                RETURN r
            """
            val result = session.run(query, Values.parameters("nombreDeUbicacion", nombreDeUbicacion))
            result.list { record: Record ->
                val conectada = record[0]
                val nombreUbicacion = conectada["nombreUbicacion"].asString()
                Ubicacion(nombreUbicacion)
            }
        }
    }

    fun crearVector(vector: Vector): Vector {
        val idVector = contadorIdVector + 1
        val tipoVector = vector.tipo!!.name
        driver.session().use { session ->
            session.writeTransaction {
                val query = "MERGE (vec:Vector { id:${'$'}idDeVectorACrear, tipo:${'$'}unTipo })"
                it.run(query, Values.parameters(
                        "idDeVectorACrear", idVector,
                        "unTipo", tipoVector
                ))
            }
        }
        vector.id = idVector.toLong()
        return vector
    }

    fun recuperarVector(vectorId: Int): Vector{
        driver.session().use { session ->
            val query = """
            MATCH (vector:Vector { id: ${'$'}idBuscado }) RETURN vector.tipo
            """
            val result = session.run(query, Values.parameters("idBuscado", vectorId))
            val nombreTipoVector = (result.list { record: Record -> record[0]})[0].asString()
            var tipoACrear = VectorFrontendDTO.TipoDeVector.Persona
            if (nombreTipoVector == "Animal"){
                tipoACrear = VectorFrontendDTO.TipoDeVector.Animal
            }
            if (nombreTipoVector == "Insecto"){
                tipoACrear = VectorFrontendDTO.TipoDeVector.Insecto
            }
            val ubicacion = this.ubicacionDeVector(vectorId)
            val vectorRecuperado = Vector(ubicacion, tipoACrear)
            vectorRecuperado.setId(vectorId.toLong())
            return vectorRecuperado
        }
    }

    fun existeVector(vectorIdBuscado: Int): Boolean {
        driver.session().use { session ->
            //val query = """  MATCH (vec:Vector { tipo: ${'$'}unTipo  }) RETURN vec """
            val query = "MATCH (vector:Vector { id: ${'$'}idBuscado }) RETURN vector"
            /*val result = session.run(
                    query, Values.parameters(
                    "unTipo", vector.name
            )*/
            val result = session.run(
                    query, Values.parameters(
                    "idBuscado", vectorIdBuscado
            )
            )
            //return result.single().size() == 1
            return result.list().isNotEmpty()
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

    fun ubicacionesDeVector(vectorId: Int): List<Ubicacion> {
        return driver.session().use { session ->
            val query = """
                MATCH (vec:Vector { id: ${'$'}unId})
                MATCH (vec)-[:ubicacionActual]->(ubi)
                RETURN ubi
            """
            val result = session.run(query, Values.parameters("unId", vectorId))
            result.list { record: Record ->
                val ubi = record[0]
                val nombreUbicacion = ubi["nombreUbicacion"].asString()
                Ubicacion(nombreUbicacion)
            }
        }
    }

    fun ubicacionDeVector(vectorId: Int): Ubicacion {
            driver.session().use { session ->
            val query = """
                MATCH (vec:Vector { id: ${'$'}unId})
                MATCH (vec)-[:ubicacionActual]->(ubi)
                RETURN ubi
            """
            val result = session.run(query, Values.parameters("unId", vectorId))
            return (result.list() { record: Record ->
                val ubi = record[0]
                val nombreUbicacion = ubi["nombreUbicacion"].asString()
                Ubicacion(nombreUbicacion)
            })[0]
        }
    }

    fun moverMasCorto(vectorId: Long, nombreDeUbicacion: String) { //Revisar si es int o long el vectorId!!!
        val idVector = vectorId.toInt()
        val listCaminos = this.caminosDeVector(vectorId).toMutableList()
        println(listCaminos)

        //val ubicActual = this.ubicacionDeVector(vectorId.toInt())
        /*val ubicConectadas = this.conectados(ubicActual.nombreDeLaUbicacion!!)
        for (ubicDestino in ubicConectadas){
            val tipoCamino = tipoCaminoEntre(ubicActual.nombreDeLaUbicacion!!, ubicDestino.nombreDeLaUbicacion!!)
            //if (vector.estrategiaDeContagio!!.puedePasarPor(tipoCamino))
            // No se puede usar la funcion porque no esta en la super clase pero tampoco se puede ponerla
        }*/
        //
         driver.session().use { session ->
            val query = """
                MATCH (vec:Vector  { id: ${'$'}unId }), 
                 (ubi:Ubicacion { nombreUbicacion:${'$'}unaUbicacion }),
                 p = shortestPath((vec)-[*..15]-(ubi))
                 MATCH ()-[r]->(ubi) WHERE r.tipo IN  (relacion)
                RETURN p

              """
             session.run(query, Values.parameters(
                    "unId", idVector,
                    "unaUbicacion", nombreDeUbicacion,
                     "relacion",listCaminos

            ))
        }
    }

    private fun caminosDeVector(vectorId: Long): List<String> {

        val vector = this.recuperarVector(vectorId.toInt())
        val tipoVector = vector.tipo!!.name
        var caminos: ArrayList<String> = ArrayList()

           if(tipoVector == "Persona") {
                caminos.add("Terreste")
                caminos.add("Maritimo")
               return caminos
            }
           if(tipoVector == "Insecto") {
                caminos.add("Terrestre")
                caminos.add("Aereo")
                return caminos

           }
           if(tipoVector == "Animal") {
                caminos.add("Terrestre")
                caminos.add("Maritimo")
                caminos.add("Aereo")
                return caminos


            }
            return caminos
        }




    fun clear() {
        return driver.session().use { session ->
            session.run("MATCH (n) DETACH DELETE n")
        }
    }
}







