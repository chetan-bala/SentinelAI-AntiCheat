# SentinelAI AntiCheat - Deployment Guide

## Free Hosting Setup (Render + Vercel + GitHub)

### Prerequisites
- GitHub account
- Render account (free at render.com)
- Vercel account (free at vercel.com)

---

## 1. BACKEND DEPLOYMENT (Render)

### Step 1: Push to GitHub
```bash
cd SentinelAI-AntiCheat
git init
git add .
git commit -m "Initial commit: SentinelAI AntiCheat"
git remote add origin https://github.com/YOUR_USERNAME/SentinelAI-AntiCheat.git
git push -u origin main
```

### Step 2: Deploy to Render
1. Go to https://render.com and sign in
2. Click "New +" → "Web Service"
3. Connect your GitHub repository
4. Configure:
   - **Name**: sentinelai-backend
   - **Environment**: Node
   - **Build Command**: `npm install`
   - **Start Command**: `npm start`
   - **Plan**: Free

5. Add Environment Variables:
   - `API_KEY`: Generate a secure key (e.g., `openssl rand -hex 32`)
   - `PORT`: 8000 (Render will override this)

6. Click "Create Web Service"
7. Wait for deployment (2-3 minutes)
8. Copy your backend URL: `https://sentinelai-backend.onrender.com`

---

## 2. FRONTEND DEPLOYMENT (Vercel)

### Step 1: Deploy to Vercel
1. Go to https://vercel.com and sign in
2. Click "New Project"
3. Import your GitHub repository
4. Configure:
   - **Root Directory**: `frontend`
   - **Framework Preset**: Create React App
   - **Build Command**: `npm run build`
   - **Output Directory**: `build`

5. Add Environment Variable:
   - `REACT_APP_API_URL`: Your Render backend URL

6. Click "Deploy"
7. Wait for deployment (1-2 minutes)
8. Copy your dashboard URL: `https://sentinelai-dashboard.vercel.app`

---

## 3. PLUGIN SETUP (Minecraft Server)

### Step 1: Build the Plugin
```bash
cd plugin
mvn clean package
```

### Step 2: Install on Server
1. Copy `target/SentinelAI-AntiCheat-1.0.0.jar` to your server's `plugins/` folder
2. Start your Paper server
3. Edit `plugins/SentinelAI/config.yml`:

```yaml
api:
  enabled: true
  url: "https://your-backend.onrender.com"  # Your Render URL
  key: "your-api-key-here"  # Same as RENDER_API_KEY

ai:
  enabled: true
```

4. Restart server or run `/sentinel reload`

---

## 4. ACCESS DASHBOARD

### Get Dashboard URL
```bash
curl https://your-backend.onrender.com/dashboard-info
```

Response:
```json
{
  "dashboardUrl": "https://your-backend.onrender.com/dashboard?token=xxx",
  "message": "Share this URL to access the dashboard (no login required)"
}
```

Share this URL with your team - no passwords needed!

---

## 5. ENVIRONMENT VARIABLES

### Backend (.env on Render)
```
API_KEY=sentinel-secret-key-12345
PORT=8000
```

### Frontend (Vercel)
```
REACT_APP_API_URL=https://sentinelai-backend.onrender.com
```

---

## 6. UPDATING

### Backend
- Push changes to GitHub
- Render auto-deploys (2-3 minutes)

### Frontend
- Push changes to GitHub
- Vercel auto-deploys (1-2 minutes)

### Plugin
- Rebuild with Maven
- Upload new .jar to server
- Restart server

---

## 7. TROUBLESHOOTING

### Backend not connecting?
- Check Render logs
- Verify API_KEY matches in plugin config
- Ensure CORS is enabled

### Frontend not loading?
- Check Vercel build logs
- Verify REACT_APP_API_URL is correct
- Check browser console for errors

### Plugin not flagging?
- Check server logs
- Verify API URL in config.yml
- Run `/sentinel status` in-game

---

## 8. SCALING (When You Grow)

### Free Tier Limits
- **Render**: 750 hours/month, sleeps after 15 min inactivity
- **Vercel**: Unlimited deployments, 100GB bandwidth/month
- **GitHub**: Unlimited public repos

### Upgrade Options
- Render Starter ($7/month): No sleep
- Vercel Pro ($20/month): Advanced features
- MongoDB Atlas: Free 512MB database

---

## 9. SECURITY CHECKLIST

- [ ] Use strong API_KEY (32+ random chars)
- [ ] Keep dashboard URL private (share only with team)
- [ ] Enable Discord webhooks for alerts
- [ ] Regularly update dependencies
- [ ] Monitor Render/Vercel logs

---

## 10. ONE-CLICK DEPLOY

### Backend (Render)
[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/YOUR_USERNAME/SentinelAI-AntiCheat)

### Frontend (Vercel)
[![Deploy to Vercel](https://vercel.com/button)](https://vercel.com/new/clone?repository-url=https://github.com/YOUR_USERNAME/SentinelAI-AntiCheat&root-directory=frontend)

---

**Need Help?**
- Check logs: Render Dashboard → Logs
- Test API: `curl https://your-backend.onrender.com/health`
- Discord: Join our community at discord.gg/sentinelai
