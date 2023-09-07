import io.github.serpro69.kfaker.Faker
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.util.PGInterval
import kotlin.random.Random
import kotlin.random.nextULong

val faker = Faker()
const val defaultValue = 0

fun main(args: Array<String>) {
    val parser = ArgParser("railway")
    val url by parser.option(ArgType.String).default("jdbc:postgresql://localhost:5432/railway")
    val stationsNumber by parser.option(ArgType.Int, shortName = "s").default(defaultValue)
    val passengersNumber by parser.option(ArgType.Int, shortName = "p").default(defaultValue)
    val routesNumber by parser.option(ArgType.Int, shortName = "r").default(defaultValue)
    val trainsNumber by parser.option(ArgType.Int, shortName = "t").default(defaultValue)
    val ordersNumber by parser.option(ArgType.Int, shortName = "o").default(defaultValue)
    parser.parse(args)

    require(stationsNumber >= 0) { "Negative Value: stations" }
    require(passengersNumber >= 0) { "Negative Value: passengers" }
    require(routesNumber >= 0) { "Negative Value: routes" }
    require(trainsNumber >= 0) { "Negative Value: trains" }
    require(ordersNumber >= 0) { "Negative Value: orders" }

    connectDatabase(url)

    println("New stations: $stationsNumber")
    println("New passengers: $passengersNumber")
    println("New routes: $routesNumber")
    println("New trains: $trainsNumber")
    println("New orders: $ordersNumber")
    println()

    generateWagonClasses()
    generateStations(stationsNumber)
    generatePassengers(passengersNumber)
    generateFullRoutes(routesNumber)
    generateTrains(trainsNumber)
    generateOrders(ordersNumber)

    removeUnlinkedPassengersAndOrders()
}

fun connectDatabase(url: String) {
    Database.connect(
        url = url,
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "postgres"
    )
}

fun generateWagonClasses() {
    transaction {
        addLogger(StdOutSqlLogger)
        if (WagonClasses.selectAll().empty()) {
            WagonClasses.insert {
                it[name] = "common"
                it[capacity] = 32
                it[costMultiplier] = 1f
            }
            WagonClasses.insert {
                it[name] = "lux"
                it[capacity] = 16
                it[costMultiplier] = 1.5f
            }
            WagonClasses.insert {
                it[name] = "ultra"
                it[capacity] = 4
                it[costMultiplier] = 20f
            }
            WagonClasses.insert {
                it[name] = "seat"
                it[capacity] = 48
                it[costMultiplier] = 0.7f
            }
        }
    }
}

fun generateStations(n: Int) {
    transaction {
        addLogger(StdOutSqlLogger)
        repeat(n) {
            Stations.insert {
                it[name] = faker.address.unique.city()
                it[latitude] = (Random.nextFloat() - 0.5f) * 180f
                it[longitude] = (Random.nextFloat() - 0.5f) * 360f
            }
        }
    }
}

fun generatePassengers(n: Int) {
    transaction {
        addLogger(StdOutSqlLogger)
        repeat(n) {
            Passengers.insertIgnore {
                val g = faker.gender.shortBinaryTypes().first()
                it[firstName] = if (g == 'm') faker.name.maleFirstName() else faker.name.femaleFirstName()
                it[lastName] = faker.name.lastName()
                it[gender] = g
                it[document] = "PASSPORT NO ${Random.nextULong()}"
                it[phoneNumber] = if (Random.nextBoolean()) faker.phoneNumber.cellPhone() else null
            }
        }
    }
}

fun generateFullRoutes(
    n: Int,
    maxStationsBetween: Int = 8,
    maxCostPerSection: Float = 1000f,
    maxTravelMinutes: Int = 24 * 60,
    maxWaitMinutes: Int = 60
) {
    transaction {
        addLogger(StdOutSqlLogger)
        val stations = Stations.selectAll().toList()
        repeat(n) {
            val slice = stations.shuffled().take(2 + Random.nextInt(maxStationsBetween))
            val first = slice.first()
            val last = slice.last()
            val routeId = Routes.insertIgnoreAndGetId {
                it[name] = "${first[Stations.name]} > ${last[Stations.name]}"
                it[firstStationId] = first[Stations.id]
                it[lastStationId] = last[Stations.id]
            }
            var departureTime = DateTimePeriod(minutes = Random.nextInt(maxTravelMinutes))
            if (routeId != null) {
                for (pair in slice.windowed(2)) {
                    val destinationTime =
                        departureTime + DateTimePeriod(minutes = 1 + Random.nextInt(maxTravelMinutes))
                    val departureTimePG = PGInterval(0, 0, departureTime.hours / 24, departureTime.hours % 24, departureTime.minutes, .0)
                    val destinationTimePG = PGInterval(0, 0, destinationTime.hours / 24, destinationTime.hours % 24, destinationTime.minutes, .0)
                    exec(
                        "INSERT INTO \"public\".route_sections " +
                                "(\"cost\", departure_station_id, departure_time, destination_station_id, destination_time, route_id) " +
                                "VALUES (${maxCostPerSection * Random.nextFloat()}, " +
                                "${pair.first()[Stations.id]}, '$departureTimePG', " +
                                "${pair.last()[Stations.id]}, '$destinationTimePG', " +
                                "${routeId.value})"
                    )
                    departureTime = destinationTime + DateTimePeriod(minutes = Random.nextInt(maxWaitMinutes))
                }
            }
        }
    }
}

