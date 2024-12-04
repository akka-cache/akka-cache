// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when paired with `@ts-check`).
// There are various equivalent ways to declare your Docusaurus config.
// See: https://docusaurus.io/docs/api/docusaurus-config

import {themes as prismThemes} from 'prism-react-renderer';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'AkkaCache.io',
  tagline: 'Akka.io is cool',
  favicon: 'img/akka_favicon.png',

  // Set the production url of your site here
  // TODO: this needs to be updated
  url: 'https://your-docusaurus-site.example.com',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  // baseUrl: '/',
  baseUrl: '/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'Akka.io', // Usually your GitHub org/user name.
  projectName: 'AkkaCache.io', // Usually your repo name.

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          routeBasePath: '/', // Serve the docs at the site's root
          sidebarPath: './sidebars.js',
       },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      // Replace with your project's social card
      //image: 'img/docusaurus-social-card.jpg',
      navbar: {
        title: 'AkkaCache.io',
        // logo: {
        //   alt: 'AkkaCache.io Logo',
        //   src: 'img/logo.svg',
        // },
        items: [
         {
            type: 'docSidebar',
            sidebarId: 'cacheSidebar',
            position: 'left',
            label: 'Cache',
          },
          {
            href: 'https://github.com/lightbend/akka-cache',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'AkkaCache.io',
            items: [
              {
                label: 'Cache',
                to: '/',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'Akka.io',
                href: 'https://akka.io',
              },
              {
                label: 'Bluesky',
                href: 'https://bsky.app/profile/akka.io',
              },
              {
                label: 'X',
                href: 'https://x.com/akka_io_',
              },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'GitHub',
                href: 'https://github.com/lightbend/akka-cache',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Akka.io.`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
      },
    }),
};

export default config;