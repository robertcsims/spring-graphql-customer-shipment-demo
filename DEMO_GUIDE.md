# Executive Demo Guide – GraphQL + Spring for GraphQL

**Goal of this demo:**  
Show Senior Leadership how quickly and powerfully a rich relational domain can be exposed via GraphQL when starting from a clear schema specification. Emphasize **developer velocity**, **reduced network chatter**, and **client-controlled data fetching**.

## Recommended Start Command (Smoothest Experience)

**Easiest:**

```bash
./demo.sh
```

This is a one-line wrapper that starts the app in demo mode (no auth, clean logs).

Alternative (explicit):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

This activates `application-demo.properties` which:
- Turns off authentication (no login popups)
- Keeps logging clean
- Still uses persistent Derby database

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
Paste this query (it's pre-documented on the landing page):

```graphql
query FullRelationalHierarchy {
  customer(id: 1) {
    id
    entityName
    type
    contacts { firstName lastName email phone }
    shipmentLocations { name city state zip gateCode }
    shipments {
      activity
      itemDescription
      weight
      dimensions
      contact { lastName }
      shipmentLocation { name city }
      serviceOffering { description typeCd }
    }
  }
}
```

**Talking points:**
- "One single HTTP request returned data from **five different tables** with multiple relationships."
- "In a traditional REST architecture this would require at least 5 separate API calls plus client-side stitching."
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
```graphql
mutation CreateShipment {
  createShipment(input: {
    customerId: 2
    serviceOfferingId: 1
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

Run it, then immediately re-run the customer hierarchy query to show the new data appeared.

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
