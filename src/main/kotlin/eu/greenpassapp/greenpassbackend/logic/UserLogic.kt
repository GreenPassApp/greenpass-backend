package eu.greenpassapp.greenpassbackend.logic

import eu.greenpassapp.greenpassbackend.model.User

interface UserLogic {
    fun insert(certificate: String): Pair<User, String>
    fun update(certificate: String, token: String)
    fun getUser(link: String): User
    fun delete(token: String)
    fun generatePressKit(certificate: String, serialNumber: String): ByteArray
}