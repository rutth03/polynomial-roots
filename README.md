# Proyecto: Raíces de Polinomios

> **Estado del Proyecto:** 🛠️ *En Desarrollo (In Progress)*

Aplicación interactiva para el análisis de polinomios y localización de raíces desarrollada para la asignatura **Programación Numérica** (Tecnicatura Universitaria en Programación - Universidad Nacional de Salta).

---

## 📋 Descripción del Proyecto

El objetivo general es implementar un software liviano y modular bajo el patrón **MVC (Modelo - Vista - Controlador)** en **Java** y **JavaFX**. Permite realizar operaciones algebraicas sobre polinomios y aplicar métodos numéricos directos e iterativos para la acotación y cálculo de raíces.

### Módulos y Funcionalidades
* **Operaciones Polinómicas:** Evaluación de polinomios por regla de Horner, división sintética por $(ax \pm b)$ y por cuadráticos $(x^2 + px + q)$.
* **Localización de Raíces:** Determinación de posibles raíces enteras y racionales.
* **Acotación y Aislamiento:** Cotas superiores/inferiores para raíces reales (métodos de Laguerre, Lagrange y Newton - Teorema de Sturm).
* **Localización de Raíces Complejas:** Método de Bairstow.
* **Visualización Gráfica:** Interfaz basada en JavaFX para el trazado de funciones polinómicas y selección de intervalos.

---

## 🛠️ Requisitos del Sistema

* **Java JDK:** Versión **21** (o posterior).
* **Entorno de Desarrollo:** Visual Studio Code (con la extensión *Extension Pack for Java*).
* **JavaFX SDK:** Descarga manual según el sistema operativo.

---

## 🚀 Guía de Instalación y Configuración

Sigue estos pasos para preparar el entorno en tu computadora local:

### 1. Clonar el Repositorio
```bash
git clone https://github.com/rutth03/polynomial-roots-java.git
cd polynomial-roots-java
```

### 2. Descargar JavaFX SDK
1. Ingresa a la página oficial de descargas: https://gluonhq.com/products/javafx/.
2. Filtra las opciones de descarga con los siguientes parámetros:
    * Versión: Selecciona 21 (LTS) — o una versión compatible si tu versión de Java JDK instalada es posterior.
    * Operating System: Elige tu sistema operativo (Windows, macOS o Linux).
    * Architecture: x64 (o aarch64 si usas Mac con chip Apple Silicon M1/M2/M3).
    * Type: SDK.
3. Haz clic en 'Download' y descomprime el archivo ZIP descargado.

### 3. Configurar la carpeta lib/
1. Abre la carpeta del SDK de JavaFX que descomprimiste.
2. Entra a la subcarpeta lib/ y copia todos los archivos .jar que contiene (javafx.controls.jar, javafx.graphics.jar, javafx.fxml.jar, etc.).
3. Entra a la subcarpeta bin/ (o lib/ en macOS/Linux) y copia todos los archivos binarios nativos correspondientes a tu sistema:
      * En Windows: Archivos con extensión .dll (glass.dll, prism_d3d.dll, etc.).
      * En macOS: Archivos con extensión .dylib.
      * En Linux: Archivos con extensión .so.
4. Pega todos esos archivos (tanto los .jar como los binarios nativos) dentro de la carpeta lib/ vacía de este proyecto.

---

## ▶️ Pasos para Ejecutar el Proyecto
⚠️ IMPORTANTE: Para que VS Code reconozca la ruta de las librerías nativas y los módulos de JavaFX, NO debes usar el botón de Play de la esquina superior derecha del editor.
1. Abre la carpeta del proyecto en VS Code.
2. En el panel lateral izquierdo, dirígete a la pestaña Run and Debug (Ctrl + Shift + D en Windows/Linux o Cmd + Shift + D en Mac).
3. En el menú desplegable superior, confirma que esté seleccionada la configuración **Ejecutar Aplicación**.
4. Presiona la tecla F5 o el botón verde de Play.
