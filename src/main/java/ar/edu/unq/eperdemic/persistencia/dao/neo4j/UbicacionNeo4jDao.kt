package ar.edu.unq.eperdemic.persistencia.dao.neo4j

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.services.runner.Neo4jSessionFactoryProvider
import org.neo4j.driver.*

class UbicacionNeo4jDao {

    private var contadorIdVector: Int = -1 //Forma rapida de tener un control de ids para vectores creados


    fun crearUbicacion(ubicacion: Ubicacion) {
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        session.use { session ->
            session.writeTransaction {
                val query = "MERGE (ubi:Ubicacion {nombreUbicacion: ${'$'}unaUbicacion}) "
                it.run(query, Values.parameters(
                        "unaUbicacion", ubicacion.nombreDeLaUbicacion
                ))
            }
        }
    }

    fun existeUbicacion(ubicacion: Ubicacion): Boolean {
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        session.use { session ->
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
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        val camino = tipoCamino
        session.use { session ->
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
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        session.use { session ->
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

    fun tipoCaminoEntre(nombreUbicacionBase: String, nombreUbicacionDestino: String): String {
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        session.use { session ->
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
            val resultado = (result.list { record: Record -> record[0] })[0].asString()
            return resultado
        }
    }

    fun conectados(nombreDeUbicacion: String): List<Ubicacion> {
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        return session.use { session ->
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

    fun relacionarUbicacion(vector: Vector, ubicacion: Ubicacion) {
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        session.use { session ->
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

    fun ubicacionDeVector(vector: Vector): Ubicacion {
        val nombreBuscado = vector.location!!.nombreDeLaUbicacion
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        session.use { session ->
            val query = """
                MATCH (ubicacion:Ubicacion { nombreUbicacion: ${'$'}unNombre})
                RETURN ubicacion
            """
            val result = session.run(query, Values.parameters("unNombre", nombreBuscado))
            return (result.list() { record: Record ->
                val ubi = record[0]
                val nombreUbicacion = ubi["nombreUbicacion"].asString()
                Ubicacion(nombreUbicacion) //No estoy seguro si tiene que instanciar o recuperar de hiberante ese nombre
            })[0]
        }
    }

    fun moverMasCorto(vectorId: Long, nombreDeUbicacion: String) { //Revisar si es int o long el vectorId!!!
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        val idVector = vectorId.toInt()
        val listCaminos = this.caminosDeVector(vectorId).toMutableList()
        println(listCaminos)
        session.use { session ->
            val query = """
                MATCH (vec:Vector  { id: ${'$'}unId }), 
                 (ubi:Ubicacion { nombreUbicacion:${'$'}unaUbicacion }),
                 p = shortestPath((vec)-[*..15]-(ubi))
                 MATCH ()-[r]->(ubi) WHERE r.tipo IN (${'$'}relacion)
                RETURN p
              """
            session.run(query, Values.parameters(
                    "unId", idVector,
                    "unaUbicacion", nombreDeUbicacion,
                    "relacion", listCaminos
            ))
        }
    }

    private fun caminosDeVector(vectorId: Long): List<String> {
        val vector = this.recuperarVector(vectorId)
        val tipoVector = vector.tipo!!.name
        val caminos: ArrayList<String> = ArrayList()
        if (tipoVector == "Persona") {
            caminos.add("Terreste")
            caminos.add("Maritimo")
        } else {
            if (tipoVector == "Insecto") {
                caminos.add("Terrestre")
                caminos.add("Aereo")
            } else {
                caminos.add("Terrestre")
                caminos.add("Maritimo")
                caminos.add("Aereo")
            }
        }
        return caminos
    }

    fun clear() {
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        return session.use { session ->
            session.run("MATCH (n) DETACH DELETE n")
        }
    }
}