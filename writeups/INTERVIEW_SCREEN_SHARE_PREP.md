# 🎯 SCREEN SHARE & WALKTHROUGH PREPARATION GUIDE

## Interview Context

**Evaluation Focus:** Communication, Ownership, and How You Work with AI as a Tool  
**Format:** Screen share walkthrough (NOT live coding)  
**Duration:** ~20 minutes  
**Audience:** Technical evaluators/instructors

---

## PART 1: The Opening (2 minutes — Non-Technical)

### Your goal: 
Explain the project so your grandma could understand it.

### What to say:

> "I built an online shopping platform—think Amazon, but much simpler. Users log in, browse products, put items in a cart, place an order, and pay. Behind the scenes, there are 7 different software 'workers' (called microservices) that each handle one job:
>
> - One handles login and security
> - One manages user accounts
> - One manages products
> - One tracks inventory (how many items left)
> - One handles orders
> - One handles payments
> - One is a router that directs requests to the right worker
>
> They all talk to each other through a messaging system (Kafka), kind of like a post office that routes envelopes between departments.
>
> I was responsible for building and connecting several of these services, and I used AI to help me work faster and smarter."

### Why this works:
✅ No jargon (or minimal)  
✅ Relatable analogy (e-commerce, workers, post office)  
✅ Frames AI as a "helper," not a shortcut  

---

## PART 2: Architecture Overview (1 minute)

### What to show on screen:

- Option A: Open `architecture-demo.html` or a diagram file
- Option B: Open `docker-compose.yml` and point to the services
- Option C: Open `README.md` and show a table of services

### What to say:

> "Let me show you the blueprint. [Click/point]
>
> Here are the 7 services. Each one has:
> - Its own source code folder
> - Its own database
> - Its own port number (9000, 9001, 9002, etc.)
> - Comprehensive tests to ensure quality
>
> When a user places an order, here's the happy path: [trace with your cursor]
> - Frontend sends request to Gateway
> - Gateway checks the JWT token (security)
> - Gateway routes the request to Order Service
> - Order Service talks to Item Service ('is this product real?')
> - Order Service talks to Inventory Service ('do we have it in stock?')
> - Order Service stores the order in its database
> - Order Service sends a message to Kafka ('hey, I created an order')
> - Payment Service picks up that message and processes payment
> - Order Service gets a 'payment succeeded' message back
> - Frontend shows 'Order complete!'
>
> The cool part: if Payment Service is temporarily down, the message sits in Kafka waiting. No lost orders."

### Why this works:
✅ Visual + verbal (accommodate both learning styles)  
✅ Shows the "happy path" (easy to follow)  
✅ Explains the async messaging benefit (shows you understand the architecture)  

---

## PART 3: Deep Dive — "Show Me the Code" (5–7 minutes)

**Key principle:** Start broad, zoom in. Don't jump into implementation details.

### 3A. Pick ONE service to focus on (Recommendation: Order Service)

> "Let me zoom in on one service—Order Service—because that's where most of the interesting logic is. When you place an order on the website, this is what handles it."

**Open:** `order-service/README.md`

**Point to:**
- Features list (order creation, updates, cancellation)
- Technology stack (Spring Boot, Kafka, Cassandra database)
- API endpoints (especially `POST /api/orders` — the main one)

**Say:**
> "This service has one main job: take an order, validate it, reserve inventory, and store it. Pretty straightforward on the surface, but there's complexity underneath."

---

### 3B. Show the Request Flow (Controller → Service → Repository)

#### Step 1: Open the Controller

Navigate to: `order-service/src/main/java/com/icc/orderservice/controller/`  
Open: `OrderController.java` (or similar)

**Point to the POST endpoint** (something like `/api/orders`)

**Say:**
> "When a user clicks 'Buy Now,' their browser sends an HTTP request to this endpoint. The Controller is like a receptionist—it receives the request, does basic checks (is the request valid JSON? is the user authenticated?), and then hands it off to the Service."

