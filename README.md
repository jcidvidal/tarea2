# Compras por unidad

Implementación del desafío 1: `GET /compras/{unidad}`, con Java 21, Spring Boot 4.1.1 y Maven.

Desde la raíz del proyecto:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Consultas:

```text
GET http://localhost:8080/compras/uf
GET http://localhost:8080/compras/dolar
```

La respuesta contiene `unidad` (`UF` o `USD`), `compras` y `total_pesos`.
Cada compra contiene `id`, `producto`, `monto`, `fecha`, `valor_unidad` y `monto_pesos`.
Se conserva el orden del CSV. Cada monto se redondea al peso más cercano (0,5 hacia arriba)
y el total suma los montos ya redondeados.

El archivo externo `compras.csv` se lee en cada solicitud. Se puede cambiar su ubicación
mediante la variable de entorno `CSV_PATH`. Se usa el formato de cinco columnas del archivo
entregado: `id,producto,monto,unidad,fecha`, con fechas `yyyy-MM-dd` y punto decimal.

El valor histórico se consulta en
`https://mindicador.cl/api/{uf|dolar}/{dd-MM-yyyy}`, según la
[documentación de mindicador.cl](https://mindicador.cl/).
Se consulta la cotización correspondiente a la fecha de cada compra.

El código mantiene la estructura inicial:

- `model/Compras`: representa una fila del CSV mediante un `record`, con fecha `LocalDate`
  y monto `BigDecimal`. Java genera el constructor y los accesores; no necesita setters.
- `configuracion/RestClientConfig`: crea el bean `RestClient` para llamar a la API.
- `service/IndicadorService`: consulta el valor histórico con ese `RestClient`.
- `service/ComprasService`: lee el CSV, filtra por unidad y calcula los montos y el total.
- `controller/ComprasController`: expone `GET /compras/{unidad}`.

- Unidad distinta de `uf` o `dolar`: HTTP 400.
- Sin compras de la unidad: HTTP 200, lista vacía y total 0.
- Sin cotización para la fecha exacta: HTTP 422; no se sustituye por otra fecha.
- Falla de mindicador.cl: HTTP 502.
- Archivo inexistente o malformado: HTTP 500.

Las pruebas usan datos temporales y respuestas HTTP simuladas; no dependen de mindicador.cl.

Si el endpoint responde HTTP 502, revise el campo `message` y la consola de Spring Boot.
La consola registra la causa de la consulta fallida (conexión, tiempo de espera, TLS o error HTTP).
Puede comprobar la API externa consultando directamente
`https://mindicador.cl/api/uf/01-09-2026`. Si esa consulta también falla, se necesita
restablecer la conexión o esperar a que el proveedor vuelva a responder.
