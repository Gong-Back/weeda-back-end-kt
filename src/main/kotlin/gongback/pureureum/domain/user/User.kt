package gongback.pureureum.domain.user

import jakarta.persistence.*

@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Embedded
    var information: UserInformation,
) {
    val name: String
        get() = information.name

    val email: String
        get() = information.email

    constructor(
        name: String,
        email: String,
        id: Long = 0L
    ) : this(
        id, UserInformation(name, email)
    )
}