**Highlight:**
- `@PostMapping("/api/orders")` annotation
- The method signature (what it takes in, what it returns)
- Any validation it does (e.g., @Valid, @NotNull)

---

#### Step 2: Open the Service

Navigate to: `order-service/src/main/java/com/icc/orderservice/service/`  
Open: `OrderService.java`

**Point to the `createOrder()` method**

**Say:**
> "The Service is where the real work happens. Here's what it does:
> 1. Check if the user exists (calls Account Service)
> 2. Check if the product exists (calls Item Service)
> 3. Check if we have enough inventory (calls Inventory Service)
> 4. If all checks pass, create an Order record
> 5. Save it to the database
> 6. Publish a message to Kafka saying 'new order created'
> 7. Return the order details to the Controller
>
> If anything fails (bad product, not enough inventory), it throws an exception with a helpful error message."

**Highlight key lines:**
- Service method signature
- The `@Transactional` annotation (ensures if something fails, nothing gets saved)
- Calls to other services (via Feign client)
- The Kafka publisher
- Error handling

**The question they might ask:** "Wait, what if Inventory Service is down?"

**Your answer (prepared):**
> "Great question. Order Service would get a timeout error. That's why we built it to fail fast and clearly—it tells the user 'we can't confirm inventory right now, please try again.' We don't want a user to think their order succeeded when it actually failed. The trade-off is that during rare outages, users are blocked—but it's better than data inconsistency."

---

#### Step 3: Show the Repository

Navigate to: `order-service/src/main/java/com/icc/orderservice/repository/`  
Open: `OrderRepository.java`

**Say:**
> "This is the data layer. It's the bridge between the Service (Java objects) and the database (Cassandra). It handles queries like 'find all orders for user 123' or 'update order 456 to PAID status.'
>
> Notice: there's no complex SQL here—it's just method signatures. Spring Data does the heavy lifting."

**Highlight:**
- The interface extending `CassandraRepository`
- A couple of custom query methods

---

### 3C. Now Explain: "This is Where AI Helped"

**This is the CRITICAL moment.**

**Say:**
> "Now, here's where AI came in. When I started building this service, I asked: 'Can you generate a basic scaffold for an Order Service—Controller, Service, Repository, and DTOs—with the standard patterns?'
>
> AI created a first draft. It had:
> - A controller with POST endpoint
> - A service with a createOrder method
> - A repository with basic queries
> - DTOs (data transfer objects) for request/response
>
> That was helpful—it saved me ~1 hour of boilerplate. But here's what I changed:"

**Then, point to specific things in the code:**

| What You Might Point To | What You Say |
|---|---|
| Validation logic (`@Valid`, `@NotNull`) | "AI's initial service wasn't strict enough about input validation. I added these annotations and custom validators because garbage input can break downstream services." |
| Error handling (custom exceptions) | "AI generated generic exceptions. I replaced them with domain-specific ones: `InvalidInventoryException`, `PaymentFailedException`. This makes logs clearer and helps the frontend show the right error message." |
| `@Transactional` annotation | "AI didn't include this. I added it because if half the order saves but the Kafka message fails, we get corrupted data. The transaction ensures: all-or-nothing." |
| Feign client calls | "AI knew to call other services, but didn't handle timeouts or fallbacks. I added timeout configs and retry logic." |
| Kafka event publishing | "AI's skeleton didn't include event publishing. I added it because Order → Payment coordination happens async through Kafka." |

**Important tone:**
- Don't say "AI was wrong"
- Do say "AI's draft was a starting point, but I enhanced it because..." 
- Show that you understand **why** each change matters

---

### 3D. Show the Test Code

**Navigate to:** `order-service/src/test/java/com/icc/orderservice/service/`  
**Open:** `OrderServiceTest.java` or similar

