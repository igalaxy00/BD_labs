import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.castTo
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Stations : IntIdTable("public.stations") {
    val name = varchar("name", 50)
    val latitude = float("latitude")
    val longitude = float("longitude")
}

object Orders : IntIdTable("public.orders") {
    val creationTime = timestamp("creation_time")
    val email = varchar("email", 255)
    val phoneNumber = varchar("phone_number", 50).nullable()
}

object Passengers : IntIdTable("public.passengers") {
    val firstName = varchar("first_name",50)
    val lastName = varchar("last_name", 50)
    val gender = char("gender")
    val document = varchar("document", 255)
    val phoneNumber = varchar("phone_number", 50).nullable()
}

object Routes : IntIdTable("public.routes") {
    val name = varchar("name", 100)
    val firstStationId = reference("first_station_id", Stations)
    val lastStationId = reference("last_station_id", Stations)
}

object RouteSections : IntIdTable("public.route_sections") {
    val routeId = integer("route_id").references(Routes.id)
    val departureStationId = reference("departure_station_id", Stations)
    val departureTime = text("departure_time")
    val destinationStationId = reference("destination_station_id", Stations)
    val destinationTime = text("destination_time")
    val cost = float("cost")
}

object WagonClasses : IntIdTable("public.wagon_classes") {
    val name = varchar("name", 50).uniqueIndex()
    val capacity = integer("capacity").check { it greater 0 }
    val costMultiplier = float("cost_multiplier").check { it greater 0 }
}

object Trains : IntIdTable("public.trains") {
    val routeId = reference("route_id", Routes)
    val length = integer("length")
    val departureDate = date("departure_date")
}

object TrainWagons : Table("public.train_wagons") {
    val trainId = reference("train_id", Trains)
    val positionInTrain = integer("position_in_train")
    val wagonClassId = reference("wagon_class_id", WagonClasses)
    override val primaryKey = PrimaryKey(trainId, positionInTrain)
}

object Tickets : IntIdTable("public.tickets") {
    val trainId = reference("train_id", Trains)
    val orderId = reference("order_id", Orders)
    val passengerId = reference("passenger_id", Passengers)
    val departureStationId = reference("departure_station_id", Stations)
    val departureTime = datetime("departure_time")
    val destinationStationId = reference("destination_station_id", Stations)
    val destinationTime = datetime("destination_time")
    val wagonNumber = integer("wagon_number")
    val seat = integer("seat")
    val pricePaid = decimal("price_paid", 19, 4)
}