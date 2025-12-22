import jsPDF from 'jspdf'
import { ensureVietnameseFonts, PDF_FONT_FAMILY } from './pdfFonts'

const MACRO_BAR_HEIGHT = 6
const MACRO_GAP = 5

const hexToRgb = (hex) => {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)

  return result
    ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
      }
    : { r: 0, g: 0, b: 0 }
}

const toNumber = (value) => {
  if (value === null || value === undefined) return null
  const num = Number(value)
  return Number.isFinite(num) ? num : null
}

const formatNumber = (value, decimals = 0, suffix = '', fallback = 'N/A') => {
  const num = toNumber(value)
  if (num === null) return fallback
  const formatted = num.toFixed(decimals)
  return suffix ? `${formatted} ${suffix}` : formatted
}

const defaultTranslator = (key, vars) => {
  if (!vars) return key
  return `${key} ${JSON.stringify(vars)}`
}

const getGenderLabel = (sex, t) => {
  if (sex === null || sex === undefined) return 'N/A'
  return sex ? t('dashboard.male') : t('dashboard.female')
}

const getActivityLevelLabel = (activityLevel, t) => {
  const map = {
    1: t('dashboard.activityLevels.sedentary'),
    2: t('dashboard.activityLevels.lightlyActive'),
    3: t('dashboard.activityLevels.moderatelyActive'),
    4: t('dashboard.activityLevels.veryActive'),
    5: t('dashboard.activityLevels.athlete')
  }
  return map[activityLevel] || t('dashboard.activityLevelUnknown')
}

const getGoalLabel = (calorieGoal, fallback, t) => {
  const map = {
    1: t('dashboard.calorieGoals.loseWeight'),
    2: t('dashboard.calorieGoals.maintain'),
    3: t('dashboard.calorieGoals.gainMuscle'),
    4: t('dashboard.calorieGoals.recomp')
  }

  return map[calorieGoal] || fallback || t('dashboard.goalUnknown')
}

const getMealFrequency = (activityLevel) => {
  if (activityLevel >= 4) return 5
  if (activityLevel >= 2) return 4
  return 3
}

const getHydrationTargetLiters = (weight) => {
  const weightValue = toNumber(weight)
  if (weightValue === null) return null
  // 35ml per kg of bodyweight
  return (weightValue * 0.035).toFixed(1)
}

const drawKeyValueRows = (doc, items, startX, startY, lineHeight) => {
  let offsetY = 0
  items.forEach(({ label, value }) => {
    doc.setFont(PDF_FONT_FAMILY, 'normal')
    doc.text(`${label}:`, startX, startY + offsetY)
    doc.setFont(PDF_FONT_FAMILY, 'bold')
    doc.text(value, startX + 60, startY + offsetY)
    offsetY += lineHeight
  })
  return offsetY
}

const drawTwoColumnRows = (doc, left, right, startX, startY, lineHeight) => {
  const rows = Math.max(left.length, right.length)
  for (let i = 0; i < rows; i += 1) {
    const leftRow = left[i]
    const rightRow = right[i]
    const currentY = startY + i * lineHeight

    if (leftRow) {
      doc.setFont(PDF_FONT_FAMILY, 'normal')
      doc.text(`${leftRow.label}:`, startX, currentY)
      doc.setFont(PDF_FONT_FAMILY, 'bold')
      doc.text(leftRow.value, startX + 60, currentY)
    }

    if (rightRow) {
      doc.setFont(PDF_FONT_FAMILY, 'normal')
      doc.text(`${rightRow.label}:`, startX + 90, currentY)
      doc.setFont(PDF_FONT_FAMILY, 'bold')
      doc.text(rightRow.value, startX + 150, currentY, { align: 'right' })
    }
  }

  return rows * lineHeight
}