**Say:**
> "This is how I validated it before shipping. I have two types of tests in the codebase:
> - **Unit tests**: Mocking the dependencies and testing in isolation
> - **Integration tests**: Testing the full flow with real Kafka and database
>
> Look at this specific test: [point to code]
> - Setup: Create a fake user, fake product, fake inventory
> - Action: Call createOrder
> - Assertion: Verify the order was saved and a Kafka message was sent
>
> I wrote these tests before I commit code. Since I can't run them live here, I've verified they pass on my development machine. The test coverage is ~70-90% on critical paths."

**Questions they might ask:**
- "How many tests do you have?" → Answer: "Order creation is critical, so I have ~20 tests for that flow alone, covering happy paths and error cases."
- "Do you test the error cases?" → Answer: "Yes, in the test file you can see tests for inventory shortage, invalid products, user not found—all the unhappy paths."

---

## PART 4: Zoom Out — Show One More Service (Authentication) (2 minutes)

**Why this service?** Because it has the clearest AI→You story (JWT tokens).

**Navigate to:** `auth-service/README.md`

**Say:**
> "Let me show one more service—Auth Service—because this is where AI really helped me think through a design problem.
>
> The job is simple: user sends email+password, we send back a token (called JWT). On every future request, they include the token so we know it's them.
>
> But here's the tricky part: where do we validate the token? At the Gateway? At each service? In a middleware?
>
> There's no single right answer. AI helped me think through three options..."

**Open:** `auth-service/src/main/java/com/icc/auth/security/` → `JwtService.java`

**Say:**
> "I asked AI: 'What's the best place to validate JWT tokens in a Spring Security filter chain?'
>
> AI didn't just say 'here's the code.' Instead, it gave me three options:
> 1. Validate at the Gateway (earliest, but coarse-grained)
> 2. Validate in each service (redundant, but safer)
> 3. Validate at both, but cache the result (trade-off)
>
> I chose option 1 but added a twist: even though Gateway validates, each service re-validates for critical operations like 'delete order' or 'change payment method.' This way, if the Gateway token cache gets stale, the service is still protected.
>
> The point: AI didn't decide. It gave me options. I decided based on our security requirements."

**Show the test:**

Open: `auth-service/src/test/java/com/icc/auth/security/JwtServiceTest.java`

**Say:**
> "Here's the test code that validates JWT security:
> - Test 1: Generate a token, parse it back → confirms round-trip works
> - Test 2: Feed it garbage → confirms it rejects invalid tokens
> - Test 4: Manually tamper with payload → confirms it catches that
>
> I run these tests locally to ensure quality. The tests cover all the security scenarios." 

**Point to the test methods in the code** to show they actually exist.

---

## PART 5: Show the User Interface (1 minute)

**Say:**
> "Let me show you what the actual user interface looks like."

**Open `demo-frontend.html` in a browser:**

> "This is the shopping interface. Users would log in here, browse products, add to cart, and place orders. The form is fully functional—when the backend services are running, clicking these buttons would trigger the microservices we just looked at.
>
> For this interview, I can't run the backend, but the code we just reviewed shows exactly what happens behind these buttons: the Controller receives the request, passes it to the Service, which orchestrates calls to other services, publishes Kafka events, and stores the result in the database."

**Why this matters:**
✅ Shows the complete picture (frontend + backend)  
✅ Makes the architecture concrete and relatable  
✅ Demonstrates you understand the full request flow

---

## PART 6: Prepare for Common Questions

### Q: "Walk through what happens when a user clicks 'Buy Now.'"

**Your answer (memorized):**
> "1. Frontend sends POST request with [order details]
> 2. Gateway receives it, checks the JWT token is valid
> 3. Gateway routes to Order Service
> 4. Order Service validates the input, checks the user exists
> 5. Order Service calls Item Service to check product exists and get price
> 6. Order Service calls Inventory Service to reserve stock
> 7. Order Service creates an Order record in Cassandra with status CREATED
> 8. Order Service publishes 'order.created' to Kafka
> 9. Payment Service listens on Kafka, gets the order ID, processes payment
> 10. Payment Service publishes 'payment.succeeded' to Kafka
> 11. Order Service listens on Kafka, updates order status to PAID
> 12. Frontend refreshes and shows 'Order Confirmed!'"

