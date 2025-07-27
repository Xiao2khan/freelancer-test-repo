package com.respiroc.webapp.controller.advice

import com.respiroc.util.exception.BaseException
import com.respiroc.util.exception.MissingTenantContextException
import com.respiroc.webapp.controller.BaseController
import com.respiroc.webapp.controller.response.Callout
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.RestClientException
import org.springframework.web.servlet.ModelAndView


@ControllerAdvice(basePackages = ["com.respiroc.webapp.controller.web"])
class WebExceptionHandler : BaseController() {

    @ExceptionHandler(MissingTenantContextException::class)
    fun handleMissingTenantContext(): ModelAndView {
        return ModelAndView("redirect:/tenant/create")
    }

    @ExceptionHandler(value = [BaseException::class, IllegalArgumentException::class, IllegalStateException::class])
    fun handleBaseException(ex: RuntimeException, model: Model, response: HttpServletResponse): String {
        response.status = HttpStatus.BAD_REQUEST.value()
        model.addAttribute(calloutAttributeName, Callout.Error(ex.message!!))
        return "fragments/r-callout"
    }

    @ExceptionHandler(RestClientException::class)
    fun handleRestClientException(model: Model): String {
        model.addAttribute(calloutAttributeName, Callout.Error("REST API Client Error"))
        return "fragments/r-callout"
    }
}