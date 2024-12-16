---
sidebar_position: 1
---

# Boilerplate

This is example boilerplate for a markdown file. It includes examples of headings, paragraphs, blockquotes, lists, and inline code.

# Heading 1
## Heading 2
### Heading 3
#### Heading 4
##### Heading 5
###### Heading 6

This is a paragraph with **bold text** and *italic text*, as well as ***bold and italic text***.

> Blockquote example.
> 
> > Nested quote inside.

### Example blockquote

> Mollit irure magna Lorem sunt tempor ea sit. Qui et consectetur voluptate magna mollit minim veniam anim. Ullamco nisi reprehenderit amet ut irure incididunt et minim. Ea elit fugiat ex commodo laborum laboris ut anim do ad commodo anim. Consequat non magna qui mollit quis deserunt labore.

- Unordered list item 1
- Unordered list item 2
  - Nested unordered item

1. Ordered list item 1
2. Ordered list item 2
   1. Nested ordered item

Here is some `inline code` inside a paragraph.

First Term
: This is the definition of the first term.

Second Term
: This is one definition of the second term.
: This is another definition of the second term.

:::note

Laborum incididunt eiusmod sunt proident. Ea qui ut sint qui in enim aliqua cillum exercitation irure est laborum dolor. Ipsum incididunt consectetur exercitation et consequat proident ad voluptate ipsum. Esse incididunt deserunt qui ea do consectetur labore pariatur.

:::

:::tip

Laboris laborum nostrud non sunt qui minim veniam. Elit sint et officia veniam excepteur non dolore ut. Ex ut proident cupidatat officia elit nulla commodo sunt et ut sint eiusmod tempor eu. Dolore ipsum veniam irure do esse commodo occaecat minim minim quis nostrud eu minim fugiat. Magna exercitation tempor officia pariatur cupidatat est reprehenderit excepteur mollit ad. Fugiat anim minim est ullamco velit enim non mollit. Velit cillum labore excepteur sunt et et.

:::

:::danger Breaking changes in minor versions

Features prefixed by `experimental_` or `unstable_` are subject to changes in **minor versions**, and not considered as [Semantic Versioning breaking changes](/community/release-process).

Features prefixed by `v<MajorVersion>_` (`v6_` `v7_`, etc.) are future flags that are expected to be turned on by default in the next major versions. These are less likely to change, but we keep the possibility to do so.

`future` API breaking changes should be easy to handle, and will be documented in minor/major version blog posts.

:::

```js title="docusaurus.config.js"
export default async function createConfigAsync() {
  return {
    title: 'Docusaurus',
    url: 'https://docusaurus.io',
    // your site config ...
  };
}
```