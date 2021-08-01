package com.pablotolentino.donaciones.builders

import modelos.DonacionModel

class DonacionModelBuilder {

    private var  monto: Double = 0.0

    companion object {
        fun CrearBuilder(): DonacionModelBuilder {
            return DonacionModelBuilder()
        }
    }

    fun agregarMonto(monto:Double):DonacionModelBuilder{
        this.monto = monto
        return this
    }
    fun crear():DonacionModel{
        var donacion = DonacionModel()
        donacion.monto = this.monto
        return donacion
    }

}