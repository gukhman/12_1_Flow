import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class Person(val name: String, val role: String) {
    override fun toString(): String {
        return "Пользователь: $name, $role"
    }
}

fun getPersonsFlow(): Flow<Person> = flow {
    val persons = listOf(
        Person("Валерий Меладзе", "Менеджер"),
        Person("Владимир Ильич", "Разработчик"),
        Person("Никола́е Чауше́ску", "Дизайнер"),
        Person("Юлий Цезарь", "Аналитик")
    )
    for (person in persons) {
        emit(person)
        delay(100)
    }
}

fun getPhoneFlow(length: Int): Flow<String> = flow {
    repeat(length) {
        val phone = generateRandomPhone()
        emit(phone)
        delay(100)
    }
}

private fun generateRandomPhone(): String {
    val countryCode = "7"
    val phoneStart = "917"
    //padStart используется для дополнения строки до определенной длины с использованием указанного символа
    val randomEnd = (0..99999).random().toString().padStart(7, '0')
    return "+$countryCode$phoneStart$randomEnd"
}

fun main() = runBlocking {
    val personsFlow = getPersonsFlow()
    val phonesFlow = getPhoneFlow(personsFlow.count())

    val persons = mutableListOf<Person>()
    val phones = mutableListOf<String>()

    val personsJob = launch {
        personsFlow.collect { person ->
            persons.add(person)
        }
    }

    val phonesJob = launch {
        phonesFlow.collect { phone ->
            phones.add(phone)
        }
    }

    personsJob.join()
    phonesJob.join()

    val personInfo = persons.zip(phones) { person, phone ->
        "${person}, Телефон: $phone"
    }

    personInfo.forEach { println(it) }
}
