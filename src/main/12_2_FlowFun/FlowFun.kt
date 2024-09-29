import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.system.measureTimeMillis


fun main() = runBlocking {
    print("Введите количество пользователей: ")
    val length = readlnOrNull()?.toIntOrNull() ?: 100

    print("Какой первый символ пароля?: ")
    val input = readlnOrNull()?.take(1) ?: "X"

    val idFlow = getIdFlow(length)
    val passwordFlow = getPasswordFlow(input, length)

    val time = measureTimeMillis {
        val identifiers = mutableListOf<String>()
        val passwords = mutableListOf<String>()

        val idJob = launch {
            idFlow.collect { identifiers.add(it) }
        }

        val passwordJob = launch {
            passwordFlow.collect { passwords.add(it) }
        }

        idJob.join()
        passwordJob.join()

        val userMap = identifiers.zip(passwords).toMap()

        println("ID -> пароль: $userMap")
    }

    println("Затраченное время: $time ms")
}

//Создаем пароль из 6 символов
fun createPassword(): String {
    val alphabet = ('a'..'z') + ('A'..'Z') + ('а'..'я') + ('А'..'Я') + ('0'..'9')
    var password = ""

    for (i in 1..6) {
        val char = alphabet.random()
        password += if (i % 2 == 0 && char.isLetter()) char.uppercaseChar() else char
    }

    return password
}

fun getListOfPassword(input: String, length: Int): List<String> {
    val passwordList = mutableListOf<String>()

    while (passwordList.size < length) {
        val password = createPassword()
        // В задании указано "Т.е. генерироваться будут разные, но помещать в список нужно только начинающиеся на эту букву."
        //Сделаем как в задании, но это сильно замедлит работу программы, т.к. генерироваться будут всевозможные пароли
        //при 100 000 затраченное время было 32 секунды
        if (password[0].toString() == input) passwordList.add(password)
    }

    return passwordList
}

fun getListId(length: Int): List<String> {
    return (1..length).map { it.toString().padStart(6, '0') }
}

fun getIdFlow(length: Int): Flow<String> = flow {
    val identifiers = getListId(length)
    identifiers.forEach { emit(it) }
}

fun getPasswordFlow(input: String, length: Int): Flow<String> = flow {
    val passwords = getListOfPassword(input, length)
    passwords.forEach { emit(it) }
}