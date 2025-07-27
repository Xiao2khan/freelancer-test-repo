package com.respiroc.webapp.controller.web

import com.respiroc.user.application.UserService
import com.respiroc.webapp.controller.BaseController
import com.respiroc.webapp.service.JwtService
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam


data class LoginRequest(val email: String, val password: String)
data class SignupRequest(val email: String, val password: String)

@Controller
@RequestMapping("/auth")
class AuthWebController : BaseController() {

    @GetMapping("/login")
    fun loginPage(model: Model): String {
        if (isUserLoggedIn())
            return "redirect:/"
        model.addAttribute(titleAttributeName, "Login")
        return "auth/login"
    }

    @GetMapping("/signup")
    fun signupPage(model: Model): String {
        if (isUserLoggedIn())
            return "redirect:/"
        model.addAttribute(titleAttributeName, "Sign Up")
        return "auth/signup"
    }

    @GetMapping("/logout")
    fun logout(
        @CookieValue("token", required = true) token: String,
        response: HttpServletResponse
    ): String {
        val jwtCookie = Cookie("token", "")
        jwtCookie.isHttpOnly = true
        jwtCookie.secure = false
        jwtCookie.path = "/"
        jwtCookie.maxAge = 0
        response.addCookie(jwtCookie)

        return "redirect:/auth/login"
    }
}

@Controller
@RequestMapping("/htmx/auth")
class AuthHTMXController(
    private val userService: UserService,
    private val jwt: JwtService
) : BaseController() {

    @PostMapping("/login")
    @HxRequest
    fun loginHTMX(
        @ModelAttribute loginRequest: LoginRequest,
        response: HttpServletResponse
    ): String {
        val result = userService.loginByEmailPassword(
            email = loginRequest.email,
            password = loginRequest.password
        )
        val token = jwt.generateToken(subject = result.id.toString(), tenantId = result.tenantId)
        setJwtCookie(token, response)
        return "redirect:htmx:/"
    }

    @PostMapping("/signup")
    @HxRequest
    fun signupHTMX(
        @ModelAttribute signupRequest: SignupRequest,
        response: HttpServletResponse,
        model: Model
    ): String {
        val result = userService.signupByEmailPassword(
            signupRequest.email,
            signupRequest.password
        )
        val token = jwt.generateToken(subject = result.id.toString(), tenantId = result.tenantId)
        setJwtCookie(token, response)
        return "redirect:htmx:/"
    }

    @PostMapping("/select-tenant")
    @HxRequest
    fun selectTenant(
        @RequestParam(value = "tenantId", required = true) tenantId: Long,
        response: HttpServletResponse
    ): String {
        val user = user()
        userService.selectTenant(user, tenantId)
        val token = jwt.generateToken(subject = user.id.toString(), tenantId = tenantId)
        setJwtCookie(token, response)
        return "redirect:htmx:/"
    }
}