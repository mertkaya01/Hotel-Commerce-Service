# --- 1. asama: Angular uygulamasini derle ---
FROM node:22-alpine AS build
WORKDIR /app

# bagimliliklari once kur (cache dostu)
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
# production build (environment.production.ts + fileReplacements devreye girer)
RUN npm run build

# --- 2. asama: nginx ile statik dosyalari serve et ---
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
