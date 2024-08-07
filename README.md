# Image2PDF Backend

**Image2PDF Backend** es una API construida con **Spring Boot WebFlux** que proporciona servicios para la compresión de imágenes y su conversión en documentos PDF personalizados. Este backend es consumido por la aplicación principal para procesar imágenes y generar PDFs con opciones personalizables.

## Repositorios Relacionados

- **[Repositorio Principal](https://github.com/NickRayan1965/image2pdf)**: Este es el proyecto principal, contiene lo relacionado a la implementación completa.
- **[Repositorio Frontend](https://github.com/tu-usuario/repositorio-frontend)**: Interfaz de usuario para interactuar con la API de Image2PDF Backend.

## Endpoints del Backend

### 1. Comprimir Imagen

**POST** `/images/compress`

- **Descripción**: Comprime una imagen individual y devuelve la imagen comprimida.
- **Cuerpo de la Solicitud**:
  - `file`: Imagen a comprimir (tipo `multipart/form-data`).

- **Respuesta**:
  - Imagen comprimida en bytes.

### 2. Comprimir Múltiples Imágenes y Devolver como ZIP

**POST** `/images/compress-many-and-return-as-zip`

- **Descripción**: Comprime varias imágenes y devuelve un archivo ZIP con las imágenes comprimidas.
- **Cuerpo de la Solicitud**:
  - `files`: Imágenes a comprimir (tipo `multipart/form-data`).

- **Respuesta**:
  - Archivo ZIP con las imágenes comprimidas en bytes.

### 3. Convertir Imágenes a PDF

**POST** `/image-pdf/convert-to-pdf`

- **Descripción**: Convierte imágenes en un archivo PDF según las opciones proporcionadas.
- **Cuerpo de la Solicitud**:
  - `options`: Configuración para el PDF, incluyendo diseño y otras opciones (tipo `application/json`).

- **Respuesta**:
  - Archivo PDF generado en bytes.

## Instalación
