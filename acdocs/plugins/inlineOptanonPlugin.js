function InlineOptanonPlugin() {
    return {
      name: 'inline-optanon-plugin',
      injectHtmlTags() {
        return {
          postBodyTags: [
            {
              tagName: 'script',
              // inline code in `innerHTML`
              innerHTML: `
                console.log('optanon-wrapper.js loaded');
                window.OptanonWrapper = function() {
                  console.log('OptanonWrapper called');
                };
              `,
            },
          ],
        };
      },
    };
  }
  
  module.exports = InlineOptanonPlugin;
  