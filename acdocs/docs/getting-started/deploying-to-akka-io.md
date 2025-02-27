---
sidebar_position: 2
title: Deploying to Your Akka Account
---

Like all Akka services, the **Akka Cache** service is flexible in how it can be deployed. In this guide, you'll go through the steps to deploy it into your own [akka.io](https://akka.io) account. In order to continue, you'll want to make sure that you've set up all of the [prerequisites](./prerequisites.md), including creating an Akka account.

## Create or Reuse a Project
Every service deployed on the Akka platform exists within a **project**. You can either create a new one or select a pre-existing one. You'll need to use the `akka` CLI to choose the target project for your deployments. For example, to select a project named `cache`, you would enter the following command at a terminal prompt:

```
akka config set project cache
```

Make sure the project name matches exactly what you see in the output of `akka projects list`.


## Deploy the Service
Now that your Akka CLI is configured to point to your desired target project and organization, you're ready to deploy. Deploying a service is done with just one easy command:

```
akka service deploy akka-cache hub.akka.io/akka-cache:latest
```

This will deploy the cache service from its well-known public registry location. If you are deploying your own custom build, you'll want to use the appropriate image tag.

If all goes well, you should see a message indicating that the service has been created:

```
Deployment of service 'akka-cache' initiated, check its status with `akka services list`
```

If you have a browser tab opened to the services list in your Akka project view, you'll immediately see a new box for the Akka cache service appear. A moment later, you'll see the realtime status of that service become _available_. 


You're ready to go! You now have a completely managed, resilient, replicated cache that you were able to deploy into your account without writing a single line of code.

## Internal Endpoint
The **Akka Cache** service is designed to be used _by your components_. In other words, it isn't designed to be exposed to the public as a general-purpose cache, it's designed to be a supporting service for your application/project. As such, its HTTP endpoint should _not_ be exposed to the public and its [ACL](https://doc.akka.io/java/access-control.html#_configuring_acls) configuration will prevent anything from hitting the endpoint that isn't a service in your project.


