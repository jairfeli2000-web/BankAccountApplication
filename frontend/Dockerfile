# Etapa 1: Compilar el frontend React
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json ./
COPY packages ./packages
COPY src ./src
COPY index.html vite.config.ts tsconfig.json ./
RUN npm install && npm run build

# Etapa 2: Servir archivos estáticos con Nginx
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
