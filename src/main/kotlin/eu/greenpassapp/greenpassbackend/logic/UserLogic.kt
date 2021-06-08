package eu.greenpassapp.greenpassbackend.logic

import eu.greenpassapp.greenpassbackend.model.User
import javax.persistence.Tuple

interface UserLogic {
    fun insert(certificate: String): Pair<User, String>
    fun update(certificate: String, token: String)
    fun getUser(link: String): User
}