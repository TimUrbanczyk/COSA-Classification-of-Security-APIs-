# COSA – Classification of Security APIs

![Build](https://github.com/TimUrbanczyk/COSA-Classification-of-Security-APIs-/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

An IntelliJ IDEA plugin that scans Java projects for usages of well-known security libraries, classifies every match into a security category, and surfaces code-quality metrics directly inside the IDE.

<!-- Plugin description -->
COSA (Classification of Security APIs) detects and classifies security-related API calls in Java source code. It maps library namespaces (e.g. `org.springframework.security`, `javax.crypto`) to semantic security categories such as *Cryptography*, *Authentication*, or *Authorization*, and presents the results in a tool window with sortable tables and an overview panel showing Lines of Code, Scattering, and Tangling metrics per security class.
<!-- Plugin description end -->

---

## Features

- **Single-file scan** – classify security API usage in the currently open file.
- **Project-wide scan** – scan every source file in the project at once.
- **HAnS-style annotation** – insert `//&line [Category]` comments directly into source files.
- **Sortable results table** – sort detected matches by security class name or by filename.
- **Security Class Overview** – a dedicated panel showing per-class metrics: Features, LoC, Scattering, and Tangling.
- **Click-to-navigate** – click any row in the results table to jump to the matched line in the editor.
- **20+ built-in library mappings** – covers Spring Security, Bouncy Castle, Apache Shiro, OWASP, Java SE crypto, javax.crypto, javax.ssl, Jasypt, Google Tink, pac4j, and more.

---

## Supported Security Libraries

| Library | Mapping File |
|---|---|
| Spring Security | `spring-security.json` |
| Bouncy Castle | `bouncycastle.json` |
| Apache Shiro | `apache-shiro.json` |
| OWASP AntiSamy | `owasp-AntiSamy.json` |
| OWASP ESAPI | `owasp-espi.json` |
| Google Tink | `google-tink.json` |
| Google Auth | `google-auth.json` |
| Jasypt | `jasypt.json` |
| bcrypt | `bcrypt.json` |
| pac4j | `pac4j.json` |
| Java SE (security) | `java-se.json` |
| javax.crypto | `javax-crypto.json` |
| javax.ssl | `javax-ssl.json` |
| javax.validation | `javax-validation.json` |
| javax.servlet | `javax-servlet.json` |
| Java Servlet | `java-servlet.json` |
| Jakarta RESTful WS | `jakarta-RESTful-web-services.json` |
| Apache Commons | `apache-commons.json` |
| Apache Lucene | `apache-lucene.json` |
| Java Standard Library | `java-standard-library.json` |
| javax.mail | `javax-mail.json` |

---



## Usage

After the plugin is loaded, a **COSA** tool window appears on the right side of the IDE.

### Toolbar buttons

| Button | Action |
|---|---|
| **Mark apis in File** | Scan the currently active editor file and populate the results table. |
| **Mark apis in Projekt** | Scan all source files in the project. |
| **Annotate apis** | Write `//&line [Category]` comments into matched source lines. |
| **Sort by Filename** | Sort the results table by file path. |
| **Sort by SC name** | Sort the results table by security class name. |
| **Clear Table** | Remove all rows from the results table. |
| **Overview** | Open the Security Class Overview dialog. |

### Results table columns

| Column | Description |
|---|---|
| Securityclass | The security category the match belongs to. |
| Filename | The source file containing the match. |
| line/row | The line number of the match. |
| Matched | The PSI element text that triggered the match. |

Click any row to navigate the editor to the matched line.

### Security Class Overview

Click **Overview** after a scan to open a summary dialog:

| Column | Description |
|---|---|
| Security Class | Category name. |
| Features | Feature label (mirrors the category name). |
| LoC | Total number of matched lines across all files. |
| Scattering | Number of distinct files the class appears in. |
| Tangling | Number of other security classes that share at least one file with this class. |

Click **Refresh** inside the dialog to update metrics after running a new scan without closing it.

### Right-click context menu

Right-click on any method call or reference in the editor and choose **Classify API – COSA** to manually classify a single element.

---

## Adding a custom library mapping

Create a JSON file in `src/main/resources/lib-mappings/` following the existing structure:

```json
{
  "name": "my-security-lib",
  "namespace": "com.example.security",
  "categories": ["Authentication"],
  "children": [
    {
      "name": "token",
      "namespace": "com.example.security.token",
      "categories": ["Authentication"],
      "children": []
    }
  ]
}
```

The plugin loads all JSON files from that directory automatically on startup.

---

## Project Structure

```
src/main/java/
  gui/                  # Tool window, pop-ups, and actions
  scanner/              # PSI-based API locator (MappingLocator)
  data/                 # JSON loader and MappingNode model
  SecurityClass/        # SecurityClass model and SecurityclassUtils registry
  psi/                  # PSI helper utilities
src/main/resources/
  lib-mappings/         # JSON library-to-category mapping files
  META-INF/plugin.xml   # Plugin descriptor
```

---

## Tech Stack

- **Language:** Java 17
- **Build:** Gradle with IntelliJ Platform Gradle Plugin
- **UI:** IntelliJ Platform Swing components (`JBTable`, `JBScrollPane`)
- **PSI:** IntelliJ Program Structure Interface for AST traversal
- **Mapping format:** JSON via Jackson Databind
- **Boilerplate reduction:** Lombok

---

Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
