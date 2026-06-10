# Executive Demo Guide – GraphQL + Spring for GraphQL

**Goal of this demo:**  
Show Senior Leadership how quickly and powerfully a rich relational domain can be exposed via GraphQL when starting from a clear schema specification. Emphasize **developer velocity**, **reduced network chatter**, and **client-controlled data fetching**.

## Recommended Start Command (Smoothest Experience)

**Easiest (and recommended for high-stakes demos):**

```bash
./demo.sh
```

This launches the application in demo mode with extremely clean logs and **no authentication friction**.

**Critical for professionalism:** Watch the console. The new static first screen at `http://localhost:8080` is served **as soon as the web server binds** (very early). It features a live readiness widget that polls `/actuator/health` and transitions from "initializing" to "ready".

You can open `http://localhost:8080` immediately — no more 404 Whitelabel pages. The page is self-contained, beautiful, and designed to impress at the highest levels (President / EU / boardroom).

Alternative (explicit):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

`application-demo.properties` ensures:
- Zero auth prompts
- Minimal framework noise in logs
- Persistent Derby database
- Professional readiness experience

Wait for the "Started CustomerShipmentGraphqlApplication..." line for full backend capabilities, but the landing experience is available far earlier.

Alternative (if you want to show security too):
```bash
mvn spring-boot:run
```
Then use credentials `admin` / `admin123` when GraphiQL prompts.

---

## Recommended Demo Flow (10–15 minutes)

### 1. Opening (1 min)
- Open the root URL in browser: **http://localhost:8080**
- Walk through the landing page:
  - "This entire working system was built from a textual schema description."
  - Highlight the three value cards: One Round-Trip, Client-Driven Precision, Rapid Implementation.
- Click **"Launch GraphiQL"**.

### 2. The "Wow" Query – Full Hierarchy (3–4 min)
Paste this query (it's pre-documented on the landing page). Note: it is deliberately **data-driven** — it locates the rich customer by a stable business attribute (`entityNameContains`) instead of a hard-coded surrogate database ID. IDs in real systems are auto-generated and can shift; filters on domain values do not.

```graphql
query FullRelationalHierarchy {
  customers(
    filter: { entityNameContains: "Acme" }
    page: 0
    size: 1
  ) {
    content {
      id
      entityName
      type
      contacts { id firstName lastName email phone }
      shipmentLocations { id name addressLine1 city state zip gateCode }
      shipments {
        id activity itemDescription weight dimensions
        contact { id firstName lastName email }
        shipmentLocation { id name city state zip gateCode }
        serviceOffering { id description typeCd }
      }
    }
    totalElements
    totalPages
  }
}
```

**Talking points:**
- "One single HTTP request returned data from **five different tables** with multiple relationships."
- "In a traditional REST architecture this would require at least 5 separate API calls plus client-side stitching."
- "The query uses a filter on the business name rather than a brittle hard-coded ID — this is the correct, maintainable pattern."
- Point out how the client shaped exactly the data it wanted — no more, no less.
- Open the **Network tab** in browser dev tools before running to visually prove only **one** request was made.

### 3. Pagination + Filtering Power (2 min)
```graphql
query FilteredPaginated {
  shipments(
    filter: { activity: PICKUP, minWeight: 10 }
    page: 0
    size: 3
    sort: ["-weight"]
  ) {
    content { itemDescription weight activity }
    totalElements
    totalPages
  }
}
```

**Talking points:**
- "Filtering, pagination, and sorting are built into the API layer."
- "The same flexible approach works at any level of nesting."

### 4. Schema Introspection (1–2 min)
Inside GraphiQL:
- Click the **Docs** tab on the right.
- Or run this introspection query:

```graphql
query ShowSchema {
  __schema {
    types {
      name
      description
    }
  }
}
```

**Talking points:**
- "The entire schema is self-documenting and discoverable."
- "Tools and clients can explore the API without separate documentation."

### 5. Mutations (optional but impressive) (1–2 min)
Mutations accept IDs that you normally obtain from prior queries (data-driven). Service offerings are small reference data (usually stable low IDs); for the customer, use an ID returned from a `customers` or `customer` query.

```graphql
# Step 1 (recommended): discover a customer ID via filter (data-driven, no hard-coded id)
# query { customers(filter: { entityNameContains: "Acme" }, page:0, size:1) { content { id entityName } } }

mutation CreateShipment {
  createShipment(input: {
    # Replace customerId with a real id returned by a prior customer query
    customerId: 1
    # service offerings are reference data (created first): 1=STD, 2=EXP, 3=LTL, 4=AIR
    serviceOfferingId: 2
    itemDescription: "Demo executive gift"
    weight: 1.5
    dimensions: "8x6x4"
    activity: DELIVERY
  }) {
    id
    itemDescription
    weight
    customer { entityName }
  }
}
```

Run it, then immediately re-run a customer hierarchy (filter form) query to show the new shipment appeared in the graph.

### 6. Close – The Velocity Story (1 min)
- "From a written schema spec (5 tables + relationships + pagination/filtering requirements) to a fully tested, secured, production-pattern application with GraphiQL in a very short time."
- "All the hard parts — JPA relationships, service layer, GraphQL resolvers, error handling, persistence — were implemented cleanly."
- "This is what modern Spring for GraphQL + strong tooling enables."

---

## Backup / Fallback Queries

If anything goes wrong, these are very safe:

- `query { serviceOfferings { id description typeCd } }`
- `query { customers(page:0, size:5) { content { id entityName type } totalElements } }`

---

## Pro Tips for the Presentation

- **Have the landing page open first** — it sets the narrative before anyone sees GraphiQL.
- Use the browser's **split screen** (GraphiQL on one side, a simple REST diagram on the other if you prepared one).
- Run the big hierarchy query **twice** — once while talking, once silently so people can watch the response shape.
- After the demo, you can show the code structure quickly:
  - One resolver class handles almost everything (`CustomerGraphQlResolver`)
  - Clean separation (entities → repositories → services → GraphQL layer)
- Mention that the data is real Derby (file-based) so if you restart the app during Q&A, the data is still there.

---

## Resetting Data (if needed)

```bash
# Stop the app
rm -rf data/
# Restart — seeder will recreate the two demo customers
```

---

## How This Was Built (for internal reference)

The implementation was driven directly from the schema description provided by the requestor. Key Spring for GraphQL patterns used:
- `@QueryMapping` + `@MutationMapping` for root operations
- `@SchemaMapping` for nested relationships (the heart of the "one request" story)
- Spring Data `Pageable` + `Specification` for pagination/filtering
- Proper exception handling and clean profiles for demo vs production

This combination allowed a rich, fully relational GraphQL API to be delivered extremely quickly while maintaining good architecture and test coverage.

Good luck with the presentation!
