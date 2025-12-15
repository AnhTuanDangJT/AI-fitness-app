/**
 * Script to send weekly progress data to /api/progress/weekly
 * 
 * Run this in the browser console while logged into the dashboard.
 * Or use this as a reference for Postman requests.
 * 
 * Usage:
 * 1. Open browser console (F12)
 * 2. Copy and paste this entire script
 * 3. The script will automatically send both weekly progress entries
 */

(async function() {
  // Get the API base URL (same as in api.js)
  const API_BASE_URL = '/api';
  
  // Get authentication token from localStorage
  const token = localStorage.getItem('token');
  
  if (!token) {
    console.error('❌ No authentication token found. Please log in first.');
    return;
  }
  
  console.log('✅ Authentication token found');
  
  // First week progress entry
  const week1Data = {
    weekStartDate: "2025-12-01",
    weight: 75,
    sleepHoursPerNightAverage: 7,
    stressLevel: 4,
    hungerLevel: 5,
    energyLevel: 7,
    trainingSessionsCompleted: 3,
    caloriesAverage: 2100
  };
  
  // Second week progress entry
  const week2Data = {
    weekStartDate: "2025-12-08",
    weight: 74.2,
    sleepHoursPerNightAverage: 6.5,
    stressLevel: 5,
    hungerLevel: 4,
    energyLevel: 6,
    trainingSessionsCompleted: 4,
    caloriesAverage: 2050
  };
  
  /**
   * Helper function to send a POST request
   */
  async function sendWeeklyProgress(data) {
    try {
      const response = await fetch(`${API_BASE_URL}/progress/weekly`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(data)
      });
      
      const result = await response.json();
      
      if (response.ok && result.success) {
        console.log(`✅ Successfully saved progress for week ${data.weekStartDate}:`, result);
        return { success: true, data: result };
      } else {
        console.error(`❌ Failed to save progress for week ${data.weekStartDate}:`, result);
        return { success: false, error: result };
      }
    } catch (error) {
      console.error(`❌ Error sending progress for week ${data.weekStartDate}:`, error);
      return { success: false, error: error.message };
    }
  }
  
  // Send both entries sequentially
  console.log('📤 Sending first week progress (2025-12-01)...');
  const result1 = await sendWeeklyProgress(week1Data);
  
  // Wait a moment before sending the second entry
  await new Promise(resolve => setTimeout(resolve, 500));
  
  console.log('📤 Sending second week progress (2025-12-08)...');
  const result2 = await sendWeeklyProgress(week2Data);
  
  // Summary
  console.log('\n📊 Summary:');
  console.log(`Week 1 (2025-12-01): ${result1.success ? '✅ Success' : '❌ Failed'}`);
  console.log(`Week 2 (2025-12-08): ${result2.success ? '✅ Success' : '❌ Failed'}`);
  
  if (result1.success && result2.success) {
    console.log('\n🎉 Both weekly progress entries saved successfully!');
    console.log('💡 Now refresh the dashboard and click "Refresh advice" to see AI coach recommendations.');
  } else {
    console.log('\n⚠️ Some entries failed. Check the errors above.');
  }
})();






