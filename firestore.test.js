const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("unauthenticated user cannot read vehicle entries", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("users").doc("user1").collection("vehicle_entries").get());
});

test("authenticated user can create their own valid vehicle entry", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc("alice").collection("vehicle_entries").add({
      userId: "alice",
      date: "2026-10-26",
      petrolCount: 10,
      petrolAmount: 1000.0,
      dieselCount: 5,
      dieselAmount: 600.0,
      truckCount: 3,
      truckAmount: 540.0
    })
  );
});

test("cross-user isolation: bob cannot read alice's vehicle entries", async () => {
  const bobDb = testEnv.authenticatedContext("bob").firestore();
  await assertFails(bobDb.collection("users").doc("alice").collection("vehicle_entries").get());
});

test("validation: invalid expenditure with negative amount is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertFails(
    aliceDb.collection("users").doc("alice").collection("expenditures").add({
      userId: "alice",
      date: "2026-10-26",
      description: "Invalid expense",
      amount: -100.0,
      category: "Stationery"
    })
  );
});

test("valid expenditure is accepted for owner", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc("alice").collection("expenditures").add({
      userId: "alice",
      date: "2026-10-26",
      description: "PUC certificate paper roll",
      amount: 250.0,
      category: "Stationery"
    })
  );
});
