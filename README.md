# TFG-ReservApp

## TFG Ingeniería Informática - Ahmad Mareie Pascual

[![CI](https://github.com/AhmadMarPas/TFG-ReservApp/actions/workflows/maven.yml/badge.svg)](https://github.com/AhmadMarPas/TFG-ReservApp/actions/workflows/maven.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ReservApp&metric=alert_status&token=e4b031bbfe58f2fd43281031c769da93e80c6bd2)](https://sonarcloud.io/summary/overall?id=ReservApp)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ReservApp&metric=security_rating&token=e4b031bbfe58f2fd43281031c769da93e80c6bd2)](https://sonarcloud.io/summary/overall?id=ReservApp)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=ReservApp&metric=reliability_rating&token=e4b031bbfe58f2fd43281031c769da93e80c6bd2)](https://sonarcloud.io/summary/overall?id=ReservApp)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ReservApp&metric=sqale_rating&token=e4b031bbfe58f2fd43281031c769da93e80c6bd2)](https://sonarcloud.io/summary/overall?id=ReservApp)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ReservApp&metric=vulnerabilities&token=e4b031bbfe58f2fd43281031c769da93e80c6bd2)](https://sonarcloud.io/summary/overall?id=ReservApp)

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=ReservApp&metric=ncloc&token=e4b031bbfe58f2fd43281031c769da93e80c6bd2)](https://sonarcloud.io/summary/overall?id=ReservApp)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ReservApp&metric=coverage&token=e4b031bbfe58f2fd43281031c769da93e80c6bd2)](https://sonarcloud.io/summary/overall?id=ReservApp)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ReservApp&metric=bugs&token=e4b031bbfe58f2fd43281031c769da93e80c6bd2)](https://sonarcloud.io/summary/overall?id=ReservApp)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ReservApp&metric=code_smells&token=e4b031bbfe58f2fd43281031c769da93e80c6bd2)](https://sonarcloud.io/summary/overall?id=ReservApp)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=ReservApp&metric=duplicated_lines_density&token=e4b031bbfe58f2fd43281031c769da93e80c6bd2)](https://sonarcloud.io/summary/overall?id=ReservApp)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=ReservApp&metric=sqale_index&token=e4b031bbfe58f2fd43281031c769da93e80c6bd2)](https://sonarcloud.io/summary/overall?id=ReservApp)

![License](https://img.shields.io/badge/license-GNU%20GPL%20v3-blue.svg?style=plastic)
[![GitHub issues](https://img.shields.io/github/issues-closed/AhmadMarPas/TFG-ReservApp)](https://github.com/AhmadMarPas/TFG-ReservApp/issues)
![GitHub Release](https://img.shields.io/github/v/release/AhmadMarPas/TFG-ReservApp?label=Release)
[![Wiki](https://img.shields.io/badge/wiki-available-brightgreen)](https://github.com/AhmadMarPas/TFG-ReservApp/wiki)
[![Zube](https://img.shields.io/badge/zube-managed-blue?logo=zube)](https://zube.io/)

---

Repositorio para la realización del TFG ReservApp.

## 📋 Descripción

ReservApp es una aplicación web diseñada para la gestión eficiente de reservas de salas de reuniones. El proyecto surge de la necesidad de solucionar la problemática de la asignación informal de espacios de trabajo, que a menudo resulta en conflictos, pérdida de tiempo y confusión entre los empleados.

Esta aplicación proporciona una plataforma centralizada donde los usuarios pueden consultar la disponibilidad de las salas en tiempo real, realizar reservas de forma sencilla y gestionar sus modificaciones o cancelaciones, evitando reservas duplicadas o solapadas.

---

## 🎓 Información Académica
Este proyecto forma parte del Trabajo de Fin de Grado (TFG) de Ingeniería Informática bajo la supervisión de:

- **José Manuel Aroca Fernández** <p>
    Departamento de Ingeniería Informática, Universidad de Burgos
    - Contacto: jmafernandez@ubu.es

- **Jesús Manuel Maudes Raedo** <p>
    Departamento de Ingeniería Informática, Universidad de Burgos
    - Contacto: jmaudes@ubu.es
---

## 🚀 Funcionalidades Principales

*   **Gestión de Usuarios:** Sistema de registro y autenticación de usuarios.
*   **Gestión de Salas:** Creación, edición y eliminación de salas de reuniones.
*   **Sistema de Reservas:**
    *   Visualización de la disponibilidad de las salas en un calendario.
    *   Creación, modificación y cancelación de reservas.
    *   Historial de reservas por usuario.
*   **Notificaciones:** Envío de correos electrónicos para confirmar las reservas.
---

## 🛠️ Tecnologías Utilizadas

*   **Backend:** Java 21, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security.
*   **Frontend:** Thymeleaf, Bootstrap 5.
*   **Base de Datos:** MySQL (para producción), H2 (para tests).
*   **Gestión de Dependencias:** Maven.
*   **Otras herramientas:** Lombok.
---

## ⚙️ Cómo Ejecutar el Proyecto

1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/AhmadMarPas/TFG-ReservApp.git
    cd TFG-ReservApp/ReservApp
    ```

2.  **Configurar la base de datos:**
    *   Si se quiere utilizar una base da datos local, será necesario crear una base de datos MySQL.
    *   Modificar las variables de entorno con la configuración de la base de datos en el fichero `.env.dev`.
	*   Se deja el archivo `.dev.prod` para utilizar una base de datos MySQL en Aiven.
	*   Copia el archivo `.env.dev` a `.env` y modifica las variables de entorno con la configuración de tu base de datos (URL, usuario y contraseña).

3.  **Compilar y ejecutar el proyecto:**
    * Se debe tener instalado y configurado un JDK 21 o superior.
    * Si se quiere ejecutar la aplicación contra la base de datos local, se debe ejecutar el siguiente comando:
    ```bash
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
    ```
	* Si se quiere ejecutar la aplicación contra la base de datos de 'prod', se debe ejecutar el siguiente comando:
    ```bash
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
    ```

5.  **Acceder a la aplicación:**
    Abre tu navegador y ve a `http://localhost:8090`.
---

## 📁 Documentación

La documentación completa del proyecto (memoria, anexos, manuales, etc.) se encuentra en la carpeta `documentación`.

---

## ©️ Licencia

Este proyecto está licenciado bajo los términos de la GNU General Public License v3.0. Consulta el archivo [LICENSE](LICENSE) para más detalles o a través del siguiente enlace: [https://www.gnu.org/licenses/gpl-3.0.txt](https://www.gnu.org/licenses/gpl-3.0.txt).
