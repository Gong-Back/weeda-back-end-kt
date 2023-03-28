package gongback.pureureum.presentation.api

import gongback.pureureum.application.UserAuthenticationService
import gongback.pureureum.application.dto.EmailReq
import gongback.pureureum.application.dto.LoginReq
import gongback.pureureum.application.dto.RegisterUserReq
import gongback.pureureum.domain.user.User
import gongback.pureureum.security.LoginUser
import gongback.pureureum.security.RefreshToken
import gongback.pureureum.support.security.Tokens.Companion.REFRESH_TOKEN_HEADER
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserRestController(
    private val userAuthenticationService: UserAuthenticationService
) {
    @PostMapping("/login")
    fun login(
        @RequestBody @Valid loginReq: LoginReq,
        response: HttpServletResponse
    ): ResponseEntity<Unit> {
        userAuthenticationService.validateAuthentication(loginReq)

        response.setHeader(
            HttpHeaders.AUTHORIZATION,
            userAuthenticationService.generateAccessTokenByEmail(loginReq.email)
        )
        response.setHeader(
            REFRESH_TOKEN_HEADER,
            userAuthenticationService.generateRefreshTokenByEmail(loginReq.email)
        )
        return ResponseEntity.ok().build()
    }

    @PostMapping("/register")
    fun register(
        @RequestBody @Valid registerUserReq: RegisterUserReq
    ): ResponseEntity<ApiResponse<String>> {
        userAuthenticationService.register(registerUserReq)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PostMapping("/validate/email")
    fun checkDuplicatedEmail(
        @RequestBody @Valid emailReq: EmailReq
    ): ResponseEntity<Unit> {
        userAuthenticationService.checkDuplicatedEmailOrNickname(emailReq.email)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/reissue/token")
    fun reissueToken(
        @RefreshToken refreshToken: String,
        response: HttpServletResponse
    ): ResponseEntity<Unit> {
        response.setHeader(
            HttpHeaders.AUTHORIZATION,
            userAuthenticationService.generateTokenByRefreshToken(refreshToken)
        )
        return ResponseEntity.ok().build()
    }

    @GetMapping("/test")
    fun test(
        @LoginUser user: User
    ): String {
        return "Test Success!"
    }
}
