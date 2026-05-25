#!/bin/bash

# ============================================================
# Personal Finance Manager - API Test Script
# Usage: bash financial_manager_tests.sh <BASE_URL>
# Example: bash financial_manager_tests.sh https://syfe-2.onrender.com/api
# ============================================================

BASE_URL="${1:-https://syfe-2.onrender.com/api}"
COOKIE_FILE=$(mktemp)
COOKIE_FILE2=$(mktemp)

TOTAL=0
PASSED=0
FAILED=0
FAILED_TESTS=()

# ── Colours ──────────────────────────────────────────────────
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ── Helpers ──────────────────────────────────────────────────
pass() { echo -e "  ${GREEN}✔ PASS${NC} $1"; ((PASSED++)); ((TOTAL++)); }
fail() { echo -e "  ${RED}✘ FAIL${NC} $1"; FAILED_TESTS+=("$1"); ((FAILED++)); ((TOTAL++)); }

check() {
  local name="$1"
  local expected_status="$2"
  local actual_status="$3"
  local body="$4"
  local extra_check="$5"

  if [[ "$actual_status" == "$expected_status" ]]; then
    if [[ -n "$extra_check" ]]; then
      if echo "$body" | grep -q "$extra_check"; then
        pass "$name"
      else
        fail "$name (status $actual_status OK but missing: $extra_check)"
      fi
    else
      pass "$name"
    fi
  else
    fail "$name (expected $expected_status, got $actual_status)"
  fi
}

section() { echo -e "\n${CYAN}${BOLD}━━━ $1 ━━━${NC}"; }

# ── Wake up server ────────────────────────────────────────────
echo -e "${YELLOW}⏳ Waking up server at $BASE_URL ...${NC}"
for i in {1..5}; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time 30 "$BASE_URL/auth/register" 2>/dev/null)
  if [[ "$STATUS" != "000" ]]; then break; fi
  echo "   Attempt $i/5 - waiting 10s..."
  sleep 10
done
echo -e "${GREEN}✔ Server responded${NC}\n"

# ════════════════════════════════════════════════════════════
section "1. USER REGISTRATION"
# ════════════════════════════════════════════════════════════

# 1.1 Register user 1
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"user1@test.com","password":"password123","fullName":"User One","phoneNumber":"+1111111111"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Register new user" "201" "$STATUS" "$BODY" "userId"

# 1.2 Register user 2 (for isolation tests)
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"user2@test.com","password":"password123","fullName":"User Two","phoneNumber":"+2222222222"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Register second user" "201" "$STATUS" "$BODY" "userId"

# 1.3 Duplicate registration → 409
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"user1@test.com","password":"password123","fullName":"User One","phoneNumber":"+1111111111"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Duplicate registration returns 409" "409" "$STATUS"

# 1.4 Invalid email → 400
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"not-an-email","password":"password123","fullName":"Bad User","phoneNumber":"+1111111111"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Invalid email returns 400" "400" "$STATUS"

# 1.5 Missing password → 400
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"nopass@test.com","fullName":"No Pass","phoneNumber":"+1111111111"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Missing password returns 400" "400" "$STATUS"

# 1.6 Short password → 400
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"short@test.com","password":"abc","fullName":"Short Pass","phoneNumber":"+1111111111"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Short password returns 400" "400" "$STATUS"

# ════════════════════════════════════════════════════════════
section "2. LOGIN & SESSION"
# ════════════════════════════════════════════════════════════

# 2.1 Login user 1
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -c "$COOKIE_FILE" \
  -d '{"username":"user1@test.com","password":"password123"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Login with valid credentials" "200" "$STATUS" "$BODY" "successful"

# 2.2 Wrong password → 401
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"user1@test.com","password":"wrongpassword"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Wrong password returns 401" "401" "$STATUS"

# 2.3 Non-existent user → 401
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"nobody@test.com","password":"password123"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Non-existent user returns 401" "401" "$STATUS"

# 2.4 Access without session → 401
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/transactions")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Unauthenticated request returns 401" "401" "$STATUS"

# Login user 2 into separate cookie jar
curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -c "$COOKIE_FILE2" \
  -d '{"username":"user2@test.com","password":"password123"}' > /dev/null

# ════════════════════════════════════════════════════════════
section "3. CATEGORIES"
# ════════════════════════════════════════════════════════════

# 3.1 Get all categories
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/categories" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Get all categories returns 200" "200" "$STATUS" "$BODY" "Salary"

# 3.2 Default categories present
check "Default category Salary present" "200" "$STATUS" "$BODY" "Salary"
check "Default category Food present" "200" "$STATUS" "$BODY" "Food"
check "Default category Rent present" "200" "$STATUS" "$BODY" "Rent"

# 3.3 Create custom category
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/categories" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"name":"Freelance","type":"INCOME"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Create custom category returns 201" "201" "$STATUS" "$BODY" "Freelance"
check "Custom category isCustom=true" "201" "$STATUS" "$BODY" "true"

