package gongback.pureureum.presentation.api

import gongback.pureureum.application.UserAuthenticationService
import gongback.pureureum.application.UserService
import gongback.pureureum.application.dto.EmailReq
import gongback.pureureum.application.dto.LoginReq
import gongback.pureureum.application.dto.RegisterUserReq
import gongback.pureureum.application.dto.UserInfoReq
import gongback.pureureum.application.dto.UserInfoRes
import gongback.pureureum.presentation.api.CookieProvider.Companion.addTokenToSecureCookie
import gongback.pureureum.security.JwtNotExistsException
import gongback.pureureum.security.LoginEmail
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/users")
class UserRestController(
    private val userService: UserService,
    private val userAuthenticationService: UserAuthenticationService
) {
    @PostMapping("/login")
    fun login(
        @RequestBody @Valid loginReq: LoginReq,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Unit> {
        userAuthenticationService.validateAuthentication(loginReq)
        val tokenRes = userAuthenticationService.getTokenRes(loginReq.email)
        addTokenToSecureCookie(tokenRes, servletResponse)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/register")
    fun register(
        @RequestBody @Valid registerUserReq: RegisterUserReq
    ): ResponseEntity<Unit> {
        userAuthenticationService.register(registerUserReq)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PostMapping("/validate/email")
    fun checkDuplicatedEmail(
        @RequestBody @Valid emailReq: EmailReq
    ): ResponseEntity<Unit> {
        userAuthenticationService.checkDuplicatedEmailOrNickname(emailReq.email)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me")
    fun getUserInfo(
        @LoginEmail email: String
    ): ResponseEntity<ApiResponse<UserInfoRes>> {
        val userInfo = userService.getUserInfoWithProfileUrl(email)
        return ResponseEntity.ok().body(ApiResponse.ok(userInfo))
    }

    @PostMapping("/update/info")
    fun updateUserInfo(
        @RequestBody @Valid userInfoReq: UserInfoReq,
        @LoginEmail email: String
    ): ResponseEntity<Unit> {
        userService.updateUserInfo(email, userInfoReq)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/update/profile")
    fun updateProfile(
        @RequestPart profile: MultipartFile?,
        @LoginEmail email: String
    ): ResponseEntity<Unit> {
        userService.updatedProfile(email, profile)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/reissue-token")
    fun reissueToken(
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Unit> {
        val bearerToken = servletRequest.getHeader(HttpHeaders.AUTHORIZATION) ?: throw JwtNotExistsException()
        val tokenRes = userAuthenticationService.reissueToken(bearerToken)
        addTokenToSecureCookie(tokenRes, servletResponse)
        return ResponseEntity.noContent().build()
    }
}
