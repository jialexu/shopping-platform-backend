import express from 'express';
import cors from 'cors';
import path from 'path';
import { DemoRunner } from './demo-runner';
import { DemoStep } from './types';

const app = express();
const PORT = 3000;

app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, '../public')));

let currentDemo: DemoRunner | null = null;
let currentSteps: DemoStep[] = [];

// API Routes
app.get('/api/demo/status', (req, res) => {
  res.json({ steps: currentSteps });
});

app.post('/api/demo/start', async (req, res) => {
  try {
    currentDemo = new DemoRunner((steps) => {
      currentSteps = steps;
      console.log('Demo step updated:', steps.filter(s => s.status !== 'pending').pop());
    });

    // Start the demo asynchronously
    currentDemo.runDemo().catch(error => {
      console.error('Demo execution failed:', error);
    });

    currentSteps = currentDemo.getSteps();
    res.json({ success: true, message: 'Demo started' });
  } catch (error: any) {
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
  res.sendFile(path.join(__dirname, '../public/index.html'));
});

app.listen(PORT, () => {
  console.log(`Shopping Service Demo Frontend running at http://localhost:${PORT}`);
  console.log('Available endpoints:');
  console.log('  GET  / - Demo dashboard');
  console.log('  POST /api/demo/start - Start demo');
  console.log('  GET  /api/demo/status - Get demo status');
  console.log('  POST /api/demo/reset - Reset demo');
});