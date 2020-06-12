package ar.edu.unq.eperdemic.utils.Impl

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDao
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImp
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImp
import ar.edu.unq.eperdemic.services.impl.VectorServiceImp
import ar.edu.unq.eperdemic.services.runner.TransactionType
import ar.edu.unq.eperdemic.utils.DataService

class DataServiceImp(private val dataDAO: DataDAO, private val neo4jDao: UbicacionNeo4jDao = UbicacionNeo4jDao()) : DataService {
    override fun crearSetDeDatosIniciales() {
        val patservice: PatogenoService = PatogenoServiceImp(HibernatePatogenoDAO(), HibernateDataDAO())
        val vecservice: VectorService = VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO() /*HibernateEspecieDAO()*/)
        val ubiservice: UbicacionService = UbicacionServiceImp(HibernateUbicacionDAO(), UbicacionNeo4jDao(), HibernateDataDAO(), HibernateVectorDAO(), vecservice as VectorServiceImp)
        val idpatogeno = patservice.crearPatogeno(Patogeno("Virus", 50, 50, 50))
        val especie: Especie = patservice.agregarEspecie(idpatogeno, "Vaca Loca", "Holanda", 15)
        val maracaibo: Ubicacion = ubiservice.crearUbicacion("Maracaibo")
        val vector: Vector = vecservice.crearVector(Vector(maracaibo, VectorFrontendDTO.TipoDeVector.Persona))
        vecservice.infectar(vector, especie)
        vecservice.enfermedades(vector.id!!.toInt())
    }

    override fun eliminarTodo() {
        runTrx ({ dataDAO.clear() }, listOf(TransactionType.HIBERNATE))
        runTrx ({ neo4jDao.clear() }, listOf(TransactionType.NEO4J))
    }
}

fun main() {

    val set: DataService = DataServiceImp(HibernateDataDAO())
    set.eliminarTodo()
    set.crearSetDeDatosIniciales()
    //  val especieDAO: EspecieDAO = HibernateEspecieDAO()
    //  println(runTrx { especieDAO.especieLider() })
}