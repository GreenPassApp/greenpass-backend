package eu.greenpassapp.greenpassbackend.dto

import eu.greenpassapp.greenpassbackend.model.User

data class UserWithCountryCode(val user: User, val requestCountryCode: String) {
}