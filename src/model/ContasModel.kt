package com.example.model

import io.ktor.features.BadRequestException
import java.io.Serializable


data class Conta(val contaId: Int, val text: String, val isDefaut: Boolean) : Serializable

data class PostConta( val contasDoPost: List<ContaDoPost>) {
    data class ContaDoPost(val text: String, val isDefaut: Boolean) {

    }

    fun isValid(): Boolean {
        contasDoPost.forEach {
            when {
                it.text.isBlank() -> throw BadRequestException("'text' está em branco.")
//                else -> {
//
//                }
            }
        }

        return true
    }
}




