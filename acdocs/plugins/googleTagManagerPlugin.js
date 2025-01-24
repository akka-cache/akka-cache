function GoogleTagManagerPlugin() {
    return {
      name: 'google-tag-manager-plugin',
      injectHtmlTags() {
        // 1. We inject a <script> tag into the <body> (postBodyTags).
        // 2. The script is "inline" through innerHTML.
        return {
          postBodyTags: [
            {
              tagName: 'script',
              innerHTML: `
                console.log("google-tag-manager.js loaded");
                (function(w, d, s, l, i){
                  w[l] = w[l] || [];
                  w[l].push({'gtm.start': new Date().getTime(), event: 'gtm.js'});
                  var f = d.getElementsByTagName(s)[0],
                    j = d.createElement(s),
                    dl = l != 'dataLayer' ? '&l=' + l : '';
                  j.async = true;
                  j.src = 'https://www.googletagmanager.com/gtm.js?id=' + i + dl;
                  f.parentNode.insertBefore(j, f);
                })(window, document, 'script', 'dataLayer', 'GTM-WL2BG9MS');
              `,
            },
          ],
        };
      },
    };
  }
  
  module.exports = GoogleTagManagerPlugin;
  
  