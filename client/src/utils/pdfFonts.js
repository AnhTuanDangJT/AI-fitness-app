import fontRegularUrl from '../assets/fonts/BeVietnamPro-Regular.ttf?url'
import fontSemiBoldUrl from '../assets/fonts/BeVietnamPro-SemiBold.ttf?url'

const FONT_FAMILY = 'BeVietnamPro'
const fontCache = {}

const arrayBufferToBase64 = (buffer) => {
  let binary = ''
  const bytes = new Uint8Array(buffer)
  const chunkSize = 0x8000

  for (let i = 0; i < bytes.length; i += chunkSize) {
    const subArray = bytes.subarray(i, i + chunkSize)
    binary += String.fromCharCode.apply(null, subArray)
  }

  return btoa(binary)
}

const fetchFontAsBase64 = async (url, cacheKey) => {
  if (fontCache[cacheKey]) {
    return fontCache[cacheKey]
  }

  const response = await fetch(url)
  const buffer = await response.arrayBuffer()
  const base64 = arrayBufferToBase64(buffer)
  fontCache[cacheKey] = base64

  return base64
}

const registerFontVariant = async (doc, { fileName, style, url }) => {
  const base64Data = await fetchFontAsBase64(url, fileName)
  doc.addFileToVFS(fileName, base64Data)
  doc.addFont(fileName, FONT_FAMILY, style)
}

export const ensureVietnameseFonts = async (doc) => {
  await Promise.all([
    registerFontVariant(doc, {
      fileName: 'BeVietnamPro-Regular.ttf',
      style: 'normal',
      url: fontRegularUrl
    }),
    registerFontVariant(doc, {
      fileName: 'BeVietnamPro-SemiBold.ttf',
      style: 'bold',
      url: fontSemiBoldUrl
    })
  ])
}

export const PDF_FONT_FAMILY = FONT_FAMILY



