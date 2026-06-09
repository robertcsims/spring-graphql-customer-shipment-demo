# Customer + Shipment GraphQL Prototype

**Executive / Leadership Demo Ready**

Fully functional Spring Boot + Spring for GraphQL demonstration built directly from a detailed schema specification. Shows the power of GraphQL for complex relational domains and the speed at which it can be delivered with modern tooling.

See [DEMO_GUIDE.md](DEMO_GUIDE.md) for the recommended presentation script and talking points.

Built with the latest stable libraries (Spring Boot 3.3.4, Spring for GraphQL, etc.).

## What This Prototype Demonstrates (The Power of GraphQL)

- **One round-trip for complex relational data** — Fetch a customer + all contacts + locations + shipments + every shipment's contact/location/serviceOffering in a **single query**.
- **Client-controlled selection** — Ask for exactly the fields you need. No over/under-fetching.
- **Pagination, sorting, and filtering at the API level** (root queries and sub-selections).
- **Full schema introspection** + beautiful interactive UI (GraphiQL).
- **Mutations** for create/update/delete.
- **Real persistence** — Derby embedded file database (`./data/derbydb`). Restart the app and your data is still there.
- **Proper layered architecture** with services, repositories, and tests for every implementation class.
- **Spring Security** (HTTP Basic) protecting the GraphQL endpoint and GraphiQL.

## Tech Stack (Latest Stable as of 2026)

- Java 21
- Spring Boot 3.3.4 (parent)
- `spring-boot-starter-graphql`
- `spring-boot-starter-data-jpa` + Hibernate
- `spring-boot-starter-security`
- Apache Derby (embedded, file-based for persistence)
- Maven
- JUnit 5 + AssertJ + Spring GraphQL Test + Spring Security Test

## Project Structure

```
src/main/java/com/example/graphql/
├── CustomerShipmentGraphqlApplication.java
├── config/
│   └── DataSeeder.java
├── domain/               # 5 JPA entities + 2 enums (exact tables requested)
├── repository/           # Spring Data JPA + Specifications for dynamic filters
├── service/              # Interfaces + *Impl (all have tests)
├── graphql/
│   ├── dto/              # Input records for mutations & filters
│   └── resolver/         # The GraphQL heart (queries, mutations, nested resolvers)
└── security/
```

## Quick Start

**Best experience for leadership demos:**

```bash
./demo.sh
```

(This is a one-line wrapper script that launches the app in demo mode — no auth friction, clean console.)

Alternative (explicit):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

Normal mode:
```bash
mvn spring-boot:run
```

App starts on **http://localhost:8080**

- Professional landing page: **http://localhost:8080**
- **GraphiQL**: **http://localhost:8080/graphiql**
- GraphQL endpoint: **http://localhost:8080/graphql**

### Login (when not using demo profile)

Use `admin` / `admin123`.

- `admin` / `admin123` (ADMIN role)
- `viewer` / `viewer123` (USER role)

GraphiQL will prompt you for credentials in the browser. For curl / other clients, use:

```bash
curl -u admin:admin123 -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ customers { content { id entityName type } } }"}'
```

## Powerful GraphQL Examples (Copy-Paste into GraphiQL)

### 1. The Killer Feature — Complete Relational Hierarchy in ONE Request

```graphql
query FullCustomerHierarchy {
  customer(id: 1) {
    id
    entityName
    type
    contacts {
      id
      firstName
      lastName
      email
      phone
    }
    shipmentLocations {
      id
      name
      city
      state
      zip
      gateCode
      locationInstructions
    }
    shipments {
      id
      activity
      itemDescription
      weight
      dimensions
      contact {
        lastName
        email
      }
      shipmentLocation {
        name
        city
      }
      serviceOffering {
        description
        typeCd
      }
    }
  }
}
```

This is impossible to do efficiently with traditional REST without multiple roundtrips or massive DTOs.

### 2. Paginated + Filtered Customers (with sorting)

```graphql
query PaginatedFilteredCustomers {
  customers(
    filter: { type: BUSINESS, entityNameContains: "acme" }
    page: 0
    size: 10
    sort: ["entityName"]
  ) {
    content {
      id
      entityName
      type
    }
    totalElements
    totalPages
    number
    size
  }
}
```

### 3. Shipments with Advanced Filtering + Pagination