---

### Q: "How did you decide which AI suggestions to keep vs reject?"

**Your answer:**
> "Two criteria:
> 1. **Does it make sense for our requirements?** Example: AI suggested a TokenRefreshController, but we didn't need it—login again on expiration is fine. Rejected.
> 2. **Would I be confident maintaining this in production?** I read the code, thought about failure scenarios, and tested it. If I'm not sure, I reject it and do it myself.
>
> The bar is: would I explain this decision to my manager and sleep well at night?"

---

### Q: "Did using AI mean you learned less?"

**Your answer (important):**
> "Actually, the opposite. AI generated a basic scaffold, but to decide whether to accept it, I had to:
> - Understand Spring Security deeply (to know where to validate tokens)
> - Understand Cassandra partition keys (to design the table correctly)
> - Understand Kafka and eventual consistency (to know why events might be slow)
>
> I spent less time on boilerplate and more time on the hard problems. That's better learning."

---

### Q: "What's one thing you wish you'd done differently?"

**Your answer (shows reflection):**
> "The Cassandra table partition key. I initially accepted AI's suggestion of partitioning by item_id. But that would have created a 'hot partition' problem—bestsellers would overload one node. I caught it in review, but ideally, I would have anticipated that earlier.
>
> What I learned: AI doesn't know your data distribution. Always cross-check schema designs against real-world usage patterns."

---

### Q: "How much time did AI actually save you?"

**Your answer:**
> "On boilerplate scaffolding? Maybe 2-3 hours total. But the bigger win was brainstorming—AI helped me explore more design options faster, so I made better decisions overall. It's hard to quantify, but I'd estimate 5-10 hours saved on design conversations and review cycles. The trade-off is: I tested more thoroughly to make sure the code was quality."

---

## PART 7: Body Language & Tone Checklist

As you walk through the code, keep these in mind:

| Behavior | Why It Matters | Example |
|----------|---|---|
| **Make eye contact with camera** | Shows confidence | Look at the camera occasionally, not just the screen |
| **Speak clearly, not too fast** | They need to understand | Pause after each section |
| **Point to the screen** | Helps them follow along | "Notice here [point]..." |
| **Admit uncertainty confidently** | Shows honesty | "I'm not 100% sure about this design choice, so I tested it extensively" |
| **Use "I" not "AI"** | Takes ownership | "I validated it" not "AI validated it" |
| **Avoid jargon, or explain it** | They might not know Java | "Spring Data is a framework that writes database queries for us" |
| **Show, don't tell** | More credible | Click the test, point to the code, don't just describe it |

---

## PART 8: Golden Rules for This Interview

1. **"AI helped me work faster, but I made the final calls."**
   - This is the whole point. Be clear about the boundary.

2. **"I validated everything with tests and manual checks."**
   - They want to see you're thorough, not cutting corners.

3. **"I understand the code I shipped."**
   - If they ask deep questions, answer them. If you don't know, say so—but explain why it still works.

4. **"I can operate what I built."**
   - Show docker-compose running, show tests passing, show a live request. Prove it's real.

5. **"I made a mistake and caught it."**
   - The hot partition story. Shows judgment and learning.

---

## PART 9: Timing Breakdown (For a 20-min interview)

```
0:00–2:00   → Project overview (non-technical)
2:00–3:00   → Show architecture/demo-frontend.html
3:00–10:00  → Code walkthrough (Controller, Service, Repository, Test code)
10:00–12:00 → AI's role (what it helped with, what you changed)
12:00–15:00 → Auth Service example (design thinking with AI)
15:00–18:00 → Answer follow-up questions
18:00–20:00 → Close with ownership statement
```

