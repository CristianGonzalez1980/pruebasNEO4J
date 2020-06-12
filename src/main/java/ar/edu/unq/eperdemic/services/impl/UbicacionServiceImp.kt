package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.neo4jDao.UbicacionNeo4jDao
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.services.runner.TransactionType
import kotlin.math.roundToInt

class UbicacionServiceImp(
        private val ubicacionDAO: UbicacionDAO,
        private val ubicacionNeoDao: UbicacionNeo4jDao,
        private val dataDAO: DataDAO,
        private val vectorDAO: VectorDAO,
        private val vectorServiceImp: VectorServiceImp

) : UbicacionService {

    override fun mover(vectorId: Int, nombreUbicacion: String) {

        val vectorRecuperado = vectorServiceImp.recuperarVector(vectorId)
        val ubicacionVieja = vectorRecuperado.location
        val ubicacionNueva = this.recuperar(nombreUbicacion)

        vectorRecuperado.cambiarDeUbicacion(ubicacionNueva)
        //vectorDAO.actualizar(vector) La comente porque estamos actualizando con el mismo vector que nos pasan, no tiene mucho sentido
        /* if (ubicacionVieja != null) {
             ubicacionDAO.actualizar(ubicacionVieja)
         }*/

        //this.actualizar(ubicacionVieja!!)
        this.actualizar(ubicacionNueva)
        if (vectorRecuperado.estaInfectado()) {
            vectorServiceImp.contagiar(vectorRecuperado, ubicacionNueva.vectores.toList())
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

        runTrx({ ubicacionNeoDao.conectar(ubicacion1, ubicacion2, tipoCamino) })
    }

    override fun conectados(nombreDeUbicacion: String): List<Ubicacion> {
        TODO("Not yet implemented")
    }

    override fun moverMasCorto(vectorId: Long, nombreDeUbicacion: String) {
        TODO("Not yet implemented")
    }

/*
    fun evaluarNombre(nombreTipoCamino: String): TipoDeCamino {
        val caminos: List<TipoDeCamino> = listOf(TipoDeCamino.Maritimo, TipoDeCamino.Terrestre, TipoDeCamino.Aereo)
        var caminoSolicitado: TipoDeCamino? = null
        for (camino in caminos) {
            if (camino.name == nombreTipoCamino) {
                caminoSolicitado = camino
            }
        }
        return caminoSolicitado!!
    }
*/

    override fun capacidadDeExpansion(vectorId: Long, nombreDeUbicacion: String, movimientos: Int): Int {
        TODO("Not yet implemented")
    }
}
