// Seeds the Firestore emulator with sample restaurant data for local
// development / demoing. Run with the emulator already started:
//   FIRESTORE_EMULATOR_HOST=localhost:8080 node scripts/seed.js

process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST || "localhost:8081";

const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");

initializeApp({ projectId: "demo-lunchbox" });
const db = getFirestore();

const restaurants = [
  { name: "Golden Wok", location: "Main St", cuisine: "Chinese", averageRating: 4.8, reviewCount: 32 },
  { name: "Casa Luna", location: "Raymond Ave", cuisine: "Mexican", averageRating: 4.6, reviewCount: 21 },
  { name: "Spice Route", location: "College Ave", cuisine: "Indian", averageRating: 4.5, reviewCount: 18 },
  { name: "Trattoria Bella", location: "Main St", cuisine: "Italian", averageRating: 4.3, reviewCount: 27 },
  { name: "Blue Fin Sushi", location: "Raymond Ave", cuisine: "Japanese", averageRating: 4.7, reviewCount: 40 },
  { name: "The Falafel Spot", location: "College Ave", cuisine: "Middle Eastern", averageRating: 4.2, reviewCount: 14 },
];

async function seed() {
  const batch = db.batch();
  for (const restaurant of restaurants) {
    const ref = db.collection("restaurants").doc();
    batch.set(ref, restaurant);
  }
  await batch.commit();
  console.log(`Seeded ${restaurants.length} restaurants into the Firestore emulator.`);
}

seed().then(() => process.exit(0)).catch((err) => {
  console.error(err);
  process.exit(1);
});
