package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Excepciones.UbicacionMuyLejana
import ar.edu.unq.eperdemic.modelo.Excepciones.UbicacionNoAlcanzable
import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDao
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.runner.Neo4jSessionFactoryProvider
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.services.runner.TransactionType
import org.neo4j.driver.Record
import org.neo4j.driver.Session
import org.neo4j.driver.Values

class UbicacionServiceImp(
        private val ubicacionDAO: UbicacionDAO,
        private val ubicacionNeoDao: UbicacionNeo4jDao,
        private val dataDAO: DataDAO,
        private val vectorDAO: VectorDAO,
        private val vectorServiceImp: VectorServiceImp

) : UbicacionService {

    override fun mover(vectorId: Int, nombreUbicacion: String) {

        val vectorRecuperado = vectorServiceImp.recuperarVector(vectorId)
        val ubicacionActual = vectorRecuperado.location
        val ubicacionDestino = this.recuperar(nombreUbicacion)
        val ubicacionesLindantes = this.conectados(ubicacionActual!!.nombreDeLaUbicacion!!)
        val nombresConectados = ubicacionesLindantes.map { it.nombreDeLaUbicacion }

        if (ubicacionDestino.nombreDeLaUbicacion !in nombresConectados) {
            throw UbicacionMuyLejana(ubicacionActual!!.nombreDeLaUbicacion!!, ubicacionDestino.nombreDeLaUbicacion!!)
        }
        val tiposDeCaminoFactibles = vectorRecuperado.estrategiaDeTipo!!.puedeAtravesar()

        var puedeAtravezar: Boolean = false
        for (tipo in tiposDeCaminoFactibles) {
            puedeAtravezar = puedeAtravezar || this.estanConectadasPorCamino(ubicacionActual!!.nombreDeLaUbicacion!!, ubicacionDestino.nombreDeLaUbicacion!!, tipo)
        }
        if (!puedeAtravezar) {
            throw UbicacionNoAlcanzable(vectorRecuperado.tipo!!.name, ubicacionDestino.nombreDeLaUbicacion!!, vectorRecuperado.caminos())
        }

        vectorRecuperado.cambiarDeUbicacion(ubicacionDestino)
        //vectorDAO.actualizar(vector) La comente porque estamos actualizando con el mismo vector que nos pasan, no tiene mucho sentido
        /* if (ubicacionVieja != null) {
             ubicacionDAO.actualizar(ubicacionVieja)
         }*/
        //this.actualizar(ubicacionVieja!!)
        this.actualizar(ubicacionDestino)
        if (vectorRecuperado.estaInfectado()) {
            vectorServiceImp.contagiar(vectorRecuperado, ubicacionDestino.vectores.toList())
        }
    }

    override fun actualizar(ubicacion: Ubicacion) {
        runTrx({ ubicacionDAO.actualizar(ubicacion) }, listOf(TransactionType.HIBERNATE))
    }

    override fun expandir(nombreUbicacion: String) {
        val ubicacion: Ubicacion = this.recuperar(nombreUbicacion)
        val vectores: MutableList<Vector> = ubicacion.vectores.toMutableList()
        val vectoresInfectados: MutableList<Vector> = vectores.filter { it.estaInfectado() }.toMutableList()

        if (vectoresInfectados.isNotEmpty()) {
            val vectorInfectado: Vector = vectoresInfectados[Math.floor(Math.random() * (vectoresInfectados.size)).toInt()]
            ubicacion.actualizarInfectadoseEnUbicacion(vectorInfectado, vectores)
            runTrx({ ubicacionDAO.actualizar(ubicacion) }, listOf(TransactionType.HIBERNATE))
        }
    }

    override fun crearUbicacion(nombre: String): Ubicacion {
        return runTrx({
            val ubicacion = Ubicacion(nombre)
            ubicacionNeoDao.crearUbicacion(ubicacion)
            ubicacionDAO.crear(ubicacion)
        }, listOf(TransactionType.HIBERNATE, TransactionType.NEO4J))
    }

    override fun recuperar(ubicacion: String): Ubicacion {
        return runTrx({ ubicacionDAO.recuperar(ubicacion) }, listOf(TransactionType.HIBERNATE))
    }

    override fun conectar(ubicacion1: String, ubicacion2: String, tipoCamino: String) {
        runTrx({ ubicacionNeoDao.conectar(ubicacion1, ubicacion2, tipoCamino) }, listOf(TransactionType.NEO4J))
    }

    override fun conectados(nombreDeUbicacion: String): List<Ubicacion> {
        return runTrx({ ubicacionNeoDao.conectados(nombreDeUbicacion) }, listOf(TransactionType.NEO4J))
    }

    override fun moverMasCorto(vectorId: Long, nombreDeUbicacion: String) {
        val vector = vectorServiceImp.recuperarVector(vectorId.toInt())
        val tiposCaminos = vector.caminos()
        val ubicActual = vector.location!!.nombreDeLaUbicacion!!
        val caminoMasCorto: List<Ubicacion> = runTrx({ ubicacionNeoDao.caminoMasCorto(tiposCaminos, ubicActual, nombreDeUbicacion) }, listOf(TransactionType.NEO4J))

        print("------------------------------------------------------------------------------------------------------RESULTADO:" + caminoMasCorto)

        if (caminoMasCorto.isEmpty()) {
            throw UbicacionNoAlcanzable(vector.tipo!!.name, nombreDeUbicacion, tiposCaminos)
        }
        for (ubicCamino in caminoMasCorto) {
            this.mover(vector.id!!.toInt(), ubicCamino.nombreDeLaUbicacion!!)
        }
    }

    override fun capacidadDeExpansion(vectorId: Long, movimientos: Int): Int {
        val vector = vectorServiceImp.recuperarVector(vectorId.toInt())
        return runTrx({ ubicacionNeoDao.capacidadDeExpansion(vector, movimientos) }, listOf(TransactionType.NEO4J))
    }

    override fun estanConectadasPorCamino(nombreUbicacionBase: String, nombreUbicacionDestino: String, nombreTipoCamino: String): Boolean {
        return runTrx({ ubicacionNeoDao.estanConectadasPorCamino(nombreUbicacionBase, nombreUbicacionDestino, nombreTipoCamino) }, listOf(TransactionType.NEO4J))
    }

    override fun tipoCaminoEntre(nombreUbicacionBase: String, nombreUbicacionDestino: String): String {
        return runTrx({ ubicacionNeoDao.tipoCaminoEntre(nombreUbicacionBase, nombreUbicacionDestino) }, listOf(TransactionType.NEO4J))
    }

    override fun ubicacionDeVector(vectorId: Long): Ubicacion {
        val vector = vectorServiceImp.recuperarVector(vectorId.toInt())
        return vector.location!!
    }

    override fun existeUbicacion(ubicacionCreada: Ubicacion): Boolean {
        return runTrx({ ubicacionNeoDao.existeUbicacion(ubicacionCreada) }, listOf(TransactionType.NEO4J))
    }
}
