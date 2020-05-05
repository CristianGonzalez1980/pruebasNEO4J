package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO

open class HibernateUbicacionDAO : HibernateDAO<Ubicacion>(Ubicacion::class.java), UbicacionDAO {

    override fun crear(ubicacion: Ubicacion) : Ubicacion{
        this.guardar(ubicacion)
        return this.recuperar(ubicacion.id)
    }



}