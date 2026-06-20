# Flight Database

A catalogue of the static aviation world — the airlines, airports, airplane models, and the scheduled connections between them. Future plans include a runtime feed of in-flight activity, but the persistent core is the schedule and the entities behind it.

## Language

### Operations

**Route**:
A static, recurring scheduled connection operated by an airline from a start airport to a destination airport, identified by a route number that is unique per airline (e.g. "LH 400"). One-way — the reverse direction (e.g. JFK→FRA) is a separate Route with its own number. A Route has a single assigned airplane model at any given time; equipment substitutions on a particular day belong to a Flight, not to the Route. Exists whether anyone is flying it today or not.
_Avoid_: Scheduled flight, service, connection, city-pair (a city-pair is bidirectional; a Route is not)

**Flight** *(future)*:
A concrete in-progress or historical instance of a Route on a particular date — e.g. "LH 400 on 2026-06-15". Lives in the planned Kafka feed, not in the schedule. Carries runtime state — position along the great-circle arc, percent complete, on-time vs. delayed — computed against the system clock and **never persisted**. A Flight is born when the system clock reaches its Route's scheduled departure.
_Avoid_: Leg, trip (those are passenger-itinerary concepts and do not belong here)

**Flight lifecycle (OOOI)**:
The runtime states a Flight passes through, named with the industry-standard OOOI milestones. All ephemeral, never persisted.
- **Out** — off the gate (off-block); taxi-out begins. A Flight enters the live system here.
- **Off** — wheels up; now airborne and travelling the great-circle arc.
- **On** — wheels down at the destination.
- **In** — at the arrival gate (on-block); the Flight leaves the live system here.
*Taxi-out* is Out→Off; *taxi-in* is On→In. Both have real duration; only taxi-out can end in cancellation. A Flight is **Cancelled** if ground delay exceeds its cap *before Out*, or during *taxi-out* (after Out, before Off). Once airborne (Off), a Flight always reaches On/In in this model — diversions are out of scope. Out is the actual counterpart of the Route's scheduled Out; the difference between them is delay.
_Avoid_: Departed (ambiguous — say Out for off-block or Off for wheels-up), Arrived (say On for wheels-down or In for on-block)

**Schedule**:
The persisted intended timing of a Route. At minimum the scheduled **Out** time — gate push-back, the published departure (STD) — which is what makes Emirates EK45 and EK47 (both DXB→FRA) distinct Routes rather than duplicates. Optionally a scheduled **In** time (gate arrival, STA); the gap between them is block time. An attribute of Route, not a separate entity. The Schedule is *intent*; the actual Out/Off/On/In a Flight achieves are runtime state, computed against the system clock and never persisted.
_Avoid_: Timetable (implies a whole published document), departure time (ambiguous — name the OOOI point: scheduled Out)

**HubCity**:
A city an airline considers a home base, hub, or operational footprint. An airline can have many hub cities (e.g. Lufthansa → Frankfurt *and* Munich), and a city can be a hub for many airlines — hence the many-to-many. Independent of which routes happen to touch the city today. (The persisted join table is named `AirlineCity` for join-table convention, but the domain concept is HubCity.)
_Avoid_: Base, operational footprint, served city

**Fleet**:
The set of airplane models an airline operates.

**FleetEntry**:
One model in an airline's Fleet — a single (Airline, AirplaneModel) row. A Route's assigned model must be a FleetEntry of the same airline; the FK chain (`AirlineRoute.airlineAirplaneId` → `AirlineAirplane` row) enforces this. (The persisted join table is named `AirlineAirplane` for join-table convention, but the domain concept is FleetEntry.)
_Avoid_: Aircraft assignment, operated model

### Equipment

**AirplaneModel**:
A make-and-model of airplane (e.g. A380, 747-400, ATR 72), defined by its manufacturer, capacity, and max range. Not a physical aircraft — there are no tail numbers in this domain. (The persisted entity is named `Airplane` in code; the domain concept is AirplaneModel.)
_Avoid_: Airplane (ambiguous between model and physical aircraft), aircraft type

**Aircraft** / **Airframe** *(reserved)*:
Reserved for a future physical tail-numbered aircraft, distinct from AirplaneModel. Not in the schema today. Likely arrives alongside Flight.

### Places

**JunctionAirport**:
An airport flagged as a major connecting node in general — high transit volume regardless of carrier (e.g. Atlanta, Frankfurt). A *per-airport* property, distinct from HubCity, which is *per-airline*. An airport can be a JunctionAirport without being any specific airline's hub, and vice versa. Persisted as the `junction` boolean on `Airport`.
_Avoid_: Hub airport (ambiguous — could mean per-airline), connecting airport

**AirportCapacity**:
An airport's annual passenger throughput, measured in passengers per year (PPY). The published figure, not real-time or peak-concurrent. Persisted as `Airport.capacity`.
_Avoid_: Throughput, peak capacity (ambiguous — those mean different things)

**AirplaneCapacity**:
The maximum passenger seats supported by an AirplaneModel in its highest-density configuration — a manufacturer-published property of the model itself. Persisted as `Airplane.capacity`. Not to be confused with AirportCapacity (different entity, different unit).
_Avoid_: Seat count (ambiguous — could mean a specific airline's layout)

**SeatLayout** *(future)*:
Planned addition to capture a *typical* or airline-configured seat count for a (Airline, AirplaneModel) pair — distinct from AirplaneCapacity (the model's max). Belongs on FleetEntry, not on AirplaneModel, since it's per-airline.

**ManufacturerBase**:
The city an AirplaneModel manufacturer is corporately headquartered in (e.g. Boeing → Arlington, Airbus → Leiden). Updated on corporate relocations; not necessarily where assembly happens. Persisted as `Manufacturer.baseCityId`.
_Avoid_: HQ city (ambiguous — could mean operational vs legal HQ), home city, factory city

**CountryLanguages**:
A country's official languages, ranked by official-status priority — main is constitutionally the primary official language; secondary and tertiary are co-official languages when they exist. Currently capped at three slots on `Country` (`mainLanguageId`, `secondaryLanguageId`, `tertiaryLanguageId`); countries with more co-official languages (e.g. South Africa, India) only have their top three captured. The cap is provisional; a future `CountryLanguage` join table may replace the three-column layout.
_Avoid_: Spoken languages, popular languages (these would imply speaker-count ranking, not official status)
