AI Coach must:

Load advice when user is logged in (not stuck on error)

Show a meaningful empty state only if there’s truly no data yet

Not break if OpenAI/AI provider fails (shows fallback + retry)

Log errors server-side with request id

AI Meal Plan must:

Save preferences successfully

Generate a weekly plan successfully and persist it (DB)

“Get current plan” returns:

200 + plan when exists

404 when none exists (treated as empty UI, not an error)

No infinite loading (timeouts + UI status)

1) Most likely root causes (based on your screenshot)
AI Coach

“Unable to load advice right now.” usually means:

Frontend call is failing (401/403/500/network)

Backend route missing / wrong path

AI provider key missing on server (500)

Timeout (AI call takes too long, frontend treats as failure)

Response shape mismatch (frontend expects {summary, recommendations} but backend returns something else)

AI Meal Plan

“Meal plan will appear here.” often means:

Generate request never succeeds / returns error

Generate succeeds but not saved, so “current” stays empty

Frontend never refreshes after generation (or refresh hits wrong endpoint)

Auth issue (401) so nothing is returned

Preferences endpoint not saving correctly, so generator fails validation

2) Step-by-step debug plan (do this in order)
Step 1 — Reproduce with DevTools (Frontend)

For each feature, do this once:

Open Chrome DevTools → Network

Click:

“Refresh advice”

“Generate Weekly Plan”

“Set Meal Preferences” (save)

For each request, record:

URL (path)

Status code (200/401/403/404/500)

Response body

Request headers (especially Authorization: Bearer …)

Time (does it hang?)

Decision immediately:

If 401/403 → auth/token issue

If 404 → route mismatch / wrong base URL / proxy

If 500 → backend exception (log it)

If request hangs → backend stuck / AI call timing out

Step 2 — Confirm frontend API base URL + auth injection

Check your api.js (or axios/fetch wrapper):

Is the base URL correct for dev and prod?

Is the token attached on every protected call?

Is your frontend hitting something like:

http://localhost:xxxx/api/...

but backend is on different port and CORS blocks it?

Good state:

Every protected request includes Authorization: Bearer <jwt>

Base URL is consistent and correct in .env for dev/prod

Step 3 — Verify backend routes actually exist and match frontend

List the real backend endpoints for both features and confirm they match frontend calls exactly.

You want something like:

AI Coach

GET /api/ai/coach/advice (or similar)

Meal Plan

POST /api/ai/meals/preferences (save)

POST /api/ai/meals/generate (generate weekly plan)

GET /api/ai/meals/current (fetch latest plan)

Common bug: frontend calls /api/ai/meal/current but backend is /api/ai/meals/current (plural mismatch).

Step 4 — Backend logging: capture the real error

Add/confirm logging in the backend controller/service:

Log:

userId

endpoint

request payload (without sensitive)

AI provider response status (if any)

exception stacktrace

a request id

Goal: when frontend shows error, backend logs tell you exactly why.

Step 5 — Validate authentication + permissions

If endpoints require auth:

Confirm JWT middleware runs correctly

Confirm userId is extracted and not null

Confirm endpoints don’t accidentally block because role mismatch

Test quickly:

Call endpoint from Postman with a valid token

If Postman succeeds but browser fails → CORS / frontend auth header missing

Step 6 — Confirm DB persistence (Meal Plan)

Meal plan must be stored and retrievable.

Check:

MealPlan schema/table exists

After POST /generate, verify it writes a plan row/document

GET /current reads latest plan for that userId

If generate returns 200 but current is still 404:

You’re not saving

Or saving under wrong userId

Or reading query is wrong (sorting/filters)

Step 7 — Confirm response formats match what UI expects

This is a silent killer.

Example:

UI expects:

{ "summary": "...", "recommendations": ["..."] }


But backend returns:

{ "advice": { "summary": "...", "recs": [...] } }


Fix by either:

Standardizing backend response shape

OR mapping response shape in frontend before rendering

Step 8 — Timeouts + retries for AI calls (both features)

AI calls can be slow or fail.

Backend must enforce:

Timeout (ex: 20–30s)

Retry once for transient errors

Return structured error:

503 AI_UNAVAILABLE

504 AI_TIMEOUT

400 BAD_INPUT (missing preferences)

401 UNAUTHORIZED

Frontend must handle:

show “Generating…” state

show “Try again” button

never hang forever

3) Fix plan for each feature
A) AI Coach — Make it reliably run
A1. Decide what data AI Coach uses

AI Coach usually needs:

User metrics (weight, BMI, activity, goals, logs, etc.)

Recent progress history

If you don’t have data, AI coach should return:

200 with a helpful “starter advice” OR

404 EMPTY (then UI shows “Add your first workout/meal to get advice”)

A2. Backend “advice pipeline”

Implement a stable pipeline:

Fetch user profile + metrics

Fetch recent logs (if any)

Build prompt/context

Call AI provider

Parse output into strict schema:

summary: string

recommendations: string[]

Save advice in DB (optional cache)

Return it

Important: if AI fails, return last cached advice if available.

A3. Frontend states (explicit)

