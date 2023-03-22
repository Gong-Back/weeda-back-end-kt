package support

import gongback.pureureum.application.dto.RegisterUserReq
import gongback.pureureum.domain.user.Gender
import gongback.pureureum.domain.user.Password
import gongback.pureureum.domain.user.Role
import gongback.pureureum.domain.user.User
import java.time.LocalDate

const val NAME: String = "회원"
const val EMAIL: String = "testEmail"
const val PHONE_NUMBER: String = "010-0000-0000"
val GENDER: Gender = Gender.MALE
val BIRTHDAY: LocalDate = createLocalDate(1998, 12, 28)
val PASSWORD: Password = Password("passwordTest")

fun createUser(
    name: String = NAME,
    email: String = EMAIL,
    phoneNumber: String = PHONE_NUMBER,
    gender: Gender = GENDER,
    birthday: LocalDate = BIRTHDAY,
    password: Password = PASSWORD,
    id: Long = 0L
): User {
    return User(name, email, phoneNumber, gender, birthday, password, Role.ROLE_USER, id)
}

fun createRegisterReq(
    email: String = EMAIL,
    password: Password = PASSWORD,
    name: String = NAME,
    gender: Gender = GENDER,
    phoneNumber: String = PHONE_NUMBER,
    birthday: LocalDate = BIRTHDAY
): RegisterUserReq {
    return RegisterUserReq(email, password, name, gender, phoneNumber, birthday)
}