const drawMacroChart = (doc, macros, startX, startY, width, options = {}) => {
  const usableMacros = macros.filter((macro) => macro.value !== null)
  const emptyMessage = options.emptyMessage || ''
  const baseColor = options.textColor || { r: 232, g: 234, b: 241 }

  if (!usableMacros.length) {
    doc.setFont(PDF_FONT_FAMILY, 'normal')
    doc.setFontSize(9)
    doc.setTextColor(baseColor.r, baseColor.g, baseColor.b)
    doc.text(emptyMessage, startX, startY + 4, { maxWidth: width })
    return MACRO_BAR_HEIGHT + MACRO_GAP
  }

  const maxValue = Math.max(...usableMacros.map((macro) => macro.value))

  usableMacros.forEach((macro, index) => {
    const offsetY = startY + index * (MACRO_BAR_HEIGHT + MACRO_GAP)
    doc.setFont(PDF_FONT_FAMILY, 'bold')
    doc.setFontSize(9)
    doc.setTextColor(baseColor.r, baseColor.g, baseColor.b)
    doc.text(macro.label, startX, offsetY - 1)

    const barWidth = maxValue > 0 ? (macro.value / maxValue) * width : 0
    doc.setFillColor(macro.color.r, macro.color.g, macro.color.b)
    doc.roundedRect(startX, offsetY, Math.max(barWidth, 10), MACRO_BAR_HEIGHT, 1.5, 1.5, 'F')

    doc.setTextColor(macro.color.r, macro.color.g, macro.color.b)
    doc.text(`${Math.round(macro.value)}g`, startX + Math.max(barWidth + 6, 24), offsetY + MACRO_BAR_HEIGHT - 1)
  })

  doc.setTextColor(baseColor.r, baseColor.g, baseColor.b)
  return usableMacros.length * (MACRO_BAR_HEIGHT + MACRO_GAP)
}

