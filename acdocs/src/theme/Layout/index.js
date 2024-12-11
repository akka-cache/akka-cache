import React from 'react';
import Layout from '@theme-original/Layout';
import Head from '@docusaurus/Head';

export default function LayoutWrapper(props) {
  return (
    <>
      <Head>
        <meta name="custom-tag" content="My Custom Meta Tag" />
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
        <link href="https://fonts.googleapis.com/css2?family=Instrument+Sans:ital,wdth,wght@0,75..100,400..700;1,75..100,400..700&family=Roboto+Mono:ital,wght@0,400;0,700;1,400;1,700&display=swap" rel="stylesheet" />
      </Head>
      <Layout {...props} />
    </>
  );
}
