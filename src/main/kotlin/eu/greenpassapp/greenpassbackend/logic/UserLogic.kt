package eu.greenpassapp.greenpassbackend.logic

import eu.greenpassapp.greenpassbackend.model.User

interface UserLogic {
    fun insertOrUpdate(user: User): User
}