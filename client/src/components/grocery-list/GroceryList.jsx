import React, { useState, useEffect } from 'react'
import { mealPlanAPI } from '../../services/api'
import jsPDF from 'jspdf'
import './GroceryList.css'

function GroceryList({ mealPlan }) {
  const [groceryList, setGroceryList] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [checkedItems, setCheckedItems] = useState(new Set())

  // Get storage key based on user and week
  const getStorageKey = () => {
    const userData = localStorage.getItem('user')
    const userId = userData ? JSON.parse(userData).id : 'anonymous'
    const weekStart = mealPlan?.weekStartDate || 'default'
    return `grocery_checked_${userId}_${weekStart}`
  }

  // Load checked state from localStorage
  const loadCheckedState = () => {
    try {
      const storageKey = getStorageKey()
      const stored = localStorage.getItem(storageKey)
      if (stored) {
        const checkedArray = JSON.parse(stored)
        return new Set(checkedArray)
      }
    } catch (error) {
      console.error('Error loading checked state from localStorage:', error)
    }
    return new Set()
  }

  // Save checked state to localStorage
  const saveCheckedState = (checkedSet) => {
    try {
      // Only save if mealPlan is available
      if (mealPlan?.weekStartDate) {
        const storageKey = getStorageKey()
        const checkedArray = Array.from(checkedSet)
        localStorage.setItem(storageKey, JSON.stringify(checkedArray))
      }
    } catch (error) {
      console.error('Error saving checked state to localStorage:', error)
    }
  }

  // Load persisted checked state when mealPlan/week changes
  useEffect(() => {
    // Only load persisted state if mealPlan is available
    if (mealPlan?.weekStartDate) {
      const persistedChecked = loadCheckedState()
      setCheckedItems(persistedChecked)
    }
  }, [mealPlan?.weekStartDate])

  useEffect(() => {
    fetchGroceryList()
  }, [mealPlan])

  const fetchGroceryList = async () => {
    try {
      setLoading(true)
      setError('')
      
      const result = await mealPlanAPI.getGroceryList()
      
      // Handle structured response from API
      if (result.type === 'SUCCESS') {
        // Grocery list found - set success state
        setGroceryList(result.data || [])
        
        // Merge persisted checked state with API's alreadyHave flags
        // Don't reset checkedItems - preserve user's selections
        setCheckedItems(prevChecked => {
          const merged = new Set(prevChecked)
          // Add items marked as alreadyHave in API response
          result.data?.forEach((item, index) => {
            if (item.alreadyHave) {
              merged.add(index)
            }
          })
          // Save merged state
          saveCheckedState(merged)
          return merged
        })
      } else if (result.type === 'EMPTY') {
        // No meal plan exists - this is a valid empty state, not an error
        // HTTP 404 from backend means user hasn't generated a meal plan yet
        setGroceryList([])
        setError('') // Clear any error state
      } else if (result.type === 'ERROR') {
        // Real API/network error occurred
        setError(result.error)
      } else {
        // Unexpected response format
        setError('Failed to load grocery list')
      }
    } catch (err) {
      // This catch block should rarely execute since API service handles errors
      // But keep it as a safety net
      setError(err.message || 'Failed to load grocery list')
      console.error('Error fetching grocery list:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleToggleItem = (index) => {
    const newChecked = new Set(checkedItems)
    if (newChecked.has(index)) {
      newChecked.delete(index)
    } else {
      newChecked.add(index)
    }
    setCheckedItems(newChecked)
    // Persist to localStorage immediately
    saveCheckedState(newChecked)
  }

  const handleDownloadPDF = () => {
    if (groceryList.length === 0) {
      alert('No items to export')
      return
    }

    const doc = new jsPDF()
    const pageWidth = doc.internal.pageSize.getWidth()
    const pageHeight = doc.internal.pageSize.getHeight()
    const margin = 25 // Increased left/right margins for better readability
    const lineHeight = 7 // Increased line height for better spacing
    const itemSpacing = 6 // Spacing between items
    let yPos = margin

    // Header
    doc.setFontSize(22)
    doc.setFont('helvetica', 'bold')
    doc.text('AI Fitness - Grocery List', pageWidth / 2, yPos, { align: 'center' })
    yPos += 18 // Increased spacing after header

    // Week range
    if (mealPlan?.weekStartDate) {
      doc.setFontSize(12)
      doc.setFont('helvetica', 'normal')
      const startDate = new Date(mealPlan.weekStartDate)
      const endDate = new Date(startDate)
      endDate.setDate(startDate.getDate() + 6)
      const weekRange = `${startDate.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} - ${endDate.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}`
      doc.text(`Week: ${weekRange}`, pageWidth / 2, yPos, { align: 'center' })
      yPos += 12
    }

    // Date generated
    doc.setFontSize(10)
    doc.setFont('helvetica', 'italic')
    doc.text(`Generated: ${new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}`, pageWidth / 2, yPos, { align: 'center' })
    yPos += 18 // Increased spacing before separator

    // Line separator
    doc.setLineWidth(0.5)
    doc.line(margin, yPos, pageWidth - margin, yPos)
    yPos += 15 // Increased spacing after separator

    // Grocery items section title
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Items to Buy:', margin, yPos)
    yPos += 12 // Increased spacing after section title

    // Set consistent font for items
    doc.setFontSize(12) // Increased from 11pt for better readability
    doc.setFont('helvetica', 'normal')

    // Calculate right-aligned position for quantities
    const quantityXPos = pageWidth - margin - 20 // Right-aligned with margin

    groceryList.forEach((item, index) => {
      // Check if we need a new page (with better margin to prevent splitting)
      // Reserve space for at least one full item (lineHeight + itemSpacing)
      if (yPos > pageHeight - 40) {
        doc.addPage()
        yPos = margin
      }

      const isChecked = checkedItems.has(index)
      const checkbox = isChecked ? '[x]' : '[ ]'
      const itemName = item.name
      const quantityText = item.quantityText || ''

      // Add checkbox and item name
      doc.setFont('helvetica', 'normal')
      doc.setFontSize(12)
      doc.text(`${checkbox} ${itemName}`, margin + 5, yPos)
      
      // Add quantity right-aligned if available
      if (quantityText) {
        doc.setFont('helvetica', 'normal')
        doc.setFontSize(12) // Same size as item name for consistency
        // Get text width to properly align
        const quantityWidth = doc.getTextWidth(quantityText)
        doc.text(quantityText, quantityXPos - quantityWidth, yPos)
      }

      yPos += lineHeight + itemSpacing // Increased spacing between items
    })

    // Footer
    const totalPages = doc.internal.pages.length - 1
    for (let i = 1; i <= totalPages; i++) {
      doc.setPage(i)
      doc.setFontSize(9) // Slightly increased footer font
      doc.setFont('helvetica', 'normal')
      doc.text(`Page ${i} of ${totalPages}`, pageWidth / 2, pageHeight - 15, { align: 'center' })
    }

    // Generate filename
    const today = new Date()
    const filename = `grocery-list-${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}.pdf`

    // Save PDF
    doc.save(filename)
  }

  if (loading) {
    return (
      <div className="grocery-list-container">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading grocery list...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="grocery-list-container">
        <div className="error-container">
          <p className="error-message">{error}</p>
          <button onClick={fetchGroceryList} className="retry-button">
            Retry
          </button>
        </div>
      </div>
    )
  }

  if (groceryList.length === 0 && !error) {
    return (
      <div className="grocery-list-container">
        <div className="empty-container">
          <p>Generate a meal plan to see your grocery list</p>
        </div>
      </div>
    )
  }

  const uncheckedCount = groceryList.length - checkedItems.size

  return (
    <div className="grocery-list-container">
      <div className="grocery-list-header">
        <h2>Grocery List</h2>
        <div className="grocery-list-actions">
          <span className="item-count">
            {uncheckedCount} of {groceryList.length} items remaining
          </span>
          <button onClick={handleDownloadPDF} className="download-pdf-button">
            Download as PDF
          </button>
        </div>
      </div>

      <div className="grocery-list-items">
        {groceryList.map((item, index) => {
          const isChecked = checkedItems.has(index)
          return (
            <div
              key={index}
              className={`grocery-item ${isChecked ? 'checked' : ''}`}
            >
              <label className="grocery-item-label">
                <input
                  type="checkbox"
                  checked={isChecked}
                  onChange={() => handleToggleItem(index)}
                  className="grocery-item-checkbox"
                />
                <span className="grocery-item-name">{item.name}</span>
                {item.quantityText && (
                  <span className="grocery-item-quantity">{item.quantityText}</span>
                )}
              </label>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default GroceryList