export const generateFitnessProfilePdf = async ({ profileData, t }) => {
  const translate = typeof t === 'function' ? t : defaultTranslator
  const data = profileData || {}

  const doc = new jsPDF('p', 'mm', 'a4')
  await ensureVietnameseFonts(doc)

  const today = new Date().toLocaleDateString()
  const bgColor = hexToRgb('#0D1117')
  const accentColor = hexToRgb('#55C0FF')
  const textColor = hexToRgb('#E8EAF1')
  const sectionBgColor = {
    r: Math.min(255, bgColor.r + 10),
    g: Math.min(255, bgColor.g + 10),
    b: Math.min(255, bgColor.b + 10)
  }

  doc.setFillColor(bgColor.r, bgColor.g, bgColor.b)
  doc.rect(0, 0, 210, 297, 'F')

  doc.setDrawColor(accentColor.r, accentColor.g, accentColor.b)
  doc.setLineWidth(1.5)
  doc.rect(5, 5, 200, 287)

  doc.setFont(PDF_FONT_FAMILY, 'bold')
  doc.setTextColor(accentColor.r, accentColor.g, accentColor.b)
  doc.setFontSize(30)
  const pdfTitle = data.name
    ? translate('dashboard.fitnessProfileName', { name: data.name })
    : translate('dashboard.fitnessProfile')
  doc.text(pdfTitle, 105, 25, { align: 'center' })

  doc.setLineWidth(0.5)
  doc.line(35, 30, 175, 30)

  doc.setFontSize(10)
  doc.setFont(PDF_FONT_FAMILY, 'normal')
  doc.setTextColor(textColor.r, textColor.g, textColor.b)
  doc.text(translate('dashboard.generatedOn', { date: today }), 105, 36, { align: 'center' })

  const sectionX = 20
  const sectionWidth = 170
  const sectionSpacing = 8
  const lineHeight = 7
  let currentY = 46

  const drawSection = (title, height, drawContent) => {
    doc.setFillColor(sectionBgColor.r, sectionBgColor.g, sectionBgColor.b)
    doc.roundedRect(sectionX, currentY, sectionWidth, height, 2, 2, 'F')

    doc.setFont(PDF_FONT_FAMILY, 'bold')
    doc.setFontSize(13)
    doc.setTextColor(accentColor.r, accentColor.g, accentColor.b)
    doc.text(title, sectionX + 5, currentY + 10)

    doc.setTextColor(textColor.r, textColor.g, textColor.b)
    doc.setFont(PDF_FONT_FAMILY, 'normal')
    doc.setFontSize(10.5)

    drawContent(currentY + 20)
    currentY += height + sectionSpacing
  }

  const personalItems = [
    { label: translate('dashboard.name'), value: data.name || 'N/A' },
    { label: translate('dashboard.email'), value: data.email || 'N/A' },
    { label: translate('dashboard.gender'), value: getGenderLabel(data.sex, translate) },
    { label: translate('dashboard.age'), value: formatNumber(data.age, 0, translate('dashboard.years')) }
  ]

  const personalHeight = 18 + personalItems.length * lineHeight + 6
  drawSection(translate('dashboard.personalInformation'), personalHeight, (startY) => {
    drawKeyValueRows(doc, personalItems, sectionX + 5, startY, lineHeight)
  })

  const bodyLeft = [
    { label: translate('dashboard.height'), value: formatNumber(data.height, 0, translate('dashboard.cm')) },
    { label: translate('dashboard.weight'), value: formatNumber(data.weight, 0, translate('dashboard.kg')) },
    { label: translate('dashboard.waist'), value: formatNumber(data.waist, 0, translate('dashboard.cm')) },
    { label: translate('dashboard.hip'), value: formatNumber(data.hip, 0, translate('dashboard.cm')) }
  ]

  const bodyRight = [
    { label: translate('dashboard.bmiValue'), value: formatNumber(data.bmi, 1) },
    { label: translate('dashboard.whrValue'), value: formatNumber(data.whr, 2) },
    { label: translate('dashboard.bodyFat'), value: formatNumber(data.bodyFat, 1, translate('dashboard.percent')) },
    { label: translate('dashboard.bmrValue'), value: formatNumber(data.bmr, 0, translate('dashboard.kcalPerDayShort')) },
    { label: translate('dashboard.tdeeValue'), value: formatNumber(data.tdee, 0, translate('dashboard.kcalPerDayShort')) }
  ]

  const bodyRows = Math.max(bodyLeft.length, bodyRight.length)
  const bodyHeight = 20 + bodyRows * lineHeight + 10
  drawSection(translate('dashboard.bodyMetrics'), bodyHeight, (startY) => {
    drawTwoColumnRows(doc, bodyLeft, bodyRight, sectionX + 5, startY, lineHeight)
  })

  const goalLabel = getGoalLabel(data.calorieGoal, data.fitnessGoal, translate)
  const activityLabel = getActivityLevelLabel(data.activityLevel, translate)
  const mealsPerDay = getMealFrequency(data.activityLevel || 0)
  const caloriesNeeded = formatNumber(data.caloriesNeeded, 0, translate('dashboard.kcalPerDayShort'))
  const caloriesPerMeal = formatNumber(
    data.caloriesNeeded && mealsPerDay ? data.caloriesNeeded / mealsPerDay : null,
    0,
    translate('dashboard.kcalShort')
  )
  const hydrationTarget = getHydrationTargetLiters(data.weight)

  const lifestyleLeft = [
    { label: translate('dashboard.fitnessGoalLabel'), value: goalLabel },
    { label: translate('dashboard.activityLevel'), value: activityLabel },
    { label: translate('dashboard.hydrationTarget'), value: hydrationTarget ? `~${hydrationTarget} L` : 'N/A' }
  ]

  const lifestyleRight = [
    { label: translate('dashboard.caloriesNeeded'), value: caloriesNeeded },
    { label: translate('dashboard.mealsPerDay'), value: `${mealsPerDay}` },
    { label: translate('dashboard.perMeal'), value: caloriesPerMeal }
  ]

  const lifestyleRows = Math.max(lifestyleLeft.length, lifestyleRight.length)
  const lifestyleHeight = 32 + lifestyleRows * lineHeight
  drawSection(translate('dashboard.goalAndLifestyle'), lifestyleHeight, (startY) => {
    const usedHeight = drawTwoColumnRows(doc, lifestyleLeft, lifestyleRight, sectionX + 5, startY, lineHeight)
    const summaryY = startY + usedHeight + 6
    doc.setFont(PDF_FONT_FAMILY, 'normal')
    doc.setFontSize(9.5)
    const summaryText = goalLabel && activityLabel
      ? translate('dashboard.lifestyleSummary', { goal: goalLabel, activity: activityLabel })
      : translate('dashboard.lifestyleSummaryFallback')
    doc.text(summaryText, sectionX + 5, summaryY, { maxWidth: sectionWidth - 10 })
  })

  const macros = [
    {
      label: translate('dashboard.protein'),
      value: toNumber(data.protein),
      color: hexToRgb('#55C0FF')
    },
    {
      label: translate('dashboard.carbs'),
      value: toNumber(data.carbs),
      color: hexToRgb('#79FFB2')
    },
    {
      label: translate('dashboard.fat'),
      value: toNumber(data.fat),
      color: hexToRgb('#FF8E71')
    }
  ]

  const nutritionRows = [
    {
      label: translate('dashboard.caloriesNeeded'),
      daily: caloriesNeeded,
      perMeal: caloriesPerMeal
    },
    ...macros.map((macro) => ({
      label: macro.label,
      daily: formatNumber(macro.value, 0, translate('dashboard.gPerDay')),
      perMeal: formatNumber(
        macro.value && mealsPerDay ? macro.value / mealsPerDay : null,
        0,
        translate('dashboard.gramsPerMeal')
      )
    }))
  ]

  const macroEntries = Math.max(macros.filter((macro) => macro.value !== null).length, 1)
  const tableHeight = (nutritionRows.length + 1) * lineHeight
  const chartSectionHeight = 18 + macroEntries * (MACRO_BAR_HEIGHT + MACRO_GAP)
  const nutritionHeight = 20 + tableHeight + chartSectionHeight

  drawSection(translate('dashboard.dailyNutritionTargets'), nutritionHeight, (startY) => {
    const headerY = startY
    doc.setFont(PDF_FONT_FAMILY, 'bold')
    doc.setFontSize(10)
    doc.text(translate('dashboard.daily'), sectionX + 85, headerY)
    doc.text(translate('dashboard.perMeal'), sectionX + 130, headerY)

    doc.setFontSize(10.5)
    doc.setFont(PDF_FONT_FAMILY, 'normal')
    let currentRowY = headerY + lineHeight
    nutritionRows.forEach((row) => {
      doc.text(row.label, sectionX + 5, currentRowY)
      doc.setFont(PDF_FONT_FAMILY, 'bold')
      doc.text(row.daily, sectionX + 85, currentRowY)
      doc.text(row.perMeal, sectionX + 130, currentRowY)
      doc.setFont(PDF_FONT_FAMILY, 'normal')
      currentRowY += lineHeight
    })

    const chartY = currentRowY + 8
    doc.setFont(PDF_FONT_FAMILY, 'bold')
    doc.setFontSize(11)
    doc.text(translate('dashboard.macroBreakdown'), sectionX + 5, chartY)

    doc.setFontSize(9.5)
    doc.setFont(PDF_FONT_FAMILY, 'normal')
    const macroSummary = translate('dashboard.macroSummary')
    doc.text(macroSummary, sectionX + 5, chartY + 7, { maxWidth: sectionWidth - 10 })

    const chartStartY = chartY + 16
    drawMacroChart(doc, macros, sectionX + 5, chartStartY, sectionWidth - 50, {
      emptyMessage: translate('dashboard.macroDataMissing'),
      textColor
    })
  })

  const filename = `profile-${today.replace(/\//g, '-')}.pdf`
  doc.save(filename)
}

