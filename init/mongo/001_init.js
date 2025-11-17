db = db.getSiblingDB('item');
db.createCollection('items');
db.items.createIndex({ upc: 1 }, { unique: true });