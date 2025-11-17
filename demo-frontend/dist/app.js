"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const cors_1 = __importDefault(require("cors"));
const path_1 = __importDefault(require("path"));
const demo_runner_1 = require("./demo-runner");
const app = (0, express_1.default)();
const PORT = 3000;
app.use((0, cors_1.default)());
app.use(express_1.default.json());
app.use(express_1.default.static(path_1.default.join(__dirname, '../public')));
let currentDemo = null;
let currentSteps = [];
// API Routes
app.get('/api/demo/status', (req, res) => {
    res.json({ steps: currentSteps });
});
app.post('/api/demo/start', async (req, res) => {
    try {
        currentDemo = new demo_runner_1.DemoRunner((steps) => {
            currentSteps = steps;
            console.log('Demo step updated:', steps.filter(s => s.status !== 'pending').pop());
        });
        // Start the demo asynchronously
        currentDemo.runDemo().catch(error => {
            console.error('Demo execution failed:', error);
        });
        currentSteps = currentDemo.getSteps();
        res.json({ success: true, message: 'Demo started' });
    }
    catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});
app.post('/api/demo/reset', (req, res) => {
    currentDemo = null;
    currentSteps = [];
    res.json({ success: true, message: 'Demo reset' });
});
// Serve the main HTML page
app.get('/', (req, res) => {
    res.sendFile(path_1.default.join(__dirname, '../public/index.html'));
});
app.listen(PORT, () => {
    console.log(`Shopping Service Demo Frontend running at http://localhost:${PORT}`);
    console.log('Available endpoints:');
    console.log('  GET  / - Demo dashboard');
    console.log('  POST /api/demo/start - Start demo');
    console.log('  GET  /api/demo/status - Get demo status');
    console.log('  POST /api/demo/reset - Reset demo');
});
