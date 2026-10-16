# Compras por unidad

API para consultar compras registradas en CSV y convertir sus montos a pesos chilenos usando la cotización histórica de UF o dólar de mindicador.cl.

## Requisitos

- Java 21 y Maven para ejecutar localmente.
- Docker y Docker Compose para ejecutar en contenedor.

## Ejecutar con Docker Compose

Desde la raíz del proyecto:

```powershell
docker compose up --build
```

La API queda disponible en `http://localhost:3001`. Compose monta `compras.csv` desde el host en modo solo lectura; el archivo no se copia a la imagen. Para usar otro archivo sin reconstruir la imagen:

```powershell
$env:CSV_FILE = "./datos/compras.csv"
docker compose up --build
```

Se puede cambiar el puerto publicado y usado por la aplicación con `APP_PORT` (por defecto `3001`). El contenedor ejecuta la aplicación como usuario no root y define `APP_ENV=production`.

Para construir solo la imagen:

```powershell
docker build -t tarea2:local .
```

## Ejecutar localmente con Maven

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Por defecto, Spring Boot escucha en el puerto `3001`. `APP_PORT` cambia el puerto y `CSV_PATH` permite indicar la ruta del archivo CSV.

## Endpoints

```text
GET http://localhost:3001/compras/uf
GET http://localhost:3001/compras/dolar
GET http://localhost:3001/compras/detalle/3
```

El primer endpoint devuelve las compras de UF; el segundo, las compras en USD. Ambos incluyen `unidad`, `compras` y `total_pesos`. Cada compra contiene `id`, `producto`, `monto`, `fecha`, `valor_unidad` y `monto_pesos`. El detalle devuelve esos datos para una compra e incluye su `unidad`.

Las conversiones usan la cotización correspondiente a la fecha de cada compra. Los montos convertidos se redondean al peso más cercano (0,5 hacia arriba) y el total suma los valores ya redondeados. Se conserva el orden del CSV.

El CSV debe tener las columnas `id,producto,monto,unidad,fecha`, con fecha `yyyy-MM-dd` y punto decimal. `compras.csv` está en la raíz del repositorio.

## Pipeline y publicación

GitHub Actions ejecuta los jobs `test`, `build` y `publish` en ese orden. `build` genera el JAR, construye la imagen y comprueba que su tamaño sea menor que 250 MB. `publish` solo se ejecuta al hacer push a `main` y publica en GHCR los tags `latest` y los primeros siete caracteres del SHA del commit.

La primera publicación de un paquete GHCR queda privada por defecto. Después de la primera ejecución exitosa en `main`, cambiar la visibilidad del paquete a **Public** en GitHub: abrir el paquete en el perfil de la cuenta, entrar a **Package settings** y cambiar su visibilidad. GitHub no permite volver a hacerlo privado después de publicar un paquete como público.

## Integrantes y desafíos

| Integrante | Usuario de GitHub | Desafíos realizados |
| --- | --- | --- |
| Juan Pablo Cid | [@jcidvidal](https://github.com/jcidvidal) | 1. Compras por unidad; 4. Pipeline |
| John | [@JohnFernandezR](https://github.com/JohnFernandezR) | 2. Detalle de compra; 3. Docker |
