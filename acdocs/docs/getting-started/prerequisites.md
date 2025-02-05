---
sidebar_position: 1
---

# Akka Cache Prerequisites

Before getting started with Akka Cache, ensure that your development environment meets the necessary requirements. Depending on your chosen development path, you may need different tools and dependencies.

## Development Path-Specific Prerequisites
### **1. Local Development Only** (No Akka Account Required)
For offline development, rapid prototyping, and troubleshooting:

- **Akka SDK** – Required for developing Akka-based applications. The Akka SDK GitHub repository can be found here: [Akka SDK](https://github.com/akka/akka-sdk)
  - **Git** – Required to clone the Akka Cache GitHub repository.
  - **curl** – Used for interacting with RESTful APIs.
  - **Java 21+** (Eclipse Adoptium recommended)
  - **Apache Maven 3.9+**

  More detailed developing instructions are available via the [Akka Developing documentation](https://doc.akka.io/java/index.html).
- The **Akka Cache GitHub repository** can be found here: [Akka Cache GitHub](https://github.com/akka-cache/akka-cache). This repo contains:
  - Documentation (`~/acdocs`)
  - Java APIs (`~/backend`)
  - TypeScript SDK (OpenAPI 3-based, requires Node.js)
- **Node.js (v20.9.0+)** – Required if using the TypeScript SDK

### **2. Local Development with Deployment to Akka Platform** (Akka Account Required)
For developers who want to build locally and deploy to Akka Platform:

- **Akka CLI** – Required for deploying and managing applications.
  - Install the Akka CLI:
    ```sh
    curl -sL https://doc.akka.io/install-cli.sh | bash
    ```
  - For permission issues:
    ```sh
    curl -sL https://doc.akka.io/install-cli.sh | bash -s -- --prefix /tmp && \
    sudo mv /tmp/akka /usr/local/bin/akka
    ```

    For additional installation options and manual downloads, visit the [Akka CLI Installation Page](https://doc.akka.io/install-cli).
- **Akka SDK** – Required for developing Akka-based applications.
  - **Java 21+** (Eclipse Adoptium recommended)
  - **Apache Maven 3.9+**
  - **Node.js (v20.9.0+)** – Required if using the TypeScript SDK
- **Docker Engine 27+** – Required for running containerized environments.
- **Akka Account** – Sign up on the [Akka platform](https://console.akka.io/register).

### **3. Deploy Dockerized Akka Cache** (Akka Account Required)

For developers who want to deploy the existing Akka Cache Docker image without local development setup:

- **Akka CLI** – Required for deploying and managing applications.
- **Docker Engine 27+** – To pull and run the container.
- **Akka Account** – Sign up on the [Akka platform](https://console.akka.io/register).

## Next Steps

Once the prerequisites are installed, proceed to the appropriate *Getting Started* guide based on your selected development path.