fun generateTrains(n: Int, maxLength: Int = 16) {
    transaction {
        addLogger(StdOutSqlLogger)
        val routes = Routes.selectAll().toList()
        val wagonClasses = WagonClasses.selectAll().toList()
        repeat(n) {
            val len = Random.nextInt(1, maxLength)
            val trainId = Trains.insertAndGetId {
                it[routeId] = routes.random()[Routes.id]
                it[length] = len
                it[departureDate] = randomDate()
            }
            for (i in 1..len) {
                TrainWagons.insert {
                    it[TrainWagons.trainId] = trainId
                    it[positionInTrain] = i
                    it[wagonClassId] = wagonClasses.random()[WagonClasses.id]
                }
            }
        }
    }
}

fun generateOrders(n: Int, maxTickets: Int = 8) {
    val timeZone = TimeZone.UTC
    transaction {
        addLogger(StdOutSqlLogger)
        val passengers = Passengers.selectAll().toList()
        for (i in 0 until n) {
            val orderTime = randomDateTime()
            val orderTimestamp = orderTime.toInstant(timeZone)
            val trains = Trains.select {
                Trains.departureDate greater orderTime.date
            }.toList()
            if (trains.isEmpty()) continue
            val orderId = Orders.insertAndGetId {
                it[creationTime] = orderTimestamp
                it[email] = faker.internet.email()
                it[phoneNumber] = if (Random.nextBoolean()) faker.phoneNumber.cellPhone() else null
            }
            for (j in 0..Random.nextInt(maxTickets)) {
                val train = trains.random()

                val wagonNumber = 1 + Random.nextInt(0, train[Trains.length])
                val wagonClassId = TrainWagons.select {
                    TrainWagons.trainId.eq(train[Trains.id]) and TrainWagons.positionInTrain.eq(wagonNumber)
                }.first()[TrainWagons.wagonClassId]
                val wagonClass = WagonClasses.select {
                    WagonClasses.id eq wagonClassId
                }.first()

                val routeSections = RouteSections.select {
                    RouteSections.routeId.eq(train[Trains.routeId].value)
                }.orderBy(RouteSections.departureTime to SortOrder.ASC).toList()
                val first = Random.nextInt(0, routeSections.lastIndex + 1)
                val last = Random.nextInt(first, routeSections.lastIndex + 1)

                val departureTime = routeSections[first][RouteSections.departureTime] as PGInterval
                val destinationTime = routeSections[last][RouteSections.destinationTime] as PGInterval

                val departureRealTime = train[Trains.departureDate]
                    .atTime(0, 0, 0)
                    .toInstant(timeZone)
                    .plus(24 * departureTime.days + departureTime.hours, DateTimeUnit.HOUR)
                    .plus(departureTime.minutes, DateTimeUnit.MINUTE)
                    .toLocalDateTime(timeZone)
                val destinationRealTime = train[Trains.departureDate]
                    .atTime(0, 0, 0)
                    .toInstant(timeZone)
                    .plus(24 * destinationTime.days + destinationTime.hours, DateTimeUnit.HOUR)
                    .plus(destinationTime.minutes, DateTimeUnit.MINUTE)
                    .toLocalDateTime(timeZone)

                val capacity = wagonClass[WagonClasses.capacity]
                val seat = 1 + Random.nextInt(0, capacity)
                val occupied = Tickets.select {
                    Tickets.trainId.eq(train[Trains.id]) and
                            Tickets.wagonNumber.eq(wagonNumber) and
                            Tickets.seat.eq(seat) and
                            (Tickets.departureTime.between(departureRealTime, destinationRealTime) or
                                    Tickets.destinationTime.between(departureRealTime, destinationRealTime))
                }.empty().not()
                if (occupied) continue

                val departureStationId = routeSections[first][RouteSections.departureStationId]
                val destinationStationId = routeSections[last][RouteSections.destinationStationId]

                var price = 0f
                for (i in first..last) {
                    price += routeSections[i][RouteSections.cost] * wagonClass[WagonClasses.costMultiplier]
                }

                Tickets.insert {
                    it[Tickets.trainId] = train[Trains.id]
                    it[Tickets.orderId] = orderId.value
                    it[Tickets.passengerId] = passengers.random()[Passengers.id].value
                    it[Tickets.departureStationId] = departureStationId
                    it[Tickets.departureTime] = departureRealTime
                    it[Tickets.destinationStationId] = destinationStationId
                    it[Tickets.destinationTime] = destinationRealTime
                    it[Tickets.wagonNumber] = wagonNumber
                    it[Tickets.seat] = seat
                    it[Tickets.pricePaid] = price.toBigDecimal()
                }
            }
        }
    }
}

fun removeUnlinkedPassengersAndOrders() {
    transaction {
        addLogger(StdOutSqlLogger)
        Orders.deleteWhere {
            Orders.id.notInSubQuery(Tickets.slice(Tickets.orderId).selectAll())
        }
        Passengers.deleteWhere {
            Passengers.id.notInSubQuery(Tickets.slice(Tickets.passengerId).selectAll())
        }
    }
}

fun randomDateTime() = LocalDateTime(
    2022,
    Random.nextInt(1, 12),
    Random.nextInt(1, 29),
    Random.nextInt(24),
    Random.nextInt(60),
    Random.nextInt(60)
)

fun randomDate() = LocalDate(2022, Random.nextInt(1, 12), Random.nextInt(1, 29))