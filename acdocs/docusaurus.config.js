// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when paired with `@ts-check`).
// There are various equivalent ways to declare your Docusaurus config.
// See: https://docusaurus.io/docs/api/docusaurus-config

import {themes as prismThemes} from 'prism-react-renderer';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const isProd = process.env.NODE_ENV === 'production';
// console.log('isProd:', isProd);

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Akka Cache',
  tagline: 'Akka Cache is an elastic, agile and resilient data accelerator.',
  favicon: 'img/favicon.ico',
  noIndex: true, // Defaults to `false`
  // Set the production url of your site here
  // TODO: this needs to be updated
  url: 'https://your-docusaurus-site.example.com',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  // baseUrl: '/',
  baseUrl: '/cache/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'akka-cache', // Usually your GitHub org/user name.
  projectName: 'akka-cache', // Usually your repo name.

  onBrokenLinks: 'warn',
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
      // @type {import('@docusaurus/preset-classic').Options}
      {
        docs: {
          routeBasePath: '/', // Serve the docs at the site's root
          sidebarPath: './sidebars.js',
       },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      },
    ],
  ],

  scripts: [
    {
      src: 'https://cdn.cookielaw.org/scripttemplates/otSDKStub.js',
      type: 'text/javascript',
      charset: 'UTF-8',
      // Use different data-domain-script for dev vs. production
      'data-domain-script': isProd
        ? '28b912e7-09e9-43d5-91e4-3d1897044004'
        : '28b912e7-09e9-43d5-91e4-3d1897044004-test',
    },
  ],

  plugins: [
    require.resolve('./plugins/inlineOptanonPlugin.js'),
    require.resolve('./plugins/googleTagManagerPlugin.js'),
  ],

  themeConfig:
    // @type {import('@docusaurus/preset-classic').ThemeConfig} {
    ({
      prismThemes: prismThemes,
      // Replace with your project's social card
      //image: 'img/docusaurus-social-card.jpg',
      colorMode: {
        defaultMode: 'dark',
        disableSwitch: false,
        respectPrefersColorScheme: true,
      },
      navbar: {
        title: 'Akka Cache',
        // logo: {
        //   alt: 'AkkaCache Logo',
        //   src: '',
        //   srcDark: '',
        //   href: 'https://akkacache.io',
        //   target: '_self',
        //   width: 150,
        //   className: 'custom-navbar-logo-class',
        // },
        items: [
        //  {
        //     type: 'docSidebar',
        //     sidebarId: 'cacheSidebar',
        //     position: 'right',
        //     label: 'Cache',
        //   },
          // {
          //   href: 'https://github.com/akka-cache/akka-cache',
          //   label: 'GitHub',
          //   position: 'right',
          // },
        ],
      },
      footer: {
        style: 'dark',
        // links: [
        //   {
        //     title: 'Resources',
        //     items: [
        //       {
        //         label: 'Akka.io',
        //         href: 'https://akka.io',
        //       },
        //       {
        //         label: 'Bluesky',
        //         href: 'https://bsky.app/profile/akka.io',
        //       },
        //       {
        //         label: 'X',
        //         href: 'https://x.com/akka_io_',
        //       },
        //     ],
        //   },
        // ],
        copyright: `© ${new Date().getFullYear()} Lightbend Inc dba Akka.io. All rights reserved. | <a href="https://akka.io/legal/terms-of-use" target="_blank">Terms</a> | <a href="https://akka.io/legal/privacy" target="_blank">Privacy Policy</a> | <a class="ot-sdk-show-settings">Cookie Settings</a>`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
      },
    }),
};

export default config;
