const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_bn_123";
const BOB_UID = "bob_bn_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write chats or messages", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("chats").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("chats").doc("chat_1").set({
      userId: ALICE_UID,
      title: "নতুন চ্যাট",
      lastMessagePreview: "হ্যালো",
      createdAtMs: 1000,
      updatedAtMs: 1000,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Authenticated owner can create, update, list, and delete chats and messages", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const past = new Date(Date.now() - 5000);
  const chatRef = aliceDb.collection("users").doc(ALICE_UID).collection("chats").doc("chat_abc1");

  await assertSucceeds(
    chatRef.set({
      userId: ALICE_UID,
      title: "ভাষা আন্দোলন ১৯৫২",
      lastMessagePreview: "১৯৫২ সালের ভাষা আন্দোলন...",
      createdAtMs: 1700000000000,
      updatedAtMs: 1700000000000,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Batch / child message create with optional attachment fields
  await assertSucceeds(
    chatRef.collection("messages").doc("msg_1").set({
      userId: ALICE_UID,
      role: "me",
      text: "এই ছবিটি দেখুন",
      attachmentName: "book.jpg",
      attachmentType: "image",
      timestampMs: 1700000001000,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Partial update on chat preserving immutable createdAt & userId
  await assertSucceeds(
    chatRef.update({
      title: "১৯৫২ ভাষা আন্দোলন",
      lastMessagePreview: "আপডেট করা উত্তর",
      updatedAtMs: 1700000002000,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // List chats scoped to owner
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("chats").get()
  );

  // List messages scoped to owner
  await assertSucceeds(chatRef.collection("messages").get());

  // Delete message and chat
  await assertSucceeds(chatRef.collection("messages").doc("msg_1").delete());
  await assertSucceeds(chatRef.delete());
});

test("Cross-user isolation blocks Bob from reading or writing Alice chats", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(ALICE_UID).collection("chats").doc("chat_alice").set({
      userId: ALICE_UID,
      title: "Alice Private Chat",
      lastMessagePreview: "Secret",
      createdAtMs: 1000,
      updatedAtMs: 1000,
      createdAt: new Date(Date.now() - 5000),
      updatedAt: new Date(Date.now() - 5000),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("chats").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("chats").doc("chat_alice").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("chats").doc("chat_alice").update({
      title: "Hijacked",
      updatedAtMs: 2000,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("chats").doc("chat_alice").delete());
});

test("Schema validation rejects spoofed userId, ghost fields, immutable mutation, or bad role", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const past = new Date(Date.now() - 5000);
  const chatRef = aliceDb.collection("users").doc(ALICE_UID).collection("chats").doc("chat_sec");

  // Spoofed userId
  await assertFails(
    chatRef.set({
      userId: BOB_UID,
      title: "Spoof",
      lastMessagePreview: "",
      createdAtMs: 1000,
      updatedAtMs: 1000,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Extra ghost field (`id` or `isAdmin`)
  await assertFails(
    chatRef.set({
      id: "chat_sec",
      userId: ALICE_UID,
      title: "Ghost",
      lastMessagePreview: "",
      createdAtMs: 1000,
      updatedAtMs: 1000,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Valid create
  await assertSucceeds(
    chatRef.set({
      userId: ALICE_UID,
      title: "Valid",
      lastMessagePreview: "",
      createdAtMs: 1000,
      updatedAtMs: 1000,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Reject immutable createdAt update
  await assertFails(
    chatRef.update({
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject invalid message role
  await assertFails(
    chatRef.collection("messages").doc("bad_msg").set({
      userId: ALICE_UID,
      role: "admin",
      text: "hi",
      timestampMs: 1000,
      createdAt: past,
      updatedAt: past,
    })
  );
});
