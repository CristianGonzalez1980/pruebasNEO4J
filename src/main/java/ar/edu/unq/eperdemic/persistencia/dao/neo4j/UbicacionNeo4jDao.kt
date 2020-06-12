package ar.edu.unq.eperdemic.persistencia.dao.neo4j

import ar.edu.unq.eperdemic.modelo.Excepciones.UbicacionMuyLejana
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDataDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateUbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImp
import ar.edu.unq.eperdemic.services.impl.VectorServiceImp
import ar.edu.unq.eperdemic.services.runner.Neo4jSessionFactoryProvider
import org.neo4j.driver.*

class UbicacionNeo4jDao {

    private val vectorServiceImp: VectorServiceImp = VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO())
    private val ubicacionServiceImp: UbicacionServiceImp = UbicacionServiceImp(HibernateUbicacionDAO(), UbicacionNeo4jDao(),
            HibernateDataDAO(), HibernateVectorDAO(), VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO()))


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
                MATCH ({ nombreUbicacion:${'$'}nombreBuscado })-[]->(r)
                RETURN r
            """
            val result = session.run(query, Values.parameters("nombreBuscado", nombreDeUbicacion))
            result.list { record: Record ->
                val conectada = record[0]
                val nombreUbicacion = conectada["nombreUbicacion"].asString()
                Ubicacion(nombreUbicacion)
            }
        }
    }

    fun ubicacionDeVector(vector: Vector): Ubicacion {
        return vector.location!!
    }

    fun mover(vectorId: Long, nombreDeUbicacionDestino: String) {
        val vector = vectorServiceImp.recuperarVector(vectorId.toInt())
        val ubicActual = this.ubicacionDeVector(vector)
        val ubicacionesLindantes = this.conectados(ubicActual.nombreDeLaUbicacion!!)
        val nombresConectados = ubicacionesLindantes.map { it.nombreDeLaUbicacion }
        if (nombreDeUbicacionDestino !in nombresConectados){
            UbicacionMuyLejana(ubicActual.nombreDeLaUbicacion!!, nombreDeUbicacionDestino)
        }
        else {
            ubicacionServiceImp.mover(vector.id!!.toInt(), nombreDeUbicacionDestino)
        }
    }

    fun moverMasCorto(vectorId: Long, nombreDeUbicacion: String) {
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        val vector = vectorServiceImp.recuperarVector(vectorId.toInt())
        val tiposCaminos = this.caminosDeVector(vector)
        val ubicActual = this.ubicacionDeVector(vector)
        print("------------------------------------------------------------------------------------------------------------------------------" + tiposCaminos)
        session.use { session ->
            val query = """
                MATCH (ubicP:Ubicacion { nombreUbicacion: ${'$'}ubicActual }),(ubicL:Ubicacion { nombreUbicacion: ${'$'}unaUbicacion }), 
                p = shortestPath((ubicP)-[${'$'}relacion]-(ubicL)) 
                RETURN p
              """
            val result = session.run(query, Values.parameters(
                    "ubicActual", ubicActual.nombreDeLaUbicacion,
                    "unaUbicacion", nombreDeUbicacion,
                    "relacion", tiposCaminos
            ))
            result.list { record: Record ->
                val conectada = record[0]
                val nombreUbicacion = conectada["nombreUbicacion"].asString()
                Ubicacion(nombreUbicacion)
            }
            print("------------------------------------------------------------------------------------------------------RESULTADO:" + result)
            //si la lista es vacia lanzar
            // ubicacionNoAlcanzable
        }
    }

    private fun caminosDeVector(vector: Vector): String {

        val tipoVector = vector.tipo!!.name

        if (tipoVector == "Persona") {
            return ":Terreste|:Maritimo*"
        }

        if (tipoVector == "Insecto") {
            return ":Terrestre|:Aereo*"
        }

        if (tipoVector == "Animal") {
            return ":Terrestre|:Maritimo|:Aereo*"
        }

        return ""
    }

    fun capacidadDeExpansion(vector: Vector, nombreDeUbicacion: String, movimientos: Int): Int {
        return 0
    }

    fun clear() {
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        return session.use { session ->
            session.run("MATCH (n) DETACH DELETE n")
        }
    }
}