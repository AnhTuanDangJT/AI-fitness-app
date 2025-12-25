/**
 * Backend Evidence Collection Script
 * 
 * This script checks backend logs and configuration.
 * Run this to collect backend runtime evidence.
 */

const fs = require('fs');
const path = require('path');

console.log('='.repeat(60));
console.log('BACKEND LOG TRACE EVIDENCE COLLECTION');
console.log('='.repeat(60));
console.log('');

// Check log file
const logFile = path.join(__dirname, 'logs', 'spring-boot.log');

console.log('📋 Checking log file:');
console.log(`   Path: ${logFile}`);
console.log('');

if (fs.existsSync(logFile)) {
  console.log('✅ Log file exists');
  console.log('');
  
  // Read last 200 lines
  const logContent = fs.readFileSync(logFile, 'utf8');
  const lines = logContent.split('\n');
  const lastLines = lines.slice(-200);
  
  console.log('📄 Last 200 lines of log file:');
  console.log('-'.repeat(60));
  lastLines.forEach(line => console.log(line));
  console.log('-'.repeat(60));
  console.log('');
  
  // Search for verify-email related logs
  console.log('🔍 Searching for verify-email related logs:');
  const verifyLogs = lines.filter(line => 
    line.toLowerCase().includes('verify') || 
    line.toLowerCase().includes('verification') ||
    line.toLowerCase().includes('exception') ||
    line.toLowerCase().includes('error')
  );
  
  if (verifyLogs.length > 0) {
    console.log(`   Found ${verifyLogs.length} relevant log entries:`);
    verifyLogs.slice(-50).forEach(line => console.log(`   ${line}`));
  } else {
    console.log('   No verify-email related logs found');
  }
  console.log('');
  
} else {
  console.log('❌ Log file does not exist');
  console.log('   Make sure backend is running and logging to file');
  console.log('');
}

// Check application properties
console.log('📋 Checking application properties:');
const appProps = path.join(__dirname, 'src', 'main', 'resources', 'application.properties');
const appPropsProd = path.join(__dirname, 'src', 'main', 'resources', 'application-production.properties');

if (fs.existsSync(appProps)) {
  console.log('✅ Found application.properties');
  const props = fs.readFileSync(appProps, 'utf8');
  const mailConfig = props.split('\n').filter(line => 
    line.includes('mail') || line.includes('MAIL') || line.includes('email')
  );
  console.log('   Email configuration:');
  mailConfig.forEach(line => {
    // Don't show passwords
    if (!line.includes('password') && !line.includes('PASSWORD')) {
      console.log(`   ${line}`);
    } else {
      console.log(`   ${line.split('=')[0]}=*** (hidden)`);
    }
  });
  console.log('');
}

if (fs.existsSync(appPropsProd)) {
  console.log('✅ Found application-production.properties');
  const props = fs.readFileSync(appPropsProd, 'utf8');
  const mailConfig = props.split('\n').filter(line => 
    line.includes('mail') || line.includes('MAIL') || line.includes('email')
  );
  console.log('   Email configuration:');
  mailConfig.forEach(line => {
    // Don't show passwords
    if (!line.includes('password') && !line.includes('PASSWORD')) {
      console.log(`   ${line}`);
    } else {
      console.log(`   ${line.split('=')[0]}=*** (hidden)`);
    }
  });
  console.log('');
}

// Check environment variables (if available)
console.log('📋 Environment Variables (if available):');
console.log(`   MAIL_HOST: ${process.env.MAIL_HOST || 'NOT SET'}`);
console.log(`   MAIL_PORT: ${process.env.MAIL_PORT || 'NOT SET'}`);
console.log(`   MAIL_USERNAME: ${process.env.MAIL_USERNAME || 'NOT SET'}`);
console.log(`   APP_EMAIL_FROM: ${process.env.APP_EMAIL_FROM || 'NOT SET'}`);
console.log(`   MAIL_PASSWORD: ${process.env.MAIL_PASSWORD ? '*** (set)' : 'NOT SET'}`);
console.log('');

console.log('='.repeat(60));
console.log('INSTRUCTIONS:');
console.log('='.repeat(60));
console.log('1. Check backend console output for startup logs');
console.log('2. Look for "Email service is configured" or "Email service is NOT configured"');
console.log('3. Trigger a verify-email request and capture console output');
console.log('4. Look for exception stack traces');
console.log('='.repeat(60));









