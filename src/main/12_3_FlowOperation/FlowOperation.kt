import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

suspend fun main() {
    println("1."); FlowOperation1().task(); println("----------------------------------")
    println("2."); FlowOperation2().task(); println("----------------------------------")
    println("3."); FlowOperation3().task(); println("----------------------------------")
}

/*1. Дан список целых чисел, который передан в поток. Найти сумму всех его элементов,
возведенных в квадрат. Целесообразно использовать функции map(), reduce().*/
private class FlowOperation1 {
    suspend fun task() {
        val numbersFlow = (1..9).asFlow()
        val newFlow = numbersFlow.map { it * it }.reduce { sum, it -> sum + it }
        println("Сумма квадратов = $newFlow")
    }
}

/*2. Дан список объектов класса Person, состоящий из 10 человек. Каждый участник списка
имеет о себе информацию: имя и возраст. Необходимо написать функцию – расширения
getPerson(first: String, age: Int), которая расширяет Flow <Person>.
Данная функция принимает поток данных и выводит в консоль информацию о Person,
соответствующую условиям, переданным в ее параметры.

В функции main с клавиатуры ввести критерии поиска по первому символу имени и возрасту.
Вызвать функцию getPerson() у объекта, который был передан в поток в виде списка Person,
состоящего из 10 человек.*/
private class FlowOperation2 {
    suspend fun task() {

        val persons = listOf(
            Person("Василий", 16),
            Person("Юлия", 19),
            Person("Андрей", 37),
            Person("Мария", 25),
            Person("Владлен", 52),
            Person("Николай", 34),
            Person("Андромеда", 14),
            Person("Корбен", 28),
            Person("Жанна", 46),
            Person("Евгений", 31)
        ).asFlow()

        print("Введите первый символ имени: ")
        val firstChar = readlnOrNull()?.firstOrNull() ?: 'В'

        print("Введите возраст: ")
        val age = readlnOrNull()?.toIntOrNull() ?: 52

        persons.getPerson(firstChar, age)
    }

    data class Person(val name: String, val age: Int)

    suspend fun Flow<Person>.getPerson(nameStart: Char, age: Int) {
        this.filter { person ->
            person.name.startsWith(nameStart, ignoreCase = true) && person.age == age
        }.collect { person ->
            println("Результат фильтра: $person")
        }
    }
}

/* 3. Программа формирования информации о сотрудниках компании. В отдел кадров поступил список принятых на работу сотрудников.
Существует список банковских карт и паролей к ним по количеству принятых сотрудников.
Все три списка данных будут переданы потоками в отдел кадров для формирования списка полной информации.
Результат: получить список такого вида:
[Person(name=Петр, cart=2981 4664 2788 5634 , password=7828 ), Person(name=Николай, cart=7728 4346 5068 1464 , password=8333 ),
Person(name=Василий, cart=9456 7443 9129 6753 , password=3092 )]

В программе существует класс Person с полями имени, карты и пароля.
Должна быть написана функция получения сгенерированного номера карты и пароля. Полученные номера карт и пароли передаются
в списки карт и паролей соответственно. Данные всех трех списков предаются потоками.
Написать функцию объединения трех потоков, которая принимает на вход три потока first: Flow<T1>, second: Flow<T2>, third: Flow<T3>,
функцию типа suspend (T1, T2, T3) -> R и возвращает Flow<R>, в функции происходит объединение first.zip(second)
и затем .zip(third), в результате чего мы будем получать Person(name, card, password).

В функции main создать список persons, в который будут добавляться объекты Person, которые получаются
при работе функции объединения трех потоков данных списков и получения их с помощью функции collect.
Вывести в консоль полученный полный список данных.*/

private class FlowOperation3 {
    suspend fun task() = runBlocking {

        //список сотрудников
        val names = listOf("Петр", "Николай", "Василий")

        //поток сотрудников
        val personsFlow = flow {
            names.forEach { name ->
                emit(name)
            }
        }

        //поток карт для каждого сотрудника
        val cardsFlow = flow {
            names.forEach { _ ->
                emit(generateCardNumber())
            }
        }

        //поток паролей для каждого сотрудника
        val passwordsFlow = flow {
            names.forEach { _ ->
                emit(generatePassword())
            }
        }

        //Объединение 3 потоков
        val persons = mergeFlows(personsFlow, cardsFlow, passwordsFlow) { name, cart, password ->
            Person(name, cart, password)
        }

        val resultList = persons.toList()
        println(resultList)
    }

    data class Person(val name: String, val card: String, val password: String)

    fun generateCardNumber(): String {
        return List(4) { Random.nextInt(1000, 10000).toString() }.joinToString(" ")
    }

    fun generatePassword(): String {
        return Random.nextInt(99_999, 999_999).toString()
    }

    suspend fun <T1, T2, T3, R> mergeFlows(
        first: Flow<T1>,
        second: Flow<T2>,
        third: Flow<T3>,
        combine: suspend (T1, T2, T3) -> R
    ): Flow<R> {
        return first.zip(second) { item1, item2 -> item1 to item2 }
            .zip(third) { (item1, item2), item3 ->
                combine(item1, item2, item3)
            }
    }
}