```graphql
query FilteredShipments {
  shipments(
    filter: { 
      activity: PICKUP
      minWeight: 10
    }
    page: 0
    size: 5
    sort: ["-weight"]
  ) {
    content {
      id
      itemDescription
      weight
      activity
      customer { entityName }
    }
    totalElements
  }
}
```

### 4. Nested Pagination on a Customer's Shipments

```graphql
query CustomerWithPaginatedShipments {
  customer(id: 1) {
    entityName
    shipments(page: 0, size: 2, sort: ["-weight"]) {
      id
      weight
      itemDescription
    }
  }
}
```

### 5. Mutations — Create a New Shipment

```graphql
mutation CreateNewShipment {
  createShipment(input: {
    customerId: 1
    serviceOfferingId: 1
    itemDescription: "Emergency replacement parts"
    weight: 7.5
    dimensions: "10x8x6"
    activity: DELIVERY
  }) {
    id
    itemDescription
    weight
    activity
    serviceOffering { description }
  }
}
```

### 6. Introspection / Explore the Full Schema

In GraphiQL click the **Docs** tab on the right, or run:

```graphql
query IntrospectSchema {
  __schema {
    types {
      name
      description
    }
  }
}
```

Or simply use the "Schema" explorer in GraphiQL.

## Database Persistence

- Location: `./data/derbydb` (relative to where you run the app)
- DDL: `update` on first run, then your data stays forever.
- To reset: stop the app, delete the `data/` folder, restart.

## Running Tests

```bash
mvn test
```

Tests cover:
- Every `*ServiceImpl` class (service behavior + filtering/pagination)
- Full Spring context + real (in-memory for tests) Derby

## Architecture & Design Decisions

| Concern                    | Choice                                      | Why |
|---------------------------|---------------------------------------------|-----|
| Pagination                | Spring Data `Page` + `Pageable` args        | Natural, powerful, works great with GraphQL |
| Filtering                 | `Specification` + input records             | Type-safe, composable, demonstrates dynamic queries |
| Relations in GraphQL      | `@SchemaMapping` + initialized collections  | Shows deep nesting without N+1 pain for demo data |
| Security                  | HTTP Basic + InMemory users                 | Simple, works immediately in GraphiQL/curl |
| Persistence               | Embedded Derby (file)                       | Zero external DB, survives restarts |
| Schema                    | Pure annotation-driven (no .graphqls)       | Fastest with Spring for GraphQL; introspection still 100% |
| Testing                   | `@SpringBootTest` + service tests           | Tests the actual implementation classes as requested |

## Extending the Prototype

Ideas for next steps (the spec was already very solid):

- Add DataLoader / `@BatchMapping` for true N+1 prevention on large lists.
- Switch to Relay-style `Connection` + cursors for "infinite scroll" pagination.
- Add more sophisticated filters (date ranges, weight between, etc.).
- Role-based `@PreAuthorize` on mutations.
- JWT / OAuth2 instead of Basic.
- Proper exception handling mapped to GraphQL errors (`@GraphQlExceptionHandler`).
- GraphQL subscriptions (e.g. shipment status updates).
- OpenAPI / REST fallback alongside GraphQL (if needed).
- Docker + docker-compose with volume for the Derby data dir.

## Is the Original Spec Enough?

**Yes — it was more than enough for a high-quality, compelling prototype.**

The combination of a clear schema description + Spring for GraphQL patterns allowed rapid delivery of a production-style application with full hierarchy support, pagination, filtering, security, persistence, and tests.

See `DEMO_GUIDE.md` for how to present this effectively to leadership.

Your requirements already covered:
- All 5 tables + exact columns + referential integrity
- Full hierarchy + "anything in between"
- Pagination + sorting + filtering
- Schema exposure + GraphiQL
- Latest stable stack + tests + Maven + JPA + embedded persistent DB + Spring Security

The only minor things I chose (and documented):
- Added mutations (highly recommended for a "functional" demo)
- Used Specifications + Page instead of raw limit/offset (much more powerful and idiomatic)
- Basic auth instead of anonymous (Security requirement satisfied cleanly)

If you want any of the above extensions or changes (e.g. pure read-only, different auth, cursor pagination), just say the word and I'll iterate.

Enjoy exploring the power of GraphQL with this prototype!
