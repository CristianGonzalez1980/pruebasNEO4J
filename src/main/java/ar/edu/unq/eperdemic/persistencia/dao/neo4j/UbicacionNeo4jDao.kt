package ar.edu.unq.eperdemic.persistencia.dao.neo4j

import ar.edu.unq.eperdemic.modelo.Excepciones.UbicacionMuyLejana
import ar.edu.unq.eperdemic.modelo.Excepciones.UbicacionNoAlcanzable
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
                MERGE (conectar)-[:""" + camino + "]->(conectado)"
            session.run(
                    query, Values.parameters(
                    "unaUbicacion", ubicacion1,
                    "otraUbicacion", ubicacion2//,
                    //"tCamino", tipoCamino
            )
            )
        }
    }

    fun estanConectadasPorCamino(nombreUbicacionBase: String, nombreUbicacionDestino: String, nombreTipoCamino: String): Boolean {
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        session.use { session ->
            val query = """
                MATCH (ubicBase { nombreUbicacion:${'$'}nombreUbicBase })-[caminos]->(ubicDestino { nombreUbicacion:${'$'}nombreUbicDestino })
                RETURN type(caminos) = ${'$'}tipoCaminoBuscado
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
                RETURN type(relacion)
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

    fun caminoMasCorto(tiposCaminos: String, nombreDeUbicacionActual: String, nombreDeUbicacionDestino: String): List<Ubicacion> {
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        print("------------------------------------------------------------------------------------------------------------------------------" + tiposCaminos)
        return session.use { session ->
            val query = """
                MATCH p=shortestPath((ubicP:Ubicacion { nombreUbicacion: ${'$'}ubicActual })-[tiposCaminos]->(ubicL:Ubicacion { nombreUbicacion: ${'$'}unaUbicacion })) RETURN p
              """
            val result = session.run(query, Values.parameters(
                    "ubicActual", nombreDeUbicacionActual,
                    "unaUbicacion", nombreDeUbicacionDestino,
                    "relacion", tiposCaminos
            ))
            print("------------------------------------------------------------------------------------------------------RESULTADO:" + result)
            result.list { record: Record ->
                val conectada = record[0]
                val nombreUbicacion = conectada["nombreUbicacion"].asString()
                Ubicacion(nombreUbicacion)
            }
        }
    }

    fun capacidadDeExpansion(vector: Vector, movimientos: Int): Int {

        var session: Session = Neo4jSessionFactoryProvider.instance.createSession()

        val ubicacionActual = vector.location!!.nombreDeLaUbicacion
        var cantUbicaciones: Int? = null
        //val ubicacionesConectadas = this.conectados(ubicacionDelVector.nombreDeLaUbicacion!!)
        //val ubicacionActual = ubicacionesConectadas.get(0)
        session.use { session ->
            val query = """
                MATCH (ubi:Ubicacion { nombreUbicacion:${'$'}unaUbicacion }),(u:Ubicacion),
                 (ubi)-[*..movimientos]-(u)
                RETURN u
              """
            val result = session.run(query, Values.parameters(
                    "unaUbicacion", ubicacionActual, "movimientos", movimientos))

            cantUbicaciones = result.list().size
        }
        return cantUbicaciones!!
    }

    fun clear() {
        val session: Session = Neo4jSessionFactoryProvider.instance.createSession()
        return session.use { session ->
            session.run("MATCH (n) DETACH DELETE n")
        }
    }
}