# 3.4 Duplicate custom category → 409
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/categories" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"name":"Freelance","type":"INCOME"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Duplicate custom category returns 409" "409" "$STATUS"

# 3.5 Cannot delete default category → 403
RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/categories/Salary" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Delete default category returns 403" "403" "$STATUS"

# 3.6 Delete non-existent custom category → 404
RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/categories/NoSuchCategory" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Delete non-existent category returns 404" "404" "$STATUS"

# 3.7 Missing type → 400
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/categories" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"name":"NoType"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Missing category type returns 400" "400" "$STATUS"

# ════════════════════════════════════════════════════════════
section "4. TRANSACTIONS"
# ════════════════════════════════════════════════════════════

# 4.1 Create income transaction
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"amount":50000.00,"date":"2024-01-15","category":"Salary","description":"January Salary"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Create income transaction returns 201" "201" "$STATUS" "$BODY" "INCOME"
TX1_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

# 4.2 Create expense transaction
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"amount":1200.00,"date":"2024-01-20","category":"Rent","description":"January Rent"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Create expense transaction returns 201" "201" "$STATUS" "$BODY" "EXPENSE"
TX2_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

# 4.3 Create transaction with custom category
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"amount":5000.00,"date":"2024-02-01","category":"Freelance","description":"Freelance work"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Create transaction with custom category returns 201" "201" "$STATUS" "$BODY" "Freelance"
TX3_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

# 4.4 Future date → 400
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"amount":100.00,"date":"2099-01-01","category":"Salary","description":"Future"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Future date transaction returns 400" "400" "$STATUS"

# 4.5 Invalid category → 404
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"amount":100.00,"date":"2024-01-01","category":"NoSuchCategory","description":"Bad"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Invalid category returns 404" "404" "$STATUS"

# 4.6 Negative amount → 400
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"amount":-100.00,"date":"2024-01-01","category":"Salary","description":"Negative"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Negative amount returns 400" "400" "$STATUS"

# 4.7 Get all transactions
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/transactions" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Get all transactions returns 200" "200" "$STATUS" "$BODY" "transactions"

# 4.8 Filter by date range
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/transactions?startDate=2024-01-01&endDate=2024-01-31" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Filter transactions by date range" "200" "$STATUS" "$BODY" "transactions"

# 4.9 Update transaction (amount + description only, no date)
RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/transactions/$TX1_ID" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"amount":60000.00,"description":"Updated January Salary"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Update transaction returns 200" "200" "$STATUS" "$BODY" "60000"

# 4.10 Update non-existent transaction → 404
RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/transactions/99999" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"amount":100.00}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Update non-existent transaction returns 404" "404" "$STATUS"

# 4.11 Data isolation - user2 cannot see user1's transactions
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/transactions" -b "$COOKIE_FILE2")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "User2 sees only own transactions (isolation)" "200" "$STATUS"
# user2 should have empty list
if echo "$BODY" | grep -q '"transactions":\[\]'; then
  pass "User2 transaction list is empty (data isolated)"
else
  fail "User2 transaction list should be empty"
fi

# 4.12 Cannot delete category used in transaction → 400
RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/categories/Freelance" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Cannot delete category with transactions returns 400" "400" "$STATUS"

# ════════════════════════════════════════════════════════════
section "5. SAVINGS GOALS"
# ════════════════════════════════════════════════════════════

# 5.1 Create goal
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/goals" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"goalName":"Emergency Fund","targetAmount":5000.00,"targetDate":"2027-01-01","startDate":"2024-01-01"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Create savings goal returns 201" "201" "$STATUS" "$BODY" "Emergency Fund"
check "Goal has currentProgress field" "201" "$STATUS" "$BODY" "currentProgress"
check "Goal has progressPercentage field" "201" "$STATUS" "$BODY" "progressPercentage"
check "Goal has remainingAmount field" "201" "$STATUS" "$BODY" "remainingAmount"
GOAL1_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

# 5.2 Past target date → 400
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/goals" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"goalName":"Old Goal","targetAmount":1000.00,"targetDate":"2020-01-01"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Past target date returns 400" "400" "$STATUS"

# 5.3 Get all goals
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/goals" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Get all goals returns 200" "200" "$STATUS" "$BODY" "goals"

# 5.4 Get goal by ID
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/goals/$GOAL1_ID" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Get goal by ID returns 200" "200" "$STATUS" "$BODY" "Emergency Fund"

# 5.5 Get non-existent goal → 404
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/goals/99999" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Get non-existent goal returns 404" "404" "$STATUS"

# 5.6 Update goal
RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/goals/$GOAL1_ID" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE" \
  -d '{"targetAmount":6000.00,"targetDate":"2027-06-01"}')
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Update goal returns 200" "200" "$STATUS" "$BODY" "6000"

