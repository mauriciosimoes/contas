package com.example.negocio

import com.example.NotFoundException
import com.example.RouteContas
import com.example.RouteContas_contaText
import com.example.dao.DAOFacade
import com.example.model.Conta
import com.example.model.PostConta
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.features.BadRequestException
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.Logger


@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.routeContas(dao: DAOFacade, log: Logger) {
    authenticate {
        get<RouteContas_contaText> {
            log.trace("a" + it.contaText + "a")
            val conta = dao.contaByText(it.contaText)

            if (conta == null) {
                throw NotFoundException( "Conta não encontrada." )
            } else {
                call.respond(mapOf("OK" to true, "conta" to conta))
            }
        }

        post<RouteContas> {
            val userIdPrincipal =
                call.principal<UserIdPrincipal>() ?: error("Informação de autenticação não encontrada no POST.")

            val post = try {
                call.receive<PostConta>()
            } catch (e: JsonParseException) {
                throw BadRequestException(e.toString())
            } catch (e: JsonMappingException) {
                throw BadRequestException(e.toString())
            } catch (e: MismatchedInputException) {
                throw BadRequestException(e.toString())
            } catch (e: MissingKotlinParameterException) {
                throw BadRequestException(e.toString())
            }

            post.isValid()

            val contasResponse = mutableListOf<Conta>()

            post.contasDoPost.forEach {
                val idContaLocal = dao.createConta(it.text, it.isDefaut, userIdPrincipal.name)
                contasResponse.add( Conta(idContaLocal, it.text, it.isDefaut, userIdPrincipal.name) )
            }

            call.respond( mapOf("OK" to true, "contas" to contasResponse) )
        }
    }
}

