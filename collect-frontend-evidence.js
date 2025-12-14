/**
 * Frontend Evidence Collection Script
 * 
 * This script makes a verify-email API call and captures all request/response details.
 * Run this while the backend is running to collect runtime evidence.
 */

// Note: This script requires axios to be installed
// Run: npm install axios (in root directory, or use client/node_modules)
const path = require('path');
let axios;
try {
  axios = require('axios');
} catch (e) {
  // Try client/node_modules
  try {
    const axiosPath = path.join(__dirname, 'client', 'node_modules', 'axios');
    axios = require(axiosPath);
  } catch (e2) {
    console.error('❌ axios not found. Please install: npm install axios');
    console.error('   Or run this script from client directory');
    process.exit(1);
  }
}

// Configuration
const API_BASE_URL = process.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
const TEST_EMAIL = 'dangtuananh04081972@gmail.com';
const TEST_CODE = '123456'; // Any test code

console.log('='.repeat(60));
console.log('FRONTEND NETWORK TRACE EVIDENCE COLLECTION');
console.log('='.repeat(60));
console.log('');

// Log configuration
console.log('📋 Configuration:');
console.log(`   API Base URL: ${API_BASE_URL}`);
console.log(`   Test Email: ${TEST_EMAIL}`);
console.log(`   Test Code: ${TEST_CODE}`);
console.log('');

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  validateStatus: function (status) {
    // Don't throw on any status - we want to capture all responses
    return true;
  }
});

// Add request interceptor to log request details
api.interceptors.request.use(
  (config) => {
    console.log('📤 REQUEST DETAILS:');
    console.log(`   Method: ${config.method.toUpperCase()}`);
    console.log(`   URL: ${config.baseURL}${config.url}`);
    console.log(`   Headers:`, JSON.stringify(config.headers, null, 2));
    console.log(`   Payload:`, JSON.stringify(config.data, null, 2));
    console.log('');
    return config;
  },
  (error) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

// Add response interceptor to log response details
api.interceptors.response.use(
  (response) => {
    console.log('📥 RESPONSE DETAILS:');
    console.log(`   Status Code: ${response.status}`);
    console.log(`   Status Text: ${response.statusText}`);
    console.log(`   Headers:`, JSON.stringify(response.headers, null, 2));
    console.log(`   Response Body (Raw JSON):`);
    console.log(JSON.stringify(response.data, null, 2));
    console.log('');
    return response;
  },
  (error) => {
    console.log('📥 ERROR RESPONSE DETAILS:');
    if (error.response) {
      console.log(`   Status Code: ${error.response.status}`);
      console.log(`   Status Text: ${error.response.statusText}`);
      console.log(`   Headers:`, JSON.stringify(error.response.headers, null, 2));
      console.log(`   Response Body (Raw JSON):`);
      console.log(JSON.stringify(error.response.data, null, 2));
    } else if (error.request) {
      console.log('   No response received');
      console.log(`   Request:`, error.request);
    } else {
      console.log(`   Error: ${error.message}`);
    }
    console.log(`   Full Error:`, error);
    console.log('');
    return Promise.reject(error);
  }
);

// Make the verify-email request
async function collectEvidence() {
  console.log('🚀 Making verify-email request...');
  console.log('');

  try {
    const response = await api.post('/auth/verify-email', {
      email: TEST_EMAIL,
      code: TEST_CODE
    });

    console.log('='.repeat(60));
    console.log('SUMMARY:');
    console.log('='.repeat(60));
    console.log(`   Request URL: ${API_BASE_URL}/auth/verify-email`);
    console.log(`   HTTP Method: POST`);
    console.log(`   Request Payload: { email: "${TEST_EMAIL}", code: "${TEST_CODE}" }`);
    console.log(`   Response Status Code: ${response.status}`);
    console.log(`   Response Body:`, JSON.stringify(response.data, null, 2));
    console.log('='.repeat(60));

  } catch (error) {
    console.log('='.repeat(60));
    console.log('SUMMARY (ERROR):');
    console.log('='.repeat(60));
    console.log(`   Request URL: ${API_BASE_URL}/auth/verify-email`);
    console.log(`   HTTP Method: POST`);
    console.log(`   Request Payload: { email: "${TEST_EMAIL}", code: "${TEST_CODE}" }`);
    
    if (error.response) {
      console.log(`   Response Status Code: ${error.response.status}`);
      console.log(`   Response Body:`, JSON.stringify(error.response.data, null, 2));
    } else {
      console.log(`   Error: ${error.message}`);
      console.log(`   No response received (network error or CORS issue)`);
    }
    console.log('='.repeat(60));
  }
}

// Run evidence collection
collectEvidence().catch(console.error);