Use states:

loading (spinner)

empty (no data yet)

error (retry button)

success (render advice)

Your screenshot suggests you already do fallback logic somewhere—make it consistent and driven by HTTP type.

B) AI Meal Plan — Make it end-to-end functional
B1. Ensure preferences are required & validated

Preferences should include:

dietary type / restrictions

allergies

calories/macros targets

meals per day

excluded foods

budget/time constraints (optional)

Backend should:

Validate required fields

If missing → return 400 with a message UI can show

B2. Generation must persist, then refresh current

Flow:

User clicks “Generate Weekly Plan”

Frontend calls POST /generate

Backend:

loads preferences

generates plan

saves plan to DB

returns {planId, plan}

Frontend sets status=success and renders immediately

Also optionally calls GET /current to ensure it’s consistent

If your UI stays empty, it’s because step 3 (save) or step 4 (render) isn’t happening.

B3. Use your “404 = EMPTY” pattern consistently

You already did this for /current earlier.

Make sure:

GET /current returns 404 if none

frontend treats it as empty (NOT error)

Only real errors go to error

4) Production hardening checklist (so users don’t face random failures)

✅ Add server-side request timeout for AI calls

✅ Add request size limits + input validation

✅ Add structured error codes and consistent JSON responses

✅ Add rate limiting per user (avoid abuse + cost explosion)

✅ Add caching for AI Coach advice (e.g., 1 hour) so refresh is fast

✅ Add observability:

request id

error logs

metrics (success rate, latency)

5) Cursor prompt (paste this as-is)

Copy this into Cursor. It forces it to do the job in the right order and not “guess”.

You are working on my AI Fitness App. Two features are broken in UI:
1) AI Coach shows "Unable to load advice right now."
2) AI Meal Plan: Generate Weekly Plan / Set Meal Preferences does not actually produce a working plan for users.

GOAL:
Make both features work end-to-end for real users, with correct backend routes, auth, DB persistence, and frontend state handling. No infinite loading. Clear empty/error states.

DO THIS STEP-BY-STEP:

PHASE 1: Identify current API calls and failure points
1. Search the frontend for "AI Coach", "Refresh advice", "Meal Plan", "Generate Weekly Plan", "Set Meal Preferences".
2. List the exact API endpoints being called (paths, methods) and where the token is attached (api.js/axios wrapper).
3. Inspect current response handling logic and UI states (loading/empty/error/success). Identify where it falls into error.

PHASE 2: Verify backend endpoints exist and match frontend
4. Search backend controllers/routes for coach advice and meal plan endpoints.
5. Ensure these endpoints exist (or create them if missing) and match frontend paths exactly:
   - GET /api/ai/coach/advice
   - POST /api/ai/meals/preferences
   - POST /api/ai/meals/generate
   - GET  /api/ai/meals/current
6. Confirm auth middleware protects these routes and userId is extracted correctly.

PHASE 3: Make Meal Plan fully functional (preferences -> generate -> persist -> fetch)
7. Implement/verify MealPreferences storage by userId (DB schema/model).
8. Implement/verify MealPlan storage by userId with timestamp and plan content.
9. In POST /generate:
   - load preferences for user
   - validate required fields (return 400 with message if missing)
   - generate plan (mock deterministic plan if AI integration not ready)
   - persist plan to DB
   - return { type: "SUCCESS", data: plan }
10. In GET /current:
   - return 404 with { type: "EMPTY" } if no plan
   - else return { type: "SUCCESS", data: plan }

PHASE 4: Make AI Coach functional and resilient
11. In GET /coach/advice:
   - gather user profile + recent metrics/logs (whatever exists)
   - if no data: return 404 { type:"EMPTY" } (or a starter advice)
   - else generate advice (or provide a temporary deterministic advice if AI not set up)
   - return { type:"SUCCESS", data:{ summary, recommendations[] } }
12. Add server-side timeout and safe error handling:
   - on timeout: 504 { type:"ERROR", message:"AI timeout" }
   - on AI unavailable: 503 { type:"ERROR", message:"AI unavailable" }

PHASE 5: Frontend fixes (no hanging, consistent states)
13. Update frontend API service functions to return structured results:
   - SUCCESS / EMPTY / ERROR (treat 404 as EMPTY for /current and /coach/advice if you choose)
14. Update UI components to use explicit status:
   loading | empty | success | error
15. Ensure "Generate Weekly Plan" triggers a refresh and sets UI to success immediately after generation.
16. Add retry buttons for error and avoid infinite spinners (add request timeout on frontend too).

PHASE 6: Add logging and test steps
17. Add backend logs for each endpoint (userId, status, error).
18. Provide manual test checklist:
   - new user (no preferences/plan/advice)
   - user with preferences
   - generate plan -> refresh -> current plan returns 200
   - coach advice returns success or empty state
19. Output a final summary of files changed and why.

IMPORTANT:
- Do not change unrelated UI styling.
- Keep 404 as EMPTY for meal plan current and coach advice if applicable.
- If AI provider key is missing, do NOT crash—return structured error and show retry UI.