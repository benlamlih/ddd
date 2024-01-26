FROM node:16 AS build
WORKDIR /usr/src/app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /usr/src/app/dist/your-angular-app-name /usr/share/nginx/html
