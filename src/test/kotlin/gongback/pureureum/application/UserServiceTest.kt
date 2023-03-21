package gongback.pureureum.application

import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.existsByEmail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class UserServiceTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val smsLogService = mockk<SmsLogService>()
    val bCryptPasswordEncoder = mockk<BCryptPasswordEncoder>()

    val userService = UserService(userRepository, smsLogService, bCryptPasswordEncoder)

    Given("회원가입 정보") {
        val registerReq = createRegisterReq()
        When("이미 존재하는 아이디이면") {
            every { userRepository.existsByEmail(registerReq.email) } returns true
            Then("예외가 발생한다.") {
                shouldThrow<IllegalStateException> {
                    userService.register(registerReq)
                }
            }
        }

        When("인증 받지 않은 전화번호일 경우") {
            every { userRepository.existsByEmail(registerReq.email) } returns false
            every { smsLogService.isCertification(registerReq.phoneNumber) } returns false

            Then("예외가 발생한다.") {
                shouldThrow<IllegalStateException> {
                    userService.register(registerReq)
                }
            }
        }

        When("존재하지 않는 아이디이면서 본인 인증한 전화번호인 경우") {
            every { userRepository.existsByEmail(registerReq.email) } returns false
            every { smsLogService.isCertification(registerReq.phoneNumber) } returns true
            every { bCryptPasswordEncoder.encode(registerReq.password) } returns "encodedPassword"
            every { userRepository.save(any()) } returns createUser()

            Then("성공한다.") {
                userService.register(registerReq)
            }
        }
    }
})