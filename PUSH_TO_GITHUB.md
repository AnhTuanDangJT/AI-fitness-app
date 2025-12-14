# 🚀 Push Changes to GitHub - Quick Guide

## ✅ CORS Configuration Updated

**File:** `src/main/java/com/aifitness/config/SecurityConfig.java`

**Allowed Origins:**
- ✅ `http://localhost:3000`
- ✅ `http://localhost:5173`
- ✅ `https://ai-fitness-app-one.vercel.app`

**GitHub Remote:** `https://github.com/AnhTuanDangJT/AI-fitness-app.git`

---

## 📋 Push to GitHub (Choose Method)

### Method 1: Using GitHub Desktop (Easiest)

1. **Open GitHub Desktop**
2. **File** → **Add Local Repository**
3. Select: `C:\Users\Admin\Desktop\AiFitness`
4. You'll see the changes:
   - ✅ `SecurityConfig.java` (Modified)
5. **Commit Message:** `Update CORS configuration to allow ai-fitness-app-one.vercel.app`
6. Click **"Commit to main"**
7. Click **"Push origin"** button

✅ **Done!** Railway will auto-redeploy.

---

### Method 2: Using VS Code

1. **Open VS Code**
2. **File** → **Open Folder** → Select: `C:\Users\Admin\Desktop\AiFitness`
3. Click **Source Control** icon (left sidebar)
4. You'll see changed files:
   - ✅ `SecurityConfig.java`
5. **Commit Message:** `Update CORS configuration to allow ai-fitness-app-one.vercel.app`
6. Click **"Commit"** button (✓)
7. Click **"Sync Changes"** or **"Push"** button

✅ **Done!** Railway will auto-redeploy.

---

### Method 3: Using Git Command Line

**If Git is installed:**

```bash
cd C:\Users\Admin\Desktop\AiFitness

# Add changes
git add src/main/java/com/aifitness/config/SecurityConfig.java

# Commit
git commit -m "Update CORS configuration to allow ai-fitness-app-one.vercel.app"

# Push to GitHub
git push origin main
```

**If asked for credentials:**
- **Username:** Your GitHub username
- **Password:** Use Personal Access Token (not password)
  - Create token: https://github.com/settings/tokens
  - Scope: `repo`
  - Copy token and use as password

---

## ✅ After Pushing to GitHub

**Railway will automatically:**
1. ✅ Detect the push
2. ✅ Rebuild the backend
3. ✅ Deploy with new CORS configuration
4. ✅ Backend will allow requests from `https://ai-fitness-app-one.vercel.app`

**Wait time:** 2-5 minutes for Railway to rebuild

---

## 🔄 Rebuild Frontend on Vercel

After Railway redeploy completes:

### Option 1: Via Vercel Dashboard

1. Go to **https://vercel.com**
2. Login to your account
3. Go to project: **ai-fitness-app-one**
4. Click **"Redeploy"** button (or **"Deploy"**)
5. Wait for deployment (1-3 minutes)

### Option 2: Via Git Push

If frontend code is also in GitHub:
- Push any change to trigger redeploy
- OR just wait - Vercel may auto-redeploy

### Option 3: Via Vercel CLI

```bash
cd C:\Users\Admin\Desktop\AiFitness\client
vercel --prod
```

---

## ✅ Verification

After both deployments complete:

1. **Test Backend:**
   - Go to: `https://web-production-4b668.up.railway.app/api/health`
   - Should return: `{"status": "UP"}`

2. **Test Frontend:**
   - Go to: `https://ai-fitness-app-one.vercel.app`
   - Open browser console (F12)
   - Check for CORS errors (should be none)

3. **Test Integration:**
   - Try signup/login
   - Should work without CORS errors ✅

---

**Status:** ✅ Ready to push!

*Choose your preferred method above and push changes.*


