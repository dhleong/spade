# spade [![Clojars Project](https://img.shields.io/clojars/v/net.dhleong/spade.svg?style=flat)](https://clojars.org/net.dhleong/spade) [![cljdoc](https://cljdoc.org/badge/net.dhleong/spade)](https://cljdoc.org/jump/release/net.dhleong/spade)

*A nice tool to use in the Garden*

## What?

Spade is a lightweight [css-modules][1]-inspired CSS-in-clojurescript library.
It is built on top of [garden][2] for powerful, intuitive, programmatic style
generation.

## How?

Add to whichever dependency manager you prefer, substituting `LATEST-VERSION`
for the one indicated at the top of this file:

```clojure
; leiningen, shadow-cljs.edn, etc:
[net.dhleong/spade "LATEST-VERSION"]

; deps.edn:
{net.dhleong/spade {:mvn/version "LATEST-VERSION"}}
```

### Basic usage

Similar to [Herb][3], Spade leans on functions and macros to allow you to
dynamically generate styles. Where Herb uses macros at the call site, however,
Spade uses them at the declaration site to enable a much richer, more intuitive
syntax:

```clojure
(ns co.serenity
  (:require [spade.core :refer [defclass]]))

(defclass ship-style []
  {:background "#999"}
  [:.wing {:background "#777"}])
```

Notice how we don't have to return a single value from our defclass, but can
instead return multiple statements. The first map will apply to whatever
element gets the class, and the rest are used for the children. The above
would translate naturally to garden syntax as:

```clojure
[:.ship {:background "#999"}
 [:.wing {:background "#777"}]]
```

`defclass` creates a function, which is what that empty vector is for. If you
wanted to be able to dynamically choose the ship's wing colors, you could write:

```clojure
(defclass ship-style [wing-color]
  {:background "#999"}
  [:.wing {:background wing-color}])
```

When you call the function generated by `defclass`, it inserts a new `<style>`
element into `<head>` if necessary, and returns a class name. This name is
globally unique based on the parameters passed, so if you have many ships with
the same wing color, they will all share the same class name and a single
`<style>` declaration.

To use this in reagent, you might do:

```clojure
(defn ship [wing-color]
  [:div {:class (ship-style wing-color)}
   [:div.wing]])
```

Since this pattern is quite common, Spade comes with a convenience:

```clojure
(ns co.serenity
  (:require [spade.core :refer [defattrs]]))

; defattrs is identical in syntax to defclass, but returns an attributes
; map for use in hiccup-based frameworks like reagent
(defattrs ship-attrs [wing-color]
  {:background "#999"}
  [:.wing {:background wing-color}])

(defn ship [wing-color]
  [:div (ship-attrs wing-color)
   [:div.wing]])
```

### Global styles

Sometimes you need to create global styles. No problem!

```clojure
(ns co.serenity
  (:require [spade.core :refer [defglobal]]))

(defglobal window-styles
 [:body {:background "#333"}])
```

Global styles still require a unique name for hotswapping purposes, but cannot
accept parameters. They are inserted into the DOM as soon as they are
evaluated.

### Style Composition

Spade supports composing styles just like css-modules:

```clojure
(defclass stealth-ship []
  {:composes [(ship-style "#111")]
   :background "#111"})
```

The `:composes` key is only supported on the root element of a style. It supports
either a single style name or a collection of style names, as shown above.

### Media queries

Spade supports `@media` queries in the exact same way you see them in the [garden][2] documentation:

```clojure
(defclass carrier-style []
  (at-media {:min-width "750px"}
    {:padding "80px"})
  {:padding "8px"})
```

### Keyframes

Spade even supports generating `@keyframes` just like you'd expect:

```clojure
(ns co.serenity
  (:require [spade.core :refer [defkeyframes]]))

(defkeyframes anim-frames []
  ["0%" {:opacity 0}]
  ["100%" {:opacity 1}])
```

`defkeyframes`, like `defclass`, generates a function that inserts the
appropriate CSS into the DOM on-demand, and returns the animation identifier:

```clojure
(defclass serenity []
  {:animation [[(anim-frames) "560ms" 'ease-in-out]]})
```

### Syntactic Sugar

Spade also provides some extra syntactic sugar, performed at compile time
for "zero-cost" abstractions.

#### CSS Custom Properties

CSS custom properties (AKA variables) can be a convenient way to, for
example, define dynamic theme colors once and reuse them throughout the
codebase. Spade provides some extra sugar to make using them easier and
more idiomatic:

```clojure
(defglobal light-dark
  (at-media {:prefers-color-scheme 'dark}
    [":root" {:theme/*background* "#000"
              :theme/*text* "#E0EBFF"}])
  [":root" {:theme/*background* "#fff"
            :theme/*text* "#000"}])

(defclass page []
  {:background :theme/*background*
   :color :theme/*text*})
```

Notice how our declaration and usage sites are identical, and that
they're "just" normal keywords. However, by using `*earmuffs*` around
the name of the keyword, Spade knows that it is meant to be a variable,
and applies the correct CSS styling based on the position within the
style. This naming was chosen because it is reminiscent of the naming
of dynamic Clojure vars, and because `*` is not valid in a CSS property
name, so the meaning is unambiguous.

Normal keyword namespace semantics apply, so you can expect that
`:theme/*text*` and `:crew.quarters/*text*` will result in distinct
variables. In fact, you don't need any namespace at all; `:*text*` will
also result in a perfectly valid CSS variable, disinct from any of the
other two mentioned above.

The above example will generate the following CSS:

```css
@media (prefers-color-scheme: dark) {
  :root {
    --theme--background: #000;
    --theme--text: #E0EBFF;
  }
}

:root {
  --theme--background: #fff;
  --theme--text: #000;
}

.page {
  background: var(--theme--background);
  color: var(--theme--text);
}
```

Note that [CSS Custom Properties are not supported on all browsers][4], and this syntax compiles to that feature directly without any attempt at backwards compatibility—if CSS Custom Properties are not supported on a browser you are targetting, this syntax will also not be supported.

## Development

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

## License

Copyright © 2019-2024 Daniel Leong

Distributed under the Eclipse Public License either version 1.0

[1]: https://github.com/css-modules/css-modules
[2]: https://github.com/noprompt/garden/
[3]: https://github.com/roosta/herb
[4]: https://caniuse.com/css-variables