# 5.7 User2 cannot access user1's goal → 404
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/goals/$GOAL1_ID" -b "$COOKIE_FILE2")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "User2 cannot access User1 goal (data isolation)" "404" "$STATUS"

# 5.8 Delete goal
RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/goals/$GOAL1_ID" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Delete goal returns 200" "200" "$STATUS" "$BODY" "deleted"

# 5.9 Delete non-existent goal → 404
RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/goals/99999" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Delete non-existent goal returns 404" "404" "$STATUS"

# ════════════════════════════════════════════════════════════
section "6. REPORTS"
# ════════════════════════════════════════════════════════════

# 6.1 Monthly report
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/reports/monthly/2024/1" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Monthly report returns 200" "200" "$STATUS" "$BODY" "netSavings"
check "Monthly report has totalIncome" "200" "$STATUS" "$BODY" "totalIncome"
check "Monthly report has totalExpenses" "200" "$STATUS" "$BODY" "totalExpenses"
check "Monthly report month=1" "200" "$STATUS" "$BODY" '"month":1'

# 6.2 Yearly report
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/reports/yearly/2024" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Yearly report returns 200" "200" "$STATUS" "$BODY" "netSavings"
check "Yearly report has totalIncome" "200" "$STATUS" "$BODY" "totalIncome"
check "Yearly report year=2024" "200" "$STATUS" "$BODY" '"year":2024'

# 6.3 Invalid month → 400
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/reports/monthly/2024/13" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Invalid month (13) returns 400" "400" "$STATUS"

# 6.4 Report without auth → 401
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/reports/monthly/2024/1")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Report without auth returns 401" "401" "$STATUS"

# ════════════════════════════════════════════════════════════
section "7. LOGOUT"
# ════════════════════════════════════════════════════════════

# 7.1 Logout
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/logout" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Logout returns 200" "200" "$STATUS" "$BODY" "successful"

# 7.2 Access after logout → 401
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/transactions" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Access after logout returns 401" "401" "$STATUS"

# ════════════════════════════════════════════════════════════
section "8. TRANSACTION DELETE"
# ════════════════════════════════════════════════════════════

# Re-login for delete tests
curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -c "$COOKIE_FILE" \
  -d '{"username":"user1@test.com","password":"password123"}' > /dev/null

# 8.1 Delete transaction
RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/transactions/$TX2_ID" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Delete transaction returns 200" "200" "$STATUS" "$BODY" "deleted"

# 8.2 Delete already-deleted transaction → 404
RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/transactions/$TX2_ID" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Re-delete transaction returns 404" "404" "$STATUS"

# 8.3 Delete non-existent transaction → 404
RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/transactions/99999" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Delete non-existent transaction returns 404" "404" "$STATUS"

# 8.4 Now delete Freelance category (no more transactions using it after delete above... 
# TX3 still uses Freelance, so should still be 400)
RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/categories/Freelance" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Category with active transaction still cannot be deleted" "400" "$STATUS"

# Delete TX3 first, then category
curl -s -X DELETE "$BASE_URL/transactions/$TX3_ID" -b "$COOKIE_FILE" > /dev/null
RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/categories/Freelance" -b "$COOKIE_FILE")
BODY=$(echo "$RESP" | head -n-1); STATUS=$(echo "$RESP" | tail -n1)
check "Delete custom category after removing transactions returns 200" "200" "$STATUS" "$BODY" "deleted"

# ════════════════════════════════════════════════════════════
# SUMMARY
# ════════════════════════════════════════════════════════════
rm -f "$COOKIE_FILE" "$COOKIE_FILE2"

echo ""
echo -e "${BOLD}================================================${NC}"
echo -e "${BOLD}           TEST EXECUTION SUMMARY              ${NC}"
echo -e "${BOLD}================================================${NC}"
echo -e " Base URL:     ${CYAN}$BASE_URL${NC}"
echo -e " Total Tests:  ${BOLD}$TOTAL${NC}"
echo -e " Tests Passed: ${GREEN}${BOLD}$PASSED${NC}"
echo -e " Tests Failed: ${RED}${BOLD}$FAILED${NC}"

PCT=$(echo "scale=1; $PASSED * 100 / $TOTAL" | bc)
echo -e " Success Rate: ${BOLD}$PCT%${NC}"
echo -e "${BOLD}================================================${NC}"

if [[ $FAILED -gt 0 ]]; then
  echo -e "\n${RED}${BOLD}Failed Tests:${NC}"
  for t in "${FAILED_TESTS[@]}"; do
    echo -e "  ${RED}✘${NC} $t"
  done
  echo ""
  exit 1
else
  echo ""
  echo -e "${GREEN}${BOLD}🎉 ALL TESTS PASSED! 🎉${NC}"
  echo -e "${GREEN}The Personal Finance Manager API is working correctly.${NC}"
  echo ""
fi