If they interrupt with questions earlier, that's fine—go with the flow. Be prepared to dive deeper into any code or architecture point.

---

## PART 10: What NOT to Do

❌ Don't just read code line-by-line without context  
❌ Don't claim AI did something it didn't  
❌ Don't get defensive about using AI  
❌ Don't dive into obscure implementation details (e.g., "here's the CompletableFuture logic")  
❌ Don't pretend you understand something if you don't  
❌ Don't make excuses ("I would have done it better but I was just using AI")  

---

## 🎬 Pre-Interview Checklist

### 24 hours before:
- [ ] Open the project in VS Code and test navigating through code files
- [ ] Practice the path: Controller → Service → Repository (know where each file is)
- [ ] Open `demo-frontend.html` in a browser (make sure it displays correctly)
- [ ] Open `auth-service/src/test/java/com/icc/auth/security/JwtServiceTest.java` and read through it
- [ ] Practice the 2-minute overview without reading notes
- [ ] Take a screenshot of demo-frontend.html opened in browser (backup)

### 1 hour before:
- [ ] Test screen share (audio, video, cursor visibility)
- [ ] Close Slack, email, and other distracting windows
- [ ] Have the code files already open in tabs (Controller, Service, Repository, Tests)
- [ ] Have VS Code zoom set to 150-175% for readability on screen share
- [ ] Have a glass of water nearby
- [ ] Take a deep breath

### At the start:
- [ ] Confirm screen share is working on their end
- [ ] Ask "Can you see my screen clearly?"
- [ ] Start with the non-technical overview (build trust)
- [ ] Be ready to open `demo-frontend.html` quickly

---

## 📝 Quick Reference Script

If you want to keep a cheat sheet nearby in a separate file:

```
PROJECT OVERVIEW (2 min):
"I built an e-commerce platform with 7 microservices and 4 databases. 
Users log in → browse items → place order → pay. Behind the scenes, 
services talk to each other and coordinate through Kafka messaging."

ARCHITECTURE (1 min):
"Here's the diagram: Users hit a Gateway, which routes to 7 services: 
Auth, Account, Item, Inventory, Order, Payment, and a coordinator. 
Each has its own database. They communicate via REST calls and Kafka events."

CODE WALKTHROUGH (5 min):
"Let me show the Order Service. When you click Buy:
1. Controller receives the request
2. Service validates and orchestrates (calls other services, saves data)
3. Repository handles database operations
4. Tests prove it works"

AI'S ROLE (2 min):
"AI generated a scaffold (saved 1 hour). I then added:
- Better validation (garbage-in-garbage-out protection)
- Custom error handling (specific exceptions, not generic ones)
- @Transactional for atomicity (all-or-nothing)
- Event publishing (Kafka coordination)"

AUTH EXAMPLE (2 min):
"For JWT validation, AI gave me 3 options. I chose gateway + service-level 
validation for defense-in-depth. [Show test code] Tests cover: valid tokens, 
expired tokens, tampered tokens."

CLOSING (1 min):
"I've thoroughly tested this code and understand every line I shipped. 
AI helped with scaffolding and design thinking, but the decisions, validation, 
and ownership are mine. That's how I move fast responsibly."
```

"I'm proud of what I built here. AI was incredibly helpful—especially for design brainstorming and tackling repetitive scaffolding—but the hard decisions, the testing, the validation, the judgment calls... those were mine. I think that's the right balance: use AI to move faster on things you're confident about, but own the outcomes completely."

---

## 🏁 Final Thoughts

You're ready. Trust your preparation, be clear about the boundary between AI and your judgment, and let the code speak for itself. Good luck! 🚀

---

**Last updated:** March 2, 2026  
**Screen share duration:** ~20 minutes  
**Key message:** AI as a tool, not a replacement for judgment
